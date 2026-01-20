package sitn.smthinthenight.home;

import com.google.gson.JsonParser;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.resource.Resource;
import net.minecraft.util.math.BlockPos;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class HomeBlockRules {

    private static final Set<Identifier> EXCLUDED = new HashSet<>();

    public static void load(ServerWorld world) {
        try {
            var manager = world.getServer().getResourceManager();

            Optional<Resource> optional = manager.getResource(
                    new Identifier("something-in-the-night", "home_block_exclusions.json")
            );

            if (optional.isEmpty()) {
                System.err.println("[SITN] home_block_exclusions.json not found");
                return;
            }

            Resource resource = optional.get();

            var json = JsonParser.parseReader(
                    new InputStreamReader(resource.getInputStream())
            ).getAsJsonObject();

            json.getAsJsonArray("excluded_blocks").forEach(e ->
                    EXCLUDED.add(new Identifier(e.getAsString()))
            );

            System.out.println("[SITN] Loaded " + EXCLUDED.size() + " home block exclusions");

        } catch (Exception e) {
            System.err.println("[SITN] Failed to load home_block_exclusions.json");
            e.printStackTrace();
        }
    }

    public static boolean isAllowedBlock(ServerWorld world, BlockPos pos) {
        var id = world.getRegistryManager()
                .get(RegistryKeys.BLOCK)
                .getId(world.getBlockState(pos).getBlock());

        return id != null && !EXCLUDED.contains(id);
    }
}
