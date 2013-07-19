package com.laytonsmith.PureUtilities;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that reads in a class file, and provides class info for it.
 *
 * @author lsmith
 */
public class ClassReader {

//	public static void main(String[] args) throws IOException {
//		//This only works if running from inside a maven project
////		go(new File("target/classes/" + TestClass.class.getName().replace(".", "/") + ".class"));
//	}

	public static void go(File f) throws IOException {
		byte[] b = readFile(f);
		ClassInfo ci = new ClassInfo(b);
		System.out.println("Magic: 0x" + Integer.toHexString(ci.magic()).toUpperCase());
		System.out.println("Class name: " + ci.className());
		System.out.println("Major Version: " + ci.majorVersion());
		System.out.println("Minor Version: " + ci.minorVersion());
		System.out.println("Compiled for Java " + JavaVersion.fromMajorVersion(ci.majorVersion()).toString());
		System.out.println("Super class: " + ci.superClass());
		System.out.println("Interfaces: " + ci.interfaces());
	}

	public static byte[] readFile(File file) throws IOException {
		// Open file
		RandomAccessFile f = new RandomAccessFile(file, "r");

		try {
			// Get and check length
			long longlength = f.length();
			int length = (int) longlength;
			if (length != longlength) {
				throw new IOException("File size >= 2 GB");
			}

			// Read file and return data
			byte[] data = new byte[length];
			f.readFully(data);
			return data;
		} finally {
			f.close();
		}
	}

	public static class ClassInfo {

		private int magic;
		private int minor_version;
		private int major_version;
		private int constant_pool_count;
		private List<Object> constant_pool = new ArrayList<Object>();
		private int access_flags;
		private String this_class;
		private String super_class;
		private int interfaces_count;
		private String[] interfaces;
		private int fields_count;
		//private field_info[] fields;
		private int methods_count;
		//private method_info[] methods;
		private int attributes_count;
		//private attribute_info[] attributes;

		/**
		 * Returns the value of the magic of the class file. This will currently
		 * always return 0xCAFEBABE
		 * @return 
		 */
		public int magic(){
			return magic;
		}
		
		/**
		 * Returns the name of this class. If the class is an inner class, it will be
		 * named <code>fully.qualified.classname.Outer$Inner</code>
		 * @return 
		 */
		public String className() {
			return this_class;
		}
		
		/**
		 * The class file's major version.
		 * @return 
		 */
		public int majorVersion(){
			return major_version;
		}
		
		/**
		 * The class file's minor version.
		 * @return 
		 */
		public int minorVersion(){
			return minor_version;
		}
		
		/**
		 * Returns the name of the super class, subject to the same
		 * conventions that className() follows.
		 * @return 
		 */
		public String superClass(){
			return super_class;
		}
		
		/**
		 * Returns a list of the implemented interfaces in this class.
		 * @return 
		 */
		public List<String> interfaces(){
			return Arrays.asList(interfaces);
		}
		
		//Class access and property modifiers
		/**
		 * Declared public; may be accessed from outside its package.
		 */
		public static final int ACC_PUBLIC = 0x0001;
		/**
		 * Declared final; no subclasses allowed.
		 */
		public static final int ACC_FINAL = 0x0010;
		/**
		 * Treat superclass methods specially when invoked by the invokespecial
		 * instruction.
		 */
		public static final int ACC_SUPER = 0x0020;
		/**
		 * Is an interface, not a class.
		 */
		public static final int ACC_INTERFACE = 0x0200;
		/**
		 * Declared abstract; must not be instantiated.
		 */
		public static final int ACC_ABSTRACT = 0x0400;
		/**
		 * Declared synthetic; Not present in the source code.
		 */
		public static final int ACC_SYNTHETIC = 0x1000;
		/**
		 * Declared as an annotation type.
		 */
		public static final int ACC_ANNOTATION = 0x2000;
		/**
		 * Declared as an enum type.
		 */
		public static final int ACC_ENUM = 0x4000;

		public ClassInfo() {
		}
		int offset = 0;
		boolean processed = false;
		byte[] clazz;

		public ClassInfo(byte[] clazz) {
			this.clazz = clazz;
			process();
		}

