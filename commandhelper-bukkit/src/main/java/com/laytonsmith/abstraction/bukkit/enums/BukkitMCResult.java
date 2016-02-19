
package com.laytonsmith.abstraction.bukkit.enums;

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

	private static BukkitMCResult instance;

	public static BukkitMCResult getConvertor() {
		if (instance == null) {
			instance = new BukkitMCResult();
		}
		return instance;
	}
}
