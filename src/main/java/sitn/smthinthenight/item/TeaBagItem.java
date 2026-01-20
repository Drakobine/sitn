package sitn.smthinthenight.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeaBagItem extends Item {

    public TeaBagItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(
            ItemStack stack,
            @Nullable World world,
            List<Text> tooltip,
            TooltipContext context
    )
    {
        tooltip.add(
                Text.translatable("item.sitn.psychosis_tea_bag.desc")
                        .formatted(Formatting.GRAY)
        );

        tooltip.add(
                Text.translatable("item.sitn.psychosis_tea_bag.condition")
                        .formatted(Formatting.DARK_GRAY)
        );
    }
}
