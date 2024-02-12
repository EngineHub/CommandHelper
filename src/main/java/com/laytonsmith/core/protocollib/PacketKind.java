package com.laytonsmith.core.protocollib;

import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.Map;

/**
 * Created by JunHyung Im on 2020-07-05
 */
public class PacketKind {

	private final String protocol;
	private final String side;
	private final String name;

	public PacketKind(String protocol, String side, String name) {
		this.protocol = protocol;
		this.side = side;
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("%s_%s_%s", protocol, side, name);
	}

	public CArray toCArray(Target target) {
		CArray typeInfo = new CArray(target, 3);
		typeInfo.set("protocol", protocol);
		typeInfo.set("side", side);
		typeInfo.set("name", name);
		return typeInfo;
	}

	public void write(Map<String, Mixed> map, Target target) {
		map.put("protocol", new CString(protocol, target));
		map.put("side", new CString(side, target));
		map.put("name", new CString(name, target));
	}
}
