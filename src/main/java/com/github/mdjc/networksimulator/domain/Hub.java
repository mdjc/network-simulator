package com.github.mdjc.networksimulator.domain;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hub {
	private static final Logger LOGGER = LoggerFactory.getLogger(Hub.class);

	protected final Jack[] jacks;

	public Hub(byte jackCount) {
		jacks = new Jack[jackCount];

		for (byte i = 0; i < jackCount; i++) {
			final byte j = i;
			jacks[i] = new Jack(f -> receive(j, f));
		}
	}

	public void connnectTo(Cable cable) {
		if (cable == null) {
			throw new IllegalArgumentException();
		}

		Jack freeJack = getFreeJack(jacks);

		if (freeJack == null) {
			throw new RuntimeException("unavailable jack");
		}

		cable.connectTo(freeJack);
	}

	public void disconnectFrom(Cable cable) {
		if (cable == null) {
			throw new IllegalArgumentException();
		}

		for (Jack jack : jacks) {
			if (cable.isConnectedTo(jack)) {
				cable.disconnectFrom(jack);
				break;
			}
		}
	}

	protected void receive(byte index, Frame frame) {
		LOGGER.info(String.format("Receiving frame: %s", frame));
		Arrays.stream(jacks)
				.filter(j -> j != jacks[index])
				.forEach(j -> j.send(frame));
	}

	private Jack getFreeJack(Jack[] jacks) {
		return Arrays.stream(jacks).filter(j -> j.isFree()).findFirst().get();
	}
}
