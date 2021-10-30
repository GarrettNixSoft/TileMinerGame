package com.floober.engine.renderEngine.batches.transparent;

import com.floober.engine.renderEngine.batches.TransparentBatch;
import com.floober.engine.renderEngine.elements.TileElement;
import com.floober.engine.renderEngine.renderers.TileRenderer;
import com.floober.engine.renderEngine.textures.TextureAtlas;
import com.floober.engine.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileBatchTransparent extends TransparentBatch {

	private final Map<TextureAtlas, List<TileElement>> elements = new HashMap<>();
	private final TileRenderer renderer;

	public TileBatchTransparent(int layer, TileRenderer renderer) {
		super(layer);
		this.renderer = renderer;
	}

	public void addElement(TileElement element) {
		if (element.hasTransparency()) {
			List<TileElement> elementList = elements.computeIfAbsent(element.getRenderAtlas(), k -> new ArrayList<>());
			elementList.add(element);
		}
		else {
			Logger.logError("Tried to add an opaque tile element to a transparent batch!");
		}
	}

	public void clear() {
		elements.clear();
	}

	@Override
	public void render() {
		renderer.render(elements, false);
	}
}
