package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;

/**
 * 
 */
@core
public class ByteArrays {
	public static String docs(){
		return "This class contains all the methods needed to manipulate a byte array primitive. Since"
				+ " byte arrays would be very inefficient to implement using a normal array, this data type"
				+ " allows for more efficient operations, while still allowing for low level data access."
				+ " Most data transferred within scripts is higher level, and does not require access"
				+ " to a byte array, however, code that interacts with external processes may require"
				+ " use of these functions to properly manipulate the data. Note that all the methods"
				+ " deal with low level types, so the following definitions apply: a byte is 8 bits,"
				+ " a short is 16 bits, an int is 32 bits, a long is 64 bits. UTF-8 strings are supported"
				+ " directly. The byte array is automatically resized as needed.";
	}
	
	@api
	public static class byte_array extends ba {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CByteArray(t);
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "byte_array {} Returns a new byte array primitive, which can be operated on with the ba_ series of functions.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "byte_array";
		}
		
	}
	
	@api
	public static class ba_as_array extends ba {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = Static.getByteArray(args[0], t);
			return ba.asArray(t);
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {byte_array} Returns a new read only copy of the underlying byte array. This array is much more efficient"
					+ " than if the array were made manually, however, it is read only. If you need to manipulate the array's"
					+ " contents, then you can clone the array, however, the returned array (and any clones) cannot be automatically"
					+ " interfaced with the byte array primitives. This operation is discouraged, because normal arrays are very"
					+ " inefficient for dealing with low level bit data.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_as_array";
		}
		
	}
	
	@api
	public static class ba_rewind extends ba {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			ba.rewind();
			return CVoid.VOID;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {byte_array} Rewinds the byte array marker to 0.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_rewind";
		}
		
	}
	
	@api
	public static class ba_get_byte extends ba_get {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			Integer pos = get_getPos(args, t);
			try {
				return new CInt(ba.getByte(pos), t);
			} catch(IndexOutOfBoundsException | BufferUnderflowException e){
				throw new CRERangeException(e.getMessage(), t);
			}
		}

		@Override
		public String docs() {
			return "int {byte_array, [pos]} Returns an int, read in as an 8 bit byte, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_get_byte";
		}
		
	}
	
	@api
	public static class ba_get_char extends ba_get {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			Integer pos = get_getPos(args, t);
			try{
				return new CString(ba.getChar(pos), t);
			} catch(IndexOutOfBoundsException | BufferUnderflowException e){
				throw new CRERangeException(e.getMessage(), t);
			}
		}

		@Override
		public String docs() {
			return "string {byte_array, [pos]} Returns a one character string, read in as an 32 bit char, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_get_char";
		}
		
	}
	
	@api
	public static class ba_get_short extends ba_get {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			Integer pos = get_getPos(args, t);
			try{
				return new CInt(ba.getShort(pos), t);
			} catch(IndexOutOfBoundsException | BufferUnderflowException e){
				throw new CRERangeException(e.getMessage(), t);
			}
		}

		@Override
		public String docs() {
			return "int {byte_array, [pos]} Returns an int, read in as a 16 bit short, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
			
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_get_short";
		}
	}
	
	@api
	public static class ba_get_int extends ba_get {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			Integer pos = get_getPos(args, t);
			try{
				return new CInt(ba.getInt(pos), t);
			} catch(IndexOutOfBoundsException | BufferUnderflowException e){
				throw new CRERangeException(e.getMessage(), t);
			}
		}

		@Override
		public String docs() {
			return "int {byte_array, [pos]} Returns an int, read in as a 32 bit int, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_get_int";
		}
		
	}
	
	@api
	public static class ba_get_long extends ba_get {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			Integer pos = get_getPos(args, t);
			try {
				return new CInt(ba.getLong(pos), t);
			} catch (IndexOutOfBoundsException | BufferUnderflowException e) {
				throw new CRERangeException(e.getMessage(), t);
			}
		}

		@Override
		public String docs() {
			return "int {byte_array, [pos]} Returns an int, read in as a 64 bit long, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_get_long";
		}
		
	}
	
	@api
	public static class ba_get_float extends ba_get {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			Integer pos = get_getPos(args, t);
			try {
				return new CDouble(ba.getFloat(pos), t);
			} catch (IndexOutOfBoundsException | BufferUnderflowException e) {
				throw new CRERangeException(e.getMessage(), t);
			}
		}

		@Override
		public String docs() {
			return "double {byte_array, [pos]} Returns a double, read in as a 32 bit float, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_get_float";
		}
		
	}
	
	@api
	public static class ba_get_double extends ba_get {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			Integer pos = get_getPos(args, t);
			try {
				return new CDouble(ba.getDouble(pos), t);
			} catch(IndexOutOfBoundsException | BufferUnderflowException e){
				throw new CRERangeException(e.getMessage(), t);
			}
		}

		@Override
		public String docs() {
			return "double {byte_array, [pos]} Returns a double, read in as a 64 bit double, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_get_double";
		}
	}
	
	@api
	public static class ba_get_bytes extends ba_get {

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			int size = Static.getInt32(args[1], t);
			Integer pos = null;
			if(args.length == 3){
				pos = Static.getInt32(args[2], t);
			}
			try {
				return ba.getBytes(size, pos);
			} catch(IndexOutOfBoundsException | BufferUnderflowException e){
				throw new CRERangeException(e.getMessage(), t);
			}
		}

		@Override
		public String docs() {
			return "byte_array {byte_array, length, [pos]} Returns a new byte_array primitive, starting from pos (or wherever the marker is"
					+ " by default) to length.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_get_bytes";
		}
		
	}
	
	@api
	public static class ba_get_string extends ba_get {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			Integer pos = get_getPos(args, t);
			String encoding = null;
			if(args.length == 3){
				encoding = args[2].nval();
			}
			try{
				return new CString(ba.readUTF8String(pos, encoding), t);
			} catch(UnsupportedEncodingException e){
				throw new CREFormatException(e.getMessage(), t);
			} catch(IndexOutOfBoundsException | BufferUnderflowException e){
				throw new CRERangeException(e.getMessage(), t);
			} catch(NegativeArraySizeException e){
				throw new CREFormatException("Invalid data", t);
			}
		}

		@Override
		public String docs() {
			return "string {byte_array, [pos], [encoding]} Returns a UTF-8 encoded string, from the given position, or wherever the"
					+ " marker is currently at by default. The string is assumed to have encoded the length of the string"
					+ " with a 32 bit integer, then the string bytes. (This will be the case is the byte_array was encoded"
					+ " with ba_set_string.) The encoding of the string may be set, but defaults to UTF-8.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_get_string";
		}
		
	}
	
	@api
	public static class ba_put_byte extends ba_put {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			byte b = Static.getInt8(args[1], t);
			Integer pos = set_getPos(args, t);
			ba.putByte(b, pos);
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {byte_array, int, [pos]} Writes an int, interpreted as an 8 bit byte, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_put_byte";
		}
		
	}
	
	@api
	public static class ba_put_char extends ba_put {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			String b = args[1].val();
			char c = '\0';
			if(b.length() > 0){
				c = b.charAt(0);
			}
			Integer pos = set_getPos(args, t);
			ba.putChar(c, pos);
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {byte_array, string, [pos]} Writes the first character of the string, interpreted as an 32 bit char, starting from the given position, or wherever the"
					+ " marker is currently at by default. If the string is empty, a \\0 is written instead.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_put_char";
		}
		
	}
	
	@api
	public static class ba_put_short extends ba_put {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			short b = Static.getInt16(args[1], t);
			Integer pos = set_getPos(args, t);
			ba.putShort(b, pos);
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {byte_array, int, [pos]} Writes an int, interpreted as an 16 bit short, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_put_short";
		}
		
	}
	
	@api
	public static class ba_put_int extends ba_put {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			int b = Static.getInt32(args[1], t);
			Integer pos = set_getPos(args, t);
			ba.putInt(b, pos);
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {byte_array, int, [pos]} Writes an int, interpreted as a 32 bit int, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_put_int";
		}
	}
	
	@api
	public static class ba_put_long extends ba_put {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			long b = Static.getInt(args[1], t);
			Integer pos = set_getPos(args, t);
			ba.putLong(b, pos);
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {byte_array, int, [pos]} Writes an int, interpreted as a 64 bit, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_put_long";
		}
		
	}
	
	@api
	public static class ba_put_float extends ba_put {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			float b = Static.getDouble32(args[1], t);
			Integer pos = set_getPos(args, t);
			ba.putFloat(b, pos);
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {byte_array, double, [pos]} Writes a double, interpreted as a 32 bit float, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_put_float";
		}
		
	}
	
	@api
	public static class ba_put_double extends ba_put {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			double b = Static.getDouble(args[1], t);
			Integer pos = set_getPos(args, t);
			ba.putDouble(b, pos);
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {byte_array, double, [pos]} Writes a double, interpreted as a 64 bit double, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_put_double";
		}
		
	}
	
	@api
	public static class ba_put_bytes extends ba_put {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray dest = getBA(args, t);
			CByteArray src = Static.getByteArray(args[1], t);
			Integer pos = set_getPos(args, t);
			dest.putBytes(src, pos);
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {destination_byte_array, source_byte_array, [pos]} Writes the contents of the source_byte_array into this byte array,"
					+ " starting at pos, or wherever the marker is currently at by default.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_put_bytes";
		}
		
	}
	
	@api
	public static class ba_put_string extends ba_put {

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = getBA(args, t);
			String s = args[1].val();
			Integer pos = set_getPos(args, t);
			String encoding = null;
			if(args.length == 3){
				encoding = args[2].nval();
			}
			try{
				ba.writeUTF8String(s, pos, encoding);
			} catch(IndexOutOfBoundsException e){
				throw new CRERangeException(e.getMessage(), t);
			} catch(UnsupportedEncodingException e){
				throw new CREFormatException(e.getMessage(), t);
			}
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "void {byte_array, string, [pos], [encoding]} Writes the length of the string to the byte array, as a short, (interpreted as UTF-8),"
					+ " then writes the UTF-8 string itself. If an external application requires the string to be serialized"
					+ " in a different manner, then use the string-byte_array conversion methods in StringHandling, however"
					+ " strings written in this manner are compatible with ba_get_string. The encoding may be set, but defaults to UTF-8.";
		}
		
		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
		@Override
		public String getName() {
			return "ba_put_string";
		}
		
	}

	@api
	@seealso({ba_is_little_endian.class})
	public static class ba_set_little_endian extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = Static.getByteArray(args[0], t);
			boolean setLittle = Static.getBoolean(args[1]);
			ba.setOrder(setLittle ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "ba_set_little_endian";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {byte_array, setLittleEndian} Sets the byte order that the specified byte array will use"
					+ " for all future gets/sets. By default, a byte array is Big Endian. If setLittleEndian is true,"
					+ " the byte array will be set to little endian, otherwise it will be set to big endian.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	@seealso({ba_set_little_endian.class})
	public static class ba_is_little_endian extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CByteArray ba = Static.getByteArray(args[0], t);
			return CBoolean.get(ba.getOrder() == ByteOrder.LITTLE_ENDIAN);
		}

		@Override
		public String getName() {
			return "ba_is_little_endian";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {byte_array} Returns true if this byte array is little endian. By default, byte arrays are big endian,"
					+ " but this may be changed with ba_set_little_endian.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

	}
	
	private static CByteArray getBA(Construct [] args, Target t){
		return Static.getByteArray(args[0], t);
	}
	
	private static Integer get_getPos(Construct [] args, Target t){
		if(args.length == 2){
			return Static.getInt32(args[1], t);
		} else {
			return null;
		}
	}
	
	private static Integer set_getPos(Construct [] args, Target t){
		if(args.length == 3){
			return Static.getInt32(args[2], t);
		} else {
			return null;
		}
	}
	
	private static abstract class ba extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}
	}
	
	public static abstract class ba_put extends ba {

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}
		
	}
	
	public static abstract class ba_get extends ba {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class};
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}
		
	}
	
}
