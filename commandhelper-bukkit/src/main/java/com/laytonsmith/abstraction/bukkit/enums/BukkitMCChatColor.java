
package com.laytonsmith.abstraction.bukkit.enums;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.ChatColor;

/**
 *
 * 
 */
@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
forAbstractEnum = MCChatColor.class,
forConcreteEnum = ChatColor.class)
public class BukkitMCChatColor extends EnumConvertor<MCChatColor, ChatColor> {
	private static BukkitMCChatColor instance;

	public static BukkitMCChatColor getConvertor() {
		if (instance == null) {
			instance = new BukkitMCChatColor();
		}
		return instance;
	}

	@Override
	protected MCChatColor getAbstractedEnumCustom(ChatColor concrete) {
		switch(concrete){
			case MAGIC:
				return MCChatColor.RANDOM;
			case RESET:
				return MCChatColor.PLAIN_WHITE;
		}
		return super.getAbstractedEnumCustom(concrete);
	}

	@Override
	protected ChatColor getConcreteEnumCustom(MCChatColor abstracted) {
		switch(abstracted){
			case RANDOM:
				return ChatColor.MAGIC;
			case PLAIN_WHITE:
				return ChatColor.RESET;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
	
	
	
	
}
