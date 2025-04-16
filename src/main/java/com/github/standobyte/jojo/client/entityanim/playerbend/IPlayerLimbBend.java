package com.github.standobyte.jojo.client.entityanim.playerbend;

import net.minecraft.client.model.geom.ModelPart;

public interface IPlayerLimbBend {
	void jojoROASetBendBone(ModelPart bendBone, boolean invertBend);
	ModelPart jojoROAGetBendBone();
}
