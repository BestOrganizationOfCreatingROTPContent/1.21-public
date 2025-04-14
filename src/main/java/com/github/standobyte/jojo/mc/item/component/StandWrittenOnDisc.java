package com.github.standobyte.jojo.mc.item.component;

import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public class StandWrittenOnDisc {
	private final StandInstance stand;

	public StandWrittenOnDisc(StandInstance stand) {
		this.stand = stand;
	}
	
	
	public Component getStandName() {
		return stand.getStandName();
	}
	
	public StandInstance copyStandInstance() {
		return stand.copy();
	}
	
	public boolean isValid() {
		return stand != null;
	}
	

	@Override
	public int hashCode() {
		return stand.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof StandWrittenOnDisc other
				&& this.stand.equals(other.stand);
	}
	
	public static final Codec<StandWrittenOnDisc> CODEC = StandInstance.CODEC.xmap(
			StandWrittenOnDisc::new, discData -> discData.stand);
	
	public static final StreamCodec<FriendlyByteBuf, StandWrittenOnDisc> STREAM_CODEC = StandInstance.NETWORK_CODEC.map(
			StandWrittenOnDisc::new, discData -> discData.stand);

}
