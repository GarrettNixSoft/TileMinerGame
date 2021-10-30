package com.floober.engine.util.performance;

public class MethodProfiler {

	public static long testRuntimeMilliseconds(TestMethod method) {

		long start = System.nanoTime();

		method.run();

		return (System.nanoTime() - start) / 1_000_000;

	}

}