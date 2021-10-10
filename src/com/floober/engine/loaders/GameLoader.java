package com.floober.engine.loaders;

import com.floober.engine.game.Game;
import com.floober.engine.game.RunGame;
import com.floober.engine.loaders.assets.*;
import com.floober.engine.renderEngine.renderers.LoadRenderer;
import com.floober.engine.util.Globals;
import com.floober.engine.util.Logger;
import com.floober.engine.util.time.TimeScale;

import java.util.ArrayList;
import java.util.List;

public class GameLoader {

	public static boolean LOAD_COMPLETE = false;

	private final List<AssetLoader> loaders = new ArrayList<>();

	public GameLoader(Game game) {
		// fade in the loading screen
//		animateLoadRenderer();
		// begin the loading process
		AssetLoader.Mode mode = AssetLoader.Mode.DIRECTORY;
		loaders.add(new FontLoader());
		loaders.add(new TextureLoader());
		loaders.add(new AnimationLoader());
		loaders.add(new SfxLoader());
		loaders.add(new MusicLoader());
		// TODO implement this per project if you need more asset types to load
		// set modes
		for (AssetLoader assetLoader : loaders) {
			assetLoader.setMode(mode);
		}
	}

	public void load() {
		for (AssetLoader loader : loaders) {
			loader.load();
		}
//		animateEndLoadRenderer();
		// Loading process done; put something fancy in the log to make this point easy to find
		Logger.log("**************************************");
		Logger.log("*                                    *");
		Logger.log("*    ****    LOADING DONE    ****    *");
		Logger.log("*                                    *");
		Logger.log("**************************************");
		LOAD_COMPLETE = true;
	}

	private void animateLoadRenderer() {
		long start = System.nanoTime();
		Globals.LOAD_STAGE = LoadRenderer.FADE_IN;
		while (TimeScale.getScaledTime(start) < 1000) {
			RunGame.loadRenderer.render();
		}
	}

	private void animateEndLoadRenderer() {
		long start = System.nanoTime();
		// FADE IN CHECK MARK
		Globals.LOAD_STAGE = LoadRenderer.DONE;
		while (TimeScale.getScaledTime(start) < 1000) {
			RunGame.loadRenderer.render();
		}
		// FADE OUT LOAD BAR
		Globals.LOAD_STAGE = LoadRenderer.FADE_OUT;
		while (TimeScale.getScaledTime(start) < 2000) {
			RunGame.loadRenderer.render();
		}
	}

}
