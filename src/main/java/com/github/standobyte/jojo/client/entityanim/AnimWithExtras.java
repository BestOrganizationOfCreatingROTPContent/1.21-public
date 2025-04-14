package com.github.standobyte.jojo.client.entityanim;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityanim.action.AnimActionPhase;
import com.github.standobyte.jojo.client.entityanim.action.AnimObjTimeline;
import com.github.standobyte.jojo.client.entityanim.molang.AnimMolangQuery;
import com.github.standobyte.jojo.client.entityanim.molang.KeyframeQuery;
import com.github.standobyte.jojo.client.entityanim.playerbend.PlayerModelBends;
import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class AnimWithExtras {
	protected final AnimationDefinition animation;
	protected final Map<Keyframe, KeyframeQuery> queries;
	
	@Nullable protected AnimObjTimeline<AnimActionPhase> phasesTimeline;
	@Nullable protected Map<String, AnimObjTimeline<String>> stringValTimelines = new HashMap<>();
	@Nullable protected Map<String, AnimObjTimeline<Double>> numericValTimelines = new HashMap<>();
	
//	public float animTime;
	
	public AnimWithExtras(AnimationDefinition anim, Map<Keyframe, KeyframeQuery> queries) {
		this.animation = anim;
		this.queries = queries != null ? queries : Collections.emptyMap();
	}

	
	protected static final Vector3f TEMP = new Vector3f();
	
	public void animate(Model model, LivingEntityRenderState renderState, float ticks, float animSpeed) {
		evaluateQueries(renderState);
		float seconds = animation.looping() ? (ticks / 20.0f) % animation.lengthInSeconds() : ticks / 20.0f;
		
		for (Map.Entry<String, List<AnimationChannel>> entry : animation.boneAnimations().entrySet()) {
			model.getAnyDescendantWithName(entry.getKey()).ifPresent(modelPart -> {
				animateModelPart(this, modelPart, entry.getValue(), seconds, animSpeed);
			});
		}
	}

	public void animatePlayer(HumanoidModel<?> humanoidModel, LivingEntityRenderState renderState, float ticks, float animSpeed) {
		evaluateQueries(renderState);
		float seconds = animation.looping() ? (ticks / 20.0f) % animation.lengthInSeconds() : ticks / 20.0f;

		for (Map.Entry<String, List<AnimationChannel>> entry : animation.boneAnimations().entrySet()) {
			ModelPart modelPart = PlayerModelBends.getModelPartForPlayerAnim(humanoidModel, entry.getKey());
			if (modelPart != null) {
				animateModelPart(this, modelPart, entry.getValue(), seconds, animSpeed);
			}
		}
	}
	
	
	public static void animateModelPart(AnimWithExtras anim, ModelPart modelPart, List<AnimationChannel> transformations, float seconds, float animSpeed) {
		if (modelPart == null) return;
		for (AnimationChannel tf : transformations) {
			Vector3f vec = calcVec(anim, tf, seconds, animSpeed);
			setTargetValue(modelPart, vec, tf.target());
		}
	}
	
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
			modelPart.xRot = initialPose.xRot();
			modelPart.yRot = initialPose.yRot();
			modelPart.zRot = initialPose.zRot();
		}
		else if (target == AnimationChannel.Targets.POSITION) {
			modelPart.x = initialPose.x();
			modelPart.y = initialPose.y();
			modelPart.z = initialPose.z();
		}
		else if (target == AnimationChannel.Targets.SCALE) {
			modelPart.xScale = initialPose.xScale();
			modelPart.yScale = initialPose.yScale();
			modelPart.zScale = initialPose.zScale();
		}
	}
	
	
	private void evaluateQueries(LivingEntityRenderState renderState) {
		AnimMolangQuery.instance.fillContext(renderState);
		queries.values().forEach(KeyframeQuery::evaluate);
	}
	
	
	// TODO (!) (entity anims) phases stuff (EntityActionRenderStateExtension)
	
	
	// TODO (!) (entity anims) parse instructions
	public static class Builder {
		protected final AnimationDefinition.Builder vanillaAnimBuilder;
		protected final Map<Keyframe, KeyframeQuery> queries = new HashMap<>();
		
		public Builder(AnimationDefinition.Builder vanillaAnimBuilder) {
			this.vanillaAnimBuilder = vanillaAnimBuilder;
		}
		
		public AnimationDefinition.Builder anim() {
			return vanillaAnimBuilder;
		}
		
		public void addExpressionQuery(KeyframeQuery query) {
			if (!query.isNumericLiteral()) {
				queries.put(query.getKeyframe(), query);
			}
		}
		
		public AnimWithExtras build() {
			return new AnimWithExtras(vanillaAnimBuilder.build(), queries);
		}
	}
	
	

