package com.pjbhva.materialismcore.mixin.computercraft;

import com.pjbhva.materialismcore.compat.ComputerPower;
import com.pjbhva.materialismcore.registry.ModAttachments;
import dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity;
import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity;
import dan200.computercraft.shared.computer.core.ServerComputer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A running desktop computer draws FE from its buffer every tick and shuts down when it runs out. */
@Mixin(value = AbstractComputerBlockEntity.class, remap = false)
public abstract class AbstractComputerBlockEntityMixin {
    @Inject(method = "serverTick()V", at = @At("TAIL"))
    private void materialismcore$drawPower(CallbackInfo ci) {
        if (!((Object) this instanceof ComputerBlockEntity computer) || !ComputerPower.isPowered(computer.getFamily())) {
            return;
        }
        ServerComputer server = computer.getServerComputer();
        if (server == null || !server.isOn()) {
            return;
        }
        int draw = ComputerPower.desktopDraw(computer.getFamily());
        int stored = ComputerPower.stored(computer);
        if (stored < draw) {
            server.shutdown();
            return;
        }
        computer.setData(ModAttachments.COMPUTER_ENERGY.get(), stored - draw);
        if (computer.getLevel() != null && computer.getLevel().getGameTime() % 20 == 0) {
            computer.setChanged();
        }
    }
}
