package com.laytonsmith.PureUtilities.Common;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

/**
 * This class extends ObjectOutputStream, but automatically flushes after each write operation.
 */
public class AutoFlushObjectOutputStream extends ObjectOutputStream {

	public AutoFlushObjectOutputStream() throws IOException {
		super();
	}

	public AutoFlushObjectOutputStream(OutputStream outputStream) throws IOException {
		super(outputStream);
	}

	@Override
	public void write(byte[] buf) throws IOException {
		super.write(buf);
		super.flush();
	}

	@Override
	public void write(int val) throws IOException {
		super.write(val);
		super.flush();
	}

	@Override
	public void write(byte[] buf, int off, int len) throws IOException {
		super.write(buf, off, len);
		super.flush();
	}

	@Override
	public void writeBoolean(boolean val) throws IOException {
		super.writeBoolean(val);
		super.flush();
	}

	@Override
	public void writeByte(int val) throws IOException {
		super.writeByte(val);
		super.flush();
	}

	@Override
	public void writeBytes(String str) throws IOException {
		super.writeBytes(str);
		super.flush();
	}

	@Override
	public void writeChar(int val) throws IOException {
		super.writeChar(val);
		super.flush();
	}

	@Override
	public void writeChars(String str) throws IOException {
		super.writeChars(str);
		super.flush();
	}

	@Override
	protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
		super.writeClassDescriptor(desc);
		super.flush();
	}

	@Override
	public void writeDouble(double val) throws IOException {
		super.writeDouble(val);
		super.flush();
	}

	@Override
	public void writeFields() throws IOException {
		super.writeFields();
		super.flush();
	}

	@Override
	public void writeFloat(float val) throws IOException {
		super.writeFloat(val);
		super.flush();
	}

	@Override
	public void writeInt(int val) throws IOException {
		super.writeInt(val);
		super.flush();
	}

	@Override
	public void writeLong(long val) throws IOException {
		super.writeLong(val);
		super.flush();
	}

	@Override
	protected void writeObjectOverride(Object obj) throws IOException {
		super.writeObjectOverride(obj);
		super.flush();
	}

	@Override
	public void writeShort(int val) throws IOException {
		super.writeShort(val);
		super.flush();
	}

	@Override
	protected void writeStreamHeader() throws IOException {
		super.writeStreamHeader();
		super.flush();
	}

	@Override
	public void writeUTF(String str) throws IOException {
		super.writeUTF(str);
		super.flush();
	}

	@Override
	public void writeUnshared(Object obj) throws IOException {
		super.writeUnshared(obj);
		super.flush();
	}

}
