package com.laytonsmith.core.packetjumper;

import com.comphenix.protocol.PacketType;
import com.laytonsmith.annotations.MEnum;

/**
 *
 */
@MEnum("com.commandhelper.PacketDirection")
public enum PacketDirection {
	IN, OUT;

	private PacketDirection() {
	}

	public static PacketDirection FromSender(PacketType.Sender sender) {
		return sender == PacketType.Sender.CLIENT ? IN : OUT;
	}
}
