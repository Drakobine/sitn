package sitn.smthinthenight;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import sitn.smthinthenight.advancement.SitnAdvancements;
import sitn.smthinthenight.home.HomeDetector;

public class NightLogic {

    private static final long NIGHT_START_TICK = 13500;
    private static final long NIGHT_END_TICK = 23100;

    private static final double PLAYER_RADIUS = 15.0;

    // ⚠️ ТУТ ТЫ МОЖЕШЬ ЛЕГКО БАЛАНСИТЬ
    private static final float PSYCHOSIS_GAIN = 5f;
    private static final float PSYCHOSIS_LOSS = 0.025f;

    public static void tick(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        boolean night = isNight(world);
        boolean hasPlayers = hasNearbyPlayers(player);
        boolean hasLight = hasLightSource(player);
        boolean inHome = HomeDetector.isPlayerInHome(player);

        boolean hasCalm = player.hasStatusEffect(ModEffects.CALM);

        if (night && !inHome && !hasPlayers && !hasLight) {

            if (!hasCalm) {
                PsychosisData.increase(PSYCHOSIS_GAIN);
            }

        } else {
            PsychosisData.decrease(PSYCHOSIS_LOSS);
        }

        syncPsychosis(player, inHome);

        if (PsychosisData.isMax()) {
            SitnAdvancements.grantPsychosisMax(player);
        }
    }

    /* ================= CHECKS ================= */

    private static boolean isNight(ServerWorld world) {
        long time = world.getTimeOfDay() % 24000;
        return time >= NIGHT_START_TICK && time <= NIGHT_END_TICK;
    }

    private static boolean hasNearbyPlayers(ServerPlayerEntity player) {
        return player.getWorld().getPlayers().stream()
                .anyMatch(other ->
                        other != player &&
                                other.squaredDistanceTo(player) <= PLAYER_RADIUS * PLAYER_RADIUS
                );
    }

    private static boolean hasLightSource(ServerPlayerEntity player) {
        return isLightItem(player.getMainHandStack().getItem())
                || isLightItem(player.getOffHandStack().getItem());
    }

    private static boolean isLightItem(net.minecraft.item.Item item) {
        return item == Items.TORCH || item == Items.LANTERN;
    }

    /* ================= SYNC ================= */

    private static void syncPsychosis(ServerPlayerEntity player, boolean inHome) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeFloat(
                MathHelper.clamp(
                        PsychosisData.get(),
                        0f,
                        PsychosisData.MAX
                )
        );
        buf.writeBoolean(inHome);

        ServerPlayNetworking.send(
                player,
                SomethingInTheNight.PSYCHOSIS_PACKET,
                buf
        );
    }
}
