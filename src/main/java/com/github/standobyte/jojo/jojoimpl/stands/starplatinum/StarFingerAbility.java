package com.github.standobyte.jojo.jojoimpl.stands.starplatinum;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityStoppableSoundInstance;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.hitboxes.OBBCollisionUtil;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
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
        public void actionPerformStart() {
            LivingEntity user = getPowerUser();
            StandPower standPower = StandPower.get(user);
            standPower.consumeStamina(25);
        }

        @Override
        public void actionTick() {
            if (getPhase() == ActionPhase.PERFORM && starFingerBB != null){
                Vec3 pos = getPerformer().position();
                Vec3 offset = new Vec3(0.07, 1.5, 1)
                        .yRot(-getPerformer().yBodyRot * MathUtil.DEG_TO_RAD);
                this.starFingerBB.updatePosition(level(), pos, offset, getPerformer().getXRot(), getPerformer().getYRot());
                if (!level().isClientSide()){
                    Vec3 endPos = this.starFingerBB.rotatableHitbox().center.add(getPerformer().getLookAngle().scale(starFingerBB.rotatableHitbox().extent.length()));
                    OBBCollisionUtil.getEntitiesInOBB(level(), this.starFingerBB.rotatableHitbox(), entity -> entity != getPerformer() && entity != getPowerUser()).forEach(entity -> {
                        if (performer instanceof StandEntity stand) {
                            var damageType = DamageUtil.type(level(), ModDamageTypes.STAND_ATTACK);
                            DamageSource dmgSource = new DamageSource(damageType, performer);
                            float dmgAmount = StandStatFormulas.getLightAttackDamage(stand.getAttackDamage());
                            if (standEntityAttack(stand, entity, dmgSource, dmgAmount)) {
                                this.starFingerBB.forceRetract(level(), getPerformer(), this.id);
                            }
                        }
                    });
                    HitResult result = level().clip(new ClipContext(starFingerBB.rotatableHitbox().center, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty()));
                    if (result instanceof BlockHitResult blockHitResult){
                        BlockState blockCollision = OBBCollisionUtil.getCollidingBlock(level(), blockHitResult.getBlockPos());
                        if (blockCollision != null){
                            // TODO Add button, lever and other interactions
                            this.starFingerBB.forceRetract(level(), getPerformer(), this.id);
                        }
                    }

                }
                this.starFingerBB.tick();
                if (this.starFingerBB.isRetracted()){
                    setPhaseStart(ActionPhase.RECOVERY);
                    syncPhaseChanges();
                }
            }
        }

        @Override
        public void onSetPhase(ActionPhase newPhase) {
            Level level = level();
            if (newPhase == ActionPhase.PERFORM){
                if (level.isClientSide() && performer instanceof StandEntity stand) {
                    if (ClientGlobals.canHearStands){
                        level.playLocalSound(stand, ClientsideSoundsHelper.withStandSkin(
                                        ModSoundEvents.STAR_PLATINUM_STAR_FINGER.get(), stand),
                                stand.getSoundSource(), 1, 1);
                    }
                    level.playLocalSound(stand, ClientsideSoundsHelper.withStandSkin(
                                    ModSoundEvents.JOTARO_STAR_FINGER.get(), stand),
                            stand.getSoundSource(), 1, 1);
                }
            }
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
