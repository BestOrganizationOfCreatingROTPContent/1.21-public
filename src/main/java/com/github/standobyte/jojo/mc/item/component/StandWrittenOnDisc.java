package com.github.standobyte.jojo.mc.item.component;

import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class StandWrittenOnDisc {
	public final StandInstance standInstance;

	public StandWrittenOnDisc(StandInstance stand) {
		this.standInstance = stand;
	}
	
	
	public StandInstance copyStandInstance() {
		return standInstance.copy();
	}
	
	public boolean isValid() {
		return standInstance != null;
	}
	

	@Override
	public int hashCode() {
		return standInstance.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof StandWrittenOnDisc other
				&& this.standInstance.equals(other.standInstance);
	}
	
	public static final Codec<StandWrittenOnDisc> CODEC = StandInstance.CODEC.xmap(
			StandWrittenOnDisc::new, discData -> discData.standInstance);
	
	public static final StreamCodec<FriendlyByteBuf, StandWrittenOnDisc> STREAM_CODEC = StandInstance.NETWORK_CODEC.map(
			StandWrittenOnDisc::new, discData -> discData.standInstance);

}
