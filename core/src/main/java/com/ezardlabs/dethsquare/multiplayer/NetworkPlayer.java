package com.ezardlabs.dethsquare.multiplayer;

import org.json.JSONObject;

public class NetworkPlayer {
	private String ip;
	private int port;

	private NetworkPlayer(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	static NetworkPlayer fromJson(JSONObject object) {
		return new NetworkPlayer(object.getString("ip"), object.getInt("port"));
	}
}
