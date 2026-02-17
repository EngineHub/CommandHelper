package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCTeleportCause.class,
		forConcreteEnum = TeleportCause.class
)
public class BukkitMCTeleportCause extends EnumConvertor<MCTeleportCause, TeleportCause> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCTeleportCause instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCTeleportCause getConvertor() {
		if(instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCTeleportCause();
		}
		return instance;
	}

	@Override
	protected MCTeleportCause getAbstractedEnumCustom(TeleportCause concrete) {
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_5)
				&& concrete.name().equals("CONSUMABLE_EFFECT")) {
			return MCTeleportCause.CHORUS_FRUIT;
		}
		return super.getAbstractedEnumCustom(concrete);
	}

	@Override
	protected TeleportCause getConcreteEnumCustom(MCTeleportCause abstracted) {
		BukkitMCServer server = (BukkitMCServer) Static.getServer();
		if(server.isPaper() && server.getMinecraftVersion().gte(MCVersion.MC1_21_5)
				&& abstracted == MCTeleportCause.CHORUS_FRUIT) {
			return TeleportCause.valueOf("CONSUMABLE_EFFECT");
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}
