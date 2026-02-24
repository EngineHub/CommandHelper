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
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.function.BiFunction;

/**
 * Minecraft NBT types, with functions to convert to and from MethodScript constructs.
 */
public enum MCTagType {
	BYTE(
			(Mixed v, Environment env) -> ArgumentValidation.getInt8(v, v.getTarget(), env),
			(Byte v, Environment env) -> new CInt(((Number) v).longValue(), Target.UNKNOWN)),
	BYTE_ARRAY(
			(Mixed v, Environment env) -> {
				CArray array = ArgumentValidation.getArray(v, v.getTarget(), env);
				if(array.isAssociative()) {
					throw new CRECastException("Expected byte array to not be associative.", v.getTarget());
				}
				byte[] bytes = new byte[(int) array.size(env)];
				int i = 0;
				for(Mixed m : array) {
					bytes[i++] = ArgumentValidation.getInt8(m, m.getTarget(), env);
				}
				return bytes;
			},
			(byte[] array, Environment env) -> {
				CArray r = new CArray(Target.UNKNOWN, null, env);
				for(int i : array) {
					r.push(new CInt(i, Target.UNKNOWN), Target.UNKNOWN, env);
				}
				return r;
			}),
	DOUBLE(
			(Mixed v, Environment env) -> ArgumentValidation.getDouble(v, v.getTarget(), env),
			(Double v, Environment env) -> new CDouble(v, Target.UNKNOWN)),
	FLOAT(
			(Mixed v, Environment env) -> ArgumentValidation.getDouble32(v, v.getTarget(), env),
			(Float v, Environment env) -> new CDouble(v.doubleValue(), Target.UNKNOWN)),
	INTEGER(
			(Mixed v, Environment env) -> ArgumentValidation.getInt32(v, v.getTarget(), env),
			(Integer v, Environment env) -> new CInt(((Number) v).longValue(), Target.UNKNOWN)),
	INTEGER_ARRAY(
			(Mixed v, Environment env) -> {
				CArray array = ArgumentValidation.getArray(v, v.getTarget(), env);
				if(array.isAssociative()) {
					throw new CRECastException("Expected integer array to not be associative.", v.getTarget());
				}
				int[] ints = new int[(int) array.size(env)];
				int i = 0;
				for(Mixed m : array) {
					ints[i++] = ArgumentValidation.getInt32(m, m.getTarget(), env);
				}
				return ints;
			},
			(int[] array, Environment env) -> {
				CArray r = new CArray(Target.UNKNOWN, null, env);
				for(int i : array) {
					r.push(new CInt(i, Target.UNKNOWN), Target.UNKNOWN, env);
				}
				return r;
			}),
	LONG(
			(Mixed v, Environment env) -> ArgumentValidation.getInt(v, v.getTarget(), env),
			(Long v, Environment env) -> new CInt(((Number) v).longValue(), Target.UNKNOWN)),
	LONG_ARRAY(
			(Mixed v, Environment env) -> {
				CArray array = ArgumentValidation.getArray(v, v.getTarget(), env);
				if(array.isAssociative()) {
					throw new CRECastException("Expected long array to not be associative.", v.getTarget());
				}
				long[] longs = new long[(int) array.size(env)];
				int i = 0;
				for(Mixed m : array) {
					longs[i++] = ArgumentValidation.getInt(m, m.getTarget(), env);
				}
				return longs;
			},
			(long[] array, Environment env) -> {
				CArray ret = new CArray(Target.UNKNOWN, null, env);
				for(long i : array) {
					ret.push(new CInt(i, Target.UNKNOWN), Target.UNKNOWN, env);
				}
				return ret;
			}),
	SHORT(
			(Mixed v, Environment env) -> ArgumentValidation.getInt16(v, v.getTarget(), env),
			(Short v, Environment env) -> new CInt(((Number) v).longValue(), Target.UNKNOWN)),
	STRING(
			(Mixed v, Environment env) -> v.val(),
			(String v, Environment env) -> new CString(v, Target.UNKNOWN)),
	TAG_CONTAINER(
			(Mixed v, Environment env) -> {
				throw new UnsupportedOperationException();
			},
			(MCTagContainer v, Environment env) -> {
				throw new UnsupportedOperationException();
			}),
	TAG_CONTAINER_ARRAY(
			(Mixed v, Environment env) -> {
				throw new UnsupportedOperationException();
			},
			(MCTagContainer[] v, Environment env) -> {
				throw new UnsupportedOperationException();
			});

