package com.floober.engine.util.time;

import com.floober.engine.util.Logger;

public class GameThread {

	public static void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (Exception e) {
			Logger.logError("Sleep error.", Logger.MEDIUM);
		}
	}

}