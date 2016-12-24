package com.ezardlabs.dethsquare.util;

import com.ezardlabs.dethsquare.util.Utils.ResourceNotFoundError;

import org.apache.commons.io.IOUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

import static com.ezardlabs.dethsquare.util.Utils.IO;
import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_SIZE;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetBufferi;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.Stdlib.free;

public class DesktopAudioUtils implements AudioUtils {
	private static HashMap<Integer, int[]> audio = new HashMap<>();

	public DesktopAudioUtils() {
		String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		long device = alcOpenDevice(defaultDeviceName);

		int[] attributes = {0};
		long context = alcCreateContext(device, attributes);
		alcMakeContextCurrent(context);

		AL.createCapabilities(ALC.createCapabilities(device));
	}

	public void create(int id, String path) {
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

		alGetBufferi(bufferPointer, AL_SIZE);

		int sourcePointer = alGenSources();

		// Assign our buffer to the source
		alSourcei(sourcePointer, AL_BUFFER, bufferPointer);

		audio.put(id, new int[]{sourcePointer, bufferPointer});
	}

	public void play(int id) {
		alSourcePlay(audio.get(id)[0]);
	}

	public void pause(int id) {
		alSourcePause(audio.get(id)[0]);
	}

	public void stop(int id) {
		alSourceStop(audio.get(id)[0]);
	}

	public void setLoop(int id, boolean loop) {
		alSourcei(audio.get(id)[0], AL_LOOPING, loop ? 1 : 0);
	}

	public void setVolume(int id, int volume) {
		alSourcef(audio.get(id)[0], AL_GAIN, volume / 100f);
	}

	public void destroy(int id) {
		int[] data = audio.remove(id);
		alDeleteSources(data[0]);
		alDeleteBuffers(data[1]);
	}

	public void destroyAll() {
		audio.keySet().forEach(this::destroy);
		audio.clear();
	}

	private static ByteBuffer loadAudio(String path) throws IOException {
		byte[] bytes = IOUtils.toByteArray(IO.getInputStream(path));
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
		buffer.put(bytes);
		buffer.flip();
		return buffer;
	}
}
