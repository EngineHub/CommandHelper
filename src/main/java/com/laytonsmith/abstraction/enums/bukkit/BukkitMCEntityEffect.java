/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.EntityEffect;

/**
 *
 * @author Layton
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCEntityEffect.class,
forConcreteEnum = EntityEffect.class)
public class BukkitMCEntityEffect extends EnumConvertor<MCEntityEffect, EntityEffect> {
	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityEffect instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityEffect getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityEffect();
		}
		return instance;
	}
}
