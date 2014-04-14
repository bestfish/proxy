package com.fish.play.proxy.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class EncodingUtil {

	public static String getAsciiString(final byte[] data, int offset,
			int length) throws IOException {

		if (data == null) {
			throw new IllegalArgumentException("Parameter may not be null");
		}

		try {
			return new String(data, offset, length, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedEncodingException(
					"It requires ASCII support");
		}
	}

	public static String getAsciiString(final byte[] data) throws IOException {
		return getAsciiString(data, 0, data.length);
	}
	
	

}
