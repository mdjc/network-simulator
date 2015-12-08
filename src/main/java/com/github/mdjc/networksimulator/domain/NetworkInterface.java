package com.github.mdjc.networksimulator.domain;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mdjc.common.Args;
import com.github.mdjc.common.RuntimeExceptions;
import com.github.mdjc.networksimulator.common.IpUtils;

public class NetworkInterface {
	public static final byte IP_ADDRESSS_BYTE_COUNT = 4;
	public static final int IP_PACKET_SOURCE_IP_POSITION = 0;
	public static final int IP_PACKET_DESTINATION_IP_POSITION = IP_PACKET_SOURCE_IP_POSITION + IP_ADDRESSS_BYTE_COUNT;
	public static final int IP_PACKET_PAYLOAD_POSITION = IP_PACKET_DESTINATION_IP_POSITION * 2;

	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkInterface.class);
	private static final byte ARP_REQUEST = 1;
	private static final byte ARP_REPLY = 2;
	private static final byte ARP_PACKET_TYPE_BYTE_COUNT = 1;
	private static final byte ARP_PACKET_TYPE_POSITION = 0;
	private static final byte ARP_PACKET_IP_POSITION = 1;
	private static final int ARP_PACKET_LENGTH = ARP_PACKET_TYPE_BYTE_COUNT + IP_ADDRESSS_BYTE_COUNT;

	private final NetworkCard networkCard;
	private Consumer<byte[]> payloadConsumer;
	private final Map<String, Long> arpTable;
	private String ipAddress;
	private byte slashNetMask;
	private String defaultGateway;

	public NetworkInterface(Consumer<byte[]> payloadConsumer) {
		networkCard = new NetworkCard();
		networkCard.setFrameConsumer(this::receive);
		this.payloadConsumer = payloadConsumer;
		arpTable = new Hashtable<>();
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress, byte slashNetMask) {
		Args.validate(isValidMask(slashNetMask));

		this.slashNetMask = slashNetMask;
		this.ipAddress = ipAddress;
	}

	public String getDefaultGateway() {
		return defaultGateway;
	}

	public void setDefaultGateway(String ipAddress) {
		this.defaultGateway = ipAddress;
	}

	public boolean isConnectedTo(Cable cable) {
		return networkCard.isConnectedTo(cable);
	}

	public void connectTo(Cable cable) {
		Args.validateNull(cable);
		this.networkCard.connectTo(cable);
	}

	public void disconnectFrom(Cable cable) {
		Args.validateNull(cable);
		this.networkCard.disconnectFrom(cable);
	}

	public boolean isFree() {
		return this.networkCard.isFree();
	}

	public void send(String destinationIpAddress, byte[] payload) {
		Args.validateNull(destinationIpAddress, payload);

		if (destinationIpAddress.equals(this.ipAddress)) {
			return;
		}

		if (isOnSameNetwork(destinationIpAddress)) {
			networkCard.sendIpv4Frame(getDestinationMacAddress(destinationIpAddress), payload);
			return;
		}

		if (defaultGateway == null) {
			LOGGER.info("unknown default gateway");
			return;
		}

		byte[] ipPayload = buildIpPayload(destinationIpAddress, payload);
		networkCard.sendIpv4Frame(getDestinationMacAddress(defaultGateway), ipPayload);
	}

	private boolean isValidMask(byte slashNetMask) {
		return slashNetMask < NetworkAddress.MIN_NETWORK_MASK_VALUE
				|| slashNetMask > NetworkAddress.MAX_NETWORK_MASK_VALUE;
	}

	private boolean isOnSameNetwork(String destinationIpAddress) {
		int destinationNetwork = IpUtils.getNetworkIpAddress(destinationIpAddress, slashNetMask);
		int sourceNetwork = IpUtils.getNetworkIpAddress(ipAddress, slashNetMask);
		return sourceNetwork == destinationNetwork;
	}

	private Long getDestinationMacAddress(String destinationIpAddress) {
		Long destinationMacAddress = arpTable.get(destinationIpAddress);

		if (destinationMacAddress == null) {
			networkCard.sendArpFrame(NetworkCard.BROADCAST_MACADDRESS, buildArpRequest(destinationIpAddress));
			destinationMacAddress = arpTable.get(destinationIpAddress);
		}

		RuntimeExceptions.throwWhen(destinationMacAddress == null, "Unknown macAddress");
		return destinationMacAddress;
	}

	private byte[] buildArpRequest(String destinationIpAddress) {
		byte[] payload = new byte[ARP_PACKET_LENGTH];
		payload[ARP_PACKET_TYPE_POSITION] = ARP_REQUEST;

		String[] tokens = destinationIpAddress.split("\\.");

		for (int i = 1, index = 0; i <= IP_ADDRESSS_BYTE_COUNT; i++, index++) {
			payload[i] = Integer.valueOf(tokens[index]).byteValue();
		}

		return payload;
	}

	private byte[] buildIpPayload(String destinationIpAddress, byte[] payload) {
		LOGGER.info("Building ip payload [from: {}, to: {}, payload{}]", ipAddress, destinationIpAddress, payload);
		int frameLenth = IP_ADDRESSS_BYTE_COUNT * 2 + payload.length;
		byte ipPayload[] = new byte[frameLenth];
		IpUtils.setIp(ipAddress, ipPayload, IP_PACKET_SOURCE_IP_POSITION);
		IpUtils.setIp(destinationIpAddress, ipPayload, IP_PACKET_DESTINATION_IP_POSITION);
		System.arraycopy(payload, 0, ipPayload, IP_PACKET_PAYLOAD_POSITION, payload.length);
		return ipPayload;
	}

	private void receive(Frame frame) {
		if (frame.getEtherType() == EtherType.ARP) {
			receiveArpFrame(frame);
			return;
		}

		payloadConsumer.accept(frame.getPayload());
	}

	private void receiveArpFrame(Frame frame) {
		byte requestType = frame.getPayload()[ARP_PACKET_TYPE_POSITION];

		switch (requestType) {
		case ARP_REQUEST:
			receiveArpRequest(frame);
			break;
		case ARP_REPLY:
			receiveArpReply(frame);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void receiveArpRequest(Frame frame) {
		if (ipAddress == null) {
			return;
		}

		String requestedIp = IpUtils.getIpAsString(frame.getPayload(), ARP_PACKET_IP_POSITION);

		if (!ipAddress.equals(requestedIp)) {
			return;
		}

		LOGGER.info("processing ARP request packet...");
		networkCard.sendArpFrame(frame.getSourceMacAddress(), buildArpReply());
	}

	private byte[] buildArpReply() {
		byte[] payload = new byte[ARP_PACKET_LENGTH];
		payload[ARP_PACKET_TYPE_POSITION] = ARP_REPLY;
		IpUtils.setIp(ipAddress, payload, ARP_PACKET_IP_POSITION);
		return payload;
	}

	private void receiveArpReply(Frame frame) {
		LOGGER.info("processing ARP reply");
		byte[] payload = frame.getPayload();
		String ip = IpUtils.getIpAsString(payload, ARP_PACKET_IP_POSITION);
		arpTable.put(ip, frame.getSourceMacAddress());
	}
}