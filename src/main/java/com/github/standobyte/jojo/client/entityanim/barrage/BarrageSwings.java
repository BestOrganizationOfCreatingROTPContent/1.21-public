package com.github.standobyte.jojo.client.entityanim.barrage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.entityrender.stand.HumanoidPart;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityModel;
import com.github.standobyte.jojo.client.ui.utils.RGBUtil;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

public class BarrageSwings {
	@ApiStatus.Internal public List<BarrageSwing> barrageSwings = new LinkedList<>();
	@ApiStatus.Internal public float loopLast = -1;

	@ApiStatus.Internal public boolean isBarragingAnim = false;
	@ApiStatus.Internal public String barrageType;
	@ApiStatus.Internal public AddBarrageSwing addSwingFunction;


	public void frameStandBarrage(Minecraft mc, RotpAnimDefinition barrageAnim, String barrageTypeName, float curAnimTimeSecs, LivingEntityRenderState renderState) {
		frameUpdateSwings(mc);
		frameUpdateBarrageType(barrageTypeName);
		if (isBarragingAnim) {
			frameSetValuesAndAddNewSwings(barrageAnim, renderState, curAnimTimeSecs);
		}
	}

	public void frameUpdateSwings(Minecraft mc) {
		if (!mc.isPaused() && !barrageSwings.isEmpty()) {
			float timeDelta = mc.getTimer().getGameTimeDeltaTicks();
			Iterator<BarrageSwing> iter = barrageSwings.iterator();
			while (iter.hasNext()) {
				BarrageSwing swing = iter.next();
				swing.addDelta(timeDelta);
				if (swing.removeSwing()) {
					iter.remove();
				}
			}
		}
	}

	public void frameUpdateBarrageType(String barrageTypeName) {
		this.isBarragingAnim = false;
		this.barrageType = barrageTypeName;
		this.addSwingFunction = null;

		if (barrageType != null) {
			AddBarrageSwing addSwingFunction = BARRAGE_SWING_TYPES.get(barrageType);
			if (addSwingFunction != null) {
				this.isBarragingAnim = true;
				this.addSwingFunction = addSwingFunction;
			}
		}
	}

	public void frameSetValuesAndAddNewSwings(RotpAnimDefinition barrageAnim, LivingEntityRenderState curRenderState, float curAnimTimeSecs) {
		addSwingFunction.addSwings(this, barrageAnim, curRenderState, curAnimTimeSecs);
	}

	
	public boolean hasSmthToRender() {
		return !barrageSwings.isEmpty();
	}
	
	public void renderLayerBarrage(EntityModel<?> model, 
			PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color) {
		for (BarrageSwing swing : barrageSwings) {
			swing.poseAndRender(model, poseStack, buffer, 
					packedLight, packedOverlay, color);
		}
		restoreVisibility(model);
	}





	public static final Map<String, AddBarrageSwing> BARRAGE_SWING_TYPES = Util.make(new HashMap<>(), map -> {
		map.put("TWO_HANDED", TwoHandedBarrageLoopSwing::addSwings);
	});

	@FunctionalInterface
	public static interface AddBarrageSwing {
		void addSwings(BarrageSwings swings, RotpAnimDefinition barrageAnim, 
				LivingEntityRenderState curRenderState, float curAnimTimeSecs);
	}



	public abstract static class BarrageSwing {
		protected static final Random RANDOM = new Random();
		protected static final LivingEntityRenderState sharedRenderState = new LivingEntityRenderState();
		protected static final EntityActionRenderState sharedActionRenderState = new EntityActionRenderState();
		
		protected RotpAnimDefinition barrageAnim;
		protected float ticks;
		protected float ticksMax;

		public BarrageSwing(RotpAnimDefinition barrageAnim, float startingAnim, float animMax) {
			this.barrageAnim = barrageAnim;
			this.ticks = startingAnim;
			this.ticksMax = animMax;
		}

		public void addDelta(float delta) {
			ticks += delta * 0.5F;
		}

		public boolean removeSwing() {
			return ticks >= ticksMax;// * 0.75F;
		}

		public abstract void poseAndRender(EntityModel<?> model, 
				PoseStack poseStack, VertexConsumer buffer, 
				int packedLight, int packedOverlay, int color);
	}


	public static class TwoHandedBarrageLoopSwing extends BarrageSwing {
		protected final float xRot;
		protected float animTimeOffset;
		protected final HumanoidArm side;
		protected final Vec3 offset;
		protected final float zRot;

		public TwoHandedBarrageLoopSwing(RotpAnimDefinition barrageAnim, LivingEntityRenderState curRenderState, 
				float startingAnim, float animMax, HumanoidArm side, double maxOffset, float animTimeOffset) {
			super(barrageAnim, startingAnim, animMax);
			this.xRot = curRenderState.xRot;
			this.animTimeOffset = animTimeOffset;
			this.side = side;
			double upOffset = (RANDOM.nextDouble() - 0.5) * maxOffset;
			double leftOffset = RANDOM.nextDouble() * maxOffset / 2;
			double frontOffset = RANDOM.nextDouble() * 0.5;
			if (side == HumanoidArm.RIGHT) {
				leftOffset *= -1;
			}
			double atan = Mth.atan2(upOffset, leftOffset);
			zRot = maxOffset == 0 ? 0 : MathUtil.wrapRadians((float) (Math.PI / 2 - atan));
			offset = new Vec3(leftOffset, upOffset, frontOffset);
		}

