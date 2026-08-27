package com.pjbhva.materialismcore.mixin;

import com.negodya1.vintageimprovements.content.kinetics.helve_hammer.HelveBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(HelveBlockEntity.class)
public abstract class HelveBlockEntityMixin {

    @Inject(method = "acceptOutputs(Ljava/util/List;Z)Z", at = @At("HEAD"))
    private void materialismcore$copyHeatToOutputs(List<ItemStack> outputs, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (!simulate) {
            return;
        }

        HelveBlockEntity self = (HelveBlockEntity) (Object) this;
        SmartInventory input = self.getInputInventory();
        float hottest = 0f;
        for (int i = 0; i < input.getSlots(); i++) {
            hottest = Math.max(hottest, HeatCapability.getTemperature(input.getStackInSlot(i)));
        }
        if (hottest <= 0f) {
            return;
        }

        for (ItemStack output : outputs) {
            HeatCapability.setTemperature(output, hottest);
        }
    }
}
