/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Villager;

/**
 *
 * @author Jim
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCProfession.class,
		forConcreteEnum=Villager.Profession.class
		)
public class BukkitMCProfession extends EnumConvertor<MCProfession, Villager.Profession> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCProfession();
		}
		return instance;
	}
}