package com.github.standobyte.jojo.powersystem.entityaction.type;

import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * An EntityActionType, that plays an action over multiple ticks and can have its entity animation,
 * but is not registered as an ability in a power's moveset, instead using its own registry.
 * Can be used for stuff like special weapon attacks or item abilities.
 */
public abstract class SpecialEntityActionType implements EntityActionType {
	public final ResourceLocation id;
	protected ResourceLocation animSet;
	protected ActionAnimIdentifier anim;
	
	public SpecialEntityActionType(String animFileName, ResourceLocation id) {
		this.id = id;
		this.animSet = id.withPath(animFileName);
		this.anim = ActionAnimIdentifier.getOrCreate(id.getPath(), false);
	}


	@Override
	public void encodeAbility(LivingEntity user, FriendlyByteBuf buffer) {
		buffer.writeBoolean(false);
		buffer.writeResourceLocation(id);
	}

	@Override
	public ActionAnimIdentifier getEntityAnim(EntityActionInstance action) {
		return anim;
	}
	
	@Override
	public ResourceLocation getEntityAnimSet(LivingEntity user) {
		return animSet;
	}


	@Override
	public EntityActionInstance createActionObj() {
		return new EntityActionInstance(this);
	}

}
