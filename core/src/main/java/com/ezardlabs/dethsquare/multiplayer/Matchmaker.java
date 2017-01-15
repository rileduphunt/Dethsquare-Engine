package com.ezardlabs.dethsquare.multiplayer;

import com.ezardlabs.dethsquare.util.GameListeners;
import com.ezardlabs.dethsquare.util.GameListeners.UpdateListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Matchmaker implements NetworkConstants {
	private final InetSocketAddress server;

	public Matchmaker(String serverIp, int serverPort) {
		this.server = new InetSocketAddress(serverIp, serverPort);
	}

	public void findGame(MatchmakingListener listener) {
		CompletableFuture<String> task = CompletableFuture.supplyAsync(() -> {
			byte[] bytes = MATCHMAKING_JOIN.getBytes();
			try (DatagramSocket socket = new DatagramSocket(Network.myPort)) {
				socket.send(new DatagramPacket(bytes, bytes.length, server));
				DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
				socket.receive(packet);
				return new String(packet.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		});
		GameListeners.addUpdateListener(new UpdateListener() {
			@Override
			public void onUpdate() {
				if (task.isDone()) {
					GameListeners.removeUpdateListener(this);
					try {
						String data = task.get();
						if (data == null) {
							listener.onError("Server error");
						} else {
							JSONObject json = new JSONObject(data);
							String message = json.getString("message");
							switch (message) {
								case GAME_CREATE:
									listener.onCreateGame();
									Matchmaker.this.registerNewGame();
									break;
								case GAME_JOIN:
									JSONArray jsonPlayers = json.getJSONArray("players");
									NetworkPlayer[] players = new NetworkPlayer[jsonPlayers
											.length()];
									for (int i = 0; i < players.length; i++) {
										players[i] = NetworkPlayer
												.fromJson(jsonPlayers.getJSONObject(i));
									}
									listener.onGameFound(json.getInt("playerId"), players);
									break;
								default:
									listener.onError("Unknown error");
									break;
							}
						}
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
						listener.onError("Internal error");
					}
				}
			}
		});
	}

	private void registerNewGame() {
		new Thread(() -> {
			byte[] bytes = GAME_CREATE.getBytes();
			try (DatagramSocket socket = new DatagramSocket(Network.myPort)) {
				socket.send(new DatagramPacket(bytes, bytes.length, server));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public interface MatchmakingListener {
		void onCreateGame();

		void onGameFound(int playerId, NetworkPlayer[] players);

		void onError(String error);
	}
}
