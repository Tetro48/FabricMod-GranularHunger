package tetro48.system;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ExhaustionUpdatePacket(float exhaustion) implements CustomPayload {
    public static final CustomPayload.Id<ExhaustionUpdatePacket> ID = new CustomPayload.Id<>(GranularHunger.EXHAUSTION_UPDATE_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, ExhaustionUpdatePacket> CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, ExhaustionUpdatePacket::exhaustion, ExhaustionUpdatePacket::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
