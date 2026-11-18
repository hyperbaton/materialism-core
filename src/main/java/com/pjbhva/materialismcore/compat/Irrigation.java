package com.pjbhva.materialismcore.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pjbhva.materialismcore.ModConfigs;
import com.pjbhva.materialismcore.registry.ModAttachments;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

// Irrigated calendar-tick interval of a soil block, open-ended while its sprinkler keeps running
public record Irrigation(long since, long until, long sprinkler) {
    public static final Irrigation NONE = new Irrigation(0, -1, 0);

    public static final Codec<Irrigation> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.fieldOf("since").forGetter(Irrigation::since),
            Codec.LONG.fieldOf("until").forGetter(Irrigation::until),
            Codec.LONG.optionalFieldOf("sprinkler", 0L).forGetter(Irrigation::sprinkler)
    ).apply(i, Irrigation::new));

    public boolean covers(long calendarTick) {
        return calendarTick >= since && calendarTick <= until;
    }

    // Calendar tick a crop growth query asks about; unset means now
    public static final ThreadLocal<Long> QUERIED_TICK = new ThreadLocal<>();
    private static BlockPos clientPos;
    private static long clientTime = Long.MIN_VALUE;
    private static boolean clientIrrigated;

    // Hydration floor of the soil at pos: the stamp on the server, a running sprinkler nearby on the client
    public static int floor(Level level, BlockPos pos) {
        if (level.isClientSide) {
            if (!pos.equals(clientPos) || level.getGameTime() != clientTime) {
                clientPos = pos.immutable();
                clientTime = level.getGameTime();
                clientIrrigated = ModList.get().isLoaded("solarpunk") && Sprinklers.irrigatedBySprinkler(level, pos);
            }
            return clientIrrigated ? ModConfigs.SPRINKLER_HYDRATION_FLOOR.get() : 0;
        }
        Long tick = QUERIED_TICK.get();
        return floor(level, pos, tick != null ? tick : Calendars.get(level).getCalendarTicks());
    }

    // Hydration floor of the soil at pos at a calendar tick, 0 if it wasn't irrigated then
    public static int floor(Level level, BlockPos pos, long calendarTick) {
        if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof FarmlandBlockEntity soil)
                || !soil.hasData(ModAttachments.IRRIGATION)) {
            return 0;
        }
        Irrigation irrigation = soil.getData(ModAttachments.IRRIGATION);
        boolean wet = calendarTick >= irrigation.since() && (calendarTick <= irrigation.until()
                || ModList.get().isLoaded("solarpunk") && Sprinklers.running(level, BlockPos.of(irrigation.sprinkler())));
        return wet ? ModConfigs.SPRINKLER_HYDRATION_FLOOR.get() : 0;
    }

    // Stamps a spray on the soil; the interval carries on while the soil is still moist or its sprinkler never stopped
    public static void stamp(Level level, FarmlandBlockEntity soil, BlockPos sprinkler, boolean sprinklerWasRunning) {
        long now = Calendars.get(level).getCalendarTicks();
        Irrigation old = soil.hasData(ModAttachments.IRRIGATION) ? soil.getData(ModAttachments.IRRIGATION) : NONE;
        boolean continuing = old.covers(now) || old.until() >= old.since() && old.sprinkler() == sprinkler.asLong() && sprinklerWasRunning;
        long since = continuing ? old.since() : now;
        soil.setData(ModAttachments.IRRIGATION, new Irrigation(since, now + ModConfigs.SPRINKLER_RETENTION_TICKS.get(), sprinkler.asLong()));
        soil.setChanged();
    }
}
