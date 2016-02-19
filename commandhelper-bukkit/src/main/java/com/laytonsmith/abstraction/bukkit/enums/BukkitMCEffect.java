
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCEffect;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.Effect;

/**
 *
 * 
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCEffect.class,
forConcreteEnum = Effect.class)
public class BukkitMCEffect extends EnumConvertor<MCEffect, Effect> {
	private static BukkitMCEffect instance;

	public static BukkitMCEffect getConvertor() {
		if (instance == null) {
			instance = new BukkitMCEffect();
		}
		return instance;
	}
}
