package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCTreeType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.TreeType;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCTreeType.class,
		forConcreteEnum = TreeType.class
)
public class BukkitMCTreeType extends EnumConvertor<MCTreeType, TreeType> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType getConvertor() {
		if(instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType();
		}
		return instance;
	}

	@Override
	protected MCTreeType getAbstractedEnumCustom(TreeType concrete) {
		try {
			return super.getAbstractedEnumCustom(concrete);
		} catch(IllegalArgumentException ex) {
			// Poplar variants were briefly merged on Paper 26.3
			if(concrete.name().equals("POPLAR")) {
				return MCTreeType.ORANGE_POPLAR;
			}
			throw ex;
		}
	}

	@Override
	protected TreeType getConcreteEnumCustom(MCTreeType abstracted) {
		try {
			return super.getConcreteEnumCustom(abstracted);
		} catch(IllegalArgumentException ex) {
			// Poplar variants were briefly merged on Paper 26.3
			if(abstracted == MCTreeType.ORANGE_POPLAR
					|| abstracted == MCTreeType.RED_POPLAR
					|| abstracted == MCTreeType.YELLOW_POPLAR) {
				return TreeType.valueOf("POPLAR");
			}
			throw ex;
		}
	}
}
