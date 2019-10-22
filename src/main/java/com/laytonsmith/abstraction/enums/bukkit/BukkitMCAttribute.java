package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.attribute.Attribute;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCAttribute.class,
		forConcreteEnum = Attribute.class
)
public class BukkitMCAttribute extends EnumConvertor<MCAttribute, Attribute> {

	private static BukkitMCAttribute instance;

	public static BukkitMCAttribute getConvertor() {
		if(instance == null) {
			instance = new BukkitMCAttribute();
		}
		return instance;
	}
}
