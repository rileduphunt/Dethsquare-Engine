package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.Collider.Collision;
import com.ezardlabs.dethsquare.networking.AutoNetworkBehaviour;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Base class for everything attached to {@link GameObject GameObjects}
 */
public class Component {
	/**
	 * The {@link GameObject} this component is attached to. A component is always attached to a
	 * {@link GameObject}
	 */
	public GameObject gameObject;
	/**
	 * The {@link Transform} attached to the {@link GameObject} that this {@link Component} is also
	 * attached to (null if there is none attached)
	 */
	public Transform transform;
	/**
	 * Whether or not this {@link Component} is enabled
	 */
	public boolean enabled = true;
	/**
	 * The {@link AutoNetworkBehaviour} that relies on this {@link Component}
	 */
	private AutoNetworkBehaviour autoNetworkBehaviour;

	void internalStart() {
		for (Annotation a : getClass().getAnnotations()) {
			if (a instanceof RequiredComponents) {
				for (Class<? extends Component> c : ((RequiredComponents) a).value()) {
					if (!gameObject.hasComponentOfType(c)) {
						throw new RequiredComponentNotPresentError(gameObject.name, getClass(), c);
					}
				}
				break;
			}
		}
		start();
	}

	/**
	 * Called when this {@link Component} is first created
	 */
	public void start() {
		// Only used in subclasses
	}

	/**
	 * Called when this {@link Component} is destroyed
	 */
	protected void destroy() {
		// Only used in subclasses
	}

	/**
	 * Called when the {@link Collider} other enters the trigger
	 *
	 * @param other The other {@link Collider} involved in the collision
	 */
	public void onTriggerEnter(Collider other) {
		// Only used in subclasses
	}

	/**
	 * Called when this {@link Component}'s {@link Collider} collides with another {@link Collider}
	 *
	 * @param collision the {@link Collision} data associated with this collision
	 */
	public void onCollision(Collision collision) {
		// Only used in subclasses
	}

	void setAutoNetworkBehaviour(AutoNetworkBehaviour autoNetworkBehaviour) {
		this.autoNetworkBehaviour = autoNetworkBehaviour;
	}

	AutoNetworkBehaviour getAutoNetworkBehaviour() {
		return autoNetworkBehaviour;
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface RequiredComponents {
		Class<? extends Component>[] value();
	}

	private static class RequiredComponentNotPresentError extends Error {

		private RequiredComponentNotPresentError(String gameObjectName, Class component, Class requiredComponent) {
			super(gameObjectName + ": Could not create " + addIndefiniteArticle(component.getSimpleName()) +
					" because it requires " + addIndefiniteArticle(requiredComponent.getSimpleName()) +
					", which was missing");
		}

		private static String addIndefiniteArticle(String str) {
			switch (str.charAt(0)) {
				case 'a':
				case 'e':
				case 'i':
				case 'o':
				case 'u':
				case 'A':
				case 'E':
				case 'I':
				case 'O':
				case 'U':
					return "an '" + str + "'";
				default:
					return "a '" + str + "'";
			}
		}
	}
}
