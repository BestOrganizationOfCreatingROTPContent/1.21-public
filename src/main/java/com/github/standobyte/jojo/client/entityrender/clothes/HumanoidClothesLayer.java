package com.github.standobyte.jojo.client.entityrender.clothes;

import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

// TODO (clothes) disable the outer skin layer under worn clothes pieces
//public class HumanoidClothesLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
public class HumanoidClothesLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private static final ClothesSlotType[] RENDER_ORDER = {
			ClothesSlotType.CHEST,
			ClothesSlotType.HEAD,
			ClothesSlotType.LEGS,
			ClothesSlotType.FEET
	};
	
	public HumanoidClothesLayer(RenderLayerParent<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource bufferSource, 
			int packedLight, T livingEntity,
			float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, 
			float netHeadYaw, float headPitch) {
//	public void render(PoseStack poseStack, MultiBufferSource bufferSource, 
//			int packedLight, S renderState, float yRot, float xRot) {
//		HumanoidClothesRSExtension clothesRS = renderState.getRenderData(ModEntityRenderers.CLOTHES_CONTEXT);
		HumanoidClothesRSExtension clothesRS = HumanoidClothesRSExtension.reusedInstance.extract(livingEntity) ? HumanoidClothesRSExtension.reusedInstance : null;
		if (clothesRS == null) return;
		ClothesModelLoader clothesModels = ClothesModelLoader.getInstance();
		if (clothesModels == null) return;
		M parentModel = getParentModel();
		for (ClothesSlotType piece : RENDER_ORDER) {
			ItemStack clothesItem = clothesRS.items.get(piece); if (clothesItem.isEmpty()) continue;
			var clothesComponent = clothesItem.get(ModItemDataComponents.CLOTHES_PIECE.get()); if (clothesComponent == null) continue;
			var clothesPiece = clothesComponent.getPiece(); if (clothesPiece == null) continue;
			var assetId = clothesPiece.assetId; if (assetId == null) continue;
			var assetPath = assetId.location();
			ClothesModelEntry modelEntry = clothesModels.getClothesModelEntry(assetPath); if (modelEntry == null) continue;
			
			HumanoidClothesModel clothesModel = modelEntry.getModel();
			ResourceLocation clothesTexture = modelEntry.texPath;
			parentModel.copyPropertiesTo((M) clothesModel);
			clothesModel.setClothesPartsVisibility(clothesRS.slimModel, piece);
			clothesModel.poseClothes(parentModel);
			VertexConsumer ivertexbuilder = bufferSource.getBuffer(RenderType.entityCutoutNoCull(clothesTexture));
			clothesModel.renderToBuffer(poseStack, ivertexbuilder, packedLight, OverlayTexture.NO_OVERLAY);
		}
	}

}