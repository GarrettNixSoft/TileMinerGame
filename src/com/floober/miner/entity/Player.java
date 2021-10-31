package com.floober.miner.entity;

import com.floober.engine.display.DisplayManager;
import com.floober.engine.renderEngine.Render;
import com.floober.engine.renderEngine.lights.Light;
import com.floober.engine.renderEngine.lights.LightMaster;
import com.floober.engine.renderEngine.util.Layers;
import com.floober.engine.renderEngine.util.color.Colors;
import com.floober.engine.util.Logger;
import com.floober.engine.util.input.KeyInput;
import com.floober.miner.tilemap.Tile;
import com.floober.miner.tilemap.TileMap;

@SuppressWarnings("FieldCanBeLocal")
public class Player extends TileEntity {

	private final float DEFAULT_SPEED = Tile.SIZE * 2;
	private float SPEED_MULTIPLIER = 4;
	private final float BOOST_MULTIPLIER = 2;
	private final float DIG_MULTIPLIER = 0.4f;

//	private final float centerRange = 1f;

	// map movement
	private boolean drilling;
	private boolean left, right, up, down;
	private float moveSpeed;
	private int rowTarget;
	private int colTarget;

	// map light
	private Light flashlight;

	public Player(TileMap tileMap) {
		super(tileMap);
		x = 608;
		y = 480;
		width = height = 64;
		moveSpeed = DEFAULT_SPEED;
		layer = Layers.DEFAULT_LAYER + 2;
		// player's light
		flashlight = new Light(getPosition(), Colors.WHITE, 1.2f, 120, 165, 200);
		LightMaster.addLight(flashlight);
	}

	// GETTERS
	public boolean isLeft() {
		return left;
	}

	public boolean isRight() {
		return right;
	}

	public boolean isUp() {
		return up;
	}

	public boolean isDown() {
		return down;
	}

	// PLAYER MOTION
	private void checkDrillingFinished() {
		// get frame time
		float time = DisplayManager.getFrameTimeSeconds();
		// get current tile position
		int currCol = (int) x / Tile.SIZE;
		int currRow = (int) y / Tile.SIZE;
		// handle left or right motion
		if (left || right) {
			// move on x axis
			x += dx * time;
			// calculate target position
			int centerCol = colTarget * Tile.SIZE + Tile.SIZE / 2;
			// check finished
			if (currCol == colTarget) {
				if (right) {
					if (x >= centerCol) {
						x = centerCol;
						drilling = false;
						right = false;
						dx = 0;
						tileMap.destroyTileAt(currRow, currCol);
					}
				}
				else {
					if (x <= centerCol) {
						x = centerCol;
						drilling = false;
						left = false;
						dx = 0;
						tileMap.destroyTileAt(currRow, currCol);
					}
				}
//				// get distance from the center of this tile
//				float distance = Math.abs(centerCol - x);
//				// if it's within the specified range, snap to center and stop drilling
//				if (distance < centerRange * SPEED_MULTIPLIER) {
//					x = centerCol;
//					drilling = false;
//					left = right = false;
//					dx = 0;
//					tileMap.destroyTileAt(currRow, currCol);
//				}
			}
		}
		else if (up || down) {
			// move on y axis
			y += dy * time;
			// calculate target position
			int centerRow = rowTarget * Tile.SIZE + Tile.SIZE / 2;
			// check finished
			if (currRow == rowTarget) {
				if (up) {
					if (y < centerRow) {
						y = centerRow;
						drilling = false;
						up = false;
						dy = 0;
						tileMap.destroyTileAt(currRow, currCol);
					}
				}
				else {
					if (y > centerRow) {
						y = centerRow;
						drilling = false;
						down = false;
						dy = 0;
						tileMap.destroyTileAt(currRow, currCol);
					}
				}
//				// get distance from the center of this tile
//				float distance = Math.abs(centerRow - y);
//				// if it's within the specified range, snap to center and stop drilling
//				if (distance < centerRange * SPEED_MULTIPLIER) {
//					y = centerRow;
//					drilling = false;
//					up = down = false;
//					dy = 0;
//					tileMap.destroyTileAt(currRow, currCol);
//				}
			}
		}
	}

	private void checkDrilling() {

		// get key input and reject conflicting inputs
		checkInputs();

		// if no inputs, return
		if (!(up || down || left || right)) return;

		// get current tile position
		int currCol = (int) x / Tile.SIZE;
		int currRow = (int) y / Tile.SIZE;

		// check tile collisions in direction of motion
		if (up) {

			rowTarget = currRow - 1;
			colTarget = currCol;

			// special case: cannot go above row 7
			if (rowTarget < 7) {
				rowTarget = currRow;
				up = false;
				return;
			}

		}
		else if (down) {

			rowTarget = currRow + 1;
			colTarget = currCol;

			// cannot go past bottom of map
			if (rowTarget >= tileMap.getTileHeight()) {
				rowTarget = currRow;
				down = false;
				return;
			}

		}
		else if (right) {

			rowTarget = currRow;
			colTarget = currCol + 1;

			// cannot go past right side of map
			if (colTarget >= tileMap.getTileWidth()) {
				colTarget = currCol;
				right = false;
				return;
			}

		}
		else { // if (left)

			rowTarget = currRow;
			colTarget = currCol - 1;

			if (colTarget < 0) {
				colTarget = currCol;
				left = false;
				return;
			}

		}

		Tile targetTile = tileMap.getTileAt(rowTarget, colTarget);
		boolean blocked = targetTile.isBlocked();

		if (blocked) {
			// apply dig multiplier; dig multiplier is increased exponentially for every level of hardness of the target tile
			moveSpeed *= Math.pow(DIG_MULTIPLIER, targetTile.getHardness());
		}

		// apply directions to speed
		if (left) dx = -moveSpeed;
		else if (right) dx = moveSpeed;
		if (up) dy = -moveSpeed;
		else if (down) dy = moveSpeed;

		// flag self as drilling
		drilling = true;

	}

	private void checkInputs() {
		// check boosting
		moveSpeed = KeyInput.isShift() ? DEFAULT_SPEED * SPEED_MULTIPLIER * BOOST_MULTIPLIER : DEFAULT_SPEED * SPEED_MULTIPLIER;

		// check input directions
		up = KeyInput.isHeld(KeyInput.W);
		down = KeyInput.isHeld(KeyInput.S);
		left = KeyInput.isHeld(KeyInput.A);
		right = KeyInput.isHeld(KeyInput.D);

		// inputs in opposite directions cancel out
		if (up && down) up = down = false;
		if (left && right) left = right = false;

		// input axis collision; left/right wins
		if ((up || down) && (left || right)) {
			up = down = false;
		}

		// TESTING: Control light
		//

	}

	@Override
	public void update() {
		// move/drill
		if (drilling) checkDrillingFinished();
		else checkDrilling();
		fixBounds();
		// update light
		flashlight.position().set(getMapPosition());
	}

	@Override
	public void render() {
		setMapPosition();
		Render.drawRect(Colors.WHITE, x + xmap, y + ymap, layer, width, height, true);
	}
}