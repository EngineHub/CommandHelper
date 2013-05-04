
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCWorldCreator;
import com.laytonsmith.abstraction.enums.MCWorldEnvironment;
import com.laytonsmith.abstraction.enums.MCWorldType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldEnvironment;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCWorldType;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

/**
 *
 * @author Layton
 */
public class BukkitMCWorldCreator implements MCWorldCreator {
	
	@WrappedItem WorldCreator creator;
	public BukkitMCWorldCreator(String name){
		creator = new WorldCreator(name);
	}

	public MCWorld createWorld() {
		return new BukkitMCWorld(creator.createWorld());
	}

	public MCWorldCreator type(MCWorldType type) {
		WorldType wt = BukkitMCWorldType.getConvertor().getConcreteEnum(type);
		creator.type(wt);
		return this;
	}

	public MCWorldCreator environment(MCWorldEnvironment environment) {
		World.Environment we = BukkitMCWorldEnvironment.getConvertor().getConcreteEnum(environment);
		creator.environment(we);
		return this;
	}

	public MCWorldCreator seed(long seed) {
		creator.seed(seed);
		return this;
	}
	
	public MCWorldCreator generator(String generator) {
		creator.generator(generator);
		return this;
	}
}
