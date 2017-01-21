package com.ezardlabs.dethsquare.multiplayer;

import com.ezardlabs.dethsquare.util.GameListeners;
import com.ezardlabs.dethsquare.util.GameListeners.UpdateListener;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Matchmaker implements NetworkConstants {
	private final MatchmakingThread matchmakingThread;

	public Matchmaker(InetAddress ip, int serverPort) {
		matchmakingThread = new MatchmakingThread(ip, serverPort);
		matchmakingThread.start();
	}

	public void findGame(MatchmakingListener listener) {
		matchmakingThread.send(MATCHMAKING_JOIN, true);
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
								if (listener.onCreateGame()) {
									matchmakingThread.send(GAME_CREATE, false);
								}
								break;
							case GAME_JOIN:
								MatchmakingGame game = MatchmakingGame
										.fromJson(json.getJSONObject("game"));
								listener.onGameFound(json.getInt("playerId"), game);
								matchmakingThread.send(GAME_JOIN, false);
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

	private class MatchmakingThread extends Thread {
		private final InetAddress ip;
		private final int port;
		private final DatagramSocket socket;
		private volatile String message;
		private boolean expectingResponse;
		private volatile String response;

		private MatchmakingThread(InetAddress ip, int port) {
			this.ip = ip;
			this.port = port;
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
					while (message == null) {
						synchronized (this) {
							wait();
						}
					}
					byte[] bytes = message.getBytes();
					message = null;
					socket.send(new DatagramPacket(bytes, bytes.length, ip, port));
					if (expectingResponse) {
						DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
						socket.receive(packet);
						response = new String(packet.getData());
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void send(String data, boolean expectingResponse) {
			message = "{\"message\":\"" + data + "\"}";
			this.expectingResponse = expectingResponse;
			synchronized (this) {
				notify();
			}
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
		boolean onCreateGame();

		void onGameFound(int playerId, MatchmakingGame game);

		void onError(String error);
	}
}
