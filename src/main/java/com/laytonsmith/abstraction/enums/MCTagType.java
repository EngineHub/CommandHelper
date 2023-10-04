package com.laytonsmith.abstraction.enums;

import com.laytonsmith.abstraction.MCNamespacedKey;
import com.laytonsmith.abstraction.MCTagContainer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.function.Function;

/**
 * Minecraft NBT types, with functions to convert to and from MethodScript constructs.
 */
public enum MCTagType {
	BYTE(
			(Mixed v) -> ArgumentValidation.getInt8(v, v.getTarget()),
			(Byte v) -> new CInt(((Number) v).longValue(), Target.UNKNOWN)),
	BYTE_ARRAY(
			(Mixed v) -> {
				CArray array = ArgumentValidation.getArray(v, v.getTarget());
				if(array.isAssociative()) {
					throw new CRECastException("Expected byte array to not be associative.", v.getTarget());
				}
				byte[] bytes = new byte[(int) array.size()];
				int i = 0;
				for(Mixed m : array) {
					bytes[i++] = ArgumentValidation.getInt8(m, m.getTarget());
				}
				return bytes;
			},
			(byte[] array) -> {
				CArray r = new CArray(Target.UNKNOWN);
				for(int i : array) {
					r.push(new CInt(i, Target.UNKNOWN), Target.UNKNOWN);
				}
				return r;
			}),
	DOUBLE(
			(Mixed v) -> ArgumentValidation.getDouble(v, v.getTarget()),
			(Double v) -> new CDouble(v, Target.UNKNOWN)),
	FLOAT(
			(Mixed v) -> ArgumentValidation.getDouble32(v, v.getTarget()),
			(Float v) -> new CDouble(v.doubleValue(), Target.UNKNOWN)),
	INTEGER(
			(Mixed v) -> ArgumentValidation.getInt32(v, v.getTarget()),
			(Integer v) -> new CInt(((Number) v).longValue(), Target.UNKNOWN)),
	INTEGER_ARRAY(
			(Mixed v) -> {
				CArray array = ArgumentValidation.getArray(v, v.getTarget());
				if(array.isAssociative()) {
					throw new CRECastException("Expected integer array to not be associative.", v.getTarget());
				}
				int[] ints = new int[(int) array.size()];
				int i = 0;
				for(Mixed m : array) {
					ints[i++] = ArgumentValidation.getInt32(m, m.getTarget());
				}
				return ints;
			},
			(int[] array) -> {
				CArray r = new CArray(Target.UNKNOWN);
				for(int i : array) {
					r.push(new CInt(i, Target.UNKNOWN), Target.UNKNOWN);
				}
				return r;
			}),
	LONG(
			(Mixed v) -> ArgumentValidation.getInt(v, v.getTarget()),
			(Long v) -> new CInt(((Number) v).longValue(), Target.UNKNOWN)),
	LONG_ARRAY(
			(Mixed v) -> {
				CArray array = ArgumentValidation.getArray(v, v.getTarget());
				if(array.isAssociative()) {
					throw new CRECastException("Expected long array to not be associative.", v.getTarget());
				}
				long[] longs = new long[(int) array.size()];
				int i = 0;
				for(Mixed m : array) {
					longs[i++] = ArgumentValidation.getInt(m, m.getTarget());
				}
				return longs;
			},
			(long[] array) -> {
				CArray ret = new CArray(Target.UNKNOWN);
				for(long i : array) {
					ret.push(new CInt(i, Target.UNKNOWN), Target.UNKNOWN);
				}
				return ret;
			}),
	SHORT(
			(Mixed v) -> ArgumentValidation.getInt16(v, v.getTarget()),
			(Short v) -> new CInt(((Number) v).longValue(), Target.UNKNOWN)),
	STRING(
			(Mixed v) -> v.val(),
			(String v) -> new CString(v, Target.UNKNOWN)),
	TAG_CONTAINER(
			(Mixed v) -> {
				throw new UnsupportedOperationException();
			},
			(MCTagContainer v) -> {
				throw new UnsupportedOperationException();
			}),
	TAG_CONTAINER_ARRAY(
			(Mixed v) -> {
				throw new UnsupportedOperationException();
			},
			(MCTagContainer[] v) -> {
				throw new UnsupportedOperationException();
			});

	private final Function conversion;
	private final Function construction;

