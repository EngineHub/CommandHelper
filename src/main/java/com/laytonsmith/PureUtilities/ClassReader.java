package com.laytonsmith.PureUtilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that reads in a class file, and provides class info for it.
 */
public class ClassReader {
    public static void main(String[] args) throws IOException {
		//This only works if running from inside a maven project
		go(new File("target/classes/" + ClassReader.class.getName().replace(".", "/") + ".class"));
	}

	public static void go(File f) throws IOException {
		byte[] b = readFile(f);
		ClassInfo ci = new ClassInfo(b);		
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
		private int super_class;
		private int interfaces_count;
		private int[] interfaces;
		private int fields_count;
		//private field_info[] fields;
		private int methods_count;
		//private method_info[] methods;
		private int attributes_count;
		//private attribute_info[] attributes;
                
                public String className(){
                    return this_class;
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
		 * Treat superclass methods specially when invoked by the 
		 * invokespecial instruction.
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
		
		public ClassInfo(byte[] clazz){
			this.clazz = clazz;
			process();
		}	
		
		private void process(){			
			magic = toInt(consume(4));
			if(magic != 0xCAFEBABE){
				throw new ClassCastException("Invalid magic for class file!");
			}
			minor_version = toInt(consume(2));
			major_version = toInt(consume(2));
			constant_pool_count = toInt(consume(2));
                        constant_pool.add(null);
			for(int i = 0; i < constant_pool_count - 1; i++){
				int tag = toInt(consume(1));
				switch(tag){
					case 1:
						//UTF-8 Unicode
						int length = toInt(consume(2));
						String string = toString(consume(length));
						constant_pool.add(string);
						break;
					case 3:
						int integer = toInt(consume(4));
						constant_pool.add(integer);
						break;
					case 4:
						float f = 0;
						consume(4); //TODO
						constant_pool.add(f);
						break;
					case 5:
					case 6:
						consume(8);
                                                constant_pool.add(null);
						break;
					case 7:
                                                int index = toInt(consume(2));
                                                constant_pool.add(index);
                                                break;
					case 8:
						consume(2);
                                                constant_pool.add(null);
						break;
					case 9:
					case 10:
					case 11:
					case 12:
						consume(4);
                                                constant_pool.add(null);
						break;
					default:
						throw new ClassCastException("Invalid tag supplied in constant pool: " + tag);						
				}
			}
			access_flags = toInt(consume(2));
                        this_class = (String)constant_pool.get((Integer)constant_pool.get(toInt(consume(2))));
                        this_class = this_class.replace("/", ".");
                        System.out.println(this_class);
                        //TODO: Finish this as needed
		}
		
		ByteArrayInputStream stream;
		Map<Integer, byte[]> temps = new HashMap<Integer, byte[]>();
		private byte[] consume(int size){
			byte[] temp;
			if(temps.containsKey(size)){
				temp = temps.get(size);
			} else {
				temp = new byte[size];
				temps.put(size, temp);
			}			
			for(int i = 0; i < size; i++){
				temp[i] = clazz[offset];
				offset++;
			}
			return temp;
		}
		                
		private int toInt(byte[] b){
			int num = 0;
                        int start = 0;
                        int stop = b.length;
                        for(int i = start; i < stop; i++){
                                int temp = (b[i] & 0x00FF) << ((b.length - i - 1) * 8);
                                num |= temp;
                        }
			return num;
		}
		
		private String toString(byte[] b){
			char[] c = new char[b.length];
			for(int i = 0; i < b.length; i++){
				c[i] = (char)b[i];
			}
			return String.copyValueOf(c);
		}
		
				
	}
}
