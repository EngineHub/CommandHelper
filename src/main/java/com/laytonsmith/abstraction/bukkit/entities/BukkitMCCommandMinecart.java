package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCCommandMinecart;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.CommandMinecart;

public class BukkitMCCommandMinecart extends BukkitMCMinecart implements MCCommandMinecart {

	CommandMinecart cm;

	public BukkitMCCommandMinecart(Entity e) {
		super(e);
		this.cm = (CommandMinecart) e;
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

	@Override
	public void sendMessage(String string) {
		cm.sendMessage(string);
	}

	@Override
	public boolean isOp() {
		return cm.isOp();
	}

	@Override
	public boolean hasPermission(String perm) {
		return cm.hasPermission(perm);
	}

	@Override
	public boolean isPermissionSet(String perm) {
		return cm.isPermissionSet(perm);
	}

	@Override
	public List<String> getGroups() {
		// CommandMinecarts cannot be in a group.
		return new ArrayList<String>();
	}

	@Override
	public boolean inGroup(String groupName) {
		// CommandMinecarts cannot be in a group.
		return false;
	}
}
