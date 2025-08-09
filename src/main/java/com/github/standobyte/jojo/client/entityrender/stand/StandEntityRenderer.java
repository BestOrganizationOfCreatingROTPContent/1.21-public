package com.github.standobyte.jojo.client.entityrender.stand;

import java.util.Optional;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.RotpGeckoModelLoader;
import com.github.standobyte.jojo.client.entityanim.AnimWithExtras;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.shader.EntityShaders;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.java.LazyNullable;
import com.github.standobyte.v1_21_4_stuff.renderstate.ArmedEntityRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.RenderStateCrutches;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class StandEntityRenderer<
				T extends StandEntity, 
				S extends StandEntityRenderState, 
				M extends StandEntityModel<T, S>> 
		extends LivingEntityRenderer<T, M> {
	@Deprecated(forRemoval = true)
	public final S reusedState = this.createRenderState();
	public final S outOfLevelRenderState = createRenderState();
	protected LazyNullable<M> missingSkinModel;

	public StandEntityRenderer(Context context) {
		this(context, 0);
	}

	public StandEntityRenderer(Context context, float shadowRadius) {
		super(context, null, shadowRadius);
		this.missingSkinModel = LazyNullable.of(() -> createStandModel(
				RotpGeckoModelLoader.getInstance().getModelDefinition(JojoMod.resLoc("stand_default"))));
		this.addLayer(new StandItemInHandLayer<>(this, context.getItemInHandRenderer()));
	}
	
	public final S createRenderState(T entity, float partialTick) {
		S s = this.reusedState;
		this.extractRenderState(entity, s, partialTick);
		return s;
	}

	/**
	 * If you extend StandEntityRenderer and put a sub-class of StandEntityRenderState as S, 
	 * don't forget to override this method too to actually create the new render state object.
	 */
	@SuppressWarnings("unchecked")
//	@Override // 1.21.2+
	public S createRenderState() {
		return (S) new StandEntityRenderState();
	}
	
	public M createStandModel(LayerDefinition definition) {
		return (M) new StandEntityModel<>(definition.bakeRoot());
	}
	
	public Model createModel(ResourceLocation modelPath, LayerDefinition definition) {
		return null;
//		return new Model(definition.bakeRoot(), RenderType::entityTranslucent);
	}
	
	public static final ActionAnimIdentifier IDLE_ANIM = ActionAnimIdentifier.getOrCreate("idle");
//	@Override // 1.21.2+
	public void extractRenderState(T entity, S renderState, float partialTick) {
//		super.extractRenderState(entity, renderState, partialTick); // 1.21.2+
		LivingEntityRenderState.extract(entity, renderState, this, entityRenderDispatcher, partialTick);
		ArmedEntityRenderState.extractArmedEntityRenderState(entity, renderState/*, this.itemModelResolver*/);
		renderState.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		renderState.rightArmPose = HumanoidModel.ArmPose.EMPTY;
		
		renderState.standId = entity.getStandId();
		Optional<ResourceLocation> selectedSkin = entity.getStandSkin();
		StandSkinsLoader standSkins = StandSkinsLoader.getInstance();
		renderState.defaultSkin = standSkins.getDefaultSkin(renderState.standId);
		renderState.skin = standSkins.getSkinFromId(renderState.standId, selectedSkin);
		if (renderState.skin == null) renderState.skin = renderState.defaultSkin;

		renderState.visibleParts = HumanoidPart.ALL;
		
		EntityActionInstance action = entity.getCurStandAction();
		EntityActionRenderState.extract(renderState.action, 
				entity, action, partialTick);
		if (renderState.action.animId == null) {
			renderState.action.animId = IDLE_ANIM;
			renderState.action.time = entity.tickCount - entity.nonIdlePoseTimeStamp + partialTick;
		}
		if (renderState.action.animId != IDLE_ANIM) {
			entity.nonIdlePoseTimeStamp = entity.tickCount;
		}
		EntityActionRenderState.setAnim(renderState.action, renderState, 
				getStandAnim(renderState), entity.clientStuff.barrageSwings);
		
		renderState.tint = -1;
		
		Minecraft mc = Minecraft.getInstance();
		renderState.mayObstructView = mc.options.getCameraType().isFirstPerson() && mc.player != null && entity.getUser() == mc.player;
	}
	
	public void extractSkinMenuRenderState(S renderState, StandSkin skin, ResourceLocation standId, float ticks) {
		renderState.defaultSkin = StandSkinsLoader.getInstance().getDefaultSkin(standId);
		renderState.skin = skin;
		renderState.visibleParts = HumanoidPart.ALL;
		renderState.standId = standId;
		renderState.action.animId = StandEntityRenderer.IDLE_ANIM;
		renderState.action.time = ticks;
		EntityActionRenderState.setAnim(renderState.action, renderState, this.getStandAnim(renderState), null);
		renderState.tint = -1;
	}
	
	public AnimWithExtras getStandAnim(S renderState) {
		if (renderState.skin != null) {
			EntityActionRenderState action = renderState.action;
			if (action.animId != null) {
				AnimWithExtras anim = renderState.skin.getStandAnimation(anims -> anims.getNamedAnim(action.animId), renderState.defaultSkin);
				return anim;
			}
		}
		return null;
	}
	
	
	protected static final ResourceLocation MISSING_TEXTURE = JojoMod.resLoc("textures/entity/stand_default.png");
//	@Override // 1.21.1+
	public ResourceLocation getTextureLocation(S renderState) {
		StandSkin standSkin = renderState.skin;
		ResourceLocation texture = standSkin != null ? standSkin.getStandTexture(renderState.defaultSkin, MISSING_TEXTURE) : null;
		return texture != null ? texture : MISSING_TEXTURE;
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity) {
		if (RenderStateCrutches.currentEntityRenderState != null) {
			return getTextureLocation((S) RenderStateCrutches.currentEntityRenderState);
		}
		return MISSING_TEXTURE;
	}

//	@Override
//    protected int getModelTint(S renderState) {
//		return renderState.tint;
//    }

	protected void setModelFrom(S renderState) {
		StandSkin standSkin = renderState.skin;
		this.model = standSkin != null ? (M) standSkin.getStandModel(this, renderState.defaultSkin) : null;
		if (this.model == null) {
			this.model = missingSkinModel.get();
			if (standSkin != null) {
				renderState.tint = standSkin.getColor(renderState.defaultSkin);
			}
		}
	}
	
	
	// 1.21.2+
//	public void renderWithRenderState(Consumer<S> renderState, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
//		renderState.accept(outOfLevelRenderState);
//		render(outOfLevelRenderState, poseStack, bufferSource, light);
//	}
	
	
//	@Override // 1.21.2+
//	public void render(S renderState, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
//		setModelFrom(renderState);
//		if (this.model != null) {
//			super.render(renderState, poseStack, bufferSource, light);
//		}
//	}

	@Override
	public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
		S s = this.createRenderState(entity, partialTicks);
		render(entity, s, entityYaw, partialTicks, poseStack, bufferSource, light);
	}

	public void render(T entity, S renderState, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
		RenderStateCrutches.currentEntityRenderState = renderState;

		setModelFrom(renderState);
		
		StandItemInHandLayer.itemBufferSource = bufferSource;
		if (renderState.mayObstructView) {
			bufferSource = EntityShaders.firstPersonStandTranslucency.useBufferSourceThisFrame();
		}
		
		if (this.model != null) {
			super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, light);
		}
		RenderStateCrutches.currentEntityRenderState = null;

	}

	@Override
	protected boolean shouldShowName(T entity/*, double distSqr*/) {
		return false;
	}
	
	
	/* If i try to render ItemInHandLayer using the FirstPersonStandTranslucentShader.standTranslucencyBufferSource
	 * (in order to render the Stand entity on a separate buffer to apply a shader to the entire buffer),
	 * instead it makes it so that the Stand model does not render at all, only the held items do.
	 * Couldn't fix it, so to not waste too much time, I'll just leave this crutch here - the items just won't be translucent, so in terms of gameplay it's fine.
	 */
	protected static class StandItemInHandLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends ItemInHandLayer<T, M> {
		protected static MultiBufferSource itemBufferSource;
		
	    public StandItemInHandLayer(RenderLayerParent<T, M> renderer, ItemInHandRenderer itemInHandRenderer) {
	        super(renderer, itemInHandRenderer);
	    }

	    @Override
	    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T livingEntity, 
	    		float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
	    	buffer = itemBufferSource;
	    	super.render(poseStack, buffer, light, livingEntity, 
	    			limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
	    }
	}

}
