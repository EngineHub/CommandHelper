package com.laytonsmith.abstraction;

public interface MCCompassMeta extends MCItemMeta {

	MCLocation getTargetLocation();

	void setTargetLocation(MCLocation location);

	boolean isLodestoneTracked();

	void setLodestoneTracked(boolean tracked);
}
