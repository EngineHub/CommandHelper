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

public class BukkitMCItemFrame extends BukkitMCHanging implements MCItemFrame {

	public BukkitMCItemFrame(Entity frame) {
		super(frame);
	}

	public BukkitMCItemFrame(AbstractionObject ao) {
		this((ItemFrame) ao.getHandle());
	}

	@Override
	public ItemFrame getHandle() {
		return (ItemFrame) super.getHandle();
	}

	@Override
	public MCItemStack getItem() {
		return new BukkitMCItemStack(getHandle().getItem());
	}

	@Override
	public void setItem(MCItemStack item) {
		if(item != null) {
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

	@Override
	public boolean isVisible() {
		try {
			return getHandle().isVisible();
		} catch (NoSuchMethodError ex) {
			// prior to 1.16
		}
		return true;
	}

	@Override
	public void setVisible(boolean visible) {
		try {
			getHandle().setVisible(visible);
		} catch (NoSuchMethodError ex) {
			// prior to 1.16
		}
	}

	@Override
	public boolean isFixed() {
		try {
			return getHandle().isFixed();
		} catch (NoSuchMethodError ex) {
			// prior to 1.16
		}
		return false;
	}

	@Override
	public void setFixed(boolean fixed) {
		try {
			getHandle().setFixed(fixed);
		} catch (NoSuchMethodError ex) {
			// prior to 1.16
		}
	}
}
