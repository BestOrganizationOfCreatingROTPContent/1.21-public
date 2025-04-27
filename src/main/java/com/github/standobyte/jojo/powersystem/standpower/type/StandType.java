package com.github.standobyte.jojo.powersystem.standpower.type;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.datapack.DataDrivenStandsLoader;
import com.github.standobyte.jojo.powersystem.standpower.datapack.StandTypeClass;
import com.github.standobyte.jojo.powersystem.standpower.type.SummonedStand.BlankSummonedStand;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class StandType extends PowerType {
	protected final ResourceLocation standTypeId;
	protected StandStats stats;
	protected boolean isEnabled;
	
	public StandType(StandStats stats, Moveset.Builder moveset, 
			ResourceLocation id) {
		super(moveset);
		this.standTypeId = id;
		if (stats == null) stats = new StandStats(0, 0, 0, 0, 0, 0);
		this.stats = stats;
		this.isEnabled = true;
	}
	
	@Override
	public JsonObject makeConfigTemplate() {
		JsonObject json = super.makeConfigTemplate();
		StandTypeClass.addClassToJson(json, this.getClass());
		json.add("stats", stats.makeConfigTemplate());
		return json;
	}
	
	@Override
	public void applyConfig(JsonElement json) {
		super.applyConfig(json);
		JsonObject config = json.getAsJsonObject();
		Optional.ofNullable(config.get("stats")).ifPresent(stats::applyConfig);
	}

	@Override
	public void restoreDefaults() {
		super.restoreDefaults();
		stats.restoreDefaults();
	}
	
	
	public StandStats getStandStats() {
		return stats;
	}
	
	
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	
	public void toggleSummon(LivingEntity user, StandPower standPower) {
		if (!standPower.isSummoned()) {
			summon(user, standPower);
		}
		else {
			unsummon(user, standPower);
		}
	}
	
	public boolean summon(LivingEntity user, StandPower standPower) {
		if (!standPower.isSummoned()) {
			SummonedStand summonedStand = makeSummonedStand();
			if (summonedStand == null) return false;
			standPower.setSummonedStand(summonedStand);
			return true;
		}
		return false;
	}
	
	protected SummonedStand makeSummonedStand() {
		return new BlankSummonedStand();
	}
	
	public void unsummon(LivingEntity user, StandPower standPower) {
		forceUnsummon(user, standPower);
	}
	
	public void forceUnsummon(LivingEntity user, StandPower standPower) {
		if (standPower.isSummoned()) {
			standPower.setSummonedStand(null);
		}
	}
	
	
	@Override
	public ResourceLocation getId() {
		return standTypeId;
	}
	
	@Nullable
	public static StandType fromId(ResourceLocation id) {
		StandType stand = DataDrivenStandsLoader.getDatapackStand(id);
		if (stand == null) {
			stand = JojoRegistries.DEFAULT_STANDS_REG.getValue(id);
		}
		if (stand != null && !stand.isEnabled()) {
			stand = null;
		}
		return stand;
	}
	
	public static Stream<StandType> getAllEnabledStands() {
		return Stream.concat(
				JojoRegistries.DEFAULT_STANDS_REG.entrySet().stream().map(Map.Entry::getValue).filter(StandType::isEnabled), 
				DataDrivenStandsLoader.getAllDatapackStands());
	}
	
//	public static final StreamCodec<ByteBuf, StandType> SYNC_VIA_ID = 
//			ResourceLocation.STREAM_CODEC.map(StandType::fromId, StandType::getId);
//	
	
	
	@Override
	public PowerClass<StandPower> getPowerClass() {
		return PowerClass.STAND;
	}
	
}
