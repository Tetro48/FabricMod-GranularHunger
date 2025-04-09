package tetro48.system;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record ExhaustionUpdatePacket(float exhaustion) implements CustomPayload {
    public static final CustomPayload.Id<ExhaustionUpdatePacket> ID = new CustomPayload.Id<>(GranularHunger.EXHAUSTION_UPDATE_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, ExhaustionUpdatePacket> CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, ExhaustionUpdatePacket::exhaustion, ExhaustionUpdatePacket::new);
    // should you need to send more data, add the appropriate record parameters and change your codec:
    // public static final PacketCodec<RegistryByteBuf, ExhaustionUpdatePacket> CODEC = PacketCodec.tuple(
    //         BlockPos.PACKET_CODEC, ExhaustionUpdatePacket::blockPos,
    //         PacketCodecs.INTEGER, ExhaustionUpdatePacket::myInt,
    //         Uuids.PACKET_CODEC, ExhaustionUpdatePacket::myUuid,
    //         ExhaustionUpdatePacket::new
    // );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
