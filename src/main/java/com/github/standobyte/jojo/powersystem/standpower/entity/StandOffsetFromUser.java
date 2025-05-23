package com.github.standobyte.jojo.powersystem.standpower.entity;

import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class StandOffsetFromUser {
	private LivingEntity standEntity;
	
	public final Vec3 idleOffset;
	public final OffsetMode idleOffsetMode;
	
	private Vec3 relativeOffset;
	private OffsetMode offsetMode;
	
	private Vec3 prevAbsoluteOffset;
	private OffsetMode prevOffsetMode;
	private float prevBodyRotDiff;
	private int changedTimestamp;
	
	public StandOffsetFromUser(LivingEntity standEntity, Vec3 idleOffset, OffsetMode idleOffsetMode) {
		this.standEntity = standEntity;
		this.idleOffset = idleOffset;
		this.idleOffsetMode = idleOffsetMode;
		setOffset(idleOffset, idleOffsetMode, null);
	}
	
	public void setOffset(Vec3 offset, OffsetMode offsetMode, LivingEntity userEntity) {
		if (this.relativeOffset == null || this.offsetMode == null || 
				offset.x != this.relativeOffset.x || offset.y != this.relativeOffset.y || offset.z != this.relativeOffset.z || 
				offsetMode != this.offsetMode) {
			if (userEntity != null) {
				this.prevAbsoluteOffset = getAbsoluteOffset(userEntity, false);
				this.prevBodyRotDiff = userEntity.yBodyRot - userEntity.getYRot();
			}
			else {
				this.prevAbsoluteOffset = null;
			}
			this.prevOffsetMode = this.offsetMode != null ? this.offsetMode : offsetMode;
			
			this.relativeOffset = offset;
			this.offsetMode = offsetMode;
			this.changedTimestamp = standEntity.tickCount;
		}
	}
	
	public void setOffset(Vec3 offset, LivingEntity userEntity) {
		setOffset(offset, OffsetMode.HEAD, userEntity);
	}
	
	public void resetToIdle(LivingEntity userEntity) {
		setOffset(idleOffset, idleOffsetMode, userEntity);
	}
	
	public boolean isIdle() {
		return offsetMode == idleOffsetMode && relativeOffset.equals(idleOffset);
	}
	
	public Vec3 getPosition(LivingEntity userEntity) {
		Vec3 offset = getAbsoluteOffset(userEntity, standEntity.level().isClientSide());
		Vec3 userCenter = userEntity.getBoundingBox().getCenter();
		Vec3 standCenter = userCenter.add(offset);
		Vec3 pos = standCenter.subtract(0, standEntity.getBoundingBox().getYsize() / 2, 0);
		return pos;
	}
	
	public Vec3 getAbsoluteOffset(LivingEntity userEntity, boolean lerp) {
		if (lerp && prevAbsoluteOffset == null) {
			prevAbsoluteOffset = getAbsoluteOffset(userEntity, false);
		}
		
		Vec3 absoluteOffset = relativeOffset;
		if (offsetMode == OffsetMode.HEAD_XY) {
			absoluteOffset = relativeOffset.xRot(-userEntity.getXRot() * MathUtil.DEG_TO_RAD);
		}
		float userYRot = offsetMode == OffsetMode.BODY ? userEntity.yBodyRot : userEntity.getYRot();
		absoluteOffset = absoluteOffset.yRot(-userYRot * MathUtil.DEG_TO_RAD);
		
		if (lerp) {
			double lerpAmount = getLerpAmount();
			absoluteOffset = new Vec3(
					Mth.lerp(lerpAmount, prevAbsoluteOffset.x, absoluteOffset.x),
					Mth.lerp(lerpAmount, prevAbsoluteOffset.y, absoluteOffset.y),
					Mth.lerp(lerpAmount, prevAbsoluteOffset.z, absoluteOffset.z));
		}
		
		return absoluteOffset;
	}
	
	public void copyRotation(LivingEntity userEntity, boolean lerp) {
		standEntity.setYRot(userEntity.getYRot());
		standEntity.setXRot(userEntity.getXRot());
		standEntity.yRotO = userEntity.yRotO;
		standEntity.yHeadRot = userEntity.yHeadRot;
		standEntity.yHeadRotO = userEntity.yHeadRotO;
		
		// this shit so ass
		boolean isBodyRot = offsetMode == OffsetMode.BODY;
		float bodyRotAmount = isBodyRot ? 1 : 0;
		if (lerp) {
			boolean wasBodyRot = prevOffsetMode == OffsetMode.BODY;
			if (isBodyRot != wasBodyRot) {
				float lerpAmount = getLerpAmount();
				bodyRotAmount = isBodyRot ? lerpAmount : (1 - lerpAmount);
			}
		}
		
		if (bodyRotAmount == 1) {
			standEntity.yBodyRot = userEntity.yBodyRot;
			standEntity.yBodyRotO = userEntity.yBodyRotO;
		}
		else if (bodyRotAmount == 0) {
			standEntity.yBodyRot = userEntity.getYRot();
			standEntity.yBodyRotO = userEntity.yRotO;
		}
		else {
			standEntity.yBodyRot = Mth.lerp(bodyRotAmount, userEntity.getYRot(), userEntity.yBodyRot);
			standEntity.yBodyRotO = standEntity.yBodyRot + prevBodyRotDiff / (isBodyRot ? -LERP_TIME : LERP_TIME);
		}
	}
	
	protected float getLerpAmount() {
		return getLerpAmount(0);
	}
	
	protected float getLerpAmount(int tickOffset) {
		int timeDiff = (standEntity.tickCount + tickOffset) - changedTimestamp;
		return Mth.clamp((float) timeDiff / LERP_TIME, 0, 1);
	}
	
	public static final int LERP_TIME = 4;
	
	
	public enum OffsetMode {
		HEAD,
		BODY,
		HEAD_XY
	}
}
