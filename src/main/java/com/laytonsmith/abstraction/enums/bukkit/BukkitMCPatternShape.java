package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.banner.PatternType;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCPatternShape.class,
		forConcreteEnum = PatternType.class
)
public class BukkitMCPatternShape extends EnumConvertor<MCPatternShape, PatternType> {

	private static BukkitMCPatternShape instance;

	public static BukkitMCPatternShape getConvertor() {
		if(instance == null) {
			instance = new BukkitMCPatternShape();
		}
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
			// Spigot remaps PatternType.DIAGONAL_RIGHT_MIRROR to DIAGONAL_RIGHT, but that value is also remapped.
			// Storing a reference to the pattern allows us to convert back and forth.
			instance.diagonalRight = Registry.BANNER_PATTERN.get(NamespacedKey.minecraft("diagonal_right"));
		}
		return instance;
	}

	private PatternType diagonalRight;

	@Override
	protected MCPatternShape getAbstractedEnumCustom(PatternType concrete) {
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
			if(concrete == diagonalRight) {
				return MCPatternShape.DIAGONAL_RIGHT_MIRROR;
			}
			switch(concrete) {
				case DIAGONAL_UP_RIGHT:
					return MCPatternShape.DIAGONAL_RIGHT;
				case SMALL_STRIPES:
					return MCPatternShape.STRIPE_SMALL;
				case DIAGONAL_UP_LEFT:
					return MCPatternShape.DIAGONAL_LEFT_MIRROR;
				case CIRCLE:
					return MCPatternShape.CIRCLE_MIDDLE;
				case RHOMBUS:
					return MCPatternShape.RHOMBUS_MIDDLE;
				case HALF_VERTICAL_RIGHT:
					return MCPatternShape.HALF_VERTICAL_MIRROR;
				case HALF_HORIZONTAL_BOTTOM:
					return MCPatternShape.HALF_HORIZONTAL_MIRROR;
			}
		}
		return super.getAbstractedEnumCustom(concrete);
	}

	@Override
	protected PatternType getConcreteEnumCustom(MCPatternShape abstracted) {
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
			if(abstracted == MCPatternShape.DIAGONAL_RIGHT_MIRROR) {
				return instance.diagonalRight;
			}
		}
		return PatternType.valueOf(abstracted.name());
	}
}
