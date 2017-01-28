package com.ezardlabs.dethsquare.multiplayer;

import com.ezardlabs.dethsquare.multiplayer.Network.UDPReader;
import com.ezardlabs.dethsquare.multiplayer.Network.UDPWriter;
import com.ezardlabs.dethsquare.util.GameListeners;
import com.ezardlabs.dethsquare.util.GameListeners.UpdateListener;

import org.json.JSONObject;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Matchmaker implements NetworkConstants {
	private UDPReader udpReader;
	private UDPWriter udpWriter;
	private MatchmakingGame game;
	private long lastGamePing = 0;

	public Matchmaker(InetAddress ip, int serverPort) {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		if (socket == null) {
			throw new IllegalStateException("Failed to create matchmaking socket");
		} else {
			udpReader = new UDPReader(socket);
			udpWriter = new UDPWriter(socket,
					new InetSocketAddress[]{new InetSocketAddress(ip, serverPort)});
			udpReader.start();
			udpWriter.start();
			Network.init();
		}
	}

	public void findGame(MatchmakingListener listener) {
		udpWriter.sendMessage(getJsonMessage(MATCHMAKING_JOIN, false).toString().getBytes());
		GameListeners.addUpdateListener(new UpdateListener() {
			@Override
			public void onUpdate() {
				if (game != null && System.currentTimeMillis() - lastGamePing > PING_INTERVAL) {
					udpWriter.sendMessage(getJsonMessage(GAME_PING, true).toString().getBytes());
					lastGamePing = System.currentTimeMillis();
				}
				synchronized (udpReader.udpMessages) {
					while (!udpReader.udpMessages.isEmpty()) {
						JSONObject json = new JSONObject(
								new String(udpReader.udpMessages.remove(0)));
						String message = json.getString("message");
						switch (message) {
							case GAME_CREATE:
								setGame(MatchmakingGame.fromJson(json.getJSONObject("game")));
								Network.createGame();
								if (listener.onCreateGame()) {
									udpWriter.sendMessage(
											getJsonMessage(GAME_CREATE, true).toString()
																			 .getBytes());
								}
								break;
							case GAME_JOIN:
								MatchmakingGame game = MatchmakingGame
										.fromJson(json.getJSONObject("game"));
								setGame(game);
								Network.joinGame(game, json.getInt("playerId"));
								listener.onGameFound(game);
								udpWriter.sendMessage(
										getJsonMessage(GAME_JOIN, true).toString().getBytes());
								break;
							case PLAYER_JOIN:
								Network.addPlayer(
										MatchmakingPlayer.fromJson(json.getJSONObject("player")));
								break;
							default:
								listener.onError("Unknown error");
								break;
						}
					}
				}
			}
		});
	}

	private void setGame(MatchmakingGame game) {
		this.game = game;
	}

	private MatchmakingGame getGame() {
		return game;
	}

	private JSONObject getJsonMessage(String message, boolean includeGame) {
		JSONObject json = new JSONObject();
		json.put("udpPort", Network.getUdpPort());
		json.put("tcpPort", Network.getTcpPort());
		json.put("message", message);
		if (includeGame) {
			json.put("gameId", getGame().getId());
		}
		return json;
	}

	public interface MatchmakingListener {
		boolean onCreateGame();

		void onGameFound(MatchmakingGame game);

		void onError(String error);
	}
}
