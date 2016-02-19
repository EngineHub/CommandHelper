
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCRotation;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.Rotation;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCRotation.class,
forConcreteEnum = Rotation.class)
public class BukkitMCRotation extends EnumConvertor<MCRotation, Rotation>{
	private static BukkitMCRotation instance;

	public static BukkitMCRotation getConvertor() {
		if (instance == null) {
			instance = new BukkitMCRotation();
		}
		return instance;
	}
}
