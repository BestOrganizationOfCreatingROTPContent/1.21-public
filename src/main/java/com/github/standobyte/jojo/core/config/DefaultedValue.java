package com.github.standobyte.jojo.core.config;

public class DefaultedValue<V> {
	public final V defaultValue;
	public V value;
	
	public DefaultedValue(V defaultValue) {
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}
	
	public void reset() {
		this.value = this.defaultValue;
	}
	
	
	public static class Int {
		public final int defaultValue;
		public int value;
		
		public Int(int defaultValue) {
			this.defaultValue = defaultValue;
			this.value = defaultValue;
		}
		
		public void reset() {
			this.value = this.defaultValue;
		}
	}
	
	
	public static class Bool {
		public final boolean defaultValue;
		public boolean value;
		
		public Bool(boolean defaultValue) {
			this.defaultValue = defaultValue;
			this.value = defaultValue;
		}
		
		public void reset() {
			this.value = this.defaultValue;
		}
	}
	
	
	public static class Float {
		public final float defaultValue;
		public float value;
		
		public Float(float defaultValue) {
			this.defaultValue = defaultValue;
			this.value = defaultValue;
		}
		
		public void reset() {
			this.value = this.defaultValue;
		}
	}
	
	
	public static class Double {
		public final double defaultValue;
		public double value;
		
		public Double(double defaultValue) {
			this.defaultValue = defaultValue;
			this.value = defaultValue;
		}
		
		public void reset() {
			this.value = this.defaultValue;
		}
	}
	
}
