package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCParrotType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.entity.Parrot;

@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCParrotType.class,
		forConcreteEnum=Parrot.Variant.class
)
public class BukkitMCParrotType extends EnumConvertor<MCParrotType, Parrot.Variant> {

	private static BukkitMCParrotType instance;

	public static BukkitMCParrotType getConvertor() {
		if(instance == null) {
			instance = new BukkitMCParrotType();
		}
		return instance;
	}

	@Override
	protected Parrot.Variant getConcreteEnumCustom(MCParrotType abstracted) {
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_12)) {
			return null;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}