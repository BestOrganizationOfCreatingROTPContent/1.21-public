package com.github.standobyte.jojo.jojoimpl.stands.starplatinum;

import com.github.standobyte.jojo.client.sound.sounds.EntityStoppableSoundInstance;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class StarInhaleAbility extends StandEntityAbility {

    public StarInhaleAbility(AbilityType<?> abilityType, AbilityId abilityId) {
        super(abilityType, abilityId, InhaleAbilityInstance::new);
    }

    public static class InhaleAbilityInstance extends EntityActionInstance {
        public InhaleAbilityInstance(EntityActionType ability) {
            super(ability);
        }

        private static final double RANGE = 12.0;
        @Nullable
        private EntityStoppableSoundInstance inhaleSoundInstance;

        @Override
        public void onActionSet(@Nullable EntityActionInstance prevAction) {
            super.onActionSet(prevAction);
        }

        @Override
        public void actionTick() {
            if (getPhase() != ActionPhase.PERFORM) {
                return;
            }

            Level level = level();
            if (!(performer instanceof StandEntity standEntity)) {
                return;
            }
            LivingEntity user = getPowerUser();

            Vec3 mouthPos = standEntity.position()
                    .add(0, standEntity.getBbHeight() * 0.75F, 0)
                    .add(new Vec3(0, standEntity.getBbHeight() / 16F, standEntity.getBbWidth() * 0.5F)
                            .xRot(-standEntity.getXRot() * MathUtil.DEG_TO_RAD)
                            .yRot(-standEntity.getYRot() * MathUtil.DEG_TO_RAD));

            Vec3 spLookVec = standEntity.getLookAngle();
            level.getEntities(standEntity, standEntity.getBoundingBox().inflate(RANGE, RANGE, RANGE),
                    entity -> spLookVec.dot(entity.position().subtract(standEntity.position()).normalize()) > 0.886
                            && standEntity.hasLineOfSight(entity)
                            && entity.distanceToSqr(standEntity) > 0.5
                            && !entity.is(user)
            ).forEach(entity -> {
                double distance = entity.distanceTo(standEntity);

                double efficiency = 1.0;
                Vec3 suctionVec = mouthPos.subtract(entity.getBoundingBox().getCenter())
                        .normalize().scale(0.5 * efficiency);

                entity.setDeltaMovement(distance > 2 ?
                        entity.getDeltaMovement().add(suctionVec.scale(1 / distance))
                        : suctionVec.scale(Math.max(distance - 1, 0)));

                if (!level.isClientSide() && distance < 4 && entity instanceof LivingEntity livingEntity && getPhaseTick() % 15 == 0) {
                    var damageType = DamageUtil.type(level, ModDamageTypes.SUFFOCATION);
                    DamageSource dmgSource = new DamageSource(damageType, performer);
                    livingEntity.hurt(dmgSource, 0.5F);
                }
            });

            if (level.isClientSide()) {
                for (int i = 0; i < 2; i++) {
                    spawnAirStreamParticle(level, mouthPos, spLookVec);
                }
                if (level.random.nextFloat() < 0.5F) {
                    spawnAirStreamParticle(level, mouthPos, spLookVec);
                }
            }
        }

        private void spawnAirStreamParticle(Level level, Vec3 mouthPos, Vec3 lookVec) {
            Vec3 particlePos = mouthPos.add(lookVec.scale(RANGE)
                    .xRot((float) ((Math.random() * 2 - 1) * Math.PI / 6))
                    .yRot((float) ((Math.random() * 2 - 1) * Math.PI / 6)));
            Vec3 vecToStand = mouthPos.subtract(particlePos).normalize().scale(0.75);
            level.addParticle(ModParticles.AIR_STREAM.get(), particlePos.x, particlePos.y, particlePos.z, vecToStand.x, vecToStand.y, vecToStand.z);
        }

        @Override
        public void onButtonStopHold() {
            setPhaseStart(ActionPhase.RECOVERY);
            syncPhaseChanges();
        }

        @Override
        public void onSetPhase(ActionPhase newPhase) {
            super.onSetPhase(newPhase);
            Level level = level();
            if (!level.isClientSide()) {
                return;
            }

            if (newPhase == ActionPhase.PERFORM) {
                if (performer instanceof StandEntity stand) {
                    EntityStoppableSoundInstance soundInstance = new EntityStoppableSoundInstance(
                            ModSoundEvents.STAR_PLATINUM_INHALE.get(),
                            stand.getSoundSource(),
                            1.0F,
                            2.0F,
                            stand,
                            this.id,
                            () -> getPhase() != ActionPhase.PERFORM
                    );
                    Minecraft.getInstance().getSoundManager().play(soundInstance);
                }
            }
        }
    }
}