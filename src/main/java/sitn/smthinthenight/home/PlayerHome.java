package sitn.smthinthenight.home;

import net.minecraft.util.math.BlockPos;

public class PlayerHome {

    private final BlockPos min;
    private final BlockPos max;

    public PlayerHome(BlockPos min, BlockPos max) {
        this.min = min;
        this.max = max;
    }

    public boolean containsXZ(BlockPos pos) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }


    public BlockPos getMin() {
        return min;
    }

    public BlockPos getMax() {
        return max;
    }
}
