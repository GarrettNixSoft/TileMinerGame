package com.floober.miner.gameState.states;

import com.floober.engine.display.Display;
import com.floober.engine.game.Game;
import com.floober.engine.game.gameState.GameState;
import com.floober.engine.game.gameState.GameStateManager;
import com.floober.engine.renderEngine.fonts.fontMeshCreator.GUIText;
import com.floober.engine.renderEngine.util.Layers;
import com.floober.engine.renderEngine.util.color.Colors;
import com.floober.engine.util.Logger;
import com.floober.engine.util.input.KeyInput;
import org.joml.Vector3f;

public class MainMenuState extends GameState {

	private GUIText centerText;

	public MainMenuState(Game game, GameStateManager gsm) {
		super(game, gsm);
		centerText = new GUIText("Press ENTER to start", 1.25f,
				Game.getFont("default"), new Vector3f(0f, 0.5f, 0),
				1, true);
		centerText.setColor(Colors.GREEN);
		centerText.setWidth(0.5f);
		centerText.setEdge(0.2f);
		centerText.show();
	}

	@Override
	public void update() {
		centerText.update();
	}

	@Override
	public void render() {
		if (!centerText.isOnScreen()) centerText.show();
	}

	@Override
	public void handleInput() {
		if (KeyInput.isPressed(KeyInput.ENTER)) {
			gsm.transitionState(GameStateManager.PLAY_STATE, 1.5f, Colors.WHITE_3);
//			Game.playSfx("button_click");
		}
	}

	@Override
	public void cleanUp() {
		centerText.remove();
	}
}