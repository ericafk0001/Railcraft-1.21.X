package dev.railcraft.datagen;

import dev.railcraft.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
        return new RecipeGenerator(registryLookup, exporter) {
            @Override
            public void generate() {
                createShaped(RecipeCategory.TRANSPORTATION, ModBlocks.WOODEN_RAIL, 8)
                        .pattern("# #")
                        .pattern("#/#")
                        .pattern("# #")
                        .input('#', ItemTags.PLANKS)
                        .input('/', Items.STICK)
                        .group("wooden_rail")
                        .criterion(hasItem(Items.STICK), conditionsFromItem(Items.STICK))
                        .criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
                        .offerTo(exporter);

                createShaped(RecipeCategory.TRANSPORTATION, ModBlocks.LUBRICATED_RAIL, 16)
                        .pattern("#@#")
                        .pattern("#/#")
                        .pattern("# #")
                        .input('#', Items.IRON_INGOT)
                        .input('/', Items.STICK)
                        .input('@', Items.HONEYCOMB)
                        .criterion(hasItem(Items.STICK), conditionsFromItem(Items.STICK))
                        .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                        .criterion(hasItem(Items.HONEYCOMB), conditionsFromItem(Items.HONEYCOMB))
                        .offerTo(exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "";
    }
}
