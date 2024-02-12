package com.laytonsmith.core.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CPacket;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.functions.XPacketJumper;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.fabricmc.mappingio.tree.MappingTree;

/**
 * Code adapted from code by JunHyung Im
 */
public final class PacketUtils {

	private PacketUtils() {}

	private static volatile CArray allPackets = null;
	private static final Object ALL_PACKETS_LOCK = new Object();

	/**
	 * Returns a list of packets which can be returned to the user, and also used internally.
	 *
	 * @return
	 */
	public static CArray getAllPackets() {
		return getAllPacketsInternal().clone();
	}

	/**
	 * Gets the packet structure, but does not clone it. Don't change the structure.
	 * @return
	 */
	private static CArray getAllPacketsInternal() {
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		CArray allPackets = PacketUtils.allPackets;
		if(allPackets == null) {
			synchronized(ALL_PACKETS_LOCK) {
				allPackets = PacketUtils.allPackets;
				if(allPackets == null) {
					CArray packetTypeArray = new CArray(Target.UNKNOWN);
					for(PacketType.Protocol protocol : PacketType.Protocol.values()) {
						CArray subtypes = new CArray(Target.UNKNOWN);
						subtypes.set("IN", new CArray(Target.UNKNOWN), Target.UNKNOWN);
						subtypes.set("OUT", new CArray(Target.UNKNOWN), Target.UNKNOWN);
						packetTypeArray.set(protocol.name(), subtypes, Target.UNKNOWN);
					}
					List<PacketType> output = new ArrayList<>();
					output.addAll(PacketRegistry.getServerPacketTypes());
					output.addAll(PacketRegistry.getClientPacketTypes());
					Set<String> unsupportedTypes = new HashSet<>();
					for(PacketType type : output) {
						if(type.name() == null) {
							// Not sure how to deal with these types of packets.
							continue;
						}
						if(type.isDynamic()) {
							continue;
						}
						try {
							CArray array = (CArray)((ArrayAccess)packetTypeArray.get(type.getProtocol().name(), Target.UNKNOWN))
									.get(type.getSender() == PacketType.Sender.CLIENT ? "IN" : "OUT", Target.UNKNOWN);
							array.set(type.name().toUpperCase(), getPacketInfo(type, unsupportedTypes), Target.UNKNOWN);
						} catch(ClassNotFoundException ex) {
							Logger.getLogger(XPacketJumper.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
					CArray array = new CArray(Target.UNKNOWN);
					for(String s : unsupportedTypes) {
						array.push(s);
					}
					packetTypeArray.set("__UnsupportedTypes", array, Target.UNKNOWN);
					PacketUtils.allPackets = packetTypeArray;
				}
			}
		}
		return PacketUtils.allPackets;
	}

	private static CArray getPacketInfo(PacketType type, Set<String> unsupportedTypes) throws ClassNotFoundException {
		int serverType = PacketJumper.GetServerNamespace();
		CArray arr = new CArray(Target.UNKNOWN);
		arr.set("protocol", type.getProtocol().name());
		arr.set("name", type.name().toUpperCase());
		arr.set("sender", type.getSender().name());
		arr.set("deprecated", CBoolean.get(type.isDeprecated()), Target.UNKNOWN);
		MappingTree tree = PacketJumper.GetMappingTree();
		MappingTree.ClassMapping mapping = tree.getClass(type.getPacketClass().getName().replace(".", "/"), serverType);
		Class clazz = ClassUtils.forCanonicalName(mapping.getName(serverType).replace("/", "."));
		arr.set("class", mapping.getName(PacketJumper.GetMojangNamespace()).replace("/", "."));
		arr.set("superclass", clazz.getSuperclass().getName());
		CArray fields = new CArray(Target.UNKNOWN);
		int index = 0;
		do {
			for(MappingTree.FieldMapping fieldMapping : mapping.getFields()) {
				CArray field = new CArray(Target.UNKNOWN);
				field.set("name", fieldMapping.getName(PacketJumper.GetMojangNamespace()));
				Mixed typeData = CNull.NULL;
				try {
					typeData = getTypeData(fieldMapping.getSrcName(),
							fieldMapping.getDesc(PacketJumper.GetMojangNamespace()),
							clazz,
							ClassUtils.forCanonicalName(ClassUtils
									.getCommonNameFromJVMName(fieldMapping.getDesc(serverType))), unsupportedTypes);
				} catch(ClassNotFoundException | NoSuchFieldException ex) {
					Logger.getLogger(XPacketJumper.class.getName()).log(Level.SEVERE, null, ex);
				}
				field.set("type", typeData, Target.UNKNOWN);
				int currentId = index++;
				field.set("field", currentId);
				fields.set(fieldMapping.getName(PacketJumper.GetMojangNamespace()), field, Target.UNKNOWN);
			}
			clazz = clazz.getSuperclass();
			mapping = tree.getClass(clazz.getName().replace(".", "/"), serverType);
		} while(clazz != Object.class && clazz != Record.class);
		arr.set("fields", fields, Target.UNKNOWN);
		arr.set("id", new CInt(type.getCurrentId(), Target.UNKNOWN), Target.UNKNOWN);
		return arr;
	}

	private static CArray getTypeData(String nativeFieldName, String type, Class containingClazz, Class clazz,
			Set<String> unsupportedTypes) throws NoSuchFieldException, ClassNotFoundException {
		CArray array = new CArray(Target.UNKNOWN);
		CArray enumTypes = new CArray(Target.UNKNOWN);
		CClassType ctype = Conversions.getTypeConversion(clazz);
		if(clazz.isEnum()) {
			for(Object o : clazz.getEnumConstants()) {
				enumTypes.push(new CString(o.toString(), Target.UNKNOWN), Target.UNKNOWN);
			}
			ctype = CString.TYPE;
			array.set("enumType", ClassUtils.getCommonNameFromJVMName(type));
			array.set("enumValues", enumTypes, Target.UNKNOWN);
		}
		if(ctype == null) {
			unsupportedTypes.add(type);
		}
		array.set("type", (ctype == null ? "<UNSUPPORTED>" : ctype.getFQCN().getFQCN()));
		if(CArray.TYPE.equals(ctype)) {
			// Grab the generic type
			if(type.startsWith("[")) {
				// The "generic" type is just the array type. Currently the protocol only supports 1 deep arrays.
				Class subtype = ClassUtils.forCanonicalName(ClassUtils.getCommonNameFromJVMName(type.substring(1)));
				CClassType genericType = Conversions.getTypeConversion(subtype);
				array.set("genericType", (genericType == null
						? new CString("<UNSUPPORTED>", Target.UNKNOWN) : genericType), Target.UNKNOWN);
			} else {
				Field f = containingClazz.getDeclaredField(nativeFieldName);
				if(f.getGenericType() instanceof ParameterizedType generic) {
					Type[] types = generic.getActualTypeArguments();
					CArray genericType = parseSubtype(types, clazz);
					if(genericType != null) {
						array.set("genericType", genericType, Target.UNKNOWN);
					}
				}
			}
		}
		array.set("originalType", ClassUtils.getCommonNameFromJVMName(type));
		return array;
	}

	private static CArray parseSubtype(Type[] types, Class clazz) {
		CArray array = new CArray(Target.UNKNOWN);
		Type t = types[0];
		if(clazz == Map.class) {
			// Types will be 2, but we can't really support this if the key isn't a string.
			if(!t.getTypeName().equals("java.lang.String")) {
				array.set("type", "<UNSUPPORTED>");
			} else {
				t = types[1];
			}
		} else if(t instanceof Class type) {
			CClassType c = Conversions.getTypeConversion(type);
			array.set("type", (c == null ? new CString("<UNSUPPORTED>", Target.UNKNOWN) : c), Target.UNKNOWN);
			array.set("originalType", type.getName());
		} else if(t instanceof ParameterizedType type) {
			CClassType c = Conversions.getTypeConversion((Class) type.getRawType());
			array.set("type", (c == null ? new CString("<UNSUPPORTED>", Target.UNKNOWN) : c), Target.UNKNOWN);
			array.set("originalType", ((Class) type.getRawType()).getName());
			if(c != null && type.getActualTypeArguments().length > 0) {
				CArray genericType = parseSubtype(type.getActualTypeArguments(), (Class) type.getRawType());
				if(genericType != null) {
					array.set("genericType", genericType, Target.UNKNOWN);
				}
			}
		}
		if(t instanceof WildcardType) {
			// T<?> so we can just return null here, as we don't need to worry about the subtype.
			return null;
		}
		return array;
	}

	public static PacketType.Sender getSide(String name, Target target) {
		switch(name) {
			case "IN":
				return PacketType.Sender.CLIENT;
			case "OUT":
				return PacketType.Sender.SERVER;
		}
		throw new CREIllegalArgumentException("Unknown sender type: " + name, target);
	}

	public static PacketType findPacketType(String protocol, String name, Target target) {
		try {
			// Convert the packet class name into the current server version name, since this will always
			// be the mojang version, not necessarily the current server version.
			MappingTree tree = PacketJumper.GetMappingTree();
			name = tree.getClass(name.replace(".", "/"), PacketJumper.GetMojangNamespace())
					.getDstName(PacketJumper.GetServerNamespace()).replace("/", ".");
			return PacketRegistry.getPacketType(PacketType.Protocol.valueOf(protocol), Class.forName(name));
		} catch(Exception exception) {
			throw new CREIllegalArgumentException("Error while finding the packet of type \"" + name + "\"."
					+ " Check the results of all_packets() for information about valid packet types.", target, exception);
		}
	}

	public static CPacket createPacket(String protocol, String side, String name, Target target) {
		try {
			CArray packetData = (CArray)((ArrayAccess)((ArrayAccess)getAllPacketsInternal().get(protocol, target))
					.get(side, target))
					.get(name, target);
			String clazz = packetData.get("class", target).val();
			name = clazz;
		} catch(CREIndexOverflowException ioe) {
			// Do nothing, use the name exactly as is.
		}
		PacketContainer container = ProtocolLibrary.getProtocolManager().createPacket(findPacketType(protocol, name, target));
		return CPacket.create(container, name, target, PacketType.Protocol.valueOf(protocol),
				(side.equals("IN") ? PacketType.Sender.CLIENT : PacketType.Sender.SERVER));
	}

	public static PacketKind getPacketKind(PacketType type) {
		return new PacketKind(
				type.getProtocol().name(),
				type.getSender().getPacketName().toUpperCase(),
				type.name().toUpperCase()
		);
	}
}
