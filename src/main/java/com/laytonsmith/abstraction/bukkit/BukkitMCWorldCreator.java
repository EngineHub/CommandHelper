package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCWorldCreator;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldEnvironment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldType;
import com.laytonsmith.core.Static;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.io.File;
import java.util.logging.Level;

public class BukkitMCWorldCreator implements MCWorldCreator {

	WorldCreator creator;

	public BukkitMCWorldCreator(String name) {
		creator = new WorldCreator(name);
	}

	@Override
	public MCWorld createWorld() {
		World w = creator.createWorld();
		if(w != null && w.getEnvironment() == Environment.NORMAL) {
			File nether = new File(w.getWorldFolder(), "DIM-1");
			if(nether.exists()) {
				Static.getLogger().log(Level.WARNING, "Loaded " + w.getName() + " world with overworld (NORMAL)"
						+ " environment but found DIM-1 (NETHER) directory exists.");
			}
			File end = new File(w.getWorldFolder(), "DIM1");
			if(end.exists()) {
				Static.getLogger().log(Level.WARNING, "Loaded " + w.getName() + " world with overworld (NORMAL)"
						+ " environment but found DIM1 (THE_END) directory exists.");
			}
		}
		return new BukkitMCWorld(w);
	}

	@Override
	public MCWorldCreator type(MCWorldType type) {
		WorldType wt = BukkitMCWorldType.getConvertor().getConcreteEnum(type);
		creator.type(wt);
		return this;
	}

	@Override
	public MCWorldCreator environment(MCWorldEnvironment environment) {
		World.Environment we = BukkitMCWorldEnvironment.getConvertor().getConcreteEnum(environment);
		creator.environment(we);
		return this;
	}

	@Override
	public MCWorldCreator seed(long seed) {
		creator.seed(seed);
		return this;
	}

	@Override
	public MCWorldCreator generator(String generator) {
		creator.generator(generator);
		return this;
	}

	@Override
	public MCWorldCreator copy(MCWorld toCopy) {
		creator.copy((World) toCopy.getHandle());
		return this;
	}
}
