package sitn.smthinthenight.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sitn.smthinthenight.ModItems;
import sitn.smthinthenight.PsychosisData;
import net.minecraft.entity.effect.StatusEffectInstance;
import sitn.smthinthenight.ModEffects;

import java.util.List;

public class PsychosisTeaItem extends Item {

    public PsychosisTeaItem(Settings settings) {
        super(settings);
    }

    // ☕ Пить можно всегда
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof PlayerEntity player) {

            float psychosis = PsychosisData.get();
            boolean permanent = PsychosisData.isMax();

            // 🔴 100% — просто лечим
            if (permanent) {
                PsychosisData.drinkTea();
            }
            // 🟢 < 75%
            else if (psychosis < 75f) {
                PsychosisData.drinkTea();

                player.addStatusEffect(
                        new StatusEffectInstance(
                                ModEffects.CALM,
                                9600, // 8 минут
                                0,
                                false,
                                true,
                                true
                        )
                );
            }

            world.playSound(
                    null,
                    player.getBlockPos(),
                    SoundEvents.ENTITY_GENERIC_DRINK,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
            );

            // 🔥 ВАЖНО: возвращаем пустой стакан
            if (!player.getAbilities().creativeMode) {
                return new ItemStack(ModItems.PSYCHOSIS_TEA_EMPTY);
            }
        }

        return stack;
    }


    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    @Override
    public void appendTooltip(
            ItemStack stack,
            @Nullable World world,
            List<Text> tooltip,
            TooltipContext context
    ) {
        tooltip.add(
                Text.translatable("item.sitn.psychosis_tea.desc")
                        .formatted(Formatting.GRAY)
        );

        tooltip.add(
                Text.translatable("item.sitn.psychosis_tea.condition")
                        .formatted(Formatting.DARK_GRAY)
        );
    }
}
