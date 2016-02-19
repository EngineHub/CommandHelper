
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCProfession;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Villager;

/**
 *
 * @author jb_aero
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCProfession.class,
		forConcreteEnum=Villager.Profession.class
		)
public class BukkitMCProfession extends EnumConvertor<MCProfession, Villager.Profession> {

	private static BukkitMCProfession instance;

	public static BukkitMCProfession getConvertor() {
		if (instance == null) {
			instance = new BukkitMCProfession();
		}
		return instance;
	}
}