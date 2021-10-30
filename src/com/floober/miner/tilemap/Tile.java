package com.floober.miner.tilemap;

public record Tile(byte type, byte contents) {

	public static final int SIZE = 64;

	public boolean isBlocked() {
		return type >= 0;
	}

	public int getHardness() {
		return Math.max(type / 8, 1);
	}

	@Override
	public String toString() {
		return "Tile[type=" + type + ", contents=" + contents + "]";
	}
}