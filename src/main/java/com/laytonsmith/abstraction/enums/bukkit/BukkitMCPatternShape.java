package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
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

	@Override
	protected PatternType getConcreteEnumCustom(MCPatternShape abstracted) {
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_8)) {
			return null;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}
