package com.github.standobyte.jojo.mixin.client.debug;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.utils.RGBUtil;
import com.github.standobyte.jojo.util.target.HitResultUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

	@Inject(method = "renderHitbox", at = @At("TAIL"))
	private static void jojo_ripples$standPrecisionBox(PoseStack poseStack, VertexConsumer buffer, Entity p_entity, float red, float green, float blue, float alpha, CallbackInfo ci) {
		if (ClientGlobals.standPrecision > 0 && ClientGlobals.playerStandEntity != null && p_entity != ClientGlobals.playerStandEntity && p_entity != Minecraft.getInstance().player) {
	        AABB aabb = p_entity.getBoundingBox().move(-p_entity.getX(), -p_entity.getY(), -p_entity.getZ());
	        AABB precisionAABB = HitResultUtil.standPrecisionTargetHitbox(aabb, ClientGlobals.standPrecision);
	        StandSkin skin = StandSkinsLoader.getInstance().getSkin(ClientGlobals.playerStandEntity);
	        float[] color = RGBUtil.argb(skin.color);
	        ShapeRenderer.renderLineBox(poseStack, buffer, precisionAABB, color[0], color[1], color[2], 1);
		}
	}
}
