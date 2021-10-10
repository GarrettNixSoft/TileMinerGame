package com.floober.miner.tilemap;

import com.floober.engine.game.Game;
import com.floober.engine.renderEngine.Render;
import com.floober.engine.renderEngine.elements.TileElement;
import com.floober.engine.renderEngine.textures.TextureAtlas;
import com.floober.engine.renderEngine.util.Layers;
import com.floober.engine.util.Logger;
import com.floober.engine.util.configuration.Config;
import com.floober.engine.util.data.ArrayPrinter;
import com.floober.engine.util.input.KeyInput;
import com.floober.miner.tilemap.data.ChunkFile;

import java.io.IOException;

@SuppressWarnings("ManualArrayCopy")
public class TileMap {

	// constants
	public final int CHUNK_SIZE_PIXELS;

	// contents
	private ChunkFile chunks;
	private final int totalChunksX;
	private final int totalChunksY;
	private final int screenChunksX;
	private final int screenChunksY;
	private final int chunkSize;

	// rendering
	private TextureAtlas typeAtlas;
	private TextureAtlas contentsAtlas;

	private final Chunk[][] screenChunks;
	private final TileElement[][] screenTiles;
	private final int rowsToDraw;
	private final int colsToDraw;
	private int chunkRowOffset, tileRowOffset, tileRowOffsetRaw;
	private int chunkColOffset, tileColOffset, tileColOffsetRaw;
	private int prevChunkRowOffset, prevChunkColOffset;
	private final int tilePad = 4;

	// chunk array manipulation
	private static final int UP = 0;
	private static final int DOWN = 1;
	private static final int LEFT = 2;
	private static final int RIGHT = 3;
	private static final int UP_LEFT = 4;
	private static final int UP_RIGHT = 5;
	private static final int DOWN_LEFT = 6;
	private static final int DOWN_RIGHT = 7;

	// position
	private float x, y;
	private final int width, height;
	private final int xmin, ymin;
	private final int xmax, ymax;

//	private List<TileElement> frameCoverage = new ArrayList<>();
//	private int frameDupes;

