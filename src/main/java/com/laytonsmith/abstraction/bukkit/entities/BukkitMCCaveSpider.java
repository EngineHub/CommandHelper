package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.CaveSpider;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCCaveSpider;

/**
 *
 * @author Hekta
 */
public class BukkitMCCaveSpider extends BukkitMCSpider implements MCCaveSpider {

	public BukkitMCCaveSpider(CaveSpider spider) {
		super(spider);
	}

	public BukkitMCCaveSpider(AbstractionObject ao) {
		this((CaveSpider) ao.getHandle());
	}

	@Override
	public CaveSpider getHandle() {
		return (CaveSpider) metadatable;
	}
}