package com.github.mdjc.common;

import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class Args {
	public static void validate(boolean evaluation) {
		if (evaluation) {
			throw new IllegalArgumentException();
		}
	}

	public static void validate(BooleanSupplier supplier) {
		validate(supplier.getAsBoolean());
	}

	public static void validateNull(Object... args) {
		Arrays.stream(args).forEach(a -> validate(a == null));
	}
}