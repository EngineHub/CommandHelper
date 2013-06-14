

package com.laytonsmith.abstraction.bukkit.events.drivers;

import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import com.laytonsmith.abstraction.bukkit.events.BukkitVehicleEvents.BukkitMCVehicleBlockCollideEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitVehicleEvents.BukkitMCVehicleEnterEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitVehicleEvents.BukkitMCVehicleEntityCollideEvent;
import com.laytonsmith.abstraction.bukkit.events.BukkitVehicleEvents.BukkitMCVehicleExitEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;

/**
 *
 */
public class BukkitVehicleListener implements Listener{
    
	@EventHandler(priority= EventPriority.LOWEST)
	public void onEnter(VehicleEnterEvent event) {
		BukkitMCVehicleEnterEvent vee = new BukkitMCVehicleEnterEvent(event);
		EventUtils.TriggerExternal(vee);
		EventUtils.TriggerListener(Driver.VEHICLE_ENTER, "vehicle_enter", vee);
	}
	
	//@EventHandler(priority= EventPriority.LOWEST)
	public void onExit(VehicleExitEvent event) {
		BukkitMCVehicleExitEvent vee = new BukkitMCVehicleExitEvent(event);
		EventUtils.TriggerExternal(vee);
		EventUtils.TriggerListener(Driver.VEHICLE_LEAVE, "vehicle_leave", vee);
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onBlockCollide(VehicleBlockCollisionEvent event) {
		if (event.getVehicle() instanceof Pig && !((Pig) event.getVehicle()).hasSaddle()) {
			return;
		}
		BukkitMCVehicleBlockCollideEvent vbc = new BukkitMCVehicleBlockCollideEvent(event);
		EventUtils.TriggerExternal(vbc);
		EventUtils.TriggerListener(Driver.VEHICLE_COLLIDE, "vehicle_collide", vbc);
	}

	@EventHandler(priority= EventPriority.LOWEST)
	public void onEntityCollide(VehicleEntityCollisionEvent event) {
		if (event.getVehicle().getPassenger() != event.getEntity()) {
			BukkitMCVehicleEntityCollideEvent vec = new BukkitMCVehicleEntityCollideEvent(event);
			EventUtils.TriggerExternal(vec);
			EventUtils.TriggerListener(Driver.VEHICLE_COLLIDE, "vehicle_collide", vec);
		}
	}
}
