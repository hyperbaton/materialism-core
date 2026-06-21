package com.pjbhva.materialismcore.registry;

import com.pjbhva.materialismcore.MaterialismCore;
import com.pjbhva.materialismcore.block.GalvanicCellBlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MaterialismCore.MOD_ID);

    public static final DeferredItem<GalvanicCellBlockItem> GALVANIC_CELL = ITEMS.register("galvanic_cell",
            () -> new GalvanicCellBlockItem(ModBlocks.GALVANIC_CELL.get(), new Item.Properties()));
}
