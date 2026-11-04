package com.pjbhva.materialismcore.mixin;

import com.drmangotea.tfmg.recipes.VatRecipeParams;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.pjbhva.materialismcore.compat.VatRecipeExtras;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets {@code tfmg:vat} recipes carry the optional fields in {@link VatRecipeExtras}, by reading
 * them alongside TFMG's own and syncing them to clients.
 */
@Mixin(VatRecipeParams.class)
public abstract class VatRecipeParamsMixin implements VatRecipeExtras.Holder {
    @Unique
    private VatRecipeExtras materialismcore$extras = VatRecipeExtras.NONE;

    @Override
    public VatRecipeExtras materialismcore$getExtras() {
        return materialismcore$extras;
    }

    @Override
    public void materialismcore$setExtras(VatRecipeExtras extras) {
        materialismcore$extras = extras;
    }

    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;mapCodec(Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;"
            )
    )
    private static MapCodec<VatRecipeParams> materialismcore$readExtras(MapCodec<VatRecipeParams> original) {
        return Codec.mapPair(original, VatRecipeExtras.CODEC).xmap(
                pair -> {
                    ((VatRecipeExtras.Holder) pair.getFirst()).materialismcore$setExtras(pair.getSecond());
                    return pair.getFirst();
                },
                params -> Pair.of(params, ((VatRecipeExtras.Holder) params).materialismcore$getExtras())
        );
    }

    @Inject(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;)V", at = @At("TAIL"))
    private void materialismcore$encodeExtras(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        VatRecipeExtras.STREAM_CODEC.encode(buffer, materialismcore$extras);
    }

    @Inject(method = "decode(Lnet/minecraft/network/RegistryFriendlyByteBuf;)V", at = @At("TAIL"))
    private void materialismcore$decodeExtras(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        materialismcore$extras = VatRecipeExtras.STREAM_CODEC.decode(buffer);
    }
}
