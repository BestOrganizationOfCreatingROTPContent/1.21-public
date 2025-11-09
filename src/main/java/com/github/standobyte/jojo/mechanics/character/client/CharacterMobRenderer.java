package com.github.standobyte.jojo.mechanics.character.client;

import java.text.DecimalFormat;

import com.github.standobyte.jojo.mechanics.character.mob.PowerUserMobEntity;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class CharacterMobRenderer<T extends PowerUserMobEntity> extends LivingEntityRenderer<T, EntityModel<T>> {
	protected EntityModel<T> regularPlayerModel;
	protected EntityModel<T> slimPlayerModel;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CharacterMobRenderer(Context context) {
		super(context, new PlayerModel<T>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
		this.regularPlayerModel = this.model;
		this.slimPlayerModel = new PlayerModel<T>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

		this.addLayer(new HumanoidArmorLayer(this,
				new HumanoidArmorModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
				new HumanoidArmorModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
				context.getModelManager()));
		this.addLayer(new PlayerItemInHandLayer(this, context.getItemInHandRenderer()));
		this.addLayer(new CustomHeadLayer(this, context.getModelSet(), context.getItemInHandRenderer()));
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return entity.clientStuff.getTexture(entity);
	}

	@Override
	public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		PlayerSkin.Model modelType = entity.clientStuff.getModelType(entity);
		this.model = switch (modelType) {
			case WIDE -> regularPlayerModel;
			case SLIM -> slimPlayerModel;
		};
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
	}

	@Override
	protected void scale(T livingEntity, PoseStack poseStack, float partialTickTime) {
		boolean usingPlayerModel = true;
		if (usingPlayerModel) {
			poseStack.scale(0.9375F, 0.9375F, 0.9375F);
		}
	}

}
