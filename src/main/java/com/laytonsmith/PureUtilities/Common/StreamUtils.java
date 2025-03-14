package com.laytonsmith.PureUtilities.Common;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Streams are hard sometimes. This class abstracts most of the functionality that is commonly used.
 *
 */
public class StreamUtils {

	/**
	 * Copies from one stream to another
	 *
	 * @param out
	 * @param in
	 * @throws IOException
	 */
	public static void Copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while(len != -1) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
	}

	/**
	 * Given an input stream, a UTF-8 encoded string is returned, which is a reasonable assumption for most textual
	 * data. This assumes that the stream is finite, i.e. not a streaming socket, for instance, and reads until the
	 * stream reaches the end.
	 *
	 * @param out
	 * @return
	 */
	public static String GetString(InputStream in) {
		try {
			return GetString(in, "UTF-8");
		} catch(UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
	}

	/**
	 * Gets a string from an input stream, assuming the given encoding. This assumes that the stream is finite, i.e. not
	 * a streaming socket, for instance, and reads until the stream reaches the end.
	 *
	 * @param in
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String GetString(InputStream in, String encoding) throws UnsupportedEncodingException {
		if(encoding == null) {
			encoding = "UTF-8";
		}
		if(in == null) {
			throw new NullPointerException("InputStream is null");
		}
		InputStreamReader input;
		input = new InputStreamReader(new BufferedInputStream(in), encoding);
		final int charsPerPage = 5000; //counting spaces
		final char[] buffer = new char[charsPerPage];
		StringBuilder output = new StringBuilder(charsPerPage);
		try {
			for(int read = input.read(buffer, 0, buffer.length);
					read != -1;
					read = input.read(buffer, 0, buffer.length)) {
				output.append(buffer, 0, read);
			}
		} catch(IOException ignore) {
		}

		return output.toString();

	}

	/**
	 * Fully reads in a stream, as efficiently as possible, and returns a byte array. The input stream is not closed
	 * afterwards.
	 *
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] GetBytes(InputStream in) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(in);
		try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			int i;
			byte[] buffer = new byte[8 * 1024];
			while((i = bis.read(buffer)) != -1) {
				out.write(buffer, 0, i);
			}
			return out.toByteArray();
		}
	}

	/**
	 * Assuming a UTF-8 encoded string is provided, returns an InputStream for that String.
	 *
	 * @param contents
	 * @return
	 */
	public static InputStream GetInputStream(String contents) {
		try {
			return GetInputStream(contents, "UTF-8");
		} catch(UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
	}

	/**
	 * Returns an InputStream for a given string, using the given encoding.
	 *
	 * @param contents
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static InputStream GetInputStream(String contents, String encoding) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(contents.getBytes(encoding));
	}

	/**
	 * Returns an InputStream for a given byte array (ByteArrayInputStream).
	 *
	 * @param bytes The bytes to wrap in an InputStream
	 * @return An InputStream wrapping the bytes
	 */
	public static InputStream GetInputStream(byte[] bytes) {
		return new ByteArrayInputStream(bytes);
	}

	/**
	 * Returns System.out, but wrapped in a UTF-8 capable output stream. This is required, because higher order
	 * characters cannot print by default in Java.
	 *
	 * @return A new PrintStream object, based on System.out
	 */
	public static PrintStream GetSystemOut() {
		return new PrintStream(System.out, true, StandardCharsets.UTF_8);
	}

	/**
	 * Returns System.err, but wrapped in a UTF-8 capable output stream. This is required, because higher order
	 * characters cannot print by default in Java.
	 *
	 * @return A new PrintStream object, based on System.err
	 */
	public static PrintStream GetSystemErr() {
		return new PrintStream(System.err, true, StandardCharsets.UTF_8);
	}

	/**
	 * Gets a resource string with the specified encoding, relative to the class that is calling this method.
	 *
	 * @param name The name of the resource. The name should follow the same naming conventions used by
	 * {@link Class#getResource(java.lang.String)}.
	 * @param encoding The encoding to use on the resource.
	 * @return A string depiction of the specified resource.
	 * @throws java.io.UnsupportedEncodingException If the encoding is not supported.
	 * @throws java.lang.IllegalArgumentException If the resource was not found.
	 */
	public static final String GetResource(String name, String encoding) throws UnsupportedEncodingException,
			IllegalArgumentException {
		InputStream is = StackTraceUtils.getCallingClass().getResourceAsStream(name);
		if(is == null) {
			throw new IllegalArgumentException("Could not find resource " + name);
		}
		return GetString(is, encoding);
	}

	/**
	 * Gets a resource as a UTF-8 encoded string, relative to the class that is calling this method.
	 *
	 * @param name The name of the resource. The name should follow the same naming conventions used by
	 * {@link Class#getResource(java.lang.String)}.
	 * @return A string depiction of the specified resource.
	 * @throws java.lang.IllegalArgumentException If the resource was not found.
	 */
	public static final String GetResource(String name) throws IllegalArgumentException {
		try {
			return GetResource(name, "UTF-8");
		} catch(UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
	}
}
