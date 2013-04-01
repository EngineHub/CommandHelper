package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.arguments.ArgList;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.annotations.NonNull;
import java.io.UnsupportedEncodingException;

/**
 * 
 * @author lsmith
 */
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
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			return new CByteArray(t);
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "Returns a new byte array primitive, which can be operated on with the ba_ series of functions.";
		}
		
		public Argument returnType() {
			return new Argument("The newly created byte_array", CByteArray.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.NONE;
		}
		
	}
	
	@api
	public static class ba_as_array extends ba {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			return ba.asArray(t);
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Returns a new read only copy of the underlying byte array. This array is much more efficient"
					+ " than if the array were made manually, however, it is read only. If you need to manipulate the array's"
					+ " contents, then you can clone the array, however, the returned array (and any clones) cannot be automatically"
					+ " interfaced with the byte array primitives. This operation is discouraged, because normal arrays are very"
					+ " inefficient for dealing with low level bit data.";
		}
		
		public Argument returnType() {
			return new Argument("The new read-only array, based on the underlying byte_array at the current point in time", CArray.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to convert", CByteArray.class, "ba")
					);
		}
		
	}
	
	@api
	public static class ba_rewind extends ba {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			ba.rewind();
			return new CVoid(t);
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Rewinds the byte array marker to 0.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The target byte array", CByteArray.class, "ba")
					);
		}
		
	}
	
	@api
	public static class ba_get_byte extends ba_get {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			Integer pos = list.getIntegerWithNull("pos", t);
			return new CInt(ba.getByte(pos), t);
		}

		public String docs() {
			return "Returns an int, read in as an 8 bit byte, from the given position, or wherever the"
					+ " marker is currently at by default, and advances the marker 8 bits.";
		}
		
		public Argument returnType() {
			return new Argument("The 8 bit byte (as an int) read in", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_get_char extends ba_get {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			Integer pos = list.getIntegerWithNull("pos", t);
			return new CString(ba.getChar(pos), t);
		}

		public String docs() {
			return "string {byte_array, [pos]} Returns a one character string, read in as an 32 bit char, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return new Argument("The 32 bit (one character) string value", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_get_short extends ba_get {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			Integer pos = list.getIntegerWithNull("pos", t);
			return new CInt(ba.getShort(pos), t);
		}

		public String docs() {
			return "Returns an int, read in as a 16 bit short, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return new Argument("The 16 bit short (as an int) read in", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
			
	}
	
	@api
	public static class ba_get_int extends ba_get {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			Integer pos = list.getIntegerWithNull("pos", t);
			return new CInt(ba.getInt(pos), t);
		}

		public String docs() {
			return "Returns an int, read in as a 32 bit int, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return new Argument("The 32 bit int (as an int) read in", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_get_long extends ba_get {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			Integer pos = list.getIntegerWithNull("pos", t);
			return new CInt(ba.getLong(pos), t);
		}

		public String docs() {
			return "Returns an int, read in as a 64 bit long, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return new Argument("The 64 bit long (as an int) read in", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_get_float extends ba_get {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			Integer pos = list.getIntegerWithNull("pos", t);
			return new CDouble(ba.getFloat(pos), t);
		}

		public String docs() {
			return "Returns a double, read in as a 32 bit float, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return new Argument("The 32 bit float (as a double) read in", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_get_double extends ba_get {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			Integer pos = list.getIntegerWithNull("pos", t);
			return new CDouble(ba.getDouble(pos), t);
		}

		public String docs() {
			return "Returns a double, read in as a 64 bit double, from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return new Argument("The 64 bit double (as a double) read in", CDouble.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_get_bytes extends ba_get {

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			int size = list.getInt("length", t);
			Integer pos = list.getIntegerWithNull("pos", t);
			return ba.getBytes(size, pos);
		}

		public String docs() {
			return "Returns a new byte_array primitive, starting from pos (or wherever the marker is"
					+ " by default) to length.";
		}
		
		public Argument returnType() {
			return new Argument("The variable length byte_array read in", CInt.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The length of the byte array to read in", CByteArray.class, "length"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_get_string extends ba_get {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			Integer pos = list.getIntegerWithNull("pos", t);
			String encoding = list.getString("encoding", t);
			try{
				return new CString(ba.readUTF8String(pos, encoding), t);
			} catch(UnsupportedEncodingException e){
				throw new Exceptions.FormatException(e.getMessage(), t);
			}
		}

		public String docs() {
			return "Returns a UTF-8 encoded string, from the given position, or wherever the"
					+ " marker is currently at by default. The string is assumed to have encoded the length of the string"
					+ " with a 32 bit integer, then the string bytes. (This will be the case is the byte_array was encoded"
					+ " with ba_set_string.) The encoding of the string may be set, but defaults to UTF-8.";
		}
		
		public Argument returnType() {
			return new Argument("The variable length string read in", CString.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull(),
						new Argument("The encoding to use", CString.class, "encoding").setOptionalDefault("UTF-8").addAnnotation(new NonNull())
					);
		}
		
	}
	
	@api
	public static class ba_put_byte extends ba_put {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			byte b = list.getByte("val", t);
			Integer pos = list.getIntegerWithNull("pos", t);
			ba.putByte(b, pos);
			return new CVoid(t);
		}

		public String docs() {
			return "Writes an int, interpreted as an 8 bit byte, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The byte value to push on to this byte array The top 56 bits of the int are ignored", CInt.class, "val"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_put_char extends ba_put {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			String b = list.getString("val", t);
			char c = '\0';
			if(b.length() > 0){
				c = b.charAt(0);
			}
			Integer pos = list.getIntegerWithNull("pos", t);
			ba.putChar(c, pos);
			return new CVoid(t);
		}

		public String docs() {
			return "Writes the first character of the string, interpreted as an 32 bit char, starting from the given position, or wherever the"
					+ " marker is currently at by default. If the string is empty, a \\0 is written instead.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The char value to push on to this byte array. Only the first character is considered, and the rest of the string is ignored", CString.class, "val"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_put_short extends ba_put {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			short b = list.getShort("val", t);
			Integer pos = list.getIntegerWithNull("pos", t);
			ba.putShort(b, pos);
			return new CVoid(t);
		}

		public String docs() {
			return "Writes an int, interpreted as an 16 bit short, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The short value to push on to this byte array. The top 48 bits of the int are ignored", CInt.class, "val"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_put_int extends ba_put {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			int b = list.getInt("val", t);
			Integer pos = list.getIntegerWithNull("pos", t);
			ba.putInt(b, pos);
			return new CVoid(t);
		}

		public String docs() {
			return "Writes an int, interpreted as a 32 bit int, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The int value to push on to this byte array. Thet op 32 bits of the int are ignored", CInt.class, "val"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_put_long extends ba_put {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			long b = list.getLong("val", t);
			Integer pos = list.getIntegerWithNull("pos", t);
			ba.putLong(b, pos);
			return new CVoid(t);
		}

		public String docs() {
			return "Writes an int, interpreted as a 64 bit, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The long value to push on to this byte array", CInt.class, "val"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_put_float extends ba_put {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			float b = list.getFloat("val", t);
			Integer pos = list.getIntegerWithNull("pos", t);
			ba.putFloat(b, pos);
			return new CVoid(t);
		}

		public String docs() {
			return "Writes a double, interpreted as a 32 bit float, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The float value to push on to this byte array. The top 32 bits of the double are ignored", CDouble.class, "val"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_put_double extends ba_put {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			double b = list.getDouble("val", t);
			Integer pos = list.getIntegerWithNull("pos", t);
			ba.putDouble(b, pos);
			return new CVoid(t);
		}

		public String docs() {
			return "Writes a double, interpreted as a 64 bit double, starting from the given position, or wherever the"
					+ " marker is currently at by default.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The double value to push on to this byte array", CDouble.class, "val"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_put_bytes extends ba_put {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray dest = list.get("destination");
			CByteArray src = list.get("source");
			Integer pos = list.getIntegerWithNull("pos", t);
			dest.putBytes(src, pos);
			return new CVoid(t);
		}

		public String docs() {
			return "Writes the contents of the source_byte_array into this byte array,"
					+ " starting at pos, or wherever the marker is currently at by default.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "destination"),
						new Argument("The byte_array value to push on to this byte array", CByteArray.class, "source"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull()
					);
		}
		
	}
	
	@api
	public static class ba_put_string extends ba_put {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			ArgList list = getBuilder().parse(args, this, t);
			CByteArray ba = list.get("ba");
			String s = list.getString("val", t);
			Integer pos = list.getIntegerWithNull("pos", t);
			String encoding = list.getString("encoding", t);
			try{
				ba.writeUTF8String(s, pos, encoding);
			} catch(IndexOutOfBoundsException e){
				throw new Exceptions.RangeException(e.getMessage(), t);
			} catch(UnsupportedEncodingException e){
				throw new Exceptions.FormatException(e.getMessage(), t);
			}
			return new CVoid(t);
		}

		public String docs() {
			return "Writes the length of the string to the byte array, as a short, (interpreted as UTF-8),"
					+ " then writes the UTF-8 string itself. If an external application requires the string to be serialized"
					+ " in a different manner, then use the string-byte_array conversion methods in StringHandling, however"
					+ " strings written in this manner are compatible with ba_get_string. The encoding may be set, but defaults to UTF-8.";
		}
		
		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("The byte array to operate on", CByteArray.class, "ba"),
						new Argument("The string value to push on to this byte array", CString.class, "val"),
						new Argument("The position to read from, or the current position if null", CInt.class, "pos").setOptionalDefaultNull(),
						new Argument("The encoding to use when creating the string", CString.class, "encoding").setOptionalDefault("UTF-8").addAnnotation(new NonNull())
					);
		}
		
	}
	
	
//	private static Integer get_getPos(Construct [] args, Target t){
//		if(args.length == 2){
//			return Static.getInt32(args[1], t);
//		} else {
//			return null;
//		}
//	}
//	
//	private static Integer set_getPos(Construct [] args, Target t){
//		if(args.length == 3){
//			return Static.getInt32(args[2], t);
//		} else {
//			return null;
//		}
//	}
	
	private static abstract class ba extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public String getName() {
			return getClass().getSimpleName();
		}

		public Boolean runAsync() {
			return null;
		}

		public boolean isRestricted() {
			return false;
		}
	}
	
	public static abstract class ba_put extends ba {

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}
		
	}
	
	public static abstract class ba_get extends ba {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.RangeException};
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}
		
	}
	
}
