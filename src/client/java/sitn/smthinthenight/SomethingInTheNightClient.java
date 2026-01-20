package sitn.smthinthenight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class SomethingInTheNightClient implements ClientModInitializer {

    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 8;

    private static SoundInstance nightSound;
    private static boolean nightPlaying = false;

    private static final Identifier NIGHT_SOUND_ID =
            new Identifier("something-in-the-night", "ambient_night");

    private static final Random RANDOM = Random.create();

    private static boolean wasDetectorEquipped = false;

    private static final Identifier DETECTOR_ON_SOUND =
            new Identifier("something-in-the-night", "psychosis_detector.on");

    private static final Identifier DETECTOR_OFF_SOUND =
            new Identifier("something-in-the-night", "psychosis_detector.off");

    // ===== DETECTOR BOOT =====
    private static boolean detectorBooting = false;
    private static float detectorBootProgress = 0f;
    private static final float DETECTOR_BOOT_SPEED = 1f / 400f; // 20 секунд

    private static final Identifier[] PSYCHOSIS_SOUNDS = new Identifier[] {
            new Identifier("something-in-the-night", "psychosis.animalwalking"),
            new Identifier("something-in-the-night", "psychosis.darkhorror"),
            new Identifier("something-in-the-night", "psychosis.disappear"),
            new Identifier("something-in-the-night", "psychosis.dogbark"),
            new Identifier("something-in-the-night", "psychosis.rustlinggrass"),
            new Identifier("something-in-the-night", "psychosis.rustlinggrass2"),
            new Identifier("something-in-the-night", "psychosis.scaryambience"),
            new Identifier("something-in-the-night", "psychosis.whisper1"),
            new Identifier("something-in-the-night", "psychosis.whisper2"),
            new Identifier("something-in-the-night", "psychosis.whisper3")
    };

    private static int psychosisSoundTimer = -1;
    private static int lastPsychosisSound = -1;

    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(SomethingInTheNightClient::tick);

        ClientPlayNetworking.registerGlobalReceiver(
                SomethingInTheNight.PSYCHOSIS_PACKET,
                (client, handler, buf, responseSender) -> {

                    float psychosis = buf.readFloat();
                    boolean inHome = buf.readBoolean();

                    client.execute(() -> {
                        PsychosisData.setClient(psychosis, inHome);
                    });
                }
        );

        HudRenderCallback.EVENT.register(SomethingInTheNightClient::renderHud);
    }

    /* ================= HUD ================= */

    private static void renderHud(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // нет аксессуара — нет HUD
        if (!AccessoriesUtil.hasPsychosDetector(client.player)) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        /* ================= BOOT TEXT ================= */
        if (detectorBooting) {
            int percentBoot = MathHelper.clamp(
                    (int) (detectorBootProgress * 100f),
                    0,
                    100
            );

            String text = Text
                    .translatable("hud.sitn.boot", percentBoot)
                    .getString();

            int color = 0xFFAAAAAA;

            int textWidth = client.textRenderer.getWidth(text);
            int x = screenWidth - textWidth - 10;
            int y = screenHeight - 48;

            context.drawText(
                    client.textRenderer,
                    text,
                    x,
                    y,
                    color,
                    false
            );
            return;
        }

        /* ================= VERTICAL BAR ================= */

        int barWidth = 8;
        int barHeight = 60;

        int x = screenWidth - barWidth - 10;
        int y = screenHeight - barHeight - 34; // 🔽 реально нижний угол

        float psychosis = PsychosisData.get();
        boolean permanent = PsychosisData.isMax();

        float percent = permanent
                ? 1f
                : MathHelper.clamp(psychosis / PsychosisData.MAX, 0f, 1f);

        int fillHeight = (int) (barHeight * percent);

        // рамка
        context.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0x90000000);
        // фон
        context.fill(x, y, x + barWidth, y + barHeight, 0xFF121212);

        // ===== FILL (BOTTOM → TOP) =====

// 🔴 КРИТИЧЕСКОЕ СОСТОЯНИЕ — 100%
        if (percent >= 1f) {

            int criticalColor = 0xFFAA2222; // плотный красный

            context.fill(
                    x + 1,
                    y + 1,
                    x + barWidth - 1,
                    y + barHeight - 1,
                    criticalColor
            );

        }
