package com.floober.miner.tilemap;

import com.floober.engine.event.audio.AudioEvent;
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
		Logger.log("GENERATING NEW MAP: " + mapName + " with chunk size [" + numChunksY + " x " + numChunksX + "]," +
					"a chunkSize of " + chunkSize + " and storing " + chunksPerFile + " chunks per chunk file");
		// validate parameters
		if (!validateParameters(numChunksX, numChunksY, chunkSize, chunksPerFile)) {
			Logger.log("Map parameters invalid!");
			return;
		}
		// create JSON metadata
		JSONObject metaJSON = generateMapJSON(numChunksX, numChunksY, chunkSize, chunksPerFile);
		// save metadata to file
		String mapDirectoryName = FileUtil.generateFilePath("maps") + mapName;
		writeJSONToFile(metaJSON, mapDirectoryName);
		// NOTE: the following 2 tasks are performed separately to allow for performance profiling each one individually
		// generate chunks
		Chunk[][] chunks = generateChunks(numChunksX, numChunksY, chunkSize);
		// calculate how many chunk files to create
		int chunkSide = (int) Math.sqrt(chunksPerFile);
		int rows = numChunksY / chunkSide;
		int cols = numChunksX / chunkSide;
		Logger.log("There will be a total of " + (rows * cols) + " chunk files");
		// write chunks to disk
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {

				// get the chunks to write to this file
				Chunk[][] fileChunks = new Chunk[chunkSide][chunkSide];
				for (int cr = 0; cr < fileChunks.length; cr++) {
					for (int cc = 0; cc < fileChunks[cr].length; cc++) {
						int absoluteRow = r * chunkSide + cr;
						int absoluteCol = c * chunkSide + cc;
						fileChunks[cr][cc] = chunks[absoluteRow][absoluteCol];
					}
				}

				// write these chunks
				ChunkFile.writeChunksToFile(mapDirectoryName, fileChunks, chunkSize, chunksPerFile, r, c);

			}
		}
	}

	/**
	 * Validate map parameters before generating any chunks. The parameters must be as follows.
	 * @param numChunksX must be a multiple of the square root of chunkSize
	 * @param numChunksY must be a multiple of the square root of chunkSize
	 * @param chunkSize must be a perfect square
	 * @param chunksPerFile must be a perfect square in the range [1, 64]
	 * @return {@code true} if all requirements are met, and map generation may begin
	 */
	private static boolean validateParameters(int numChunksX, int numChunksY, byte chunkSize, byte chunksPerFile) {
		if (!MathUtil.isPerfectSquare(chunkSize)) throw new IllegalArgumentException("chunkSize must be a perfect square");
		if (chunksPerFile < 1) throw new IllegalArgumentException("Must have at least 1 chunk per file");
		else if (chunksPerFile > 64) throw new IllegalArgumentException("Max chunks per file is 64");
		else if (!MathUtil.isPerfectSquare(chunksPerFile)) throw new IllegalArgumentException("chunksPerFile must be a perfect square");
		int chunkSide = (int) Math.sqrt(chunksPerFile);
		if (numChunksX % chunkSide != 0) throw new IllegalArgumentException("Chunk columns not a multiple of sqrt(chunksPerFile)");
		if (numChunksY % chunkSide != 0) throw new IllegalArgumentException("Chunk rows not a multiple of sqrt(chunksPerFile)");
		if (numChunksX < 4 || numChunksY < 4) throw new IllegalArgumentException("Cannot have less than 4 chunks");
		return true;
	}

	/**
	 * Generate a JSON string from the metadata describing the data contained in the chunk files.
	 * @param numChunksX an int
	 * @param numChunksY an int
	 * @param chunkSize a byte
	 * @param chunksPerFile a byte
	 * @return the JSONObject created
	 */
	private static JSONObject generateMapJSON(int numChunksX, int numChunksY, byte chunkSize, byte chunksPerFile) {
		JSONObject metaJSON = new JSONObject();
		metaJSON.put("rows", numChunksY);
		metaJSON.put("cols", numChunksX);
		metaJSON.put("chunkSize", chunkSize);
		metaJSON.put("chunksPerFile", chunksPerFile);
		return metaJSON;
	}

	/**
	 * Write the map's JSON metadata to disk.
	 * @param metaJSON the JSON to write
	 * @param mapDirectoryName the location to save the JSON
	 * @throws IOException if there is an exception while writing the JSON to disk
	 */
	private static void writeJSONToFile(JSONObject metaJSON, String mapDirectoryName) throws IOException {
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
	}

	/**
	 * Generate an array of chunks of the specified size.
	 * @param numChunksX the number of columns in the array
	 * @param numChunksY the number of rows in the array
	 * @param chunkSize the side length of the chunks (i.e. 64 means a 64x64 tile chunk)
	 * @return the resulting array of chunks
	 */
	private static Chunk[][] generateChunks(int numChunksX, int numChunksY, byte chunkSize) {
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
		return chunks;
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

	private static final int START = 8;
	private static final int rockStartLevel = START + 32;
	private static final int rockFillLevel = rockStartLevel + 256;
	private static final int stoneStartLevel = START + 128;
	private static final int stoneFillLevel = START + 512;

	private static Tile generateTile(int depth) {
		byte type = -1;
		byte contents = -1;
		// decorate the top
		if (depth == START) {
			type = getType(TileType.GRASS);
		}
		// generate solid tiles starting on the 9th row
		else if (depth > START) {
			if (depth < rockStartLevel) { // dirt only
				type = getType(TileType.DIRT);
			}
			else if (depth < rockFillLevel) { // dirt and rock

				// determine where in the range of rocks we are
				int rockDepth = depth - rockStartLevel;
				int rockRange = rockFillLevel - rockStartLevel;
				double rockProbability = (double) rockDepth / rockRange;

				// given the calculated probability, roll to see if it's a rock
				if (Math.random() < rockProbability) type = getType(TileType.ROCK);
				else {
					// if it's not a rock, check if we're in the stone range -- if not, this tile is dirt
					if (depth < stoneStartLevel) {
						type = getType(TileType.DIRT);
					}
					else { // if we might be stone, calculate that probability

						int stoneDepth = depth - stoneStartLevel;
						int stoneRange = stoneFillLevel - stoneStartLevel;
						double stoneProbability = (double) stoneDepth / stoneRange;

						// roll for either stone or dirt
						type = Math.random() < stoneProbability ? getType(TileType.STONE) : getType(TileType.DIRT);

					}

				}

			}
			else if (depth < stoneFillLevel) { // rock and stone

				int stoneDepth = depth - stoneStartLevel;
				int stoneRange = stoneFillLevel - stoneStartLevel;
				double stoneProbability = (double) stoneDepth / stoneRange;

				type = Math.random() < stoneProbability ? getType(TileType.STONE) : getType(TileType.ROCK);

			}
			else { // all stone
				type = getType(TileType.STONE);
			}
		}
		return new Tile(type, contents);
	}

	private static byte getType(TileType type) {
		return switch (type) {
			case GRASS -> RandomUtil.getByte(8);
			case DIRT -> RandomUtil.getByte(8, 16);
			case ROCK -> RandomUtil.getByte(16, 24);
			case STONE -> RandomUtil.getByte(24, 32);
		};
	}

	private enum TileType {
		GRASS, DIRT, ROCK, STONE
	}

}