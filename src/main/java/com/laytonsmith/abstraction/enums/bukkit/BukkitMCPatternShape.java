package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.block.banner.PatternType;

@abstractionenum(
	implementation = Implementation.Type.BUKKIT,
	forAbstractEnum = MCPatternShape.class,
	forConcreteEnum = PatternType.class
)
public class BukkitMCPatternShape extends EnumConvertor<MCPatternShape, PatternType> {

	private static BukkitMCPatternShape instance;

	public static BukkitMCPatternShape getConvertor() {
		if (instance == null) {
			instance = new BukkitMCPatternShape();
		}
		return instance;
	}
}