		public static void addSwings(BarrageSwings swings, RotpAnimDefinition barrageAnim, 
				LivingEntityRenderState curRenderState, float curAnimTimeSecs) {
			float lastLoop = swings.loopLast;
			float loopLen = 4;
			float loop = curRenderState.ageInTicks / loopLen;
			if (swings.isBarragingAnim && loop > lastLoop) {
				EntityActionRenderState stats = EntityActionRenderState.getFrom(curRenderState);
				
				float hits = stats.barrageSwingsPerSecond / 20F * Math.min(loop - lastLoop, 1) * loopLen;
				int swingsToAdd = MathUtil.fractionRandomInc(hits / 2);
				if (swingsToAdd > 0) {
					HumanoidArm side = HumanoidArm.RIGHT;
					double maxOffset = Math.max(1 - stats.barragePrecision / 64, 0);
					if (RANDOM.nextBoolean()) side = side.getOpposite();

					for (int i = 0; i < swingsToAdd; i++) {
						float x = ((float) i + (RANDOM.nextFloat() - 0.5F) * 0.4F) / swingsToAdd;
						float f = x * loopLen * 0.5F;
						float addTime = (side == HumanoidArm.LEFT ? loopLen * 0.5f : 0) + (curAnimTimeSecs - curAnimTimeSecs % loopLen);
						swings.barrageSwings.add(new BarrageSwings.TwoHandedBarrageLoopSwing(
								barrageAnim, curRenderState, f, loopLen, side, maxOffset, addTime));
						side = side.getOpposite();
					}
				}
			}
			swings.loopLast = loop;
		}

		@Override
		public void poseAndRender(EntityModel<?> model, 
				PoseStack poseStack, VertexConsumer buffer, 
				int packedLight, int packedOverlay, int color) {
			setOnlyOneArmVisible(model, side);
			float loopCompletion = ticks / ticksMax;
			float swingAmount = loopCompletion < 0.5 ? loopCompletion * 2 : (1 - loopCompletion) * 2;
			double zAdditional = 0.5 * swingAmount;
			Vec3 offsetRot = new Vec3(offset.x, -offset.y, offset.z + zAdditional).xRot(xRot * MathUtil.DEG_TO_RAD);
			poseStack.pushPose();
			poseStack.translate(offsetRot.x, offsetRot.y, -offsetRot.z);
			
			sharedActionRenderState.actionPhase = ActionPhase.PERFORM;
			sharedActionRenderState.phaseTime = ticks + animTimeOffset;
			sharedActionRenderState.disableCrouch = true;
			sharedRenderState.xRot = this.xRot;
			sharedRenderState.yRot = 0;
			
			float seconds = barrageAnim.getAnimTime(sharedActionRenderState);
			barrageAnim.animate(model, sharedRenderState, seconds, 1);
			ModelPart arm = getNoXRotArm(model, side);
			
			arm.zRot = Mth.lerp(swingAmount, arm.zRot, arm.zRot + zRot * 1.25f);
			arm.yRot = Mth.lerp(swingAmount, arm.yRot, 0);
			arm.xRot = Mth.lerp(swingAmount, arm.xRot, (float) -Math.PI * 0.5f);
			
			// XXX (barrage anim) some layers are not translucent (armor, clothes, mannequin model, etc.)
			float alpha = 0.75f * swingAmount;
			color = RGBUtil.scaleAlpha(color, alpha);
			((Model_1_21_2plus) model).jojo_ripples$root().render(poseStack, buffer, packedLight, packedOverlay, color);
			poseStack.popPose();
		}
	}

	
	public static ModelPart getNoXRotArm(EntityModel<?> model, HumanoidArm side) {
		return switch (model) {
			case StandEntityModel<?, ?> standModel -> {
				yield switch (side) {
					case LEFT -> standModel.left_arm;
					case RIGHT -> standModel.right_arm;
				};
			}
			case HumanoidModel<?> humanoidModel -> {
				yield switch (side) {
					case LEFT -> humanoidModel.leftArm;
					case RIGHT -> humanoidModel.rightArm;
				};
			}
			default -> null;
		};
	}
	
	public static void setOnlyOneArmVisible(EntityModel<?> model, HumanoidArm side) {
		switch (model) {
			case StandEntityModel<?, ?> standModel -> {
				HumanoidPart.setPartsVisible(standModel, switch (side) {
					case LEFT -> HumanoidPart.LEFT_ARM_ONLY;
					case RIGHT -> HumanoidPart.RIGHT_ARM_ONLY;
				});
			}
			case HumanoidModel<?> humanoidModel -> {
				humanoidModel.setAllVisible(false);
				(switch (side) {
					case LEFT -> humanoidModel.leftArm;
					case RIGHT -> humanoidModel.rightArm;
				}).visible = true;
			}
			default -> {}
		}
	}
	
	public static void restoreVisibility(EntityModel<?> model) {
		switch (model) {
			case StandEntityModel<?, ?> standModel -> {
				standModel.setAllVisible(true);
			}
			case HumanoidModel<?> humanoidModel -> {
				humanoidModel.setAllVisible(true);
			}
			default -> {}
		}
	}
	
	
	@Nullable
	public static BarrageSwings getBarrageSwings(LivingEntityRenderState renderState) {
		EntityActionRenderState action = EntityActionRenderState.getFrom(renderState);
		return action != null ? action.barrageSwings : null;
	}
	
	@Nullable public static BarrageSwings currentlyRendering = null;
	
	public static void setupToRender(BarrageSwings barrage) {
		BarrageSwings.currentlyRendering = barrage;
	}

}

