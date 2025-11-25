package tetro48.system;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ExhaustionUpdatePacket(float exhaustion) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ExhaustionUpdatePacket> ID = new CustomPacketPayload.Type<>(GranularHunger.EXHAUSTION_UPDATE_PACKET_ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, ExhaustionUpdatePacket> CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, ExhaustionUpdatePacket::exhaustion, ExhaustionUpdatePacket::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
