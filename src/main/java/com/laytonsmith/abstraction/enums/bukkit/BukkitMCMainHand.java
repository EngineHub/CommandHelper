package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCMainHand;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.inventory.MainHand;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCMainHand.class,
		forConcreteEnum = MainHand.class
)
public class BukkitMCMainHand extends EnumConvertor<MCMainHand, MainHand> {

	private static BukkitMCMainHand instance;

	public static BukkitMCMainHand getConvertor() {
		if(instance == null) {
			instance = new BukkitMCMainHand();
		}
		return instance;
	}
}
