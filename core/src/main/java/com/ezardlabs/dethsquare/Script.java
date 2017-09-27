package com.ezardlabs.dethsquare;

/**
 * Base class for all interactive {@link Component Components}
 */
public class Script extends Component {
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
