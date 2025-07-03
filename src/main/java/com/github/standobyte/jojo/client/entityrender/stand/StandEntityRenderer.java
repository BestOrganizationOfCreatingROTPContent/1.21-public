package com.github.standobyte.jojo.client.entityrender.stand;

import java.util.Optional;
import java.util.function.Consumer;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.RotpGeckoModelLoader;
import com.github.standobyte.jojo.client.entityanim.AnimWithExtras;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.java.LazyNullable;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class StandEntityRenderer<
				T extends StandEntity, 
				S extends StandEntityRenderState, 
				M extends StandEntityModel<? super S>> 
		extends LivingEntityRenderer<T, S, M> {
	protected final S outOfLevelRenderState = createRenderState();
	protected LazyNullable<M> missingSkinModel;

	public StandEntityRenderer(Context context) {
		this(context, 0);
	}

	public StandEntityRenderer(Context context, float shadowRadius) {
		super(context, null, shadowRadius);
		this.missingSkinModel = LazyNullable.of(() -> createStandModel(
				RotpGeckoModelLoader.getInstance().getModelDefinition(JojoMod.resLoc("stand_default"))));
	}

	/**
	 * If you extend StandEntityRenderer and put a sub-class of StandEntityRenderState as S, 
	 * don't forget to override this method too to actually create the new render state object.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public S createRenderState() {
		return (S) new StandEntityRenderState();
	}
	
	public M createStandModel(LayerDefinition definition) {
		return (M) new StandEntityModel<>(definition.bakeRoot());
	}
	
	public Model createModel(ResourceLocation modelPath, LayerDefinition definition) {
		return new Model(definition.bakeRoot(), RenderType::entityTranslucent) {} /* Mojang making base Model class abstract for no reason, very cool */;
	}
	
	public static final ActionAnimIdentifier IDLE_ANIM = ActionAnimIdentifier.getOrCreate("idle");
	@Override
	public void extractRenderState(T entity, S renderState, float partialTick) {
		super.extractRenderState(entity, renderState, partialTick);
		ArmedEntityRenderState.extractArmedEntityRenderState(entity, renderState, this.itemModelResolver);
		renderState.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		renderState.rightArmPose = HumanoidModel.ArmPose.EMPTY;
		
		renderState.standId = entity.getStandId();
		Optional<ResourceLocation> selectedSkin = entity.getStandSkin();
		StandSkinsLoader standSkins = StandSkinsLoader.getInstance();
		renderState.defaultSkin = standSkins.getDefaultSkin(renderState.standId);
		renderState.skin = standSkins.getSkinFromId(renderState.standId, selectedSkin);
		if (renderState.skin == null) renderState.skin = renderState.defaultSkin;
		
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
		
		if (entity.onlyVisibleToStandUsers()) {
			renderState.isInvisible |= !ClientGlobals.canSeeStands;
			renderState.isInvisibleToPlayer |= !ClientGlobals.canSeeStands;
		}
		
		renderState.tint = -1;
	}
	
	public void extractSkinMenuRenderState(S renderState, StandSkin skin, ResourceLocation standId, float ticks) {
		renderState.defaultSkin = StandSkinsLoader.getInstance().getDefaultSkin(standId);
		renderState.skin = skin;
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
	@Override
	public ResourceLocation getTextureLocation(S renderState) {
		StandSkin standSkin = renderState.skin;
		ResourceLocation texture = standSkin != null ? standSkin.getStandTexture(renderState.defaultSkin, MISSING_TEXTURE) : null;
		return texture != null ? texture : MISSING_TEXTURE;
	}

	@Override
    protected int getModelTint(S renderState) {
		return renderState.tint;
    }

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
	
	
	public void renderWithRenderState(Consumer<S> renderState, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
		renderState.accept(outOfLevelRenderState);
		render(outOfLevelRenderState, poseStack, bufferSource, light);
	}
	
	
	@Override
	public void render(S renderState, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
		setModelFrom(renderState);
		if (this.model == null) return;
		this.model.setAllVisible(true);
		super.render(renderState, poseStack, bufferSource, light);
	}

	@Override
	protected boolean shouldShowName(T entity, double distSqr) {
		return false;
	}

}
