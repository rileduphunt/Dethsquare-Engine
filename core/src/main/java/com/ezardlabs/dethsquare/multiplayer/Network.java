package com.ezardlabs.dethsquare.multiplayer;

import com.ezardlabs.dethsquare.GameObject;
import com.ezardlabs.dethsquare.Vector2;
import com.ezardlabs.dethsquare.prefabs.PrefabManager;
import com.ezardlabs.dethsquare.util.GameListeners;
import com.ezardlabs.dethsquare.util.GameListeners.UpdateListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class Network {
	private static UpdateListener updateListener;

	private static UDPWriter udpOut;
	private static UDPReader udpIn;
	private static final InetSocketAddress[] udpAddresses = new InetSocketAddress[4];
	private static final TCPWriter[] tcpOut = new TCPWriter[4];

	private static final int START_PORT = 2828;
	private static DatagramSocket datagramSocket;
	private static ServerSocket serverSocket;
	private static int udpPort = -1;
	private static int tcpPort = -1;

	private static int playerId = 0;
	private static boolean host = true;

	private static int networkIdCounter = 0;
	private static HashMap<Integer, InstantiationData> networkObjects = new HashMap<>();

	private static final long UPDATES_PER_SECOND = 60;
	private static long lastUpdate = 0;

	private static final String DIVIDER = "|";
	private static final String SPLIT_DIVIDER = Pattern.quote(DIVIDER);
	private static final String INSTANTIATE = "instantiate";
	private static final String DESTROY = "destroy";

	public enum Protocol {
		UDP,
		TCP
	}

	static void init() {
		datagramSocket = getNewDatagramSocket();
		udpPort = datagramSocket.getLocalPort();
		serverSocket = getNewServerSocket(udpPort + 1);
		tcpPort = serverSocket.getLocalPort();

		UPnPManager.discover();
		UPnPManager.addPortMapping(udpPort, Protocol.UDP, "Lost Sector UDP " + udpPort);
		UPnPManager.addPortMapping(tcpPort, Protocol.TCP, "Lost Sector TCP " + tcpPort);

		udpOut = new UDPWriter(datagramSocket, udpAddresses);
		udpOut.start();
		udpIn = new UDPReader(datagramSocket);
		udpIn.start();
		new TCPServer(serverSocket).start();
	}

	static DatagramSocket getNewDatagramSocket() {
		return getNewDatagramSocket(START_PORT);
	}

	static DatagramSocket getNewDatagramSocket(int startPort) {
		int port = startPort;
		DatagramSocket ds;
		while (true) {
			try {
				ds = new DatagramSocket(port);
				break;
			} catch (SocketException ignored) {
			}
			port++;
		}
		return ds;
	}

	static ServerSocket getNewServerSocket() {
		return getNewServerSocket(START_PORT);
	}

	static ServerSocket getNewServerSocket(int startPort) {
		int port = startPort;
		ServerSocket ss;
		while (true) {
			try {
				ss = new ServerSocket(port);
				break;
			} catch (IOException ignored) {
			}
		}
		return ss;
	}

	static int getUdpPort() {
		return udpPort;
	}

	static int getTcpPort() {
		return tcpPort;
	}

	public static int getPlayerId() {
		return playerId;
	}

	static int getNewNetworkId() {
		return networkIdCounter++;
	}

	public static boolean isHost() {
		return host;
	}

	private static void update() {
		if (System.currentTimeMillis() >= lastUpdate + 1000 / UPDATES_PER_SECOND) {
			lastUpdate = System.currentTimeMillis();
			ByteBuffer data = ByteBuffer.allocate(NetworkBehaviour.totalSize + (NetworkBehaviour
					.myNetworkBehaviours.size() * 8));
			for (NetworkBehaviour nb : NetworkBehaviour.myNetworkBehaviours.values()) {
				data.putInt(nb.getNetworkId());
				data.putShort(nb.getSize());
				data.put(nb.onSend());
			}
			udpOut.sendMessage(data.array());
		}
		synchronized (udpIn.udpMessages) {
			while (!udpIn.udpMessages.isEmpty()) {
				int count = 0;
				ByteBuffer data = ByteBuffer.wrap(udpIn.udpMessages.remove(0));
				NetworkBehaviour nb;
				while (count < data.capacity()) {
					data.position(count);
					int networkId = data.getInt(count);
					if (networkId == 0) break;
					int size = data.getShort(count + 4);
					nb = NetworkBehaviour.otherNetworkBehaviours.get(networkId);
					if (nb != null) {
						nb.onReceive(data, count + 6);
					}
					count += size + 6;
				}
			}
		}
	}

	static void createGame() {
		host = true;
		GameListeners.addUpdateListener(Network::update);
	}

	static void joinGame(MatchmakingGame game, int playerId) {
		host = false;
		Network.playerId = playerId;
		networkIdCounter = playerId * (Integer.MAX_VALUE / 4) + 1;
		for (MatchmakingPlayer player : game.getPlayers()) {
			if (player.getId() != playerId) {
				udpAddresses[player.getId()] = new InetSocketAddress(player.getIp(), player.getUdpPort());
				udpOut.setAddresses(udpAddresses);
				try {
					tcpOut[player.getId()] = new TCPWriter(new Socket(player.getIp(), player.getTcpPort()));
					tcpOut[player.getId()].start();
				} catch (IOException e) {
					// TODO add proper error handling
					e.printStackTrace();
				}
			}
		}
		GameListeners.addUpdateListener(Network::update);
	}

	static void addPlayer(MatchmakingPlayer player) {
		udpAddresses[player.getId()] = new InetSocketAddress(player.getIp(), player.getUdpPort());
		udpOut.setAddresses(udpAddresses);
		try {
			tcpOut[player.getId()] = new TCPWriter(new Socket(player.getIp(), player.getTcpPort()));
			tcpOut[player.getId()].start();
			if (host) {
				for (InstantiationData data : networkObjects.values()) {
					if (data.playerId != player.getId()) {
						tcpOut[player.getId()].sendMessage(INSTANTIATE, getInstantiationMessage(data));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class UDPReader extends Thread {
		private final DatagramSocket socket;
		final ArrayList<byte[]> udpMessages = new ArrayList<>();

		UDPReader(DatagramSocket socket) {
			super("UDPReader");
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
				while (true) {
					socket.receive(packet);
					synchronized (udpMessages) {
						udpMessages.add(packet.getData());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static class UDPWriter extends Thread {
		private final DatagramSocket socket;
		private final ArrayList<byte[]> messages = new ArrayList<>();
		private final DatagramPacket[] packets;

		UDPWriter(DatagramSocket socket, SocketAddress[] addresses) {
			super("UDPWriter");
			this.socket = socket;
			packets = new DatagramPacket[addresses.length];
			for (int i = 0; i < packets.length; i++) {
				if (addresses[i] == null) {
					packets[i] = null;
				} else {
					packets[i] = new DatagramPacket(new byte[0], 0, addresses[i]);
				}
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					synchronized (messages) {
						messages.wait();
						while (!messages.isEmpty()) {
							byte[] message = messages.remove(0);
							for (int i = 0; i < packets.length; i++) {
								if (packets[i] != null) {
									packets[i].setData(message);
									socket.send(packets[i]);
								}
							}
						}
					}
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		void sendMessage(byte[] message) {
			synchronized (messages) {
				messages.add(message);
				messages.notify();
			}
		}

		void setAddresses(SocketAddress[] addresses) {
			for (int i = 0; i < packets.length; i++) {
				if (addresses[i] == null) {
					packets[i] = null;
				} else {
					packets[i] = new DatagramPacket(new byte[0], 0, addresses[i]);
				}
			}
		}
	}

	private static class TCPServer extends Thread {
		private final ServerSocket serverSocket;

		TCPServer(ServerSocket serverSocket) {
			super("TCPServer");
			this.serverSocket = serverSocket;
		}

		@Override
		public void run() {
			try {
				while (true) {
					new TCPReader(serverSocket.accept()).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static class TCPReader extends Thread {
		private final Socket socket;

		TCPReader(Socket socket) {
			super("TCPReader");
			this.socket = socket;
		}

		@Override
		public void run() {
			try (BufferedReader in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()))) {
				socket.setKeepAlive(true);
				while (socket.isConnected()) {
					String command = in.readLine();
					if (command != null) {
						switch (command) {
							case INSTANTIATE:
								processInstantiation(in.readLine());
								break;
							case DESTROY:
								processDestruction(in.readLine());
								break;
							default:
								System.out.println("Unknown command:" + command);
								break;
						}
					}
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static class TCPWriter extends Thread {
		private final Socket socket;
		private final ArrayList<String[]> messages = new ArrayList<>();

		TCPWriter(Socket socket) {
			super("TCPWriter");
			this.socket = socket;
		}

		@Override
		public void run() {
			try (BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()))) {
				while (true) {
					synchronized (messages) {
						messages.wait();
						while (!messages.isEmpty()) {
							String[] s = messages.remove(0);
							out.write(s[0]);
							out.newLine();
							out.write(s[1]);
							out.newLine();
							out.flush();
						}
					}
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		void sendMessage(String command, String message) {
			if (message.contains("\n") || message.contains("\r")) {
				throw new IllegalArgumentException("Message cannot contain newline characters");
			}
			synchronized (messages) {
				messages.add(new String[]{command,
						message});
				messages.notify();
			}
		}
	}

	private static class InstantiationData {
		final String prefabName;
		final Vector2 position;
		final int playerId;
		final GameObject gameObject;

		private InstantiationData(String prefabName, Vector2 position, int playerId, GameObject gameObject) {
			this.prefabName = prefabName;
			this.position = position;
			this.playerId = playerId;
			this.gameObject = gameObject;
		}
	}

	public static GameObject instantiate(String prefabName, Vector2 position) {
		return instantiate(prefabName, position, tcpOut);
	}

	private static GameObject instantiate(String prefabName, Vector2 position, TCPWriter... tcpWriters) {
		GameObject gameObject = PrefabManager.loadPrefab(prefabName);
		gameObject.networkId = getNewNetworkId();
		InstantiationData data = new InstantiationData(prefabName, position, playerId, gameObject);
		String message = getInstantiationMessage(data);
		for (TCPWriter writer : tcpWriters) {
			if (writer != null) {
				writer.sendMessage(INSTANTIATE, message);
			}
		}
		GameObject go = GameObject.instantiate(gameObject, position);
		networkObjects.put(go.networkId, data);
		return go;
	}

	private static String getInstantiationMessage(InstantiationData data) {
		List<NetworkBehaviour> networkBehaviours = data.gameObject.getComponentsOfType(NetworkBehaviour.class);
		HashMap<String, Integer> networkIds = new HashMap<>();
		for (NetworkBehaviour nb : networkBehaviours) {
			networkIds.put(nb.getClass().getCanonicalName(), nb.getNetworkId());
		}
		StringBuilder sb = new StringBuilder();
		if (PrefabManager.prefabExists(data.prefabName + "_other")) {
			sb.append(data.prefabName).append("_other").append(DIVIDER);
		} else {
			sb.append(data.prefabName).append(DIVIDER);
		}
		sb.append(data.gameObject.networkId).append(DIVIDER);
		sb.append(data.position.x).append(DIVIDER);
		sb.append(data.position.y).append(DIVIDER);
		sb.append(playerId).append(DIVIDER);
		for (String key : networkIds.keySet()) {
			sb.append(key).append(DIVIDER).append(networkIds.get(key)).append(DIVIDER);
		}
		String message = sb.toString();
		message = message.substring(0, message.length() - 1);
		return message;
	}

	private static void processInstantiation(String message) throws IOException {
		String[] split = message.split(SPLIT_DIVIDER);
		GameObject gameObject = PrefabManager.loadPrefab(split[0]);
		gameObject.networkId = Integer.parseInt(split[1]);
		Vector2 position = new Vector2(Float.parseFloat(split[2]),
				Float.parseFloat(split[3]));
		int playerId = Integer.parseInt(split[4]);
		List<NetworkBehaviour> networkBehaviours = gameObject
				.getComponentsOfType(NetworkBehaviour.class);
		HashMap<String, Integer> networkIds = new HashMap<>();
		for (int i = 0; i < networkBehaviours.size(); i++) {
			networkIds.put(split[5 + (i * 2)], Integer.parseInt(split[6 + (i * 2)]));
		}
		for (NetworkBehaviour nb : networkBehaviours) {
			nb.setPlayerId(playerId);
			nb.setNetworkId(networkIds.get(nb.getClass().getCanonicalName()));
		}
		GameObject go = GameObject.instantiate(gameObject, position);
		networkObjects.put(go.networkId, new InstantiationData(split[0], position, playerId, gameObject));
	}

	public static void destroy(GameObject gameObject) {
		GameObject.destroy(gameObject);
		networkObjects.remove(gameObject.networkId);
		for (TCPWriter writer : tcpOut) {
			if (writer != null) {
				writer.sendMessage(DESTROY, String.valueOf(gameObject.networkId));
			}
		}
	}

	public static void destroy(GameObject gameObject, long delay) {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				destroy(gameObject);
			}
		}, delay);
	}

	private static void processDestruction(String message) throws IOException {
		int networkId = Integer.parseInt(message);
		GameObject.destroy(networkId);
		networkObjects.remove(networkId);
	}
}