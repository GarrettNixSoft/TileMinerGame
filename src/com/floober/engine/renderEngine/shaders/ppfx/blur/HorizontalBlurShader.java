package com.floober.engine.renderEngine.shaders.ppfx.blur;

import com.floober.engine.renderEngine.shaders.ShaderProgram;

public class HorizontalBlurShader extends ShaderProgram {

	private static final String VERTEX_FILE = "/com/floober/engine/renderEngine/shaders/shadercode/ppfx/blur/horizontalBlurVertex.glsl";
	private static final String FRAGMENT_FILE = "/com/floober/engine/renderEngine/shaders/shadercode/ppfx/blur/blurFragment.glsl";

	int location_targetWidth;

	public HorizontalBlurShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_targetWidth = super.getUniformLocation("targetWidth");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}

	public void loadTargetWidth(float targetWidth) {
		super.loadFloat(location_targetWidth, targetWidth);
	}

}