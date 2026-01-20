package sitn.smthinthenight;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sitn.smthinthenight.item.PsychosisDetectorItem;
import sitn.smthinthenight.item.PsychosisTeaEmptyItem;
import sitn.smthinthenight.item.PsychosisTeaItem;
import sitn.smthinthenight.item.TeaBagItem;

public class ModItems {

    public static final Item PSYCHOSIS_DETECTOR =
            new PsychosisDetectorItem(new FabricItemSettings().maxCount(1));

    public static final Item PSYCHOSIS_TEA =
            new PsychosisTeaItem(new FabricItemSettings().maxCount(1));

    public static final Item PSYCHOSIS_TEA_EMPTY =
            new PsychosisTeaEmptyItem(new FabricItemSettings().maxCount(1));

    public static final Item TEA_BAG =
            new TeaBagItem(new FabricItemSettings().maxCount(1));

    public static void register() {
        Registry.register(
                Registries.ITEM,
                new Identifier(SomethingInTheNight.MOD_ID, "psychosis_detector"),
                PSYCHOSIS_DETECTOR
        );

        Registry.register(
                Registries.ITEM,
                new Identifier(SomethingInTheNight.MOD_ID, "psychosis_tea"),
                PSYCHOSIS_TEA
        );

        Registry.register(
                Registries.ITEM,
                new Identifier(SomethingInTheNight.MOD_ID, "psychosis_tea_empty"),
                PSYCHOSIS_TEA_EMPTY
        );

        Registry.register(
                Registries.ITEM,
                new Identifier(SomethingInTheNight.MOD_ID, "tea_bag"),
                TEA_BAG
        );
    }
}
