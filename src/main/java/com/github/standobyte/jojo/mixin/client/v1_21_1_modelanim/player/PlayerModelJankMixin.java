package com.github.standobyte.jojo.mixin.client.v1_21_1_modelanim.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.v1_21_4_stuff.OldPlayerModelJank;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

@Mixin(PlayerModel.class)
public abstract class PlayerModelJankMixin extends HumanoidModelMixin {
	
	@Inject(method = "<init>("
			+ "Lnet/minecraft/client/model/geom/ModelPart;"
			+ "Z)V", at = @At("RETURN"))
	protected void jojo_ripples$_onInitModel(ModelPart root, boolean slim, CallbackInfo ci) {
		OldPlayerModelJank._setOuterLayerBends((EntityModel<?>) this, this);
	}

}
