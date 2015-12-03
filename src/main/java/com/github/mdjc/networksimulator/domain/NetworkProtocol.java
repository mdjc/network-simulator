package com.github.mdjc.networksimulator.domain;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkProtocol {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkProtocol.class);
	private static final byte ARP_REQUEST = 1;
	private static final byte ARP_REPLY = 2;
	private static final byte IP_ADDRESSS_BYTE_COUNT = 4;
	private static final byte ARP_PACKET_TYPE_BYTE_COUNT = 1;
	private static final byte ARP_PACKET_TYPE_POSITION = 0;
	private static final byte ARP_PACKET_IP_POSITION = 1;
	private static final int ARP_PACKET_LENGTH = ARP_PACKET_TYPE_BYTE_COUNT + IP_ADDRESSS_BYTE_COUNT;

	private final NetworkCard networkCard;
	private String ipAddress;
	private Consumer<byte[]> payloadConsumer;
	private final Map<String, Long> arpTable;

	public NetworkProtocol(NetworkCard networkCard, Consumer<byte[]> payloadConsumer) {
		this.networkCard = networkCard;
		networkCard.setFrameConsumer(this::receive);
		this.payloadConsumer = payloadConsumer;
		arpTable = new Hashtable<>();
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void send(String destinationIpAdress, byte[] payload) {
		if (destinationIpAdress.equals(this.ipAddress)) {
			return;
		}

		Long destinationMacAddress = arpTable.get(destinationIpAdress);

		if (destinationMacAddress == null) {
			sendArpRequest(destinationIpAdress);
			destinationMacAddress = arpTable.get(destinationIpAdress);
		}

		if (destinationMacAddress == null) {
			throw new RuntimeException("Unknown macAddress");
		}

		networkCard.sendIpv4Frame(destinationMacAddress, payload);
	}

	public void receive(Frame frame) {
		if (frame.getEtherType() == EtherType.ARP) {
			receiveArpFrame(frame);
			return;
		}

		payloadConsumer.accept(frame.getPayload());
	}

	private void sendArpRequest(String destinationIpAddress) {
		networkCard.sendArpFrame(NetworkCard.BROADCAST_MACADDRESS, buildArpRequest(destinationIpAddress));
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

		String requestedIp = getIpAsString(frame.getPayload());

		if (!ipAddress.equals(requestedIp)) {
			return;
		}

		LOGGER.info("processing ARP request packet...");
		byte[] payload = buildArpReply();
		networkCard.sendArpFrame(frame.getSourceMacAddress(), payload);
	}

	private static String getIpAsString(byte[] payload) {
		StringBuilder sb = new StringBuilder();
		int indexFrom = ARP_PACKET_IP_POSITION;
		int length = indexFrom + IP_ADDRESSS_BYTE_COUNT;

		for (int i = indexFrom; i < length; i++) {
			sb.append(payload[i] & 255);

			if (i != length - 1) {
				sb.append(".");
			}
		}

		return sb.toString();
	}

	private byte[] buildArpReply() {
		byte[] payload = new byte[ARP_PACKET_LENGTH];
		payload[ARP_PACKET_TYPE_POSITION] = ARP_REPLY;
		setIp(ipAddress, payload);
		return payload;
	}

	private static void setIp(String ip, byte[] payload) {
		String[] tokens = ip.split("\\.");
		int indexFrom = ARP_PACKET_IP_POSITION;
		int length = indexFrom + IP_ADDRESSS_BYTE_COUNT;

		for (int i = indexFrom, index = 0; i < length; i++, index++) {
			payload[i] = Integer.valueOf(tokens[index]).byteValue();
		}
	}

	private void receiveArpReply(Frame frame) {
		LOGGER.info("processing ARP reply");
		byte[] payload = frame.getPayload();
		String ip = getIpAsString(payload);
		arpTable.put(ip, frame.getSourceMacAddress());
	}
}