package com.github.standobyte.jojo.client.entityanim.playerbend;

import net.minecraft.client.model.geom.ModelPart;

public interface IPlayerLimbBend {
	void rotpSetBendBone(ModelPart bendBone, boolean invertBend);
	ModelPart rotpGetBendBone();
}
