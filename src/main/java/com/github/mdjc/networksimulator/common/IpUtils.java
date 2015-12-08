package com.github.mdjc.networksimulator.common;

import com.github.mdjc.common.Args;
import com.github.mdjc.common.Bytes;
import com.github.mdjc.networksimulator.domain.NetworkAddress;
import com.github.mdjc.networksimulator.domain.NetworkInterface;

public class IpUtils {
	public static void setIp(int ip, byte[] payload, int indexFrom) {
		int length = indexFrom + NetworkInterface.IP_ADDRESSS_BYTE_COUNT;

		for (int i = indexFrom, octetPos = NetworkInterface.IP_ADDRESSS_BYTE_COUNT - 1; i < length; i++, octetPos--) {
			payload[i] = Bytes.getByte(ip, octetPos);
		}
	}

	public static void setIp(String destinationIpAddress, byte[] payload, int indexFrom) {
		setIp(convertToInt(destinationIpAddress), payload, indexFrom);
	}

	public static String getIp(byte[] payload, int indexFrom) {
		Args.validateNull(payload);
		StringBuilder sb = new StringBuilder();
		int length = indexFrom + NetworkInterface.IP_ADDRESSS_BYTE_COUNT;

		for (int i = indexFrom; i < length; i++) {
			sb.append(Bytes.asInt(payload[i]));

			if (i != length - 1) {
				sb.append(".");
			}
		}

		return sb.toString();
	}

	public static NetworkAddress getNetworkAddress(int ipAddress, byte mask) {
		int ntwAddres = getNetworkIpAddress(ipAddress, mask);
		return new NetworkAddress(ntwAddres, mask);
	}

	public static NetworkAddress getNetworkAddress(String ipAddress, byte mask) {
		return getNetworkAddress(convertToInt(ipAddress), mask);
	}

	public static int getMaskAsInt(byte slashMask) {
		int bitsTotal = NetworkInterface.IP_ADDRESSS_BYTE_COUNT * 8;
		return ((1 << slashMask) - 1) << (bitsTotal - slashMask);
	}

	public static int convertToInt(String ipAddress) {
		int ipInt = 0;
		String[] tokens = ipAddress.split("\\.");

		for (int i = tokens.length - 1, octetPos = 0; i >= 0; i--, octetPos++) {
			ipInt |= Integer.valueOf(tokens[i]).intValue() << octetPos * 8;
		}
		return ipInt;
	}

	public static String convertToString(int ipAddress) {
		StringBuilder ipBuilder = new StringBuilder();

		for (int octetPos = NetworkInterface.IP_ADDRESSS_BYTE_COUNT - 1; octetPos >= 0; octetPos--) {
			byte b = Bytes.getByte(ipAddress, octetPos);
			ipBuilder.append(Bytes.asInt(b));

			if (octetPos != 0) {
				ipBuilder.append(".");
			}
		}

		return ipBuilder.toString();
	}

	private static int getNetworkIpAddress(int ipAddress, byte slashMask) {
		return ipAddress & getMaskAsInt(slashMask);
	}
}