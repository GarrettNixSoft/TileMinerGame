package com.floober.miner.tilemap;

public record Tile(byte type, byte contents) {

	public static final int SIZE = 64;
	public static final int BLOCKED = 8;

	public boolean isBlocked() {
		return type >= BLOCKED;
	}

	@Override
	public String toString() {
		return "Tile[type=" + type + ", contents=" + contents + "]";
	}
}