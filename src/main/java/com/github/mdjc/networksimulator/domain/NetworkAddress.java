package com.github.mdjc.networksimulator.domain;

import com.github.mdjc.networksimulator.common.IpUtils;

public class NetworkAddress implements Comparable<NetworkAddress> {
	public static final int MIN_NETWORK_MASK_VALUE = 8;
	public static final int MAX_NETWORK_MASK_VALUE = 30;

	private final int ipAddress;
	private final byte slashMask;

	public NetworkAddress(int ipAddress, byte slashMask) {
		this.ipAddress = ipAddress;
		this.slashMask = slashMask;
	}

	public NetworkAddress(String ipAddress, byte slashMask) {
		this(IpUtils.convertToInt(ipAddress), slashMask);
	}

	public int getIpAddress() {
		return ipAddress;
	}

	public byte getSlashMask() {
		return slashMask;
	}

	@Override
	public int compareTo(NetworkAddress o) {
		int ipComparison = Integer.compare(this.ipAddress, o.ipAddress);

		if (ipComparison == 0) {
			return Byte.compare(this.slashMask, o.slashMask);
		}

		return ipComparison;
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
		int ipAddressHash = Integer.hashCode(ipAddress);
		int slashMashHash = Byte.hashCode(slashMask);
		return 31 * ipAddressHash + slashMashHash;
	}
}