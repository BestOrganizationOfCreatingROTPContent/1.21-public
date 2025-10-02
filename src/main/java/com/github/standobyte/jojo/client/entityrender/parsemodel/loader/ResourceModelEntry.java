package com.github.standobyte.jojo.client.entityrender.parsemodel.loader;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

public class ResourceModelEntry {
	public final ResourceLocation modelPath;
	@Nullable public LayerDefinition modelDefinition;
	@Nullable public ModelPart modelRoot;
	@Nullable public Model model;
	
	public ResourceModelEntry(ResourceLocation modelPath) {
		this.modelPath = modelPath;
	}
	
	public void reset() {
		this.modelDefinition = null;
		this.modelRoot = null;
		this.model = null;
	}
	
	public void onModelLoad(@Nonnull LayerDefinition newModelLoaded) {
		this.modelDefinition = newModelLoaded;
		this.modelRoot = modelDefinition.bakeRoot();
	}
	
	@Nullable
	public <M extends Model> M getModel(Function<LayerDefinition, M> modelConstructor) {
		if (model == null && modelDefinition != null) {
			this.model = modelConstructor.apply(modelDefinition);
		}
		return (M) model;
	}
	
}
