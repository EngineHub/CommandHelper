package com.laytonsmith.core.packetjumper;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.core.constructs.CPacket;
import com.laytonsmith.core.constructs.Target;

/**
 * Created by JunHyung Im on 2020-07-05
 */
public class ProtocolLibPacketEvent implements BindablePacketEvent {
    private final PacketEvent packetEvent;

	public ProtocolLibPacketEvent(PacketEvent packetEvent) {
		this.packetEvent = packetEvent;
	}

    @Override
    public MCPlayer getPlayer() {
        return new BukkitMCPlayer(packetEvent.getPlayer());
    }

    @Override
    public PacketKind getKind() {
        return PacketUtils.getPacketKind(packetEvent.getPacketType());
    }

    @Override
    public CPacket getPacket(Target target) {
        return CPacket.create(packetEvent.getPacket(), packetEvent.getPacketType().name(), target,
				packetEvent.getPacketType().getProtocol(), packetEvent.getPacketType().getSender());
    }

    @Override
    public PacketContainer getInternalPacket() {
        return packetEvent.getPacket();
    }

	public PacketEvent getPacketEvent() {
		return packetEvent;
	}

    @Override
    public Object _GetObject() {
        return packetEvent;
    }
}
