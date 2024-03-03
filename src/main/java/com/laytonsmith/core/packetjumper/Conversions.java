package com.laytonsmith.core.packetjumper;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.fabricmc.mappingio.tree.MappingTree;
import org.bukkit.inventory.ItemStack;

/**
 * Created by JunHyung Im on 2020-07-05
 */
public final class Conversions {

	private Conversions() {}

	public static Object convertMixedToObject(Mixed mixed, Class<?> type, Target t) {
		// When adding to this list, ensure you update the Packet_Jumper docs,
		// as well as the getTypeConversion method.
		if(Enum.class.isAssignableFrom(type)) {
			return getEnum(mixed.val(), type);
//		} else if(Optional.class.isAssignableFrom(type)) {
//			CArray array = ArgumentValidation.getArray(mixed, t);
//			switch((int) array.size()) {
//				case 0:
//					return Optional.empty();
//				case 1:
//					// TODO: Need the generic type to get this working. It's available, at least in all_packets.
//					throw new Error();
//				default:
//					throw new CRELengthException("Arrays representing Optionals must only be of length 0 or 1.", t);
//			}
		} else if(MinecraftReflection.getIChatBaseComponentClass().isAssignableFrom(type)) {
			String contents = mixed.val();
			return contents.startsWith("{") && contents.endsWith("}")
					? WrappedChatComponent.fromJson(contents).getHandle()
					: WrappedChatComponent.fromText(contents).getHandle();
		} else if(MinecraftReflection.getItemStackClass().isAssignableFrom(type)) {
			return ObjectGenerator.GetGenerator().item(mixed, t);
		} else if(MinecraftReflection.getBlockPositionClass().isAssignableFrom(type)) {
			CArray array = ArgumentValidation.getArray(mixed, t);
			int x = ArgumentValidation.getInt32(array.get("x", t), t);
			int y = ArgumentValidation.getInt32(array.get("y", t), t);
			int z = ArgumentValidation.getInt32(array.get("z", t), t);
			return ReflectionUtils.newInstance(MinecraftReflection.getBlockPositionClass(),
					new Class[]{int.class, int.class, int.class},
					new Object[]{x, y, z});
		}
		return Static.getJavaObject(mixed);
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
		// When adding to this list, ensure you update the Packet_Jumper docs,
		// as well as the convertObject/MixedToMixed/Object methods.
		// Minecraft Types
//		if(Optional.class.isAssignableFrom(clazz)) {
//			return CArray.TYPE;
//		}
		if(MinecraftReflection.getIChatBaseComponentClass().isAssignableFrom(clazz)) {
			return CString.TYPE;
		} else if(MinecraftReflection.getItemStackClass().isAssignableFrom(clazz)) {
			return CArray.TYPE;
		} else if(MinecraftReflection.getBlockPositionClass().isAssignableFrom(clazz)) {
			return CArray.TYPE;
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

	public static Mixed convertObjectToMixed(Object object) {
		// When adding to this list, ensure you update the Packet_Jumper docs,
		// as well as the getTypeConversion method.
		if(object == null) {
			return CNull.NULL;
		}
//		if(object instanceof Optional optional) {
//			CArray a = new CArray(Target.UNKNOWN);
//			if(optional.isPresent()) {
//				a.push(convertObjectToMixed(optional.get()), Target.UNKNOWN);
//			}
//			return a;
//		}
		// Useful for finding the methods
//		java.util.List<String> list = java.util.stream.Stream.of(object.getClass().getDeclaredMethods()).map(x -> x.getName())
//				.toList();
//		System.out.println(list);
		Class clazz = object.getClass();
		if(object instanceof Enum aEnum) {
			return new CString(aEnum.name(), Target.UNKNOWN);
		} else if(MinecraftReflection.getIChatBaseComponentClass().isAssignableFrom(clazz)) {
			return new CString(object.toString(), Target.UNKNOWN);
		} else if(MinecraftReflection.getItemStackClass().isAssignableFrom(clazz)) {
			Object itemStack = ReflectionUtils.invokeMethod(object, "getBukkitStack");
			return ObjectGenerator.GetGenerator().item(new BukkitMCItemStack((ItemStack) itemStack), Target.UNKNOWN);
		} else if(MinecraftReflection.getBlockPositionClass().isAssignableFrom(clazz)) {
			java.util.List<String> list = java.util.stream.Stream.of(object.getClass().getDeclaredMethods()).map(x -> x.getName())
					.toList();
 			System.out.println(list);
			MappingTree tree = PacketJumper.GetMappingTree();
			MappingTree.ClassMapping vec3i
					= tree.getClass(object.getClass().getSuperclass().getName().replace(".", "/"), PacketJumper.GetServerNamespace());
			int x = ReflectionUtils.invokeMethod(object, vec3i.getMethod("getX", null, PacketJumper.GetMojangNamespace()).getSrcName());
			int y = ReflectionUtils.invokeMethod(object, vec3i.getMethod("getY", null, PacketJumper.GetMojangNamespace()).getSrcName());
			int z = ReflectionUtils.invokeMethod(object, vec3i.getMethod("getZ", null, PacketJumper.GetMojangNamespace()).getSrcName());
			CArray array = new CArray(Target.UNKNOWN);
			array.set("x", x);
			array.set("y", y);
			array.set("z", z);
			return array;
		}
		return Static.getMSObject(object, Target.UNKNOWN);
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
