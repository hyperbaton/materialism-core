package com.pjbhva.materialismcore.steam;

import com.pjbhva.materialismcore.registry.ModAttachments;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

// Drain-only view of a steam outlet's buffer
public record SteamOutletFluidHandler(SteamEngineBlockEntity engine) implements IFluidHandler {

    private SteamOutletBuffer buffer() {
        return engine.getData(ModAttachments.STEAM_OUTLET);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        int amount = buffer().amount();
        return amount > 0 ? new FluidStack(SteamCycle.pressurizedSteam(), amount) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return SteamOutletBuffer.CAPACITY;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !resource.getFluid().isSame(SteamCycle.pressurizedSteam())) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        Fluid steam = SteamCycle.pressurizedSteam();
        int drained = buffer().drain(maxDrain, action.execute());
        if (drained <= 0) return FluidStack.EMPTY;
        if (action.execute()) engine.setChanged();
        return new FluidStack(steam, drained);
    }
}
