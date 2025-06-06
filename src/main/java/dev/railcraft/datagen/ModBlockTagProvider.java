package dev.railcraft.datagen;

import dev.railcraft.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(ModBlocks.WOODEN_RAIL);

        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.LUBRICATED_RAIL)
                .add(ModBlocks.ICE_RAIL)
                .add(ModBlocks.POWERED_ICE_RAIL);

        getOrCreateTagBuilder(BlockTags.RAILS)
                .add(ModBlocks.WOODEN_RAIL)
                .add(ModBlocks.LUBRICATED_RAIL)
                .add(ModBlocks.ICE_RAIL)
                .add(ModBlocks.POWERED_ICE_RAIL);
    }
}