// 🌈 Обычный режим — плавный градиент
        else {

            for (int i = 0; i < fillHeight; i++) {

                float globalT = (float) i / barHeight;

                int r, g, b;

                if (globalT < 0.25f) {
                    float t = globalT / 0.25f;
                    r = (int) MathHelper.lerp(t, 80, 220);
                    g = (int) MathHelper.lerp(t, 200, 220);
                    b = (int) MathHelper.lerp(t, 80, 80);
                } else if (globalT < 0.50f) {
                    float t = (globalT - 0.25f) / 0.25f;
                    r = (int) MathHelper.lerp(t, 220, 255);
                    g = (int) MathHelper.lerp(t, 220, 165);
                    b = (int) MathHelper.lerp(t, 80, 60);
                } else if (globalT < 0.75f) {
                    float t = (globalT - 0.50f) / 0.25f;
                    r = (int) MathHelper.lerp(t, 255, 220);
                    g = (int) MathHelper.lerp(t, 165, 60);
                    b = (int) MathHelper.lerp(t, 60, 60);
                } else {
                    float t = (globalT - 0.75f) / 0.25f;
                    r = (int) MathHelper.lerp(t, 220, 180);
                    g = (int) MathHelper.lerp(t, 60, 40);
                    b = (int) MathHelper.lerp(t, 60, 40);
                }

                int color =
                        (0xFF << 24) |
                                (r << 16) |
                                (g << 8) |
                                b;

                context.fill(
                        x + 1,
                        y + barHeight - i - 1,
                        x + barWidth - 1,
                        y + barHeight - i,
                        color
                );
            }
        }


        // лёгкое свечение на уровне
        if (fillHeight > 0) {
            context.fill(
                    x,
                    y + barHeight - fillHeight - 2,
                    x + barWidth,
                    y + barHeight - fillHeight,
                    0x66FFFFFF
            );
        }
    }

    /* ================= TICK ================= */

    private static void tick(MinecraftClient client) {

        if (client.player == null) {
            wasDetectorEquipped = false;
            psychosisSoundTimer = -1;
            lastPsychosisSound = -1;
            stopNight(client);
            return;
        }

        if (client.isPaused()) return;

        /* ================= DETECTOR EQUIP ================= */

        boolean equipped = AccessoriesUtil.hasPsychosDetector(client.player);
        PsychosisData.setDetectorEquippedClient(equipped);

        if (!wasDetectorEquipped && equipped) {
            client.player.playSound(
                    SoundEvent.of(DETECTOR_ON_SOUND),
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
            );

            detectorBooting = true;
            detectorBootProgress = 0f;
        }

        if (wasDetectorEquipped && !equipped) {
            client.player.playSound(
                    SoundEvent.of(DETECTOR_OFF_SOUND),
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
            );

            detectorBooting = false;
            detectorBootProgress = 0f;
        }

        wasDetectorEquipped = equipped;

        /* ================= BOOT PROGRESS ================= */

        if (detectorBooting) {
            detectorBootProgress += DETECTOR_BOOT_SPEED;

            if (detectorBootProgress >= 1f) {
                detectorBootProgress = 1f;
                detectorBooting = false;
            }
        }

        /* ================= PSYCHOSIS SOUNDS ================= */

        float psychosis = PsychosisData.get();
        boolean permanent = PsychosisData.isMax();

        boolean shouldPlayPsychosisSounds = psychosis >= 75f || permanent;

        if (shouldPlayPsychosisSounds) {

            if (psychosisSoundTimer < 0) {
                // первый звук: 10–15 секунд
                psychosisSoundTimer = randomTicks(10, 15);
            } else {
                psychosisSoundTimer--;

                if (psychosisSoundTimer <= 0) {
                    playRandomPsychosisSound(client);

                    // последующие: 15–25 секунд
                    psychosisSoundTimer = randomTicks(15, 25);
                }
            }

        } else {
            // психоз упал — сбрасываем
            psychosisSoundTimer = -1;
            lastPsychosisSound = -1;
        }

        /* ================= NIGHT AMBIENT ================= */

        if (client.world == null ||
                client.world.getRegistryKey() != World.OVERWORLD) {
            stopNight(client);
            return;
        }

        long time = client.world.getTimeOfDay() % 24000;
        boolean night = time >= 13500 && time <= 23100;

        if (night && !nightPlaying) {
            nightSound = new PositionedSoundInstance(
                    NIGHT_SOUND_ID,
                    SoundCategory.AMBIENT,
                    1f,
                    1f,
                    RANDOM,
                    true,
                    0,
                    SoundInstance.AttenuationType.NONE,
                    0, 0, 0,
                    true
            );
            client.getSoundManager().play(nightSound);
            nightPlaying = true;
        }

        if (!night) {
            stopNight(client);
        }

    }


    private static void stopNight(MinecraftClient client) {
        if (nightSound != null) {
            client.getSoundManager().stop(nightSound);
        }
        nightSound = null;
        nightPlaying = false;
    }

    public static boolean isDetectorReady() {
        return !detectorBooting;
    }

    private static int randomTicks(int minSeconds, int maxSeconds) {
        return (minSeconds * 20) + RANDOM.nextInt((maxSeconds - minSeconds + 1) * 20);
    }

    private static void playRandomPsychosisSound(MinecraftClient client) {
        int index;

        do {
            index = RANDOM.nextInt(PSYCHOSIS_SOUNDS.length);
        } while (index == lastPsychosisSound && PSYCHOSIS_SOUNDS.length > 1);

        lastPsychosisSound = index;

        client.getSoundManager().play(
                PositionedSoundInstance.master(
                        SoundEvent.of(PSYCHOSIS_SOUNDS[index]),
                        1.0f
                )
        );
    }

    public static boolean isDetectorEquippedClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null
                && AccessoriesUtil.hasPsychosDetector(client.player);
    }

}
