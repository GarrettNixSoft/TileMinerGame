package com.floober.engine.renderEngine.elements;

import com.floober.engine.renderEngine.textures.TextureAtlas;
import org.joml.Vector4f;

public class TileElement extends TextureElement {

	private final TextureAtlas typeAtlas;
	private final TextureAtlas contentsAtlas;
	private final Vector4f typeTextureOffsets;
	private final Vector4f contentsTextureOffsets;
	private boolean hasTransparency;
	private float type, contents;

	public TileElement(TextureAtlas typeAtlas, TextureAtlas contentsAtlas, byte type, byte contents, float x, float y, int layer, int tileSize) {
		super(null, x, y, layer, tileSize, tileSize, false);
		if (typeAtlas == null) throw new IllegalArgumentException("typeAtlas is null!");
		if (contentsAtlas == null) throw new IllegalArgumentException("contentsAtlas is null!");
		this.typeAtlas = typeAtlas;
		this.contentsAtlas = contentsAtlas;
		typeTextureOffsets = new Vector4f();
		contentsTextureOffsets = new Vector4f();
		setTypeAndContents(type, contents);
	}

	private void setTextureOffset(Vector4f offset, byte index) {
		int column = index % typeAtlas.numRows();
		int row = index / typeAtlas.numRows();
		int tileOffsetPixels = (int) (width * 0.05);
		float tileOffsetCoords = (float) tileOffsetPixels / typeAtlas.width();
		offset.x = (float) column / typeAtlas.numRows() + tileOffsetCoords;
		offset.y = (float) row / typeAtlas.numRows() + tileOffsetCoords;
		offset.z = offset.x + 1f / typeAtlas.numRows() - tileOffsetCoords * 2;
		offset.w = offset.y + 1f / typeAtlas.numRows() - tileOffsetCoords * 2;
//		Logger.log("Offsets for index " + index + " computed: " + offset);
	}

	public void setTypeAndContents(byte type, byte contents) {
		this.type = type;
		this.contents = contents;
		setTextureOffset(typeTextureOffsets, type);
		setTextureOffset(contentsTextureOffsets, contents);
	}

	// GETTERS
	public TextureAtlas getTypeAtlas() {
		return typeAtlas;
	}

	public TextureAtlas getContentsAtlas() {
		return contentsAtlas;
	}

	public Vector4f getTypeTextureOffsets() {
		return typeTextureOffsets;
	}
	public boolean hasTransparency() { return hasTransparency; }

	public float getType() {
		return type;
	}

	public float getContents() {
		return contents;
	}

	// SETTERS
	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
		transform();
	}

	public void setHasTransparency(boolean hasTransparency) {
		this.hasTransparency = hasTransparency;
	}

}