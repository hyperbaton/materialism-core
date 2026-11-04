package com.pjbhva.materialismcore;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ModConfigs {
    public static final ModConfigSpec STARTUP_SPEC;
    public static final ModConfigSpec.IntValue COPPER_ELECTRODE_DURABILITY;
    public static final ModConfigSpec.IntValue ZINC_ELECTRODE_DURABILITY;
    public static final ModConfigSpec.IntValue GRAPHITE_ELECTRODE_DURABILITY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // Durability is part of the items' default components, so it's needed before other configs load.
        builder.comment("Durability of TFMG's electrodes. Vat recipes wear them with the electrode_wear field.")
                .push("electrodes");
        COPPER_ELECTRODE_DURABILITY = builder.defineInRange("copperElectrodeDurability", 1000, 1, Integer.MAX_VALUE);
        ZINC_ELECTRODE_DURABILITY = builder.defineInRange("zincElectrodeDurability", 1000, 1, Integer.MAX_VALUE);
        GRAPHITE_ELECTRODE_DURABILITY = builder.defineInRange("graphiteElectrodeDurability", 1000, 1, Integer.MAX_VALUE);
        builder.pop();

        STARTUP_SPEC = builder.build();
    }

    private ModConfigs() {}
}
