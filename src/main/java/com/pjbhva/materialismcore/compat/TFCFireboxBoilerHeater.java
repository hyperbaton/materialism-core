package com.pjbhva.materialismcore.compat;

import com.simibubi.create.api.boiler.BoilerHeater;
import net.dries007.tfc.common.blockentities.FireboxBlockEntity;
import net.dries007.tfc.common.blocks.FireboxBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;

public final class TFCFireboxBoilerHeater {

    private TFCFireboxBoilerHeater() {}

    public static void register() {
        BoilerHeater.REGISTRY.register(TFCBlocks.FIREBOX.get(), (level, pos, state) -> {
            if (!state.getValue(FireboxBlock.LIT)) {
                return BoilerHeater.NO_HEAT;
            }
            if (!(level.getBlockEntity(pos) instanceof FireboxBlockEntity firebox)) {
                return BoilerHeater.NO_HEAT;
            }

            float temperature = firebox.getTemperature();
            if (temperature < 200f) {
                return BoilerHeater.NO_HEAT;
            }
            if (temperature < 600f) {
                return BoilerHeater.PASSIVE_HEAT;
            }
            if (temperature < 1200f) {
                return 1f;
            }
            return 2f;
        });
    }
}
