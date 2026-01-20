package sitn.smthinthenight;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sitn.smthinthenight.effect.CalmEffect;

public class ModEffects {

    public static final StatusEffect CALM = new CalmEffect();

    public static void register() {
        Registry.register(
                Registries.STATUS_EFFECT,
                new Identifier(SomethingInTheNight.MOD_ID, "calm"),
                CALM
        );
    }
}
