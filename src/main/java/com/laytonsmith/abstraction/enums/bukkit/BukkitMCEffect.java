
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCEffect;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.Effect;

/**
 *
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCEffect.class,
forConcreteEnum = Effect.class)
public class BukkitMCEffect extends EnumConvertor<MCEffect, Effect> {
	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCEffect instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCEffect getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCEffect();
		}
		return instance;
	}
}
