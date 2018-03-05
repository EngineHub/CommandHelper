package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCAction;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.block.Action;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCAction.class,
		forConcreteEnum = Action.class
)
public class BukkitMCAction extends EnumConvertor<MCAction, Action> {

	private static BukkitMCAction instance;

	public static BukkitMCAction getConvertor() {
		if (instance == null) {
			instance = new BukkitMCAction();
		}
		return instance;
	}
}
