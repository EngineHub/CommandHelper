package com.laytonsmith.abstraction;

import java.util.List;

public interface MCBundleMeta extends MCItemMeta {
	List<MCItemStack> getItems();
	void addItem(MCItemStack item);
}
