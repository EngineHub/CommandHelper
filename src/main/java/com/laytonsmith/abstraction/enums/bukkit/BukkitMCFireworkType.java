
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.FireworkEffect;

/**
 *
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCFireworkType.class,
forConcreteEnum = FireworkEffect.Type.class)
public class BukkitMCFireworkType extends EnumConvertor<MCFireworkType, FireworkEffect.Type> {
	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCFireworkType instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCFireworkType getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCFireworkType();
		}
		return instance;
	}
}
