package com.floober.engine.entity.util;

import com.floober.engine.entity.core.Entity;
import com.floober.engine.entity.core.enemy.Enemy;
import com.floober.engine.entity.core.pickup.Pickup;
import com.floober.engine.entity.projectile.Projectile;
import com.floober.engine.util.math.Collisions;
import com.floober.miner.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EntityManager {

	// player and player utils
	private Player player;

	// entity lists
	private final List<Enemy> enemies = new ArrayList<>();
	private final List<Pickup> pickups = new ArrayList<>();
	private final List<Entity> misc = new ArrayList<>();

	// projectiles
	private final List<Projectile> playerProjectiles = new ArrayList<>();
	private final List<Projectile> enemyProjectiles = new ArrayList<>();

	// GETTERS
	public Player getPlayer() { return player; }
	public List<Enemy> getEnemies() { return enemies; }
	public List<Pickup> getPickups() { return pickups; }
	public List<Entity> getMiscEntities() { return misc; }

	public Entity getEntityById(String entityId) {
		if (entityId.equals("player")) {
			return player;
		}
		else {
			for (Enemy enemy : enemies)
				if (enemy.getId().equals(entityId))
					return enemy;
			for (Pickup pickup : pickups)
				if (pickup.getId().equals(entityId))
					return pickup;
			for (Entity entity : misc)
				if (entity.getId().equals(entityId))
					return entity;
			return null;
		}
	}

	// SETTERS
	public void setPlayer(Player player) {
		this.player = player;
	}

	// ADDING ENTITIES / ELEMENTS
	public void addEnemy(Enemy enemy) { enemies.add(enemy); }
	public void addPickup(Pickup pickup) { pickups.add(pickup); }
	public void addMisc(Entity entity) { misc.add(entity); }

	public void addPlayerProjectile(Projectile projectile) {
		playerProjectiles.add(projectile);
	}
	public void addEnemyProjectile(Projectile projectile) {
		enemyProjectiles.add(projectile);
	}

	/**
	 * Spawn an entity. It will be added to its appropriate list automatically.
	 * @param entity The entity to spawn.
	 */
	public void spawn(Entity entity) {
		if (entity instanceof Enemy e) {
			addEnemy(e);
		}
		else if (entity instanceof Pickup p) {
			addPickup(p);
		}
		else {
			addMisc(entity);
		}
	}

	// UPDATE
	public void update() {
		if (player != null) player.update();
		updateEverythingOtherThanThePlayer();
	}

//	public void updateNoPlayerMotion() {
//		if (player != null) player.updateNoMotion();
//		updateEverythingOtherThanThePlayer();
//	}

	private void updateEverythingOtherThanThePlayer() {
		updateAll(enemies);
		updateAll(pickups);
		updateAll(misc);

		// check for collisions
		doPlayerCollisions();

		// clean up entities
		cleanUp();
	}

	// UPDATE UTILITY FUNCTIONS
	// update all entities in a list
	private void updateAll(List<? extends Entity> entities) {
		for (Entity e : entities)
			e.update();
	}

	private void renderAll(List<? extends Entity> entities) {
		for (Entity e : entities)
			e.render();
	}

	/**
	 * Remove all expired entities from all lists.
 	 */
	private void cleanUp() {
		cleanUp(enemies);
		cleanUp(pickups);
		cleanUp(misc);
		cleanUp(playerProjectiles);
		cleanUp(enemyProjectiles);
	}

	// handle entity deletion
	private void cleanUp(List<? extends Entity> entities) {
		int size = entities.size();
		for (int i = 0; i < size; ++i) {
			if (entities.get(i).remove()) {
				entities.remove(i);
				--i; --size;
			}
		}
	}

	// COLLISION HANDLERS

	/**
	 * Check for collisions between the player and other entities.
	 * Calls {@code doPickupCollisions()} and {@code doEnemyCollisions()}.
	 */
	private void doPlayerCollisions() {
		if (!player.isDead()) {
			// CONTACT COLLISIONS
			doPickupCollisions();
			doEnemyCollisions();
		}
	}

	/**
	 * Check for collisions between the player and pickup items.
	 */
	private void doPickupCollisions() {
		for (Pickup p : pickups) {
			if (p.remove()) continue;
			if (Collisions.collision(player, p))
				p.pickUp(player);
		}
	}

	/**
	 * Check for collisions between the player and enemies.
	 * Attempt to damage the player if a collision is detected.
	 */
	private void doEnemyCollisions() {
		for (Enemy e : enemies) {
			if (e.remove()) continue;
			if (Collisions.collision(player, e)) {
				player.damage(e.getDamage());
			}
		}
	}

	// RENDER
	public void render() {
		renderAll(misc);
		renderAll(pickups);
		renderAll(enemies);
		player.render();
	}

}