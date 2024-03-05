package com.laytonsmith.core.functions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CPacket;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.packetjumper.PacketDirection;
import com.laytonsmith.core.packetjumper.PacketEventNotifier;
import com.laytonsmith.core.packetjumper.PacketJumper;
import com.laytonsmith.core.packetjumper.PacketUtils;

/**
 *
 */
public final class XPacketJumper {

	private XPacketJumper() {}

	public static String docs() {
		return "Provides functions related to raw packet management. Note that the use of these functions"
				+ " may break in subtle ways between Minecraft versions, and should only be used as a"
				+ " last resort when other functions don't already do what you need."
				+ " Forward compatibility is NOT guaranteed."
				+ " To use these functions, ProtocolLib must be installed and available.";
	}

	@api
	@hide("Experimental")
	public static class all_packets extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{
				CRECastException.class,
				CREIllegalArgumentException.class
			};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public CArray exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return PacketUtils.getAllPackets();
		}

		@Override
		public String getName() {
			return "all_packets";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "array {} Returns an array containing basic information for all packet types. ---- "
					+ " Note that in general, deprecated packets are included in the list, however, these"
					+ " should generally not be used, as they are either renamed packets, or have been removed"
					+ " from Minecraft. Other packet types, such as completely invalid packets and dynamic"
					+ " packets, are not included.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@api
	@hide("Experimental")
	public static class create_packet extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{
				CRECastException.class,
				CREIllegalArgumentException.class
			};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			PacketDirection direction = ArgumentValidation.getEnum(args[1], PacketDirection.class, t);
			return PacketUtils.createPacket(
					args[0].val().toUpperCase(),
					direction,
					args[2].val(), t);
		}

		@Override
		public String getName() {
			return "create_packet";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "packet {protocol, direction, name} Creates a packet, which can then be written to. Protocol"
					+ " is the name of the protocol, one of " + (StringUtils.Join(PacketType.Protocol.values(), ", ", ", or "))
					+ " direction is either 'IN' or 'OUT', and name is the name specified in all_packets()."
					+ " The returned packet object can then be further modified with packet_write, and sent"
					+ " via send_packet.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

	@api
	@hide("Experimental")
	public static class packet_write extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{
				CRECastException.class,
				CREIllegalArgumentException.class
			};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CPacket packet = ArgumentValidation.getObject(args[0], t, CPacket.class);
			Mixed value = args[2];
			if(args[1].isInstanceOf(CString.TYPE)) {
				String field = ArgumentValidation.getString(args[1], t);
				packet.writeMixed(field, value, t);
			} else if(args[1].isInstanceOf(CInt.TYPE)) {
				int field = ArgumentValidation.getInt32(args[1], t);
				packet.writeMixed(field, value, t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "packet_write";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "void {packet, field, value} Writes a value to the given field of the packet.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@api
	@hide("Experimental")
	public static class send_packet extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{
				CRECastException.class,
				CREIllegalArgumentException.class,
				CREPlayerOfflineException.class,
			};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			int index = 0;
			MCPlayer player = args.length >= 2
					? Static.GetPlayer(args[index++], t)
					: Static.getPlayer(env, t);
			CPacket packet = ArgumentValidation.getObject(args[index++], t, CPacket.class);
			packet.send(player, t);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "send_packet";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], packet} Sends the packet to the given player, or the current player if none specified.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

	@api
	@hide("Experimental")
	public static class register_packet extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{
				CRECastException.class,
				CREIllegalArgumentException.class,
				CREPluginInternalException.class,
			};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String protocol = ArgumentValidation.getStringObject(args[0], t);
			String type = ArgumentValidation.getStringObject(args[1], t);
			PacketEventNotifier notifier = PacketJumper.GetPacketEventNotifier()
					.orElseThrow(() -> new CREPluginInternalException("Packet Jumper not enabled.", t));
			for(PacketType p : PacketType.values()) {
				if(p.getProtocol().name().equals(protocol) && p.name().equals(type)) {
					notifier.register(ListenerPriority.NORMAL, p);
					return CVoid.VOID;
				}
			}
			throw new CREIllegalArgumentException("Cannot find a packet of type \""
					+ protocol + "\":\"" + type + "\"", t);
		}

		@Override
		public String getName() {
			return "register_packet";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {string protocol, string type} Registers the system to listen for packets of the given"
					+ " protocol and type. The packet_sent and packet_received events will only fire for packets"
					+ " that have been explicitely registered for. You can see the list of possible packets with"
					+ " all_packets(). Note that you do not register the direction in this function, but you can"
					+ " filter on direction within the event handlers themselves.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

	@api
	public static class packet_read extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{
				CRECastException.class,
				CRERangeException.class,
				CREIllegalArgumentException.class,
			};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CPacket packet = ArgumentValidation.getObject(args[0], t, CPacket.class);
			if(args[1].isInstanceOf(CInt.TYPE)) {
				int index = ArgumentValidation.getInt32(args[1], t);
				return packet.readMixed(index, t);
			} else {
				return packet.readMixed(args[1].val(), t);
			}
		}

		@Override
		public String getName() {
			return "packet_read";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "mixed {packet, index} Returns the value at the index. The index should be either a string or an"
					+ "int. The string version uses the mojang mappings, and the int version passes the value"
					+ " directly through to ProtocolLib.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}

	@api
	public static class packet_info extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] { CRECastException.class };
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CPacket packet = ArgumentValidation.getObject(args[0], t, CPacket.class);
			return packet.toCArray();
		}

		@Override
		public String getName() {
			return "packet_info";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "array {packet} Returns data about the given packet.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

	}
}
