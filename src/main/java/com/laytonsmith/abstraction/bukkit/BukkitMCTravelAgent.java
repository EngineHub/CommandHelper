package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCTravelAgent;
import org.bukkit.TravelAgent;

/**
 *
 * @author MariuszT
 */
public class BukkitMCTravelAgent implements MCTravelAgent {

    TravelAgent a;
    public BukkitMCTravelAgent(TravelAgent a) {
        this.a = a;
    }
	
	public BukkitMCTravelAgent(AbstractionObject o) {
		a = (TravelAgent)o;
	}

	@Override
	public String toString() {
		return a.toString();
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		return a.equals(obj);
	}

	@Override
	public int hashCode() {
		return a.hashCode();
	}

	public boolean createPortal(MCLocation location) {
		return a.createPortal(((BukkitMCLocation)location).asLocation());
	}

	public MCLocation findOrCreate(MCLocation location) {
		return new BukkitMCLocation(a.findOrCreate(((BukkitMCLocation)location).asLocation()));
	}

	public MCLocation findPortal(MCLocation location) {
		return new BukkitMCLocation(a.findPortal(((BukkitMCLocation)location).asLocation()));
	}

	public boolean getCanCreatePortal() {
		return a.getCanCreatePortal();
	}

	public void setCanCreatePortal(boolean create) {
		a.setCanCreatePortal(create);
	}

	public int getCreationRadius() {
		return a.getCreationRadius();
	}

	public MCTravelAgent setCreationRadius(int radius) {
		return new BukkitMCTravelAgent(a.setCreationRadius(radius));
	}

	public int getSearchRadius() {
		return a.getSearchRadius();
	}

	public MCTravelAgent setSearchRadius(int radius) {
		return new BukkitMCTravelAgent(a.setSearchRadius(radius));
	}

	public Object getHandle() {
		return a;
	}
}
