package com.floober.engine.renderEngine.lights;

import com.floober.engine.display.Display;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Objects;

public final class Light {

	private final Vector2f position;
	private final Vector4f color;
	private float intensity;
	private float innerRadius;
	private float outerRadius;
	private float maxRadius;

	public Light(Vector2f position, Vector4f color, float intensity, float innerRadius, float outerRadius, float maxRadius) {
		this.position = position;
		this.color = color;
		this.intensity = intensity;
		this.innerRadius = innerRadius;
		this.outerRadius = outerRadius;
		this.maxRadius = maxRadius;
	}


	// GETTERS
	public Vector2f position() { return position; }

	public Vector4f color() { return color; }

	public float intensity() { return intensity; }

	public float innerRadius() { return innerRadius; }

	public float outerRadius() { return outerRadius; }

	public float maxRadius() { return maxRadius; }

	// SETTERS

	/**
	 * Set a new position for this Light.
	 * @param newPosition A position in world-space pixel coordinates, which will be automatically
	 *                    converted to screen-space coordinates.
	 */
	public void setPosition(Vector2f newPosition) {
		position.set(Display.convertToDisplayPosition2D(newPosition));
	}

	public void setIntensity(float intensity) {
		this.intensity = intensity;
	}

	public void setInnerRadius(float innerRadius) {
		this.innerRadius = innerRadius;
	}

	public void setOuterRadius(float outerRadius) {
		this.outerRadius = outerRadius;
	}

	public void setMaxRadius(float maxRadius) {
		this.maxRadius = maxRadius;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (Light) obj;
		return Objects.equals(this.position, that.position) &&
				Objects.equals(this.color, that.color) &&
				Float.floatToIntBits(this.intensity) == Float.floatToIntBits(that.intensity) &&
				Float.floatToIntBits(this.innerRadius) == Float.floatToIntBits(that.innerRadius) &&
				Float.floatToIntBits(this.outerRadius) == Float.floatToIntBits(that.outerRadius) &&
				Float.floatToIntBits(this.maxRadius) == Float.floatToIntBits(that.maxRadius);
	}

	@Override
	public int hashCode() {
		return Objects.hash(position, color, intensity, innerRadius, outerRadius, maxRadius);
	}

	@Override
	public String toString() {
		return "Light[" +
				"position=" + position + ", " +
				"color=" + color + ", " +
				"intensity=" + intensity + ", " +
				"innerRadius=" + innerRadius + ", " +
				"outerRadius=" + outerRadius + ", " +
				"maxRadius=" + maxRadius + ']';
	}


}