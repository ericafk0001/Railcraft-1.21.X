package dev.railcraft.block;

import dev.railcraft.RailCraft;
import dev.railcraft.block.custom.IceRailBlock;
import dev.railcraft.block.custom.LubricatedRailBlock;
import dev.railcraft.block.custom.PoweredIceRailBlock;
import dev.railcraft.block.custom.WoodenRailBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModBlocks {
    public static final Block WOODEN_RAIL = registerBlock("wooden_rail",
            WoodenRailBlock::new,
            AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .requiresTool()
                    .nonOpaque()
                    .noCollision()
                    .sounds(BlockSoundGroup.WOOD),
            true
    );

    public static final Block LUBRICATED_RAIL = registerBlock("lubricated_rail",
            LubricatedRailBlock::new,
            AbstractBlock.Settings.create()
                    .strength(0.8f)
                    .requiresTool()
                    .nonOpaque()
                    .noCollision(),
                    true
    );

    public static final Block ICE_RAIL = registerBlock("ice_rail",
            IceRailBlock::new,
            AbstractBlock.Settings.create()
                    .strength(1.1f)
                    .requiresTool()
                    .nonOpaque()
                    .noCollision()
                    .slipperiness(0.6f),
            true
    );

    public static final Block POWERED_ICE_RAIL = registerBlock("powered_ice_rail",
            PoweredIceRailBlock::new,
            AbstractBlock.Settings.create()
                    .strength(1.1f)
                    .requiresTool()
                    .nonOpaque()
                    .noCollision()
                    .slipperiness(0.6f),
            true
    );


    private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        // Create a registry key for the block
        RegistryKey<Block> blockKey = keyOfBlock(name);
        // Create the block instance
        Block block = blockFactory.apply(settings.registryKey(blockKey));

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            // Items need to be registered with a different type of registry key, but the ID
            // can be the same.
            RegistryKey<Item> itemKey = keyOfItem(name);

            BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey());
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(RailCraft.MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(RailCraft.MOD_ID, name));
    }

    public static void initialize() {
        RailCraft.LOGGER.info("ModBlocks initialized");
    }
}