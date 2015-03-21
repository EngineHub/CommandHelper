package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.entities.MCItemFrame;
import com.laytonsmith.abstraction.enums.MCRotation;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCRotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Hekta
 */
public class BukkitMCItemFrame extends BukkitMCHanging implements MCItemFrame {

	public BukkitMCItemFrame(Entity frame) {
		super(frame);
	}

	public BukkitMCItemFrame(AbstractionObject ao) {
		this((ItemFrame) ao.getHandle());
	}

	@Override
	public ItemFrame getHandle() {
		return (ItemFrame)super.getHandle();
	}

	@Override
	public MCItemStack getItem() {
		ItemStack item = getHandle().getItem();
		if (item != null) {
			return new BukkitMCItemStack(getHandle().getItem());
		} else {
			return null;
		}
	}

	@Override
	public void setItem(MCItemStack item) {
		if (item != null) {
			getHandle().setItem((ItemStack) item.getHandle());
		} else {
			getHandle().setItem(null);
		}
	}

	@Override
	public MCRotation getRotation() {
		return BukkitMCRotation.getConvertor().getAbstractedEnum(getHandle().getRotation());
	}

	@Override
	public void setRotation(MCRotation rotation) {
		getHandle().setRotation(BukkitMCRotation.getConvertor().getConcreteEnum(rotation));
	}
}