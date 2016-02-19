
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.EntityEffect;

/**
 *
 * 
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCEntityEffect.class,
forConcreteEnum = EntityEffect.class)
public class BukkitMCEntityEffect extends EnumConvertor<MCEntityEffect, EntityEffect> {
	private static BukkitMCEntityEffect instance;

	public static BukkitMCEntityEffect getConvertor() {
		if (instance == null) {
			instance = new BukkitMCEntityEffect();
		}
		return instance;
	}
}
