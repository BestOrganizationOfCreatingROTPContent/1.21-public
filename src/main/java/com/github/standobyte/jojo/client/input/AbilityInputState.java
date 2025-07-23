package com.github.standobyte.jojo.client.input;

import org.jetbrains.annotations.ApiStatus;

public class AbilityInputState {

	public static AbilityInputState init() {
		return withValue(1 << IS_ACTIVE);
	}

	public void setFlag(int flag, boolean value) {
		this._value = _setBit(this._value, flag, value);
	}

	public boolean flag(int flag) {
		return _getBit(this._value, flag);
	}

	// named flags

	public static final int IS_ACTIVE = 0;
	public static final int VISIBLE_EVEN_INACTIVE = 1;
	public static final int VISIBLE_TRANSLUCENT = 2;

	public static final int ONLY_IN_CONTAINER = 3;
	public static final int HIGH_PRIORITY = 4;


	@ApiStatus.Internal protected static AbilityInputState instance = new AbilityInputState();
	@ApiStatus.Internal protected AbilityInputState() {}

	@ApiStatus.Internal public int _value = 1;

	@ApiStatus.Internal public static AbilityInputState withValue(int value) {
		instance._value = value;
		return instance;
	}

	@ApiStatus.Internal public static int _setBit(int num, int bit, boolean value) {
		if (value)	return num | (1 << bit);
		else 		return num & ~(1 << bit);
	}

	@ApiStatus.Internal public static boolean _getBit(int num, int bit) {
		return (num & (1 << bit)) > 0;
	}
}
