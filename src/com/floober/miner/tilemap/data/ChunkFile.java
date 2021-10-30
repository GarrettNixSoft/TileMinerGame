package com.floober.miner.tilemap.data;

import com.floober.engine.loaders.Loader;
import com.floober.engine.util.Logger;
import com.floober.engine.util.data.Pair;
import com.floober.engine.util.file.FileUtil;
import com.floober.miner.tilemap.Chunk;
import com.floober.miner.tilemap.Tile;
import com.floober.miner.util.ChunkImageGenerator;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChunkFile {

	// store name of save file
	private final String fileName;

	// store total chunks in map
	private int numChunksX;
	private int numChunksY;

	// store chunk size and file config
	private byte chunksPerFile;
	private byte chunkSize;

	// cache recently loaded chunks
	private final List<ChunkDataFile> chunkFileCache;
	private final int FILE_CACHE_SIZE = 10;

	private final List<Chunk> chunkCache;
	private final int CHUNK_CACHE_SIZE = 100;

	// remember which chunks have been moodified since last save
	private final List<Chunk> modifiedChunks;

	public ChunkFile(String fileName) {
		this.fileName = FileUtil.GAME_DIR + FileUtil.SEPARATOR + "maps" + FileUtil.SEPARATOR + fileName;
		readMetaData(this.fileName);
		chunkFileCache = new ArrayList<>(FILE_CACHE_SIZE);
		chunkCache = new ArrayList<>(CHUNK_CACHE_SIZE);
		modifiedChunks = new ArrayList<>();
	}

	public static boolean exists(String mapName) {
		String mapDirectoryName = FileUtil.generateFilePath("maps") + mapName;
		File mapFile;
		if ((mapFile = new File(mapDirectoryName)).exists()) {
			Logger.log("ChunkFile.exists(): Found a potential match! Validating...");
			File[] dirFiles = mapFile.listFiles();
			if (dirFiles == null) return false;
			boolean hasMetadata = false;
			boolean hasChunk = false;
			for (File file : dirFiles) {
				if (file.getName().endsWith(".json")) hasMetadata = true;
				else if (file.getName().endsWith(".chunk")) hasChunk = true;
			}
			Logger.log("Validation success: " + (hasMetadata && hasChunk));
			return hasMetadata && hasChunk;
		}
		else return false;
	}

	private void readMetaData(String fileName) {
		String metaFile = fileName + FileUtil.SEPARATOR + "metadata.json";
		JSONObject metaJSON = Loader.getJSON(metaFile);
		numChunksX = metaJSON.getInt("cols");
		numChunksY = metaJSON.getInt("rows");
		chunkSize = (byte) metaJSON.getInt("chunkSize");
		chunksPerFile = (byte) metaJSON.getInt("chunksPerFile");
	}

	public int getNumChunksX() {
		return numChunksX;
	}

	public int getNumChunksY() {
		return numChunksY;
	}

	public byte getChunkSize() {
		return chunkSize;
	}

	public Chunk getChunkAt(int r, int c) throws IllegalArgumentException, IOException {
//		Logger.log("[ChunkFile] Loading chunk [" + r + ", " + c + "]");
		// validate location
		if (c > numChunksX) throw new IllegalArgumentException("Requested Chunk out of bounds! Num rows: " + numChunksX + "; Requested: " + c);
		if (r > numChunksY) throw new IllegalArgumentException("Requested Chunk out of bounds! Num rows: " + numChunksY + "; Requested: " + r);
		// FIRST: check if this chunk is cached!
		Chunk chunkCacheCheck = tryRetrieveFromChunkCache(r, c);
		if (chunkCacheCheck != null) {
//			Logger.log("Successful retrieval of chunk from CHUNK_CACHE");
			return chunkCacheCheck;
		}
		// file cache flag (disables debug image generation)
		boolean fromCache = false;
		// determine which file to load
		int fileChunkSquareSize = (int) Math.sqrt(chunksPerFile);
//		Logger.log("chunksPerFile = " + chunksPerFile + ", fileChunkSquareSize = " + fileChunkSquareSize);
		int fileRow = r / fileChunkSquareSize;
		int fileCol = c / fileChunkSquareSize;
//		Logger.log("Chunk[" + r + "][" + c + "] is in file Cr" + fileRow + "c" + fileCol + ".chunk");
		String chunkFileID = "Cr" + fileRow + "c" + fileCol;
		ChunkDataFile chunkDataFile;
		// FIRST: CHECK IF IT'S IN THE CACHE!
		ChunkDataFile fileCacheCheck = tryRetrieveFromFileCache(chunkFileID);
		if (fileCacheCheck != null) {
			// if it is, skip the file loading!
			chunkDataFile = fileCacheCheck;
//			Logger.log("Successful retrieval of chunk file from CHUNK_FILE_CACHE");
			fromCache = true;
		}
		else {
			// If it's not, load it!
			String chunkFileName = fileName + FileUtil.SEPARATOR + chunkFileID + ".chunk";
			File chunkFile = new File(chunkFileName);
			// check if the file exists
			if (!chunkFile.exists()) throw new FileNotFoundException("Chunk file " + chunkFileName + " does not exist!");
			if (!chunkFile.canRead()) throw new FileNotFoundException("Chunk file " + chunkFileName + " cannot be read!");
			// load the file's binary data
			FileInputStream stream = new FileInputStream(chunkFile);
			byte[] chunkData = stream.readAllBytes();
			chunkDataFile = new ChunkDataFile(chunkFileID, chunkData);
			stream.close();
			// cache the data
			cacheChunkFile(chunkDataFile);
		}
		// get the data from the loaded file
		byte[] chunkData = chunkDataFile.data();
		// parse the data
		byte chunksPerFile = chunkData[0];
		byte chunkSize = chunkData[1];
		// check that the file contains the right amount of data
		int totalChunks = chunkSize * chunkSize * chunksPerFile;
		if (chunkData.length != totalChunks * 2 + 2) throw new IOException("File contains incorrect amount of bytes!" +
													"Expected: " + (totalChunks * 2 + 2) + "; Found: " + chunkData.length);
		// calculate where to start reading bytes from the file
		int bytesPerChunk = chunkSize * chunkSize * 2;
		int chunkRow = r % fileChunkSquareSize;
		int chunkCol = c % fileChunkSquareSize;
		int bytePointer = 2;
		int chunkPosition = chunkRow * fileChunkSquareSize + chunkCol;
//		chunkPosition /= 2;
		bytePointer += chunkPosition * bytesPerChunk;
//		Logger.log("Chunk[" + r + "][" + c + "] calculated to be position " + chunkPosition + " and therefore should begin at byte #" + bytePointer);
		// create the tiles based on the data
		Tile[][] tiles = new Tile[chunkSize][chunkSize];
		for (int row = 0; row < tiles.length; row++) {
			for (int col = 0; col < tiles[row].length; col++) {
				if (bytePointer % 2 != 0) throw new RuntimeException("Invalid pointer position!");
				tiles[row][col] = new Tile(chunkData[bytePointer++], chunkData[bytePointer++]);
				if (tiles[row][col].type() == -1 && tiles[row][col].contents() > 0) {
					throw new RuntimeException("File contained invalid tile! Chunk[" + r + "][" + c + "], tile (" + row + ", " + col + ")");
				}
			}
		}
		// create a chunk containing the tiles
		String id = Chunk.generateChunkID(r, c);
		Chunk result = new Chunk(id, tiles);
		// cache it
		cacheChunk(result);
		// debug
//		Logger.log("This chunk starts with tile: " + result.getTileAt(0,0));
		ChunkImageGenerator.generateChunkImage(result, result.id() + "_loaded");
		// return it
		return result;
	}

	/**
	 * Add a chunk to the chunk cache.
	 * @param chunk the chunk to save
	 */
	private void cacheChunk(Chunk chunk) {
		// remove oldest entry if cache is full
		if (chunkCache.size() >= CHUNK_CACHE_SIZE) {
			chunkCache.remove(0);
		}
		// add chunk to cache
		chunkCache.add(chunk);
	}

	/**
	 * Attempt to retrieve a Chunk from the cache list.
	 * Performs a linear search checking each entry's location,
	 * determined by the Chunk's id using the {@code getLocation()} method.
	 * If no match is found, returns null.
	 * @param r the row of the chunk to check for
	 * @param c the column of the chunk to check for
	 * @return the associated Chunk from the cache, or null if none is found
	 */
	private Chunk tryRetrieveFromChunkCache(int r, int c) {
		Pair<Integer, Integer> location = new Pair<>(r, c);
		// run the for-loop in reverse to speed up retrieval of most recent chunks
		for (int i = chunkCache.size() - 1; i >= 0; i--) {
			Chunk chunk = chunkCache.get(i);
			if (chunk.getLocation().equals(location)) {
				// move to the end of the cache (treated as most recent)
				chunkCache.remove(chunk);
				chunkCache.add(chunk);
				return chunk;
			}
		}
		return null;
	}

	/**
	 * Add a ChunkDataFile to the cache list. If the cache
	 * has reached its capacity, the oldest cache entry will
	 * be removed so GC can free the associated memory.
	 * @param chunkDataFile the file to cache
	 */
	private void cacheChunkFile(ChunkDataFile chunkDataFile) {
		// remove oldest entry if cache is full
		if (chunkFileCache.size() >= FILE_CACHE_SIZE) {
			chunkFileCache.remove(0);
		}
		// add file data to cache
		chunkFileCache.add(chunkDataFile);
	}

	/**
	 * Attempt to retrieve a ChunkDataFile from the cache list.
	 * Performs a linear search checking each entry's fileName
	 * property. If no match is found, returns null.
	 * @param chunkFileName the file name to search for
	 * @return the associated ChunkDataFile, or null if none is found
	 */
	private ChunkDataFile tryRetrieveFromFileCache(String chunkFileName) {
		// run the for-loop in reverse to speed up retrieval of most recent files
		for (int i = chunkFileCache.size() - 1; i >= 0; i--) {
			ChunkDataFile dataFile = chunkFileCache.get(i);
			if (dataFile.fileName().equals(chunkFileName)) {
				// move to the end of the cache (treated as most recent)
				chunkFileCache.remove(dataFile);
				chunkFileCache.add(dataFile);
				return dataFile;
			}
		}
		return null;
	}

	public static void writeChunksToFile(String mapDirectoryName, Chunk[][] chunks, byte chunkSize, byte chunksPerFile, int fileRow, int fileCol) throws IOException {
		Logger.log("Saving chunk file -- File position is R" + fileRow + ",C" + fileCol + "; chunks array is " + chunks.length + "x" + chunks[0].length);
		String chunkFileName = mapDirectoryName + FileUtil.SEPARATOR + "Cr" + fileRow + "c" + fileCol + ".chunk";
		File chunkFile = new File(chunkFileName);
		try {
			if (!chunkFile.exists() && !chunkFile.createNewFile()) {
				throw new IOException("Could not create chunk file on disk -- " + chunkFile.getPath());
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not create chunk file on disk -- " + chunkFile.getPath());
		}
//		r = r << 1;
//		c = c << 1;
//			Logger.log("Writing chunks to chunk files!");
		// create the stream to write bytes to
		FileOutputStream stream = new FileOutputStream(chunkFile);
		// calculate how many bytes we need to store, and create an array to store them
		int totalBytes = chunksPerFile * chunks[0][0].getNumTiles() * 2 + 2;
//			Logger.log("Total bytes needed for chunk file: " + totalBytes);
		byte[] fileBytes = new byte[totalBytes];
		// store all bytes
		fileBytes[0] = chunksPerFile;
		fileBytes[1] = chunkSize;
		int bytePointer = 2;
		int chunkSide = (int) Math.sqrt(chunksPerFile);
//			Logger.log("Writing " + (chunkSide * chunkSide) + " chunks");
		for (int row = 0; row < chunkSide; row++) {
			for (int col = 0; col < chunkSide; col++) {
//				int chunkRow = fileRow * chunkSide + row;
//				int chunkCol = fileCol * chunkSide + col;
				Logger.log("Writing chunk " + chunks[row][col] + " to byte array");
				bytePointer = writeTilesToByteArray(fileBytes, chunks[row][col].tiles(), bytePointer);
			}
		}
		// write all bytes to the file
		stream.write(fileBytes, 0, fileBytes.length);
		stream.flush();
		// close the stream
		stream.close();
	}

	private static int writeTilesToByteArray(byte[] bytes, Tile[][] tiles, int pointer) {
		if (pointer % 2 != 0) throw new RuntimeException("Pointer position invalid! (expected: even; was: " + pointer + ")");
		for (Tile[] row : tiles) {
			for (Tile col : row) {
				bytes[pointer++] = col.type();
				bytes[pointer++] = col.contents();
			}
		}
		return pointer;
	}

	public void setModified(Chunk chunk) {
		if (!modifiedChunks.contains(chunk)) modifiedChunks.add(chunk);
	}

	public boolean saveMapToDisk() throws IOException {

		// calculate side length of each file
		int fileChunkSquareSize = (int) Math.sqrt(chunksPerFile);

		// generate a hashMap of all chunks files that will need to be saved
		HashMap<String, Pair<Integer, Integer>> modifiedFiles = new HashMap<>();

		// for every modified chunk, put it into the hashMap
		for (Chunk chunk : modifiedChunks) {

			// get the world position of the chunk
			int row = chunk.getAbsoluteRow();
			int col = chunk.getAbsoluteCol();

			// determine which file it's in

			int fileRow = row / fileChunkSquareSize;
			int fileCol = col / fileChunkSquareSize;

			// generate that string
			String fileString = "Cr" + fileRow + "c" + fileCol;

			// if absent, add the string to the list
			modifiedFiles.computeIfAbsent(fileString, n -> new Pair<>(fileRow, fileCol));

		}

		for (String file : modifiedFiles.keySet()) {

			Pair<Integer, Integer> filePosition = modifiedFiles.get(file);
			int fileRow = filePosition.data1();
			int fileCol = filePosition.data2();

			Chunk[][] chunksToWrite = new Chunk[fileChunkSquareSize][fileChunkSquareSize];

			for (int r = 0; r < chunksToWrite.length; r++) {
				for (int c = 0; c < chunksToWrite[r].length; c++) {
					chunksToWrite[r][c] = getChunkAt(fileRow * fileChunkSquareSize + r, fileCol * fileChunkSquareSize + c);
				}
			}

//			String mapDirectoryName = FileUtil.generateFilePath("maps") + fileName;
			writeChunksToFile(fileName, chunksToWrite, chunkSize, chunksPerFile, fileRow, fileCol);

		}

		return true;
	}

//	public void saveChunkAt(int r, int c) throws IOException {
//		// validate location
//		if (c > numChunksX) throw new IllegalArgumentException("Requested Chunk out of bounds! Num rows: " + numChunksX + "; Requested: " + c);
//		if (r > numChunksY) throw new IllegalArgumentException("Requested Chunk out of bounds! Num rows: " + numChunksY + "; Requested: " + r);
//		// determine which file to write to
//		int fileRow = r / chunksPerFile;
//		int fileCol = c / chunksPerFile;
//		String chunkFileName = fileName + FileUtil.SEPARATOR + "Cr" + fileRow + "c" + fileCol + ".chunk";
//		File chunkFile = new File(chunkFileName);
//		// check if the file exists
//		if (!chunkFile.exists()) throw new FileNotFoundException("Chunk file " + fileName + " does not exist!");
//		if (!chunkFile.canWrite()) throw new FileNotFoundException("Chunk file " + fileName + " cannot be written to!");
//		// write the data to the file
//		//
//	}

}