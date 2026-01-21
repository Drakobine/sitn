package sitn.smthinthenight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance.AttenuationType;
import java.util.Random;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SomethingInTheNightClient implements ClientModInitializer {

    private static boolean wasDetectorEquipped = false;

    private static final Identifier DETECTOR_ON_SOUND =
            new Identifier("something-in-the-night", "psychosis_detector.on");
    private static final Identifier DETECTOR_OFF_SOUND =
            new Identifier("something-in-the-night", "psychosis_detector.off");

    private static final Identifier AMBIENT_NIGHT =
            new Identifier("something-in-the-night", "ambient_night");

    private static SoundInstance nightSound;

    private static final long NIGHT_START = 13500;
    private static final long NIGHT_END   = 23100;

    // ===== DETECTOR BOOT =====
    private static boolean detectorBooting = false;
    private static float detectorBootProgress = 0f;
    private static final float DETECTOR_BOOT_SPEED = 1f / 400f;

    private static final Identifier[] PSYCHOSIS_SOUNDS = new Identifier[] {
            new Identifier("something-in-the-night", "animalwalking"),
            new Identifier("something-in-the-night", "darkhorror"),
            new Identifier("something-in-the-night", "disappear"),
            new Identifier("something-in-the-night", "dogbark"),
            new Identifier("something-in-the-night", "rustlinggrass"),
            new Identifier("something-in-the-night", "rustlinggrass2"),
            new Identifier("something-in-the-night", "scaryambience"),
            new Identifier("something-in-the-night", "whisper1"),
            new Identifier("something-in-the-night", "whisper2"),
            new Identifier("something-in-the-night", "whisper3")
    };

    private static final Random RANDOM = new Random();

    private static int nextPsychosisSoundTick = 0;

    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(SomethingInTheNightClient::tick);

        ClientPlayNetworking.registerGlobalReceiver(
                SomethingInTheNight.PSYCHOSIS_PACKET,
                (client, handler, buf, responseSender) -> {
                    float psychosis = buf.readFloat();
                    boolean inHome = buf.readBoolean();
                    client.execute(() -> PsychosisData.setClient(psychosis, inHome));
                }
        );

        HudRenderCallback.EVENT.register(SomethingInTheNightClient::renderHud);
    }

    /* ================= HUD ================= */

    private static void renderHud(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (!AccessoriesUtil.hasPsychosDetector(client.player)) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // ===== BOOT TEXT =====
        if (detectorBooting) {
            int percent = PsychosisData.getDetectorBootPercent();

            String text = Text.translatable("hud.sitn.boot", percent).getString();
            int textWidth = client.textRenderer.getWidth(text);

            context.drawText(
                    client.textRenderer,
                    text,
                    screenWidth - textWidth - 10,
                    screenHeight - 48,
                    0xFFAAAAAA,
                    false
            );
            return;
        }

        // ===== VERTICAL BAR (OLD STYLE) =====

        int barWidth = 8;
        int barHeight = 60;

        int x = screenWidth - barWidth - 10;
        int y = screenHeight - barHeight - 34;

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

        // ===== FILL =====

        // 🔴 100% — критическое состояние
        if (percent >= 1f) {
            context.fill(
                    x + 1,
                    y + 1,
                    x + barWidth - 1,
                    y + barHeight - 1,
                    0xFFAA2222
            );
        }
        // 🌈 Градиент
        else {
            for (int i = 0; i < fillHeight; i++) {

                float t = (float) i / barHeight;

                int r, g, b;

                if (t < 0.25f) {
                    float k = t / 0.25f;
                    r = (int) MathHelper.lerp(k, 80, 220);
                    g = (int) MathHelper.lerp(k, 200, 220);
                    b = 80;
                } else if (t < 0.5f) {
                    float k = (t - 0.25f) / 0.25f;
                    r = (int) MathHelper.lerp(k, 220, 255);
                    g = (int) MathHelper.lerp(k, 220, 165);
                    b = 60;
                } else if (t < 0.75f) {
                    float k = (t - 0.5f) / 0.25f;
                    r = (int) MathHelper.lerp(k, 255, 220);
                    g = (int) MathHelper.lerp(k, 165, 60);
                    b = 60;
                } else {
                    float k = (t - 0.75f) / 0.25f;
                    r = (int) MathHelper.lerp(k, 220, 180);
                    g = (int) MathHelper.lerp(k, 60, 40);
                    b = 40;
                }

                int color = (0xFF << 24) | (r << 16) | (g << 8) | b;

                context.fill(
                        x + 1,
                        y + barHeight - i - 1,
                        x + barWidth - 1,
                        y + barHeight - i,
                        color
                );
            }
        }

        // ===== GLOW =====
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
            return;
        }

        if (client.isPaused()) return;

        boolean equipped = AccessoriesUtil.hasPsychosDetector(client.player);
        PsychosisData.setDetectorEquippedClient(equipped);

        if (!wasDetectorEquipped && equipped) {
            client.player.playSound(SoundEvent.of(DETECTOR_ON_SOUND),
                    SoundCategory.PLAYERS, 1f, 1f);

            detectorBooting = true;
            detectorBootProgress = 0f;

            PsychosisData.setDetectorBootingClient(true);
            PsychosisData.setDetectorBootPercent(0);
        }

        if (wasDetectorEquipped && !equipped) {
            client.player.playSound(SoundEvent.of(DETECTOR_OFF_SOUND),
                    SoundCategory.PLAYERS, 1f, 1f);

            detectorBooting = false;
            detectorBootProgress = 0f;

            PsychosisData.setDetectorBootingClient(false);
            PsychosisData.setDetectorBootPercent(0);
        }

        wasDetectorEquipped = equipped;

        if (detectorBooting) {
            detectorBootProgress += DETECTOR_BOOT_SPEED;

            int percent = MathHelper.clamp(
                    (int) (detectorBootProgress * 100f),
                    0,
                    100
            );
            PsychosisData.setDetectorBootPercent(percent);

            if (detectorBootProgress >= 1f) {
                detectorBootProgress = 1f;
                detectorBooting = false;
                PsychosisData.setDetectorBootingClient(false);
                PsychosisData.setDetectorBootPercent(100);
            }
        }

