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
	public static final int IP_PACKET_SRC_IP_POSITION = 0;
	public static final int IP_PACKET_DEST_IP_POSITION = IP_PACKET_SRC_IP_POSITION + IP_ADDRESSS_BYTE_COUNT;
	public static final int IP_PACKET_PAYLOAD_POSITION = IP_PACKET_DEST_IP_POSITION * 2;

	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkInterface.class);
	private static final byte ARP_REQUEST = 1;
	private static final byte ARP_REPLY = 2;
	private static final byte ARP_PACKET_TYPE_BYTE_COUNT = 1;
	private static final byte ARP_PACKET_TYPE_POSITION = 0;
	private static final byte ARP_PACKET_IP_POSITION = 1;
	private static final int ARP_PACKET_LENGTH = ARP_PACKET_TYPE_BYTE_COUNT + IP_ADDRESSS_BYTE_COUNT;

	private final NetworkCard networkCard;
	private Consumer<byte[]> payloadConsumer;
	private final Map<Integer, Long> arpTable;
	private int ipAddress;
	private byte slashNetMask;
	private int defaultGateway;

	public NetworkInterface(Consumer<byte[]> payloadConsumer) {
		networkCard = new NetworkCard();
		networkCard.setFrameConsumer(this::receive);
		this.payloadConsumer = payloadConsumer;
		arpTable = new Hashtable<>();
	}

	public String getIpAddress() {
		return IpUtils.convertToString(ipAddress);
	}

	public void setIpAddress(String ipAddress, byte slashNetMask) {
		Args.validate(isValidMask(slashNetMask));
		this.slashNetMask = slashNetMask;
		this.ipAddress = IpUtils.convertToInt(ipAddress);
	}

	public String getDefaultGateway() {
		return IpUtils.convertToString(defaultGateway);
	}

	public void setDefaultGateway(String ipAddress) {
		this.defaultGateway = IpUtils.convertToInt(ipAddress);
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
		LOGGER.info("Sending from: {} to: {}", IpUtils.convertToString(ipAddress), destinationIpAddress);
		Args.validateNull(destinationIpAddress, payload);
		int destinationIp = IpUtils.convertToInt(destinationIpAddress);

		if (destinationIp == ipAddress) {
			return;
		}

		if (isOnSameNetwork(destinationIp)) {
			networkCard.sendIpv4Frame(getDestinationMacAddress(destinationIp), payload);
			return;
		}

		if (defaultGateway == 0) {
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

	private boolean isOnSameNetwork(int destinationIpAddress) {
		NetworkAddress destinationNetwAddress = IpUtils.getNetworkAddress(destinationIpAddress, slashNetMask);
		NetworkAddress sourceNetwAddress = IpUtils.getNetworkAddress(ipAddress, slashNetMask);
		return destinationNetwAddress.equals(sourceNetwAddress);
	}

	private Long getDestinationMacAddress(int destinationIpAddress) {
		Long destinationMacAddress = arpTable.get(destinationIpAddress);

		if (destinationMacAddress == null) {
			networkCard.sendArpFrame(NetworkCard.BROADCAST_MACADDRESS, buildArpRequest(destinationIpAddress));
			destinationMacAddress = arpTable.get(destinationIpAddress);
		}

		RuntimeExceptions.throwWhen(destinationMacAddress == null, "Unknown macAddress");
		return destinationMacAddress;
	}

	private byte[] buildArpRequest(int destinationIpAddress) {
		byte[] payload = new byte[ARP_PACKET_LENGTH];
		payload[ARP_PACKET_TYPE_POSITION] = ARP_REQUEST;
		IpUtils.setIp(destinationIpAddress, payload, ARP_PACKET_IP_POSITION);
		return payload;
	}

	private byte[] buildIpPayload(String destinationIpAddress, byte[] payload) {
		LOGGER.info("Building ip payload [from: {}, to: {}, payload{}]", ipAddress, destinationIpAddress, payload);
		int frameLenth = IP_ADDRESSS_BYTE_COUNT * 2 + payload.length;
		byte ipPayload[] = new byte[frameLenth];
		IpUtils.setIp(ipAddress, ipPayload, IP_PACKET_SRC_IP_POSITION);
		IpUtils.setIp(destinationIpAddress, ipPayload, IP_PACKET_DEST_IP_POSITION);
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
		if (ipAddress == 0) {
			return;
		}

		String requestedIp = IpUtils.getIp(frame.getPayload(), ARP_PACKET_IP_POSITION);

		if (ipAddress != IpUtils.convertToInt(requestedIp)) {
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
		String ip = IpUtils.getIp(payload, ARP_PACKET_IP_POSITION);
		arpTable.put(IpUtils.convertToInt(ip), frame.getSourceMacAddress());
	}
}