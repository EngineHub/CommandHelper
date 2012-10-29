/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCGameMode;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.GameMode;

/**
 *
 * @author Layton
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCGameMode.class,
forConcreteEnum = GameMode.class)
public class BukkitMCGameMode extends EnumConvertor<MCGameMode, GameMode> {
	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCGameMode instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCGameMode getConvertor() {
		if (instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCGameMode();
		}
		return instance;
	}
}
