package com.ezardlabs.dethsquare.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DesktopIOUtils implements IOUtils {

	public BufferedReader getReader(String path) {
		InputStream is = getInputStream(path);
		if (is == null) throw new NullPointerException("InputStream for path " + path + " is null");
		return new BufferedReader(new InputStreamReader(is));
	}

	public String[] listFileNames(String dirPath) {
		try (BufferedReader reader = getReader(dirPath + "/files.lsd")) {
			ArrayList<String> list = new ArrayList<>();
			String temp;
			while ((temp = reader.readLine()) != null) {
				list.add(temp);
			}
			return list.toArray(new String[list.size()]);
		} catch (IOException e) {
			System.err.println("Failed to load " + dirPath + " files.lsd");
			e.printStackTrace();
			return new String[0];
		}
	}

	@Override
	public InputStream getInputStream(String path) {
		return ClassLoader.getSystemResourceAsStream(path);
	}

	@Override
	public String[] getFileLines(String path) {
		try (BufferedReader reader = getReader(path)) {
			ArrayList<String> data = new ArrayList<>();
			String temp;
			while ((temp = reader.readLine()) != null) {
				data.add(temp);
			}
			return (data.toArray(new String[data.size()]));
		} catch (IOException e) {
			e.printStackTrace();
			return new String[0];
		}
	}
}
