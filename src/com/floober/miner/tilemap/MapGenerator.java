package com.floober.miner.tilemap;

import com.floober.engine.util.Logger;
import com.floober.engine.util.file.FileUtil;
import com.floober.engine.util.math.MathUtil;
import com.floober.engine.util.math.RandomUtil;
import com.floober.miner.tilemap.data.ChunkFile;
import com.floober.miner.util.ChunkImageGenerator;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MapGenerator {

	public static void generateMap(String mapName, int numChunksX, int numChunksY, byte chunkSize, byte chunksPerFile) throws IllegalArgumentException, IOException {
		// validate parameters
		int chunkSide = (int) Math.sqrt(chunksPerFile);
		if (chunksPerFile < 1) throw new IllegalArgumentException("Must have at least 1 chunk per file");
		else if (chunksPerFile > 64) throw new IllegalArgumentException("Max chunks per file is 64");
		else if (!MathUtil.isPerfectSquare(chunksPerFile)) throw new IllegalArgumentException("chunksPerFile must be a perfect square");
		if (numChunksX % chunkSide != 0) throw new IllegalArgumentException("Chunk columns not a multiple of sqrt(chunksPerFile)");
		if (numChunksY % chunkSide != 0) throw new IllegalArgumentException("Chunk rows not a multiple of sqrt(chunksPerFile)");
		if (numChunksX < 4 || numChunksY < 4) throw new IllegalArgumentException("Cannot have less than 4 chunks");
		// create JSON metadata
		JSONObject metaJSON = new JSONObject();
		metaJSON.put("rows", numChunksY);
		metaJSON.put("cols", numChunksX);
		metaJSON.put("chunkSize", chunkSize);
		metaJSON.put("chunksPerFile", chunksPerFile);
		// save metadata to file
		String mapDirectoryName = FileUtil.generateFilePath("maps") + mapName;
		File mapDirectory = new File(mapDirectoryName);
		if (!mapDirectory.exists() && !mapDirectory.mkdirs()) {
			throw new IOException("Error creating map directory");
		}
		else {
			String metaFileName = mapDirectoryName + FileUtil.SEPARATOR + "metadata.json";
			File metaFile = new File(metaFileName);
			if (!metaFile.exists() && !metaFile.createNewFile()) {
				throw new IOException("Error creating metadata JSON file");
			}
			else {
				// write JSON to disk
				FileWriter writer = new FileWriter(metaFile);
				writer.write(metaJSON.toString());
				writer.close();
			}
		}
		// NOTE: the following 2 tasks are performed separately to allow for performance profiling each one individually
		// generate chunks
		Chunk[][] chunks = new Chunk[numChunksY][numChunksX];
		for (int r = 0; r < numChunksY; r++) {
			for (int c = 0; c < numChunksX; c++) {
//				chunks[r][c] = generateTestChunk(Chunk.generateChunkID(r, c));
				chunks[r][c] = generateChunk(r, c, chunkSize);
				// DEBUG
				ChunkImageGenerator.generateChunkImage(chunks[r][c]);
				// END_DEBUG
			}
		}
		// calculate how many chunk files to create
		int rows = numChunksY / chunkSide;
		int cols = numChunksX / chunkSide;
		Logger.log("There will be a total of " + (rows * cols) + " chunk files");
		// write chunks to disk
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				ChunkFile.writeChunksToFile(mapDirectoryName, chunks, chunkSize, chunksPerFile, r, c);
			}
		}
	}

	private static Chunk generateTestChunk(String id) {
		int chunkSize = 64;
		Tile[][] tiles = new Tile[chunkSize][chunkSize];
		for (Tile[] tileRow : tiles) {
			for (int i = 0; i < tileRow.length; i++) {
				tileRow[i] = new Tile(getTestTileType(), (byte) -1);
			}

		}
		return new Chunk(id, tiles);
	}

	private static byte getTestTileType() {
		if (Math.random() < 0.9) return 9;
		else return (byte) (17 + RandomUtil.getInt(0, 7));
	}

	private static Chunk generateChunk(int r, int c, byte chunkSize) {
//		Logger.log("Generating chunk [" + r + "][" + c + "]");
		int mapRow = r * chunkSize;
//		int mapCol = c * chunkSize;
		Tile[][] tiles = new Tile[chunkSize][chunkSize];
		for (int row = 0; row < tiles.length; row++) {
			for (int col = 0; col < tiles[row].length; col++) {
				tiles[row][col] = generateTile(mapRow + row);
			}
		}
//		Logger.log("This chunk starts with tile: " + tiles[0][0]);
		return new Chunk(Chunk.generateChunkID(r, c), tiles);
	}

	private static Tile generateTile(int depth) {
		byte type = -1;
		byte contents = -1;
		// decorate the top
		if (depth == 8) {
			type = RandomUtil.getByte(8);
		}
		// generate solid tiles starting on the 9th row
		else if (depth > 8) {
			if (depth < 108) type = RandomUtil.getByte(8, 16);
			else if (depth < 308) {
				double rockProbability = (depth - 108.0) / 200.0;
				if (Math.random() < rockProbability) type = RandomUtil.getByte(16, 24);
				else type = RandomUtil.getByte(8, 16);
			}
		}
		return new Tile(type, contents);
	}
}