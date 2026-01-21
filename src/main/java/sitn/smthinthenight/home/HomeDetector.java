package sitn.smthinthenight.home;

import net.minecraft.block.BedBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class HomeDetector {

    // РАДИУС ДОМА ОТ КРОВАТИ
    private static final int HOME_RADIUS_XZ = 12;
    private static final int HOME_RADIUS_Y  = 6;

    public static boolean isPlayerInHome(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        BlockPos spawn = player.getSpawnPointPosition();
        if (spawn == null) return false;

        if (!(world.getBlockState(spawn).getBlock() instanceof BedBlock)) {
            return false;
        }

        BlockPos pos = player.getBlockPos();

        return Math.abs(pos.getX() - spawn.getX()) <= HOME_RADIUS_XZ
                && Math.abs(pos.getZ() - spawn.getZ()) <= HOME_RADIUS_XZ
                && Math.abs(pos.getY() - spawn.getY()) <= HOME_RADIUS_Y;
    }
}
