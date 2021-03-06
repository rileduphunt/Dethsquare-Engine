package com.ezardlabs.dethsquare.util;

import com.ezardlabs.dethsquare.util.Dethsquare.ResourceNotFoundError;

import org.apache.commons.io.IOUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

import static com.ezardlabs.dethsquare.util.Dethsquare.IO;
import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_INVERSE_DISTANCE_CLAMPED;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_MAX_DISTANCE;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_REFERENCE_DISTANCE;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alListener3f;
import static org.lwjgl.openal.AL10.alSource3f;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class DesktopAudioUtils implements AudioUtils {
	private static HashMap<Integer, Integer> audio = new HashMap<>();
	private static HashMap<String, Integer> mapping = new HashMap<>();
	private final long device;

	DesktopAudioUtils() {
		String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		device = alcOpenDevice(defaultDeviceName);

		int[] attributes = {0};
		long context = alcCreateContext(device, attributes);
		alcMakeContextCurrent(context);

		AL.createCapabilities(ALC.createCapabilities(device));

		alDistanceModel(AL_INVERSE_DISTANCE_CLAMPED);
	}

	public void create(int id, String path) {
		int bufferPointer;
		if (mapping.containsKey(path)) {
			bufferPointer = mapping.get(path);
		} else {
			bufferPointer = createBuffer(path);
		}

		int sourcePointer = alGenSources();

		alSourcei(sourcePointer, AL_BUFFER, bufferPointer);
		alSourcei(sourcePointer, AL_REFERENCE_DISTANCE, 500);
		alSourcei(sourcePointer, AL_MAX_DISTANCE, 4000);

		audio.put(id, sourcePointer);
		mapping.put(path, bufferPointer);
	}

	private int createBuffer(String path) {
		// Allocate space to store return information from the function
		stackPush();
		IntBuffer channelsBuffer = stackMallocInt(1);
		stackPush();
		IntBuffer sampleRateBuffer = stackMallocInt(1);

		ByteBuffer data;
		try {
			data = loadAudio(path);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceNotFoundError(path);
		}

		ShortBuffer rawAudioBuffer = stb_vorbis_decode_memory(data, channelsBuffer, sampleRateBuffer);

		// Retrieve the extra information that was stored in the buffers by the function
		int channels = channelsBuffer.get();
		int sampleRate = sampleRateBuffer.get();
		//Free the space we allocated earlier
		stackPop();
		stackPop();

		// Find the correct OpenAL format
		int format = -1;
		if (channels == 1) {
			format = AL_FORMAT_MONO16;
		} else if (channels == 2) {
			format = AL_FORMAT_STEREO16;
		}

		// Request space for the buffer
		int bufferPointer = alGenBuffers();

		// Send the data to OpenAL
		alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);

		// Free the memory allocated by STB
		free(rawAudioBuffer);

		return bufferPointer;
	}

	public void play(int id) {
		if (audio.containsKey(id)) {
			alSourcePlay(audio.get(id));
		}
	}

	public void pause(int id) {
		if (audio.containsKey(id)) {
			alSourcePause(audio.get(id));
		}
	}

	public void stop(int id) {
		if (audio.containsKey(id)) {
			alSourceStop(audio.get(id));
		}
	}

	public void setLoop(int id, boolean loop) {
		if (audio.containsKey(id)) {
			alSourcei(audio.get(id), AL_LOOPING, loop ? 1 : 0);
		}
	}

	public void setVolume(int id, float volume) {
		if (audio.containsKey(id)) {
			alSourcef(audio.get(id), AL_GAIN, volume);
		}
	}

	@Override
	public void setAudioPosition(int id, float x, float y) {
		if (audio.containsKey(id)) {
			alSource3f(audio.get(id), AL_POSITION, x, y, 0);
		}
	}

	@Override
	public void setListenerPosition(float x, float y) {
		alListener3f(AL_POSITION, x, y, 0);
	}

	public void destroy(int id) {
		if (audio.containsKey(id)) {
			alDeleteSources(audio.remove(id));
		}
	}

	public void destroyAll() {
		audio.values().forEach(AL10::alDeleteSources);
		audio.clear();
		mapping.values().forEach(AL10::alDeleteBuffers);
		mapping.clear();
	}

	@Override
	public void shutdown() {
		alcCloseDevice(device);
	}

	private static ByteBuffer loadAudio(String path) throws IOException {
		byte[] bytes = IOUtils.toByteArray(IO.getInputStream(path));
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
		buffer.put(bytes);
		buffer.flip();
		return buffer;
	}
}
