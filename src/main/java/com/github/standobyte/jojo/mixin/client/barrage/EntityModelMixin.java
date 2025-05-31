package com.github.standobyte.jojo.mixin.client.barrage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;

@Mixin(EntityModel.class)
public class EntityModelMixin extends ModelMixin {

	@Override
	public void jojo_ripples$renderBarrageSwings(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {
		if (BarrageSwings.currentlyRendering != null) {
			BarrageSwings.currentlyRendering.renderLayerBarrage((EntityModel<?>) (Object) this, 
					poseStack, buffer, packedLight, packedOverlay, color);
		}
	}
}
