package com.humanoid.horror.registry;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.block.TPBlock1;
import com.humanoid.horror.block.TPBlock2;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(
                    ForgeRegistries.BLOCKS,
                    HumanoidMod.MOD_ID
            );

    public static final RegistryObject<Block> TPBLOCK1 =
            BLOCKS.register(
                    "tpblock1",
                    () -> new TPBlock1(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(3.5F, 6.0F)
                                    .requiresCorrectToolForDrops()
                    )
            );

    public static final RegistryObject<Block> TPBLOCK2 =
            BLOCKS.register(
                    "tpblock2",
                    () -> new TPBlock2(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(3.5F, 6.0F)
                                    .requiresCorrectToolForDrops()
                    )
            );
}
