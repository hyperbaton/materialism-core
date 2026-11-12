package com.pjbhva.materialismcore.steam;

import com.pjbhva.materialismcore.ModConfigs;
import com.pjbhva.materialismcore.registry.ModAttachments;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

// A steam engine with a fluid pipe where its shaft goes outputs pressurized steam instead of rotation
public final class SteamOutlet {
    private SteamOutlet() {}

    public static BlockPos pipePos(SteamEngineBlockEntity engine) {
        return SteamEngineBlock.getShaftPos(engine.getBlockState(), engine.getBlockPos());
    }

    public static boolean isOutlet(SteamEngineBlockEntity engine) {
        Level level = engine.getLevel();
        if (level == null || engine.getShaft() != null) return false;
        return BlockEntityBehaviour.get(level, pipePos(engine), FluidTransportBehaviour.TYPE) != null;
    }

    // Each engine slot of the boiler turns its share of the water supply into steam
    public static void tick(SteamEngineBlockEntity engine) {
        Level level = engine.getLevel();
        if (level == null || level.isClientSide || !isOutlet(engine)) return;
        FluidTankBlockEntity tank = engine.getTank();
        if (tank == null || tank.boiler == null) return;
        float efficiency = Mth.clamp(tank.boiler.getEngineEfficiency(tank.getTotalTankSize()), 0, 1);
        if (efficiency <= 0) return;
        SteamOutletBuffer buffer = engine.getData(ModAttachments.STEAM_OUTLET);
        if (buffer.add(efficiency * ModConfigs.STEAM_PER_BOILER_LEVEL.get().floatValue())) {
            engine.setChanged();
        }
    }

    @SuppressWarnings("unchecked")
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("create", "steam_engine"));
        if (type == null) return;
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                (BlockEntityType<SteamEngineBlockEntity>) type, SteamOutlet::capability);
    }

    @Nullable
    public static IFluidHandler capability(SteamEngineBlockEntity engine, @Nullable Direction side) {
        if (!isOutlet(engine)) return null;
        BlockPos offset = pipePos(engine).subtract(engine.getBlockPos());
        Direction towardsPipe = Direction.fromDelta(offset.getX(), offset.getY(), offset.getZ());
        if (side != null && side != towardsPipe) return null;
        return new SteamOutletFluidHandler(engine);
    }
}
