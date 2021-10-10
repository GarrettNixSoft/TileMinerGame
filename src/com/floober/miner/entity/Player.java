package com.floober.miner.entity;

import com.floober.engine.display.DisplayManager;
import com.floober.engine.renderEngine.Render;
import com.floober.engine.renderEngine.util.Layers;
import com.floober.engine.renderEngine.util.color.Colors;
import com.floober.engine.util.Logger;
import com.floober.engine.util.input.KeyInput;
import com.floober.miner.tilemap.Tile;
import com.floober.miner.tilemap.TileMap;

@SuppressWarnings("FieldCanBeLocal")
public class Player extends TileEntity {

	private final float DEFAULT_SPEED = Tile.SIZE * 2;
	private final float BOOST_MULTIPLIER = 2;
	private final float DIG_MULTIPLIER = 0.4f;

	private final float centerRange = 2f;

	// map movement
	private boolean drilling;
	private boolean left, right, up, down;
	private float moveSpeed;
	private int rowTarget;
	private int colTarget;

	public Player(TileMap tileMap) {
		super(tileMap);
		x = 608;
		y = 480;
		width = height = 64;
		moveSpeed = DEFAULT_SPEED;
		layer = Layers.DEFAULT_LAYER + 2;
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
				// get distance from the center of this tile
				float distance = Math.abs(centerCol - x);
				// if it's within the specified range, snap to center and stop drilling
				if (distance < centerRange) {
					x = centerCol;
					drilling = false;
					left = right = false;
					tileMap.destroyTileAt(currRow, currCol);
				}
			}
		}
		else if (up || down) {
			// move on y axis
			y += dy * time;
			// calculate target position
			int centerRow = rowTarget * Tile.SIZE + Tile.SIZE / 2;
			// check finished
			if (currRow == rowTarget) {
				// get distance from the center of this tile
				float distance = Math.abs(centerRow - y);
				// if it's within the specified range, snap to center and stop drilling
				if (distance < centerRange) {
					y = centerRow;
					drilling = false;
					up = down = false;
					tileMap.destroyTileAt(currRow, currCol);
				}
			}
		}
	}

	private void checkDrilling() {

		// get key input and reject conflicting inputs
		checkInputs();

		// if no inputs, return
		if (!(up || down || left || right)) return;

		// apply directions to speed
		if (left) dx = -moveSpeed;
		else if (right) dx = moveSpeed;
		if (up) dy = -moveSpeed;
		else if (down) dy = moveSpeed;

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

		boolean blocked = tileMap.isBlocked(rowTarget, colTarget);

		if (blocked) {
			// apply dig multiplier
			moveSpeed *= DIG_MULTIPLIER;
		}

		// flag self as drilling
		drilling = true;

	}

	private void checkInputs() {
		// check boosting
		moveSpeed = KeyInput.isShift() ? DEFAULT_SPEED * BOOST_MULTIPLIER : DEFAULT_SPEED;

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
	}

	@Override
	public void update() {
		if (drilling) checkDrillingFinished();
		else checkDrilling();
		fixBounds();
	}

	@Override
	public void render() {
		setMapPosition();
		Render.drawRect(Colors.WHITE, x + xmap, y + ymap, layer, width, height, true);
	}
}