package com.github.mdjc.networksimulator.domain;

public class Cable {
	public static class Plug {
		private final Cable cable;
		private Jack jack;

		private Plug(Cable cable) {
			// TODO: as this is a very common code, find or create util to throw iae in when null
			if (cable == null) {
				throw new IllegalArgumentException();
			}

			this.cable = cable;
		}

		public void connect(Jack jack) {
			if (isConnectedTo(jack)) {
				return;
			}

			if (!isFree()) {
				throw new RuntimeException("Plug not free");
			}

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

	public void connectTo(Jack jack) {
		if (jack == null) {
			throw new IllegalArgumentException();
		}

		Plug freePlug = getFreePlug();

		if (freePlug == null) {
			throw new RuntimeException("cable not free");
		}

		jack.connect(freePlug);
		freePlug.connect(jack);
	}

	public void disconnectFrom(Jack jack) {
		if (!isConnectedTo(jack)) {
			throw new IllegalArgumentException();
		}

		jack.disconnect();

		if (plug1.isConnectedTo(jack)) {
			plug1.disconnect();
			return;
		}

		plug2.disconnect();
	}

	public boolean isConnectedTo(Jack jack) {
		return plug1.isConnectedTo(jack) || plug2.isConnectedTo(jack);
	}

	public void free() {
		free(plug1);
		free(plug2);
	}

	private Plug other(Plug plug) {
		if (!isValid(plug)) {
			throw new IllegalArgumentException();
		}

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
