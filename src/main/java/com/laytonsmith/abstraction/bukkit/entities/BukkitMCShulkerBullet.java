package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.entities.MCShulkerBullet;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ShulkerBullet;

public class BukkitMCShulkerBullet extends BukkitMCProjectile implements MCShulkerBullet {

	private ShulkerBullet sb;

	public BukkitMCShulkerBullet(Entity be) {
		super(be);
		this.sb = (ShulkerBullet) be;
	}

	@Override
	public void setTarget(MCEntity entity) {
		if(entity == null) {
			sb.setTarget(null);
		} else {
			sb.setTarget((Entity) entity.getHandle());
		}
	}

	@Override
	public MCEntity getTarget() {
		Entity e = sb.getTarget();
		if(e == null) {
			return null;
		}
		return new BukkitMCEntity(sb.getTarget());
	}
}
