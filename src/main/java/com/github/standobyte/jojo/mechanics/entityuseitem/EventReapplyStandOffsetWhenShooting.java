package com.github.standobyte.jojo.mechanics.entityuseitem;

import com.github.standobyte.jojo.core.packet.fromserver.TrSyncStandOffsetPacket;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class EventReapplyStandOffsetWhenShooting {

	@SubscribeEvent
	public static void onProjectileCreated(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		if (!entity.level().isClientSide() && entity instanceof Projectile projectile
				&& projectile.getOwner() instanceof StandEntity stand) {
			EntityActionInstance curAction = stand.getCurStandAction();
			if (curAction == null) {
				LivingComponentAction.getComponent(stand).setAction(new VanillaItemClickAsAction.ItemClickInstance(), SyncType.TRACKING_AND_SELF);
			}
			// if the stand is throwing multiple, for example, snowballs because the player is holding RMB, 
			// reset the 5 tick timer (recovery phase of VanillaItemClickAsAction.ItemClickInstance)
			else if (curAction.ability == ModSpecialActions.RMB_CLICK_ITEM.get()) {
				curAction.start();
				curAction.syncPhaseChanges();
			}
			
			curAction = stand.getCurStandAction();
			// if the stand used an item as a projectile, set the offset to the front of the user
			// and readjust the projectile position before it's added
			if (curAction != null && stand.offsetFromUser.isIdle() && 
					(curAction.ability == ModSpecialActions.RMB_CLICK_ITEM.get()
					|| curAction.ability == ModSpecialActions.RMB_USING_ITEM.get())) {
				Vec3 offset = new Vec3(0, 0, 1.5);
				StandOffsetFromUser.Rotations rotations = StandOffsetFromUser.Rotations.HEAD_XY;
				stand.offsetFromUser.setOffset(offset, rotations);
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(stand, new TrSyncStandOffsetPacket(stand.getId(), offset, rotations));

				if (stand.updatePosition(stand.getUser())) {
					projectile.setPos(stand.getX(), stand.getEyeY() - 0.1, stand.getZ());
				}
			}
		}
	}
}
