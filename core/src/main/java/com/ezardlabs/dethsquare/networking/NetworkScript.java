package com.ezardlabs.dethsquare.networking;

import com.ezardlabs.dethsquare.Script;
import com.ezardlabs.dethsquare.networking.NetworkScript.NetVarField.Type;
import com.ezardlabs.dethsquare.networking.markers.NetVar;
import com.ezardlabs.dethsquare.networking.markers.Networked;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class NetworkScript extends Script implements Networked {
	private int networkId = -1;
	private int playerId = -1;

	private NetVarField[] fields = getNetVarFields(getClass());
	private short size = calculateSize(fields);
	private ByteBuffer data = ByteBuffer.allocate(size);

	static class NetVarField {
		private final Field field;
		private final NetVarField.Type type;
		private final short size;

		enum Type {
			BOOLEAN,
			BYTE,
			CHAR,
			DOUBLE,
			FLOAT,
			INT,
			LONG,
			SHORT
		}

		private NetVarField(Field field, NetVarField.Type type) {
			this.field = field;
			this.type = type;
			this.size = calculateSize();
		}

		private short calculateSize() {
			switch (type) {
				case BOOLEAN:
					return 1;
				case BYTE:
					return 1;
				case CHAR:
					return 2;
				case DOUBLE:
					return 8;
				case FLOAT:
					return 4;
				case INT:
					return 4;
				case LONG:
					return 8;
				case SHORT:
					return 2;
				default:
					return 0;
			}
		}
	}

	private static NetVarField[] getNetVarFields(Class clazz) {
		ArrayList<NetVarField> fieldList = new ArrayList<>();
		for (Field field : clazz.getFields()) {
			if (!Modifier.isStatic(field.getModifiers()) && field.getAnnotation(NetVar.class) != null) {
				Type type;
				try {
					type = Type.valueOf(field.getType().toString().toUpperCase());
				} catch (IllegalArgumentException e) {
					System.err.println(clazz + ": " + field.getName() + ": NetVars can only be primitives");
					continue;
				}
				fieldList.add(new NetVarField(field, type));
			}
		}
		return fieldList.toArray(new NetVarField[fieldList.size()]);
	}

	private static short calculateSize(NetVarField[] fields) {
		short size = 0;
		for (NetVarField field : fields) {
			size += field.size;
		}
		return size;
	}

	@Override
	public final ByteBuffer onSend() {
		data.position(0);
		int pos = 0;
		for (NetVarField field : fields) {
			try {
				switch (field.type) {
					case BOOLEAN:
						data.put(pos, (byte) (field.field.getBoolean(this) ? 1 : 0));
						break;
					case BYTE:
						data.put(pos, field.field.getByte(this));
						break;
					case CHAR:
						data.putChar(pos, field.field.getChar(this));
						break;
					case DOUBLE:
						data.putDouble(pos, field.field.getDouble(this));
						break;
					case FLOAT:
						data.putFloat(pos, field.field.getFloat(this));
						break;
					case INT:
						data.putInt(pos, field.field.getInt(this));
						break;
					case LONG:
						data.putLong(pos, field.field.getLong(this));
						break;
					case SHORT:
						data.putShort(pos, field.field.getShort(this));
						break;
				}
				pos += field.size;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				data.clear();
				break;
			}
		}
		return data;
	}

	@Override
	public final void onReceive(ByteBuffer data, int index) {
		int pos = 0;
		for (NetVarField field : fields) {
			try {
				switch (field.type) {
					case BOOLEAN:
						field.field.setBoolean(this, data.get(index + pos) == 1);
						break;
					case BYTE:
						field.field.setByte(this, data.get(index + pos));
						break;
					case CHAR:
						field.field.setChar(this, data.getChar(index + pos));
						break;
					case DOUBLE:
						field.field.setDouble(this, data.getDouble(index + pos));
						break;
					case FLOAT:
						field.field.setFloat(this, data.getFloat(index + pos));
						break;
					case INT:
						field.field.setInt(this, data.getInt(index + pos));
						break;
					case LONG:
						field.field.setLong(this, data.getLong(index + pos));
						break;
					case SHORT:
						field.field.setShort(this, data.getShort(index + pos));
						break;
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				data.clear();
				break;
			}
			pos += field.size;
		}
	}

	@Override
	public final short getSize() {
		return size;
	}

	final int getNetworkId() {
		return networkId;
	}

	final void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	final int getPlayerId() {
		return playerId;
	}

	final void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	protected final void sendMessage(String command, String message) {
		Network.sendMessage(this, command, message);
	}

	protected final void receiveMessage(String command, String message) {
		// only used in subclasses
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <em>Derived classes must call through to the super class's implementation of this method. If they do not, an
	 * exception will be thrown.</em>
	 */
	@Override
	public void start() {
		Network.registerNetworkScript(this);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <em>Derived classes must call through to the super class's implementation of this method. If they do not, an
	 * exception will be thrown.</em>
	 */
	@Override
	protected void destroy() {
		Network.deregisterNetworkScript(this);
	}
}