		private void process() {
			magic = toInt(consume(4));
			if (magic != 0xCAFEBABE) {
				throw new ClassCastException("Invalid magic for class file!");
			}
			minor_version = toInt(consume(2));
			major_version = toInt(consume(2));
			constant_pool_count = toInt(consume(2));
			System.out.println("There are " + constant_pool_count);
 			constant_pool.add(null);
			for (int i = 0; i < constant_pool_count - 1; i++) {
				int tag = consume(1)[0];
				switch (tag) {
					case 1: {
						//UTF-8 Unicode
						int length = toInt(consume(2));
						String string = toString(consume(length));
						constant_pool.add(string);
						break;
					}
					case 3: {
						//Integer
						int integer = toInt(consume(4));
						constant_pool.add(integer);
						break;
					}
					case 4: {
						//Float
						float f = toFloat(consume(4));
						constant_pool.add(f);
						break;
					}
					//Doubles and longs take up two constant pool slots, so
					//increment i for both of them.
					case 5: {
						//Long
						long l = toLong(consume(8));
						constant_pool.add(l);
						i++;
						break;
					}
					case 6: {
						//Double
						double d = toDouble(consume(8));
						constant_pool.add(d);
						i++;
						break;
					}
					case 7: {
						//Class reference
						int index = toInt(consume(2));
						constant_pool.add(index);
						break;
					}
					case 8: {
						//String reference
						int index = toInt(consume(2));
						constant_pool.add(index);
						break;
					}
					case 9: {
						//Field reference
						int classReference = toInt(consume(2));
						int nameAndTypeReference = toInt(consume(2));
						constant_pool.add(new MemberReference(MemberReferenceType.FIELD, classReference, nameAndTypeReference));
						break;
					}
					case 10: {
						//Method reference
						int classReference = toInt(consume(2));
						int nameAndTypeReference = toInt(consume(2));
						constant_pool.add(new MemberReference(MemberReferenceType.METHOD, classReference, nameAndTypeReference));
						break;
					}
					case 11: {
						//Interface reference
						int classReference = toInt(consume(2));
						int nameAndTypeReference = toInt(consume(2));
						constant_pool.add(new MemberReference(MemberReferenceType.INTERFACE, classReference, nameAndTypeReference));
						break;
					}
					case 12: {
						//Name and type descriptor
						int nameIndex = toInt(consume(2));
						int typeIndex = toInt(consume(2));
						NameAndType nat = new NameAndType(nameIndex, typeIndex);
						constant_pool.add(nat);
						break;
					}
					default:
						throw new RuntimeException("Invalid tag supplied in constant pool: " + tag + " (at constant pool index " + i + ")");
				}
			}
			access_flags = toInt(consume(2));
			this_class = (String) constant_pool.get((Integer) constant_pool.get(toInt(consume(2))));
			this_class = this_class.replace("/", ".");
			super_class = (String) constant_pool.get((Integer) constant_pool.get(toInt(consume(2))));
			super_class = super_class.replace("/", ".");
			interfaces_count = toInt(consume(2));
			interfaces = new String[interfaces_count];
			for(int i = 0; i < interfaces_count; i++){
				interfaces[i] = (String) constant_pool.get((Integer) constant_pool.get(toInt(consume(2))));
			}
			fields_count = toInt(consume(2));
			for(int i = 0; i < fields_count; i++){
				
			}
			//TODO: Finish this as needed
		}
		ByteArrayInputStream stream;
		Map<Integer, byte[]> temps = new HashMap<Integer, byte[]>();

		private byte[] consume(int size) {
			byte[] temp;
			if (temps.containsKey(size)) {
				temp = temps.get(size);
			} else {
				temp = new byte[size];
				temps.put(size, temp);
			}
			for (int i = 0; i < size; i++) {
				temp[i] = clazz[offset];
				offset++;
			}
			return temp;
		}

		private static int toInt(byte[] b) {
			int num = 0;
			for (int i = 0; i < b.length; i++) {
				int temp = (b[i] & 0x00FF) << ((b.length - i - 1) * 8);
				num |= temp;
			}
			return num;
		}
		
		private static float toFloat(byte[] b){
			int bits = toInt(b);
			if(bits == 0x7f800000){
				return Float.POSITIVE_INFINITY;
			} else if(bits == 0xff800000){
				return Float.NEGATIVE_INFINITY;
			} else if(bits >= 0x7f800001 && bits <= 0x7fffffff
					|| bits >= 0xff800001 && bits <= 0xffffffff){
				return Float.NaN;
			} else {
				int s = ((bits >> 31) == 0) ? 1 : -1;
				int e = ((bits >> 23) & 0xff);
				int m = (e == 0) ? 
						(bits & 0x7fffff) << 1 :
						(bits & 0x7fffff) | 0x800000;
				return (float)(s * m * Math.pow(2, e - 150));
			}
		}
		