	public TileMap(ChunkFile chunks, float startX, float startY) {
		// assign fields
		this.chunks = chunks;
		totalChunksX = chunks.getNumChunksX();
		totalChunksY = chunks.getNumChunksY();
		// load tile texture atlases
		typeAtlas = Game.getTextureAtlas("tiles_base");
		if (typeAtlas == null) throw new RuntimeException("typeAtlas: it's not supposed to be null why is it null");
		contentsAtlas = Game.getTextureAtlas("tiles_ore");
		if (contentsAtlas == null) throw new RuntimeException("contentsAtlas: it's not supposed to be null why is it null");
		// set chunk values
		chunkSize = chunks.getChunkSize();
		CHUNK_SIZE_PIXELS = chunkSize * Tile.SIZE;
		screenChunksX = Math.max(Config.INTERNAL_WIDTH / CHUNK_SIZE_PIXELS, 1) + 1;
		screenChunksY = Math.max(Config.INTERNAL_HEIGHT / CHUNK_SIZE_PIXELS, 1) + 1;
		Logger.log("Chunks to render on X axis: " + screenChunksX);
		Logger.log("Chunks to render on Y axis: " + screenChunksY);
		// calculate dimensions
		width = chunks.getNumChunksX() * CHUNK_SIZE_PIXELS;
		height = chunks.getNumChunksY() * CHUNK_SIZE_PIXELS;
		rowsToDraw = Config.INTERNAL_HEIGHT / Tile.SIZE + tilePad;
		colsToDraw = Config.INTERNAL_WIDTH / Tile.SIZE + tilePad;
		Logger.log("Tile rows to draw: " + rowsToDraw);
		Logger.log("Tile cols to draw: " + colsToDraw);
		// create rendering arrays
		screenChunks = new Chunk[screenChunksY][screenChunksX];
		screenTiles = new TileElement[rowsToDraw][colsToDraw];
		// set movement constraints
		xmin = Config.INTERNAL_WIDTH - width;
		xmax = 0;
		ymin = Config.INTERNAL_HEIGHT - height;
		ymax = 0;
		// set starting position
		setPosition(startX, startY);
		// DEBUG
		// load all chunks
		for (int r = 0; r < 4; r++) {
			for (int c = 0; c < 4; c++) {
				try {
					chunks.getChunkAt(r,c);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// END_DEBUG
		// load everything we need
		initialize();
	}

	// GETTERS
	public float getX() {
		return this.x;
	}
	public float getY() {
		return this.y;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public int getTileWidth() { return totalChunksX * Tile.SIZE; }
	public int getTileHeight() { return totalChunksY * Tile.SIZE; }
	public int getChunkRowOffset() {
		return chunkRowOffset;
	}
	public int getTileRowOffset() {
		return tileRowOffset;
	}
	public int getTileRowOffsetRaw() {
		return tileRowOffsetRaw;
	}
	public int getChunkColOffset() {
		return chunkColOffset;
	}
	public int getTileColOffset() {
		return tileColOffset;
	}
	public int getTileColOffsetRaw() {
		return tileColOffsetRaw;
	}

	// CREATION
	private void initialize() {
		loadStartingChunks();
		initScreenTiles();
	}

	private void loadStartingChunks() {
		for (int r = 0; r < screenChunks.length; r++) {
			for (int c = 0; c < screenChunks[r].length; c++) {
				int chunkRow = chunkRowOffset * chunkSize + r;
				int chunkCol = chunkColOffset * chunkSize + c;
				try {
					screenChunks[r][c] = chunks.getChunkAt(chunkRow, chunkCol);
				} catch (IOException e) {
					e.printStackTrace();
					screenChunks[r][c] = null;
				}
			}
		}
	}

	private void initScreenTiles() {
		for (int r = 0; r < screenTiles.length; r++) {
			for (int c = 0; c < screenTiles[r].length; c++) {
				Tile tile = getTileAt(r, c);
				screenTiles[r][c] = new TileElement(typeAtlas, contentsAtlas, tile.type(), tile.contents(),
						r * Tile.SIZE, c * Tile.SIZE, Layers.DEFAULT_LAYER, Tile.SIZE);
			}
		}
	}

//	/**
//	 * Ensures that all rows in the chunks array contain
//	 * the same number of chunks.
//	 * @param chunks the chunks array
//	 */
//	private void validateChunks(Chunk[][] chunks) {
//		int chunksX = chunks[0].length;
//		for (int i = 1; i < chunks.length; i++) {
//			if (chunks[i].length != chunksX) {
//				throw new IllegalArgumentException("Invalid chunk array!" +
//						"All rows must contain the same number of chunks." +
//						"Expected: " + chunksX + "; row #" + i + " contains: " + chunks[i].length);
//			}
//		}
//	}

	public Tile getTileAt(int row, int col) {
		try {
			// determine which chunk the requested tile is in
			int chunkRow = row / chunkSize;
			int chunkCol = col / chunkSize;
			// determine where in the chunk the tile is
			int tileRow = row % chunkSize;
			int tileCol = col % chunkSize;
			// GET THE CHUNK AND TILE
			// first look in screen chunks
			Chunk chunk;
			if (chunkRow >= chunkRowOffset && chunkRow < chunkRowOffset + screenChunksY &&
				chunkCol >= chunkColOffset && chunkCol < chunkColOffset + screenChunksX) {
				chunk = screenChunks[chunkRow - chunkRowOffset][chunkCol - chunkColOffset];
			}
			else {
				chunk = chunks.getChunkAt(chunkRow, chunkCol); // this should be optimized by the caching system
			}
			return chunk.getTileAt(tileRow, tileCol);
		} catch (ArrayIndexOutOfBoundsException | IOException e) {
			return null;
		}
	}

	public void setTileAt(int row, int col, byte type, byte contents) {
		try {
			// determine which chunk the requested tile is in
			int chunkRow = row / chunkSize;
			int chunkCol = col / chunkSize;
			// determine where in the chunk the tile is
			int tileRow = row % chunkSize;
			int tileCol = col % chunkSize;
			// GET THE CHUNK AND TILE
			// first look in screen chunks
			Chunk chunk;
			if (chunkRow >= chunkRowOffset && chunkRow < chunkRowOffset + screenChunksY &&
					chunkCol >= chunkColOffset && chunkCol < chunkColOffset + screenChunksX) {
				chunk = screenChunks[chunkRow - chunkRowOffset][chunkCol - chunkColOffset];
			}
			else {
				chunk = chunks.getChunkAt(chunkRow, chunkCol); // this should be optimized by the caching system
			}
			chunk.setTileAt(tileRow, tileCol, new Tile(type, contents));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void destroyTileAt(int row, int col) {
		setTileAt(row, col, (byte) -1, (byte) -1);
	}

	/**
	 * Check whether the tile at a given position allows entities to pass through it.
	 * @param row the absolute row of the tile to check
	 * @param col the absolute col of the tile to check
	 * @return {@code true} if the corresponding tile does not allow entities to pass through
	 */
	public boolean isBlocked(int row, int col) {
		try {
			return getTileAt(row, col).isBlocked();
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public void setPosition(float x, float y) {
		// set the position
		this.x = x;
		this.y = y;
		// keep map inside boundaries
		fixBounds();
		// store previous chunk offsets
		prevChunkRowOffset = chunkRowOffset;
		prevChunkColOffset = chunkColOffset;
		// CALCULATE RENDERING OFFSETS
		// chunk offsets: the number of chunks to skip
		chunkRowOffset = (int) (-this.y / CHUNK_SIZE_PIXELS);
		chunkColOffset = (int) (-this.x / CHUNK_SIZE_PIXELS);
		// chunk tile offsets: the number of tiles in the skipped chunks
		int chunkTileRows = chunkRowOffset * chunkSize;
		int chunkTileCols = chunkColOffset * chunkSize;
		tileRowOffsetRaw = (int) (-this.y / Tile.SIZE);
		tileRowOffset = tileRowOffsetRaw - chunkTileRows;
		tileColOffsetRaw = (int) (-this.x / Tile.SIZE);
		tileColOffset = tileColOffsetRaw - chunkTileCols;
		// update screen tiles
		selectScreenChunks();
	}

	private void fixBounds() {
		if (x < xmin) x = xmin;
		if (x > xmax) x = xmax;
		if (y < ymin) y = ymin;
		if (y > ymax) y = ymax;
	}

	private void selectScreenChunks() {
		// only change chunk selection if chunk offset changed
		if (chunkRowOffset != prevChunkRowOffset || chunkColOffset != prevChunkColOffset) {
			// determine which direction we need to shift chunks
			int shiftType = getShiftType();
			// move chunks we can keep; then determine which chunks need to be loaded and load them
			switch (shiftType) {
				case UP -> loadChunksUp();
				case DOWN -> loadChunksDown();
				case LEFT -> loadChunksLeft();
				case RIGHT -> loadChunksRight();
				case UP_LEFT -> loadChunksUpLeft();
				case UP_RIGHT -> loadChunksUpRight();
				case DOWN_LEFT -> loadChunksDownLeft();
				case DOWN_RIGHT -> loadChunksDownRight();
			}
		}
	}

	public void update() {
		// ... doesn't do anything, actually
	}

	public void render() {
		if (KeyInput.isPressed(KeyInput.P)) {
			ArrayPrinter.print(screenChunks);
		}
		// render by chunk
		for (int r = 0; r < screenChunksY; r++) {
			for (int c = 0; c < screenChunksX; c++) {
				renderChunk(r, c);
			}
		}
	}

	private void renderChunk(int r, int c) {
		// stop if all available tiles are already rendered
//		if (tileRenderRow >= rowsToDraw && tileRenderCol >= colsToDraw) {
//			Logger.log("Not rendering any tiles from chunk [" + r + ", " + c + "]; screen is already full");
//			return;
//		}


		// get chunk rendering offsets
		// GET THE STARTING SCREEN TILE POSITIONS
		// These are based on what tiles in the screenTiles array should already
		// have been updated by previously rendered chunks -- thus for screen
		// chunk (0,0) these values are set to 0
		int screenColStart = 0;
		int screenRowStart = 0;
		if (r > 0) {
			screenRowStart = chunkSize * r - tileRowOffset + tilePad / 2;
//			if (screenRowStart < 0 || screenRowStart > rowsToDraw) Logger.log("Out of bounds screenRowStart! (" + screenRowStart + ") when rendering Chunk[" + r + "][" + c + "]");
		}
		if (c > 0) {
			screenColStart = chunkSize * c - tileColOffset + tilePad / 2;
//			if (screenColStart < 0 || screenColStart > rowsToDraw) Logger.log("Out of bounds screenColStart! (" + screenColStart + ") when rendering Chunk[" + r + "][" + c + "]");
		}

		// don't render chunks that are entirely off screen
		if (screenRowStart > rowsToDraw || screenColStart > colsToDraw) {
			return;
		}


		// get the chunk
		Chunk currentChunk = screenChunks[r][c];
		// get starting offsets for this chunk
		int startRow = r == 0 ? tileRowOffset : 0; // offsets only apply to chunk row 0 and col 0
		int startCol = c == 0 ? tileColOffset : 0; // all other chunks render in full (or until screen tile limit is met)
		if (startRow != 0) startRow -= tilePad / 2; // add padding to the top
		if (startCol != 0) startCol -= tilePad / 2; // add padding to the left


//		if (chunkRowOffset >= 1 || chunkColOffset >= 1) {
//			Logger.log("Attempting to render Chunk[" + (r + chunkRowOffset) + "][" + (c + chunkColOffset) + "]");
//		}

//		if (KeyInput.isPressed(KeyInput.P)) {
//			ArrayPrinter.print(screenChunks);
//			Logger.log("tileRowOffset = " + tileRowOffset + ", tileColOffset = " + tileColOffset);
//			Logger.log("tileRowOffsetRaw = " + tileRowOffsetRaw + ", tileColOffsetRaw = " + tileColOffsetRaw);
//			Logger.log(currentChunk.id() + " is at screenRowStart " + screenRowStart + " and screenColStart " + screenColStart);
//		}


//		Logger.log("Starting to render chunk [" + r + "," + c + "]; starting from tile: [" + startRow + "," + startCol + "]");
//		Logger.log("Chunk[" + r + "][" + c + "]; rendering from row/col offset = [" + startRow + "," + startCol + "]");

		// an iterator, since screenColStart is how we return to the beginning each new row
		int screenRow, screenCol;

		// render all visible tiles in the chunk
		screenRow = screenRowStart;
		for (int row = startRow; row < chunkSize && screenRow < rowsToDraw; row++, screenRow++) {
			screenCol = screenColStart;
			for (int col = startCol; col < chunkSize && screenCol < colsToDraw; col++, screenCol++) {
				// ignore tiles in underflow padding
				if (row < 0 || col < 0 || screenRow < 0 || screenCol < 0) {
					continue;
				}
				// get the tile
				Tile tileToRender = currentChunk.getTileAt(row, col);
//				if (r > 0 && row == 0 && col == 0) Logger.log("First tile of Chunk[1][x] is " + tileToRender);
				// render it only if its type indicates it would be visible (not -1)
				if (tileToRender.type() != -1) {
					// get position to render this tile at
					float tileX = x + (col * Tile.SIZE) + ((c + chunkColOffset) * CHUNK_SIZE_PIXELS);
					float tileY = y + (row * Tile.SIZE) + ((r + chunkRowOffset) * CHUNK_SIZE_PIXELS);
					TileElement tileElement = screenTiles[screenRow][screenCol];
//					byte debugContents = (byte) (tileRenderRow * 8 + tileRenderCol);
					tileElement.setTypeAndContents(tileToRender.type(), tileToRender.contents());
					tileElement.setPosition(tileX, tileY);
					tileElement.transform();
					Render.drawTile(tileElement);
				}
			}
		}
	}

	/**
	 * Determine which direction to shift chunks in the screenChunks array when a chunk border is crossed.
	 * @return the integer representing which direction (or combination of directions, in rare cases) to shift chunks
	 */
	private int getShiftType() {
		if (chunkRowOffset < prevChunkRowOffset) {
			if (chunkColOffset == prevChunkColOffset) return UP;
			else if (chunkColOffset < prevChunkColOffset) return UP_LEFT;
			else return UP_RIGHT;
		}
		else if (chunkRowOffset > prevChunkRowOffset) {
			if (chunkColOffset == prevChunkColOffset) return DOWN;
			else if (chunkColOffset < prevChunkColOffset) return DOWN_LEFT;
			else return DOWN_RIGHT;
		}
		if (chunkColOffset < prevChunkColOffset) return LEFT;
		else return RIGHT;
	}

	private void loadChunksUp() {
		Logger.log("loadChunksUp()");
		// Step 1: move screen chunks down, discarding bottom row
		for (int r = screenChunks.length - 1; r >= 1; r--) { // start at last row, end at second row
			System.arraycopy(screenChunks[r - 1], 0, screenChunks[r], 0, screenChunks[r].length); // replace with row above
		}
		// Step 2: load chunks into top row
		for (int c = 0; c < screenChunks[0].length; c++) {
			int chunkRow = chunkRowOffset;
			int chunkCol = chunkColOffset + c;
			try {
				screenChunks[0][c] = chunks.getChunkAt(chunkRow, chunkCol);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadChunksDown() {
		Logger.log("loadChunksDown()");
		// Step 1: move screen chunks up, discarding top row
		for (int r = 0; r < screenChunks.length - 1; r++) {
			System.arraycopy(screenChunks[r + 1], 0, screenChunks[r], 0, screenChunks[r].length);
		}
		// Step 2: load chunks into bottom row
		int row = screenChunks.length - 1;
		for (int c = 0; c < screenChunks[row].length; c++) {
			int chunkRow = chunkRowOffset + row;
			int chunkCol = chunkColOffset + c;
			try {
				screenChunks[row][c] = chunks.getChunkAt(chunkRow, chunkCol);
			} catch (IOException e) {
				Logger.log("Chunks to be loaded were out of range -- end of map reached in Y direction");
			}
		}
	}

	private void loadChunksLeft() {
		Logger.log("loadChunksLeft()");
		// Step 1: move screen chunks right, discarding rightmost column
		for (int c = screenChunks[0].length - 1; c >= 1; c--) {
			for (int r = 0; r < screenChunks.length; r++) {
				screenChunks[r][c] = screenChunks[r][c - 1];
			}
		}
		// Step 2: load chunks into left column
		for (int r = 0; r < screenChunks.length; r++) {
			int chunkRow = chunkRowOffset + r;
			int chunkCol = chunkColOffset;
			try {
				screenChunks[r][0] = chunks.getChunkAt(chunkRow, chunkCol);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadChunksRight() {
		// CHECK: only load right if chunks exist in that direction!
		if (chunkColOffset >= totalChunksX - 1) return;
		Logger.log("loadChunksRight()");
		// Step 1: move screen chunks left, discarding leftmost column
		for (int c = 0; c < screenChunks[0].length - 1; c++) {
			for (int r = 0; r < screenChunks.length; r++) {
				screenChunks[r][c] = screenChunks[r][c + 1];
			}
		}
		// Step 2: load chunks into right column
		int col = screenChunks[0].length - 1;
		for (int r = 0; r < screenChunks.length; r++) {
			int chunkRow = chunkRowOffset + r;
			int chunkCol = chunkColOffset + col;
			try {
				screenChunks[r][col] = chunks.getChunkAt(chunkRow, chunkCol);
			} catch (IOException e) {

				Logger.log("Chunks to be loaded were out of range -- end of map reached in X direction");
			}
		}
	}

	private void loadChunksUpLeft() {
		loadChunksUp();
		loadChunksLeft();
	}

	private void loadChunksUpRight() {
		loadChunksUp();
		loadChunksRight();
	}

	private void loadChunksDownLeft() {
		loadChunksDown();
		loadChunksLeft();
	}

	private void loadChunksDownRight() {
		loadChunksDown();
		loadChunksRight();
	}

}