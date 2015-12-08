package com.github.mdjc.networksimulator.domain;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Computer {
	private static final Logger LOGGER = LoggerFactory.getLogger(Computer.class);

	private final NetworkInterface networkInterface;

	public Computer() {
		networkInterface = new NetworkInterface(this::receive);
	}

	public String getIpAddress() {
		return networkInterface.getIpAddress();
	}

	public void setIpAddress(String ipAddress, byte slashNetMask) {
		networkInterface.setIpAddress(ipAddress, slashNetMask);
	}

	public String getDefaultGateway() {
		return networkInterface.getDefaultGateway();
	}

	public void setDefaultGateway(String ipAddress) {
		networkInterface.setDefaultGateway(ipAddress);
	}

	public void connectTo(Cable cable) {
		networkInterface.connectTo(cable);
	}

	public void disconnectFrom(Cable cable) {
		networkInterface.disconnectFrom(cable);
	}

	public void send(String destinationIpAddress, byte[] payload) {
		networkInterface.send(destinationIpAddress, payload);
	}

	private void receive(byte[] payload) {
		LOGGER.info("Receiving in {}, payload: {} ", getIpAddress(), Arrays.toString(payload));
	}
}