package com.ezardlabs.dethsquare.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DesktopIOUtils implements IOUtils {

	public BufferedReader getReader(String path) {
		System.out.println(path + ", " + getInputStream(path));
		return new BufferedReader(new InputStreamReader(getInputStream(path)));
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
}
