package com.github.standobyte.jojo.client.input.controlscheme;

import com.github.standobyte.jojo.util.java.LazyNullable;
import com.mojang.blaze3d.platform.InputConstants;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class ClientKeyWrapper {
	public final short keyId;
	public final InputDevice device;
	public final InputConstants.Type type;
	private final int keyCode;
	private LazyNullable<InputConstants.Key> key;
	
	protected ClientKeyWrapper(short keyId, InputDevice device, InputConstants.Type type, int keyCode) {
		this.keyId = keyId;
		this.device = device;
		this.type = type;
		this.keyCode = keyCode;
		this.key = LazyNullable.of(() -> {
			return this.device == InputDevice.KEYBOARD_MOUSE ? this.type.getOrCreate(this.keyCode) : null;
		});
	}
	
	
	public enum InputDevice {
		KEYBOARD_MOUSE
	}
	
	protected static short keyId(InputDevice device, InputConstants.Type type, int keyCode) {
		return switch (device) {
			case KEYBOARD_MOUSE -> (short) ((type.ordinal() & 3) | (keyCode << 2)); // 11 bits
		};
	}


	public static ClientKeyWrapper make(InputConstants.Type type, int keyCode) {
		return make(InputDevice.KEYBOARD_MOUSE, type, keyCode);
	}

	public static ClientKeyWrapper make(InputDevice device, InputConstants.Type type, int keyCode) {
		short id = keyId(device, type, keyCode);
		return cache.computeIfAbsent(id, _id -> new ClientKeyWrapper(_id, device, type, keyCode));
	}
	
	public static ClientKeyWrapper fromVanillaKeybind(KeyMapping keyMapping) {
		InputConstants.Key key = keyMapping.getKey();
		return make(key.getType(), key.getValue());
	}
	
	
	public InputConstants.Key getVanillaKey() {
		return key.get();
	}
	
	public Component keyName() {
		return switch (device) {
			case KEYBOARD_MOUSE -> getVanillaKey().getDisplayName();
		};
	}
	
	public short keyId() {
		return keyId;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (other != null && this.getClass() == other.getClass()) {
			return this.keyId() == ((ClientKeyWrapper) other).keyId();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return keyId();
	}
	
	
	@Override
	public String toString() {
		return switch (device) {
			case KEYBOARD_MOUSE -> getVanillaKey().toString();
		};
	}


	protected static Short2ObjectMap<ClientKeyWrapper> cache = new Short2ObjectOpenHashMap<>();
	
}
