

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.abstraction.AbstractConvertor;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCPluginMeta;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCWorldCreator;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCFallingBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCBoat;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCComplexEntityPart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCComplexLivingEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCCreeper;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEnderDragon;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEnderDragonPart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEnderSignal;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEnderman;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntityProjectileSource;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFirework;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFishHook;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHorse;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCIronGolem;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCItemFrame;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCMagmaCube;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCMinecart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCOcelot;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPigZombie;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCSheep;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCSkeleton;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCSlime;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCThrownPotion;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCVillager;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCWolf;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCZombie;
import com.laytonsmith.abstraction.bukkit.events.BukkitAbstractEventMixin;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitBlockListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitEntityListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitInventoryListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitPlayerListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitServerListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitVehicleListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitWeatherListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitWorldListener;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
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
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Boat;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Listener;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.projectiles.ProjectileSource;

/**
 *
 * @author layton
 */
@convert(type=Implementation.Type.BUKKIT)
public class BukkitConvertor extends AbstractConvertor {
	
	private static BukkitMCPluginMeta pluginMeta = null;

	@Override
    public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
        World w2 = null;
        if(w != null){
            w2 = ((BukkitMCWorld)w).__World();
        }
        return new BukkitMCLocation(new Location(w2, x, y, z, yaw, pitch));
    }

	@Override
    public Class GetServerEventMixin() {
        return BukkitAbstractEventMixin.class;
    }

	@Override
    public MCEnchantment[] GetEnchantmentValues() {
        MCEnchantment[] ea = new MCEnchantment[Enchantment.values().length];
        Enchantment[] oea = Enchantment.values();
        for (int i = 0; i < ea.length; i++) {
            ea[i] = new BukkitMCEnchantment(oea[i]);
        }
        return ea;

    }

	@Override
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

	@Override
    public MCServer GetServer() {
        return BukkitMCServer.Get();
    }

	@Override
	public MCMaterial getMaterial(int id) {
		return new BukkitMCMaterial(Material.getMaterial(id));
	}

	@Override
    public MCItemStack GetItemStack(int type, int qty) {
        return new BukkitMCItemStack(new ItemStack(type, qty));
    }
	@Override
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

	@Override
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

	@Override
    public int LookupItemId(String materialName) {
        if(Material.matchMaterial(materialName) != null){
            return new MaterialData(Material.matchMaterial(materialName)).getItemTypeId();
        } else {
            return -1;
        }
    }

	@Override
    public String LookupMaterialName(int id) {
        return Material.getMaterial(id).toString();
    }
    
    /**
     * We don't want to allow scripts to clear other plugin's tasks
     * on accident, so only ids registered through our interface
     * can also be cancelled.
     */
    private static final Set<Integer> validIDs = new TreeSet<Integer>();

	@Override
    public synchronized int SetFutureRunnable(DaemonManager dm, long ms, Runnable r) {
        int id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CommandHelperPlugin.self, r, Static.msToTicks(ms));
        validIDs.add(id);
        return id;
    }
    
	@Override
    public synchronized int SetFutureRepeater(DaemonManager dm, long ms, long initialDelay, Runnable r){
        int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CommandHelperPlugin.self, r, Static.msToTicks(initialDelay), Static.msToTicks(ms));
        validIDs.add(id);
        return id;        
    }

	@Override
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

	@Override
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
    	
		// Steve!
        if(be instanceof Player){
			// Must come before HumanEntity
            return new BukkitMCPlayer((Player)be);
        }
		
		// Mobs - Passive / Tamable
		if(be instanceof Villager){
            return new BukkitMCVillager((Villager)be);
        }
		
		if(be instanceof Wolf){
            return new BukkitMCWolf(be);
        }
		
		if(be instanceof Ocelot){
            return new BukkitMCOcelot(be);
        }
		
		if (be instanceof Sheep) {
			return new BukkitMCSheep((Sheep) be);
		}
		
		if (be instanceof Horse) {
			// Must come before Vehicle
			return new BukkitMCHorse((Horse) be);
		}
		
		if (be instanceof Pig) {
			// Must come before Vehicle
			return new BukkitMCPig((Pig) be);
		}
		
		// Mobs - Enemies
		if (be instanceof EnderDragon) {
			// Must come before ComplexLivingEntity
			return new BukkitMCEnderDragon((EnderDragon) be);
		}
		
		if (be instanceof Enderman) {
			return new BukkitMCEnderman((Enderman) be);
		}
		
		if (be instanceof Creeper) {
    		return new BukkitMCCreeper((Creeper) be);
    	}
		
		if (be instanceof IronGolem) {
    		return new BukkitMCIronGolem((IronGolem) be);
    	}
		
		if (be instanceof Skeleton) {
    		return new BukkitMCSkeleton((Skeleton) be);
    	}
		
		if (be instanceof MagmaCube) {
			// Must come before Slime
    		return new BukkitMCMagmaCube((MagmaCube) be);
    	}
		
		if (be instanceof Slime) {
    		return new BukkitMCSlime((Slime) be);
    	}
		
		if (be instanceof PigZombie) {
			// Must come before Zombie
			return new BukkitMCPigZombie((PigZombie) be);
		}
		
		if (be instanceof Zombie) {
			return new BukkitMCZombie((Zombie) be);
		}
		
		// Block entities
		if(be instanceof FallingBlock){
			return new BukkitMCFallingBlock((FallingBlock) be);
		}
		
		if(be instanceof TNTPrimed){
			return new BukkitMCTNT((TNTPrimed)be);
		}
		
		if(be instanceof EnderCrystal){
			return new BukkitMCEnderCrystal((EnderCrystal)be);
		}
		
		// Pickups
		if(be instanceof Item){
			return new BukkitMCItem((Item)be);
		}
		
		if(be instanceof ExperienceOrb){
			return new BukkitMCExperienceOrb((ExperienceOrb)be);
		}
		
		// Projectiles
		if (be instanceof ThrownPotion) {
			return new BukkitMCThrownPotion((ThrownPotion)be);
		}
		
		if (be instanceof Fish) {
			return new BukkitMCFishHook((Fish) be);
		}
		
		if (be instanceof Fireball) {
			return new BukkitMCFireball((Fireball) be);
		}
		
		if (be instanceof Firework) { 
			// Not really a projectile, but it fits here.
			return new BukkitMCFirework((Firework) be);
		}
    	
		// Static / Hanging
		if (be instanceof EnderSignal) {
    		return new BukkitMCEnderSignal((EnderSignal) be);
    	}
		
		if (be instanceof ItemFrame) {
			// Must come before Hanging
    		return new BukkitMCItemFrame((ItemFrame) be);
    	}
		
		if(be instanceof Painting){
			// Must come before Hanging
			return new BukkitMCPainting((Painting)be);
		}
		
    	if(be instanceof Hanging){
    		return new BukkitMCHanging(be);
    	}
		
		// Vehicles
    	if(be instanceof Minecart) {
			// Must come before Vehicle
    		return new BukkitMCMinecart((Minecart)be);
    	}
		
		if(be instanceof Boat) {
			// Must come before Vehicle
			return new BukkitMCBoat((Boat)be);
    	}
        
		// Weather
		if(be instanceof LightningStrike){
			return new BukkitMCLightningStrike((LightningStrike)be);
		}
		
		// Misc
		if (be instanceof EnderDragonPart) {
			// Must come before ComplexLivingEntity
			return new BukkitMCEnderDragonPart((EnderDragonPart) be);
		}
        
		// Abstractions
		if(be instanceof Projectile){
            return new BukkitMCProjectile((Projectile)be);
        }
		
    	if(be instanceof Ageable){
			// Must come before LivingEntity
    		return new BukkitMCAgeable(be);
    	}
		
        if(be instanceof HumanEntity){
			// Must come before LivingEntity
            return new BukkitMCHumanEntity((HumanEntity)be);
        }
		
		if(be instanceof ComplexEntityPart) {
			return new BukkitMCComplexEntityPart((ComplexEntityPart)be);
		}
		
		if(be instanceof ComplexLivingEntity) {
			// Must come before LivingEntity
			return new BukkitMCComplexLivingEntity((ComplexLivingEntity)be);
		}
        
        if(be instanceof LivingEntity){
            return new BukkitMCLivingEntity(((LivingEntity)be));
        }
		
		if (be instanceof ProjectileSource) {
			return new BukkitMCEntityProjectileSource(be);
		}
		
    	if(be instanceof Vehicle){
    		return new BukkitMCVehicle(be);
    	}
		
		throw new IllegalArgumentException("While trying to find the correct entity type for " + be.getClass().getName()
				+ ", was unable to find the appropriate implementation. If the named entity is not provided by mods,"
				+ " please alert the developers of this stack trace. This is not necessarily an error,"
				+ " we just don't have any special handling for this entity yet, and will treat it generically.");
	}

	@Override
	public MCEntity GetCorrectEntity(MCEntity e) {

		Entity be = ((BukkitMCEntity)e).asEntity();
		try {
			return BukkitConvertor.BukkitGetCorrectEntity(be);
		} catch (IllegalArgumentException iae) {
			CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, LogLevel.INFO, iae.getMessage(), Target.UNKNOWN);
			return e;
		}
	}

	@Override
	public MCItemMeta GetCorrectMeta(MCItemMeta im) {
		ItemMeta bim = ((BukkitMCItemMeta) im).asItemMeta();
		return BukkitConvertor.BukkitGetCorrectMeta(bim);
	}

	@Override
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
    
	@Override
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

	@Override
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

			@Override
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
	
	@Override
	public synchronized int getMaxBlockID() {
		if (maxBlockID == -1) {
			calculateIDs();
		}
		return maxBlockID;
	}
	
	@Override
	public synchronized int getMaxItemID() {
		if (maxItemID == -1) {
			calculateIDs();
		}
		return maxItemID;
	}
	
	@Override
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

	@Override
	public MCColor GetColor(int red, int green, int blue) {
		return BukkitMCColor.GetMCColor(Color.fromRGB(red, green, blue));
	}

	@Override
	public MCFireworkBuilder GetFireworkBuilder() {
		return new BukkitMCFireworkBuilder();
	}

	@Override
	public MCPluginMeta GetPluginMeta() {
		if(pluginMeta == null){
			pluginMeta = new BukkitMCPluginMeta(CommandHelperPlugin.self);
			addShutdownHook(new Runnable() {

				@Override
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

	@Override
	public MCMaterial GetMaterial(String name) {
		return new BukkitMCMaterial(Material.valueOf(name));
	}
}
