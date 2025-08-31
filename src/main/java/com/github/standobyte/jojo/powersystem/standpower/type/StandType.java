package com.github.standobyte.jojo.powersystem.standpower.type;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.init.core.ModEntityAttributes;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.datapack.DataDrivenStandsLoader;
import com.github.standobyte.jojo.powersystem.standpower.datapack.StandTypeClass;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.powersystem.standpower.type.SummonedStand.BlankSummonedStand;
import com.github.standobyte.jojo.util.mc.AttributeUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.Lazy;

public class StandType extends PowerType {
	protected final ResourceLocation standTypeId;
	protected StandStats stats;
	protected boolean isEnabled;
	
	public StandType(StandStats stats, MovesetBuilder moveset, 
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
	
	
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	
	public StandStats getStandStats() {
		return stats;
	}
	
	
	@Nonnull
	public StandTypePersistentData newDataInstance() {
		return new StandTypePersistentData();
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
	
	
	public boolean usesStamina(StandPower standPower) {
		return true;
	}
	
	public float getMaxStamina(StandPower standPower) {
		return getBaseMaxStamina(standPower) * getStaminaMultiplier(standPower);
	}
	
	protected float getBaseMaxStamina(StandPower standPower) {
		return 1000;
	}
	
	public float getStaminaRegen(StandPower standPower) {
		return getBaseStaminaRegen(standPower) * getStaminaMultiplier(standPower);
	}
	
	protected float getBaseStaminaRegen(StandPower standPower) {
		if (standPower.isSummoned()) {
			LivingEntity standEntity = standPower.getSummonedStandEntity();
			if (standEntity != null) {
				EntityActionInstance action = LivingComponentAction.getCurEntityAction(standEntity);
				if (action != null /* TODO regen stamina during stand unsummon */) {
					return 0;
				}
			}
			return 1.5f;
		}
		return 3;
	}
	
	protected static float getStaminaMultiplier(StandPower standPower) {
		double durability = AttributeUtil.getValueOrDefault(standPower.getUser(), ModEntityAttributes.STAND_DURABILITY);
		return StandStatFormulas.getStaminaMultiplier(durability);
	}
	
	
	public boolean usesResolve(StandPower standPower) {
		return true;
	}
	
	
	@Override
	public ResourceLocation getId() {
		return standTypeId;
	}
	
	@Nullable
	public static StandType fromId(ResourceLocation id) {
		StandType stand = DataDrivenStandsLoader.getDatapackStand(id);
		if (stand == null) {
			stand = JojoRegistries.DEFAULT_STANDS_REG.get(id);
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
	
	public Lazy<Component> name = Lazy.of(() -> Component.translatable(Util.makeDescriptionId("stand", this.getId())));
	@Override
	public Component getName(Power<?> playerPowerData) {
		return ((StandPower) playerPowerData).getStandInstance()
				.map(stand -> stand.getStandName(playerPowerData.getUser().level().isClientSide()))
				.orElseGet(this.name::get);
	}
	
}
