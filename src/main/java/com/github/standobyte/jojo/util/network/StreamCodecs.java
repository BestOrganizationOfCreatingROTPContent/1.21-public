package com.github.standobyte.jojo.util.network;

import java.util.OptionalInt;

import com.github.standobyte.jojo.util.java.OptionalFloat;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

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

	public static final StreamCodec<ByteBuf, Vec3> VEC_3D_APPROX = new StreamCodec<ByteBuf, Vec3>() {
		@Override
		public Vec3 decode(ByteBuf buf) {
			return new Vec3(
					buf.readInt() / 8.0, 
					buf.readInt() / 8.0, 
					buf.readInt() / 8.0);
		}

		@Override
		public void encode(ByteBuf buf, Vec3 value) {
			buf.writeInt((int) (value.x * 8.0));
			buf.writeInt((int) (value.y * 8.0));
			buf.writeInt((int) (value.z * 8.0));
		}
	};
}
