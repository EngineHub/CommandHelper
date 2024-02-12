package com.laytonsmith.core.functions;

import com.comphenix.protocol.PacketType;
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
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.protocollib.PacketUtils;

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
			String direction = args[1].val().toUpperCase();
			if(!"IN".equals(direction) && !"OUT".equals(direction)) {
				throw new CREIllegalArgumentException("Direction must be one of \"IN\" or \"OUT\".", t);
			}
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
				packet.writeMixed(field, value);
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
			throw new UnsupportedOperationException("Not supported yet.");
		}

	}
}
