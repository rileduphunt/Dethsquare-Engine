package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.Collider.Collision;
import com.ezardlabs.dethsquare.animation.Animator;
import com.ezardlabs.dethsquare.graphics.GraphicsEngine;
import com.ezardlabs.dethsquare.graphics.Renderer;
import com.ezardlabs.dethsquare.networking.AutoNetworkBehaviour;
import com.ezardlabs.dethsquare.networking.markers.Networked;
import com.ezardlabs.dethsquare.util.GameListeners;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ezardlabs.dethsquare.util.Dethsquare.AUDIO;

/**
 * Base class for all entities in the game world
 */
public final class GameObject implements Serializable {
	// Add hook into game loop
	static {
		GameListeners.addUpdateListener(GameObject::updateAll);
	}

	/**
	 * List of all {@link GameObject GameObjects} to be instantiated at the end of the current frame
	 */
	private static final ArrayList<GameObject> newObjects = new ArrayList<>();
	/**
	 * List of all {@link GameObject GameObjects} to be destroyed at the end of the current frame
	 */
	private static final HashMap<Long, GameObject> destroyedObjects = new HashMap<>();
//	private static final ArrayList<GameObject> destroyedObjects = new ArrayList<>();
	/**
	 * Root of all {@link GameObject GameObjects} currently in the game world
	 */
	static final GameObject rootObject = new GameObject("<root>");
	/**
	 * Structure containing all tags currently in use in the game world
	 */
	private static final HashMap<String, ArrayList<GameObject>> tags = new HashMap<>();
	/**
	 * List of all {@link GameObject GameObjects} whose components have been modified in some way since last frame
	 */
	private static final ArrayList<GameObject> objectsWithChangedComponents = new ArrayList<>();
	/**
	 * The name of the {@link GameObject}
	 */
	public String name;
	/**
	 * The tag of this {@link GameObject}. Tags are used to define generic groups of {@link GameObject GameObjects} (e.g. players, enemies, etc)
	 */
	private String tag;
	/**
	 * The layer that this {@link GameObject} is in. Must be in the range of [0..31]
	 */
	private int layer = 0;
	/**
	 * Equivalent to {@code 1 << }{@link #layer}
	 */
	private int layerMask = 1;
	/**
	 * Whether or not the {@link GameObject} is static, i.e. whether or not it will ever move.
	 * Static {@link GameObject GameObjects} can have their rendering and collision optimised
	 */
	public final boolean isStatic;
	/**
	 * List of {@link Component Components} currently attached to this {@link GameObject}
	 */
	private final ArrayList<Component> components = new ArrayList<>();
	/**
	 * List of {@link Script Scripts} currently attached to this {@link GameObject}
	 */
	private final ArrayList<Script> scripts = new ArrayList<>();
	/**
	 * List of {@link Component Components} that have been added to this {@link GameObject} since last frame
	 */
	private final ArrayList<Component> newComponents = new ArrayList<>();
	/**
	 * List of {@link Component Components} that have been removed from this {@link GameObject} since last frame
	 */
	private final ArrayList<Class<?>> removedComponents = new ArrayList<>();
	/**
	 * Fast access to this {@link GameObject}'s {@link Transform} component
	 */
	public Transform transform = new Transform(this);
	/**
	 * Fast access to this {@link GameObject}'s {@link Animator} component
	 */
	public Animator animator;
	/**
	 * Fast access to this {@link GameObject}'s {@link Renderer} component
	 */
	public Renderer renderer;
	/**
	 * Fast access to this {@link GameObject}'s {@link Collider} component
	 */
	public Collider collider;
	/**
	 * Fast access to this {@link GameObject}'s {@link Rigidbody} component
	 */
	public Rigidbody rigidbody;
	/**
	 * The unique identifier for this {@link GameObject}; only used when instantiated across
	 * the network
	 */
	public int networkId = -1;
	/**
	 * Whether or not this {@link GameObject} has been instantiated yet
	 */
	private boolean instantiated = false;
	/**
	 * Whether or not this {@link GameObject} is active
	 */
	private boolean active = true;
	/**
	 * Method that is run when this {@link GameObject} is destroyed
	 */
	private Runnable destructionCallback = null;

