package com.chainores.network;

import com.chainores.ChainOresMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record DashC2SPayload() implements CustomPayload {
    public static final CustomPayload.Id<DashC2SPayload> ID = new CustomPayload.Id<>(ChainOresMod.id("dash"));
    public static final PacketCodec<PacketByteBuf, DashC2SPayload> CODEC = CustomPayload.codecOf(
            (value, buf) -> {},
            buf -> new DashC2SPayload()
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
