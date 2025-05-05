package com.github.standobyte.jojo.powersystem;

import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.config.DefaultedValue;
import com.github.standobyte.jojo.core.config.JsonConfigurable;
import com.github.standobyte.jojo.util.JSONUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;

public abstract class PowerType implements JsonConfigurable {
	protected final DefaultedValue<MovesetBuilder> moveset;
	protected transient Moveset movesetLazyInit;
	
	public PowerType(MovesetBuilder movesetBuilder) {
		this.moveset = new DefaultedValue<>(movesetBuilder);
	}
	
	public Moveset getMoveset() {
		if (movesetLazyInit == null) {
			movesetLazyInit = moveset.value.build(getPowerClass(), getId());
		}
		return movesetLazyInit;
	}
	
	@ApiStatus.Internal
	public MovesetBuilder getDefaultMoveset() {
		return this.moveset.defaultValue;
	}
	
	public abstract ResourceLocation getId();
	
	public abstract PowerClass<?> getPowerClass();
	
	
	public boolean isEnabled() {
		return true;
	}
	
	
	@Override
	public JsonObject makeConfigTemplate() {
		JsonObject json = new JsonObject();
		Codec<MovesetBuilder> movesetCodec = MovesetBuilder.codec();
		movesetCodec.encodeStart(JsonOps.INSTANCE, moveset.defaultValue).result().ifPresent(movesetJson -> json.add("moveset", movesetJson));
		return json;
	}
	
	@Override
	public void applyConfig(JsonElement json) {
		JsonObject config = json.getAsJsonObject();
		Codec<MovesetBuilder> movesetCodec = MovesetBuilder.codec();
		
		Optional.ofNullable(config.getAsJsonObject("moveset")).ifPresent(movesetEditsJson -> {
			DataResult<Pair<MovesetBuilder, JsonElement>> newMoveset = movesetCodec.encodeStart(JsonOps.INSTANCE, this.moveset.defaultValue)
					.map(JsonElement::getAsJsonObject)
					.flatMap(movesetJson -> {
						JSONUtil.merge(movesetJson, config);
						return movesetCodec.decode(JsonOps.INSTANCE, movesetJson);
					});
			
			this.moveset.value = newMoveset.result().get().getFirst();
			movesetLazyInit = null;
		});
	}
	
	@Override
	public void restoreDefaults() {
		if (moveset.value != moveset.defaultValue) {
			moveset.reset();
			movesetLazyInit = null;
		}
	}
	
}