		private static long toLong(byte[] b){
			int high_bytes = toInt(new byte[]{b[0], b[1], b[2], b[3]});
			int low_bytes = toInt(new byte[]{b[4], b[5], b[6], b[7]});
			return (long)(((long)high_bytes << 32) + low_bytes);
		}
		
		private static double toDouble(byte[] b){
			long bits = toLong(b);
			if(bits == 0x7ff0000000000000L){
				return Double.POSITIVE_INFINITY;
			} else if(bits == 0xfff0000000000000L){
				return Double.NEGATIVE_INFINITY;
			} else if(bits >= 0x7ff0000000000001L && bits <= 0x7fffffffffffffffL
					|| bits >= 0xfff0000000000001L && bits <= 0xffffffffffffffffL){
				return Double.NaN;
			} else {
				int s = ((bits >> 63) == 0) ? 1 : -1;
				int e = (int)((bits >> 52) & 0x7ffL);
				long m = (e == 0) ?
						(bits & 0xfffffffffffffL) << 1 :
						(bits & 0xfffffffffffffL) | 0x10000000000000L;
				return s * m * Math.pow(2, e - 1075);
			}
		}

		private String toString(byte[] b) {
			char[] c = new char[b.length];
			for (int i = 0; i < b.length; i++) {
				c[i] = (char) b[i];
			}
			return String.copyValueOf(c);
		}
		
		public static enum MemberReferenceType{
			FIELD("Field"), METHOD("Method"), INTERFACE("Interface");
			private final String name;
			private MemberReferenceType(String name){
				this.name = name;
			}

			@Override
			public String toString() {
				return name;
			}
			
		}
		private class MemberReference {
			private int classReference;
			private int nameAndTypeReference;
			private MemberReferenceType type;
			
			
			public MemberReference(MemberReferenceType type, int classReference, int nameAndTypeReference){
				this.type = type;
				this.classReference = classReference;
				this.nameAndTypeReference = nameAndTypeReference;
			}
			
			public int getClassReference(){
				return classReference;
			}
			
			public int getNameAndTypeReference(){
				return nameAndTypeReference;
			}
			
			public String getClassName(){
				return (String) constant_pool.get(classReference);
			}
			
			public NameAndType getNameAndType(){
				return (NameAndType) constant_pool.get(nameAndTypeReference);
			}

			@Override
			public String toString() {
				String className;
				String nat;
				try {
					className = getClassName();
				} catch(Exception e){
					className = "(unresolved class name)";
				}
				try{
					nat = getNameAndType().toString();
				} catch(Exception e){
					nat = "(unresolved name and type)";
				}
				return "Member reference type: " + type + "; Class name: " + className + "; " + nat;
			}
			
		}
		
		private class NameAndType {
			private int nameIndex;
			private int typeIndex;

			public NameAndType(int nameIndex, int typeIndex){
				this.nameIndex = nameIndex;
				this.typeIndex = typeIndex;
			}

			public int getNameIndex(){
				return nameIndex;
			}

			public int getTypeIndex(){
				return typeIndex;
			}
			
			public String getName(){
				return (String) constant_pool.get(nameIndex);
			}
			
			public String getType(){
				return (String) constant_pool.get(typeIndex);
			}

			@Override
			public String toString() {
				try{
					return "Name: " + getName() + "; Type: " + getType();
				} catch(Exception e){
					return "Name: (unresolved) " + nameIndex + "; Type: (unresolved) " + typeIndex;
				}
			}
			
		}
	}
	
	
	/**
	 * Enums that correspond to known Java class file versions.
	 */
	public static enum JavaVersion {
		JAVA_1_1("1.1", 45),
		JAVA_1_2("1.2", 46),
		JAVA_1_3("1.3", 47),
		JAVA_1_4("1.4", 48),
		JAVA_5("5.0", 49),
		JAVA_6("6.0", 50),
		JAVA_7("7.0", 51);
		
		private String versionString;
		private int versionNumber;
		
		private JavaVersion(String versionString, int versionNumber){
			this.versionString = versionString;
			this.versionNumber = versionNumber;
		}

		@Override
		public String toString() {
			return versionString;
		}
		
		/**
		 * The major version number in class files corresponding to this
		 * version of Java.
		 * @return 
		 */
		public int getVersion(){
			return versionNumber;
		}
		
		/**
		 * Returns the JavaVersion given a major version number from
		 * a class file. If the version isn't recognized, null is returned.
		 * @param version
		 * @return 
		 */
		public static JavaVersion fromMajorVersion(int version){
			for(JavaVersion v : values()){
				if(v.versionNumber == version){
					return v;
				}
			}
			return null;
		}
		
	}
}
