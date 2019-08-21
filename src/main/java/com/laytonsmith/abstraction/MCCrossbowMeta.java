package com.laytonsmith.abstraction;

import java.util.List;

public interface MCCrossbowMeta extends MCItemMeta {
	boolean hasChargedProjectiles();
	List<MCItemStack> getChargedProjectiles();
	void setChargedProjectiles(List<MCItemStack> projectiles);
}
