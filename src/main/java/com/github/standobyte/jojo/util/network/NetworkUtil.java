package com.github.standobyte.jojo.util.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.JSONUtil;
import com.google.gson.JsonObject;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public class NetworkUtil {
	
	public static Stream<ServerPlayerConnection> getTrackingPlayers(Entity entity) {
		if (entity.level().isClientSide()) {
			throw new IllegalStateException("Cannot get tracking players on the client");
		}
		else if (entity.level().getChunkSource() instanceof ServerChunkCache chunkCache) {
			ChunkMap.TrackedEntity trackedEntity = chunkCache.chunkMap.entityMap.get(entity.getId());
			if (trackedEntity != null) {
				return trackedEntity.seenBy.stream();
			}
		}
		return Stream.empty();
	}
	
	// StreamCodec stuff below
	
	public static <B extends ByteBuf, T> StreamCodec<B, T> nullableCodec(StreamCodec<? super B, T> codec) {
		return new StreamCodec<>() {
			@Override
			public void encode(B buffer, T value) {
				ByteBufCodecs.BOOL.encode(buffer, value != null);
				if (value != null) {
					codec.encode(buffer, value);
				}
			}

			@Override
			public T decode(B buffer) {
				boolean isPresent = ByteBufCodecs.BOOL.decode(buffer);
				return isPresent ? codec.decode(buffer) : null;
			}
		};
	}
	
	public static final StreamCodec<ByteBuf, JsonObject> JSON_OBJECT_CODEC = ByteBufCodecs.STRING_UTF8
			.map(JSONUtil::parse, JsonObject::toString);
	
	public static <B extends ByteBuf, T> StreamCodec<B, Collection<T>> collectionCodec(StreamCodec<? super B, T> elementCodec) {
		return new StreamCodec<>() {
			@Override
			public void encode(B buffer, Collection<T> collection) {
				ByteBufCodecs.VAR_INT.encode(buffer, collection.size());
				for (T element : collection) {
					elementCodec.encode(buffer, element);
				}
			}

			@Override
			public List<T> decode(B buffer) {
				int size = ByteBufCodecs.VAR_INT.decode(buffer);
				if (size <= 0) return Collections.emptyList();
				List<T> list = new ArrayList<>();
				for (int i = 0; i < size; i++) {
					list.add(elementCodec.decode(buffer));
				}
				return list;
			}
		};
	}
	
	public static <B extends ByteBuf, T> StreamCodec<B, T> emptyObjectCodec(Supplier<T> constructor) {
		return new StreamCodec<>() {
			@Override
			public void encode(B buffer, T value) {}

			@Override
			public T decode(B buffer) {
				return constructor.get();
			}
			
		};
	}
	
	public static <B extends ByteBuf, T> StreamCodec<B, T> extraDynamicData(StreamCodec<B, T> codec, BiConsumer<T, B> encodeExtra, BiConsumer<B, T> decodeExtra) {
		return new StreamCodec<>() {
			@Override
			public void encode(B buffer, T value) {
				codec.encode(buffer, value);
				encodeExtra.accept(value, buffer);
			}

			@Override
			public T decode(B buffer) {
				T value = codec.decode(buffer);
				decodeExtra.accept(buffer, value);
				return value;
			}
			
		};
	}
	
	// FriendlyByteBuf stuff
	
	public static <T> void writeOptionally(@Nullable T value, FriendlyByteBuf buf, StreamEncoder<? super FriendlyByteBuf, T> writer) {
		if (value != null) {
			buf.writeBoolean(true);
			writer.encode(buf, value);
		} else {
			buf.writeBoolean(false);
		}
	}
	
	public static <T> Optional<T> readOptional(FriendlyByteBuf buf, StreamDecoder<? super FriendlyByteBuf, T> reader) {
		return buf.readBoolean() ? Optional.of(reader.decode(buf)) : Optional.empty();
	}

}
