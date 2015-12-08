package com.github.mdjc.networksimulator.domain;

public class NetworkAddress implements Comparable<NetworkAddress> {
	public static final int MIN_NETWORK_MASK_VALUE = 8;
	public static final int MAX_NETWORK_MASK_VALUE = 30;

	private final String ipAddress;
	private final byte slashMask;

	public NetworkAddress(String ipAddress, byte slashMask) {
		this.ipAddress = ipAddress;
		this.slashMask = slashMask;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public byte getSlashMask() {
		return slashMask;
	}

	@Override
	public int compareTo(NetworkAddress o) {
		if (this.ipAddress.compareTo(o.ipAddress) == 0) {
			return Byte.compare(this.slashMask, o.slashMask);
		}

		return this.ipAddress.compareTo(o.ipAddress);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof NetworkAddress)) {
			return false;
		}

		NetworkAddress that = (NetworkAddress) other;

		return this.compareTo(that) == 0;
	}

	@Override
	public int hashCode() {
		int ipAddressHash = ipAddress.hashCode();
		int slashMashHash = Byte.hashCode(slashMask);
		return 31 * ipAddressHash + slashMashHash;
	}
}