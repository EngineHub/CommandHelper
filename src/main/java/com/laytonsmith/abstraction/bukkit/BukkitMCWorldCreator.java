
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCWorldCreator;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldEnvironment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldType;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

/**
 *
 * 
 */
public class BukkitMCWorldCreator implements MCWorldCreator {
	
	WorldCreator creator;
	public BukkitMCWorldCreator(String name){
		creator = new WorldCreator(name);
	}

	@Override
	public MCWorld createWorld() {
		return new BukkitMCWorld(creator.createWorld());
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
