package com.github.standobyte.jojo.util.java;

import net.minecraft.util.Mth;

public class LerpValue {

	public static class Float {
		public float prevValue;
		public float value;
		boolean firstUpdate;

		public void lerpTick() {
			this.prevValue = this.value;
		}

		public float lerp(float partialTick) {
			return partialTick == 1 ? value : Mth.lerp(partialTick, prevValue, value);
		}
		
		public boolean set(float value, boolean lerp) {
			if (this.value != value) {
				if (lerp) {
					this.prevValue = this.value;
				}
				this.value = value;
				if (this.firstUpdate) {
					this.firstUpdate = false;
					this.prevValue = this.value;
				}
				return true;
			}
			return false;
		}

		public float get() {
			return value;
		}
	}

	public static class Double {
		public double prevValue;
		public double value;
		boolean firstUpdate;

		public void preTick() {
			this.prevValue = this.value;
		}

		public double lerp(float partialTick) {
			return partialTick == 1 ? value : Mth.lerp(partialTick, prevValue, value);
		}
		
		public boolean set(double value, boolean lerp) {
			if (this.value != value) {
				if (lerp) {
					this.prevValue = this.value;
				}
				this.value = value;
				if (this.firstUpdate) {
					this.firstUpdate = false;
					this.prevValue = this.value;
				}
				return true;
			}
			return false;
		}

		public double get() {
			return value;
		}
	}

}
