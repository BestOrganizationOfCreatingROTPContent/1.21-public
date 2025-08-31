package com.github.standobyte.jojo.util.network;

import java.util.OptionalInt;

import com.github.standobyte.jojo.util.java.OptionalFloat;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class StreamCodecs {

	public static final StreamCodec<ByteBuf, OptionalFloat> OPTIONAL_FLOAT = new StreamCodec<ByteBuf, OptionalFloat>() {
		@Override
		public OptionalFloat decode(ByteBuf buf) {
			return buf.readBoolean() ? OptionalFloat.of(buf.readFloat()) : OptionalFloat.empty();
		}

		@Override
		public void encode(ByteBuf buf, OptionalFloat value) {
			buf.writeBoolean(value.isPresent());
			if (value.isPresent()) {
				buf.writeFloat(value.getAsFloat());
			}
		}
	};
	
	public static final StreamCodec<ByteBuf, OptionalInt> OPTIONAL_INT = new StreamCodec<ByteBuf, OptionalInt>() {
		@Override
		public OptionalInt decode(ByteBuf buf) {
			return buf.readBoolean() ? OptionalInt.of(buf.readInt()) : OptionalInt.empty();
		}

		@Override
		public void encode(ByteBuf buf, OptionalInt value) {
			buf.writeBoolean(value.isPresent());
			if (value.isPresent()) {
				buf.writeInt(value.getAsInt());
			}
		}
	};
}
