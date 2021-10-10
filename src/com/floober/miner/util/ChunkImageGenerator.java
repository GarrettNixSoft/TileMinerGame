package com.floober.miner.util;

import com.floober.engine.util.Logger;
import com.floober.engine.util.file.FileUtil;
import com.floober.engine.util.math.MathUtil;
import com.floober.miner.tilemap.Chunk;
import com.floober.miner.tilemap.Tile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ChunkImageGenerator {

	/**
	 * Quickly generate a chunk image with default settings.
	 * The tile pixel size will be 4 and the chunk's ID will
	 * be used for the file name.
	 * @param chunk the chunk to generate an image of
	 */
	public static void generateChunkImage(Chunk chunk) {
		generateChunkImage(chunk, 4, "");
	}

	public static void generateChunkImage(Chunk chunk, int imageTileSize) {
		generateChunkImage(chunk, imageTileSize, "");
	}

	public static void generateChunkImage(Chunk chunk, String fileName) {
		generateChunkImage(chunk, 4, fileName);
	}

	/**
	 * Generate a PNG file representing the given chunk. The resulting file
	 * will be saved to the game's directory inside the debug folder.
	 * @param chunk the chunk to generate an image representation of
	 * @param imageTileSize the desired size, in pixels, of each tile in the resulting image
	 * @param fileName the name of the file -- if an empty string is provided, the chunk's ID will be used
	 */
	public static void generateChunkImage(Chunk chunk, int imageTileSize, String fileName) {
		// Get the tiles for convenience
		Tile[][] tiles = chunk.tiles();
		// Calculate image dimensions
		int imageSize = tiles.length * imageTileSize;
		// Create image canvas and get a Graphics2D to draw with
		BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		// Draw all tiles
		for (int r = 0; r < tiles.length; r++) {
			// calculate row pixel position
			int y = r * imageTileSize;
			for (int c = 0; c < tiles[r].length; c++) {
				// calculate col pixel position
				int x = c * imageTileSize;
				// determine the color
				Color tileColor = getTileColor(tiles[r][c]);
				g.setColor(tileColor);
				// draw the tile
				g.fillRect(x, y, imageTileSize, imageTileSize);
			}
		}
		// Save result to file
		String fileNameFinal = (fileName.isEmpty() ? chunk.id() : fileName) + ".png";
		String filePath = FileUtil.generateFilePath("debug") + fileNameFinal;
		File outputFile = new File(filePath);
		if (!outputFile.exists()){
			if (outputFile.mkdirs()) {
				try {
					ImageIO.write(image, "PNG", outputFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else Logger.logError("Could not create Chunk Image file!");
		}


	}

	private static Color getTileColor(Tile tile) {
		if (tile.contents() != -1) return Color.RED;
		else {
			if (MathUtil.inRange(tile.type(), 0, 7)) return Color.GREEN;
			else if (MathUtil.inRange(tile.type(), 8, 15)) return new Color(175, 90, 33);
			else if (MathUtil.inRange(tile.type(), 16, 23)) return new Color(137, 114, 112);
			else return Color.BLACK;
		}
	}

}