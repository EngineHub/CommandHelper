package com.laytonsmith.core.packetjumper;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PacketInfo implements Comparable<PacketInfo> {

	private final int packetID;
	private final String packetName;
	private final Class<?> packetClass;
	private final Constructor<?>[] constructors;
	private final Class[][] arguments;
	private final String docs;

	public PacketInfo(Class<?> packetClass) {
		this.packetClass = packetClass;
		constructors = (Constructor<?>[]) packetClass.getConstructors();
		arguments = new Class[constructors.length][];
		for (int i = 0; i < constructors.length; i++) {
			Constructor<?> constructor = constructors[i];
			arguments[i] = constructor.getParameterTypes();
		}
		packetID = Integer.parseInt(packetClass.getSimpleName().replaceAll(".*?(\\d+).*", "$1"));
		packetName = packetClass.getSimpleName().replaceAll(".*\\d+(.*)", "$1");
		//TODO: Parse docs
		docs = "Docs coming soon!";
	}

	public int getPacketID() {
		return packetID;
	}

	public String getPacketName() {
		return packetName;
	}

	public Class<?> getPacketClass() {
		return packetClass;
	}

	public Constructor<?>[] getConstructors() {
		return constructors;
	}

	public Class[] getArguments() {
		Class[] a = new Class[arguments.length];
		System.arraycopy(arguments, 0, a, 0, arguments.length);
		return a;
	}

	public String getDocs() {
		return docs;
	}

	@Override
	public String toString() {
		List<String> l = new ArrayList<>();
		for (Class[] args : arguments) {
			l.add(StringUtils.Join(args, ", "));
		}
		return packetClass.getName() + "(" + StringUtils.Join(l, " | ") + ")";
	}

	@Override
	public int compareTo(PacketInfo o) {
		if (this.packetID == o.packetID) {
			return 0;
		}
		return this.packetID < o.packetID ? -1 : 1;
	}

}
