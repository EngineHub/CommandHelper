
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.GameMode;

/**
 *
 * 
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCGameMode.class,
forConcreteEnum = GameMode.class)
public class BukkitMCGameMode extends EnumConvertor<MCGameMode, GameMode> {
	private static BukkitMCGameMode instance;

	public static BukkitMCGameMode getConvertor() {
		if (instance == null) {
			instance = new BukkitMCGameMode();
		}
		return instance;
	}
}
