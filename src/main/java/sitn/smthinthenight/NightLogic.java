package sitn.smthinthenight;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import sitn.smthinthenight.home.HomeDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NightLogic {

    private static final long NIGHT_START_TICK = 13500;
    private static final double PLAYER_RADIUS = 15.0;

    private static final float PSYCHOSIS_GAIN = 0.515f;
    private static final float PSYCHOSIS_LOSS = 0.51f;

    private static final Map<UUID, PsychosisData> PSYCHOSIS = new HashMap<>();

    public static void tick(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        PSYCHOSIS.computeIfAbsent(player.getUuid(), uuid -> new PsychosisData());

        boolean night = isNight(world);
        boolean hasPlayers = hasNearbyPlayers(player);
        boolean hasLight = hasLightSource(player);
        boolean inHome = HomeDetector.isPlayerInHome(player);

        // 🧪 проверяем ванильный эффект
        boolean hasCalm = player.hasStatusEffect(ModEffects.CALM);

        if (night && !inHome && !hasPlayers && !hasLight) {

            // если НЕТ эффекта — психоз растёт
            if (!hasCalm) {
                PsychosisData.increase(PSYCHOSIS_GAIN);
            }

        } else {
            PsychosisData.decrease(PSYCHOSIS_LOSS);
        }

        syncPsychosis(player, inHome);
    }

    /* ================= CHECKS ================= */

    private static boolean isNight(ServerWorld world) {
        long time = world.getTimeOfDay() % 24000;
        return time >= NIGHT_START_TICK && time <= 23100;
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
        buf.writeFloat(MathHelper.clamp(PsychosisData.get(), 0f, PsychosisData.MAX));
        buf.writeBoolean(inHome);

        ServerPlayNetworking.send(
                player,
                SomethingInTheNight.PSYCHOSIS_PACKET,
                buf
        );
    }
}
