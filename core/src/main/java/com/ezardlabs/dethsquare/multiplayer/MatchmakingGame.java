package com.ezardlabs.dethsquare.multiplayer;

import org.json.JSONArray;
import org.json.JSONObject;

class MatchmakingGame {
	private final int id;
	private final MatchmakingPlayer[] players;

	private MatchmakingGame(int id, MatchmakingPlayer[] players) {
		this.id = id;
		this.players = players;
	}

	static MatchmakingGame fromJson(JSONObject object) {
		JSONArray jsonPlayers = object.getJSONArray("players");
		MatchmakingPlayer[] players = new MatchmakingPlayer[jsonPlayers.length()];
		for (int i = 0; i < jsonPlayers.length(); i++) {
			if (!jsonPlayers.isNull(i)) {
				players[i] = MatchmakingPlayer.fromJson(jsonPlayers.getJSONObject(i));
			}
		}
		return new MatchmakingGame(object.getInt("id"), players);
	}
}
