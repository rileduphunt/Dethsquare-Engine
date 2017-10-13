package com.ezardlabs.dethsquare.networking;

import com.ezardlabs.dethsquare.networking.AutoNetworkBehaviour.NetVarField.Type;
import com.ezardlabs.dethsquare.networking.markers.NetVar;
import com.ezardlabs.dethsquare.networking.markers.Networked;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AutoNetworkBehaviour extends NetworkBehaviour {
	private final Networked object;
	private final NetVarField[] fields;
	private final short size;

	static class NetVarField {
		private final Field field;
		private final Type type;
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

		private NetVarField(Field field, Type type) {
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

	public AutoNetworkBehaviour(Networked object) {
		this.object = object;
		this.fields = getNetVarFields(object.getClass());
		this.size = calculateSize();
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

	private short calculateSize() {
		short size = 0;
		for (NetVarField field : fields) {
			size += field.size;
		}
		return size;
	}

	@Override
	protected ByteBuffer onSend() {
		data.position(0);
		int pos = 0;
		for (NetVarField field : fields) {
			try {
				switch (field.type) {
					case BOOLEAN:
						data.put(pos, (byte) (field.field.getBoolean(object) ? 1 : 0));
						break;
					case BYTE:
						data.put(pos, field.field.getByte(object));
						break;
					case CHAR:
						data.putChar(pos, field.field.getChar(object));
						break;
					case DOUBLE:
						data.putDouble(pos, field.field.getDouble(object));
						break;
					case FLOAT:
						data.putFloat(pos, field.field.getFloat(object));
						break;
					case INT:
						data.putInt(pos, field.field.getInt(object));
						break;
					case LONG:
						data.putLong(pos, field.field.getLong(object));
						break;
					case SHORT:
						data.putShort(pos, field.field.getShort(object));
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
	protected void onReceive(ByteBuffer data, int index) {
		int pos = 0;
		for (NetVarField field : fields) {
			try {
				switch (field.type) {
					case BOOLEAN:
						field.field.setBoolean(object, data.get(index + pos) == 1);
						break;
					case BYTE:
						field.field.setByte(object, data.get(index + pos));
						break;
					case CHAR:
						field.field.setChar(object, data.getChar(index + pos));
						break;
					case DOUBLE:
						field.field.setDouble(object, data.getDouble(index + pos));
						break;
					case FLOAT:
						field.field.setFloat(object, data.getFloat(index + pos));
						break;
					case INT:
						field.field.setInt(object, data.getInt(index + pos));
						break;
					case LONG:
						field.field.setLong(object, data.getLong(index + pos));
						break;
					case SHORT:
						field.field.setShort(object, data.getShort(index + pos));
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
	public short getSize() {
		return size;
	}
}
