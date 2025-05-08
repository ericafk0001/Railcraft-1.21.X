package dev.railcraft.item;

import dev.railcraft.RailCraft;
import dev.railcraft.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {


    public static final RegistryKey<ItemGroup> RAILCRAFT_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(RailCraft.MOD_ID, "item_group"));
    public static final ItemGroup RAILCRAFT_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(Items.MINECART))
            .displayName(Text.translatable("itemGroup.railcraft"))
            .build();

    public static final Item LEBRON_PHOTO = registerItem("lebron_photo", Item::new, new Item.Settings());

    public static Item registerItem(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registerKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(RailCraft.MOD_ID, name));
        return Items.register(registerKey, factory, settings);
    }

    public static void registerModItems() {
        Registry.register(Registries.ITEM_GROUP, RAILCRAFT_GROUP_KEY, RAILCRAFT_GROUP);
        ItemGroupEvents.modifyEntriesEvent(RAILCRAFT_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(ModItems.LEBRON_PHOTO);
            itemGroup.add(ModBlocks.WOODEN_RAIL.asItem());
        });
    }

}