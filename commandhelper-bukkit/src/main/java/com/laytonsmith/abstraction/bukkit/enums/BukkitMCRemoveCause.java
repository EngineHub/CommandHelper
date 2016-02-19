package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCRemoveCause;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

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

	private static BukkitMCRemoveCause instance;

	public static BukkitMCRemoveCause getConvertor() {
		if (instance == null) {
			instance = new BukkitMCRemoveCause();
		}
		return instance;
	}
}