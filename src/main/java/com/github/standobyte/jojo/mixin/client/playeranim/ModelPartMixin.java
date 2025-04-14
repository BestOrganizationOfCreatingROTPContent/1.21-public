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

@Mixin(ModelPart.class)
public class ModelPartMixin implements IPlayerLimbBend {
	@Shadow @Final private List<ModelPart.Cube> cubes;
	@Shadow @Final private Map<String, ModelPart> children;
	@Shadow private boolean skipDraw;
	private ModelPart rotpBendBone;
	private boolean rotpInvertBend = false;
	
	
	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", 
			at = @At(value = "INVOKE", target = "translateAndRotate", shift = Shift.AFTER), 
			cancellable = true)
	private void rotpCubesCompile(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if (rotpBendBone != null && (rotpBendBone.xRot != 0.0F || rotpBendBone.yRot != 0.0F || rotpBendBone.zRot != 0.0F)) {
			if (rotpInvertBend) {
				rotpBendBone.xRot = -rotpBendBone.xRot;
			}
			ModelPart asModelPart = (ModelPart) (Object) this;
			PlayerModelBends.compileCubes(asModelPart, rotpBendBone, skipDraw, 
					poseStack, buffer, packedLight, packedOverlay, color);
			if (rotpInvertBend) {
				rotpBendBone.xRot = -rotpBendBone.xRot;
			}
			poseStack.popPose();
			ci.cancel();
		}
	}
	
	@Override
	public void rotpSetBendBone(ModelPart bendBone, boolean invertBend) {
		this.rotpBendBone = bendBone;
		this.rotpInvertBend = invertBend;
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
