package com.github.standobyte.jojo.jojoimpl.stands._entitybase.item;

import com.github.standobyte.jojo.mechanics.entityuseitem.LivingUseItem;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.util.StandUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class StandUseItemAbility extends StandEntityAbility {

	public StandUseItemAbility(AbilityId abilityId) {
		super(abilityId);
		setDefaultPhaseLength(ActionPhase.PERFORM, 999999);
	}

	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		if (super.isAbilityAvailable(context)) {
			StandEntity standEntity = StandUtil.getSummonedStand(context);
			if (standEntity != null) {
				ItemStack lItem = standEntity.getOffhandItem();
				ItemStack rItem = standEntity.getMainHandItem();
				return !lItem.isEmpty() || !rItem.isEmpty();
			}
		}
		return false;
	}

//	@Override
//	public void writeExtraInput(FriendlyByteBuf serverboundBuf, LivingEntity user, boolean isClientPlayer) {
//		writeHitResult(serverboundBuf, Minecraft.getInstance().hitResult);
//	}
//
//	public static void writeHitResult(FriendlyByteBuf buf, HitResult value) {
//		buf.writeEnum(value.getType());
//		switch (value.getType()) {
//			case BLOCK, MISS -> {
//				buf.writeBlockHitResult((BlockHitResult) value);
//			}
//			case ENTITY -> {
//				buf.writeInt(((EntityHitResult) value).getEntity().getId());
//			}
//		}
//	}
//
//	public static HitResult readHitResult(FriendlyByteBuf buf, Level level) {
//		HitResult.Type type = buf.readEnum(HitResult.Type.class);
//		return switch (type) {
//			case BLOCK, MISS -> {
//				yield buf.readBlockHitResult();
//			}
//			case ENTITY -> {
//				int entityId = buf.readInt();
//				Entity entity = level.getEntity(entityId);
//				yield entity != null ? new EntityHitResult(entity) : null;
//			}
//		};
//	}

	@Override
	public HeldInput onKeyPress(Level level, LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime) {
		HeldInput input = super.onKeyPress(level, user, extraClientInput, InputMethod.HOLD, clickHoldResolveTime);
		if (input instanceof StandItemUse action) {
			action.useItem(true);
		}
		return input;
	}


	@Override
	public EntityActionInstance createActionObj() {
		return new StandItemUse(this);
	}

	public static class StandItemUse extends EntityActionInstance {
		protected int delay;

		public StandItemUse(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void actionTick() {
			if (getPhase() == ActionPhase.PERFORM) {
				if (delay == 0) useItem(false);
				else --delay;
			}
		}

		public void useItem(boolean targetEntity) {
			HitResult target = Minecraft.getInstance().hitResult;
			boolean usedItem = LivingUseItem.serverSideRightClick(performer, 
					powerUser.getEntity(level()) instanceof ServerPlayer player ? player : null, 
					target);
			if (usedItem) {
				delay = 3;
			}
		}

		@Override
		public void onButtonStopHold() {
			if (getPhase() != ActionPhase.RECOVERY) {
				startPhase(ActionPhase.RECOVERY);
				syncPhaseChanges();
				
				ItemStack usedItem = performer.getUseItem();
				if (!usedItem.isEmpty()) {
					LivingUseItem.releaseUsingItem(performer, powerUser.getEntity(level()) instanceof ServerPlayer player ? player : null);
				}
			}
		}

		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}

	}

}
