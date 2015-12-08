package com.github.mdjc.networksimulator.domain;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Switch extends Hub {
	private static final Logger LOGGER = LoggerFactory.getLogger(Switch.class);
	private final Map<Long, Byte> macAddressIdxMap;

	public Switch(byte jackCount) {
		super(jackCount);
		macAddressIdxMap = new HashMap<>();
	}

	@Override
	protected void receive(byte index, Frame frame) {
		LOGGER.info(String.format("Receiving frame: %s", frame));
		macAddressIdxMap.put(frame.getSourceMacAddress(), index);
		Byte destinationIdx = macAddressIdxMap.get(frame.getDestinationMacAddress());

		if (destinationIdx == null) {
			super.receive(index, frame);
			return;
		}

		LOGGER.info("forwarding to memorized jack");
		jacks[destinationIdx].send(frame);
	}
}