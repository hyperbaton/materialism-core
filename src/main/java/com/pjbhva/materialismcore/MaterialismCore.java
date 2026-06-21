package com.pjbhva.materialismcore;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MaterialismCore.MOD_ID)
public class MaterialismCore {
    public static final String MOD_ID = "materialismcore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public MaterialismCore(IEventBus modEventBus) {
        LOGGER.info("Materialism Core initializing");
    }
}
