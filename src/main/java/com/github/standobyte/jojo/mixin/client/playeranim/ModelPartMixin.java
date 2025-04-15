package com.github.standobyte.jojo.mixin.client.playeranim;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.playerbend.IPlayerLimbBend;
import com.github.standobyte.jojo.client.entityanim.playerbend.PlayerModelBends;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

@Mixin(ModelPart.class)
public class ModelPartMixin implements IPlayerLimbBend {
	@Shadow @Final private List<ModelPart.Cube> cubes;
	@Shadow @Final private Map<String, ModelPart> children;
	@Shadow PartPose initialPose;
	@Shadow private boolean skipDraw;
	private ModelPart rotpBendBone;
	private float rotpBendOffsetX;
	private float rotpBendOffsetY;
	private float rotpBendOffsetZ;
	private boolean rotpInvertBend = false;
	
	
	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", 
			at = @At(value = "INVOKE", target = "translateAndRotate", shift = Shift.AFTER), 
			cancellable = true)
	private void rotpCubesCompile(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if (rotpBendBone != null && (rotpBendBone.xRot != 0.0F || rotpBendBone.yRot != 0.0F || rotpBendBone.zRot != 0.0F)) {
			ModelPart asModelPart = (ModelPart) (Object) this;
			PlayerModelBends.drawBentCubes(asModelPart, rotpBendBone, rotpInvertBend, 
					rotpBendOffsetX, rotpBendOffsetY, rotpBendOffsetZ,
					skipDraw, poseStack, 
					buffer, packedLight, packedOverlay, color);
			poseStack.popPose();
			ci.cancel();
		}
	}
	
	@Override
	public void rotpSetBendBone(ModelPart bendBone, boolean invertBend) {
		rotpSetBendBone(bendBone, 0, 0, 0, invertBend);
	}
	
	public void rotpSetBendBone(ModelPart bendBone, float bendOffsetX, float bendOffsetY, float bendOffsetZ, boolean invertBend) {
		this.rotpBendBone = bendBone;
		this.rotpBendOffsetX = bendOffsetX;
		this.rotpBendOffsetY = bendOffsetY;
		this.rotpBendOffsetZ = bendOffsetZ;
		this.rotpInvertBend = invertBend;
		for (ModelPart modelPart : children.values()) {
			((ModelPartMixin) (Object) modelPart).rotpSetBendBone(bendBone, 
					bendOffsetX - this.initialPose.x(), 
					bendOffsetY - this.initialPose.y(), 
					bendOffsetZ - this.initialPose.z(), 
					invertBend);
		}
	}
	
	@Override
	public ModelPart rotpGetBendBone() {
		return rotpBendBone;
	}

	@Inject(method = "resetPose", at = @At("TAIL"))
	public void rotpOnResetPose(CallbackInfo ci) {
		if (this.rotpBendBone != null) {
			this.rotpBendBone.resetPose();
		}
	}
	
	@Inject(method = "copyFrom", at = @At("TAIL"))
	public void rotpOnCopyPose(ModelPart modelPart, CallbackInfo ci) {
		if (this.rotpBendBone != null) {
			ModelPart bend = ((ModelPartMixin) (Object) modelPart).rotpBendBone;
			if (bend != null) {
				this.rotpBendBone.copyFrom(bend);
			}
		}
	}
	
}
