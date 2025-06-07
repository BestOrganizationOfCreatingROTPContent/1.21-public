package com.github.standobyte.jojo.init.core;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModEntityDataSerializers {
	public static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, JojoMod.MOD_ID);
	

//	public static final Supplier<EntityDataSerializer<ActionTarget>> ACTION_TARGET = SERIALIZERS.register("action_target", 
//			() -> new EntityDataSerializer<ActionTarget>() {
//				@Override public StreamCodec<? super RegistryFriendlyByteBuf, ActionTarget> codec() { return ActionTarget.STREAM_CODEC_UNRESOLVED_ENTITY_ID; }
//				@Override public ActionTarget copy(ActionTarget value) { return value.copy(); }
//			});

}
