package com.github.standobyte.jojo.mixin.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

@Mixin(Model.class)
public abstract class ModelAnimOptimization implements Model_1_21_2plus {
	private Map<String, Optional<ModelPart>> jojo_ripples$allModelParts;

//	@Inject(method = "<init>("
//			+ "Lnet/minecraft/client/model/geom/ModelPart;"
//			+ "Ljava/util/function/Function;)V", at = @At("RETURN"))
//	public void jojo_ripples$onInit(ModelPart root, Function<ResourceLocation, RenderType> renderType, CallbackInfo ci) {
//		jojo_ripples$initModelPartsCache(root);
//	}
	
	public void jojo_ripples$initModelPartsCache(ModelPart root) {
		jojo_ripples$allModelParts = new HashMap<>();
		jojo_ripples$recursionTime(root, "root", jojo_ripples$allModelParts);
	}

	private static void jojo_ripples$recursionTime(ModelPart modelPart, String modelPartName, Map<String, Optional<ModelPart>> allModelParts) {
		allModelParts.put(modelPartName, Optional.of(modelPart));
		for (var childEntry : modelPart.children.entrySet()) {
			jojo_ripples$recursionTime(childEntry.getValue(), childEntry.getKey(), allModelParts);
		}
	}
	
//	@Inject(method = "getAnyDescendantWithName", at = @At("HEAD"), cancellable = true)
	@Override
	public /*void*/Optional<ModelPart> jojo_ripples$getAnyDescendantWithName(String name/*, CallbackInfoReturnable<Optional<ModelPart>> ci*/) {
		if (jojo_ripples$allModelParts != null) {
			Optional<ModelPart> part = jojo_ripples$allModelParts.get(name);
			return part != null ? part : Optional.empty();
		}
		return Optional.empty();
	}

}
