package com.floober.engine.renderEngine.batches.opaque;

import com.floober.engine.renderEngine.batches.OpaqueBatch;
import com.floober.engine.renderEngine.elements.TileElement;
import com.floober.engine.renderEngine.renderers.TileRenderer;
import com.floober.engine.renderEngine.textures.TextureAtlas;
import com.floober.engine.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileBatchOpaque extends OpaqueBatch {

	// tiles, which are instance rendered
	private final Map<TextureAtlas, List<TileElement>> elements = new HashMap<>();
	private final TileRenderer renderer;

	public TileBatchOpaque(int layer, TileRenderer renderer) {
		super(layer);
		this.renderer = renderer;
	}

	public void addElement(TileElement element) {
		if (!element.hasTransparency()) {
			List<TileElement> elementList = elements.computeIfAbsent(element.getRenderAtlas(), k -> new ArrayList<>());
			elementList.add(element);
		}
		else {
			Logger.logError("Tried to add tile element with transparency to an opaque batch!");
		}
	}

	public void clear() {
		elements.clear();
	}

	@Override
	public void render() {
		renderer.render(elements, true);
	}
}
