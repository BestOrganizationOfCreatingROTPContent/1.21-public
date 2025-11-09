package com.github.standobyte.jojo.util.mc;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class AttributeUtil {

	public static double getValueOrDefault(LivingEntity entity, Holder<Attribute> attribute, double defaultValue) {
		if (entity == null) return defaultValue;
		AttributeMap attributes = entity.getAttributes();
		return attributes.hasAttribute(attribute) ? attributes.getValue(attribute) : defaultValue;
	}

	public static double getValueOrDefault(LivingEntity entity, Holder<Attribute> attribute) {
		if (entity == null) return attribute.value().getDefaultValue();
		AttributeMap attributes = entity.getAttributes();
		return attributes.hasAttribute(attribute) ? attributes.getValue(attribute) : attribute.value().getDefaultValue();
	}
	
	public static void setBaseValue(AttributeMap attributes, Holder<Attribute> attribute, double baseValue) {
		var instance = attributes.getInstance(attribute);
		if (instance != null) instance.setBaseValue(baseValue);
	}
	
	static Collection<AttributeModifier> add = new ArrayList<>();
	static Collection<AttributeModifier> multBase = new ArrayList<>();
	static Collection<AttributeModifier> multTotal = new ArrayList<>();
	public static double calculateValue(ItemAttributeModifiers itemModifiers, Holder<Attribute> attribute, EquipmentSlot equipmentSlot, double baseValue) {
		add.clear();
		multBase.clear();
		multTotal.clear();
		
		for (var modifierEntry : itemModifiers.modifiers()) {
			if (modifierEntry.attribute().is(attribute) && modifierEntry.slot().test(equipmentSlot)) {
				AttributeModifier modifier = modifierEntry.modifier();
				switch (modifier.operation()) {
					case ADD_VALUE -> add.add(modifier);
					case ADD_MULTIPLIED_BASE -> multBase.add(modifier);
					case ADD_MULTIPLIED_TOTAL -> multTotal.add(modifier);
				}
			}
		}
		
		for (AttributeModifier modifier : add) {
			baseValue += modifier.amount();
		}

		double value = baseValue;

		for (AttributeModifier modifier : multBase) {
			value += baseValue * modifier.amount();
		}

		for (AttributeModifier modifier : multTotal) {
			value *= 1.0 + modifier.amount();
		}

		return attribute.value().sanitizeValue(value);
	}

}
