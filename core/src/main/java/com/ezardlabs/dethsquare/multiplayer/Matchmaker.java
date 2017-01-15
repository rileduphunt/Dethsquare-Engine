package com.ezardlabs.dethsquare.multiplayer;

import com.ezardlabs.dethsquare.util.GameListeners;
import com.ezardlabs.dethsquare.util.GameListeners.UpdateListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Matchmaker implements NetworkConstants {
	private final MatchmakingThread matchmakingThread;

	public Matchmaker(String serverIp, int serverPort) {
		matchmakingThread = new MatchmakingThread(new InetSocketAddress(serverIp, serverPort));
		matchmakingThread.start();
	}

	public void findGame(MatchmakingListener listener) {
		matchmakingThread.send(MATCHMAKING_JOIN);
		GameListeners.addUpdateListener(new UpdateListener() {
			@Override
			public void onUpdate() {
				if (matchmakingThread.isDone()) {
					GameListeners.removeUpdateListener(this);
					String data = matchmakingThread.get();
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
								NetworkPlayer[] players = new NetworkPlayer[jsonPlayers.length()];
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
				}
			}
		});
	}

	private void registerNewGame() {
		matchmakingThread.send(GAME_CREATE);
	}

	private class MatchmakingThread extends Thread {
		private final InetSocketAddress server;
		private final DatagramSocket socket;
		private volatile String message;
		private volatile String response;

		private MatchmakingThread(InetSocketAddress server) {
			this.server = server;
			try {
				socket = new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not create matchmaking socket");
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					wait();
					byte[] bytes = message.getBytes();
					socket.send(new DatagramPacket(bytes, bytes.length, server));
					DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
					socket.receive(packet);
					response = new String(packet.getData());
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void send(String data) {
			message = data;
			notify();
		}

		private boolean isDone() {
			return response != null;
		}

		private String get() {
			String temp = response;
			response = null;
			return temp;
		}
	}

	public interface MatchmakingListener {
		void onCreateGame();

		void onGameFound(int playerId, NetworkPlayer[] players);

		void onError(String error);
	}
}
