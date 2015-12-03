package com.github.mdjc.networksimulator.domain;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkCard {
	public static final long BROADCAST_MACADDRESS = 0xFFFFFFFFFFFFl;

	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkCard.class);

	private static long macAddressSequence;
	private final long macAddress;
	private final Jack jack;
	private Consumer<Frame> frameConsumer;

	public NetworkCard() {
		this.macAddress = ++macAddressSequence;
		this.jack = new Jack(this::receive);
	}

	public void connectTo(Cable cable) {
		cable.connectTo(jack);
	}

	public void disconnectFrom(Cable cable) {
		cable.disconnectFrom(jack);
	}

	public void setFrameConsumer(Consumer<Frame> frameConsumer) {
		this.frameConsumer = frameConsumer;
	}

	public void sendIpv4Frame(long destinationMacAddress, byte[] payload) {
		if (destinationMacAddress == macAddress) {
			return;
		}

		Frame frame = new Frame(EtherType.IPV4, this.macAddress, destinationMacAddress, payload);
		send(frame);
	}

	public void sendArpFrame(long destinationMacAddress, byte[] payload) {
		Frame frame = new Frame(EtherType.ARP, this.macAddress, destinationMacAddress, payload);
		send(frame);
	}

	private void receive(Frame frame) {
		if (frame.getDestinationMacAddress() != macAddress
				&& !isBroadcast(frame.getDestinationMacAddress())) {
			LOGGER.info(
					String.format("Ignoring frame at macAddress: %d, actual destination is macAddress: %d",
							macAddress,
							frame.getDestinationMacAddress()));
			return;
		}

		if (frameConsumer == null) {
			throw new RuntimeException("unknown consumer");
		}

		LOGGER.info(String.format("Receiving frame at macAddress: %s, frame is %s",
				macAddress,
				frame));
		frameConsumer.accept(frame);
	}

	@Override
	public String toString() {
		return String.valueOf(this.macAddress);
	}

	private boolean isBroadcast(long destinationMacAddress) {
		return destinationMacAddress == BROADCAST_MACADDRESS;
	}

	private void send(Frame frame) {
		LOGGER.info("Sending frame {}", frame);
		jack.send(frame);
	}
}