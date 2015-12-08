package com.github.mdjc.common;

public class Bytes {
	public static byte getByte(int number, int octetPosition) {
		int shiftedBits = octetPosition * 8;
		int octet = number & (0xFF << shiftedBits);
		return (byte) (octet >> shiftedBits);
	}

	public static int asInt(byte b) {
		return b & 0xFF;
	}
}
