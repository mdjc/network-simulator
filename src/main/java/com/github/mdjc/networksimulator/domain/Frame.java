package com.github.mdjc.networksimulator.domain;

import java.util.Arrays;

public class Frame {
	private final EtherType etherType;
	private final long sourceMacAddress;
	private final long destinationMacAddress;
	private final byte[] payload;

	public Frame(EtherType etherType, long sourceMacAddress, long destinationMacAddress, byte[] payload) {
		this.etherType = etherType;
		this.sourceMacAddress = sourceMacAddress;
		this.destinationMacAddress = destinationMacAddress;
		this.payload = payload;
	}

	public EtherType getEtherType() {
		return etherType;
	}

	public long getSourceMacAddress() {
		return sourceMacAddress;
	}

	public long getDestinationMacAddress() {
		return destinationMacAddress;
	}

	public byte[] getPayload() {
		return payload;
	}

	@Override
	public String toString() {
		return String.format("{etherType: %s, sourceMacAddress: %d, destinationMacAddress: %d, payload: %s}",
				etherType,
				sourceMacAddress,
				destinationMacAddress,
				Arrays.toString(payload));
	}
}