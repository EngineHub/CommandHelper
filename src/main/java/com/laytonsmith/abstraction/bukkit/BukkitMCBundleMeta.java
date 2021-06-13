package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCBundleMeta;
import com.laytonsmith.abstraction.MCItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCBundleMeta extends BukkitMCItemMeta implements MCBundleMeta {

	BundleMeta bm;

	public BukkitMCBundleMeta(BundleMeta m) {
		super(m);
		this.bm = m;
	}

	public BukkitMCBundleMeta(AbstractionObject o) {
		super(o);
		this.bm = (BundleMeta) o;
	}

	@Override
	public List<MCItemStack> getItems() {
		List<ItemStack> items = this.bm.getItems();
		List<MCItemStack> abstractItems = new ArrayList<>(items.size());
		for(ItemStack is : items) {
			abstractItems.add(new BukkitMCItemStack(is));
		}
		return abstractItems;
	}

	@Override
	public void addItem(MCItemStack abstractItem) {
		this.bm.addItem((ItemStack) abstractItem.getHandle());
	}
}
