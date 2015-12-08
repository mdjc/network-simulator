package com.github.mdjc.networksimulator.domain;

import com.github.mdjc.common.Args;
import com.github.mdjc.common.RuntimeExceptions;

public class Cable {
	public static class Plug {
		private final Cable cable;
		private Jack jack;

		private Plug(Cable cable) {
			Args.validateNull(cable);
			this.cable = cable;
		}

		public void connect(Jack jack) {
			if (isConnectedTo(jack)) {
				return;
			}

			RuntimeExceptions.throwWhen(!isFree(), "Plug not free");
			this.jack = jack;
		}

		public void disconnect() {
			if (jack == null) {
				return;
			}

			jack = null;
		}

		public boolean isConnectedTo(Jack jack) {
			return this.jack == jack;
		}

		public void send(Frame frame) {
			cable.other(this).receive(frame);
		}

		public void receive(Frame frame) {
			if (isFree()) {
				return;
			}

			jack.receive(frame);
		}

		public boolean isFree() {
			return jack == null;
		}
	}

	private final Plug plug1;
	private final Plug plug2;

	public Cable() {
		this.plug1 = new Plug(this);
		this.plug2 = new Plug(this);
	}

	public boolean isConnectedTo(Jack jack) {
		return plug1.isConnectedTo(jack) || plug2.isConnectedTo(jack);
	}

	public void connectTo(Jack jack) {
		Args.validateNull(jack);
		RuntimeExceptions.throwWhen(getFreePlug() == null, "cable not free");
		jack.connect(getFreePlug());
		getFreePlug().connect(jack);
	}

	public void disconnectFrom(Jack jack) {
		Args.validate(!isConnectedTo(jack));
		jack.disconnect();

		if (plug1.isConnectedTo(jack)) {
			plug1.disconnect();
			return;
		}

		plug2.disconnect();
	}

	public void free() {
		free(plug1);
		free(plug2);
	}

	private Plug other(Plug plug) {
		Args.validate(!isValid(plug));

		if (plug2 == plug) {
			return plug1;
		}

		return plug2;
	}

	private Plug getFreePlug() {
		if (plug1.isFree()) {
			return plug1;
		}

		if (plug2.isFree()) {
			return plug2;
		}

		return null;
	}

	private boolean isValid(Plug plug) {
		return plug1 == plug || plug2 == plug;
	}

	private static void free(Plug plug) {
		if (!plug.isFree()) {
			plug.jack.disconnect();
			plug.disconnect();
		}
	}
}