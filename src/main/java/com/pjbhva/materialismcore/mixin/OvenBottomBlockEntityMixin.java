package com.pjbhva.materialismcore.mixin;

import com.eerussianguy.firmalife.common.blockentities.OvenBottomBlockEntity;
import net.dries007.tfc.common.blockentities.IHeatable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Firmalife's oven already tracks a real temperature and heats items placed in it via TFC's
 * HeatCapability, but never declares {@code IHeatable}
 */
@Mixin(OvenBottomBlockEntity.class)
public abstract class OvenBottomBlockEntityMixin implements IHeatable {
}
