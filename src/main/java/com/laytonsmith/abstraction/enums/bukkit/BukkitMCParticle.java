package com.laytonsmith.abstraction.enums.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.Particle;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCParticle.class,
		forConcreteEnum = Particle.class
)
public class BukkitMCParticle extends EnumConvertor<MCParticle, Particle> {

	private static BukkitMCParticle instance;

	public static BukkitMCParticle getConvertor() {
		if(instance == null) {
			instance = new BukkitMCParticle();
		}
		return instance;
	}

	@Override
	protected Particle getConcreteEnumCustom(MCParticle abstracted) {
		if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_9)) {
			return null;
		}
		return super.getConcreteEnumCustom(abstracted);
	}
}
