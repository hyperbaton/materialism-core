package com.pjbhva.materialismcore.registry;

import com.mojang.serialization.Codec;
import com.pjbhva.materialismcore.MaterialismCore;
import com.pjbhva.materialismcore.steam.SteamOutletBuffer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MaterialismCore.MOD_ID);

    public static final Supplier<AttachmentType<SteamOutletBuffer>> STEAM_OUTLET =
            ATTACHMENT_TYPES.register("steam_outlet", () -> AttachmentType.serializable(SteamOutletBuffer::new).build());

    // FE held by a ComputerCraft computer block
    public static final Supplier<AttachmentType<Integer>> COMPUTER_ENERGY =
            ATTACHMENT_TYPES.register("computer_energy", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
}
