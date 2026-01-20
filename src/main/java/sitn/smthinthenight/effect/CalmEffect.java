package sitn.smthinthenight.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class CalmEffect extends StatusEffect {

    public CalmEffect() {
        super(
                StatusEffectCategory.BENEFICIAL, // положительный
                0x5FA8FF // цвет эффекта (спокойный голубой)
        );
    }
}
