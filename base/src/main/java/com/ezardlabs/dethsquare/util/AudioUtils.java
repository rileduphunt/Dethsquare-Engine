package com.ezardlabs.dethsquare.util;

/**
 * Interface for managing audio in a platform-specific way
 */
public interface AudioUtils {
	/**
	 * Create and initialise the audio from the given path
	 *
	 * @param id   The ID of the audio clip
	 * @param path The path to the audio file
	 */
	void create(int id, String path);

	/**
	 * Play the audio file associated with the given audio clip ID
	 *
	 * @param id The ID of the audio clip
	 */
	void play(int id);

	/**
	 * Pause the audio file associated with the given audio clip ID
	 *
	 * @param id The ID of the audio clip
	 */
	void pause(int id);

	/**
	 * Stop the audio file associated with the given audio clip ID
	 *
	 * @param id The ID of the audio clip
	 */
	void stop(int id);

	/**
	 * Specify whether the audio clip with the given ID should loop or not
	 *
	 * @param id   The ID of the audio clip
	 * @param loop Whether to loop or not
	 */
	void setLoop(int id, boolean loop);

	/**
	 * Specify the volume of the audio clip with the given ID
	 *
	 * @param id     The ID of the audio clip
	 * @param volume The volume to set
	 */
	void setVolume(int id, float volume);

	/**
	 * Destroy the audio file associated with the given audio clip ID
	 *
	 * @param id The ID of the audio clip
	 */
	void destroy(int id);

	/**
	 * Free all memory currently being used for managing, storing and
	 * playing audio
	 */
	void destroyAll();

	/**
	 * Cleanly shutdown OpenAL
	 */
	void shutdown();
}
