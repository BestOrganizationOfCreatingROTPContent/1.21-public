package com.github.standobyte.jojo.client.ui.powerhud;

public class WindupIndicator {
	public float value;
	public float maxValue;
	
	public void copyFrom(WindupIndicator src) {
		this.value = src.value;
		this.maxValue = src.maxValue;
	}
}
