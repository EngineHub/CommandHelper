package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCItemFlag;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.inventory.ItemFlag;

/**
 * A BukkitMCItemFlag can hide some Attributes from BukkitMCItemStacks, through BukkitMCItemMeta.
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCItemFlag.class,
		forConcreteEnum = ItemFlag.class)
public class BukkitMCItemFlag extends EnumConvertor<MCItemFlag, ItemFlag> {
	private static BukkitMCItemFlag instance;

	public static BukkitMCItemFlag getConvertor() {
		if(instance == null) {
			instance = new BukkitMCItemFlag();
		}
		return instance;
	}

	@Override
	protected ItemFlag getConcreteEnumCustom(MCItemFlag abstracted) {
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_8)) {
			return null;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}