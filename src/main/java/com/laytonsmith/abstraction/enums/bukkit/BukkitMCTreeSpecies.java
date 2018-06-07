package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCTreeSpecies;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.TreeSpecies;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCTreeSpecies.class,
		forConcreteEnum = TreeSpecies.class
)
public class BukkitMCTreeSpecies extends EnumConvertor<MCTreeSpecies, TreeSpecies> {

	private static BukkitMCTreeSpecies instance;

	public static BukkitMCTreeSpecies getConvertor() {
		if(instance == null) {
			instance = new BukkitMCTreeSpecies();
		}
		return instance;
	}

	@Override
	protected MCTreeSpecies getAbstractedEnumCustom(TreeSpecies concrete) {
		switch(concrete) {
			case GENERIC:
				return MCTreeSpecies.OAK;
			case REDWOOD:
				return MCTreeSpecies.SPRUCE;
		}
		return super.getAbstractedEnumCustom(concrete);
	}

	@Override
	protected TreeSpecies getConcreteEnumCustom(MCTreeSpecies abstracted) {
		switch(abstracted) {
			case OAK:
				return TreeSpecies.GENERIC;
			case SPRUCE:
				return TreeSpecies.REDWOOD;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}
