package com.github.mdjc.networksimulator.common;

import com.github.mdjc.common.Args;
import com.github.mdjc.common.Bytes;
import com.github.mdjc.networksimulator.domain.NetworkAddress;
import com.github.mdjc.networksimulator.domain.NetworkInterface;

public class IpUtils {
	public static void setIp(String ip, byte[] payload, int indexFrom) {
		String[] tokens = ip.split("\\.");
		int length = indexFrom + NetworkInterface.IP_ADDRESSS_BYTE_COUNT;

		for (int i = indexFrom, index = 0; i < length; i++, index++) {
			payload[i] = Integer.valueOf(tokens[index]).byteValue();
		}
	}

	public static int getNetworkIpAddress(String ip, byte slashMask) {
		int ipInt = 0;
		String[] tokens = ip.split("\\.");

		for (int i = tokens.length - 1, octetPos = 0; i >= 0; i--, octetPos++) {
			ipInt |= Integer.valueOf(tokens[i]).intValue() << octetPos * 8;
		}

		return ipInt & getMaskAsInt(slashMask);
	}

	public static int getMaskAsInt(byte slashMask) {
		int bitsTotal = NetworkInterface.IP_ADDRESSS_BYTE_COUNT * 8;
		return ((1 << slashMask) - 1) << (bitsTotal - slashMask);
	}

	public static String getIpAsString(byte[] payload, int indexFrom) {
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

	public static NetworkAddress getNetworkAddress(String destinationIpAddress, byte mask) {
		int ntwAddresInt = getNetworkIpAddress(destinationIpAddress, mask);
		StringBuilder ipBuilder = new StringBuilder();

		for (int octetPos = NetworkInterface.IP_ADDRESSS_BYTE_COUNT - 1; octetPos >= 0; octetPos--) {
			byte b = Bytes.getByte(ntwAddresInt, octetPos);
			ipBuilder.append(Bytes.asInt(b));

			if (octetPos != 0) {
				ipBuilder.append(".");
			}
		}

		return new NetworkAddress(ipBuilder.toString(), mask);
	}
}