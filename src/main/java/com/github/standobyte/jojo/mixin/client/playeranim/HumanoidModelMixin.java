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
	private ModelPart jojoROAAnimMainBody;
	private ModelPart jojoROAAnimTorso;
	private ModelPart jojoROAAnimTorsoBend;
	private ModelPart jojoROAAnimRightArmBend;
	private ModelPart jojoROAAnimLeftArmBend;
	private ModelPart jojoROAAnimRightLegBend;
	private ModelPart jojoROAAnimLeftLegBend;
	private ModelPart jojoROAAnimRightItem;
	private ModelPart jojoROAAnimLeftItem;
	private ModelPart jojoROAAnimCapeBend;
	private boolean jojoROAPlayerAnim;
	
	@Inject(method = "<init>("
			+ "Lnet/minecraft/client/model/geom/ModelPart;"
			+ "Ljava/util/function/Function;)V", at = @At("RETURN"))
	private void jojoROAInitModel(ModelPart root, Function<ResourceLocation, RenderType> renderType, CallbackInfo ci) {
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
		
		jojoROAAnimMainBody = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojoROAAnimMainBody.setInitialPose(PartPose.offset(0, legLength, 0));
		jojoROAAnimTorsoBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojoROAAnimTorsoBend.setInitialPose(PartPose.offset(0, (torsoLength - torsoLengthUpper), 0));
		jojoROAAnimTorso = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojoROAAnimTorso.setInitialPose(PartPose.offset(0, torsoLengthUpper, 0));
		jojoROAAnimRightArmBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojoROAAnimRightArmBend.setInitialPose(PartPose.offset(0, rightArmLengthUpper - rightArmPos, 0));
		jojoROAAnimLeftArmBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojoROAAnimLeftArmBend.setInitialPose(PartPose.offset(0, leftArmLengthUpper - leftArmPos, 0));
		jojoROAAnimRightLegBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojoROAAnimRightLegBend.setInitialPose(PartPose.offset(0, legLengthUpper, 0));
		jojoROAAnimLeftLegBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojoROAAnimLeftLegBend.setInitialPose(PartPose.offset(0, legLengthUpper, 0));
		jojoROAAnimRightItem = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojoROAAnimRightItem.setInitialPose(PartPose.offset(0, rightItemPos, 2));
		jojoROAAnimLeftItem = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojoROAAnimLeftItem.setInitialPose(PartPose.offset(0, leftItemPos, 2));
		jojoROAAnimCapeBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		jojoROAAnimCapeBend.setInitialPose(PartPose.offset(0, torsoLengthUpper, 0));
		((IPlayerLimbBend) (Object) body).jojoROASetBendBone(jojoROAAnimTorsoBend, true);
		((IPlayerLimbBend) (Object) rightArm).jojoROASetBendBone(jojoROAAnimRightArmBend, false);
		((IPlayerLimbBend) (Object) leftArm).jojoROASetBendBone(jojoROAAnimLeftArmBend, false);
		((IPlayerLimbBend) (Object) rightLeg).jojoROASetBendBone(jojoROAAnimRightLegBend, false);
		((IPlayerLimbBend) (Object) leftLeg).jojoROASetBendBone(jojoROAAnimLeftLegBend, false);
		setBend(body, "cape", jojoROAAnimCapeBend, false);
	}
	
	private static void setBend(ModelPart parent, String modelPartName, ModelPart bendBone, boolean invertBend) {
		ModelPart modelPart = parent.children.get(modelPartName);
		if (modelPart != null) {
			((IPlayerLimbBend) (Object) modelPart).jojoROASetBendBone(bendBone, invertBend);
		}
	}

	@Override
	public void jojoROASetupHumanoidAnim(HumanoidRenderState renderState) {
		RipplesPlayerRenderState jojoROARenderState = ((RipplesRenderStateExtensionMixin) renderState).get();
		this.jojoROAPlayerAnim = RipplesPlayerRenderState.setupAnim((HumanoidModel<?>) (Object) this, renderState, jojoROARenderState);
	}
	
	@Override
	public void jojoROARenderWithBends(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if (this.jojoROAPlayerAnim) {
			PlayerModelBends.renderWithBends((HumanoidModel<?>) (Object) this, this, 
					poseStack, buffer, 
					packedLight, packedOverlay, color);
			ci.cancel();
		}
	}
	
	
	@Override
	public void jojoROAResetPose(CallbackInfo ci) {
		jojoROAAnimMainBody.resetPose();
		jojoROAAnimTorso.resetPose();
		jojoROAAnimRightItem.resetPose();
		jojoROAAnimLeftItem.resetPose();
		// the bends are reset from their respective bone parts (ModelPartMixin#jojoROAOnResetPose)
	}

	@Inject(method = "copyPropertiesTo", at = @At("HEAD"))
	public void jojoROACopyPose(HumanoidModel<?> _model, CallbackInfo ci) {
//		IPlayerPseudoModelParts model = (IPlayerPseudoModelParts) _model;
		// TODO can i actually cast to a mixin class? test a build
		HumanoidModelMixin model = (HumanoidModelMixin) (IPlayerPseudoModelParts) _model;
		model.jojoROAAnimMainBody().copyFrom(this.jojoROAAnimMainBody);
		model.jojoROAAnimTorso().copyFrom(this.jojoROAAnimTorso);
		model.jojoROAAnimTorsoBend().copyFrom(this.jojoROAAnimTorsoBend);
		model.jojoROAAnimRightArmBend().copyFrom(this.jojoROAAnimRightArmBend);
		model.jojoROAAnimLeftArmBend().copyFrom(this.jojoROAAnimLeftArmBend);
		model.jojoROAAnimRightLegBend().copyFrom(this.jojoROAAnimRightLegBend);
		model.jojoROAAnimLeftLegBend().copyFrom(this.jojoROAAnimLeftLegBend);
		model.jojoROAAnimRightItem().copyFrom(this.jojoROAAnimRightItem);
		model.jojoROAAnimLeftItem().copyFrom(this.jojoROAAnimLeftItem);
		model.jojoROAAnimCapeBend().copyFrom(this.jojoROAAnimCapeBend);
		model.jojoROAPlayerAnim = this.jojoROAPlayerAnim;
	}
	
	@Override public ModelPart jojoROAAnimMainBody() { return jojoROAAnimMainBody; }
	@Override public ModelPart jojoROAAnimTorso() { return jojoROAAnimTorso; }
	@Override public ModelPart jojoROAAnimTorsoBend() { return jojoROAAnimTorsoBend; }
	@Override public ModelPart jojoROAAnimRightArmBend() { return jojoROAAnimRightArmBend; }
	@Override public ModelPart jojoROAAnimLeftArmBend() { return jojoROAAnimLeftArmBend; }
	@Override public ModelPart jojoROAAnimRightLegBend() { return jojoROAAnimRightLegBend; }
	@Override public ModelPart jojoROAAnimLeftLegBend() { return jojoROAAnimLeftLegBend; }
	@Override public ModelPart jojoROAAnimRightItem() { return jojoROAAnimRightItem; }
	@Override public ModelPart jojoROAAnimLeftItem() { return jojoROAAnimLeftItem; }
	@Override public ModelPart jojoROAAnimCapeBend() { return jojoROAAnimCapeBend; }
	
}
