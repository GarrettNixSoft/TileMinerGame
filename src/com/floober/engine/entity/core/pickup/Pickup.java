package com.floober.engine.entity.core.pickup;

import com.floober.engine.entity.core.Entity;
import com.floober.engine.entity.util.EntityManager;
import com.floober.miner.entity.Player;

public abstract class Pickup extends Entity {

	protected EntityManager entityManager;

	public Pickup(float x, float y, EntityManager entityManager) {
		super(x, y);
		this.entityManager = entityManager;
		textureElement.setHasTransparency(true);
	}

	// action on player pickup
	public abstract void pickUp(Player player);

	@Override
	public abstract void update();

	@Override
	public abstract void render();

}