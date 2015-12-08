package com.github.mdjc.common;

import java.util.function.BooleanSupplier;

public class RuntimeExceptions {
	public static void throwWhen(boolean evaluation, String msg) {
		if (evaluation) {
			throw new RuntimeException(msg);
		}
	}

	public static void throwWhen(BooleanSupplier supplier, String msg) {
		throwWhen(supplier.getAsBoolean(), msg);
	}
}
