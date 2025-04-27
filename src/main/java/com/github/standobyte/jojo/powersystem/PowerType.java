package com.github.standobyte.jojo.powersystem;

import java.util.Optional;

import com.github.standobyte.jojo.core.config.DefaultedValue;
import com.github.standobyte.jojo.core.config.JsonConfigurable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;

public abstract class PowerType implements JsonConfigurable {
	protected final DefaultedValue<Moveset.Builder> moveset;
	protected transient Moveset movesetLazyInit;
	
	public PowerType(Moveset.Builder movesetBuilder) {
		this.moveset = new DefaultedValue<>(movesetBuilder);
	}
	
	public Moveset getMoveset() {
		if (movesetLazyInit == null) {
			movesetLazyInit = moveset.value.build(getPowerClass(), getId());
		}
		return movesetLazyInit;
	}
	
	public Moveset.Builder copyDefaultMoveset() {
		return this.moveset.defaultValue.copy();
	}
	
	public abstract ResourceLocation getId();
	
	public abstract PowerClass<?> getPowerClass();
	
	
	public boolean isEnabled() {
		return true;
	}
	
	
	@Override
	public JsonObject makeConfigTemplate() {
		JsonObject json = new JsonObject();
		Codec<Moveset.Builder> movesetCodec = Moveset.builderCodec();
		movesetCodec.encodeStart(JsonOps.INSTANCE, moveset.defaultValue).result().ifPresent(movesetJson -> json.add("moveset", movesetJson));
		return json;
	}
	
	@Override
	public void applyConfig(JsonElement json) {
		JsonObject config = json.getAsJsonObject();
		Codec<Moveset.Builder> movesetCodec = Moveset.builderCodec();
		Optional.ofNullable(config.getAsJsonObject("moveset")).ifPresent(movesetJson -> {
			movesetCodec.decode(JsonOps.INSTANCE, movesetJson).result().ifPresent(movesetEdits -> {
				Moveset.Builder moveset = this.moveset.defaultValue.copy();
				Optional.ofNullable(movesetJson.getAsJsonArray("remove")).ifPresent(toRemove -> {
					for (JsonElement nameToRemove : toRemove) {
						moveset.removeAbility(nameToRemove.getAsString());
					}
				});
				moveset.merge(movesetEdits.getFirst());
				this.moveset.value = moveset;
				movesetLazyInit = null;
			});
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
