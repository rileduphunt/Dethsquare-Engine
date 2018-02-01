package com.ezardlabs.dethsquare.networking;

import com.ezardlabs.dethsquare.networking.Network.Protocol;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class UPnPManager {
	private static final String IP = "239.255.255.250";
	private static final int PORT = 1900;
	private static final String[] SEARCH_TYPES = {
			"urn:schemas-upnp-org:device:InternetGatewayDevice:1",
			"urn:schemas-upnp-org:service:WANIPConnection:1",
			"urn:schemas-upnp-org:service:WANPPPConnection:1"
	};
	private static String baseUrl;
	private static ArrayList<String[]> services = new ArrayList<>();

	private static List<InetAddress> getLocalInetAddresses() {
		List<InetAddress> arrayIPAddress = new ArrayList<>();

		// Get all network interfaces
		Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return arrayIPAddress;
		}

		if (networkInterfaces == null) return arrayIPAddress;

		// For every suitable network interface, get all IP addresses
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface card = networkInterfaces.nextElement();

			try {
				// skip devices, not suitable to search gateways for
				if (card.isLoopback() || card.isPointToPoint() || card.isVirtual() || !card.isUp()) continue;
			} catch (SocketException e) {
				continue;
			}

			Enumeration<InetAddress> addresses = card.getInetAddresses();

			while (addresses.hasMoreElements()) {
				InetAddress inetAddress = addresses.nextElement();
				int index = arrayIPAddress.size();

				if (!Inet4Address.class.isInstance(inetAddress)) continue;

				arrayIPAddress.add(index, inetAddress);
			}
		}

		return arrayIPAddress;
	}

	private static class DiscoveryThread extends Thread {
		private final InetAddress address;

		private DiscoveryThread(InetAddress address) {
			this.address = address;
		}

		@Override
		public void run() {
			try {
				DatagramSocket socket = new DatagramSocket(0, address);
				socket.setSoTimeout(3000);
				outer:
				for (String searchType : SEARCH_TYPES) {
					String message = "M-SEARCH * HTTP/1.1\r\nHOST: " + IP + ":" + PORT + "\r\nST: " + searchType +
							"\r\nMAN: \"ssdp:discover\"\r\nMX: 3\r\n\r\n";
					byte[] messageBytes = message.getBytes();
					DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length,
							InetAddress.getByName(IP), PORT);
					socket.send(packet);

					int count = -1;

					boolean waitingPacket = true;
					while (waitingPacket) {
						count++;
						DatagramPacket receivePacket = new DatagramPacket(new byte[1536], 1536);
						try {
							socket.receive(receivePacket);
							byte[] receivedData = new byte[receivePacket.getLength()];
							System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivePacket.getLength());

							String data = new String(receivedData);

							String location = getLocation(data);

							URLConnection conn = new URL(location).openConnection();

							DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
							DocumentBuilder builder = factory.newDocumentBuilder();
							Document doc = builder.parse(conn.getInputStream());

							Element root = doc.getDocumentElement();
							parseXML(root);

							if (baseUrl == null) {
								URL url = new URL(location);
								baseUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
							}
						} catch (SocketTimeoutException ste) {
							System.err.println("Timed out waiting for UPnP discovery response: " + count);
							waitingPacket = false;
						} catch (ParserConfigurationException | SAXException e) {
							e.printStackTrace();
						}
						if (!services.isEmpty()) {
							break outer;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private String getLocation(String data) {
			StringTokenizer st = new StringTokenizer(data, "\n");

			while (st.hasMoreTokens()) {
				String line = st.nextToken().trim();

				if (line.isEmpty()) continue;

				if (line.startsWith("HTTP/1.") || line.startsWith("NOTIFY *")) continue;

				String key = line.substring(0, line.indexOf(':'));
				String value = line.length() > key.length() + 1 ? line.substring(key.length() + 1) : null;

				key = key.trim();
				if (value != null) {
					value = value.trim();
				}

				if (key.compareToIgnoreCase("location") == 0) {
					return value;
				}
			}
			return null;
		}
	}

	static void discover() {
		List<InetAddress> localAddresses = getLocalInetAddresses();
		ArrayList<DiscoveryThread> discoveryThreads = new ArrayList<>(localAddresses.size());
		for (InetAddress address : localAddresses) {
			DiscoveryThread discoveryThread = new DiscoveryThread(address);
			discoveryThreads.add(discoveryThread);
			discoveryThread.start();
		}
		for (DiscoveryThread discoveryThread : discoveryThreads) {
			try {
				discoveryThread.join();
			} catch (InterruptedException ignored) {
			}
		}
	}

	private static void parseXML(Node element) {
		switch (element.getNodeName()) {
			case "root":
				NodeList children = element.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node child = children.item(i);
					if ("URLBase".equals(child.getNodeName())) {
						baseUrl = child.getTextContent();
					} else if ("device".equals(child.getNodeName())) {
						parseXML(child);
					}
				}
				break;
			case "deviceList":
				children = element.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					if ("device".equals(children.item(i).getNodeName())) {
						parseXML(children.item(i));
					}
				}
				break;
			case "device":
				children = element.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					if ("serviceList".equals(children.item(i).getNodeName()) ||
							"deviceList".equals(children.item(i).getNodeName())) {
						parseXML(children.item(i));
					}
				}
				break;
			case "serviceList":
				children = element.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					if ("service".equals(children.item(i).getNodeName())) {
						parseXML(children.item(i));
					}
				}
				break;
			case "service":
				children = element.getChildNodes();
				boolean breakOuterLoop = false;
				for (int i = 0; i < children.getLength(); i++) {
					Node child = children.item(i);
					if ("serviceType".equals(child.getNodeName()) &&
							Arrays.asList(SEARCH_TYPES).contains(child.getTextContent())) {
						for (int j = 0; j < children.getLength(); j++) {
							Node child2 = children.item(j);
							if ("controlURL".equals(child2.getNodeName())) {
								services.add(new String[]{
										child.getTextContent(),
										child2.getTextContent()
								});
								breakOuterLoop = true;
								break;
							}
						}
					}
					if (breakOuterLoop) break;
				}
				break;
			default:
				break;
		}
	}

	private static void sendSOAPMessage(String action, String service, String url, Map<String, String> args) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		sb.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
				"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">");
		sb.append("<s:Body>");
		sb.append("<m:").append(action).append(" xmlns:m=\"").append(service).append("\">");
		for (String s : args.keySet()) {
			sb.append("<").append(s).append(">").append(args.get(s)).append("</").append(s).append(">");
		}
		sb.append("</m:").append(action).append(">");
		sb.append("</s:Body>");
		sb.append("</s:Envelope>");
		String message = sb.toString();

		try {
			URL postUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();

			conn.setRequestMethod("POST");
			conn.setReadTimeout(5000);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "text/xml");
			conn.setRequestProperty("SOAPAction", "\"" + service + "#" + action + "\"");
			conn.setRequestProperty("Connection", "Close");

			byte[] messageBytes = message.getBytes();

			conn.setRequestProperty("Content-Length", String.valueOf(messageBytes.length));

			conn.getOutputStream().write(messageBytes);

			Map<String, String> nameValue = new HashMap<>();
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(new DefaultHandler() {
				private String currentElement;

				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException {
					currentElement = localName;
				}

				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
					currentElement = null;
				}

				@Override
				public void characters(char[] ch, int start, int length) throws SAXException {
					if (currentElement != null) {
						String value = new String(ch, start, length);
						String old = nameValue.put(currentElement, value);
						if (old != null) {
							nameValue.put(currentElement, old + value);
						}
					}
				}
			});
			if (conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
				try {
					parser.parse(new InputSource(conn.getErrorStream()));
				} catch (SAXException ignored) {
				}
				conn.disconnect();
			} else {
				parser.parse(new InputSource(conn.getInputStream()));
				conn.disconnect();
			}

			conn.disconnect();
		} catch (IOException | SAXException e) {
			e.printStackTrace();
		}
	}

	static void addPortMapping(int port, Protocol protocol, String description) {
		HashMap<String, String> args = new HashMap<>();
		args.put("NewRemoteHost", "");
		args.put("NewExternalPort", String.valueOf(port));
		args.put("NewProtocol", protocol.toString());
		args.put("NewInternalPort", String.valueOf(port));
		try {
			args.put("NewInternalClient", InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		args.put("NewEnabled", "1");
		args.put("NewPortMappingDescription", description == null ? "" : description);
		args.put("NewLeaseDuration", "0");

		for (String[] s : services) {
			sendSOAPMessage("AddPortMapping", s[0], baseUrl + s[1], args);
		}
	}
}
