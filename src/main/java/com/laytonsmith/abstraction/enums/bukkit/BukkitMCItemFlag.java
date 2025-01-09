package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
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
		forConcreteEnum = ItemFlag.class
)
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
		if((abstracted == MCItemFlag.HIDE_ADDITIONAL_TOOLTIP || abstracted == MCItemFlag.HIDE_STORED_ENCHANTS)
				&& Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_20_6)) {
			return ItemFlag.valueOf("HIDE_POTION_EFFECTS");
		} else if(abstracted == MCItemFlag.HIDE_STORED_ENCHANTS
				&& !((BukkitMCServer) Static.getServer()).isPaper()) {
			return ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
		}
		return ItemFlag.valueOf(abstracted.name());
	}
}
