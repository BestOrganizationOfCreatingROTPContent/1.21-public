package com.github.standobyte.jojo.mixin.client.playeranim;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.IHumanoidAnimModel;
import com.github.standobyte.jojo.client.entityanim.RipplesPlayerRenderState;
import com.github.standobyte.jojo.client.entityanim.RipplesPlayerRenderState.RipplesRenderStateExtensionMixin;
import com.github.standobyte.jojo.client.entityanim.playerbend.IPlayerLimbBend;
import com.github.standobyte.jojo.client.entityanim.playerbend.IPlayerPseudoModelParts;
import com.github.standobyte.jojo.client.entityanim.playerbend.PlayerModelBends;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceLocation;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin extends ModelMixin implements IHumanoidAnimModel, IPlayerPseudoModelParts {
	@Shadow @Final public ModelPart body;
	@Shadow @Final public ModelPart rightArm;
	@Shadow @Final public ModelPart leftArm;
	@Shadow @Final public ModelPart rightLeg;
	@Shadow @Final public ModelPart leftLeg;
	private ModelPart jojo_ripples$animMainBody;
	private ModelPart jojo_ripples$animTorso;
	private ModelPart jojo_ripples$animTorsoBend;
	private ModelPart jojo_ripples$animRightArmBend;
	private ModelPart jojo_ripples$animLeftArmBend;
	private ModelPart jojo_ripples$animRightLegBend;
	private ModelPart jojo_ripples$animLeftLegBend;
	private ModelPart jojo_ripples$animRightItem;
	private ModelPart jojo_ripples$animLeftItem;
	private ModelPart jojo_ripples$animCapeBend;
	private boolean jojo_ripples$playerAnim;
	
	@Inject(method = "<init>("
			+ "Lnet/minecraft/client/model/geom/ModelPart;"
			+ "Ljava/util/function/Function;)V", at = @At("RETURN"))
	private void jojo_ripples$initModel(ModelPart root, Function<ResourceLocation, RenderType> renderType, CallbackInfo ci) {
		float torsoLength = PlayerModelBends.getLimbHeight(body);				// 12
		float torsoLengthUpper = torsoLength / 2;								// 6
		float rightArmLength = PlayerModelBends.getLimbHeight(rightArm);		// 12
		float rightArmLengthUpper = rightArmLength / 2;							// 6
		float leftArmLength = PlayerModelBends.getLimbHeight(leftArm);			// 12
		float leftArmLengthUpper = leftArmLength / 2;							// 6
		float legLength = PlayerModelBends.getLimbHeight(rightLeg);				// 12
		float legLengthUpper = legLength / 2;									// 6
		float leftArmPos = leftArm.y;											// 2
		float rightArmPos = rightArm.y;											// 2
		float leftItemPos = (leftArmLength - leftArmLengthUpper) - 2.25f;		// 3.75
		float rightItemPos = (rightArmLength - rightArmLengthUpper) - 2.25f;	// 3.75
		
		jojo_ripples$animMainBody = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojo_ripples$animMainBody.setInitialPose(PartPose.offset(0, legLength, 0));
		jojo_ripples$animTorsoBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojo_ripples$animTorsoBend.setInitialPose(PartPose.offset(0, (torsoLength - torsoLengthUpper), 0));
		jojo_ripples$animTorso = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojo_ripples$animTorso.setInitialPose(PartPose.offset(0, torsoLengthUpper, 0));
		jojo_ripples$animRightArmBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojo_ripples$animRightArmBend.setInitialPose(PartPose.offset(0, rightArmLengthUpper - rightArmPos, 0));
		jojo_ripples$animLeftArmBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojo_ripples$animLeftArmBend.setInitialPose(PartPose.offset(0, leftArmLengthUpper - leftArmPos, 0));
		jojo_ripples$animRightLegBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojo_ripples$animRightLegBend.setInitialPose(PartPose.offset(0, legLengthUpper, 0));
		jojo_ripples$animLeftLegBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojo_ripples$animLeftLegBend.setInitialPose(PartPose.offset(0, legLengthUpper, 0));
		jojo_ripples$animRightItem = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojo_ripples$animRightItem.setInitialPose(PartPose.offset(0, rightItemPos, 2));
		jojo_ripples$animLeftItem = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojo_ripples$animLeftItem.setInitialPose(PartPose.offset(0, leftItemPos, 2));
		jojo_ripples$animCapeBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojo_ripples$animCapeBend.setInitialPose(PartPose.offset(0, torsoLengthUpper, 0));
		((IPlayerLimbBend) (Object) body).jojo_ripples$setBendBone(jojo_ripples$animTorsoBend, true);
		((IPlayerLimbBend) (Object) rightArm).jojo_ripples$setBendBone(jojo_ripples$animRightArmBend, false);
		((IPlayerLimbBend) (Object) leftArm).jojo_ripples$setBendBone(jojo_ripples$animLeftArmBend, false);
		((IPlayerLimbBend) (Object) rightLeg).jojo_ripples$setBendBone(jojo_ripples$animRightLegBend, false);
		((IPlayerLimbBend) (Object) leftLeg).jojo_ripples$setBendBone(jojo_ripples$animLeftLegBend, false);
		setBend(body, "cape", jojo_ripples$animCapeBend, false);
	}
	
	private static void setBend(ModelPart parent, String modelPartName, ModelPart bendBone, boolean invertBend) {
		ModelPart modelPart = parent.children.get(modelPartName);
		if (modelPart != null) {
			((IPlayerLimbBend) (Object) modelPart).jojo_ripples$setBendBone(bendBone, invertBend);
		}
	}

	@Override
	public void jojo_ripples$setupHumanoidAnim(HumanoidRenderState renderState) {
		RipplesPlayerRenderState jojoRenderState = ((RipplesRenderStateExtensionMixin) renderState).get();
		this.jojo_ripples$playerAnim = RipplesPlayerRenderState.setupAnim((HumanoidModel<?>) (Object) this, renderState, jojoRenderState);
	}
	
	@Override
	public void jojo_ripples$renderWithBends(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if (this.jojo_ripples$playerAnim) {
			PlayerModelBends.renderWithBends((HumanoidModel<?>) (Object) this, this, 
					poseStack, buffer, 
					packedLight, packedOverlay, color);
			ci.cancel();
		}
	}
	
	
	@Override
	public void jojo_ripples$resetPose(CallbackInfo ci) {
		jojo_ripples$animMainBody.resetPose();
		jojo_ripples$animTorso.resetPose();
		jojo_ripples$animRightItem.resetPose();
		jojo_ripples$animLeftItem.resetPose();
		// the bends are reset from their respective bone parts (ModelPartMixin#jojo_ripples$onResetPose)
	}

	@Inject(method = "copyPropertiesTo", at = @At("HEAD"))
	public void jojo_ripples$copyPose(HumanoidModel<?> _model, CallbackInfo ci) {
//		IPlayerPseudoModelParts model = (IPlayerPseudoModelParts) _model;
		// TODO can i actually cast to a mixin class? test a build
		HumanoidModelMixin model = (HumanoidModelMixin) (IPlayerPseudoModelParts) _model;
		model.jojo_ripples$animMainBody().copyFrom(this.jojo_ripples$animMainBody);
		model.jojo_ripples$animTorso().copyFrom(this.jojo_ripples$animTorso);
		model.jojo_ripples$animTorsoBend().copyFrom(this.jojo_ripples$animTorsoBend);
		model.jojo_ripples$animRightArmBend().copyFrom(this.jojo_ripples$animRightArmBend);
		model.jojo_ripples$animLeftArmBend().copyFrom(this.jojo_ripples$animLeftArmBend);
		model.jojo_ripples$animRightLegBend().copyFrom(this.jojo_ripples$animRightLegBend);
		model.jojo_ripples$animLeftLegBend().copyFrom(this.jojo_ripples$animLeftLegBend);
		model.jojo_ripples$animRightItem().copyFrom(this.jojo_ripples$animRightItem);
		model.jojo_ripples$animLeftItem().copyFrom(this.jojo_ripples$animLeftItem);
		model.jojo_ripples$animCapeBend().copyFrom(this.jojo_ripples$animCapeBend);
		model.jojo_ripples$playerAnim = this.jojo_ripples$playerAnim;
	}
	
	@Override public ModelPart jojo_ripples$animMainBody() { return jojo_ripples$animMainBody; }
	@Override public ModelPart jojo_ripples$animTorso() { return jojo_ripples$animTorso; }
	@Override public ModelPart jojo_ripples$animTorsoBend() { return jojo_ripples$animTorsoBend; }
	@Override public ModelPart jojo_ripples$animRightArmBend() { return jojo_ripples$animRightArmBend; }
	@Override public ModelPart jojo_ripples$animLeftArmBend() { return jojo_ripples$animLeftArmBend; }
	@Override public ModelPart jojo_ripples$animRightLegBend() { return jojo_ripples$animRightLegBend; }
	@Override public ModelPart jojo_ripples$animLeftLegBend() { return jojo_ripples$animLeftLegBend; }
	@Override public ModelPart jojo_ripples$animRightItem() { return jojo_ripples$animRightItem; }
	@Override public ModelPart jojo_ripples$animLeftItem() { return jojo_ripples$animLeftItem; }
	@Override public ModelPart jojo_ripples$animCapeBend() { return jojo_ripples$animCapeBend; }
	
}
