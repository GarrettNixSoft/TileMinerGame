package com.floober.miner.gameState.states;

import com.floober.engine.assets.Music;
import com.floober.engine.display.Display;
import com.floober.engine.game.Game;
import com.floober.engine.game.gameState.GameState;
import com.floober.engine.game.gameState.GameStateManager;
import com.floober.engine.renderEngine.Render;
import com.floober.engine.renderEngine.elements.TextureElement;
import com.floober.engine.renderEngine.fonts.fontMeshCreator.GUIText;
import com.floober.engine.renderEngine.lights.LightMaster;
import com.floober.engine.renderEngine.util.Layers;
import com.floober.engine.renderEngine.util.color.Colors;
import com.floober.engine.util.Logger;
import com.floober.engine.util.configuration.Config;
import com.floober.engine.util.configuration.Settings;
import com.floober.engine.util.input.KeyInput;
import com.floober.engine.util.performance.MethodProfiler;
import com.floober.engine.util.time.Timer;
import com.floober.miner.entity.Player;
import com.floober.miner.tilemap.Tile;
import com.floober.miner.tilemap.data.ChunkFile;
import com.floober.miner.tilemap.MapGenerator;
import com.floober.miner.tilemap.TileMap;
import org.joml.Vector3f;

import javax.swing.*;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;

public class PlayState extends GameState {

	private static GUIText debugDisplay;
	private static GUIText pauseDisplay;

	private final TextureElement testBg;

	private TileMap tileMap;
	private Player player;

	// pause system
	private boolean paused;

	private Timer saveTimer = new Timer(1);

	public PlayState(Game game, GameStateManager gsm) {
		super(game, gsm);
		testBg = new TextureElement(Game.getTexture("test_bg"), Display.centerX(), Display.centerY(), Layers.BOTTOM_LAYER, true);
//		LightMaster.setAmbientLight(0.5f);
		Game.playMusic("track1");
		Game.fadeMusicFromZero(Settings.musicVolume, 30);
	}

	@Override
	public void init() {
		// SET UP DEBUG TEXT
		debugDisplay = new GUIText("[Player]", 0.6f, Game.getFont("menu"), new Vector3f(0.15f, 0, 0), 1, false);
		debugDisplay.setColor(Colors.GREEN);
		debugDisplay.setWidth(0.4f);
		debugDisplay.setEdge(0.1f);
		debugDisplay.setOutlineColor(Colors.BLACK);
		debugDisplay.setBorderWidth(0.85f);
		debugDisplay.setBorderEdge(0.1f);
		debugDisplay.show();

		pauseDisplay = new GUIText("[Paused]", 0.8f, Game.getFont("menu"), new Vector3f(0.94f, 0, 0), 1, false);
		pauseDisplay.setColor(Colors.GREEN);
		pauseDisplay.setWidth(0.4f);
		pauseDisplay.setEdge(0.1f);
		pauseDisplay.setOutlineColor(Colors.BLACK);
		pauseDisplay.setBorderWidth(0.8f);
		pauseDisplay.setBorderEdge(0.05f);


		if (!ChunkFile.exists("test_map")) {
			long generationTimeMS = MethodProfiler.testRuntimeMilliseconds(() -> {
				try {
					MapGenerator.generateMap("test_map", 4, 4, (byte) 64, (byte) 4);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			Logger.log("Map generation took " + generationTimeMS + "ms");
		}
		ChunkFile chunkFile = new ChunkFile("test_map");
		tileMap = new TileMap(chunkFile, 0, 0);
		player = new Player(tileMap);
	}

	@Override
	public void update() {
		if (paused) {
			// do pause code
			return;
		}
		player.update();
		tileMap.update();
		tileMap.setPosition(Config.INTERNAL_WIDTH / 2f - player.getX(), Config.INTERNAL_HEIGHT / 2f - player.getY());

		// DEBUG TEXT
		int playerChunkRow = (int) (player.getY() / tileMap.CHUNK_SIZE_PIXELS);
		int playerChunkCol = (int) (player.getX() / tileMap.CHUNK_SIZE_PIXELS);
		debugDisplay.replaceText("[Player]\nChunk: [" + playerChunkRow + ", " + playerChunkCol + "]" +
				"\nChunk offset: [Row = " + tileMap.getChunkRowOffset() + ", Col = " + tileMap.getChunkColOffset() + "]" +
				"\nTile offset (in chunk): [Row = " + tileMap.getTileRowOffset() + ", Col = " + tileMap.getTileColOffset() + "]" +
				"\nTile offset (global): [Row = " + tileMap.getTileRowOffsetRaw() + ", Col = " + tileMap.getTileColOffsetRaw() + "]" +
				"\nIn tile: [" + (int) (player.getY() / Tile.SIZE) + ", " + (int) (player.getX() / Tile.SIZE) + "]" +
				"\nDirections: u=" + player.isUp() + " d=" + player.isDown() + " l=" + player.isLeft() + " r=" + player.isRight() +
				"\nVelocity: dx=" + player.getDX() + ", dy=" + player.getDY()
		);
	}

	@Override
	public void render() {
		// implement
		testBg.render();
		tileMap.render();
		player.render();
	}

	@Override
	public void handleInput() {
		// check save if already paused
		if (paused) {
			if (KeyInput.isCtrl() && KeyInput.isPressed(KeyInput.S) && saveTimer.finished()) {
				Logger.log("Key combo is still active");
				long saveTimeMS = MethodProfiler.testRuntimeMilliseconds(() -> tileMap.saveMapToDisk());
				if (tileMap.saveSuccess()) {
					JOptionPane.showMessageDialog(null, "Game saved.\n(Took " + saveTimeMS + "ms)", "Save Success", JOptionPane.PLAIN_MESSAGE);
				}
				else {
					JOptionPane.showMessageDialog(null, "An error occurred while saving the game.", "Save Error", JOptionPane.ERROR_MESSAGE);
				}
				saveTimer.start();
			}
		}

		// check new pause input
		if (KeyInput.isPressed(KeyInput.ESC)) {
			paused = !paused;
			if (paused) pauseDisplay.show();
			else pauseDisplay.hide();
		}
	}

	@Override
	public void cleanUp() {
		debugDisplay.remove();
		pauseDisplay.remove();
	}
}