

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.events.BukkitAbstractEventMixin;
import com.laytonsmith.abstraction.bukkit.events.drivers.*;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.DoubleChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 *
 * @author layton
 */
@convert(type=Implementation.Type.BUKKIT)
public class BukkitConvertor implements Convertor {

    public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
        World w2 = null;
        if(w != null){
            w2 = ((BukkitMCWorld)w).__World();
        }
        return new BukkitMCLocation(new Location(w2, x, y, z, yaw, pitch));
    }

    public Class GetServerEventMixin() {
        return BukkitAbstractEventMixin.class;
    }

    public MCEnchantment[] GetEnchantmentValues() {
        MCEnchantment[] ea = new MCEnchantment[Enchantment.values().length];
        Enchantment[] oea = Enchantment.values();
        for (int i = 0; i < ea.length; i++) {
            ea[i] = new BukkitMCEnchantment(oea[i]);
        }
        return ea;

    }

    public MCEnchantment GetEnchantmentByName(String name) {
        try{
            //If they are looking it up by number, we can support that
            int i = Integer.valueOf(name);
            return new BukkitMCEnchantment(Enchantment.getById(i));
        } catch(NumberFormatException e){
            return new BukkitMCEnchantment(Enchantment.getByName(name));
        }
    }

    public MCServer GetServer() {
        return BukkitMCServer.Get();
    }

    public MCItemStack GetItemStack(int type, int qty) {
        return new BukkitMCItemStack(new ItemStack(type, qty));
    }
    public MCItemStack GetItemStack(int type, int data, int qty) {
        return new BukkitMCItemStack(new ItemStack(type, qty, (short)0, (byte)data));
    }
    
    public static final BukkitBlockListener BlockListener = new BukkitBlockListener();
    public static final BukkitEntityListener EntityListener = new BukkitEntityListener();
    public static final BukkitInventoryListener InventoryListener = new BukkitInventoryListener();
    public static final BukkitPlayerListener PlayerListener = new BukkitPlayerListener();
    public static final BukkitServerListener ServerListener = new BukkitServerListener();
    public static final BukkitVehicleListener VehicleListener = new BukkitVehicleListener();
    public static final BukkitWeatherListener WeatherListener = new BukkitWeatherListener();
    public static final BukkitWorldListener WorldListener = new BukkitWorldListener();

    public void Startup(CommandHelperPlugin chp) {
        chp.registerEvent((Listener)BlockListener);
        chp.registerEvent((Listener)EntityListener);
        chp.registerEvent((Listener)InventoryListener);
        chp.registerEvent((Listener)PlayerListener);
        chp.registerEvent((Listener)ServerListener);
        chp.registerEvent((Listener)VehicleListener);
        chp.registerEvent((Listener)WeatherListener);
        chp.registerEvent((Listener)WorldListener);        
    }

    public int LookupItemId(String materialName) {
        if(Material.matchMaterial(materialName) != null){
            return new MaterialData(Material.matchMaterial(materialName)).getItemTypeId();
        } else {
            return -1;
        }
    }

    public String LookupMaterialName(int id) {
        return Material.getMaterial(id).toString();
    }
    
    /**
     * We don't want to allow scripts to clear other plugin's tasks
     * on accident, so only ids registered through our interface
     * can also be cancelled.
     */
    private static Set<Integer> validIDs = new TreeSet<Integer>();

    public int SetFutureRunnable(long ms, Runnable r) {
        int id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CommandHelperPlugin.self, r, Static.msToTicks(ms));
        validIDs.add(id);
        return id;
    }
    
    public int SetFutureRepeater(long ms, long initialDelay, Runnable r){
        int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CommandHelperPlugin.self, r, Static.msToTicks(initialDelay), Static.msToTicks(ms));
        validIDs.add(id);
        return id;        
    }

    public void ClearAllRunnables() {
		//Doing cancelTasks apparently does not work, so let's just manually cancel each task, which does appear to work.
		//Anyways, it's better that way anyhow, because we actually remove IDs from validIDs that way.
        //((BukkitMCServer)Static.getServer()).__Server().getScheduler().cancelTasks(CommandHelperPlugin.self);
		Set<Integer> ids = new TreeSet<Integer>(validIDs);
		for(int id : ids){
			ClearFutureRunnable(id);
		}
    }

    public void ClearFutureRunnable(int id) {
        if(validIDs.contains(id)){
            Bukkit.getServer().getScheduler().cancelTask(id);
            validIDs.remove(id);
        }
    }
    
    public static MCEntity BukkitGetCorrectEntity(Entity be){
    	if (be == null) {
    		return null;
    	}
    	//TODO: Change this to a reflection mechanism, this is getting tiresome to do.
		if(be instanceof Item){
			return new BukkitMCItem((Item)be);
		}
		
		if(be instanceof LightningStrike){
			return new BukkitMCLightningStrike((LightningStrike)be);
		}
		
		if(be instanceof ExperienceOrb){
			return new BukkitMCExperienceOrb((ExperienceOrb)be);
		}
		
		if(be instanceof EnderCrystal){
			return new BukkitMCEnderCrystal((EnderCrystal)be);
		}
		
		if(be instanceof TNTPrimed){
			return new BukkitMCTNT((TNTPrimed)be);
		}
		
    	if(be instanceof Projectile){
            return new BukkitMCProjectile((Projectile)be);
        }
    	
    	if(be instanceof Tameable){
            return new BukkitMCTameable(be);
        }
        
        if(be instanceof Player){
            return new BukkitMCPlayer((Player)be);
        }
        
        if(be instanceof HumanEntity){
            return new BukkitMCHumanEntity((HumanEntity)be);
        }
        
        if(be instanceof LivingEntity){
            return new BukkitMCLivingEntity(((LivingEntity)be));
        }
        
        throw new Error("While trying to find the correct entity type for " + be.getClass().getName() + ", was unable"
                + " to find the appropriate implementation. Please alert the developers of this stack trace.");
    }

    public MCEntity GetCorrectEntity(MCEntity e) {

        Entity be = ((BukkitMCEntity)e).asEntity();
        return BukkitConvertor.BukkitGetCorrectEntity(be);
    }

	public MCInventory GetEntityInventory(int entityID) {
		Entity entity = null;
		outer: for(World w : Bukkit.getWorlds()){
			for(Entity e : w.getEntities()){
				if(e.getEntityId() == entityID){
					entity = e;
					break outer;
				}
			}
		}
		if(entity == null){
			return null;
		}
		if(entity instanceof InventoryHolder){
			if(entity instanceof Player){
				return new BukkitMCPlayerInventory(((Player)entity).getInventory());
			} else {
				return new BukkitMCInventory(((InventoryHolder)entity).getInventory());
			}
		} else {
			return null;
		}
	}

	public MCInventory GetLocationInventory(MCLocation location) {
		Block b = ((Location)(location.getHandle())).getBlock();
		if(b.getState() instanceof InventoryHolder){
			if(b.getState() instanceof DoubleChest){
				DoubleChest dc = (DoubleChest)(b.getState());
				return new BukkitMCDoubleChest(dc.getLeftSide().getInventory(), dc.getRightSide().getInventory());
			} else {
				return new BukkitMCInventory(((InventoryHolder)b.getState()).getInventory());
			}
		} else {
			return null;
		}
	}

}
