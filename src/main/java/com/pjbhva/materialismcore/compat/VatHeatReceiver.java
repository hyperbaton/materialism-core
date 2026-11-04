package com.pjbhva.materialismcore.compat;

/** Implemented on TFMG's vat block entity by {@code VatBlockEntityMixin}. */
public interface VatHeatReceiver {
    /** Temperature a TFC heat source last pushed into this block, or 0 if it has stopped doing so. */
    float materialismcore$recentHeat(long gameTime);
}
