

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCFallingBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.bukkit.entities.*;
import com.laytonsmith.abstraction.bukkit.events.BukkitAbstractEventMixin;
import com.laytonsmith.abstraction.bukkit.events.drivers.*;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
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
			try{
				return new BukkitMCEnchantment(Enchantment.getByName(name));
			} catch(NullPointerException ee){
				return null;
			}
        }
    }

    public MCServer GetServer() {
        return BukkitMCServer.Get();
    }

	public MCMaterial getMaterial(int id) {
		return new BukkitMCMaterial(Material.getMaterial(id));
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
    private static final Set<Integer> validIDs = new TreeSet<Integer>();

    public synchronized int SetFutureRunnable(DaemonManager dm, long ms, Runnable r) {
        int id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CommandHelperPlugin.self, r, Static.msToTicks(ms));
        validIDs.add(id);
        return id;
    }
    
    public synchronized int SetFutureRepeater(DaemonManager dm, long ms, long initialDelay, Runnable r){
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
    
    public static MCEntity BukkitGetCorrectEntity(Entity be){
    	if (be == null) {
    		return null;
    	}
    	//TODO: Change this to a reflection mechanism, this is getting tiresome to do.
		//truth.
    	if (be instanceof EnderSignal) {
    		return new BukkitMCEnderSignal((EnderSignal) be);
    	}
    	
		if (be instanceof Firework) {
			return new BukkitMCFirework((Firework) be);
		}
		
		if(be instanceof FallingBlock){
			return new BukkitMCFallingBlock((FallingBlock) be);
		}
		
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
		
		if (be instanceof Fish) {
			return new BukkitMCFishHook((Fish) be);
		}
		
		if (be instanceof Fireball) {
			return new BukkitMCFireball((Fireball) be);
		}
		
    	if(be instanceof Projectile){
            return new BukkitMCProjectile((Projectile)be);
        }
    	
		if(be instanceof Painting){
			return new BukkitMCPainting((Painting)be);
		}
		
    	if(be instanceof Hanging){
    		return new BukkitMCHanging(be);
    	}
    	
    	if(be instanceof Wolf){
            return new BukkitMCWolf(be);
        }
    	
    	if(be instanceof Ocelot){
            return new BukkitMCOcelot(be);
        }
    	
		if (be instanceof Enderman) {
			return new BukkitMCEnderman((Enderman) be);
		}
		
		if (be instanceof Sheep) {
			return new BukkitMCSheep((Sheep) be);
		}
		
		if (be instanceof Horse) {
			return new BukkitMCHorse((Horse) be);
		}
		
		if (be instanceof Pig) {
			return new BukkitMCPig((Pig) be);
		}
		
    	if(be instanceof Ageable){
    		return new BukkitMCAgeable(be);
    	}
    	
    	if(be instanceof Boat) {
			return new BukkitMCBoat((Boat)be);
    	}

    	if(be instanceof Minecart) {
    		return new BukkitMCMinecart((Minecart)be);
    	}

    	if(be instanceof Vehicle){
    		return new BukkitMCVehicle(be);
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

	public MCItemMeta GetCorrectMeta(MCItemMeta im) {
		ItemMeta bim = ((BukkitMCItemMeta) im).asItemMeta();
		return BukkitConvertor.BukkitGetCorrectMeta(bim);
	}

	public List<MCEntity> GetEntitiesAt(MCLocation location, double radius) {
		if(location == null){
			return Collections.EMPTY_LIST;
		}
		if(radius <= 0){
			radius = 1;
		}
		Entity tempEntity = ((BukkitMCEntity)location.getWorld().spawn(location, MCEntityType.ARROW)).asEntity();
		List<Entity> near = tempEntity.getNearbyEntities(radius, radius, radius);
		tempEntity.remove();
		List<MCEntity> entities = new ArrayList<MCEntity>();
		for(Entity e : near){
			entities.add(BukkitGetCorrectEntity(e));
		}
		return entities;
	}

	public static MCItemMeta BukkitGetCorrectMeta(ItemMeta im) {
		if (im instanceof BookMeta) {
			return new BukkitMCBookMeta((BookMeta) im);
		}
		if (im instanceof EnchantmentStorageMeta) {
			return new BukkitMCEnchantmentStorageMeta((EnchantmentStorageMeta) im);
		}
		if (im instanceof FireworkEffectMeta) {
			
		}
		if (im instanceof FireworkMeta) {
			return new BukkitMCFireworkMeta((FireworkMeta) im);
		}
		if (im instanceof LeatherArmorMeta) {
			return new BukkitMCLeatherArmorMeta((LeatherArmorMeta) im);
		}
		if (im instanceof PotionMeta) {
			return new BukkitMCPotionMeta((PotionMeta) im);
		}
		if (im instanceof SkullMeta) {
			return new BukkitMCSkullMeta((SkullMeta) im);
		}
		return new BukkitMCItemMeta(im);
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

	@Override
	public void runOnMainThreadLater(DaemonManager dm, final Runnable r) {
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

	@Override
	public MCRecipe GetNewRecipe(MCRecipeType type, MCItemStack result) {
		switch (type) {
			case FURNACE:
				return new BukkitMCFurnaceRecipe(result);
			case SHAPED:
				return new BukkitMCShapedRecipe(result);
			case SHAPELESS:
				return new BukkitMCShapelessRecipe(result);
		}
		return null;
	}

	@Override
	public MCRecipe GetRecipe(MCRecipe unspecific) {
		Recipe r = ((BukkitMCRecipe) unspecific).r;
		return BukkitGetRecipe(r);
	}

	public static MCRecipe BukkitGetRecipe(Recipe r) {
		if (r instanceof ShapelessRecipe) {
			return new BukkitMCShapelessRecipe((ShapelessRecipe) r);
		} else if (r instanceof ShapedRecipe) {
			return new BukkitMCShapedRecipe((ShapedRecipe) r);
		} else if (r instanceof FurnaceRecipe) {
			return new BukkitMCFurnaceRecipe((FurnaceRecipe) r);
		} else {
			return null;
		}
	}
	
	@Override
	public MCCommand getNewCommand(String name) {
		return BukkitMCCommand.newCommand(name);
	}
	
	@Override
	public MCCommandSender GetCorrectSender(MCCommandSender unspecific) {
		if (unspecific == null) {
			return null;
		}
		return BukkitGetCorrectSender(((BukkitMCCommandSender) unspecific)._CommandSender());
	}
	
	public static MCCommandSender BukkitGetCorrectSender(CommandSender sender) {
		if (sender instanceof Player) {
			return new BukkitMCPlayer((Player) sender);
		} else if (sender instanceof ConsoleCommandSender) {
			return new BukkitMCConsoleCommandSender((ConsoleCommandSender) sender);
		} else if (sender instanceof BlockCommandSender) {
			return new BukkitMCBlockCommandSender((BlockCommandSender) sender);
		} else {
			return null;
		}
	}
}
