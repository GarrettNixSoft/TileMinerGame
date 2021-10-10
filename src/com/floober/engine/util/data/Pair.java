package com.floober.engine.util.data;

import java.util.Objects;

public record Pair<T1, T2>(T1 data1, T2 data2) {

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(data1, pair.data1) && Objects.equals(data2, pair.data2);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data1, data2);
	}
}