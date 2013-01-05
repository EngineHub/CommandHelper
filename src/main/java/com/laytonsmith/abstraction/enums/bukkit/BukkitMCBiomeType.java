
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCBiomeType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.block.Biome;

/**
 *
 * @author Layton
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCBiomeType.class,
forConcreteEnum = Biome.class)
public class BukkitMCBiomeType extends EnumConvertor<MCBiomeType, Biome> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCBiomeType instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCBiomeType getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCBiomeType();
		}
		return instance;
	}
	
	
}
