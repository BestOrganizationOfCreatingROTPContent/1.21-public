package com.github.standobyte.jojo.client.utils;

public interface SettingsField<T> {
	T get();
	void set(T value);
}
