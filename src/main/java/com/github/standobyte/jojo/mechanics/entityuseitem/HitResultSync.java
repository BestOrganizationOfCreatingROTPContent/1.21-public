package com.github.standobyte.jojo.mechanics.entityuseitem;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class HitResultSync {
	protected HitResult hitResult;
	protected HitResult.Type type;
	protected Vec3 position;
	protected int entityId;

	public HitResultSync(HitResult hitResult) {
		this.hitResult = hitResult;
		if (hitResult != null) {
			this.type = hitResult.getType();
			this.position = hitResult.getLocation();
			this.entityId = hitResult instanceof EntityHitResult entity ? entity.getEntity().getId() : 0;
		}
	}

	protected HitResultSync(int entityId, Vec3 pos) {
		this.type = HitResult.Type.ENTITY;
		this.position = pos;
		this.entityId = entityId;
	}

	@Nullable
	public HitResult resolveEntity(Level level) {
		if (hitResult == null && this.type == HitResult.Type.ENTITY) {
			Entity entity = level.getEntity(entityId);
			if (entity != null) {
				hitResult = new EntityHitResult(entity, position);
			}
		}
		return hitResult;
	}

	public static final StreamCodec<FriendlyByteBuf, HitResultSync> STREAM_CODEC = new StreamCodec<>() {

		@Override
		public void encode(FriendlyByteBuf buffer, HitResultSync value) {
			NetworkUtil.writeOptionally(value.hitResult, buffer, (buf, target) -> {
				buffer.writeEnum(value.type);
				switch (value.type) {
					case ENTITY -> {
						buf.writeInt(value.entityId);
						buf.writeVec3(value.position);
					}
					default -> {
						buf.writeBlockHitResult((BlockHitResult) value.hitResult);
					}
				}
			});
		}

		@Override
		public HitResultSync decode(FriendlyByteBuf buffer) {
			return NetworkUtil.readOptional(buffer, buf -> {
				HitResult.Type type = buf.readEnum(HitResult.Type.class);
				return switch (type) {
					case ENTITY -> {
						int entityId = buf.readInt();
						Vec3 pos = buf.readVec3();
						yield new HitResultSync(entityId, pos);
					}
					default -> {
						BlockHitResult target = buf.readBlockHitResult();
						yield new HitResultSync(target);
					}
				};
			}).orElse(new HitResultSync(null));
		}
	};
	
}
