package com.ezardlabs.dethsquare.multiplayer;

import org.json.JSONObject;

public class MatchmakingPlayer {
	private String ip;
	private int port;

	private MatchmakingPlayer(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	static MatchmakingPlayer fromJson(JSONObject object) {
		return new MatchmakingPlayer(object.getString("ip"), object.getInt("port"));
	}
}