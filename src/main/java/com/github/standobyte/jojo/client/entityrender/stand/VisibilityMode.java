package com.github.standobyte.jojo.client.entityrender.stand;

public enum VisibilityMode {
	ALL,
	ARMS_ONLY,
	LEFT_ARM_ONLY,
	RIGHT_ARM_ONLY,
	BODY_WITHOUT_ARMS(ARMS_ONLY),
	BODY_WITH_LEFT_ARM(RIGHT_ARM_ONLY),
	BODY_WITH_RIGHT_ARM(LEFT_ARM_ONLY),
	NONE(ALL);

	public final VisibilityMode baseMode;
	private VisibilityMode inverse;
	public final boolean isInverted;

	private VisibilityMode() {
		this.baseMode = this;
		this.isInverted = false;
	}

	private VisibilityMode(VisibilityMode inverting) {
		this.baseMode = inverting;
		this.isInverted = true;
		this.inverse = inverting;
		inverting.inverse = this;
	}

	public VisibilityMode invert() {
		return inverse;
	}

	public VisibilityMode invert(boolean doInvert) {
		return doInvert ? invert() : this;
	}

	public VisibilityMode reduceTo(VisibilityMode targetBaseMode) {
		if (targetBaseMode.isInverted && targetBaseMode != VisibilityMode.NONE) {
			throw new IllegalArgumentException();
		}
		if (targetBaseMode == VisibilityMode.ALL || targetBaseMode == this.baseMode) {
			return this;
		}

		switch (this.baseMode) {
		case NONE:
			return this;
		case ALL:
			break;
		case LEFT_ARM_ONLY:
			if (targetBaseMode == RIGHT_ARM_ONLY) {
				targetBaseMode = VisibilityMode.NONE;
			}
			break;
		case RIGHT_ARM_ONLY:
			if (targetBaseMode == LEFT_ARM_ONLY) {
				targetBaseMode = VisibilityMode.NONE;
			}
			break;
		default:
			throw new IllegalStateException();
		}

		if (this.isInverted) {
			targetBaseMode = targetBaseMode.inverse;
		}
		return targetBaseMode;
	}
}
