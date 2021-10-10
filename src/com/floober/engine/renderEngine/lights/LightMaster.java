package com.floober.engine.renderEngine.lights;

import java.util.ArrayList;
import java.util.List;

public class LightMaster {

	public static final int MAX_LIGHTS = 8;

	private static final List<Light> sceneLights = new ArrayList<>(MAX_LIGHTS);
	private static float ambientLight = 1.0f;

	// GETTERS
	public static float getAmbientLight() {
		return ambientLight;
	}
	public static List<Light> getSceneLights() {
		return sceneLights;
	}


	// SETTERS
	public static void setAmbientLight(float ambient) {
		ambientLight = ambient;
	}

	// ADDING/REMOVING LIGHT SOURCES
	public static void addLight(Light light) {
		if (sceneLights.size() < MAX_LIGHTS)
			sceneLights.add(light);
	}

	public void removeLight(Light light) {
		sceneLights.remove(light);
	}

	public void clearLights() {
		sceneLights.clear();
	}

}