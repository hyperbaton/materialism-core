package com.pjbhva.materialismcore.registry;

import com.pjbhva.materialismcore.MaterialismCore;
import com.pjbhva.materialismcore.block.GalvanicCellBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MaterialismCore.MOD_ID);

    public static final Supplier<BlockEntityType<GalvanicCellBlockEntity>> GALVANIC_CELL =
            BLOCK_ENTITIES.register("galvanic_cell",
                    () -> BlockEntityType.Builder.of(GalvanicCellBlockEntity::new, ModBlocks.GALVANIC_CELL.get()).build(null));
}
