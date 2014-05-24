package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCCommandMinecart;
import org.bukkit.entity.minecart.CommandMinecart;

public class BukkitMCCommandMinecart extends BukkitMCMinecart
		implements MCCommandMinecart {

	CommandMinecart cm;
	public BukkitMCCommandMinecart(CommandMinecart e) {
		super(e);
		this.cm = e;
	}

	@Override
	public String getCommand() {
		return cm.getCommand();
	}
	
	@Override
	public String getName() {
		return cm.getName();
	}

	@Override
	public void setName(String name) {
		cm.setName(name);
	}

	@Override
	public void setCommand(String cmd) {
		cm.setCommand(cmd);
	}
}
