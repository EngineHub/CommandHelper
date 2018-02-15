package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCSoundCategory;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.SoundCategory;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCSoundCategory.class,
		forConcreteEnum = SoundCategory.class
)
public class BukkitMCSoundCategory extends EnumConvertor<MCSoundCategory, SoundCategory> {
	private static BukkitMCSoundCategory instance;

	public static BukkitMCSoundCategory getConvertor() {
		if(instance == null) {
			instance = new BukkitMCSoundCategory();
		}
		return instance;
	}

	@Override
	protected SoundCategory getConcreteEnumCustom(MCSoundCategory abstracted) {
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_11)) {
			return null;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}