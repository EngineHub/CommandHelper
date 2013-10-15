package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCTreeType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.TreeType;

/**
 *
 * @author Hekta
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCTreeType.class,
		forConcreteEnum=TreeType.class
		)
public class BukkitMCTreeType extends EnumConvertor<MCTreeType, TreeType> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType();
		}
		return instance;
	}
}