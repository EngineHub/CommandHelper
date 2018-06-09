package com.laytonsmith.PureUtilities.Common;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
		} catch (UnsupportedEncodingException ex) {
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
			throw new NullPointerException();
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
		} catch (IOException ignore) {
		}

		return output.toString();

	}

	/**
	 * Fully reads in a stream, as efficiently as possible, and returns a byte array.
	 *
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] GetBytes(InputStream in) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(in);
		List<Byte> bytes = new ArrayList<Byte>();
		int i;
		while((i = bis.read()) != -1) {
			bytes.add(((byte) i));
		}
		return ArrayUtils.unbox(bytes.toArray(new Byte[bytes.size()]));
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
		} catch (UnsupportedEncodingException ex) {
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
	 * Returns System.out, but wrapped in a UTF-8 capable output stream. This is required, because higher order
	 * characters cannot print by default in Java.
	 *
	 * @return A new PrintStream object, based on System.out
	 */
	public static PrintStream GetSystemOut() {
		try {
			return new PrintStream(System.out, true, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
	}

	/**
	 * Returns System.err, but wrapped in a UTF-8 capable output stream. This is required, because higher order
	 * characters cannot print by default in Java.
	 *
	 * @return A new PrintStream object, based on System.err
	 */
	public static PrintStream GetSystemErr() {
		try {
			return new PrintStream(System.err, true, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
	}
}
