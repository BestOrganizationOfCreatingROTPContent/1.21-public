package com.github.standobyte.jojo.client.entityanim.molang;

import team.unnamed.mocha.MochaEngine;

// TODO (!!) Mocha license/copyright
public class KeyframesMolangEngine {
	private static MochaEngine<?> mochaInstance;
	
	public static void init() {
		if (mochaInstance == null) {
			mochaInstance = MochaEngineWithoutJavassist.createStandard();
			mochaInstance.scope().set(AnimMolangQuery.NAMESPACE, AnimMolangQuery.instance);
		}
	}
	
	public static MochaEngine<?> get() {
		return mochaInstance;
	}
}
