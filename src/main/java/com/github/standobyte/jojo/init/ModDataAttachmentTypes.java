package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.AbilityInputHandler;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.entitycomponent.DataEventListeners;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModDataAttachmentTypes {
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, JojoMod.MOD_ID);
	

	@ApiStatus.Internal
	public static final Supplier<AttachmentType<DataEventListeners>> DATA_EVENT_HELPER = ATTACHMENT_TYPES.register("event_listener", 
			() -> AttachmentType.builder(DataEventListeners::new).build());
	
	@ApiStatus.Internal
	public static final Supplier<AttachmentType<StandPower>> STAND_POWER = ATTACHMENT_TYPES.register("stand_power", 
			() -> AttachmentType.serializable(entity -> PowerClass._tryAttach(entity, StandPower::new)).build());

	@ApiStatus.Internal
	public static final Supplier<AttachmentType<PlayerPower>> PLAYER_POWER = ATTACHMENT_TYPES.register("player_power", 
			() -> AttachmentType.serializable(entity -> PowerClass._tryAttach(entity, PlayerPower::new)).build());
	
	@ApiStatus.Internal
	public static final Supplier<AttachmentType<LivingComponentAction>> LIVING_ACTION = ATTACHMENT_TYPES.register("living_action", 
			() -> AttachmentType.serializable(LivingComponentAction::create).build());
	
	@ApiStatus.Internal
	public static final Supplier<AttachmentType<AbilityInputHandler>> PLAYER_HELD_ACTION = ATTACHMENT_TYPES.register("player_held_action", 
			() -> AttachmentType.builder(AbilityInputHandler::create).build());
	
}
