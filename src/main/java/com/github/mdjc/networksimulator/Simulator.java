package com.github.mdjc.networksimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.mdjc.networksimulator.domain.Cable;
import com.github.mdjc.networksimulator.domain.Computer;
import com.github.mdjc.networksimulator.domain.Hub;

@Component
public class Simulator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Simulator.class);

	public void run() {
		Computer[][] computers = { new Computer[2], new Computer[1], new Computer[1], new Computer[1] };
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

		LOGGER.info("----------------------------------------------------------------------");
		computers[0][0].setIpAddress("172.44.200.0");
		computers[1][0].setIpAddress("172.44.200.5");

		LOGGER.info("................Sending Hello.....................");
		computers[0][0].send(computers[1][0].getIpAddress(), "Hello".getBytes());

		LOGGER.info("----------------------------------------------------------------------");
	}

	private void connect(Hub hub1, Hub hub2) {
		Cable cable = new Cable();
		hub1.connnectTo(cable);
		hub2.connnectTo(cable);
	}

	private void connect(Computer[] computers, Hub hub) {
		for (int i = 0; i < computers.length; i++) {
			computers[i] = new Computer();
			Cable cable = new Cable();
			computers[i].connectTo(cable);
			hub.connnectTo(cable);
		}
	}
}
