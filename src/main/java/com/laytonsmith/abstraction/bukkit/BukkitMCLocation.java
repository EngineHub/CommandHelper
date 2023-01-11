package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCChunk;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class BukkitMCLocation implements MCLocation {

	Location l;

	public BukkitMCLocation(Location l) {
		this.l = l;
	}

	public BukkitMCLocation(AbstractionObject a) {
		if(a instanceof MCLocation) {
			this.l = ((Location) a.getHandle());
		} else {
			throw new ClassCastException();
		}
	}

	@Override
	public Object getHandle() {
		return l;
	}

	@Override
	public double getX() {
		return l.getX();
	}

	@Override
	public double getY() {
		return l.getY();
	}

	@Override
	public double getZ() {
		return l.getZ();
	}

	@Override
	public double distance(MCLocation o) {
		return l.distance(((BukkitMCLocation) o)._Location());
	}

	@Override
	public double distanceSquared(MCLocation o) {
		return l.distanceSquared(((BukkitMCLocation) o)._Location());
	}

	@Override
	public MCWorld getWorld() {
		if(l.getWorld() == null) {
			return null;
		}
		return new BukkitMCWorld(l.getWorld());
	}

	@Override
	public float getYaw() {
		return l.getYaw();
	}

	@Override
	public float getPitch() {
		return l.getPitch();
	}

	@Override
	public int getBlockX() {
		return l.getBlockX();
	}

	@Override
	public int getBlockY() {
		return l.getBlockY();
	}

	@Override
	public int getBlockZ() {
		return l.getBlockZ();
	}

	@Override
	public MCBlock getBlock() {
		return new BukkitMCBlock(l.getBlock());
	}

	public Location _Location() {
		return l;
	}

	@Override
	public void setX(double x) {
		l.setX(x);
	}

	@Override
	public void setY(double y) {
		l.setY(y);
	}

	@Override
	public void setZ(double z) {
		l.setZ(z);
	}

	@Override
	public void setPitch(float p) {
		l.setPitch(p);
	}

	@Override
	public void setYaw(float y) {
		l.setYaw(y);
	}

	@Override
	public MCLocation add(MCLocation vec) {
		return new BukkitMCLocation(l.add(((BukkitMCLocation) vec)._Location()));
	}

	@Override
	public MCLocation add(Vector3D vec) {
		return new BukkitMCLocation(l.add(new Vector(vec.X(), vec.Y(), vec.Z())));
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
	public Vector3D toVector() {
		return new Vector3D(l.getX(), l.getY(), l.getZ());
	}

	@Override
	public MCLocation subtract(MCLocation vec) {
		return new BukkitMCLocation(l.subtract(((BukkitMCLocation) vec)._Location()));
	}

	@Override
	public MCLocation subtract(Vector3D vec) {
		return new BukkitMCLocation(l.subtract(new Vector(vec.X(), vec.Y(), vec.Z())));
	}

	@Override
	public MCLocation subtract(double x, double y, double z) {
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
		return (obj instanceof BukkitMCLocation && l.equals(((BukkitMCLocation) obj).l));
	}

	@Override
	public int hashCode() {
		return l.hashCode();
	}

	@Override
	public Vector3D getDirection() {
		Vector v = l.getDirection();
		return new Vector3D(v.getX(), v.getY(), v.getZ());
	}

	@Override
	public MCChunk getChunk() {
		return new BukkitMCChunk(l.getChunk());
	}

}
