package com.github.mdjc.networksimulator.domain;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Computer {
	private static final Logger LOGGER = LoggerFactory.getLogger(Computer.class);

	private final NetworkCard networkCard;
	private final NetworkProtocol networkProtocol;

	public Computer() {
		networkCard = new NetworkCard();
		networkProtocol = new NetworkProtocol(networkCard, this::receive);
	}

	public String getIpAddress() {
		return networkProtocol.getIpAddress();
	}

	public void setIpAddress(String ipAddress) {
		networkProtocol.setIpAddress(ipAddress);
	}

	public void connectTo(Cable cable) {
		networkCard.connectTo(cable);
	}

	public void disconnectFrom(Cable cable) {
		networkCard.disconnectFrom(cable);
	}

	public void send(String destinationIpAddress, byte[] payload) {
		networkProtocol.send(destinationIpAddress, payload);
	}

	@Override
	public String toString() {
		return String.format("networkCard: %s", this.networkCard);
	}

	private void receive(byte[] payload) {
		LOGGER.info("Receiving from sourceIp: {}, payload: {} ", getIpAddress(), Arrays.toString(payload));
	}
}