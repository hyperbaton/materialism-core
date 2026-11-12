package com.pjbhva.materialismcore;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ModConfigs {
    public static final ModConfigSpec STARTUP_SPEC;
    public static final ModConfigSpec.IntValue COPPER_ELECTRODE_DURABILITY;
    public static final ModConfigSpec.IntValue ZINC_ELECTRODE_DURABILITY;
    public static final ModConfigSpec.IntValue GRAPHITE_ELECTRODE_DURABILITY;

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.DoubleValue STEAM_PER_BOILER_LEVEL;
    public static final ModConfigSpec.DoubleValue EE_WATTS_PER_TFMG_WATT;

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

        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        common.comment("Steam cycle: steam engine outlets").push("steam");
        STEAM_PER_BOILER_LEVEL = common.comment("mB/t of pressurized steam per boiler level an outlet engine draws (Create takes 10 mB/t of water per level)")
                .defineInRange("steamPerBoilerLevel", 10.0, 0.0, 1000.0);
        common.pop();
        common.comment("Electro Energetics converter bridge to TFMG's electrical network").push("electroenergetics");
        EE_WATTS_PER_TFMG_WATT = common.comment("EE watts exchanged per TFMG watt (both networks compute power as V·I, so 1.0 conserves energy)")
                .defineInRange("eeWattsPerTfmgWatt", 1.0, 0.01, 100.0);
        common.pop();
        COMMON_SPEC = common.build();
    }

    private ModConfigs() {}
}
