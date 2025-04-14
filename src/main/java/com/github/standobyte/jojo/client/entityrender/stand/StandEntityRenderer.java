package com.github.standobyte.jojo.client.entityrender.stand;

import java.util.Optional;
import java.util.function.Consumer;

import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class StandEntityRenderer<
				T extends StandEntity, 
				S extends StandEntityRenderState, 
				M extends EntityModel<? super S>> 
		extends LivingEntityRenderer<T, S, M> {
	protected final S outOfLevelRenderState = createRenderState();

	public StandEntityRenderer(Context context) {
		this(context, null);
	}

	public StandEntityRenderer(Context context, M missingSkinModel) {
		super(context, missingSkinModel, 0);
		this.missingSkinModel = missingSkinModel;
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
		
		EntityActionInstance action = entity.getStandAction();
		EntityActionRenderState.extract(renderState.action, action, partialTick);
		if (renderState.action.anim == null) {
			renderState.action.anim = IDLE_ANIM;
			renderState.action.phaseTime = entity.tickCount - entity.nonIdlePoseTimeStamp + partialTick;
		}
		if (renderState.action.anim != IDLE_ANIM) {
			entity.nonIdlePoseTimeStamp = entity.tickCount;
		}
	}
	
	
	protected static final ResourceLocation MISSING_TEXTURE = JojoMod.resLoc("missing_stand_skin");
	@Override
	public ResourceLocation getTextureLocation(S renderState) {
		StandSkin standSkin = renderState.skin;
		return standSkin != null ? standSkin.getStandTexture(renderState.defaultSkin) : MISSING_TEXTURE;
	}
	
	protected final M missingSkinModel;
	protected void setModelFrom(S renderState) {
		StandSkin standSkin = renderState.skin;
		this.model = standSkin != null ? (M) standSkin.getStandModel(renderState.defaultSkin) : null;
		if (this.model == null) this.model = missingSkinModel;
	}
	
	
	public void renderWithRenderState(Consumer<S> renderState, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
		renderState.accept(outOfLevelRenderState);
		render(outOfLevelRenderState, poseStack, bufferSource, light);
	}
	
	
	@Override
	public void render(S renderState, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
		setModelFrom(renderState);
		if (this.model == null) return;
		super.render(renderState, poseStack, bufferSource, light);
	}

}
