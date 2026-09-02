package com.humanoid.horror.registry;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(
                    ForgeRegistries.ITEMS,
                    HumanoidMod.MOD_ID
            );

    public static final RegistryObject<Item> TPBLOCK1 =
            ITEMS.register(
                    "tpblock1",
                    () -> new BlockItem(
                            ModBlocks.TPBLOCK1.get(),
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> TPBLOCK2 =
            ITEMS.register(
                    "tpblock2",
                    () -> new BlockItem(
                            ModBlocks.TPBLOCK2.get(),
                            new Item.Properties()
                    )
            );
}
