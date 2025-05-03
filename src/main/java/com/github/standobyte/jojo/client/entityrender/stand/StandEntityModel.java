package com.github.standobyte.jojo.client.entityrender.stand;

import com.github.standobyte.jojo.client.entityanim.AnimWithExtras;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class StandEntityModel<T extends StandEntityRenderState> extends EntityModel<T> {

	public StandEntityModel(ModelPart root) {
		super(root);
	}

	@Override
	public void setupAnim(T renderState) {
		super.setupAnim(renderState);
		EntityActionRenderState action = renderState.action;
		if (action.anim != null) {
			if (renderState.skin != null) {
				AnimWithExtras anim = renderState.skin.getStandAnimation(anims -> anims.getNamedAnim(action.anim), renderState.defaultSkin);
				if (anim != null) {
					anim.animate(this, renderState, renderState.action, 1);
				}
			}
		}
	}

}
