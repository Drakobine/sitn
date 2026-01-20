package sitn.smthinthenight.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sitn.smthinthenight.AccessoriesUtil;
import sitn.smthinthenight.PsychosisData;

import java.util.List;

public class PsychosisDetectorItem extends Item {

    public PsychosisDetectorItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(
            ItemStack stack,
            @Nullable World world,
            List<Text> tooltip,
            TooltipContext context
    ) {
        tooltip.add(Text.translatable("item.sitn.psychosis_detector.title").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.sitn.psychosis_detector.desc").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.empty());

        if (!PsychosisData.isDetectorEquippedClient()) {
            tooltip.add(
                    Text.translatable("tooltip.sitn.detector.not_equipped")
                            .formatted(Formatting.DARK_GRAY)
            );
            return;
        }

        float psychosis = PsychosisData.get();
        float ratio = psychosis / PsychosisData.MAX;

        if (psychosis <= 0f) {
            tooltip.add(Text.translatable("tooltip.sitn.state.stable").formatted(Formatting.GREEN));
        } else if (ratio < 0.50f) {
            tooltip.add(Text.translatable("tooltip.sitn.state.anxiety").formatted(Formatting.YELLOW));
        } else if (ratio < 0.75f) {
            tooltip.add(Text.translatable("tooltip.sitn.state.unstable").formatted(Formatting.GOLD));
        } else {
            tooltip.add(Text.translatable("tooltip.sitn.state.critical").formatted(Formatting.RED));
        }
    }
}
