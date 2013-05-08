

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.bukkit.events.BukkitAbstractEventMixin;
import com.laytonsmith.abstraction.bukkit.events.drivers.*;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Color;
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
import org.bukkit.inventory.meta.*;
import org.bukkit.material.MaterialData;

/**
 *
 * @author layton
 */
@convert(type=Implementation.Type.BUKKIT)
public class BukkitConvertor extends AbstractConvertor {
	
	private static BukkitMCPluginMeta pluginMeta = null;

    public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
        World w2 = null;
        if(w != null){
            w2 = w.getHandle();
        }
        return AbstractionUtils.wrap(MCLocation.class, new Location(w2, x, y, z, yaw, pitch));
    }

    public Class GetServerEventMixin() {
        return BukkitAbstractEventMixin.class;
    }

    public MCEnchantment[] GetEnchantmentValues() {
        MCEnchantment[] ea = new MCEnchantment[Enchantment.values().length];
        Enchantment[] oea = Enchantment.values();
        for (int i = 0; i < ea.length; i++) {
            ea[i] = AbstractionUtils.wrap(MCEnchantment.class, oea[i]);
        }
        return ea;

    }

    public MCEnchantment GetEnchantmentByName(String name) {
        try{
            //If they are looking it up by number, we can support that
            int i = Integer.valueOf(name);
            return AbstractionUtils.wrap(MCEnchantment.class, Enchantment.getById(i));
        } catch(NumberFormatException e){
			try{
				return AbstractionUtils.wrap(MCEnchantment.class, Enchantment.getByName(name));
			} catch(NullPointerException ee){
				return null;
			}
        }
    }

    public MCServer GetServer() {
        return AbstractionUtils.wrap(MCServer.class, Bukkit.getServer());
    }

	public MCMaterial getMaterial(int id) {
		return AbstractionUtils.wrap(MCMaterial.class, Material.getMaterial(id));
	}

    public MCItemStack GetItemStack(int type, int qty) {
        return AbstractionUtils.wrap(MCItemStack.class, new ItemStack(type, qty));
    }
	
	@Deprecated
    public MCItemStack GetItemStack(int type, int data, int qty) {
        return AbstractionUtils.wrap(MCItemStack.class, new ItemStack(type, qty, (short)0, (byte)data));
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

    public synchronized int SetFutureRunnable(long ms, Runnable r) {
        int id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CommandHelperPlugin.self, r, Static.msToTicks(ms));
        validIDs.add(id);
        return id;
    }
    
    public synchronized int SetFutureRepeater(long ms, long initialDelay, Runnable r){
        int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CommandHelperPlugin.self, r, Static.msToTicks(initialDelay), Static.msToTicks(ms));
        validIDs.add(id);
        return id;        
    }

    public synchronized void ClearAllRunnables() {
		//Doing cancelTasks apparently does not work, so let's just manually cancel each task, which does appear to work.
		//Anyways, it's better that way anyhow, because we actually remove IDs from validIDs that way.
        //((BukkitMCServer)Static.getServer()).__Server().getScheduler().cancelTasks(CommandHelperPlugin.self);
		Set<Integer> ids = new TreeSet<Integer>(validIDs);
		for(int id : ids){
			try{
				//If this doesn't work, it shouldn't kill everything.
				ClearFutureRunnable(id);
			} catch(Exception e){
				Logger.getLogger(BukkitConvertor.class.getName()).log(null, Level.SEVERE, e);
			}
		}
    }

    public void ClearFutureRunnable(int id) {
        if(validIDs.contains(id)){
            Bukkit.getServer().getScheduler().cancelTask(id);
            validIDs.remove(id);
        }
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
				return AbstractionUtils.wrap(((Player)entity).getInventory());
			} else {
				return AbstractionUtils.wrap(((InventoryHolder)entity).getInventory());
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
				return AbstractionUtils.wrap(((InventoryHolder)b.getState()).getInventory());
			}
		} else {
			return null;
		}
	}

	@Override
	public void runOnMainThreadLater(final Runnable r) {
		Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, new Callable<Object>() {

			public Object call() throws Exception {
				r.run();
				return null;
			}
		});
	}

	@Override
	public <T> T runOnMainThreadAndWait(Callable<T> callable) {
		return (T)Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, callable);
	}

	@Override
	public MCWorldCreator getWorldCreator(String worldName) {
		return new BukkitMCWorldCreator(worldName);
	}

	@Override
	public MCNote GetNote(int octave, MCTone tone, boolean sharp) {
		return new BukkitMCNote(octave, tone, sharp);
	}
	
	private static int maxBlockID = -1;
	private static int maxItemID = -1;
	private static int maxRecordID = -1;
	
	public synchronized int getMaxBlockID() {
		if (maxBlockID == -1) {
			calculateIDs();
		}
		return maxBlockID;
	}
	
	public synchronized int getMaxItemID() {
		if (maxItemID == -1) {
			calculateIDs();
		}
		return maxItemID;
	}
	
	public synchronized int getMaxRecordID() {
		if (maxRecordID == -1) {
			calculateIDs();
		}
		return maxRecordID;
	}
	
	private void calculateIDs() {
		maxBlockID = 0;
		maxItemID = 256;
		maxRecordID = 2256;
		for (Material m : Material.values()) {
			int mID = m.getId();
			if (mID >= maxRecordID) {
				maxRecordID = mID;
			} else if (mID >= maxItemID) {
				maxItemID = mID;
			} else if (mID >= maxBlockID) {
				maxBlockID = mID;
			}
		}
	}

	public MCColor GetColor(int red, int green, int blue) {
		return BukkitMCColor.GetMCColor(Color.fromRGB(red, green, blue));
	}

	public MCFireworkBuilder GetFireworkBuilder() {
		return new BukkitMCFireworkBuilder();
	}

	public MCPluginMeta GetPluginMeta() {
		if(pluginMeta == null){
			pluginMeta = new BukkitMCPluginMeta(CommandHelperPlugin.self);
			addShutdownHook(new Runnable() {

				public void run() {
					pluginMeta = null;
				}
			});
		}
		return pluginMeta;
	}

}
