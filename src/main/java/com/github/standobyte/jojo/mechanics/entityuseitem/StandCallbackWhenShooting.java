package com.github.standobyte.jojo.mechanics.entityuseitem;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.packet.fromserver.TrSyncStandOffsetPacket;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandCallbackWhenShooting {

	/**
	 * @return The new projectile shooting vector, readjusted with the Stand's stats
	 */
	@Nullable
	public static Vec3 onStandShooting(Projectile projectile, double x, double y, double z, float velocity, float inaccuracy) {
		if (projectile.getOwner() instanceof StandEntity stand) {
			EntityActionInstance curAction = stand.getCurStandAction();
			if (curAction == null && ServerSideLivingClick.isEntityHoldingAnItem(stand)) {
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
			// and readjust the projectile position/movement before it's added
			if (curAction != null && 
					(curAction.ability == ModSpecialActions.RMB_CLICK_ITEM.get()
					|| curAction.ability == ModSpecialActions.RMB_USING_ITEM.get())) {
				double precision = stand.getPrecision();
				// 8 - 1; 12 - 2/3; 16 - 1/3; 20 - 0
				float inaccuracyMultiplier = (float) Math.max((-precision / 12.0 + 5.0 / 3.0), 0);
				inaccuracy *= inaccuracyMultiplier;
				
				double strength = stand.getAttackDamage();
				if (strength > 8) {
					velocity *= strength / 8;
				}
				Vec3 newProjectileVec = projectile.getMovementToShoot(x, y, z, velocity, inaccuracy);
				
				if (stand.offsetFromUser.isIdle()) {
					Vec3 standOffset = new Vec3(0, 0, 1.5);
					StandOffsetFromUser.Rotations rotations = StandOffsetFromUser.Rotations.HEAD_XY;
					stand.offsetFromUser.setOffset(standOffset, rotations);
					PacketDistributor.sendToPlayersTrackingEntityAndSelf(stand, new TrSyncStandOffsetPacket(stand.getId(), standOffset, rotations));
	
					if (stand.updatePosition(stand.getUser())) {
						projectile.setPos(stand.getX(), stand.getEyeY() - 0.1, stand.getZ());
					}
				}
				
				return newProjectileVec;
			}
		}
		return null;
	}

}
