package tetro48.system;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HungerBehaviorUpdatePacket(String enumIdentifier) implements CustomPacketPayload {
	public static final Type<HungerBehaviorUpdatePacket> ID = new Type<>(GranularHunger.HUNGER_BEHAVIOR_UPDATE_PACKET_ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, HungerBehaviorUpdatePacket> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, HungerBehaviorUpdatePacket::enumIdentifier, HungerBehaviorUpdatePacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
