package com.laytonsmith.core.protocollib;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * Created by JunHyung Im on 2020-07-05
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class Conversions {

	private Conversions() {
	}

	public static Object convertMixedToObject(Mixed mixed) {
		return Static.getJavaObject(mixed);
	}

	public static Object convertMixedToObject(Mixed mixed, Class<?> type) {
		if(Enum.class.isAssignableFrom(type)) {
			return getEnum(mixed.val(), type);
		} else if(MinecraftReflection.getIChatBaseComponentClass().isAssignableFrom(type)) {
			String contents = mixed.val();
			return contents.startsWith("{") && contents.endsWith("}")
					? WrappedChatComponent.fromJson(contents).getHandle()
					: WrappedChatComponent.fromText(contents).getHandle();
		}
		return convertMixedToObject(mixed);
	}

	/**
	 * Returns the CClassType which the given java class would be converted to. For instance,
	 * java.lang.String returns CString.TYPE. Null is returned if the object type is not supported by the conversion
	 * methods in this class.
	 *
	 * @param clazz
	 * @return
	 */
	public static CClassType getTypeConversion(Class clazz) {
		CArray enumTypes = new CArray(Target.UNKNOWN);
		String desc = ClassUtils.getJVMName(clazz);
		// Enums
		if(clazz.isEnum()) {
			for(Object o : clazz.getEnumConstants()) {
				enumTypes.push(new CString(o.toString(), Target.UNKNOWN), Target.UNKNOWN);
			}
			return CString.TYPE;
		}
		// Basic types
		switch(desc) {
			case "I":
				return CInt.TYPE;
			case "Z":
				return CBoolean.TYPE;
			case "B":
			case "J":
			case "S":
				return CInt.TYPE;
			case "D":
			case "F":
				return CDouble.TYPE;
			case "Ljava/lang/String;":
			case "Ljava/util/UUID;":
				return CString.TYPE;
			case "Ljava/util/List;":
			case "Ljava/util/Collection;":
			case "Ljava/util/Set;":
			case "Ljava/util/EnumSet;":
			case "Ljava/util/Map;":
			case "[Ljava/lang/String;":
			case "[I":
			case "[S":
				return CArray.TYPE;
			case "[B":
				return CByteArray.TYPE;
		}
		// Minecraft Types
		if(MinecraftReflection.getIChatBaseComponentClass().isAssignableFrom(clazz)) {
			return CString.TYPE;
		}
		return null;
	}

	public static Object adjustObject(Object object, Class<?> type) {
		if(type == int.class && object instanceof Number n) {
			return n.intValue();
		}
		if(type == short.class && object instanceof Number n) {
			return n.shortValue();
		}
		if(type == byte.class && object instanceof Number n) {
			return n.byteValue();
		}
		
		return object;
	}

	public static Mixed convertObjectToMixed(Object object, Target target) {
		if(object instanceof Enum) {
			return new CString(((Enum) object).name(), target);
		}
		return Static.getMSObject(object, target);
	}

	public static Object getEnum(String name, Class enumType) {
		try {
			return Enum.valueOf(enumType, name);
		} catch(IllegalArgumentException ex) {
			Enum<?>[] enums = (Enum<?>[]) ReflectionUtils.invokeMethod(enumType, null, "values");
			throw new IllegalArgumentException(String.format("%s has elements [%s]",
					enumType.getSimpleName(), StringUtils.Join(enums, ", ")));
		}
	}
}
