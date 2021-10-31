package com.floober.engine.renderEngine.elements;

import com.floober.engine.renderEngine.textures.TextureAtlas;
import com.floober.engine.util.Logger;
import org.joml.Vector4f;

public class TileElement extends TextureElement {

	public enum AtlasType {
		NORMAL, DESTROYED
	}

	private final TextureAtlas typeAtlas;
	private final TextureAtlas contentsAtlas;
	private final TextureAtlas destroyedAtlas;
	private AtlasType atlasType;
	private final Vector4f typeTextureOffsets;
	private final Vector4f contentsTextureOffsets;
	private boolean hasTransparency;
	private float type, contents;

	private int depth;

	public TileElement(TextureAtlas typeAtlas, TextureAtlas contentsAtlas, TextureAtlas destroyedAtlas, byte type, byte contents, float x, float y, int layer, int tileSize) {
		super(null, x, y, layer, tileSize, tileSize, false);
		if (typeAtlas == null) throw new IllegalArgumentException("typeAtlas is null!");
		if (contentsAtlas == null) throw new IllegalArgumentException("contentsAtlas is null!");
		if (destroyedAtlas == null) throw new IllegalArgumentException("destroyedAtlas is null!");
		this.typeAtlas = typeAtlas;
		this.contentsAtlas = contentsAtlas;
		this.destroyedAtlas = destroyedAtlas;
		// use the normal atlas by default
		atlasType = AtlasType.NORMAL;
		typeTextureOffsets = new Vector4f();
		contentsTextureOffsets = new Vector4f();
		setTypeAndContents(type, contents);
	}

	private void setTypeOffset(Vector4f offset, byte index) {
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

	private void setContentsOffset(Vector4f offset, byte index) {
		int column = index % contentsAtlas.numRows();
		int row = index / contentsAtlas.numRows();
		int tileOffsetPixels = (int) (width * 0.05);
		float tileOffsetCoords = (float) tileOffsetPixels / contentsAtlas.width();
		offset.x = (float) column / contentsAtlas.numRows() + tileOffsetCoords;
		offset.y = (float) row / contentsAtlas.numRows() + tileOffsetCoords;
		offset.z = offset.x + 1f / contentsAtlas.numRows() - tileOffsetCoords * 2;
		offset.w = offset.y + 1f / contentsAtlas.numRows() - tileOffsetCoords * 2;
//		Logger.log("Offsets for index " + index + " computed: " + offset);
	}

	public void setTypeAndContents(byte type, byte contents) {
		this.type = type;
		this.contents = contents;
		setTypeOffset(typeTextureOffsets, type);
		setContentsOffset(contentsTextureOffsets, contents);
//		if (type == 0) 		Logger.log("type 0 offsets     = " + typeTextureOffsets);
//		if (contents == 0) 	Logger.log("contents 0 offsets = " + contentsTextureOffsets);
//		if (contents != -1) Logger.log("Type texture offsets (" + type + ", " + contents + "): " + typeTextureOffsets + "; contents texture offsets: " + contentsTextureOffsets);
	}

	public void setRenderingAtlas(AtlasType atlasType) {
		this.atlasType = atlasType;
	}

	// GETTERS

	/**
	 * Get the atlas this tile should use to render.
	 * @return the atlas to use when rendering this tile
	 */
	public TextureAtlas getRenderAtlas() {
		return atlasType == AtlasType.NORMAL ? typeAtlas : destroyedAtlas;
	}

	public TextureAtlas getContentsAtlas() {
		return contentsAtlas;
	}

	public Vector4f getTypeTextureOffsets() {
		return typeTextureOffsets;
	}

	public Vector4f getContentsTextureOffsets() {
		return contentsTextureOffsets;
	}

	public boolean hasTransparency() { return hasTransparency; }

	public float getType() {
		return type;
	}

	public float getContents() {
		return contents;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
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