package com.github.mdjc.networksimulator.domain;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mdjc.common.Args;
import com.github.mdjc.common.RuntimeExceptions;

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

	public void setFrameConsumer(Consumer<Frame> frameConsumer) {
		this.frameConsumer = frameConsumer;
	}

	public boolean isConnectedTo(Cable cable) {
		return cable.isConnectedTo(jack);
	}

	public void connectTo(Cable cable) {
		cable.connectTo(jack);
	}

	public void disconnectFrom(Cable cable) {
		cable.disconnectFrom(jack);
	}

	public boolean isFree() {
		return this.jack.isFree();
	}

	public void sendIpv4Frame(long destinationMacAddress, byte[] payload) {
		send(EtherType.IPV4, destinationMacAddress, payload);
	}

	public void sendArpFrame(long destinationMacAddress, byte[] payload) {
		send(EtherType.ARP, destinationMacAddress, payload);
	}

	@Override
	public String toString() {
		return String.valueOf(this.macAddress);
	}

	private void send(EtherType etherType, long destinationMacAddress, byte[] payload) {
		LOGGER.info("Sending frame [{}, {}, {}]", etherType, destinationMacAddress, payload);

		if (destinationMacAddress == macAddress) {
			return;
		}

		Frame frame = new Frame(etherType, this.macAddress, destinationMacAddress, payload);
		jack.send(frame);
	}

	private boolean isBroadcast(long destinationMacAddress) {
		return destinationMacAddress == BROADCAST_MACADDRESS;
	}

	private void receive(Frame frame) {
		Args.validateNull(frame);

		if (frame.getDestinationMacAddress() != macAddress
				&& !isBroadcast(frame.getDestinationMacAddress())) {
			LOGGER.info(
					String.format("Ignoring frame at macAddress: %d, actual destination is macAddress: %d",
							macAddress,
							frame.getDestinationMacAddress()));
			return;
		}

		RuntimeExceptions.throwWhen(frameConsumer == null, "unknown consumer");
		LOGGER.info(String.format("Receiving frame at macAddress: %s, frame is %s",
				macAddress,
				frame));
		frameConsumer.accept(frame);
	}
}