package com.github.standobyte.jojo.client.entityrender.parsemodel.loader;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.entities.SimpleEntityRenderer;
import com.github.standobyte.jojo.client.standskin.StandSkin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ResourceModelEntry {
	public final ResourceLocation modelPath;
	@Nullable public LayerDefinition modelDefinition;
	@Nullable public ModelPart modelRoot;
	@Nullable public Model model;
	
	protected Function<LayerDefinition, EntityModel<?>> modelConstructor;
	protected boolean loadFromStandSkin;
	
	public ResourceModelEntry(ResourceLocation modelPath) {
		this.modelPath = modelPath;
	}
	
	public <T extends Entity> void rendererInit(Function<ModelPart, EntityModel<T>> modelClass, boolean loadFromStandSkin) {
		this.modelConstructor = (LayerDefinition modelDefinition) -> modelClass.apply(modelDefinition.bakeRoot());
		this.loadFromStandSkin = loadFromStandSkin;
	}
	
	@Nullable
	public <T extends Entity, M extends EntityModel<T>> EntityModel<T> getModel(T entity) {
		if (loadFromStandSkin) {
			StandSkin standSkin = SimpleEntityRenderer.getStandSkin(entity);
			if (standSkin != null) {
				EntityModel<T> modelFromSkin = (EntityModel<T>) standSkin.getModel(this.modelPath, this.modelConstructor);
				if (modelFromSkin != null) {
					return modelFromSkin;
				}
			}
		}
		
		EntityModel<T> modelFromResource = (EntityModel<T>) this.getModel(this.modelConstructor);
		return modelFromResource;
	}
	
	@Nullable
	public <M extends Model> M getModel(Function<LayerDefinition, M> modelConstructor) {
		if (model == null && modelDefinition != null) {
			this.model = modelConstructor.apply(modelDefinition);
		}
		return (M) model;
	}
	
	
	
	@ApiStatus.Internal
	public void reset() {
		this.modelDefinition = null;
		this.modelRoot = null;
		this.model = null;
	}
	
	@ApiStatus.Internal
	public void onModelLoad(@Nonnull LayerDefinition newModelLoaded) {
		this.modelDefinition = newModelLoaded;
		this.modelRoot = modelDefinition.bakeRoot();
	}
	
}
