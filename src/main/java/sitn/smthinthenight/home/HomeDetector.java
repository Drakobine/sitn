package sitn.smthinthenight.home;

import net.minecraft.block.BedBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class HomeDetector {

    /**
     * Главный метод — используется в NightLogic
     */
    public static boolean isPlayerInHome(ServerPlayerEntity player) {
        PlayerHome home = findHome(player);
        return home != null && home.contains(player.getBlockPos());
    }

    /**
     * Поиск дома от spawn point (кровати)
     */
    private static PlayerHome findHome(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        BlockPos spawn = player.getSpawnPointPosition();
        if (spawn == null) return null;

        // Проверяем, что это реально кровать
        if (!(world.getBlockState(spawn).getBlock() instanceof BedBlock)) {
            return null;
        }

        // Flood-fill по X/Z
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        queue.add(spawn);
        visited.add(spawn);

        int minX = spawn.getX();
        int maxX = spawn.getX();
        int minZ = spawn.getZ();
        int maxZ = spawn.getZ();
        int baseY = spawn.getY();

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();

            for (BlockPos next : new BlockPos[]{
                    pos.north(),
                    pos.south(),
                    pos.east(),
                    pos.west()
            }) {
                if (visited.contains(next)) continue;

                // блок должен быть разрешён правилами
                if (!HomeBlockRules.isAllowedBlock(world, next)) continue;

                // воздух — не часть дома
                if (world.getBlockState(next).isAir()) continue;

                visited.add(next);
                queue.add(next);

                minX = Math.min(minX, next.getX());
                maxX = Math.max(maxX, next.getX());
                minZ = Math.min(minZ, next.getZ());
                maxZ = Math.max(maxZ, next.getZ());
            }
        }

        // Проверка "без дыр": небо не должно быть видно внутри
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos check = new BlockPos(x, baseY + 1, z);
                if (world.isSkyVisible(check)) {
                    return null;
                }
            }
        }

        // Дом найден
        return new PlayerHome(
                new BlockPos(minX, baseY, minZ),
                new BlockPos(maxX, baseY + 5, maxZ)
        );
    }
}
