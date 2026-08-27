package com.pjbhva.materialismcore.item;

import com.pjbhva.materialismcore.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Reports whatever fluid was last scanned into the item's data component, but never actually
 * drains or fills it
 */
public class FluidScannerFluidHandler implements IFluidHandlerItem {
    private final ItemStack stack;

    public FluidScannerFluidHandler(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getContainer() {
        return stack;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        FluidStack scanned = stack.get(ModDataComponents.SCANNED_FLUID.get());
        return scanned != null ? scanned.copy() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return 1000;
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
        FluidStack scanned = getFluidInTank(0);
        return !scanned.isEmpty() && FluidStack.isSameFluid(scanned, resource) ? scanned : FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack scanned = getFluidInTank(0);
        if (scanned.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(scanned.getFluid(), Math.min(maxDrain, scanned.getAmount()));
    }
}