	<T extends Mixed, Z> MCTagType(Function<T, Z> conversion, Function<Z, T> construction) {
		this.conversion = conversion;
		this.construction = construction;
	}

	/**
	 * Returns a Java object from a MethodScript construct.
	 * Throws a ConfigRuntimeException if the value is not valid for this tag type.
	 * @param container the tag container context
	 * @param value MethodScript construct
	 * @return a Java object
	 */
	public Object convert(MCTagContainer container, Mixed value) {
		if(this == TAG_CONTAINER) {
			if(!value.isInstanceOf(CArray.TYPE)) {
				throw new CREFormatException("Expected tag container to be an array.", value.getTarget());
			}
			CArray containerArray = (CArray) value;
			if(!containerArray.isAssociative()) {
				throw new CREFormatException("Expected tag container array to be associative.", value.getTarget());
			}
			for(String key : containerArray.stringKeySet()) {
				Mixed possibleArray = containerArray.get(key, value.getTarget());
				if(!possibleArray.isInstanceOf(CArray.TYPE)) {
					throw new CREFormatException("Expected tag entry to be an array.", possibleArray.getTarget());
				}
				CArray entryArray = (CArray) possibleArray;
				if(!entryArray.isAssociative()) {
					throw new CREFormatException("Expected tag array to be associative.", entryArray.getTarget());
				}
				Mixed entryType = entryArray.get("type", entryArray.getTarget());
				Mixed entryValue = entryArray.get("value", entryArray.getTarget());
				MCTagType tagType;
				try {
					tagType = MCTagType.valueOf(entryType.val());
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException("Tag type is not valid: " + entryType.val(), entryType.getTarget());
				}
				Object tagValue;
				if(tagType == MCTagType.TAG_CONTAINER) {
					tagValue = tagType.convert(container.newContainer(), entryValue);
				} else if(tagType == TAG_CONTAINER_ARRAY) {
					tagValue = tagType.convert(container, entryValue);
				} else {
					tagValue = tagType.convert(container, entryValue);
				}
				try {
					container.set(StaticLayer.GetConvertor().GetNamespacedKey(key), tagType, tagValue);
				} catch (ClassCastException ex) {
					throw new CREFormatException("Tag value does not match expected type.", entryValue.getTarget());
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException(ex.getMessage(), entryValue.getTarget());
				}
			}
			return container;
		} else if(this == TAG_CONTAINER_ARRAY) {
			if(!value.isInstanceOf(CArray.TYPE)) {
				throw new CREFormatException("Expected tag container to be an array.", value.getTarget());
			}
			CArray array = (CArray) value;
			if(array.isAssociative()) {
				throw new CREFormatException("Expected tag container array to not be associative.", array.getTarget());
			}
			MCTagContainer[] containers = new MCTagContainer[(int) array.size()];
			int i = 0;
			for(Mixed possibleContainer : array) {
				containers[i++] = (MCTagContainer) TAG_CONTAINER.convert(container.newContainer(), possibleContainer);
			}
			return containers;
		}
		return conversion.apply(value);
	}

	/**
	 * Returns a MethodScript construct from a Java object.
	 * Throws a ClassCastException if the value does not match this tag type.
	 * @param value a valid Java object
	 * @return a MethodScript construct
	 */
	public Mixed construct(Object value) throws ClassCastException {
		if(this == TAG_CONTAINER) {
			MCTagContainer container = (MCTagContainer) value;
			CArray containerArray = CArray.GetAssociativeArray(Target.UNKNOWN);
			for(MCNamespacedKey key : container.getKeys()) {
				CArray entry = CArray.GetAssociativeArray(Target.UNKNOWN);
				MCTagType type = container.getType(key);
				entry.set("type", type.name(), Target.UNKNOWN);
				entry.set("value", type.construct(container.get(key, type)), Target.UNKNOWN);
				containerArray.set(key.toString(), entry, Target.UNKNOWN);
			}
			return containerArray;
		} else if(this == TAG_CONTAINER_ARRAY) {
			MCTagContainer[] containers = (MCTagContainer[]) value;
			CArray array = new CArray(Target.UNKNOWN, containers.length);
			for(MCTagContainer container : containers) {
				array.push(TAG_CONTAINER.construct(container), Target.UNKNOWN);
			}
			return array;
		}
		return (Mixed) construction.apply(value);
	}
}
