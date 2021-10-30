package com.floober.miner.tilemap;

import com.floober.engine.util.data.Pair;

/**
 * A Chunk represents a square-shaped section of Tile objects.
 * Used to control the memory impact of the map by loading and
 * unloading sections of it as they are needed.
 *
 * Chunks contain a 64x64 array of tiles, each representing 2 bytes of data.
 * Each chunk therefore represents 8KB of data.
 */
public record Chunk(String id, Tile[][] tiles) {

	public Pair<Integer, Integer> getLocation() {
		String rowStr = id.substring(2, id.lastIndexOf('c'));
		String colStr = id.substring(id.indexOf('c') + 1);
		int row = Integer.parseInt(rowStr);
		int col = Integer.parseInt(colStr);
		return new Pair<>(row, col);
	}

	public int getNumTiles() {
		return tiles.length * tiles[0].length;
	}

	public Tile getTileAt(int r, int c) {
		return tiles[r][c];
	}

	public void setTileAt(int r, int c, Tile tile) {
		tiles[r][c] = tile;
	}

	public static String generateChunkID(int r, int c) {
		return "Cr" + r + "c" + c;
	}

	public int getAbsoluteRow() {
		String rowSubstring = id.substring(2, id.indexOf('c'));
		return Integer.parseInt(rowSubstring);
	}

	public int getAbsoluteCol() {
		String colSubstring = id.substring(id.indexOf('c') + 1);
		return Integer.parseInt(colSubstring);
	}

	@Override
	public String toString() {
		String rowSubstring = id.substring(2, id.indexOf('c'));
		String colSubstring = id.substring(id.indexOf('c') + 1);
		int row = Integer.parseInt(rowSubstring);
		int col = Integer.parseInt(colSubstring);
		return "Chunk[" + row + "," + col + "]";
	}

}