package com.github.mdjc.networksimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.mdjc.networksimulator.domain.Cable;
import com.github.mdjc.networksimulator.domain.Computer;
import com.github.mdjc.networksimulator.domain.Hub;
import com.github.mdjc.networksimulator.domain.NetworkAddress;
import com.github.mdjc.networksimulator.domain.Router;
import com.github.mdjc.networksimulator.domain.Switch;

@Component
public class Simulator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Simulator.class);

	public void run() {
		performHubSimulation();
		LOGGER.info("----------------------------------------------------------------------");
		performSwitchRouterSimulation();
	}

	private void performHubSimulation() {
		LOGGER.info("Hub Simulation");
		Computer[][] computers = { new Computer[2], new Computer[1], new Computer[1], new Computer[1] };

		for (int i = 0; i < computers.length; i++) {
			for (int j = 0; j < computers[i].length; j++) {
				computers[i][j] = new Computer();
			}
		}

		Hub[] hubs = { new Hub((byte) 8), new Hub((byte) 10), new Hub((byte) 9), new Hub((byte) 9), new Hub((byte) 5),
				new Hub((byte) 5) };
		connect(computers[0], hubs[0]);
		connect(computers[1], hubs[1]);
		connect(computers[2], hubs[2]);
		connect(computers[3], hubs[3]);
		connect(hubs[0], hubs[1]);
		connect(hubs[1], hubs[2]);
		connect(hubs[2], hubs[3]);
		connect(hubs[3], hubs[4]);
		connect(hubs[3], hubs[5]);
		computers[0][0].setIpAddress("192.44.200.1", (byte) 24);
		computers[1][0].setIpAddress("192.44.200.5", (byte) 24);
		LOGGER.info("................Sending Hello.....................");
		computers[0][0].send(computers[1][0].getIpAddress(), "Hello".getBytes());
	}

	private void performSwitchRouterSimulation() {
		LOGGER.info("Switch / Router Simulation");
		Router router = new Router(6);
		Cable[] cables = { new Cable(), new Cable() };
		Switch switch1 = new Switch((byte) 4);
		Computer[] switch1Computers = { new Computer(), new Computer() };
		byte mask = (byte) 24;
		switch1Computers[0].setIpAddress("192.50.200.2", mask);
		switch1Computers[1].setIpAddress("192.50.200.3", mask);
		switch1.connnectTo(cables[0]);
		connect(switch1Computers, switch1);
		router.connnectTo(cables[0], (byte) 0);

		Switch switch2 = new Switch((byte) 4);
		Computer[] switch2Computers = { new Computer(), new Computer(), new Computer() };
		switch2Computers[0].setIpAddress("192.50.201.2", mask);
		switch2Computers[1].setIpAddress("192.50.201.3", mask);
		switch2Computers[2].setIpAddress("192.50.201.4", mask);
		connect(switch2Computers, switch2);
		switch2.connnectTo(cables[1]);
		router.connnectTo(cables[1], (byte) 1);

		router.setIpAddress("192.50.200.1", mask, (byte) 0);
		router.setRoute(new NetworkAddress("192.50.200.0", mask), (byte) 0);
		router.setIpAddress("192.50.201.1", mask, (byte) 1);
		router.setRoute(new NetworkAddress("192.50.201.0", mask), (byte) 1);

		switch1Computers[0].setDefaultGateway("192.50.200.1");
		switch2Computers[0].setDefaultGateway("192.50.201.1");
		switch2Computers[1].setDefaultGateway("192.50.201.1");
		switch2Computers[2].setDefaultGateway("192.50.201.1");

		switch1Computers[0].send("192.50.201.3", "other network message".getBytes());
	}

	private void connect(Hub hub1, Hub hub2) {
		Cable cable = new Cable();
		hub1.connnectTo(cable);
		hub2.connnectTo(cable);
	}

	private void connect(Computer[] computers, Hub hub) {
		for (int i = 0; i < computers.length; i++) {
			Cable cable = new Cable();
			computers[i].connectTo(cable);
			hub.connnectTo(cable);
		}
	}
}