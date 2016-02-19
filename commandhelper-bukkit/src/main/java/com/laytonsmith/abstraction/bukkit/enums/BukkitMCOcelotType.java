
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCOcelotType;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Ocelot;

/**
 *
 * @author jb_aero
 */
@abstractionenum(
		implementation=Implementation.Type.BUKKIT,
		forAbstractEnum=MCOcelotType.class,
		forConcreteEnum=Ocelot.Type.class
		)
public class BukkitMCOcelotType extends EnumConvertor<MCOcelotType, Ocelot.Type> {

	private static BukkitMCOcelotType instance;

	public static BukkitMCOcelotType getConvertor() {
		if (instance == null) {
			instance = new BukkitMCOcelotType();
		}
		return instance;
	}
}