package com.github.standobyte.jojo.client.entityanim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityanim.action.AnimActionPhase;
import com.github.standobyte.jojo.client.entityanim.action.AnimInstructionTimelines;
import com.github.standobyte.jojo.client.entityanim.action.AnimObjTimeline;
import com.github.standobyte.jojo.client.entityanim.molang.AnimMolangQuery;
import com.github.standobyte.jojo.client.entityanim.molang.KeyframeQuery;
import com.github.standobyte.jojo.client.entityanim.playerbend.PlayerModelBends;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.util.MathUtil;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;

import com.github.standobyte.v1_21_4_stuff.OldPlayerModelJank;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

import com.github.standobyte.v1_21_4_stuff.missingmethods._PartPose;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class AnimWithExtras {
	public final AnimationDefinition animation;
	protected final List<KeyframeQuery> queries;
	public final AnimInstructionTimelines instructionTimelines;
	
//	public float animTime;
	
	public AnimWithExtras(AnimationDefinition anim, @Nullable List<KeyframeQuery> queries, AnimInstructionTimelines instructionTimelines) {
		this.animation = anim;
		this.queries = queries != null ? queries : Collections.emptyList();
		this.instructionTimelines = instructionTimelines;
	}


	public void animate(Model model, LivingEntityRenderState renderState, float seconds, float animSpeed) {
		Model_1_21_2plus _model = (Model_1_21_2plus) model;
		evaluateQueries(renderState);
		for (Map.Entry<String, List<AnimationChannel>> entry : animation.boneAnimations().entrySet()) {
			_model.jojo_ripples$getAnyDescendantWithName(entry.getKey()).ifPresent(modelPart -> {
				animateModelPart(this, modelPart, entry.getValue(), seconds, animSpeed);
			});
		}
	}

	public void animateVanillaPlayer(HumanoidModel<?> humanoidModel, LivingEntityRenderState renderState, float seconds, float animSpeed) {
		evaluateQueries(renderState);
		for (Map.Entry<String, List<AnimationChannel>> entry : animation.boneAnimations().entrySet()) {
			ModelPart modelPart = PlayerModelBends.getModelPartForPlayerAnim(humanoidModel, entry.getKey());
			animateModelPart(this, modelPart, entry.getValue(), seconds, animSpeed);
			OldPlayerModelJank._onAnimate(humanoidModel);
		}
	}
	
	
	/**
	 * @return action anim time in seconds
	 */
	public float getAnimTime(EntityActionRenderState entityAction) {
		float animSeconds = 0;

		boolean appliedPhaseAnim = false;
		boolean usePhaseTime = entityAction.actionPhase != null && this.instructionTimelines.phases != null;
		if (usePhaseTime) {
			ActionPhase taskPhase = entityAction.actionPhase;

			Float2ObjectMap.Entry<AnimActionPhase> curPhase = null;
			Float2ObjectMap.Entry<AnimActionPhase> nextPhase = null;

			@Nonnull Float2ObjectMap.Entry<AnimActionPhase> iterPrevPhase = null;
			for (Float2ObjectMap.Entry<AnimActionPhase> animPhase : this.instructionTimelines.phases.getEntries()) {
				if (taskPhase.ordinal() < animPhase.getValue().phase.ordinal()) {
					curPhase = iterPrevPhase;
					nextPhase = animPhase;
					break;
				}
				iterPrevPhase = animPhase;
			}
			if (iterPrevPhase == null) {
				usePhaseTime = false;
			}
			else {
				if (curPhase == null) {
					if (taskPhase == iterPrevPhase.getValue().phase) {
						curPhase = iterPrevPhase;
					}
				}
				if (curPhase != null) {
					float curPhaseTime = curPhase.getFloatKey();
					float nextPhaseTime = nextPhase != null ? nextPhase.getFloatKey() : this.animation.lengthInSeconds();
					switch (curPhase.getValue().timeAnimMode) {
					case FIT_PHASE_LENGTH -> {
						animSeconds = Mth.lerp(entityAction.phaseCompletion, curPhaseTime, nextPhaseTime);
						appliedPhaseAnim = true;
					}
					case CONSTANT_LENGTH -> {
						animSeconds = curPhaseTime + entityAction.phaseTime / 20f;
//						if (entity != null && animSeconds >= this.animation.lengthInSeconds()) {
//							entity.onSetPoseAnimEnded();
//						}
						appliedPhaseAnim = true;
					}
					case LOOP_BACK -> {
						float loopLen = nextPhaseTime - curPhase.getValue().loopBackTo;
						animSeconds = curPhaseTime + (entityAction.phaseTime / 20f) % loopLen;
						appliedPhaseAnim = true;
					}
					}
				}
			}
		}

		if (!appliedPhaseAnim) {
			float time = usePhaseTime ? entityAction.phaseTime : entityAction.time;
			animSeconds = this.animation.looping() ? (time / 20f) % this.animation.lengthInSeconds() : time / 20f;
		}
		return animSeconds;
	}
	
	/**
	 * @return anim time in seconds
	 */
	public float getAnimTime(float ticks) {
		return animation.looping() ? (ticks / 20.0f) % animation.lengthInSeconds() : ticks / 20.0f;
	}
	
	
	public static void animateModelPart(AnimWithExtras anim, ModelPart modelPart, List<AnimationChannel> transformations, float seconds, float animSpeed) {
		if (modelPart == null || !modelPart.visible) return;
		for (AnimationChannel tf : transformations) {
			Vector3f vec = calcVec(anim, tf, seconds, animSpeed);
			setTargetValue(modelPart, vec, tf.target());
		}
	}

	protected static final Vector3f TEMP = new Vector3f();
	
	public static Vector3f calcVec(AnimWithExtras anim, AnimationChannel tf, float seconds, float animSpeed) {
		Keyframe[] keyframes = tf.keyframes();
		anim.lerpKeyframes(keyframes, seconds, animSpeed);
		if (tf.target() == AnimationChannel.Targets.ROTATION) {
			TEMP.mul(MathUtil.DEG_TO_RAD);
		}
		else if (tf.target() == AnimationChannel.Targets.POSITION) {
			TEMP.mul(1, -1, 1);
		}
		return TEMP;
	}
	
	public Vector3f lerpKeyframes(Keyframe[] keyframes, float seconds, float animSpeed) {
		int i = Math.max(0, Mth.binarySearch(0, keyframes.length, index -> seconds <= keyframes[index].timestamp()) - 1);
		int j = Math.min(keyframes.length - 1, i + 1);
		Keyframe keyframe = keyframes[i];
		Keyframe keyframe2 = keyframes[j];
		float h = seconds - keyframe.timestamp();
		float k = j != i ? Mth.clamp(h / (keyframe2.timestamp() - keyframe.timestamp()), 0.0f, 1.0f) : 0.0f;
		keyframe2.interpolation().apply(TEMP, k, keyframes, i, j, animSpeed);
		return TEMP;
	}
	
	public static void setTargetValue(ModelPart modelPart, Vector3f value, AnimationChannel.Target target) {
		resetChannel(modelPart, target);
		target.apply(modelPart, value);
	}
	
	public static void resetChannel(ModelPart modelPart, AnimationChannel.Target target) {
		PartPose initialPose = modelPart.getInitialPose();
		// this ain't an enum
		if (target == AnimationChannel.Targets.ROTATION) {
			modelPart.xRot = initialPose.xRot;
			modelPart.yRot = initialPose.yRot;
			modelPart.zRot = initialPose.zRot;
		}
		else if (target == AnimationChannel.Targets.POSITION) {
			modelPart.x = initialPose.x;
			modelPart.y = initialPose.y;
			modelPart.z = initialPose.z;
		}
		else if (target == AnimationChannel.Targets.SCALE) {
			modelPart.xScale = _PartPose.xScale(initialPose);
			modelPart.yScale = _PartPose.yScale(initialPose);
			modelPart.zScale = _PartPose.zScale(initialPose);
		}
	}
	
	
	private void evaluateQueries(LivingEntityRenderState renderState) {
		AnimMolangQuery.instance.fillContext(renderState);
		queries.forEach(KeyframeQuery::evaluate);
	}
	

	public static class TimelineKeys {
		public static final String BARRAGE = "barrage";
	}
	
	
	public static class Builder {
		protected final AnimationDefinition.Builder vanillaAnimBuilder;
		protected List<KeyframeQuery> queries = null;
		protected final AnimInstructionTimelines instructions = new AnimInstructionTimelines();
		
		public Builder(AnimationDefinition.Builder vanillaAnimBuilder) {
			this.vanillaAnimBuilder = vanillaAnimBuilder;
		}
		
		public AnimationDefinition.Builder anim() {
			return vanillaAnimBuilder;
		}
		
		public void addExpressionQuery(KeyframeQuery query) {
			if (queries == null) queries = new ArrayList<>();
			if (!query.isNumericLiteral()) {
				queries.add(query);
			}
		}
		
		public void addActionPhaseKeyframe(AnimActionPhase value, float time) {
			if (instructions.phases == null) {
				instructions.phases = new AnimObjTimeline<>();
			}
			instructions.phases.add(time, value);
		}
		
		public void addFieldValueKeyframe(String field, String value, float time) {
			if (instructions.stringVals == null) {
				instructions.stringVals = new HashMap<>();
			}
			AnimObjTimeline<String> timeline = instructions.stringVals.computeIfAbsent(field, __ -> new AnimObjTimeline<>());
			timeline.add(time, value);
		}
		
		public AnimWithExtras build() {
			instructions.onFinishedParsing();
			AnimWithExtras anim = new AnimWithExtras(vanillaAnimBuilder.build(), queries, instructions);
			return anim;
		}
	}
	
}
