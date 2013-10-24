package com.laytonsmith.abstraction.enums.bukkit;

import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCRemoveCause;
import com.laytonsmith.annotations.abstractionenum;

/**
 *
 * @author Hekta
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCRemoveCause.class,
		forConcreteEnum=RemoveCause.class
		)
public class BukkitMCRemoveCause extends EnumConvertor<MCRemoveCause, RemoveCause> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCRemoveCause instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCRemoveCause getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCRemoveCause();
		}
		return instance;
	}
}