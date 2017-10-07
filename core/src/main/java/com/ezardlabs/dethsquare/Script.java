package com.ezardlabs.dethsquare;

/**
 * Base class for all interactive {@link Component Components}
 */
public class Script extends Component {
	/**
	 * Whether or not this {@link Script} should have its {@link #update()} method called every frame
	 */
	public boolean enabled = true;
	/**
	 * Determines whether or not this class' {@link #update()} method should be called even when the game has been
	 * paused using {@link Time#pause()}
	 */
	protected boolean runWhenPaused = false;

	/**
	 * Called every frame
	 */
	public void update() {
		// Only used in subclasses
	}
}
