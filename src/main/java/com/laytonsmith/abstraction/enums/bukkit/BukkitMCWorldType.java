
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCWorldType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.WorldType;

/**
 *
 * 
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCWorldType.class,
forConcreteEnum = WorldType.class)
public class BukkitMCWorldType extends EnumConvertor<MCWorldType, WorldType>{
	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldType instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldType getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldType();
		}
		return instance;
	}
}