// ===== NIGHT AMBIENT =====
        if (client.world != null) {
            long time = client.world.getTimeOfDay() % 24000;
            boolean isNight = time >= NIGHT_START && time <= NIGHT_END;

            if (isNight) {
                if (nightSound == null || !client.getSoundManager().isPlaying(nightSound)) {

                    nightSound = new PositionedSoundInstance(
                            SoundEvent.of(AMBIENT_NIGHT).getId(),
                            SoundCategory.AMBIENT,
                            0.8f,                     // volume
                            1.0f,                     // pitch
                            SoundInstance.createRandom(),
                            true,                     // repeat
                            0,                        // repeat delay
                            AttenuationType.NONE,     // не зависит от позиции
                            0, 0, 0,                  // позиция
                            true                      // relative (камера)
                    );

                    client.getSoundManager().play(nightSound);
                }
            } else {
                if (nightSound != null) {
                    client.getSoundManager().stop(nightSound);
                    nightSound = null;
                }
            }
        }

        // ===== RANDOM PSYCHOSIS SOUNDS LOGIC =====
        if (client.player != null && client.world != null && !client.isPaused()) {

            long time = client.world.getTimeOfDay() % 24000;
            boolean isNight = time >= 13500 && time <= 23100;

            float psychosis = PsychosisData.get();
            float psychosisPercent = psychosis / PsychosisData.MAX;

            if (isNight && psychosisPercent >= 0.75f) {

                if (nextPsychosisSoundTick <= 0) {

                    Identifier soundId =
                            PSYCHOSIS_SOUNDS[RANDOM.nextInt(PSYCHOSIS_SOUNDS.length)];

                    client.player.playSound(
                            SoundEvent.of(soundId),
                            SoundCategory.AMBIENT,
                            0.9f,
                            0.9f + RANDOM.nextFloat() * 0.2f
                    );

                    // следующий запуск: 15–25 секунд (300–500 тиков)
                    nextPsychosisSoundTick = 300 + RANDOM.nextInt(201);

                } else {
                    nextPsychosisSoundTick--;
                }

            } else {
                // если условия не выполняются — сбрасываем таймер
                nextPsychosisSoundTick = 0;
            }
        }
    }
}
