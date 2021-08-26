package com.laytonsmith.core.asm;

import java.util.HashMap;
import java.util.Map;

/**
 * This enum lists the supported data types in the system. Note that for instance "i49" is a perfectly valid data type
 * in LLVM, but we don't have support for it, to reduce the complexity here.
 */
public enum IRType {
	/**
	 * 1 bit integer, that is, a boolean.
	 */
	INTEGER1("i1", Category.INTEGER, 1),
	/**
	 * 8 bit integer
	 */
	INTEGER8("i8", Category.INTEGER, 8),
	/**
	 * 16 bit integer
	 */
	INTEGER16("i16", Category.INTEGER, 16),
	/**
	 * 32 bit integer
	 */
	INTEGER32("i32", Category.INTEGER, 32),
	/**
	 * 64 bit integer. Note that in MethodScript, "int" is defined as 64 bit, but
	 * many native libraries use other integer sizes.
	 */
	INTEGER64("i64", Category.INTEGER, 64),
	/**
	 * A pointer to a 8 bit integer
	 */
	INTEGER8POINTER("i8*", Category.POINTER, -1),
	/**
	 * A 2D pointer to an 8 bit integer
	 */
	INTEGER8POINTERPOINTER("i8**", Category.POINTER, -1),
	/**
	 * 16 bit floating point value
	 */
	HALF("half", Category.FLOAT, 16),
	/**
	 * 32 bit floating point value
	 */
	FLOAT("float", Category.FLOAT, 32),
	/**
	 * 64 bit floating point value. Note that in MethodScript, "double" is defined as 64 bit.
	 */
	DOUBLE("double", Category.FLOAT, 64),
	/**
	 * 128 bit floating point value
	 */
	FP128("fp128", Category.FLOAT, 128),
	/**
	 * A string is just an i8* (a char array) but for convenience, we refer to it as a different type.
	 */
	STRING("i8*", Category.POINTER, -1),
	/**
	 * OTHER is a type which means that this value can't be parsed directly. This should only be used for values which
	 * are used internally within a single instruction group, and never returned as a type. (This is checked in the
	 * relevant places in IRBuilder and an error is thrown).
	 */
	OTHER(null, Category.UNSET, -1);

	private final String irType;
	private final Category category;
	private final Integer bitDepth;

	private IRType(String irType, Category category, Integer bitDepth) {
		this.irType = irType;
		this.category = category;
		this.bitDepth = bitDepth;
	}

	public String getIRType() {
		return this.irType;
	}

	public Category getCategory() {
		return this.category;
	}

	/**
	 * Returns true if the value is variable length. If this is variable length, and getBitDepth is called on this
	 * type, an Error is thrown, instead, you have to dynamically calculate the bit depth.
	 * @return
	 */
	public boolean isVariableLength() {
		return bitDepth == -1;
	}

	/**
	 * Returns the bit depth for this data type. For pointers, this is the bit depth of the underlying data, rather
	 * than the size of the memory address. If the data is variable length (i.e. an array) then an exception is thrown.
	 * In this case, you need to dynamically calculate the size of the data.
	 * @return
	 */
	public Integer getBitDepth() {
		if(bitDepth < 0) {
			throw new RuntimeException("Cannot call getBitDepth on a variable length data type. Use isVariableLength"
					+ " if the datatype is unsure.");
		}
		return bitDepth;
	}

	private static Map<String, IRType> stringToTypeMap = null;

	/**
	 * Returns the enum type given the LLVM string. Returns null if it's an invalid (or not yet implemented) type.
	 * @param irType
	 * @return
	 */
	public static IRType fromString(String irType) {
		if(stringToTypeMap == null) {
			stringToTypeMap = new HashMap<>();
			for(IRType type : IRType.values()) {
				stringToTypeMap.put(type.irType, type);
			}
		}
		return stringToTypeMap.get(irType);
	}

	public static enum Category {
		UNSET,
		INTEGER,
		FLOAT,
		POINTER,
		ARRAY
	}
}
