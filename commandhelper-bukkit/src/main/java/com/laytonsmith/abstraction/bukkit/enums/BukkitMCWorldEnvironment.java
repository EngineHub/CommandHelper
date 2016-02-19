
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.World;

/**
 *
 * 
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCWorldEnvironment.class,
forConcreteEnum = World.Environment.class)
public class BukkitMCWorldEnvironment extends EnumConvertor<MCWorldEnvironment, World.Environment>{
	private static BukkitMCWorldEnvironment instance;

	public static BukkitMCWorldEnvironment getConvertor() {
		if (instance == null) {
			instance = new BukkitMCWorldEnvironment();
		}
		return instance;
	}
}
