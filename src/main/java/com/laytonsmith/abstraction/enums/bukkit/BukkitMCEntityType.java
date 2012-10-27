/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.EntityType;

/**
 *
 * @author Layton
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCEntityType.class,
forConcreteEnum = EntityType.class)
public class BukkitMCEntityType extends EnumConvertor<MCEntityType, EntityType> {
	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType();
		}
		return instance;
	}
}
