package com.laytonsmith.PureUtilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Streams are hard sometimes. This class abstracts most of the functionality
 * that is commonly used.
 *
 * @author lsmith
 */
public class StreamUtils {

	/**
	 * Copies from one stream to another
	 * @param out
	 * @param in
	 * @throws IOException 
	 */
	public static void Copy(OutputStream out, InputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len != -1) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
	}
	
	/**
	 * Given an output stream, a UTF-8 encoded string is returned, which
	 * is a reasonable assumption for most textual data.
	 * @param out
	 * @return 
	 */
	public static String GetString(OutputStream out){
		PrintWriter pw = new PrintWriter(out);
		return pw.toString();
	}
	
	/**
	 * Assuming a UTF-8 encoded string is provided, returns an InputStream
	 * for that String.
	 * @param contents
	 * @return 
	 */
	public static InputStream GetInputStream(String contents){
		try {
			return new ByteArrayInputStream(contents.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
	}
}
