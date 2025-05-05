package dev.railcraft.block;

import dev.railcraft.RailCraft;
import dev.railcraft.block.custom.WoodenRailBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block WOODEN_RAIL = registerBlock("wooden_rail",
            new WoodenRailBlock(
                    AbstractBlock.Settings.create()
                            .strength(0.3f)
                            .requiresTool()
                            .nonOpaque()
                            .sounds(BlockSoundGroup.WOOD)
            )
    );

    private static Block registerBlock(String name, Block block) {
        try {
            Identifier id = Identifier.of("railcraft", name);
            Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
            Block registeredBlock = Registry.register(Registries.BLOCK, id, block);
            RailCraft.LOGGER.info("Registered block: " + id);
            return registeredBlock;
        } catch (Exception e) {
            RailCraft.LOGGER.error("Failed to register block: " + name, e);
            throw e;
        }
    }

    public static void initialize() {
        RailCraft.LOGGER.info("ModBlocks initialized");
    }
}