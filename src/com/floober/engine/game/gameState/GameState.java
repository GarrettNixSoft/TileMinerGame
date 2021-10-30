package com.floober.engine.game.gameState;

import com.floober.engine.game.Game;

public abstract class GameState {

	protected Game game;
	protected GameStateManager gsm;

	public GameState(Game game, GameStateManager gsm) {
		this.game = game;
		this.gsm = gsm;
	}

	/**
	 * An optional method to override. It is implemented as an empty method by default because this
	 * method is always called as the last stem of the GSM's {@code setState()} method. This method
	 * should be used to perform any setup code that needs to be executed after the GameState's constructor,
	 * but before any game update frames occur.
	 */
	public void init() {}

	/**
	 * All game logic for this GameState should be run in this method, which is called once per frame.
	 */
	public abstract void update();

	/**
	 * This method will always be called once per frame, after all {@code update()} methods have executed.
	 * All rendering for this GameState should be done here.
	 */
	public abstract void render();

	/**
	 * This method is always called immediately before {@code update()}.
	 */
	public abstract void handleInput();

	/**
	 * This method is always called when the GSM calls {@code unloadState()} with this GameState as the target.
	 */
	public abstract void cleanUp();

}