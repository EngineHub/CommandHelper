
package com.laytonsmith.core.packetjumper;

import java.util.Set;

/**
 *
 */
public class PacketInstance {
	private final Set<Object> data;
	private final PacketInfo packetInfo;
	
	public PacketInstance(PacketInfo packetInfo, Set<Object> data){
		this.packetInfo = packetInfo;
		this.data = data;
	}
	
	public Set<Object> getData(){
		return this.data;
	}
	
	public PacketInfo getPacketInfo(){
		return this.packetInfo;
	}
}
