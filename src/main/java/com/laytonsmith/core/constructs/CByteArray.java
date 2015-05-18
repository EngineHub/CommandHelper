package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Sizes;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Sizable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

/**
 *
 *
 */
@typeof("byte_array")
public class CByteArray extends CArray implements Sizable, ArrayAccess {

	/**
	 * Initial size of the ByteBuffer
	 */
	private static final int initialSize = 1024;
	/**
	 * How much to scale the ByteBuffer by when re-allocating
	 */
	private static final int scaleMultiplier = 2;

	/**
	 * Creates a new CByteArray, wrapping the given byte buffer. It is important
	 * to note that it is NOT copied, but is instead simply wrapped, meaning changes to the
	 * underlying byte array will be reflected in the CByteArray created, and vice versa.
	 * @param b
	 * @param t
	 * @return
	 */
	public static CByteArray wrap(byte[] b, Target t){
		CByteArray ba = new CByteArray(t, 0);
		ba.data = ByteBuffer.wrap(b);
		ba.maxValue = b.length;
		return ba;
	}

	private ByteBuffer data;
	private int maxValue = 0;
	private String value = null;

	/**
	 * Creates a new, empty CByteArray, with initial capacity 1024.
	 * @param t
	 */
	public CByteArray(Target t){
		this(t, initialSize);
	}

