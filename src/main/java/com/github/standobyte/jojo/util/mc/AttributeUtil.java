package com.github.standobyte.jojo.util.mc;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class AttributeUtil {

	public static double getValueOrDefault(LivingEntity entity, Holder<Attribute> attribute, double defaultValue) {
		AttributeMap attributes = entity.getAttributes();
		return attributes.hasAttribute(attribute) ? attributes.getValue(attribute) : defaultValue;
	}

	public static double getValueOrDefault(LivingEntity entity, Holder<Attribute> attribute) {
		AttributeMap attributes = entity.getAttributes();
		return attributes.hasAttribute(attribute) ? attributes.getValue(attribute) : attribute.value().getDefaultValue();
	}
	
}
