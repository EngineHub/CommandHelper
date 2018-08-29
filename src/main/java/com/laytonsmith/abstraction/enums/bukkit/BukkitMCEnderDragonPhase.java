package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCEnderDragonPhase;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.EnderDragon;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCEnderDragonPhase.class,
		forConcreteEnum = EnderDragon.Phase.class
)
public class BukkitMCEnderDragonPhase extends EnumConvertor<MCEnderDragonPhase, EnderDragon.Phase> {

	private static BukkitMCEnderDragonPhase instance;

	public static BukkitMCEnderDragonPhase getConvertor() {
		if(instance == null) {
			instance = new BukkitMCEnderDragonPhase();
		}
		return instance;
	}
}
