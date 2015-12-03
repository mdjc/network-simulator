package com.github.mdjc.networksimulator.domain;

import java.util.function.Consumer;

import com.github.mdjc.networksimulator.domain.Cable.Plug;

public class Jack {
	private final Consumer<Frame> frameConsumer;
	private Plug plug;

	public Jack(Consumer<Frame> frameConsumer) {
		this.frameConsumer = frameConsumer;
	}

	public void connect(Plug plug) {
		if (!isFree()) {
			throw new RuntimeException("Jack not free");
		}

		this.plug = plug;
	}

	public void disconnect() {
		this.plug = null;
	}

	public boolean isFree() {
		return plug == null;
	}

	public void send(Frame frame) {
		if (isFree()) {
			return;
		}

		plug.send(frame);
	}

	public void receive(Frame frame) {
		frameConsumer.accept(frame);
	}
}