	private final BiFunction conversion;
	private final BiFunction construction;

	<T extends Mixed, Z> MCTagType(BiFunction<T, Environment, Z> conversion, BiFunction<Z, Environment, T> construction) {
		this.conversion = conversion;
		this.construction = construction;
	}

	/** @deprecated Use {@link #convert(MCTagContainer, Mixed, Environment)} instead. */
	@Deprecated
	public Object convert(MCTagContainer container, Mixed value) {
		return convert(container, value, null);
	}

	/**
	 * Returns a Java object from a MethodScript construct.
	 * Throws a ConfigRuntimeException if the value is not valid for this tag type.
	 * @param container the tag container context
	 * @param value MethodScript construct
	 * @param env
	 * @return a Java object
	 */
	public Object convert(MCTagContainer container, Mixed value, Environment env) {
		if(this == TAG_CONTAINER) {
			if(!value.isInstanceOf(CArray.TYPE, null, env)) {
				throw new CREFormatException("Expected tag container to be an array.", value.getTarget());
			}
			CArray containerArray = (CArray) value;
			if(!containerArray.isAssociative()) {
				throw new CREFormatException("Expected tag container array to be associative.", value.getTarget());
			}
			for(String key : containerArray.stringKeySet()) {
				Mixed possibleArray = containerArray.get(key, value.getTarget());
				if(!possibleArray.isInstanceOf(CArray.TYPE, null, env)) {
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
					tagValue = tagType.convert(container.newContainer(), entryValue, env);
				} else if(tagType == TAG_CONTAINER_ARRAY) {
					tagValue = tagType.convert(container, entryValue, env);
				} else {
					tagValue = tagType.convert(container, entryValue, env);
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
			if(!value.isInstanceOf(CArray.TYPE, null, env)) {
				throw new CREFormatException("Expected tag container to be an array.", value.getTarget());
			}
			CArray array = (CArray) value;
			if(array.isAssociative()) {
				throw new CREFormatException("Expected tag container array to not be associative.", array.getTarget());
			}
			MCTagContainer[] containers = new MCTagContainer[(int) array.size(env)];
			int i = 0;
			for(Mixed possibleContainer : array) {
				containers[i++] = (MCTagContainer) TAG_CONTAINER.convert(container.newContainer(), possibleContainer, env);
			}
			return containers;
		}
		return conversion.apply(value, env);
	}

	/**
	 * Returns a MethodScript construct from a Java object.
	 * Throws a ClassCastException if the value does not match this tag type.
	 * @param value a valid Java object
	 * @return a MethodScript construct
	 */
	public Mixed construct(Object value, Environment env) throws ClassCastException {
		if(this == TAG_CONTAINER) {
			MCTagContainer container = (MCTagContainer) value;
			CArray containerArray = CArray.GetAssociativeArray(Target.UNKNOWN, null, env);
			for(MCNamespacedKey key : container.getKeys()) {
				CArray entry = CArray.GetAssociativeArray(Target.UNKNOWN, null, env);
				MCTagType type = container.getType(key);
				entry.set("type", type.name(), Target.UNKNOWN, env);
				entry.set("value", type.construct(container.get(key, type), env), Target.UNKNOWN, env);
				containerArray.set(key.toString(), entry, Target.UNKNOWN, env);
			}
			return containerArray;
		} else if(this == TAG_CONTAINER_ARRAY) {
			MCTagContainer[] containers = (MCTagContainer[]) value;
			CArray array = new CArray(Target.UNKNOWN, containers.length, null, env);
			for(MCTagContainer container : containers) {
				array.push(TAG_CONTAINER.construct(container, env), Target.UNKNOWN, env);
			}
			return array;
		}
		return (Mixed) construction.apply(value, env);
	}
}
