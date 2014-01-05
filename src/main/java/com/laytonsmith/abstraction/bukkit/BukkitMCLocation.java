

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.MCEntity.Velocity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 *
 * @author layton
 */
public class BukkitMCLocation implements MCLocation {
    Location l;
    public BukkitMCLocation(Location l) {
        this.l = l;
    }

    public BukkitMCLocation(AbstractionObject a) {
        if (a instanceof MCLocation) {
            this.l = ((Location)a.getHandle());
        } else {
            throw new ClassCastException();
        }
    }

    public Object getHandle() {
        return l;
    }

    public double getX() {
        return l.getX();
    }

    public double getY() {
        return l.getY();
    }

    public double getZ() {
        return l.getZ();
    }

	public double distance(MCLocation o) {
		return l.distance(((BukkitMCLocation)o)._Location());
	}
	
	public double distanceSquared(MCLocation o) {
		return l.distanceSquared(((BukkitMCLocation)o)._Location());
	}

    public MCWorld getWorld() {
        if (l.getWorld() == null) {
            return null;
        }
        return new BukkitMCWorld(l.getWorld());
    }

    public float getYaw() {
        return l.getYaw();
    }

    public float getPitch() {
        return l.getPitch();
    }

    public int getBlockX() {
        return l.getBlockX();
    }

    public int getBlockY() {
        return l.getBlockY();
    }

    public int getBlockZ() {
        return l.getBlockZ();
    }

    public MCBlock getBlock() {
        if (l == null || l.getBlock() == null) {
            return null;
        }
        return new BukkitMCBlock(l.getBlock());
    }

    public Location _Location() {
        return l;
    }

	public void setX(double x) {
		l.setX(x);
	}

	public void setY(double y) {
		l.setY(y);
	}

	public void setZ(double z) {
		l.setZ(z);
	}

    public void setPitch(float p) {
        l.setPitch(p);
    }

    public void setYaw(float y) {
        l.setYaw(y);
    }

	@Override
	public MCLocation add(MCLocation vec) {
		return new BukkitMCLocation(l.add(((BukkitMCLocation) vec)._Location()));
	}

	@Override
	public MCLocation add(Velocity vec) {
		return new BukkitMCLocation(l.add(new Vector(vec.x, vec.y, vec.z)));
	}

	@Override
	public MCLocation add(double x, double y, double z) {
		return new BukkitMCLocation(l.add(x, y, z));
	}

	@Override
	public MCLocation multiply(double m) {
		return new BukkitMCLocation(l.multiply(m));
	}

	@Override
	public Velocity toVector() {
		return new Velocity(l.getX(), l.getY(), l.getZ());
	}

	@Override
	public MCLocation subtract(MCLocation vec) {
		return new BukkitMCLocation(l.subtract(((BukkitMCLocation) vec)._Location()));
	}

	@Override
	public MCLocation subract(Velocity vec) {
		return new BukkitMCLocation(l.subtract(new Vector(vec.x, vec.y, vec.z)));
	}

	@Override
	public MCLocation subract(double x, double y, double z) {
		return new BukkitMCLocation(l.subtract(x, y, z));
	}

    @Override
    public MCLocation clone() {
        return new BukkitMCLocation(l.clone());
    }

    public Location asLocation() {
        return l;
    }

	@Override
	public String toString() {
		return l.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BukkitMCLocation?l.equals(((BukkitMCLocation)obj).l):false);
	}

	@Override
	public int hashCode() {
		return l.hashCode();
	}

	public void breakBlock() {
		l.getBlock().breakNaturally();
	}

	public Velocity getDirection() {
		return new Velocity(1, l.getDirection().getX(), l.getDirection().getY(), l.getDirection().getZ());
	}

	public MCChunk getChunk() {
		return new BukkitMCChunk(l.getChunk());
	}

}
