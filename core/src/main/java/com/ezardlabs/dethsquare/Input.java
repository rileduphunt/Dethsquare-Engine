package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.Touch.TouchPhase;
import com.ezardlabs.dethsquare.util.Dethsquare;
import com.ezardlabs.dethsquare.util.GameListeners;
import com.ezardlabs.dethsquare.util.GameListeners.GamepadListener;
import com.ezardlabs.dethsquare.util.GameListeners.KeyListener;
import com.ezardlabs.dethsquare.util.GameListeners.MouseListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public final class Input {
	static {
		GameListeners.addMouseListener(new MouseListener() {
			@Override
			public void onMove(int x, int y) {
				mousePosition.set(x, y);
			}

			@Override
			public void onButtonDown(int index) {
				switch (index) {
					case 1:
						setKeyDown(KeyCode.MOUSE_LEFT);
						break;
					case 2:
						setKeyDown(KeyCode.MOUSE_MIDDLE);
						break;
					case 3:
						setKeyDown(KeyCode.MOUSE_RIGHT);
						break;
					default:
						break;
				}
			}

			@Override
			public void onButtonUp(int index) {
				switch (index) {
					case 1:
						setKeyUp(KeyCode.MOUSE_LEFT);
						break;
					case 2:
						setKeyUp(KeyCode.MOUSE_MIDDLE);
						break;
					case 3:
						setKeyUp(KeyCode.MOUSE_RIGHT);
						break;
					default:
						break;
				}
			}
		});
		GameListeners.addKeyListener(new KeyListener() {
			@Override
			public void onKeyDown(String key) {
				setKeyDown(KeyCode.valueOf(key));
			}

			@Override
			public void onKeyUp(String key) {
				setKeyUp(KeyCode.valueOf(key));
			}
		});
		GameListeners.addGamepadListener(new GamepadListener() {
			@Override
			public void onConnectionStateChanged(boolean connected) {
				controllerConnected = connected;
			}

			@Override
			public void onButtonDown(String button) {

			}

			@Override
			public void onButtonUp(String button) {

			}

			@Override
			public void onAxis(String axis, float value) {
			}
		});
		GameListeners.addUpdateListener(Input::update);

	}

	public static final Vector2 mousePosition = new Vector2();

	public static Touch[] touches = new Touch[0];

	/**
	 * Each key that has been pressed either has a value of true if it has been pressed down in the current frame, or false if it's been held down for multiple frames
	 */
	private static HashMap<KeyCode, Integer> keys = new HashMap<>();
	private static HashMap<KeyCode, Integer> keyChanges = new HashMap<>();

	private static HashMap<ControlCode, ArrayList> keyControls = new HashMap<>();
	private static boolean controlsInitialized = false;

	private static boolean controllerConnected = false;
	private static HashMap<Button, Integer> buttons = new HashMap<>();
	private static HashMap<Axis, Vector2> axes = new HashMap<>();

	public enum Button {
		A,
		B,
		X,
		Y,
		UP,
		DOWN,
		LEFT,
		RIGHT,
		THUMB_LEFT,
		THUMB_RIGHT,
		SHOULDER_LEFT,
		SHOULDER_RIGHT,
		START,
		BACK,
		GUIDE
	}

	public enum Axis {
		LEFT,
		RIGHT,
		LEFT_RAW,
		RIGHT_RAW,
		TRIGGER_LEFT,
		TRIGGER_RIGHT
	}

	private static ArrayList<Holder> changesToMake = new ArrayList<>(10);
	private static ArrayList<Touch> touchesToRemove = new ArrayList<>(10);

	public enum KeyCode {
		A,
		B,
		C,
		D,
		E,
		F,
		G,
		H,
		I,
		J,
		K,
		L,
		M,
		N,
		O,
		P,
		Q,
		R,
		S,
		T,
		U,
		V,
		W,
		X,
		Y,
		Z,
		ALPHA_0,
		ALPHA_1,
		ALPHA_2,
		ALPHA_3,
		ALPHA_4,
		ALPHA_5,
		ALPHA_6,
		ALPHA_7,
		ALPHA_8,
		ALPHA_9,
		SPACE,
		ENTER,
		ESCAPE,
		BACKSPACE,
		DELETE,
		LEFT,
		RIGHT,
		UP,
		DOWN,
		F1,
		F2,
		F3,
		F4,
		F5,
		F6,
		F7,
		F8,
		F9,
		F10,
		F11,
		F12,
		MOUSE_LEFT,
		MOUSE_RIGHT,
		MOUSE_MIDDLE
	}

	public enum ControlCode
	{
		ABILITY_1,
		ABILITY_2,
		ABILITY_3,
		ABILITY_4,

		MELEE,
		SHOOT,

		JUMP,
		CROUCH,
		LEFT,
		RIGHT
	}




	private static class Holder {
		int id;
		float x;
		float y;
		TouchPhase phase;

		Holder(int id, float x, float y, TouchPhase phase) {
			this.id = id;
			this.x = x;
			this.y = y;
			this.phase = phase;
		}
	}

	public static void update() {
		switch (Dethsquare.PLATFORM) {
			case ANDROID:
				updateTouches();
				break;
			case DESKTOP:
				updateKeys();
				break;
			default:
				break;
		}
	}

	private static void updateTouches() {
		Holder holder;
		for (int i = 0; i < changesToMake.size(); i++) {
			switch ((holder = changesToMake.get(i)).phase) {
				case BEGAN:
					Touch[] temp = new Touch[touches.length + 1];
					System.arraycopy(touches, 0, temp, 0, touches.length);
					temp[touches.length] = new Touch(holder.id, new Vector2(holder.x, holder.y));
					touches = temp;
					break;
				case MOVED:
					for (Touch t : touches) {
						if (t.fingerId == holder.id && t.lastModified < Time.frameCount) {
							t.phase = Touch.TouchPhase.MOVED;
							t.position.set(holder.x, holder.y);
							t.lastModified = Time.frameCount;
						}
					}
					break;
				case STATIONARY:
					break;
				case ENDED:
					for (Touch t : touches) {
						if (t.fingerId == holder.id) {
							t.phase = Touch.TouchPhase.ENDED;
							t.position.set(holder.x, holder.y);
							t.lastModified = Time.frameCount;
						}
					}
					break;
				case CANCELLED:
					for (Touch t : touches) {
						if (t.fingerId == holder.id) {
							t.phase = Touch.TouchPhase.CANCELLED;
							t.position.set(holder.x, holder.y);
							t.lastModified = Time.frameCount;
						}
					}
					break;
				default:
					break;
			}
		}

		changesToMake.clear();
	}

	private static void updateKeys() {
		for (KeyCode keyCode : keys.keySet().toArray(new KeyCode[keys.size()])) {
			switch (keys.get(keyCode)) {
				case 0:
					keys.put(keyCode, 1);
					break;
				case 2:
					keys.remove(keyCode);
					break;
				default:
					break;
			}
		}
		keys.putAll(keyChanges);
		keyChanges.clear();
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < touches.length; i++) {
			if ((touches[i].phase == Touch.TouchPhase.ENDED || touches[i].phase == Touch.TouchPhase.CANCELLED) &&
					touches[i].lastModified < Time.frameCount) {
				touchesToRemove.add(touches[i]);
			} else if (touches[i].lastModified < Time.frameCount) {
				touches[i].lastModified = Time.frameCount;
				touches[i].phase = Touch.TouchPhase.STATIONARY;
			}
		}
		if (!touchesToRemove.isEmpty()) {
			Touch[] temp = new Touch[touches.length - touchesToRemove.size()];
			int count = 0;
			//noinspection ForLoopReplaceableByForEach
			for (int i = 0; i < touches.length; i++) {
				if (!touchesToRemove.contains(touches[i])) {
					temp[count++] = touches[i];
				}
			}
			touchesToRemove.clear();
			touches = temp;
		}
	}

	static void clearAll() {
		keys.clear();
		keyChanges.clear();
		changesToMake.clear();
		touches = new Touch[0];
		touchesToRemove.clear();
		if (!controlsInitialized){
			defaultControls();
		}
	}



	private static void setKeyDown(KeyCode keyCode) {
		if (!keys.containsKey(keyCode)) keyChanges.put(keyCode, 0);
	}

	private static void setKeyUp(KeyCode keyCode) {
		keyChanges.put(keyCode, 2);
	}

	public static boolean getKeyDown(KeyCode keyCode) {
		return keys.containsKey(keyCode) && keys.get(keyCode) == 0;
	}

	public static boolean getKey(KeyCode keyCode) {
		return keys.containsKey(keyCode) && keys.get(keyCode) < 2;
	}

	public static boolean getKeyUp(KeyCode keyCode) {
		return keys.containsKey(keyCode) && keys.get(keyCode) == 2;
	}

	private static int getControlValue(ControlCode control)
	{
		int smallest = 2;
		for (Object object: keyControls.get(control)) {
			if (object instanceof KeyCode) {
				if (keys.get((KeyCode)object) != null) {
					int temp = getKey((KeyCode)object) ? (getKeyDown((KeyCode)object) ? 0 : 1 ) : 2;
					smallest = (smallest > temp) ? temp : smallest;
				}
			} else if (object instanceof Button) {
				// not sure what to do with these for now.
			} else if (object instanceof Axis){
				// DEFINITELY not sure how to handle axes
			} else {
				throw new IllegalArgumentException("Control must be an Axis, Button, or KeyCode.");
			}
		}
		return smallest;
	}

	public static boolean getControlDown(ControlCode control){
		return getControlValue(control) == 0;
	}

	public static boolean getControl(ControlCode control){
		return getControlValue(control) < 2;
	}

	public static boolean getControlUp(ControlCode control){
		return getControlValue(control) == 2;
	}

	public static void defaultControls(){
		keyControls.put(ControlCode.SHOOT, new ArrayList<>(Arrays.asList(KeyCode.MOUSE_RIGHT,KeyCode.Z)));
		keyControls.put(ControlCode.MELEE, new ArrayList<>(Arrays.asList(KeyCode.MOUSE_LEFT,KeyCode.X)));

		keyControls.put(ControlCode.JUMP, new ArrayList<>(Arrays.asList(KeyCode.SPACE,KeyCode.W,KeyCode.UP)));
		keyControls.put(ControlCode.CROUCH, new ArrayList<>(Arrays.asList(KeyCode.S,KeyCode.DOWN)));
		keyControls.put(ControlCode.LEFT, new ArrayList<>(Arrays.asList(KeyCode.A,KeyCode.LEFT)));
		keyControls.put(ControlCode.RIGHT, new ArrayList<>(Arrays.asList(KeyCode.D,KeyCode.RIGHT)));

		keyControls.put(ControlCode.ABILITY_1, new ArrayList<>(Arrays.asList(KeyCode.ALPHA_1)));
		keyControls.put(ControlCode.ABILITY_2, new ArrayList<>(Arrays.asList(KeyCode.ALPHA_2)));
		keyControls.put(ControlCode.ABILITY_3, new ArrayList<>(Arrays.asList(KeyCode.ALPHA_3)));
		keyControls.put(ControlCode.ABILITY_4, new ArrayList<>(Arrays.asList(KeyCode.ALPHA_4)));
	}

	public static void addTouch(int id, float x, float y) {
		changesToMake.add(new Holder(id, x, y, TouchPhase.BEGAN));
	}

	public static void moveTouch(int id, float x, float y) {
		changesToMake.add(new Holder(id, x, y, TouchPhase.MOVED));
	}

	public static void removeTouch(int id, float x, float y) {
		changesToMake.add(new Holder(id, x, y, TouchPhase.ENDED));
	}

	public static void cancelTouch(int id, float x, float y) {
		changesToMake.add(new Holder(id, x, y, TouchPhase.CANCELLED));
	}
}