package com.github.standobyte.jojo.powersystem.ability;

import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.packet.fromserver.TrAbilityUsePacket;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.network.PacketDistributor;

public class AbilityInputHandler {
	// TODO if the player logs out and the action gets saved in NBT, after relog they won't be able to stop the action - fix that
	// TODO should held actions maybe be synced with newly tracking entities?
	private Int2ObjectMap<EntityActionHeld> heldButtonActions = new Int2ObjectArrayMap<>();
	
	public static <P extends Power<P>> void click(Ability<P> ability, Power<?> p, RegistryFriendlyByteBuf extraData, float timeTookToResolve) {
		if (ability != null) {
			P power = ability.getPowerClass().cast(p);
			LivingEntity user = power.getUser();
			if (user != null) {
				Level level = user.level();
				ability.onClick(level, user, power);
				if (!level.isClientSide()) {
					PacketDistributor.sendToPlayersTrackingEntity(user, 
							TrAbilityUsePacket.click(user.getId(), power.getPowerClass(), ability, timeTookToResolve));
				}
			}
		}
	}
	
	/* 
	 * FIXME ability inputs that are currently controlled purely by the client
	 * 	barrage can refresh its duration via a client-sent packet
	 * 	the client can use abilities from other movesets
	 */
	public static <P extends Power<P>> void startHolding(short keyId, Ability<P> ability, Power<?> p, 
			LivingEntity user, RegistryFriendlyByteBuf extraData, float timeTookToResolve) {
		if (ability != null) {
			P power = ability.getPowerClass().cast(p);
			AbilityInputHandler holder = get(user);
			if (holder != null) {
				Level level = user.level();
				EntityActionInstance action = ability.onButtonStartHold(level, user, power);
				if (!level.isClientSide()) {
					PacketDistributor.sendToPlayersTrackingEntity(user, 
							TrAbilityUsePacket.startHold(user.getId(), keyId, power.getPowerClass(), ability, timeTookToResolve));
				}
				holder.heldButtonActions.put(keyId, new EntityActionHeld(ability, action, power));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <P extends Power<P>> void releaseHolding(short keyId, LivingEntity user) {
		AbilityInputHandler holder = get(user);
		if (holder != null) {
			EntityActionHeld heldAction = holder.heldButtonActions.remove(keyId);
			if (heldAction != null) {
				Ability<P> ability = (Ability<P>) heldAction.ability;
				P power = ability.getPowerClass().cast(heldAction.power);
				EntityActionInstance action = heldAction.action;
				Level level = user.level();

				if (action != null && action.isOver()) {
					action = null;
				}
				if (action != null) {
					ability.onButtonStopHold(level, user, power, action);
				}
				if (!level.isClientSide()) {
					PacketDistributor.sendToPlayersTrackingEntity(user, 
							TrAbilityUsePacket.releaseHold(user.getId(), keyId));
				}
			}
		}
	}
	
	
	private final AtomicInteger pseudoKey = new AtomicInteger();
	public short makeMobFakeKeyId(Ability<?> ability) {
		pseudoKey.incrementAndGet();
		return pseudoKey.shortValue();
	}
	
	
	public enum ClickInputType {
		PRESS_CLICK,
		PRESS_HOLD,
		RELEASE;
	}
	
	
	@ApiStatus.Internal
	public static class EntityActionHeld {
		private final Ability<?> ability;
		private final EntityActionInstance action;
		private final Power<?> power;
		
		public EntityActionHeld(Ability<?> ability, EntityActionInstance action, Power<?> power) {
			this.ability = ability;
			this.action = action;
			this.power = power;
		}
	}
	
	
	public static AbilityInputHandler create(IAttachmentHolder obj) {
		return new AbilityInputHandler();
	}
	
	private static AbilityInputHandler get(LivingEntity entity) {
		return entity.getData(ModDataAttachmentTypes.PLAYER_HELD_ACTION.get());
	}
}
