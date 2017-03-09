package com.ezardlabs.dethsquare.multiplayer;

import org.json.JSONObject;

class MatchmakingPlayer {
	private String ip;
	private int udpPort;
	private int tcpPort;
	private final int id;

	private MatchmakingPlayer(String ip, int udpPort, int tcpPort, int id) {
		this.ip = ip;
		this.udpPort = udpPort;
		this.tcpPort = tcpPort;
		this.id = id;
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

	public int getId() {
		return id;
	}

	static MatchmakingPlayer fromJson(JSONObject object) {
		return new MatchmakingPlayer(object.getString("ip"), object.getInt("udpPort"), object.getInt("tcpPort"),
				object.getInt("id"));
	}
}
