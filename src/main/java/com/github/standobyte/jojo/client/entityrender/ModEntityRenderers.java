package com.github.standobyte.jojo.client.entityrender;

import com.github.standobyte.jojo.client.entityrender.entities.MannequinModel;
import com.github.standobyte.jojo.client.entityrender.entities.MannequinRenderer;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
	
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntityTypes.HUMANOID_STAND.get(), StandEntityRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.MANNEQUIN.get(), MannequinRenderer::new);
	}
	
	
	public static final ModelLayerLocation MANNEQUIN = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "mannequin"), 
			"main");
	
	public static final ModelLayerLocation MANNEQUIN_SLIM = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "mannequin_slim"), 
			"main");
	
	public static final ModelLayerLocation MANNEQUIN_SMALL = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "mannequin_small"), 
			"main");
	
	public static final ModelLayerLocation MANNEQUIN_SLIM_SMALL = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "mannequin_slim_small"), 
			"main");
	
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		LayerDefinition mannequin = MannequinModel.createMesh(CubeDeformation.NONE, false);
		LayerDefinition mannequinSlim = MannequinModel.createMesh(CubeDeformation.NONE, true);
		event.registerLayerDefinition(ModEntityRenderers.MANNEQUIN, () -> mannequin);
		event.registerLayerDefinition(ModEntityRenderers.MANNEQUIN_SLIM, () -> mannequinSlim);
		event.registerLayerDefinition(ModEntityRenderers.MANNEQUIN_SMALL, () -> mannequin.apply(HumanoidModel.BABY_TRANSFORMER));
		event.registerLayerDefinition(ModEntityRenderers.MANNEQUIN_SLIM_SMALL, () -> mannequinSlim.apply(HumanoidModel.BABY_TRANSFORMER));
	}
	
	
	@SubscribeEvent
	public static void addLayers(EntityRenderersEvent.AddLayers event) {
		
	}
}