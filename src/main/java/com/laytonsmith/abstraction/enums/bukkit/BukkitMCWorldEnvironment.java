package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.World;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCWorldEnvironment.class,
		forConcreteEnum = World.Environment.class
)
public class BukkitMCWorldEnvironment extends EnumConvertor<MCWorldEnvironment, World.Environment>{
	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldEnvironment instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldEnvironment getConvertor() {
		if(instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldEnvironment();
		}
		return instance;
	}
}
