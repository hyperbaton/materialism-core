package com.pjbhva.materialismcore.registry;

import com.pjbhva.materialismcore.MaterialismCore;
import com.pjbhva.materialismcore.block.GalvanicCellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MaterialismCore.MOD_ID);

    public static final DeferredBlock<GalvanicCellBlock> GALVANIC_CELL = BLOCKS.register("galvanic_cell",
            () -> new GalvanicCellBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));
}
