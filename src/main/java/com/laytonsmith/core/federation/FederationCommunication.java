package com.laytonsmith.core.federation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a wrapper for the lower level medium that communicates between servers. It may be a {@link java.net.Socket}
 * that carries the underlying communication, but that information, as well as the security encoding, if turned on, is
 * hidden from this.
 */
public class FederationCommunication {

	private final InputStream socketReader;
	private final OutputStream socketWriter;
	private final boolean isEncrypted;

	public FederationCommunication(InputStream reader, OutputStream writer) {
		this.socketReader = reader;
		this.socketWriter = writer;
		//TODO: Once the security is added, this should be changed.
		isEncrypted = false;
	}

	public void close() throws IOException {
		try {
			socketReader.close();
		} finally {
			socketWriter.close();
		}
	}

	/**
	 * Writes a line, without ensuring the connection is valid
	 *
	 * @param line
	 * @throws java.io.IOException
	 */
	public void writeLine(String line) throws IOException {
		try {
			socketWriter.write(encode(line.getBytes("UTF-8")));
			socketWriter.write("\n".getBytes("UTF-8"));
			socketWriter.flush();
		} catch (UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
	}

	/**
	 * Reads a line, without ensuring the connection is valid.
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public String readLine() throws IOException {
		try {
			List<Byte> bytes = new ArrayList<>();
			while(true) {
				int b = socketReader.read();
				if(b == '\n') {
					//The newline is never encoded.
					break;
				} else {
					bytes.add((byte) b);
				}
			}
			byte[] ba = new byte[bytes.size()];
			for(int i = 0; i < bytes.size(); i++) {
				ba[i] = bytes.get(i);
			}
			ba = decode(ba);
			return new String(ba, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
	}

	/**
	 * Writes raw bytes, without ensuring the connection is valid.
	 *
	 * @param bytes
	 * @throws IOException
	 */
	public void writeBytes(byte[] bytes) throws IOException {
		socketWriter.write(encode(bytes));
		socketWriter.flush();
	}

	/**
	 * Reads a line, without ensuring the connection is valid.
	 *
	 * @param size
	 * @return
	 * @throws java.io.IOException
	 */
	public byte[] readBytes(int size) throws IOException {
		byte[] bytes = new byte[size];
		socketReader.read(bytes);
		return decode(bytes);
	}

	/**
	 * Assumes the input is always unencrypted, but otherwise works like {@link #readBytes(int)}.
	 *
	 * @param size
	 * @return
	 * @throws java.io.IOException
	 */
	public byte[] readUnencrypted(int size) throws IOException {
		byte[] bytes = new byte[size];
		socketReader.read(bytes);
		return bytes;
	}

	/**
	 * Never encodes the output, but otherwise works like {@link #writeBytes(byte[])}.
	 *
	 * @param bytes
	 * @throws java.io.IOException
	 */
	public void writeUnencrypted(byte[] bytes) throws IOException {
		socketWriter.write(bytes);
		socketWriter.flush();
	}

	/**
	 * Assumes the input is always unencrypted, but otherwise works like {@link #readLine()}.
	 *
	 * @return
	 * @throws java.io.IOException
	 */
	public String readUnencryptedLine() throws IOException {
		try {
			// Don't put the reader in a try with resources, because we don't actually
			// want to close the reader when we're done; that would close the actual
			// InputStream as well. We're just using the BufferedReader as a shortcut
			// to reading this part of the stream.
			List<Byte> bytes = new ArrayList<>();
			while(true) {
				int b = socketReader.read();
				if(b == '\n') {
					//The newline is never encoded.
					break;
				} else {
					bytes.add((byte) b);
				}
			}
			byte[] ba = new byte[bytes.size()];
			for(int i = 0; i < bytes.size(); i++) {
				ba[i] = bytes.get(i);
			}
			return new String(ba, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
	}

	/**
	 * Never encodes the output, but otherwise works like {@link #writeLine(byte[])}.
	 *
	 * @param line The line to write.
	 * @throws java.io.IOException
	 */
	public void writeUnencryptedLine(String line) throws IOException {
		socketWriter.write(line.getBytes("UTF-8"));
		socketWriter.flush();
	}

	/**
	 * If the server's public key is provided, this encodes the data using it, before sending the data. If the public
	 * key isn't provided, then the bytes are simply returned as is.
	 *
	 * @param bytes
	 * @return
	 */
	private byte[] encode(byte[] bytes) {
		//TODO: Add the security bits here
		return bytes;
	}

	/**
	 * If the server's public key is provided, this encodes the data using it, before sending the data. If the public
	 * key isn't provided, then the bytes are simply returned as is.
	 *
	 * @param bytes
	 * @return
	 */
	private byte[] decode(byte[] bytes) {
		//TODO: Add security stuff here
		return bytes;
	}

}
