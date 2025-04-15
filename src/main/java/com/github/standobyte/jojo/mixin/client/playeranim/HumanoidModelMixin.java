package com.github.standobyte.jojo.mixin.client.playeranim;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.IHumanoidAnimModel;
import com.github.standobyte.jojo.client.entityanim.RotpPlayerRenderState;
import com.github.standobyte.jojo.client.entityanim.RotpPlayerRenderState.IRotpRenderStateExtension;
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
	private ModelPart rotpAnimMainBody;
	private ModelPart rotpAnimTorso;
	private ModelPart rotpAnimTorsoBend;
	private ModelPart rotpAnimRightArmBend;
	private ModelPart rotpAnimLeftArmBend;
	private ModelPart rotpAnimRightLegBend;
	private ModelPart rotpAnimLeftLegBend;
	private ModelPart rotpAnimRightItem;
	private ModelPart rotpAnimLeftItem;
	private ModelPart rotpAnimCapeBend;
	private boolean rotpPlayerAnim;
	
	@Inject(method = "<init>("
			+ "Lnet/minecraft/client/model/geom/ModelPart;"
			+ "Ljava/util/function/Function;)V", at = @At("RETURN"))
	private void rotpInitModel(ModelPart root, Function<ResourceLocation, RenderType> renderType, CallbackInfo ci) {
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
		
		rotpAnimMainBody = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		rotpAnimMainBody.setInitialPose(PartPose.offset(0, legLength, 0));
		rotpAnimTorsoBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		rotpAnimTorsoBend.setInitialPose(PartPose.offset(0, (torsoLength - torsoLengthUpper), 0));
		rotpAnimTorso = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		rotpAnimTorso.setInitialPose(PartPose.offset(0, torsoLengthUpper, 0));
		rotpAnimRightArmBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		rotpAnimRightArmBend.setInitialPose(PartPose.offset(0, rightArmLengthUpper - rightArmPos, 0));
		rotpAnimLeftArmBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		rotpAnimLeftArmBend.setInitialPose(PartPose.offset(0, leftArmLengthUpper - leftArmPos, 0));
		rotpAnimRightLegBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		rotpAnimRightLegBend.setInitialPose(PartPose.offset(0, legLengthUpper, 0));
		rotpAnimLeftLegBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		rotpAnimLeftLegBend.setInitialPose(PartPose.offset(0, legLengthUpper, 0));
		rotpAnimRightItem = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		rotpAnimRightItem.setInitialPose(PartPose.offset(0, rightItemPos, 2));
		rotpAnimLeftItem = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		rotpAnimLeftItem.setInitialPose(PartPose.offset(0, leftItemPos, 2));
		rotpAnimCapeBend = new ModelPart(ImmutableList.of(), ImmutableMap.of());
		rotpAnimCapeBend.setInitialPose(PartPose.offset(0, torsoLengthUpper, 0));
		((IPlayerLimbBend) (Object) body).rotpSetBendBone(rotpAnimTorsoBend, true);
		((IPlayerLimbBend) (Object) rightArm).rotpSetBendBone(rotpAnimRightArmBend, false);
		((IPlayerLimbBend) (Object) leftArm).rotpSetBendBone(rotpAnimLeftArmBend, false);
		((IPlayerLimbBend) (Object) rightLeg).rotpSetBendBone(rotpAnimRightLegBend, false);
		((IPlayerLimbBend) (Object) leftLeg).rotpSetBendBone(rotpAnimLeftLegBend, false);
		setBend(body, "cape", rotpAnimCapeBend, false);
	}
	
	private static void setBend(ModelPart parent, String modelPartName, ModelPart bendBone, boolean invertBend) {
		ModelPart modelPart = parent.children.get(modelPartName);
		if (modelPart != null) {
			((IPlayerLimbBend) (Object) modelPart).rotpSetBendBone(bendBone, invertBend);
		}
	}

	@Override
	public void rotpSetupHumanoidAnim(HumanoidRenderState renderState) {
		RotpPlayerRenderState rotpRenderState = ((IRotpRenderStateExtension) renderState).get();
		this.rotpPlayerAnim = RotpPlayerRenderState.setupAnim((HumanoidModel<?>) (Object) this, renderState, rotpRenderState);
	}
	
	@Override
	public void rotpRenderWithBends(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if (this.rotpPlayerAnim) {
			PlayerModelBends.renderWithBends((HumanoidModel<?>) (Object) this, this, 
					poseStack, buffer, 
					packedLight, packedOverlay, color);
			ci.cancel();
		}
	}
	
	
	@Override
	public void rotpResetPose(CallbackInfo ci) {
		rotpAnimMainBody.resetPose();
		rotpAnimTorso.resetPose();
		rotpAnimRightItem.resetPose();
		rotpAnimLeftItem.resetPose();
		// the bends are reset from their respective bone parts (ModelPartMixin#rotpOnResetPose)
	}

	@Inject(method = "copyPropertiesTo", at = @At("HEAD"))
	public void rotpCopyPose(HumanoidModel<?> _model, CallbackInfo ci) {
//		IPlayerPseudoModelParts model = (IPlayerPseudoModelParts) _model;
		// TODO can i actually cast to a mixin class? test a build
		HumanoidModelMixin model = (HumanoidModelMixin) (IPlayerPseudoModelParts) _model;
		model.rotpAnimMainBody().copyFrom(this.rotpAnimMainBody);
		model.rotpAnimTorso().copyFrom(this.rotpAnimTorso);
		model.rotpAnimTorsoBend().copyFrom(this.rotpAnimTorsoBend);
		model.rotpAnimRightArmBend().copyFrom(this.rotpAnimRightArmBend);
		model.rotpAnimLeftArmBend().copyFrom(this.rotpAnimLeftArmBend);
		model.rotpAnimRightLegBend().copyFrom(this.rotpAnimRightLegBend);
		model.rotpAnimLeftLegBend().copyFrom(this.rotpAnimLeftLegBend);
		model.rotpAnimRightItem().copyFrom(this.rotpAnimRightItem);
		model.rotpAnimLeftItem().copyFrom(this.rotpAnimLeftItem);
		model.rotpAnimCapeBend().copyFrom(this.rotpAnimCapeBend);
		model.rotpPlayerAnim = this.rotpPlayerAnim;
	}
	
	@Override public ModelPart rotpAnimMainBody() { return rotpAnimMainBody; }
	@Override public ModelPart rotpAnimTorso() { return rotpAnimTorso; }
	@Override public ModelPart rotpAnimTorsoBend() { return rotpAnimTorsoBend; }
	@Override public ModelPart rotpAnimRightArmBend() { return rotpAnimRightArmBend; }
	@Override public ModelPart rotpAnimLeftArmBend() { return rotpAnimLeftArmBend; }
	@Override public ModelPart rotpAnimRightLegBend() { return rotpAnimRightLegBend; }
	@Override public ModelPart rotpAnimLeftLegBend() { return rotpAnimLeftLegBend; }
	@Override public ModelPart rotpAnimRightItem() { return rotpAnimRightItem; }
	@Override public ModelPart rotpAnimLeftItem() { return rotpAnimLeftItem; }
	@Override public ModelPart rotpAnimCapeBend() { return rotpAnimCapeBend; }
	
}
