package com.floober.engine.game.gameState;

import com.floober.engine.display.GameWindow;
import com.floober.engine.game.Game;
import com.floober.engine.game.gameState.transitions.TransitionFader;
import com.floober.engine.util.configuration.Config;
import com.floober.miner.gameState.states.MainMenuState;
import com.floober.miner.gameState.states.PlayState;
import com.floober.miner.gameState.states.SettingsState;
import com.floober.engine.game.gameState.transitions.StateTransitionManager;
import org.joml.Vector3f;

public class GameStateManager {

	// state IDs
	public static final int MAIN_MENU_STATE = 0;
	public static final int SETTINGS_STATE = 1;
	public static final int PLAY_STATE = 2;

	// game state array
	private static final int NUM_STATES = 3;
	private final GameState[] gameStates;
	private int currentState;

	// one state may be saved to return to later
	private GameState cachedState;
	private int cachedStateIndex;

	// game access for initializing game states
	private final Game game;

	// changing states
	private final StateTransitionManager transitionManager;

	public GameStateManager(Game game) {
		this.game = game;
		gameStates = new GameState[NUM_STATES];
		transitionManager = new StateTransitionManager(this);
		currentState = MAIN_MENU_STATE;
		setState(currentState);
	}

	public void transitionState(int state, float time, Vector3f color) {
		transitionManager.startTransition(state, time, color);
	}

	public void setCurrentState(int currentState) {
		if (gameStates[this.currentState] != null) throw new IllegalStateException("Cannot change state index after initial load");
		else this.currentState = currentState;
	}

	public void setState(int state) {
		unloadState(currentState);
		currentState = state;
		loadState(currentState);
		gameStates[currentState].init();
	}

	private void loadState(int state) {
		switch (state) {
			case MAIN_MENU_STATE -> gameStates[state] = new MainMenuState(game, this);
			case SETTINGS_STATE -> gameStates[state] = new SettingsState(game, this);
			case PLAY_STATE -> gameStates[state] = new PlayState(game, this);
		}
	}

	private void unloadState(int state) {
		if (gameStates[state] != null) gameStates[state].cleanUp();
		gameStates[state] = null;
	}

	/**
	 * Save the current Game State to the state cache, then set the
	 * current Game State. A subsequent call to {@code returnToSavedState()}
	 * will return the game to the state stored in the state cache.
	 * @param state The ID if the new Game State.
	 */
	public void setTempState(int state) {
		cachedState = gameStates[currentState];
		cachedStateIndex = currentState;
		setState(state);
	}

	/**
	 * Return the game to the Game State stored in the state cache.
	 * The state cache will be cleared.
	 */
	public void returnToSavedState() {
		unloadState(currentState);
		currentState = cachedStateIndex;
		gameStates[currentState] = cachedState;
		cachedState = null;
//		glfwSetWindowTitle(GameWindow.windowID,  Config.WINDOW_TITLE + " - " + gameStates[currentState].getStateID());
	}

	public void update() {
		// handle state transitions
		if (transitionManager.transitionInProgress()) {
			transitionManager.update();
		}
		// update current state
		gameStates[currentState].handleInput();
		gameStates[currentState].update();
	}

	public void render() {
		gameStates[currentState].render();
		transitionManager.render();
		TransitionFader.render();
	}

	public GameState getCurrentState() {
		return gameStates[currentState];
	}

}