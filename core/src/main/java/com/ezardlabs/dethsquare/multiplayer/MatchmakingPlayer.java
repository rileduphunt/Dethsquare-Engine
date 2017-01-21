package com.ezardlabs.dethsquare.multiplayer;

import org.json.JSONObject;

class MatchmakingPlayer {
	private String ip;
	private int udpPort;
	private int tcpPort;
	private boolean host = false;

	private MatchmakingPlayer(String ip, int udpPort, int tcpPort, boolean host) {
		this.ip = ip;
		this.udpPort = udpPort;
		this.tcpPort = tcpPort;
		this.host = host;
	}

	String getIp() {
		return ip;
	}

	public int getUdpPort() {
		return udpPort;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	static MatchmakingPlayer fromJson(JSONObject object) {
		return new MatchmakingPlayer(object.getString("ip"), object.getInt("udpPort"),
				object.getInt("tcpPort"), object.has("host") && object.getBoolean("host"));
	}
}
