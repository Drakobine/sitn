package sitn.smthinthenight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SomethingInTheNight implements ModInitializer {

    public static final String MOD_ID = "something-in-the-night";
    public static final Identifier PSYCHOSIS_PACKET =
            new Identifier(MOD_ID, "psychosis_sync");

    @Override
    public void onInitialize() {

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                NightLogic.tick(player); // ⬅ ВСЁ внутри
            }
        });

        ModItems.register();

        ModEffects.register();
    }
}
