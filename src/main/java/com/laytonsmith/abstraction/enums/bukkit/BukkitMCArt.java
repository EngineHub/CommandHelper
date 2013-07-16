package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCArt;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.Art;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCArt.class,
forConcreteEnum = Art.class)
public class BukkitMCArt extends EnumConvertor<MCArt, Art> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCArt instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCArt getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCArt();
		}
		return instance;
	}
	
	
}
