package com.ezardlabs.dethsquare.util;

public interface AudioUtils {

	void create(int id, String path);

	void play(int id);

	void pause(int id);

	void stop(int id);

	void setLoop(int id, boolean loop);

	void setVolume(int id, int volume);

	void destroy(int id);

	void destroyAll();
}
