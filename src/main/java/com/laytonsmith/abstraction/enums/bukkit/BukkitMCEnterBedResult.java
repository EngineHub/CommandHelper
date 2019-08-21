package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCEnterBedResult;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.player.PlayerBedEnterEvent;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCEnterBedResult.class,
		forConcreteEnum = PlayerBedEnterEvent.BedEnterResult.class
)
public class BukkitMCEnterBedResult extends EnumConvertor<MCEnterBedResult, PlayerBedEnterEvent.BedEnterResult> {

	private static BukkitMCEnterBedResult instance;

	public static BukkitMCEnterBedResult getConvertor() {
		if(instance == null) {
			instance = new BukkitMCEnterBedResult();
		}
		return instance;
	}
}
