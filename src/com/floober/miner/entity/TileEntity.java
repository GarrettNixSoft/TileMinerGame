package com.floober.miner.entity;

import com.floober.engine.entity.core.Entity;
import com.floober.miner.tilemap.Tile;
import com.floober.miner.tilemap.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

public abstract class TileEntity extends Entity {

	protected final TileMap tileMap;
	protected float xmap, ymap;
	protected boolean bottomLeft, bottomRight, topLeft, topRight;

	public TileEntity(TileMap tileMap) {
		super();
		this.tileMap = tileMap;
	}

	public TileEntity(TileMap tileMap, float x, float y) {
		super(x, y);
		this.tileMap = tileMap;
	}

	public TileEntity(TileMap tileMap, float x, float y, int layer) {
		super(x, y, layer);
		this.tileMap = tileMap;
	}

	public TileMap getTileMap() {
		return tileMap;
	}

	protected void setMapPosition() {
		xmap = tileMap.getX();
		ymap = tileMap.getY();
	}

	public Vector2f getMapPosition() {
		return new Vector2f(x + xmap, y + ymap);
	}

	@Override
	protected void fixBounds() {
		if (x < width / 2) x = width / 2;
		if (y < height / 2) y = height / 2;
		if (x > tileMap.getWidth() - width / 2) x = tileMap.getWidth() - width / 2;
		if (y > tileMap.getHeight() - height / 2) y = tileMap.getHeight() - height / 2;
	}

	// can collide with tilemap
	protected void checkTileCollisions(float x, float y) {
		int leftTile = (int) (x - cwidth / 2) / Tile.SIZE;
		int rightTile = (int) (x + cwidth / 2 - 1) / Tile.SIZE;
		int topTile = (int) (y - cheight / 2) / Tile.SIZE;
		int bottomTile = (int) (y + cheight / 2 - 1) / Tile.SIZE;
		topLeft = tileMap.isBlocked(topTile, leftTile);
		topRight = tileMap.isBlocked(topTile, rightTile);
		bottomLeft = tileMap.isBlocked(bottomTile, leftTile);
		bottomRight = tileMap.isBlocked(bottomTile, rightTile);
	}

}