	/**
	 * Creates a new, empty CByteArray, with initial capacity set as
	 * specified.
	 * @param t
	 * @param capacity
	 */
	public CByteArray(Target t, int capacity){
		super(t, initialSize);
		//super("", ConstructType.BYTE_ARRAY, t);
		data = ByteBuffer.allocate(capacity);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	public void setOrder(ByteOrder bo){
		data.order(bo);
	}

	public ByteOrder getOrder(){
		return data.order();
	}

	private void checkSize(int need, Integer pos){
		//set our max position
		int spos = pos == null ? data.position() : pos;
		maxValue = Math.max(maxValue, spos + need);
		//Reallocate if needed
		if(spos + need >= data.limit()){
			int newSize = data.limit() * scaleMultiplier;
			if(newSize <= 0){
				//Protect from this happening
				newSize = 1;
			}
			ByteBuffer temp = ByteBuffer.allocate(newSize);
			int position = data.position();
			data.rewind();
			temp.put(data);
			data = temp;
			data.position(position);
		}
		value = null;
	}

	@Override
	public String val() {
		if(value == null){
			int position = data.position();
			data.rewind();
			try {
				value = new String(data.array(), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				throw new Error(ex);
			}
			data.position(position);
		}
		return value;
	}

	/**
	 * Resets the position to zero on this byte array.
	 */
	public void rewind(){
		data.rewind();
	}

	/**
	 * Writes a java byte into the array.
	 * @param b The data to write.
	 * @param pos The position to start writing from, or null to use the current position.
	 */
	public void putByte(byte b, Integer pos){
		checkSize(Sizes.sizeof(byte.class), pos);
		if(pos != null){
			data.position(pos);
		}
		data.put(b);
	}

	/**
	 * Writes a java char into the array.
	 * @param c The data to write.
	 * @param pos The position to start writing from, or null to use the current position.
	 */
	public void putChar(char c, Integer pos){
		checkSize(Sizes.sizeof(char.class), pos);
		if(pos == null){
			data.putChar(c);
		} else {
			data.putChar(pos, c);
		}
	}

	/**
	 * Writes a java double into the array.
	 * @param d The data to write.
	 * @param pos The position to start writing from, or null to use the current position.
	 */
	public void putDouble(double d, Integer pos){
		checkSize(Sizes.sizeof(double.class), pos);
		if(pos == null){
			data.putDouble(d);
		} else {
			data.putDouble(pos, d);
		}
	}

	/**
	 * Writes a java float into the array.
	 * @param f The data to write.
	 * @param pos The position to start writing from, or null to use the current position.
	 */
	public void putFloat(float f, Integer pos){
		checkSize(Sizes.sizeof(float.class), pos);
		if(pos == null){
			data.putFloat(f);
		} else {
			data.putFloat(pos, f);
		}
	}

	/**
	 * Writes a java int into the array.
	 * @param i The data to write.
	 * @param pos The position to start writing from, or null to use the current position.
	 */
	public void putInt(int i, Integer pos){
		checkSize(Sizes.sizeof(int.class), pos);
		if(pos == null){
			data.putInt(i);
		} else {
			data.putInt(pos, i);
		}
	}

	/**
	 * Writes a java long into the array.
	 * @param l The data to write.
	 * @param pos The position to start writing from, or null to use the current position.
	 */
	public void putLong(long l, Integer pos){
		checkSize(Sizes.sizeof(long.class), pos);
		if(pos == null){
			data.putLong(l);
		} else {
			data.putLong(pos, l);
		}
	}

	/**
	 * Writes a java short into the array.
	 * @param s The data to write.
	 * @param pos The position to start writing from, or null to use the current position.
	 */
	public void putShort(short s, Integer pos){
		checkSize(Sizes.sizeof(short.class), pos);
		if(pos == null){
			data.putShort(s);
		} else {
			data.putShort(pos, s);
		}
	}

	/**
	 * Reads a java byte from this array, and advances the position.
	 * @param pos The position to read from, or null to read from the current position.
	 * @return
	 */
	public byte getByte(Integer pos){
		if(pos == null){
			return data.get();
		} else {
			return data.get(pos);
		}
	}

	/**
	 * Reads a java char from this array, and advances the position.
	 * @param pos The position to read from, or null to read from the current position.
	 * @return
	 */
	public char getChar(Integer pos){
		if(pos == null){
			return data.getChar();
		} else {
			return data.getChar(pos);
		}
	}

	/**
	 * Reads a java double from this array, and advances the position.
	 * @param pos The position to read from, or null to read from the current position.
	 * @return
	 */
	public double getDouble(Integer pos){
		if(pos == null){
			return data.getDouble();
		} else {
			return data.getDouble(pos);
		}
	}

	/**
	 * Reads a java float from this array, and advances the position.
	 * @param pos The position to read from, or null to read from the current position.
	 * @return
	 */
	public float getFloat(Integer pos){
		if(pos == null){
			return data.getFloat();
		} else {
			return data.getFloat(pos);
		}
	}

	/**
	 * Reads a java int from this array, and advances the position.
	 * @param pos The position to read from, or null to read from the current position.
	 * @return
	 */
	public int getInt(Integer pos){
		if(pos == null){
			return data.getInt();
		} else {
			return data.getInt(pos);
		}
	}

	/**
	 * Reads a java long from this array, and advances the position.
	 * @param pos The position to read from, or null to read from the current position.
	 * @return
	 */
	public long getLong(Integer pos){
		if(pos == null){
			return data.getLong();
		} else {
			return data.getLong(pos);
		}
	}

	/**
	 * Reads a java short from this array, and advances the position.
	 * @param pos The position to read from, or null to read from the current position.
	 * @return
	 */
	public short getShort(Integer pos){
		if(pos == null){
			return data.getShort();
		} else {
			return data.getShort(pos);
		}
	}

	/**
	 * Writes another CByteArray into the array.
	 * @param d The data to write.
	 * @param pos The position to start writing from, or null to use the current position.
	 */
	public void putBytes(CByteArray d, Integer pos){
		putBytes(d.asByteArrayCopy(), pos);
	}

	/**
	 * Writes a java byte[] into the array.
	 * @param d The data to write.
	 * @param pos The position to start writing from, or null to use the current position.
	 */
	public void putBytes(byte[] d, Integer pos){
		checkSize((int)d.length, pos);
		if(pos != null){
			data.position(pos);
		}
		data.put(d);
	}

	/**
	 * Returns a byte array, of the given size, read from pos, or
	 * the current position, if null.
	 * @param size
	 * @param pos
	 * @return
	 */
	public CByteArray getBytes(int size, Integer pos){
		CByteArray ba = new CByteArray(this.getTarget(), 0);
		byte[] d = new byte[size];
		if(pos != null){
			data.position(pos);
		}
		data.get(d);
		ba.data = ByteBuffer.wrap(d);
		return ba;
	}

	/**
	 * Returns the current size of the byte array. This is not to be confused with the
	 * capacity.
	 * @return
	 */
	@Override
	public long size(){
		return maxValue;
	}

	/**
	 * Returns the maximum size of the underlying data before it would have to be
	 * resized to add more data. This is not to be confused with the size.
	 * @return
	 */
	public int capacity(){
		return data.capacity();
	}

	//Supplemental methods
	/**
	 * Writes out a UTF-8 encoded string to the buffer. First, it writes out
	 * a short representing the length of the string.
	 * @param string
	 * @param pos
	 * @param encoding Defaults to UTF-8 if null, but may be specified otherwise
	 * @throws IndexOutOfBoundsException If the length of the string is greater than 65536 bytes.
	 * @throws java.io.UnsupportedEncodingException
	 */
	public void writeUTF8String(String string, Integer pos, String encoding) throws IndexOutOfBoundsException, UnsupportedEncodingException {
		byte[] array;
		if(encoding == null){
			encoding = "UTF-8";
		}
		array = string.getBytes(encoding);
		checkSize(array.length + Sizes.sizeof(short.class), pos);
		if(pos != null){
			data.position(pos);
		}
		if(array.length > Short.MAX_VALUE){
			throw new IndexOutOfBoundsException("The length of the string cannot be greater than " + Short.MAX_VALUE + ". If you must encode a string"
					+ " longer than this, you must write the string out yourself.");
		}
		data.putShort((short)array.length);
		data.put(array);
	}

	/**
	 * Reads in a UTF-8 encoded string. It is assumed that
	 * the string begins with a 16 bit length marker.
	 * @param pos
	 * @param encoding If null, defaults to UTF-8, but may be specified directly.
	 * @return
	 * @throws java.io.UnsupportedEncodingException
	 */
	public String readUTF8String(Integer pos, String encoding) throws UnsupportedEncodingException {
		if(pos != null){
			data.position(pos);
		}
		if(encoding == null){
			encoding = "UTF-8";
		}
		byte[] array = new byte[data.getShort()];
		data.get(array);
		return new String(array, encoding);
	}

	/**
	 * Returns a new read only CArray object with integers at each index,
	 * representing the underlying byte array. They are not linked. This
	 * array is faster than normal CArrays, at the cost of being read only. Cloning
	 * the array is supported, however, so it is possible to convert this into a
	 * fully functional array that way. The backing for the CArray is independant
	 * of this CByteArray (it is a new copy).
	 * @param t
	 * @return
	 */
	public CArray asArray(Target t){
		return new CArrayByteBacking(asByteArrayCopy(), t);
	}

	/**
	 * Returns a copy of this CByteArray, as a Java byte array, at this
	 * point in time. This is meant to be used as the final step before sending the
	 * data off to an external process, or when interfacing mscript with other POJO code.
	 * @return
	 */
	public byte[] asByteArrayCopy(){
		byte[] src = data.array();
		byte[] dest = new byte[maxValue];
		System.arraycopy(src, 0, dest, 0, maxValue);
		return dest;
	}

	@Override
	public boolean canBeAssociative() {
		return false;
	}

	@Override
	public Construct slice(int begin, int end, Target t) {
		return getBytes(end - begin, begin);
	}

	@Override
	public boolean isAssociative() {
		return false;
	}

	@Override
	public Set<Construct> keySet() {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public Construct get(Construct index, Target t) throws ConfigRuntimeException {
		int i = Static.getInt32(index, t);
		byte b = getByte(i);
		return new CInt(b, t);
	}

	/**
	 * This is a more efficient implementation of CArray for the backing byte arrays.
	 */
	private static class CArrayByteBacking extends CArray {
		private final byte[] backing;
		private String value = null;
		public CArrayByteBacking(byte [] backing, Target t){
			super(t);
			this.backing = backing;
		}

		@Override
		public void reverse() {
			throw new ROException();
		}

		@Override
		public void push(Construct c) {
			throw new ROException();
		}

		@Override
		public void set(Construct index, Construct c, Target t) {
			throw new ROException();
		}

		@Override
		public Construct get(Construct index, Target t) {
			int i = Static.getInt32(index, t);
			try{
				return new CInt(backing[i], t);
			} catch(ArrayIndexOutOfBoundsException e){
				throw new Exceptions.RangeException("Index out of range. Found " + i + ", but array length is only " + backing.length, t);
			}
		}

		@Override
		public long size() {
			return backing.length;
		}

		@Override
		public String val() {
			if(value == null){
				try {
					value = new String(backing, "UTF-8");
				} catch (UnsupportedEncodingException ex) {
					throw new Error(ex);
				}
			}
			return value;
		}

		@Override
		public boolean inAssociativeMode() {
			return false;
		}

		@Override
		protected List<Construct> getArray() {
			//I'm not sure what cases this would happen in, but it should not happen normally.
			throw new RuntimeException("This error should not happen. Please report this bug to the developers");
		}

		@Override
		protected SortedMap<String, Construct> getAssociativeArray() {
			//This is even more serious, because it shouldn't ever happen.
			throw new Error("This error should not happen. Please report this bug to the developers");
		}

		public class ROException extends ConfigRuntimeException{
			public ROException(){
				super("Arrays copied from ByteArrays are read only", Exceptions.ExceptionType.ReadOnlyException, CArrayByteBacking.this.getTarget());
			}
		}

	}

}
