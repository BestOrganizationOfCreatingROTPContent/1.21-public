package com.github.standobyte.jojo.powersystem.playerpower;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.INBTSerializable;

public abstract class PowerData implements INBTSerializable<CompoundTag> {
	public abstract PlayerPowerType<?> getType();

	public abstract void syncToPlayer(ServerPlayer user);
	public abstract void syncToTracking(ServerPlayer player);
}