	public GameObject() {
		this(null);
	}

	public GameObject(String name, Component... components) {
		this(name, null, false, components);
	}

	public GameObject(String name, String tag) {
		this(name, tag, false);
	}

	public GameObject(String name, String tag, Component... components) {
		this(name, tag, false, components);
	}

	public GameObject(String name, boolean isStatic, Component... components) {
		this(name, null, isStatic, components);
	}

	public GameObject(String name, String tag, boolean isStatic, Component... components) {
		this.name = name;
		setTag(tag);
		this.isStatic = isStatic;
		addComponent(transform, instantiated);
		for (Component c : components) {
			addComponent(c, instantiated);
		}
	}

	private GameObject(String name, boolean isStatic, Transform transform) {
		this.name = name;
		this.isStatic = isStatic;
		this.transform = transform;
		addComponent(transform, instantiated);
	}

	/**
	 * Attaches a {@link Component} to this {@link GameObject}. If a {@link Component} of the same
	 * type is already present, then it is automatically replaced
	 *
	 * @param component The {@link Component} to attach to this {@link GameObject}
	 * @param <T>       The type of the {@link Component}
	 * @return The {@link Component} that has just been attached
	 */
	public <T extends Component> T addComponent(T component) {
		return addComponent(component, instantiated);
	}

	/**
	 * Attaches a {@link Component} to this {@link GameObject}. If a {@link Component} of the same
	 * type is already present, then it is automatically replaced
	 *
	 * @param component The {@link Component} to attach to this {@link GameObject}
	 * @param callStart Whether or not to call the {@link Component#start()} method on the newly
	 *                  added {@link Component}
	 * @param <T>       The type of the {@link Component}
	 * @return The {@link Component} that has just been attached
	 */
	private <T extends Component> T addComponent(T component, boolean callStart) {
		if (component instanceof AutoNetworkBehaviour || getComponent(component.getClass()) != null) {
			throw new ComponentAlreadyExistsError(component);
		}
		newComponents.add(component);
		if (component instanceof Networked) {
			AutoNetworkBehaviour autoNetworkBehaviour = new AutoNetworkBehaviour((Networked) component);
			component.setAutoNetworkBehaviour(autoNetworkBehaviour);
			newComponents.add(autoNetworkBehaviour);
		}
		if (callStart) {
			objectsWithChangedComponents.add(this);
		}
		return component;
	}