//	public void poseStand(@Nullable StandEntity entity, StandEntityModel<?> model, 
//			float yRotOffsetDeg, float xRotDeg, StandPoseData poseData) {
//		boolean appliedPhaseAnim = false;
//		if (poseData.actionPhase.isPresent() && phasesTimeline != null) {
//			Phase taskPhase = poseData.actionPhase.get();
//			
//			Float2ObjectMap.Entry<AnimActionPhase> curPhase = null;
//			Float2ObjectMap.Entry<AnimActionPhase> nextPhase = null;
//			
//			@Nonnull Float2ObjectMap.Entry<AnimActionPhase> iterPrevPhase = null;
//			for (Float2ObjectMap.Entry<AnimActionPhase> animPhase : phasesTimeline.getEntries()) {
//				if (taskPhase.ordinal() < animPhase.getValue().phase.ordinal()) {
//					curPhase = iterPrevPhase;
//					nextPhase = animPhase;
//					break;
//				}
//				iterPrevPhase = animPhase;
//			}
//			if (curPhase == null) {
//				if (taskPhase == iterPrevPhase.getValue().phase) {
//					curPhase = iterPrevPhase;
//				}
//			}
//			if (curPhase != null) {
//				float curPhaseTime = curPhase.getFloatKey();
//				float nextPhaseTime = nextPhase != null ? nextPhase.getFloatKey() : animation.lengthInSeconds();
//				switch (curPhase.getValue().timeAnimMode) {
//				case FIT_PHASE_LENGTH:
//					animTime = MathHelper.lerp(poseData.phaseCompletion, curPhaseTime, nextPhaseTime);
//					break;
//				case PRESERVE_PHASE_LENGTH:
//					animTime = curPhaseTime + poseData.animTime / 20f;
//					if (entity != null && animTime >= animation.lengthInSeconds()) {
//						entity.onSetPoseAnimEnded();
//					}
//					break;
//				case LOOP_BACK:
//					float loopLen = nextPhaseTime - curPhase.getValue().loopBackTo;
//					animTime = curPhaseTime + (poseData.animTime / 20f) % loopLen;
//					break;
//				default:
//					break;
//				}
//				appliedPhaseAnim = true;
//			}
//		}
//		
//		if (!appliedPhaseAnim) {
//			animTime = animation.looping() ? (poseData.animTime / 20f) % animation.lengthInSeconds() : poseData.animTime / 20f;
//		}
//		
//		AnimContext animContext = AnimContext.fillContext(entity, yRotOffsetDeg, xRotDeg);
//		GeckoStandAnimator.animateSecs(model, animation, animTime, ANIM_SPEED, animContext);
//	}
//	
//	
//	public void parseAssignmentInstruction(String field, String value, float keyframeTime, Map<String, String> assignmentMap) {
//		switch (field) {
//		case "phase":
//			Phase phase = Phase.valueOf(value);
//			if (phasesTimeline == null) {
//				phasesTimeline = new AnimObjTimeline<>();
//			}
//			AnimActionPhase animPhase = parseAnimPhase(phase, assignmentMap);
//			phasesTimeline.add(keyframeTime, animPhase);
//			break;
//		default:
//			if (stringValTimelines == null) {
//				stringValTimelines = new HashMap<>();
//			}
//			AnimObjTimeline<String> timeline = stringValTimelines.computeIfAbsent(field, __ -> new AnimObjTimeline<>());
//			timeline.add(keyframeTime, value);
//			break;
//		}
//	}
//	
//	protected AnimActionPhase parseAnimPhase(Phase phase, Map<String, String> assignmentMap) {
//		if (assignmentMap.containsKey("phase.loopBack")) {
//			try {
//				float loopBackTo = Float.parseFloat(assignmentMap.get("phase.loopBack"));
//				assignmentMap.remove("phase.loopBack");
//				return AnimActionPhase.loopBack(phase, loopBackTo);
//			}
//			catch (NumberFormatException e) {}
//		}
//		return new AnimActionPhase(phase, AnimActionPhase.Mode.FIT_PHASE_LENGTH);
//	}
//	
//	public void onFinishedParsing() {
//		if (stringValTimelines != null) {
//			Iterator<Map.Entry<String, AnimObjTimeline<String>>> iter = stringValTimelines.entrySet().iterator();
//			while (iter.hasNext()) {
//				Map.Entry<String, AnimObjTimeline<String>> entry = iter.next();
//				timelineToNumeric(entry.getValue()).ifPresent(numericTimeline -> {
//					if (numericValTimelines == null) {
//						numericValTimelines = new HashMap<>();
//					}
//					numericValTimelines.put(entry.getKey(), numericTimeline);
//					iter.remove();
//				});
//			}
//		}
//		
//		if (phasesTimeline != null) phasesTimeline.sort();
//		if (stringValTimelines != null) stringValTimelines.values().forEach(AnimObjTimeline::sort);
//		if (numericValTimelines != null) numericValTimelines.values().forEach(AnimObjTimeline::sort);
//	}
//	
//	private static Optional<AnimObjTimeline<Double>> timelineToNumeric(AnimObjTimeline<String> stringTimeline) {
//		AnimObjTimeline<Double> timeline = new AnimObjTimeline<>();
//		for (Float2ObjectMap.Entry<String> entry : stringTimeline.getEntries()) {
//			try {
//				double numericVal = Double.parseDouble(entry.getValue());
//				timeline.add(entry.getFloatKey(), numericVal);
//			}
//			catch (NumberFormatException notNumeric) {
//				return Optional.empty();
//			}
//		}
//		return Optional.of(timeline);
//	}
//	
//	@Nullable
//	public String getStringTimelineVal(String key, float animTime) {
//		if (stringValTimelines == null) {
//			return null;
//		}
//		AnimObjTimeline<String> timeline = stringValTimelines.get(key);
//		if (timeline == null) {
//			return null;
//		}
//		return timeline.getCurValue(animTime);
//	}
//	
//	@Nullable
//	public Double getNumericTimelineVal(String key, float animTime) {
//		if (numericValTimelines == null) {
//			return null;
//		}
//		AnimObjTimeline<Double> timeline = numericValTimelines.get(key);
//		if (timeline == null) {
//			return null;
//		}
//		return timeline.getCurValue(animTime);
//	}
//	
//	public static class TimelineKeys {
//		public static final String BARRAGE = "barrage";
//	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
