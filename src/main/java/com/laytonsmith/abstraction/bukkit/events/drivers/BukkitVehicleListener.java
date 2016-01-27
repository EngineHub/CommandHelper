

package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.events.BukkitVehicleEvents.*;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.events.drivers.VehicleEvents;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;

/**
 *
 * 
 */
public class BukkitVehicleListener implements Listener{
    
	@EventHandler(priority= EventPriority.LOWEST)
	public void onEnter(VehicleEnterEvent event) {
		BukkitMCVehicleEnterEvent vee = new BukkitMCVehicleEnterEvent(event);
		EventUtils.TriggerListener(Driver.VEHICLE_ENTER, "vehicle_enter", vee);
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onExit(VehicleExitEvent event) {
		BukkitMCVehicleExitEvent vee = new BukkitMCVehicleExitEvent(event);
		EventUtils.TriggerListener(Driver.VEHICLE_LEAVE, "vehicle_leave", vee);
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onBlockCollide(VehicleBlockCollisionEvent event) {
		if (event.getVehicle() instanceof Animals && event.getVehicle().getPassenger() == null) {
			return;
		}
		BukkitMCVehicleBlockCollideEvent vbc = new BukkitMCVehicleBlockCollideEvent(event);
		EventUtils.TriggerListener(Driver.VEHICLE_COLLIDE, "vehicle_collide", vbc);
	}

	@EventHandler(priority= EventPriority.LOWEST)
	public void onEntityCollide(VehicleEntityCollisionEvent event) {
		if (event.getVehicle().getPassenger() != event.getEntity()) {
			BukkitMCVehicleEntityCollideEvent vec = new BukkitMCVehicleEntityCollideEvent(event);
			EventUtils.TriggerListener(Driver.VEHICLE_COLLIDE, "vehicle_collide", vec);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onVehicleMove(VehicleMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		UUID id = event.getVehicle().getUniqueId();
		for(Integer threshold : VehicleEvents.GetThresholdList()) {
			Map<UUID, MCLocation> lastLocations = VehicleEvents.GetLastLocations(threshold);
			if(!lastLocations.containsKey(id)) {
				lastLocations.put(id, new BukkitMCLocation(from));
				continue;
			}
			MCLocation last = lastLocations.get(id);
			if (!to.getWorld().getName().equals(last.getWorld().getName())) {
				lastLocations.put(id, new BukkitMCLocation(to));
				continue;
			}
			BukkitMCLocation movedTo = new BukkitMCLocation(to);
			if (last.distance(movedTo) > threshold) {
				BukkitMCVehicleMoveEvent vme = new BukkitMCVehicleMoveEvent(event, threshold, last);
				EventUtils.TriggerListener(Driver.VEHICLE_MOVE, "vehicle_move", vme);
				if (!vme.isCancelled()) {
					lastLocations.put(id, movedTo);
				} else {
					event.getVehicle().setVelocity(new Vector(0, 0, 0));
					event.getVehicle().teleport(from);
				}
			}
		}
	}
}
