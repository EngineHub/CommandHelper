package com.laytonsmith.core.constructs;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.packetjumper.Conversions;
import com.laytonsmith.core.packetjumper.PacketJumper;
import com.laytonsmith.core.packetjumper.PacketKind;
import com.laytonsmith.core.packetjumper.PacketUtils;
import java.lang.reflect.Field;
import net.fabricmc.mappingio.tree.MappingTree;

/**
 * Created by JunHyung Im on 2020-07-05
 */
@typeof("com.commandhelper.Packet")
public final class CPacket extends Construct {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(CPacket.class);

	private final PacketContainer packet;
	private final PacketType.Protocol protocol;
	private final PacketType.Sender sender;
	private final String name;
	private final String packetType;

	private CPacket(String value, ConstructType ctype, String name, Target t, PacketContainer packet,
			PacketType.Protocol protocol, PacketType.Sender sender) {
		super(value, ctype, t);
		this.packet = packet;
		this.name = name;
		this.protocol = protocol;
		this.sender = sender;
		this.packetType = packet.getType().name();
	}

	public static CPacket create(PacketContainer packet, String name, Target target,
			PacketType.Protocol protocol, PacketType.Sender sender) {
		return new CPacket("PacketData", ConstructType.PACKET, name, target, packet, protocol, sender);
	}

	/**
	 * Returns the packet name.
	 *
	 * @return
	 */
	public String getPacketName() {
		return name;
	}

//    public static CPacket create(Environment env, Target target) {
//        return create(CPacketEvent.getPacket(env, target), target);
//    }
	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return new CClassType[]{Mixed.TYPE};
	}

	@Override
	public String docs() {
		return "Wraps a packet class, which is used for raw packet manipulation.";
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_5;
	}

	@Override
	public CClassType[] getInterfaces() {
		return CClassType.EMPTY_CLASS_ARRAY;
	}

	public int indexFromString(String field, Target t) {
		CArray packetData = (CArray) ((ArrayAccess) ((ArrayAccess) PacketUtils.getAllPackets()
				.get(protocol.name(), t))
				.get(sender == PacketType.Sender.CLIENT ? "IN" : "OUT", t))
				.get(packetType, t);
		CArray fieldData = (CArray) ((ArrayAccess) packetData.get("fields", t)).get(field, t);
		int index = (int) ((CInt) fieldData.get("field", t)).getInt();
		return index;
	}

	public Object read(int index) {
		return packet.getModifier().read(index);
	}

	public Mixed readMixed(int index, Target target) {
		return Conversions.convertObjectToMixed(read(index));
	}

	public Mixed readMixed(String index, Target target) {
		return readMixed(indexFromString(index, target), target);
	}

	public void write(int index, Object object) {
		packet.getModifier().write(index, object);
	}

	public void writeMixed(int index, Mixed mixed, Target t) {
		Field field = packet.getModifier().getField(index);
		Class<?> type = field.getType();
		write(index, Conversions.adjustObject(Conversions.convertMixedToObject(mixed, type, t), type));
	}

	public void writeMixed(String field, Mixed mixed, Target t) {
		writeMixed(indexFromString(field, t), mixed, t);
	}

	public CArray getFields(Target target) {
		CArray information = new CArray(target);
		for(FieldAccessor field : packet.getModifier().getFields()) {
			information.push(new CString(field.getField().getType().getSimpleName(), target), target);
		}
		return information;
	}

	public void send(MCPlayer player, Target target) {
		ProtocolLibrary.getProtocolManager().sendServerPacket(
				((BukkitMCPlayer) player)._Player(),
				packet
		);
	}

	public PacketKind getKind() {
		return PacketUtils.getPacketKind(packet.getType());
	}

	public CArray toCArray() {
		CArray array = new CArray(Target.UNKNOWN);
		MappingTree tree = PacketJumper.GetMappingTree();
		for(FieldAccessor field : packet.getModifier().getFields()) {
			Mixed value;
			Object instance = field.get(packet.getHandle());
			String type = field.getField().getType().getSimpleName();
			String name;
			Class clazz = packet.getHandle().getClass();
			MappingTree.ClassMapping classMapping
					= tree.getClass(clazz.getName().replace(".", "/"), PacketJumper.GetServerNamespace());
			MappingTree.FieldMapping fm;
			do {
				fm = classMapping.getField(field.getField().getName(), null);
				if(fm != null) {
					break;
				}
				clazz = clazz.getSuperclass();
				if(clazz == Object.class || clazz == Record.class) {
					break;
				}
				classMapping = tree.getClass(clazz.getName().replace(".", "/"),
						PacketJumper.GetServerNamespace());
				if(classMapping == null) {
					throw new CREPluginInternalException("Cannot find packet superclass.", Target.UNKNOWN);
				}
			} while(true);
			name = fm.getDstName(PacketJumper.GetMojangNamespace());
			if(instance == null) {
				value = CNull.NULL;
			} else if(Conversions.getTypeConversion(field.getField().getType()) == null) {
				value = new CString("<UNSUPPORTED:" + instance.toString() + ">", Target.UNKNOWN);
			} else {
				value = Conversions.convertObjectToMixed(instance);
			}
			CArray descriptor = new CArray(Target.UNKNOWN);
			descriptor.set("name", name);
			descriptor.set("type", type);
			descriptor.set("value", value, Target.UNKNOWN);
			array.set(name, descriptor, Target.UNKNOWN);
		}
		return array;
	}

}
