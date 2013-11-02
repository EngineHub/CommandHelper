
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.MCBlockFace;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.block.BlockFace;

/**
 * 
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCBlockFace.class,
forConcreteEnum = BlockFace.class)
public class BukkitMCBlockFace extends EnumConvertor<MCBlockFace, BlockFace> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCBlockFace instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCBlockFace getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCBlockFace();
		}
		return instance;
	}
	
	
}
