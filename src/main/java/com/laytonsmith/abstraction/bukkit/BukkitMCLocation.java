package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCVector;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import org.bukkit.Location;

/**
 *
 * @author layton
 */
public class BukkitMCLocation implements MCLocation {

    protected Location location;

    public BukkitMCLocation(Location l) {
        this.location = l;
    }

    public BukkitMCLocation(AbstractionObject ao) {
        this((Location) ao.getHandle());
    }

	@Override
    public Location getHandle() {
        return location;
    }

    public double getX() {
        return location.getX();
    }

    public double getY() {
        return location.getY();
    }

    public double getZ() {
        return location.getZ();
    }

	public double distance(MCLocation o) {
		return location.distance((Location) o.getHandle());
	}
	
	public double distanceSquared(MCLocation o) {
		return location.distanceSquared((Location) o.getHandle());
	}

    public MCWorld getWorld() {
        if (location.getWorld() == null) {
            return null;
        }
        return new BukkitMCWorld(location.getWorld());
    }

    public float getYaw() {
        return location.getYaw();
    }

    public float getPitch() {
        return location.getPitch();
    }

    public int getBlockX() {
        return location.getBlockX();
    }

    public int getBlockY() {
        return location.getBlockY();
    }

    public int getBlockZ() {
        return location.getBlockZ();
    }

    public MCBlock getBlock() {
        if (location == null || location.getBlock() == null) {
            return null;
        }
        return new BukkitMCBlock(location.getBlock());
    }

	public void setX(double x) {
		location.setX(x);
	}

	public void setY(double y) {
		location.setY(y);
	}

	public void setZ(double z) {
		location.setZ(z);
	}

    public void setPitch(float p) {
        location.setPitch(p);
    }

    public void setYaw(float y) {
        location.setYaw(y);
    }

    @Override
    public MCLocation clone() {
        return new BukkitMCLocation(location.clone());
    }

	@Override
	public String toString() {
		return location.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCLocation?location.equals(((BukkitMCLocation)obj).location):false);
	}

	@Override
	public int hashCode() {
		return location.hashCode();
	}

	public void breakBlock() {
		location.getBlock().breakNaturally();
	}

	public MCVector getDirection() {
		return new BukkitMCVector(location.getDirection());
	}

	public MCChunk getChunk() {
		return new BukkitMCChunk(location.getChunk());
	}
}