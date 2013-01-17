package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Sizes;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lsmith
 */
public class CByteArray extends Construct {
	
	/**
	 * Initial size of the ByteBuffer
	 */
	private static final int initialSize = 1024;
	/**
	 * How much to scale the ByteBuffer by when re-allocating
	 */
	private static final int scaleMultiplier = 2;
	
	private ByteBuffer data;
	
	public CByteArray(Target t){
		super("", ConstructType.BYTE_ARRAY, t);
		data = ByteBuffer.allocate(initialSize);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
	
	private void checkSize(int need){
		if(data.position() + need >= data.limit()){
			//Reallocate
			ByteBuffer temp = ByteBuffer.allocate(data.limit() * scaleMultiplier);
			data.rewind();
			temp.put(data);
			data = temp;
		}
	}
	
	public void putChar(char c, Integer pos){
		checkSize(Sizes.sizeof(char.class));
		if(pos == null){
			data.putChar(c);
		} else {
			data.putChar(pos, c);
		}
	}
	
	public void putDouble(double d, Integer pos){
		checkSize(Sizes.sizeof(double.class));
		if(pos == null){
			data.putDouble(d);
		} else {
			data.putDouble(pos, d);
		}
	}
	
	public void putFloat(float f, Integer pos){
		checkSize(Sizes.sizeof(float.class));
		if(pos == null){
			data.putFloat(f);
		} else {
			data.putFloat(pos, f);
		}
	}
	
	public void putInt(int i, Integer pos){
		checkSize(Sizes.sizeof(int.class));
		if(pos == null){
			data.putInt(i);
		} else {
			data.putInt(pos, i);
		}
	}
	
	public void putLong(long l, Integer pos){
		checkSize(Sizes.sizeof(long.class));
		if(pos == null){
			data.putLong(l);
		} else {
			data.putLong(pos, l);
		}
	}
	
	public void putShort(short s, Integer pos){
		checkSize(Sizes.sizeof(short.class));
		if(pos == null){
			data.putShort(s);
		} else {
			data.putShort(pos, s);
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
		checkSize(array.length);
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
		byte[] org = data.array();
		byte[] src = new byte[org.length];
		System.arraycopy(org, 0, src, 0, org.length);
		return new CArrayByteBacking(src, t);
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
