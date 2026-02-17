package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.entities.MCItemDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.inventory.ItemStack;

public class BukkitMCItemDisplay extends BukkitMCDisplay implements MCItemDisplay {

	ItemDisplay id;

	public BukkitMCItemDisplay(Entity e) {
		super(e);
		this.id = (ItemDisplay) e;
	}

	@Override
	public MCItemStack getItem() {
		ItemStack item = this.id.getItemStack();
		if(item == null) {
			return null;
		}
		return new BukkitMCItemStack(item);
	}

	@Override
	public void setItem(MCItemStack item) {
		if(item == null) {
			this.id.setItemStack(null);
		} else {
			this.id.setItemStack((ItemStack) item.getHandle());
		}
	}

	@Override
	public ModelTransform getItemModelTransform() {
		return ModelTransform.valueOf(this.id.getItemDisplayTransform().name());
	}

	@Override
	public void setItemModelTransform(ModelTransform transform) {
		this.id.setItemDisplayTransform(ItemDisplayTransform.valueOf(transform.name()));
	}
}
