package com.floober.engine.background;

import com.floober.engine.animation.Animation;
import com.floober.engine.entity.core.Entity;
import com.floober.engine.renderEngine.textures.TextureSet;
import com.floober.miner.tilemap.TileMap;

/**
 * @author Floober
 * 
 * An element used in building a Background. Can be static or can
 * move around, on a timer or on events. Can be animated.
 * 
 */
public class BackgroundElement extends Entity {

	// create element
	public BackgroundElement(TextureSet textures, float x, float y) {
		super(x, y);
		this.x = x;
		this.y = y;
		animation = new Animation();
		animation.setFrames(textures);
		animation.setFrameTime(-1); // static by default
	}
	
	public void setAnimationDelay(int delay) {
		animation.setFrameTime(delay);
	}

	@Override
	public void update() {
		//
	}

	@Override
	public void render() {
		//
	}
}