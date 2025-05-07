package com.github.standobyte.jojo.powersystem.standpower.entity;

import javax.annotation.Nullable;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class StandOffsetFromUser {
	private Vec3 idleOffset;
	private Vec3 relativeOffset;
	private Vec3 prevOffset;
	private int changedTimestamp;
	
	public StandOffsetFromUser(StandEntity standEntity, Vec3 idleOffset) {
		this.idleOffset = idleOffset;
		this.relativeOffset = this.idleOffset;
		this.prevOffset = this.relativeOffset;
		this.changedTimestamp = standEntity.tickCount;
	}
	
	public void setOffset(@Nullable Vec3 offset, StandEntity standEntity) {
		if (offset == null) offset = idleOffset;
		if (offset.x != this.relativeOffset.x || offset.y != this.relativeOffset.y || offset.z != this.relativeOffset.z) {
			this.prevOffset = this.relativeOffset;
			this.relativeOffset = offset;
			this.changedTimestamp = standEntity.tickCount;
		}
	}
	
	public void resetToIdle(StandEntity standEntity) {
		setOffset(null, standEntity);
	}
	
	public Vec3 getOffset() {
		return relativeOffset;
	}
	
	public static final int LERP_TIME = 4;
	public Vec3 getOffsetLerp(StandEntity standEntity) {
		int timeDiff = standEntity.tickCount - changedTimestamp;
		
		if (timeDiff >= LERP_TIME) return relativeOffset;
		if (timeDiff <= 0) return prevOffset;
		
		double lerp = (double) timeDiff / LERP_TIME;
		return new Vec3(
				Mth.lerp(lerp, prevOffset.x, relativeOffset.x),
				Mth.lerp(lerp, prevOffset.y, relativeOffset.y),
				Mth.lerp(lerp, prevOffset.z, relativeOffset.z));
	}
}
