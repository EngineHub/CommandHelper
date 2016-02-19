
package com.laytonsmith.abstraction.bukkit.enums;

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
	private static BukkitMCWorldType instance;

	public static BukkitMCWorldType getConvertor() {
		if (instance == null) {
			instance = new BukkitMCWorldType();
		}
		return instance;
	}
}
