
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCRabbitType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.entity.Rabbit;

@abstractionenum(
	implementation=Implementation.Type.BUKKIT,
	forAbstractEnum=MCRabbitType.class,
	forConcreteEnum=Rabbit.Type.class
)
public class BukkitMCRabbitType extends EnumConvertor<MCRabbitType, Rabbit.Type> {

	private static BukkitMCRabbitType instance;

	public static BukkitMCRabbitType getConvertor() {
		if (instance == null) {
			instance = new BukkitMCRabbitType();
		}
		return instance;
	}

	@Override
	protected Rabbit.Type getConcreteEnumCustom(MCRabbitType abstracted) {
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_8)) {
			return null;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}