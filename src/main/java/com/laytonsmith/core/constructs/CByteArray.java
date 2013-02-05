package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Sizes;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.Sizable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.SortedMap;

/**
 *
 * @author lsmith
 */
@typename("byte_array")
public class CByteArray extends Construct implements Sizable {
	
	/**
	 * Initial size of the ByteBuffer
	 */
	private static final int initialSize = 1024;
	/**
	 * How much to scale the ByteBuffer by when re-allocating
	 */
	private static final int scaleMultiplier = 2;
	
	public static CByteArray wrap(byte[] b, Target t){
		CByteArray ba = new CByteArray(t, 0);
		ba.data = ByteBuffer.wrap(b);
		ba.maxValue = b.length;
		return ba;
	}
	
	private ByteBuffer data;
	private int maxValue = 0;
	private String value = null;
	
	public CByteArray(Target t){
		this(t, initialSize);
	}
	
	public CByteArray(Target t, int capacity){
		super("", t);
		data = ByteBuffer.allocate(capacity);
	}

	@Override
	public boolean isDynamic() {
		return true;
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
	public String toString() {
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
	
	public void rewind(){
		data.rewind();
	}
	
	public void putByte(byte b, Integer pos){
		checkSize(Sizes.sizeof(byte.class), pos);
		if(pos != null){
			data.position(pos);
		}
		data.put(b);
	}
	
	public void putChar(char c, Integer pos){
		checkSize(Sizes.sizeof(char.class), pos);
		if(pos == null){
			data.putChar(c);
		} else {
			data.putChar(pos, c);
		}
	}
	
	public void putDouble(double d, Integer pos){
		checkSize(Sizes.sizeof(double.class), pos);
		if(pos == null){
			data.putDouble(d);
		} else {
			data.putDouble(pos, d);
		}
	}
	
	public void putFloat(float f, Integer pos){
		checkSize(Sizes.sizeof(float.class), pos);
		if(pos == null){
			data.putFloat(f);
		} else {
			data.putFloat(pos, f);
		}
	}
	
	public void putInt(int i, Integer pos){
		checkSize(Sizes.sizeof(int.class), pos);
		if(pos == null){
			data.putInt(i);
		} else {
			data.putInt(pos, i);
		}
	}
	
	public void putLong(long l, Integer pos){
		checkSize(Sizes.sizeof(long.class), pos);
		if(pos == null){
			data.putLong(l);
		} else {
			data.putLong(pos, l);
		}
	}
	
	public void putShort(short s, Integer pos){
		checkSize(Sizes.sizeof(short.class), pos);
		if(pos == null){
			data.putShort(s);
		} else {
			data.putShort(pos, s);
		}
	}
	
	public byte getByte(Integer pos){
		if(pos == null){
			return data.get();
		} else {
			return data.get(pos);
		}
	}
	
	public char getChar(Integer pos){
		if(pos == null){
			return data.getChar();
		} else {
			return data.getChar(pos);
		}
	}
	
	public double getDouble(Integer pos){
		if(pos == null){
			return data.getDouble();
		} else {
			return data.getDouble(pos);
		}
	}
	
	public float getFloat(Integer pos){
		if(pos == null){
			return data.getFloat();
		} else {
			return data.getFloat(pos);
		}
	}
	
	public int getInt(Integer pos){
		if(pos == null){
			return data.getInt();
		} else {
			return data.getInt(pos);
		}
	}
	
	public long getLong(Integer pos){
		if(pos == null){
			return data.getLong();
		} else {
			return data.getLong(pos);
		}
	}
	
	public short getShort(Integer pos){
		if(pos == null){
			return data.getShort();
		} else {
			return data.getShort(pos);
		}
	}
	
	public void putBytes(CByteArray d, Integer pos){
		checkSize((int)d.size(), pos);
		if(pos != null){
			data.position(pos);
		}

		data.put(d.asByteArrayCopy());
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
	
	public int size(){
		return maxValue;
	}
	
	public int capacity(){
		return data.capacity();
	}
	
	//Supplemental methods
	/**
	 * Writes out a UTF-8 encoded string to the buffer. First, it writes out
	 * an int representing the length of the string.
	 * @param string
	 * @param pos 
	 */
	public void writeUTF8String(String string, Integer pos){
		byte[] array;
		try {
			array = string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
		checkSize(array.length + Sizes.sizeof(int.class), pos);
		if(pos != null){
			data.position(pos);
		}
		data.putInt(array.length);
		data.put(array);
	}
	
	/**
	 * Reads in a UTF-8 encoded string. It is assumed that 
	 * the string begins with a 32 bit length marker.
	 * @param pos
	 * @return 
	 */
	public String readUTF8String(Integer pos){
		if(pos != null){
			data.position(pos);
		}
		byte[] array = new byte[data.getInt()];
		data.get(array);
		try {
			return new String(array, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
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

	public String typeName() {
		return "byte_array";
	}
	
	private static class CArrayByteBacking extends CArray {
		private byte[] backing;
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
		public void set(CPrimitive index, Construct c, Target t) {
			throw new ROException();
		}

		@Override
		public Construct get(CPrimitive index, Target t) {
			int i = index.castToInt32(t);
			try{
				return new CInt(backing[i], t);
			} catch(ArrayIndexOutOfBoundsException e){
				throw new Exceptions.RangeException("Index out of range. Found " + i + ", but array length is only " + backing.length, t);
			}
		}

		@Override
		public int size() {
			return backing.length;
		}

		@Override
		public String toString() {
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
