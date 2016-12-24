package com.ezardlabs.dethsquare.util;

import java.io.BufferedReader;
import java.io.InputStream;

public interface IOUtils {
	BufferedReader getReader(String path);

	String[] listFileNames(String dirPath);

	InputStream getInputStream(String path);
}
