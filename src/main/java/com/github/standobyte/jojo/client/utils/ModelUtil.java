package com.github.standobyte.jojo.client.utils;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.model.geom.ModelPart;

public class ModelUtil {

	public static Map<String, ModelPart> getAllNamedModelParts(ModelPart root) {
		Map<String, ModelPart> map = new HashMap<>();
		map.put("root", root);
		putChildrenRecursive(root, map);
		return map;
	}
	
	private static void putChildrenRecursive(ModelPart parent, Map<String, ModelPart> dest) {
		for (var childEntry : parent.children.entrySet()) {
			ModelPart modelPart = childEntry.getValue();
			dest.putIfAbsent(childEntry.getKey(), modelPart);
			putChildrenRecursive(modelPart, dest);
		}
	}
	
}