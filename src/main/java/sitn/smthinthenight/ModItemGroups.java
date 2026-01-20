package sitn.smthinthenight;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup SITN_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            new Identifier("sitn", "sitn"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.sitn.main_group"))
                    .icon(() -> new ItemStack(ModItems.PSYCHOSIS_DETECTOR))
                    .entries((context, entries) -> {
                        entries.add(ModItems.PSYCHOSIS_DETECTOR);
                        entries.add(ModItems.PSYCHOSIS_TEA);
                        entries.add(ModItems.PSYCHOSIS_TEA_EMPTY);
                        entries.add(ModItems.TEA_BAG);
                        // entries.add(ModItems.ЕЩЁ_ПРЕДМЕТ);
                    })
                    .build()
    );

    public static void register() {}
}