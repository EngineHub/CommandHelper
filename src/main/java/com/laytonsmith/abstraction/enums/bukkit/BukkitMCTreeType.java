package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCTreeType;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.TreeType;

import java.util.Random;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCTreeType.class,
		forConcreteEnum = TreeType.class
)
public class BukkitMCTreeType extends EnumConvertor<MCTreeType, TreeType> {

	private static com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType instance;

	public static com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType getConvertor() {
		if(instance == null) {
			instance = new com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType();
		}
		return instance;
	}

	@Override
	protected MCTreeType getAbstractedEnumCustom(TreeType concrete) {
		if(!((BukkitMCServer) Static.getServer()).isPaper()
				&& Static.getServer().getMinecraftVersion().gte(MCVersion.MC26_3)) {
			switch(concrete.name()) {
				case "RED_POPLAR":
				case "ORANGE_POPLAR":
				case "YELLOW_POPLAR":
					return MCTreeType.POPLAR;
			}
		}
		return super.getAbstractedEnumCustom(concrete);
	}

	@Override
	protected TreeType getConcreteEnumCustom(MCTreeType abstracted) {
		if(!((BukkitMCServer) Static.getServer()).isPaper()
				&& Static.getServer().getMinecraftVersion().gte(MCVersion.MC26_3)) {
			if(abstracted == MCTreeType.POPLAR) {
				String[] colors = new String[]{"RED", "ORANGE", "YELLOW"};
				return TreeType.valueOf(colors[Math.abs(new Random().nextInt()) % 3] + "_POPLAR");
			}
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}
