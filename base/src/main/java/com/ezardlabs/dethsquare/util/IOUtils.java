package com.ezardlabs.dethsquare.util;

import java.io.BufferedReader;

public interface IOUtils {
	BufferedReader getReader(String path);

	String[] listFileNames(String dirPath);
}
