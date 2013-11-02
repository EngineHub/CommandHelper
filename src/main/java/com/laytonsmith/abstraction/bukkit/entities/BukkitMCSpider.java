package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Spider;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSpider;

/**
 *
 * @author Hekta
 */
public class BukkitMCSpider extends BukkitMCCreature implements MCSpider {

	public BukkitMCSpider(Spider spider) {
		super(spider);
	}

	public BukkitMCSpider(AbstractionObject ao) {
		this((Spider) ao.getHandle());
	}

	@Override
	public Spider getHandle() {
		return (Spider) metadatable;
	}
}