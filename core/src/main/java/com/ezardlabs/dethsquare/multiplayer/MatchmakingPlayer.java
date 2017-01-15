package com.ezardlabs.dethsquare.multiplayer;

import org.json.JSONObject;

class MatchmakingPlayer {
	private String ip;
	private int port;
	private boolean host = false;

	private MatchmakingPlayer(String ip, int port, boolean host) {
		this.ip = ip;
		this.port = port;
		this.host = host;
	}

	String getIp() {
		return ip;
	}

	int getPort() {
		return port;
	}

	static MatchmakingPlayer fromJson(JSONObject object) {
		return new MatchmakingPlayer(object.getString("ip"), object.getInt("port"),
				object.has("host") && object.getBoolean("host"));
	}
}
