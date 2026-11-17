package com.pjbhva.materialismcore.mixin.solarpunk;

import com.pjbhva.materialismcore.compat.Fermenting;
import com.pjbhva.materialismcore.recipe.FermentingInput;
import com.pjbhva.materialismcore.recipe.FermentingRecipe;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import net.succ.solar_punk.block.entity.custom.MultiBlockFluidBE;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The fermentation vat runs materialismcore:fermenting recipes instead of its fixed biofuel and fertilizer batches. */
@Mixin(value = FermentationVatBlockEntity.class, remap = false)
public abstract class FermentationVatBlockEntityMixin extends MultiBlockFluidBE<FermentationVatBlockEntity> {
    @Shadow
    @Final
    public FluidTank waterTank;
    @Shadow
    @Final
    public FluidTank outputTank;
    @Shadow
    @Final
    public ItemStackHandler itemHandler;
    @Shadow
    public int progress;

    @Unique
    private FermentingRecipe materialismcore$recipe;

    private FermentationVatBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, FermentationVatBlockEntity.class);
    }

    @Shadow
    private void setLit(boolean lit) {
        throw new AssertionError();
    }

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void materialismcore$ferment(CallbackInfo ci) {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        ci.cancel();
        if (updateConnectivity) {
            updateConnectivity = false;
            ConnectivityHandler.formMulti((FermentationVatBlockEntity) (Object) this);
        }
        if (!isController()) {
            return;
        }

        FermentingRecipe recipe = null;
        if (width >= Config.fermentationVatMinWidth && height >= Config.fermentationVatMinHeight) {
            recipe = materialismcore$recipe != null
                    && materialismcore$recipe.matches(new FermentingInput(itemHandler.getStackInSlot(0), waterTank.getFluid()), level)
                    ? materialismcore$recipe
                    : Fermenting.find(level, itemHandler.getStackInSlot(0), waterTank.getFluid());
        }
        if (recipe != materialismcore$recipe) {
            materialismcore$recipe = recipe;
            progress = 0;
        }
        int batches = Fermenting.batches(width, height);
        if (recipe == null || !Fermenting.canRun(recipe, batches, itemHandler, waterTank, outputTank)) {
            if (progress > 0) {
                progress = 0;
                setChanged();
                sync();
            }
            setLit(false);
            return;
        }

        setLit(true);
        progress++;
        setChanged();
        if (progress >= recipe.processingTime()) {
            Fermenting.run(recipe, batches, itemHandler, waterTank, outputTank);
            progress = 0;
            sync();
        } else if (progress % 20 == 0) {
            sync();
        }
    }
}
