package com.laytonsmith.core.packetjumper;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.constructs.CPacket;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.BindableEvent;

/**
 * Created by JunHyung Im on 2020-07-05
 */
public interface BindablePacketEvent extends BindableEvent {

	MCPlayer getPlayer();

	PacketKind getKind();

	CPacket getPacket(Target target);

	Object getInternalPacket();
}
