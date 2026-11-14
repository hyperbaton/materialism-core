package com.pjbhva.materialismcore.mixin.electroenergetics;

import com.george_vi.electroenergetics.content.resistive_heater.ResistiveHeaterBlock;
import com.george_vi.electroenergetics.content.resistive_heater.ResistiveHeaterBlockEntity;
import com.pjbhva.materialismcore.ModConfigs;
import net.dries007.tfc.common.component.heat.IHeatConsumer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets the EE resistive heater act as a TFC heat source: while lit, it pushes a temperature that
 * follows its heat into the heat consumer above (a TFMG vat, a crucible...), like a firebox does.
 */
@Mixin(value = ResistiveHeaterBlockEntity.class, remap = false)
public abstract class ResistiveHeaterBlockEntityMixin {
    @Shadow
    public float heat;

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void materialismcore$heatConsumerAbove(CallbackInfo ci) {
        ResistiveHeaterBlockEntity self = (ResistiveHeaterBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = self.getBlockState();
        if (!state.hasProperty(ResistiveHeaterBlock.LIT) || !state.getValue(ResistiveHeaterBlock.LIT) || heat <= 0) {
            return;
        }
        if (level.getBlockEntity(self.getBlockPos().above()) instanceof IHeatConsumer consumer) {
            float temperature = (float) Math.min(heat * ModConfigs.RESISTIVE_HEATER_DEGREES_PER_HEAT.get(),
                    ModConfigs.RESISTIVE_HEATER_MAX_TEMPERATURE.get());
            consumer.setTemperature(temperature);
        }
    }
}
