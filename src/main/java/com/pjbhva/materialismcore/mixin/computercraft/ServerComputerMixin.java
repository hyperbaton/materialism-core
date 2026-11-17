package com.pjbhva.materialismcore.mixin.computercraft;

import com.pjbhva.materialismcore.compat.ComputerPower;
import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.pocket.core.PocketServerComputer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** An unpowered desktop computer won't boot, whether started by hand, redstone or world load. */
@Mixin(value = ServerComputer.class, remap = false)
public abstract class ServerComputerMixin {
    @Inject(method = "turnOn()V", at = @At("HEAD"), cancellable = true)
    private void materialismcore$needsPower(CallbackInfo ci) {
        ServerComputer self = (ServerComputer) (Object) this;
        if (self instanceof PocketServerComputer) {
            return;
        }
        if (self.getLevel().getBlockEntity(self.getPosition()) instanceof ComputerBlockEntity computer
                && computer.getServerComputer() == self && !ComputerPower.canRun(computer)) {
            ci.cancel();
        }
    }
}
