
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
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

	private static BukkitMCBlockFace instance;

	public static BukkitMCBlockFace getConvertor() {
		if (instance == null) {
			instance = new BukkitMCBlockFace();
		}
		return instance;
	}
	
	
}
