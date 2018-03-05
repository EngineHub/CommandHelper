package com.laytonsmith.abstraction;

public interface MCSkullMeta extends MCItemMeta {

	boolean hasOwner();

	String getOwner();

	MCOfflinePlayer getOwningPlayer();

	boolean setOwner(String owner);

	void setOwningPlayer(MCOfflinePlayer player);
}
