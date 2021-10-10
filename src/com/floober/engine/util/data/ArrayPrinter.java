package com.floober.engine.util.data;

public class ArrayPrinter {

	public static void print(Object[][] array) {
		for (Object[] row : array) {
			System.out.print("[");
			for (int i = 0; i < row.length; i++) {
				System.out.print(row[i] + (i < row.length - 1 ? "," : ""));
			}
			System.out.println("]");
		}
	}

}