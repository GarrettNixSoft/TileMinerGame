package com.floober.engine.entity.projectile;

import com.floober.engine.entity.core.Entity;
import com.floober.engine.entity.util.EntityManager;
import com.floober.miner.entity.TileEntity;
import com.floober.miner.tilemap.Tile;
import com.floober.miner.tilemap.TileMap;

public abstract class Projectile extends Entity {

	// attributes

	public Projectile(float x, float y) {
		super(x, y);
	}
}
