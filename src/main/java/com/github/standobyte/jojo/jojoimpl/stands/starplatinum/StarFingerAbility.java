package com.github.standobyte.jojo.jojoimpl.stands.starplatinum;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.hitboxes.EntityOBBCollisionUtil;
import com.github.standobyte.jojo.util.hitboxes.ExtendableOBB;
import com.github.standobyte.jojo.util.hitboxes.OrientedBoundingBox;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HasOBBToRender;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.util.MathUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class StarFingerAbility extends StandEntityAbility {

	public StarFingerAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
	}

    @Override
    public EntityActionInstance createActionObj() {
        return new StarFingerAbility.StarFingerInstance(this);
    }

    public static class StarFingerInstance extends EntityActionInstance implements HasOBBToRender {
        public StarFingerInstance(EntityActionType ability) {
            super(ability);
        }

        private ExtendableOBB starFingerBB;

        @Override
        public void onActionSet(@Nullable EntityActionInstance prevAction) {
            super.onActionSet(prevAction);
            setStandOffset(0, 1.5, StandOffsetFromUser.Rotations.HEAD_XY, true);
            OrientedBoundingBox obb = new OrientedBoundingBox(new Vec3(0, 1.35, 0), 0.125, 0.125d, 0.8d, getPerformer().getYRot(), getPerformer().getXRot());
            this.starFingerBB = new ExtendableOBB(obb, 0.8F, (int) phasesLength.get(ActionPhase.PERFORM).floatValue(), 10, new Vec3(0, 1.35, 0));
        }

        @Override
        public void actionTick() {
            if (getPhase() == ActionPhase.PERFORM && starFingerBB != null){
                this.starFingerBB.tick();
                Vec3 pos = getPerformer().position();
                Vec3 offset = new Vec3(0.07, 1.5, 1)
                        .yRot(-getPerformer().yBodyRot * MathUtil.DEG_TO_RAD);
                this.starFingerBB.updatePosition(level(), pos, offset, getPerformer().getXRot(), getPerformer().getYRot());
                EntityOBBCollisionUtil.getEntitiesInOBB(level(), this.starFingerBB.rotatableHitbox(), entity -> entity != getPerformer()).forEach(entity -> {
                    if (performer instanceof StandEntity stand) {
                        var damageType = DamageUtil.type(level(), ModDamageTypes.STAND_ATTACK);
                        DamageSource dmgSource = new DamageSource(damageType, performer);
                        float dmgAmount = StandStatFormulas.getLightAttackDamage(stand.getAttackDamage());
                        if (standEntityAttack(stand, entity, dmgSource, dmgAmount)) {
                            stand.addFinisherMeter(0.2f);
                            this.starFingerBB.setIsMovingForward(false);
                            this.starFingerBB.setIsRetracting(true);
                        }
                    }
                });
                JojoMod.getLogger().info(String.valueOf(this.starFingerBB.getLength()));
                if (this.starFingerBB.isRetracted()){
                    setPhaseStart(ActionPhase.RECOVERY);
                    syncPhaseChanges();
                }
            }
        }

        @Override
        public void onSetPhase(ActionPhase newPhase) {
            if (newPhase == ActionPhase.RECOVERY) {
                starFingerBB = null;
            }
        }

        @Override
        @Nullable
        public ExtendableOBB extendableOBB() {
            if (starFingerBB != null){
                return starFingerBB;
            }
            return null;
        }
    }
}
