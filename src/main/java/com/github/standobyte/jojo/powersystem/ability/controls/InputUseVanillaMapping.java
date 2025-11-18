package com.github.standobyte.jojo.powersystem.ability.controls;

import net.minecraft.client.KeyMapping;

public class InputUseVanillaMapping implements InputBindTemplate {
	public String keyMappingName;

	public InputUseVanillaMapping(KeyMapping keyBind) {
		this(keyBind.getName());
	}
	
	public InputUseVanillaMapping(String keyName) {
		this.keyMappingName = keyName;
	}
}
