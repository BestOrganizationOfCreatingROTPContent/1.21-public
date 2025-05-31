package com.github.standobyte.jojo.mixin.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

// i really hope this doesn't break any other mods, but why the hell do i even have to do this just to make my model animations not tank the FPS??
@Mixin(Model.class)
public class ModelAnimOptimization {
	@Shadow @Final ModelPart root;
	private Map<String, Optional<ModelPart>> jojo_ripples$allModelParts = new HashMap<>();

	@Inject(method = "<init>("
			+ "Lnet/minecraft/client/model/geom/ModelPart;"
			+ "Ljava/util/function/Function;)V", at = @At("RETURN"))
	public void jojo_ripples$initModelPartsCache(ModelPart root, Function<ResourceLocation, RenderType> renderType, CallbackInfo ci) {
		jojo_ripples$recursionTime(root, "root", jojo_ripples$allModelParts);
	}

	private static void jojo_ripples$recursionTime(ModelPart modelPart, String modelPartName, Map<String, Optional<ModelPart>> allModelParts) {
		allModelParts.put(modelPartName, Optional.of(modelPart));
		for (var childEntry : modelPart.children.entrySet()) {
			jojo_ripples$recursionTime(childEntry.getValue(), childEntry.getKey(), allModelParts);
		}
	}

	@Overwrite
	public Optional<ModelPart> getAnyDescendantWithName(String name) {
		Optional<ModelPart> part = jojo_ripples$allModelParts.get(name);
		return part != null ? part : Optional.empty();
	}

}
