package com.github.mdjc.networksimulator.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mdjc.common.Args;
import com.github.mdjc.common.RuntimeExceptions;

public class Router {
	private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);
	private final NetworkInterface[] interfaces;
	private final Map<NetworkAddress, Byte> routingTable;

	public Router(int interfaceCount) {
		interfaces = new NetworkInterface[interfaceCount];
		this.routingTable = new HashMap<>();

		for (int i = 0; i < interfaceCount; i++) {
			interfaces[i] = new NetworkInterface(this::receive);
		}

	}

	public void setIpAddress(String ipAddress, byte mask, byte interfaceIndex) {
		Args.validateNull(ipAddress);
		Args.validate(isValidIndex(interfaceIndex));
		NetworkInterface netwInterface = interfaces[interfaceIndex];
		netwInterface.setIpAddress(ipAddress, mask);
	}

	public void connnectTo(Cable cable, byte interfaceIndex) {
		Args.validateNull(cable);
		Args.validate(isValidIndex(interfaceIndex));
		NetworkInterface netwInterface = interfaces[interfaceIndex];
		RuntimeExceptions.throwWhen(!netwInterface.isFree(), "unvailable interface");
		netwInterface.connectTo(cable);
	}

	public void disconnectFrom(Cable cable) {
		Args.validateNull(cable);
		NetworkInterface connectedInterface = Arrays.stream(interfaces)
				.filter(i -> i.isConnectedTo(cable))
				.findFirst()
				.get();

		if (connectedInterface == null) {
			return;
		}

		connectedInterface.disconnectFrom(cable);
	}

	public void setRoute(NetworkAddress networkAddress, byte interfaceIndex) {
		Args.validateNull(networkAddress);
		Args.validate(isValidIndex(interfaceIndex));
		routingTable.put(networkAddress, interfaceIndex);
	}

	@Override
	public String toString() {
		return routingTable.toString();
	}

	private boolean isValidIndex(byte interfaceIndex) {
		return interfaceIndex < 0 || interfaceIndex > interfaces.length;
	}

	private NetworkInterface getSenderInterface(String destinationIpAddress) {
		for (byte mask = NetworkAddress.MIN_NETWORK_MASK_VALUE; mask < NetworkAddress.MAX_NETWORK_MASK_VALUE; mask++) {
			NetworkAddress netwAddress = NetworkInterface.getNetworkAddress(destinationIpAddress, mask);
			Byte index = routingTable.get(netwAddress);

			if (index != null) {
				return interfaces[index];
			}
		}

		return null;
	}

	private void receive(byte[] payload) {
		LOGGER.info("Received payload [{}] ", payload);
		String destinationIpAddress = NetworkInterface.getIpAsString(payload,
				NetworkInterface.IP_PACKET_DESTINATION_IP_POSITION);
		NetworkInterface senderInterface = getSenderInterface(destinationIpAddress);

		if (senderInterface == null) {
			LOGGER.info("avoiding sending message, unknown out Interface");
			return;
		}

		senderInterface.send(destinationIpAddress, payload);
	}
}