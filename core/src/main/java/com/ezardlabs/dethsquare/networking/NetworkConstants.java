package com.ezardlabs.dethsquare.networking;

import java.util.regex.Pattern;

interface NetworkConstants {
	// matchmaking constants
	String MATCHMAKING_JOIN = "matchmaking_join";
	String MATCHMAKING_PING = "matchmaking_ping";
	String MATCHMAKING_LEAVE = "matchmaking_leave";

	String GAME_CREATE = "game_create";
	String GAME_JOIN = "game_join";
	String GAME_PING = "game_ping";
	String GAME_LEAVE = "game_leave";

	String PLAYER_JOIN = "player_join";
	String PLAYER_LEAVE = "player_leave";

	int PING_INTERVAL = 1000;

	// messaging constants
	String DIVIDER = "|";
	String SPLIT_DIVIDER = Pattern.quote(DIVIDER);
	String INSTANTIATE = "instantiate";
	String DESTROY = "destroy";
	String REQUEST_STATE = "request_state";
	String MESSAGE = "message";
}
