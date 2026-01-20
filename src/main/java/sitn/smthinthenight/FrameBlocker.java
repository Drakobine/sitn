package sitn.smthinthenight;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public class FrameBlocker {

    public static void init() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            // работаем ТОЛЬКО с рамками
            if (!(entity instanceof ItemFrameEntity frame)) {
                return ActionResult.PASS;
            }

            // предмет в руке
            ItemStack held = player.getStackInHand(hand);
            if (!held.isOf(ModItems.PSYCHOSIS_DETECTOR)) {
                return ActionResult.PASS;
            }

            // ❌ запрещаем взаимодействие
            return ActionResult.FAIL;
        });
    }
}