	/**
	 * Checks whether a {@link Component} of the given type is attached to
	 * this {@link GameObject}
	 *
	 * @param type The type of the {@link Component}
	 * @return True if the {@link Component} is attached; false otherwise
	 */
	public boolean hasComponent(Class<? extends Component> type) {
		for (Component c : components) {
			if (c.getClass().equals(type)) {
				return true;
			}
		}
		for (Component c : newComponents) {
			if (c.getClass().equals(type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether a {@link Component} of the given type or any subclass
	 * of that type is attached to this {@link GameObject}
	 *
	 * @param type The type of the {@link Component}
	 * @return True if the {@link Component} is attached; false otherwise
	 */
	public boolean hasComponentOfType(Class<? extends Component> type) {
		for (Component c : components) {
			if (type.isAssignableFrom(c.getClass())) {
				return true;
			}
		}
		for (Component c : newComponents) {
			if (type.isAssignableFrom(c.getClass())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the {@link Component} of the given type if the {@link GameObject} has one
	 * attached, null if it doesn't
	 *
	 * @param type The type of {@link Component} to retrieve
	 */
	public <T extends Component> T getComponent(Class<T> type) {
		for (Component c : components) {
			if (c.getClass().equals(type)) {
				return type.cast(c);
			}
		}
		for (Component c : newComponents) {
			if (c.getClass().equals(type)) {
				return type.cast(c);
			}
		}
		return null;
	}

	/**
	 * Gets the first {@link Component} of the given type
	 *
	 * @param type The type of the {@link Component} to get
	 * @param <T>  The type to automatically cast the result to
	 * @return The first {@link Component} of the given type
	 */
	public <T extends Component> T getComponentOfType(Class<T> type) {
		for (Component c : components) {
			if (type.isAssignableFrom(c.getClass())) {
				//noinspection unchecked
				return (T) c;
			}
		}
		for (Component c : newComponents) {
			if (type.isAssignableFrom(c.getClass())) {
				//noinspection unchecked
				return (T) c;
			}
		}
		return null;
	}

	/**
	 * Get all {@link Component}s of the given type
	 *
	 * @param type The type of the {@link Component}s to get
	 * @param <T>  The type to automatically cast the result to
	 * @return All {@link Component}s of the given type
	 */
	public <T extends Component> List<T> getComponentsOfType(Class<T> type) {
		//noinspection unchecked
		return Stream.concat(components.stream(), newComponents.stream())
					 .filter(c -> type.isAssignableFrom(c.getClass()))
					 .map(c -> (T) c)
					 .collect(Collectors.toList());
	}

	/**
	 * Removes the {@link Component} of the given type from this {@link GameObject} if it exists.
	 * If it doesn't exist then nothing happens
	 *
	 * @param type The type of {@link Component} to remove from this {@link GameObject}
	 * @return The removed {@link Component} if it existed; otherwise null
	 */
	public <T extends Component> T removeComponent(Class<T> type) {
		if (type.equals(Transform.class)) {
			throw new IllegalArgumentException("Cannot remove the Transform type of a GameObject");
		}
		removedComponents.add(type);
		objectsWithChangedComponents.add(this);
		for (Component c : components) {
			if (c.getClass().equals(type)) {
				//noinspection unchecked
				return (T) c;
			}
		}
		return null;
	}

	/**
	 * Sets the tag of this {@link GameObject}
	 *
	 * @param tag The tag to give to this {@link GameObject}
	 */
	public void setTag(String tag) {
		if (tag == null) {
			if (this.tag != null) {
				tags.get(this.tag).remove(this);
				if (tags.get(this.tag).size() == 0) {
					tags.remove(this.tag);
				}
			}
		} else {
			if (this.tag == null) {
				if (!tags.containsKey(tag)) {
					tags.put(tag, new ArrayList<>());
				}
				tags.get(tag).add(this);
			} else {
				tags.get(this.tag).remove(this);
				if (tags.get(this.tag).size() == 0) {
					tags.remove(this.tag);
				}
				if (!tags.containsKey(tag)) {
					tags.put(tag, new ArrayList<>());
				}
				tags.get(tag).add(this);
			}
		}
		this.tag = tag;
	}

	/**
	 * Gets the tag of this {@link GameObject}
	 *
	 * @return The tag of this {@link GameObject}
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Sets the layer that this {@link GameObject} is in
	 *
	 * @param layer The layer that this {@link GameObject} should be in. Must be in the range [0..31]
	 */
	public void setLayer(int layer) {
		if (layer < 0 || layer > 31) {
			throw new IllegalArgumentException("Layer must be in the range [0..31]");
		}
		this.layer = layer;
		this.layerMask = 1 << layer;
	}

	/**
	 * Gets the layer that this {@link GameObject} is in
	 *
	 * @return The layer that this {@link GameObject} is in
	 */
	public int getLayer() {
		return layer;
	}

	/**
	 * Gets the layer mask for this {@link GameObject}
	 *
	 * @return This {@link GameObject}'s layer mask
	 */
	public int getLayerMask() {
		return layerMask;
	}

	/**
	 * Activates/deactivates this {@link GameObject}
	 *
	 * @param active Whether to activate or deactivate this {@link GameObject}
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Whether or not this {@link GameObject} is active. Note that the {@link GameObject} will only be treated as
	 * active in the game world iff it is active and all parent {@link GameObject GameObjects} are active
	 *
	 * @return Whether or not this {@link GameObject} is active
	 */
	public boolean isActiveSelf() {
		return active;
	}

	/**
	 * Whether or not this {@link GameObject} is active and all of its parent {@link GameObject GameObjects} are
	 * active. Note that the {@link GameObject} will only be treated as active in the game world iff it is active and
	 * all parent {@link GameObject GameObjects} are active
	 *
	 * @return Whether or not this {@link GameObject} is active and all of its parent {@link GameObject GameObjects}
	 * are active
	 */
	public boolean isActive() {
		if (transform.getParent() != null) {
			return active && transform.getParent().gameObject.isActive();
		} else {
			return active;
		}
	}

	/**
	 * Called when another {@link Collider} has entered this {@link GameObject GameObject's} trigger {@link Collider}
	 *
	 * @param collider The other {@link Collider} that has entered this {@link GameObject GameObject's} trigger {@link Collider}
	 */
	void onTriggerEnter(Collider collider) {
		for (Component component : components) {
			component.onTriggerEnter(collider);
		}
	}

	void onCollision(Collision collision) {
		for (int i = 0; i < components.size(); i++) {
			components.get(i).onCollision(collision);
		}
	}

	private void update() {
		if (Time.isPaused()) {
			scripts.stream().filter(script -> script.enabled && script.runWhenPaused).forEach(Script::update);
		} else {
			scripts.stream().filter(script -> script.enabled).forEach(Script::update);
		}
		transform.children.stream()
						  .filter(child -> child.gameObject.active)
						  .map(child -> child.gameObject)
						  .forEachOrdered(GameObject::update);
	}

	/**
	 * Create the given {@link GameObject} within the game world
	 *
	 * @param gameObject The {@link GameObject} to create
	 * @param position   The coordinates to create the given {@link GameObject} at
	 * @return The {@link GameObject} that was passed into this method as a parameter
	 */
	public static GameObject instantiate(GameObject gameObject, Vector2 position) {
		return instantiate(gameObject, position, new Vector2(1, 1));
	}

	/**
	 * Create the given {@link GameObject} within the game world
	 *
	 * @param gameObject The {@link GameObject} to create
	 * @param position   The coordinates to create the given {@link GameObject} at
	 * @param scale      The scale to create the {@link GameObject} at
	 * @return The {@link GameObject} that was passed into this method as a parameter
	 */
	public static GameObject instantiate(GameObject gameObject, Vector2 position, Vector2 scale) {
		gameObject.transform.position.set(position);
		gameObject.transform.scale.set(scale);
		GameObject go = gameObject.copy();
		newObjects.add(go);
		objectsWithChangedComponents.add(go);
		go.instantiated = true;
		return go;
	}

	/**
	 * Removes the given {@link GameObject} from the game world
	 *
	 * @param gameObject The {@link GameObject} to remove from the game world
	 */
	public static void destroy(GameObject gameObject) {
		destroy(gameObject, 0);
	}

	/**
	 * Removes the given {@link GameObject} from the game world after a given time period has elapsed
	 *
	 * @param gameObject The {@link GameObject} to remove from the game world
	 * @param delay      The time period to wait before the given {@link GameObject} is destroyed
	 */
	public static void destroy(GameObject gameObject, long delay) {
		destroy(gameObject, delay, null);
	}

	public static void destroy(GameObject gameObject, long delay, Runnable callback) {
		gameObject.destructionCallback = callback;
		destroyedObjects.put(System.nanoTime() + delay * 1000000L, gameObject);
	}

	private void searchDestroyNetworked(int networkId) {
		if (this.networkId == networkId) {
			destroy(this);
		} else if (!transform.children.isEmpty()) {
			for (Transform child : transform.children) {
				child.gameObject.searchDestroyNetworked(networkId);
			}
		}
	}

	/**
	 * Removes the {@link GameObject} with the given network ID from the game world
	 *
	 * @param networkId The network ID of the {@link GameObject} to remove from the game world
	 */
	public static void destroy(int networkId) {
		if (networkId < 0) return;
		rootObject.searchDestroyNetworked(networkId);
	}

	static void startAll() {
		handleCreationDestruction();
	}

	private static void updateAll() {
		handleCreationDestruction();

		rootObject.update();
	}

	static void destroyAll() {
		rootObject.transform.children.clear();
		newObjects.clear();
		destroyedObjects.clear();
		objectsWithChangedComponents.clear();
		GraphicsEngine.clearAll();
		GraphicsEngine.clearQuadTree();
		GraphicsEngine.destroyAllTextures();
		TextureAtlas.clearCache();
		Collider.clearAll();
		AUDIO.destroyAll();
		AudioManager.clearAll();
	}

	/**
	 * Finds all {@link GameObject GameObjects} with the given tag
	 *
	 * @param tag The name of the tag to search all objects for
	 * @return an array of all {@link GameObject GameObjects} with the given tag
	 */
	public static GameObject[] findAllWithTag(String tag) {
		if (tags.containsKey(tag)) {
			return tags.get(tag).toArray(new GameObject[tags.get(tag).size()]);
		} else {
			return new GameObject[0];
		}
	}

	private static void handleDestruction() {
		destroyedObjects.entrySet()
						.stream()
						.filter(entry -> entry.getKey() <= System.nanoTime())
						.forEachOrdered(entry -> {
							GameObject gameObject = entry.getValue();
							if (gameObject != null) {
								for (Component c : gameObject.components) {
									c.internalDestroy();
									c.destroy();
								}
								gameObject.transform.parent.children.remove(gameObject.transform);
								newObjects.remove(gameObject);
								objectsWithChangedComponents.remove(gameObject);
								if (gameObject.destructionCallback != null) {
									gameObject.destructionCallback.run();
								}
								entry.setValue(null);
							}
						});
		destroyedObjects.entrySet().removeIf(entry -> entry.getValue() == null);
	}

	private static void handleRemovedComponents(GameObject go) {
		for (Class clazz : go.removedComponents) {
			for (Component c : go.components.toArray(new Component[go.components.size()])) {
				if (c.getClass().equals(clazz)) {
					if (c instanceof Renderer) {
						go.renderer = null;
					} else if (c instanceof Animator) {
						go.animator = null;
					} else if (c instanceof Collider) {
						go.collider = null;
					} else if (c instanceof Rigidbody) {
						go.rigidbody = null;
					}
					go.components.remove(c);
					if (c instanceof Script) {
						go.scripts.remove(c);
					}
				}
			}
		}
		go.removedComponents.clear();
	}

	private static ArrayList<Component> handleNewComponents(GameObject go) {
		ArrayList<Component> temp = new ArrayList<>();
		for (Component component : go.newComponents) {
			if (component instanceof Transform) {
				go.transform = (Transform) component;
			} else if (component instanceof Renderer) {
				go.renderer = (Renderer) component;
			} else if (component instanceof Animator) {
				go.animator = (Animator) component;
			} else if (component instanceof Collider) {
				go.collider = (Collider) component;
			} else if (component instanceof Rigidbody) {
				go.rigidbody = (Rigidbody) component;
			}
			if (component instanceof Script) {
				go.scripts.add((Script) component);
			}
			component.gameObject = go;
			component.transform = go.transform;
			go.components.add(component);
			temp.add(component);
		}
		go.newComponents.clear();
		return temp;
	}

	private static void handleCreationDestruction() {
		handleDestruction();

		newObjects.parallelStream()
				  .filter(gameObject -> gameObject.transform.parent == null)
				  .forEachOrdered(gameObject -> gameObject.transform.setParent(rootObject.transform));

		ArrayList<Component> temp = new ArrayList<>();

		for (GameObject go : objectsWithChangedComponents) {
			handleRemovedComponents(go);

			temp.addAll(handleNewComponents(go));
		}

		objectsWithChangedComponents.clear();
		newObjects.clear();

		for (Component component : temp) {
			component.internalStart();
			component.start();
		}
	}

	private GameObject copy() {
		GameObject gameObject = new GameObject(name, isStatic, transform);
		transform.gameObject = gameObject;
		Stream.concat(components.stream(), newComponents.stream()).forEach(c -> {
			if (!(c instanceof Transform)) gameObject.addComponent(c);
		});
		String tag = this.tag;
		setTag(null);
		gameObject.setTag(tag);
		gameObject.networkId = networkId;
		return gameObject;
	}

	private static class ComponentAlreadyExistsError extends Error {

		private ComponentAlreadyExistsError(Component component) {
			super("Component of type " + component.getClass() + " already exists on this GameObject");
		}
	}
}