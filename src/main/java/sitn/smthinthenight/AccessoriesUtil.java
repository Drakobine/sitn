package sitn.smthinthenight;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.entity.player.PlayerEntity;

public class AccessoriesUtil {

    public static boolean hasPsychosDetector(PlayerEntity player) {

        var cap = AccessoriesCapability.get(player);
        if (cap == null) return false;

        return cap.isEquipped(ModItems.PSYCHOSIS_DETECTOR);
    }
}
