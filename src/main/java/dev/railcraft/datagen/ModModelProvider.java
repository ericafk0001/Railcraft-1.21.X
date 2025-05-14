package dev.railcraft.datagen;

import dev.railcraft.block.ModBlocks;
import dev.railcraft.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerTurnableRail(ModBlocks.WOODEN_RAIL);
        blockStateModelGenerator.registerStraightRail(ModBlocks.LUBRICATED_RAIL);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.LEBRON_PHOTO, Models.GENERATED);
    }
}
