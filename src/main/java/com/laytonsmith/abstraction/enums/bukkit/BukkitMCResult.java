
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCResult;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.event.Event.Result;

/**
 *
 * @author MariuszT
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCResult.class,
forConcreteEnum = Result.class)
public class BukkitMCResult extends EnumConvertor<MCResult, Result> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCResult instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCResult getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCResult();
		}
		return instance;
	}
}
