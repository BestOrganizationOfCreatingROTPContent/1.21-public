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
	private ModelPart jojoROABendBone;
	private float jojoROABendOffsetX;
	private float jojoROABendOffsetY;
	private float jojoROABendOffsetZ;
	private boolean jojoROAInvertBend = false;
	
	
	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", 
			at = @At(value = "INVOKE", target = "translateAndRotate", shift = Shift.AFTER), 
			cancellable = true)
	private void jojoROACubesCompile(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if (jojoROABendBone != null && (jojoROABendBone.xRot != 0.0F || jojoROABendBone.yRot != 0.0F || jojoROABendBone.zRot != 0.0F)) {
			ModelPart asModelPart = (ModelPart) (Object) this;
			PlayerModelBends.drawBentCubes(asModelPart, jojoROABendBone, jojoROAInvertBend, 
					jojoROABendOffsetX, jojoROABendOffsetY, jojoROABendOffsetZ,
					skipDraw, poseStack, 
					buffer, packedLight, packedOverlay, color);
			poseStack.popPose();
			ci.cancel();
		}
	}
	
	@Override
	public void jojoROASetBendBone(ModelPart bendBone, boolean invertBend) {
		jojoROASetBendBone(bendBone, 0, 0, 0, invertBend);
	}
	
	public void jojoROASetBendBone(ModelPart bendBone, float bendOffsetX, float bendOffsetY, float bendOffsetZ, boolean invertBend) {
		this.jojoROABendBone = bendBone;
		this.jojoROABendOffsetX = bendOffsetX;
		this.jojoROABendOffsetY = bendOffsetY;
		this.jojoROABendOffsetZ = bendOffsetZ;
		this.jojoROAInvertBend = invertBend;
		for (ModelPart modelPart : children.values()) {
			((ModelPartMixin) (Object) modelPart).jojoROASetBendBone(bendBone, 
					bendOffsetX - this.initialPose.x(), 
					bendOffsetY - this.initialPose.y(), 
					bendOffsetZ - this.initialPose.z(), 
					invertBend);
		}
	}
	
	@Override
	public ModelPart jojoROAGetBendBone() {
		return jojoROABendBone;
	}

	@Inject(method = "resetPose", at = @At("TAIL"))
	public void jojoROAOnResetPose(CallbackInfo ci) {
		if (this.jojoROABendBone != null) {
			this.jojoROABendBone.resetPose();
		}
	}
	
	@Inject(method = "copyFrom", at = @At("TAIL"))
	public void jojoROAOnCopyPose(ModelPart modelPart, CallbackInfo ci) {
		if (this.jojoROABendBone != null) {
			ModelPart bend = ((ModelPartMixin) (Object) modelPart).jojoROABendBone;
			if (bend != null) {
				this.jojoROABendBone.copyFrom(bend);
			}
		}
	}
	
}
