package com.floober.engine.renderEngine.framebuffers;

import com.floober.engine.renderEngine.renderers.MasterRenderer;
import com.floober.engine.renderEngine.util.Layers;
import com.floober.engine.util.configuration.Config;
import com.floober.engine.util.math.MathUtil;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class FrameBuffers {

	private static final List<FrameBuffer> buffers = new ArrayList<>();

	public static FrameBuffer createFullScreenFrameBuffer() {
		return createFrameBuffer(Config.INTERNAL_WIDTH, Config.INTERNAL_HEIGHT, FrameBuffer.DEPTH_RENDER_BUFFER);
	}

	public static FrameBuffer createFrameBuffer(int width, int height) {
		return createFrameBuffer(width, height, FrameBuffer.NONE);
	}

	public static FrameBuffer createFrameBuffer(int width, int height, int depthBufferType) {
		FrameBuffer buffer = new FrameBuffer(width, height, depthBufferType);
		buffers.add(buffer);
		return buffer;
	}

	public static Vector3f convertToFramebufferPosition(float x, float y, float z, float width, float height, boolean centered, FrameBuffer buffer) {
		// translate to top left
		if (!centered) {
			x += width / 2;
			y += height / 2;
		}
		// invert y axis
		y = buffer.getHeight() - y;
		// convert pixel coordinates to OpenGL coordinates
		float displayX = -1 + (2f / buffer.getWidth()) * x;
		float displayY = -1 + (2f / buffer.getHeight()) * y;
		// convert Z position to [0 ... 1]
		float displayZ = MathUtil.interpolateBounded(0, Layers.TOP_LAYER, z);
		// return the result
		return new Vector3f(displayX, displayY, displayZ);
	}

	public static Vector2f convertToFramebufferScale(float width, float height, FrameBuffer buffer) {
		float displayWidth = width / buffer.getWidth();
		float displayHeight = -height / buffer.getHeight();
		return new Vector2f(displayWidth, displayHeight);
	}

	public static void cleanUp() {
		for (FrameBuffer buffer : buffers) {
			if (!buffer.isDeleted()) buffer.delete();
			else buffer.deletePreservedTextures();
		}
	}

}