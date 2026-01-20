package sitn.smthinthenight.advancement;

import net.minecraft.advancement.Advancement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SitnAdvancements {

    private static final Identifier PSYCHOSIS_MAX =
            new Identifier("something-in-the-night", "psychosis_max");

    public static void grantPsychosisMax(ServerPlayerEntity player) {
        Advancement adv = player.getServer().getAdvancementLoader().get(PSYCHOSIS_MAX);
        if (adv == null) return;

        var progress = player.getAdvancementTracker().getProgress(adv);
        if (progress.isDone()) return;

        for (String criterion : progress.getUnobtainedCriteria()) {
            player.getAdvancementTracker().grantCriterion(adv, criterion);
        }
    }
}
