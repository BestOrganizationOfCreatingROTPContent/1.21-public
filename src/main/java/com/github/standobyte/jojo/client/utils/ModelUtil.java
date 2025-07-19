package com.github.standobyte.jojo.client.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nullable;

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
	
	@Nullable
	public static Map<String, ModelPart[]> modelPartInheritanceChains(String rootName, ModelPart root, String... endPartNames) {
		Stack<ModelPart> stack = new Stack<>();
		Set<String> toFind = new HashSet<>(Arrays.asList(endPartNames));
		Map<String, ModelPart[]> destination = new HashMap<>();
		recursionMyBeloved(rootName, root, stack, toFind, destination);
		return destination;
	}
	
	private static void recursionMyBeloved(String partName, ModelPart part, Stack<ModelPart> stack, Collection<String> toFind, Map<String, ModelPart[]> destination) {
		stack.add(part);
		if (toFind.remove(partName)) {
			destination.put(partName, stack.toArray(ModelPart[]::new));
		}
		if (!toFind.isEmpty()) {
			for (var child : part.children.entrySet()) {
				recursionMyBeloved(child.getKey(), child.getValue(), stack, toFind, destination);
			}
		}
		stack.pop();
	}
	
}