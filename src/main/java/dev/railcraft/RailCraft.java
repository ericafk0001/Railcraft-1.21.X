package dev.railcraft;

import dev.railcraft.block.ModBlocks;
import dev.railcraft.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RailCraft implements ModInitializer {
    public static final String MOD_ID = "railcraft";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        ModBlocks.initialize();
    }
}
