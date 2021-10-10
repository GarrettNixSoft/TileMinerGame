package com.floober.engine.renderEngine.shaders.textures;

import com.floober.engine.renderEngine.lights.Light;
import com.floober.engine.renderEngine.lights.LightMaster;
import com.floober.engine.renderEngine.shaders.ShaderProgram;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.List;

public class TileShader extends ShaderProgram {

	private static final String VERTEX_FILE = "/com/floober/engine/renderEngine/shaders/shadercode/tileVertex.glsl";
	private static final String FRAGMENT_FILE = "/com/floober/engine/renderEngine/shaders/shadercode/tileFragment.glsl";

	private int location_numRows;

	// lights!
	private int location_screenRatio;
	private int location_ambientLight;
	private int[] location_lightPositions;
	private int[] location_lightColors;
	private int[] location_lightIntensities;
	private int[] location_lightInnerRadii;
	private int[] location_lightOuterRadii;
	private int[] location_lightMaxRadii;

	public TileShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_numRows = super.getUniformLocation("numRows");
		location_screenRatio = super.getUniformLocation("screenRatio");
		location_ambientLight = super.getUniformLocation("ambientLight");
		// light properties
		location_lightPositions = new int[LightMaster.MAX_LIGHTS];
		location_lightColors = new int[LightMaster.MAX_LIGHTS];
		location_lightIntensities = new int[LightMaster.MAX_LIGHTS];
		location_lightInnerRadii = new int[LightMaster.MAX_LIGHTS];
		location_lightOuterRadii = new int[LightMaster.MAX_LIGHTS];
		location_lightMaxRadii = new int[LightMaster.MAX_LIGHTS];
		for (int i = 0; i < LightMaster.MAX_LIGHTS; ++i) {
			location_lightPositions[i] = super.getUniformLocation("lightPositions[" + i + "]");
			location_lightColors[i] = super.getUniformLocation("lightColors[" + i + "]");
			location_lightIntensities[i] = super.getUniformLocation("lightIntensities[" + i + "]");
			location_lightInnerRadii[i] = super.getUniformLocation("lightInnerRadii[" + i + "]");
			location_lightOuterRadii[i] = super.getUniformLocation("lightOuterRadii[" + i + "]");
			location_lightMaxRadii[i] = super.getUniformLocation("lightMaxRadii[" + i + "]");
		}
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "typeTransformationMatrix");
		super.bindAttribute(5, "typeOffsets");
		super.bindAttribute(6, "contentsTransformationMatrix");
		super.bindAttribute(7, "contentsOffsets");
		super.bindAttribute(11, "in_modifiers");
		super.bindAttribute(12, "in_rChannelColor");
		super.bindAttribute(13, "in_gChannelColor");
		super.bindAttribute(14, "in_bChannelColor");
		super.bindAttribute(15, "in_aChannelColor");
	}

	// load uniforms
	public void loadNumRows(float numRows) { super.loadFloat(location_numRows, numRows); }
	public void loadScreenRatio(Vector2f ratio) { super.loadVector(location_screenRatio, ratio); }
	public void loadAmbientLight(float ambientLight) { super.loadFloat(location_ambientLight, ambientLight); }

	public void loadLights(List<Light> lights) {
		int size = lights.size();
		for (int i = 0; i < LightMaster.MAX_LIGHTS; ++i) {
			if (i < size) {
				super.loadVector(location_lightPositions[i], lights.get(i).position());
				super.loadVector(location_lightColors[i], lights.get(i).color());
				super.loadFloat(location_lightIntensities[i], lights.get(i).intensity());
				super.loadFloat(location_lightInnerRadii[i], lights.get(i).innerRadius());
				super.loadFloat(location_lightOuterRadii[i], lights.get(i).outerRadius());
				super.loadFloat(location_lightMaxRadii[i], lights.get(i).maxRadius());
			}
			else {
				super.loadVector(location_lightPositions[i], new Vector2f(0));
				super.loadVector(location_lightColors[i], new Vector4f(0));
				super.loadFloat(location_lightIntensities[i], 0);
				super.loadFloat(location_lightInnerRadii[i], 0);
				super.loadFloat(location_lightOuterRadii[i], -1); // loading -1 tells the shader to skip this light, since it doesn't exist
				super.loadFloat(location_lightMaxRadii[i], -1);
			}
		}
	}

}
