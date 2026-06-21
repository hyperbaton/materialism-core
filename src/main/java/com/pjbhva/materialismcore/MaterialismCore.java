package com.pjbhva.materialismcore;

import com.pjbhva.materialismcore.registry.ModBlockEntities;
import com.pjbhva.materialismcore.registry.ModBlocks;
import com.pjbhva.materialismcore.registry.ModCreativeTab;
import com.pjbhva.materialismcore.registry.ModDataComponents;
import com.pjbhva.materialismcore.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MaterialismCore.MOD_ID)
public class MaterialismCore {
    public static final String MOD_ID = "materialismcore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public MaterialismCore(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModCreativeTab.CREATIVE_TABS.register(modEventBus);
        LOGGER.info("Materialism Core initializing");
    }
}
