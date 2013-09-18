package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCIgniteCause;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

/**
 *
 * @author MariuszT
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCIgniteCause.class,
		forConcreteEnum = IgniteCause.class)
public class BukkitMCIgniteCause extends EnumConvertor<MCIgniteCause, IgniteCause> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCIgniteCause instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCIgniteCause getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCIgniteCause();
		}
		return instance;
	}
}
