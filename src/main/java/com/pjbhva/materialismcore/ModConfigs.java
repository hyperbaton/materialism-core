package com.pjbhva.materialismcore;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class ModConfigs {
    public static final ModConfigSpec STARTUP_SPEC;
    public static final ModConfigSpec.IntValue COPPER_ELECTRODE_DURABILITY;
    public static final ModConfigSpec.IntValue ZINC_ELECTRODE_DURABILITY;
    public static final ModConfigSpec.IntValue GRAPHITE_ELECTRODE_DURABILITY;

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.DoubleValue STEAM_PER_BOILER_LEVEL;
    public static final ModConfigSpec.DoubleValue EE_WATTS_PER_TFMG_WATT;
    public static final ModConfigSpec.DoubleValue RESISTIVE_HEATER_DEGREES_PER_HEAT;
    public static final ModConfigSpec.DoubleValue RESISTIVE_HEATER_MAX_TEMPERATURE;
    public static final ModConfigSpec.IntValue COMPUTER_ENERGY_PER_TICK;
    public static final ModConfigSpec.IntValue ADVANCED_COMPUTER_ENERGY_PER_TICK;
    public static final ModConfigSpec.IntValue COMPUTER_BUFFER;
    public static final ModConfigSpec.IntValue POCKET_COMPUTER_ENERGY_PER_TICK;
    public static final ModConfigSpec.IntValue ADVANCED_POCKET_COMPUTER_ENERGY_PER_TICK;
    public static final ModConfigSpec.IntValue POCKET_COMPUTER_CAPACITY;
    public static final ModConfigSpec.IntValue ADVANCED_POCKET_COMPUTER_CAPACITY;
    public static final ModConfigSpec.IntValue SPRINKLER_CYCLE_TICKS;
    public static final ModConfigSpec.IntValue SPRINKLER_FLUID_PER_CYCLE;
    public static final ModConfigSpec.IntValue SPRINKLER_RADIUS;
    public static final ModConfigSpec.IntValue SPRINKLER_RICH_BONUS;
    public static final ModConfigSpec.IntValue SPRINKLER_DEPTH;
    public static final ModConfigSpec.IntValue SPRINKLER_HYDRATION_FLOOR;
    public static final ModConfigSpec.IntValue SPRINKLER_RETENTION_TICKS;
    public static final ModConfigSpec.DoubleValue SPRINKLER_PLANTER_WATER;
    public static final ModConfigSpec.ConfigValue<List<? extends Double>> SPRINKLER_FERTILIZER;
    public static final ModConfigSpec.ConfigValue<List<? extends Double>> SPRINKLER_RICH_FERTILIZER;
    public static final ModConfigSpec.BooleanValue SPRINKLER_HERBICIDE;

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
        RESISTIVE_HEATER_DEGREES_PER_HEAT = common.comment("°C a lit resistive heater pushes into the TFC heat consumer above it (a TFMG vat, a crucible) per unit of its heat; 1 heat is about 7.4 kW")
                .defineInRange("resistiveHeaterDegreesPerHeat", 1000.0, 0.0, 10000.0);
        RESISTIVE_HEATER_MAX_TEMPERATURE = common.comment("Highest °C a resistive heater can push, the limit of its heating element")
                .defineInRange("resistiveHeaterMaxTemperature", 1800.0, 0.0, 5000.0);
        common.pop();
        common.comment("ComputerCraft power: computers draw FE while on and shut down without it").push("computers");
        COMPUTER_ENERGY_PER_TICK = common.comment("FE/t a running computer draws from its buffer")
                .defineInRange("computerEnergyPerTick", 20, 0, 100000);
        ADVANCED_COMPUTER_ENERGY_PER_TICK = common.comment("FE/t a running advanced computer draws from its buffer")
                .defineInRange("advancedComputerEnergyPerTick", 40, 0, 100000);
        COMPUTER_BUFFER = common.comment("FE a computer block can hold, filled from any FE source such as an EE converter")
                .defineInRange("computerBuffer", 2000, 1, Integer.MAX_VALUE);
        POCKET_COMPUTER_ENERGY_PER_TICK = common.comment("FE/t a running pocket computer draws from its battery")
                .defineInRange("pocketComputerEnergyPerTick", 5, 0, 100000);
        ADVANCED_POCKET_COMPUTER_ENERGY_PER_TICK = common.comment("FE/t a running advanced pocket computer draws from its battery")
                .defineInRange("advancedPocketComputerEnergyPerTick", 10, 0, 100000);
        POCKET_COMPUTER_CAPACITY = common.comment("FE a pocket computer battery holds; charge it under a New Age energiser")
                .defineInRange("pocketComputerCapacity", 360000, 1, Integer.MAX_VALUE);
        ADVANCED_POCKET_COMPUTER_CAPACITY = common.comment("FE an advanced pocket computer battery holds")
                .defineInRange("advancedPocketComputerCapacity", 720000, 1, Integer.MAX_VALUE);
        common.pop();
        common.comment("Solarpunk kinetic sprinkler: irrigation, fertigation and herbicide over TFC soil").push("sprinkler");
        SPRINKLER_CYCLE_TICKS = common.comment("Ticks between sprays")
                .defineInRange("cycleTicks", 20, 1, 12000);
        SPRINKLER_FLUID_PER_CYCLE = common.comment("mB sprayed per cycle, spread evenly over the covered square")
                .defineInRange("fluidPerCycle", 60, 1, 100000);
        SPRINKLER_RADIUS = common.comment("Horizontal radius of the covered square")
                .defineInRange("radius", 5, 0, 16);
        SPRINKLER_RICH_BONUS = common.comment("Extra radius with rich fertilizer")
                .defineInRange("richFertilizerExtraRadius", 1, 0, 16);
        SPRINKLER_DEPTH = common.comment("Blocks below the sprinkler it reaches")
                .defineInRange("depth", 5, 1, 16);
        SPRINKLER_HYDRATION_FLOOR = common.comment("Lowest hydration % of irrigated farmland")
                .defineInRange("hydrationFloor", 60, 0, 100);
        SPRINKLER_RETENTION_TICKS = common.comment("Ticks soil stays irrigated after its sprinkler stops (runs dry or switches to herbicide); unloading and sleeping never stop it")
                .defineInRange("moistureRetentionTicks", 1200, 20, 240000);
        SPRINKLER_PLANTER_WATER = common.comment("Water added to a Firmalife planter per cycle (a planter holds 1)")
                .defineInRange("planterWaterPerCycle", 0.1, 0.0, 1.0);
        SPRINKLER_FERTILIZER = common.comment("Nitrogen, phosphorus and potassium per 1000 mB of fertilizer landing on a block")
                .defineList("fertilizerNutrients", List.of(0.8, 0.1, 0.8), () -> 0.0, o -> o instanceof Double);
        SPRINKLER_RICH_FERTILIZER = common.comment("Nitrogen, phosphorus and potassium per 1000 mB of rich fertilizer landing on a block")
                .defineList("richFertilizerNutrients", List.of(1.6, 1.6, 1.6), () -> 0.0, o -> o instanceof Double);
        SPRINKLER_HERBICIDE = common.comment("Herbicide fluids (tag materialismcore:herbicides) kill crops, wild plants and grass cover")
                .define("herbicideEnabled", true);
        common.pop();
        COMMON_SPEC = common.build();
    }

    private ModConfigs() {}
}
