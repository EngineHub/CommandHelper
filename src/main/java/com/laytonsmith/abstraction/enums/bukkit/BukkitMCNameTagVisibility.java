package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCNameTagVisibility;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.scoreboard.NameTagVisibility;

@abstractionenum(
		implementation= Implementation.Type.BUKKIT,
		forAbstractEnum=MCNameTagVisibility.class,
		forConcreteEnum=NameTagVisibility.class
)
public class BukkitMCNameTagVisibility extends EnumConvertor<MCNameTagVisibility, NameTagVisibility>{

	private static BukkitMCNameTagVisibility instance;
	
	public static BukkitMCNameTagVisibility getConvertor() {
		if (instance == null) {
			instance = new BukkitMCNameTagVisibility();
		}
		return instance;
	}

	@Override
	protected NameTagVisibility getConcreteEnumCustom(MCNameTagVisibility abstracted) {
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_8)) {
			return null;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}
