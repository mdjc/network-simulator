package com.github.mdjc.networksimulator.domain;

public enum EtherType {
	ARP("0X0806"), IPV4("0X0808");

	private final String value;

	private EtherType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}