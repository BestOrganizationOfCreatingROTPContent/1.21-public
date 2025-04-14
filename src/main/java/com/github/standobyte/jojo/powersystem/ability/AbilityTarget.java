package com.github.standobyte.jojo.powersystem.ability;

import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AbilityTarget {
	private final TargetType type;
	private final BlockPos blockPos;
	private final Direction face;
	private Entity entity;
	private final int entityId;
	private final Vec3 targetPos;
	
	public static final AbilityTarget EMPTY = new AbilityTarget();
	
	private AbilityTarget() {
		type = TargetType.EMPTY;
		this.blockPos = null;
		this.face = null;
		this.entity = null;
		this.entityId = -1;
		this.targetPos = null;
	}
	
	public AbilityTarget(@Nonnull BlockPos blockPos, @Nonnull Direction face) {
		type = TargetType.BLOCK;
		this.blockPos = blockPos;
		this.face = face;
		this.entity = null;
		this.entityId = -1;
		this.targetPos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.5);
	}
	
	public AbilityTarget(@Nonnull Entity entity) {
		if (entity != null) {
			type = TargetType.ENTITY;
			this.blockPos = null;
			this.face = null;
			this.entity = entity;
			this.entityId = entity.getId();
			this.targetPos = null;
		}
		else {
			type = TargetType.EMPTY;
			this.blockPos = null;
			this.face = null;
			this.entity = null;
			this.entityId = -1;
			this.targetPos = null;
		}
	}
	
	public AbilityTarget(int entityId, Level world) {
		this(world.getEntity(entityId));
	}
	
	public static AbilityTarget fromHitResult(HitResult result) {
		switch (result.getType()) {
		case BLOCK:
			BlockHitResult blockResult = (BlockHitResult) result;
			return new AbilityTarget(blockResult.getBlockPos(), blockResult.getDirection());
		case ENTITY:
			return new AbilityTarget(((EntityHitResult) result).getEntity());
		default:
			return AbilityTarget.EMPTY;
		}
	}
	
	public TargetType getType() {
		return type;
	}
	
	public BlockPos getBlockPos() {
		return blockPos;
	}
	
	public Direction getFace() {
		return face;
	}

	public Entity getEntity() {
		return entity;
	}

	public Vec3 getTargetPos(boolean targetEntityEyeHeight) {
		return type == TargetType.ENTITY && entity != null ? 
				targetEntityEyeHeight ? entity.getEyePosition(1.0F) : entity.position()
						: targetPos;
	}
	
	public Optional<AABB> getBoundingBox(Level world) {
		AABB aabb = null;
		switch (type) {
		case ENTITY:
			aabb = getEntity().getBoundingBox();
			break;
		case BLOCK:
			BlockState blockState = world.getBlockState(blockPos);
			VoxelShape blockShape = blockState.getShape(world, blockPos);
			if (!blockShape.isEmpty()) {
				aabb = blockShape.bounds().move(blockPos);
			}
			break;
		default:
			break;
		}
		return Optional.ofNullable(aabb);
	}
	
	
	public static final StreamCodec<FriendlyByteBuf, AbilityTarget> NETWORK_CODEC = new StreamCodec<>() {

		@Override
		public AbilityTarget decode(FriendlyByteBuf buffer) {
			TargetType type = buffer.readEnum(TargetType.class);
			return switch (type) {
				case ENTITY -> new AbilityTarget(buffer.readInt());
				case BLOCK -> new AbilityTarget(buffer.readBlockPos(), buffer.readEnum(Direction.class));
				default -> AbilityTarget.EMPTY;
			};
		}

		@Override
		public void encode(FriendlyByteBuf buffer, AbilityTarget value) {
			TargetType type = value.getType();
			buffer.writeEnum(type);
			switch (type) {
				case ENTITY -> {
					buffer.writeInt(value.entityId);
				}
				case BLOCK -> {
					buffer.writeBlockPos(value.getBlockPos());
					buffer.writeEnum(value.getFace());
				}
				default -> {}
			}
		}
	
	};
	
	public static AbilityTarget readFromBuf(FriendlyByteBuf buf, Level clientWorld) {
		AbilityTarget target = NETWORK_CODEC.decode(buf);
		return target.resolveEntityId(clientWorld);
	}
	
	public AbilityTarget resolveEntityId(Level world) {
		if (getType() == TargetType.ENTITY) {
			this.entity = world.getEntity(entityId);
			return this.entity != null ? this : AbilityTarget.EMPTY;
		}
		return this;
	}
	
	public AbilityTarget copy() {
		switch (type) {
		case EMPTY:
			return AbilityTarget.EMPTY;
		case BLOCK:
			return new AbilityTarget(blockPos, face);
		case ENTITY:
			return new AbilityTarget(entityId);
		default:
			return null;
		}
	}
	
	private AbilityTarget(int entityIdOnly) {
		type = TargetType.ENTITY;
		this.blockPos = null;
		this.face = null;
		this.entity = null;
		this.entityId = entityIdOnly;
		this.targetPos = null;
	}
	
	@Override
	public boolean equals(Object object) {
		return this == object || object instanceof AbilityTarget && this.sameTarget((AbilityTarget) object);
	}
	
	public boolean sameTarget(AbilityTarget target) {
		if (target != null && this.type == target.type) {
			switch (type) {
			case BLOCK:
				return this.blockPos.equals(target.blockPos);
			case ENTITY:
				int idThis = this.entity != null ? this.entity.getId() : this.entityId;
				int idThat = target.entity != null ? target.entity.getId() : target.entityId;
				return idThis == idThat;
			default:
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		String str = "ActionTarget";
		switch (type) {
		case EMPTY:
			str += " (EMPTY)";
			break;
		case ENTITY:
			str += " (ENTITY - ";
			str += entity != null ? entity.getName().getString() : "null (uh-oh)";
			str += ")";
			break;
		case BLOCK:
			str += " (BLOCK - ";
			str += blockPos != null ? "{" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ() + "}" : "null (uh-oh)";
			str += " / ";
			str += face != null ? face.getName() : "null";
			str += ")";
			break;
		}
		return str;
	}
	
	public static enum TargetType {
		EMPTY,
		BLOCK,
		ENTITY
	}
}
