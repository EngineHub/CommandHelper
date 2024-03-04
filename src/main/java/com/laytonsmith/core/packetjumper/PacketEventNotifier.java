package com.laytonsmith.core.packetjumper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.plugin.Plugin;

/**
 * Created by JunHyung Im on 2020-07-05
 */
public class PacketEventNotifier {

	private final Plugin plugin;
	private final ProtocolManager protocolManager;
	private final Collection<PacketAdapter> packetAdapters = new ArrayList<>();
	private final Set<PacketType> registeredTypes = new HashSet<>();

	public PacketEventNotifier(Plugin plugin, ProtocolManager protocolManager) {
		this.plugin = plugin;
		this.protocolManager = protocolManager;
	}

	public void onPacketReceiving(PacketEvent event) {
		ProtocolLibPacketEvent packetEvent = new ProtocolLibPacketEvent(event);
		EventUtils.TriggerListener(Driver.PACKET_RECEIVED, "packet_received", packetEvent);
	}

	public void onPacketSending(PacketEvent event) {
		ProtocolLibPacketEvent packetEvent = new ProtocolLibPacketEvent(event);
		EventUtils.TriggerListener(Driver.PACKET_SENT, "packet_sent", packetEvent);
	}

	public void register(ListenerPriority priority, PacketType type) {
		if(this.registeredTypes.contains(type)) {
			return;
		}
		PacketAdapter adapter = new PacketAdapter(plugin, priority, type) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketEventNotifier.this.onPacketReceiving(event);
			}

			@Override
			public void onPacketSending(PacketEvent event) {
				PacketEventNotifier.this.onPacketSending(event);
			}
		};
		this.packetAdapters.add(adapter);
		this.protocolManager.addPacketListener(adapter);
		this.registeredTypes.add(type);
	}

	public void unregister() {
		for(PacketAdapter adapter : this.packetAdapters) {
			this.protocolManager.removePacketListener(adapter);
		}
		this.packetAdapters.clear();
		this.registeredTypes.clear();
	}
}
