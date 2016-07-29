package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.abstraction.AbstractConvertor;
import com.laytonsmith.abstraction.ConvertorHelper;
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
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCPluginMeta;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCWorldCreator;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCAgeable;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCComplexEntityPart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCComplexLivingEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHanging;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHumanEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCMinecart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCProjectile;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCTameable;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCVehicle;
import com.laytonsmith.abstraction.bukkit.events.BukkitAbstractEventMixin;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitBlockListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitEntityListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitInventoryListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitPlayerListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitServerListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitVehicleListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitWeatherListener;
import com.laytonsmith.abstraction.bukkit.events.drivers.BukkitWorldListener;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPatternShape;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionType;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CancelCommandException;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.banner.Pattern;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionData;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/**
 *
 *
 */
@convert(type = Implementation.Type.BUKKIT)
public class BukkitConvertor extends AbstractConvertor {

	private static BukkitMCPluginMeta pluginMeta = null;

	@Override
	public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
		World w2 = null;
		if (w != null) {
			w2 = ((BukkitMCWorld) w).__World();
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
		try {
			//If they are looking it up by number, we can support that
			int i = Integer.valueOf(name);
			return new BukkitMCEnchantment(Enchantment.getById(i));
		} catch (NumberFormatException e) {
			try {
				return new BukkitMCEnchantment(Enchantment.getByName(name));
			} catch (NullPointerException ee) {
				return null;
			}
		} catch(NullPointerException e) {
			return null;
		}
	}

	@Override
	public MCServer GetServer() {
		return BukkitMCServer.Get();
	}

	@Override
	public MCMaterial getMaterial(int id) {
		Material mat = Material.getMaterial(id);
		return mat == null ? null : new BukkitMCMaterial(mat);
	}

	@Override
	public MCItemStack GetItemStack(int type, int qty) {
		return new BukkitMCItemStack(new ItemStack(type, qty));
	}

	@Override
	public MCItemStack GetItemStack(int type, int data, int qty) {
		return new BukkitMCItemStack(new ItemStack(type, qty, (short) data));
	}

	@Override
	public MCItemStack GetItemStack(MCMaterial type, int qty) {
		return new BukkitMCItemStack(new ItemStack(((BukkitMCMaterial) type).getHandle(), qty));
	}

	@Override
	public MCItemStack GetItemStack(MCMaterial type, int data, int qty) {
		return new BukkitMCItemStack(new ItemStack(((BukkitMCMaterial) type).getHandle(), qty, (short) data));
	}

	public MCItemStack GetItemStack(String type, int qty) {
		Material mat = Material.matchMaterial(type);
		if (mat == null) {
			return null;
		}
		return new BukkitMCItemStack(new ItemStack(mat, qty));
	}

	public MCItemStack GetItemStack(String type, int data, int qty) {
		Material mat = Material.matchMaterial(type);
		if (mat == null) {
			return null;
		}
		return new BukkitMCItemStack(new ItemStack(mat, qty, (short) data));
	}

	public MCPotionData GetPotionData(MCPotionType type, boolean extended, boolean upgraded){
		return new BukkitMCPotionData(new PotionData(
				BukkitMCPotionType.getConvertor().getConcreteEnum(type), extended, upgraded));
	}

	@Override
	public MCMetadataValue GetMetadataValue(Object value, MCPlugin plugin) {
		return new BukkitMCMetadataValue(new FixedMetadataValue(((BukkitMCPlugin) plugin).getHandle(), value));
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
		chp.registerEvents(BlockListener);
		chp.registerEventsDynamic(EntityListener);
		chp.registerEventsDynamic(InventoryListener);
		chp.registerEvents(PlayerListener);
		chp.registerEvents(ServerListener);
		chp.registerEvents(VehicleListener);
		chp.registerEvents(WeatherListener);
		chp.registerEvents(WorldListener);
	}

	@Override
	public int LookupItemId(String materialName) {
		if (Material.matchMaterial(materialName) != null) {
			return new MaterialData(Material.matchMaterial(materialName)).getItemTypeId();
		} else {
			return -1;
		}
	}

	@Override
	public String LookupMaterialName(int id) {
		return Material.getMaterial(id).toString();
	}

	@Override
	protected void triggerRunnable(final Runnable r) {
		try {
			runOnMainThreadAndWait(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					r.run();
					return null;
				}
			});
		} catch(CancellationException ex) {
			// Ignore the Exception when the plugin is disabled (server shutting down).
			if(CommandHelperPlugin.self.isEnabled()) {
				java.util.logging.Logger.getLogger(BukkitConvertor.class.getName()).log(Level.SEVERE, null, ex);
			}
		} catch (InterruptedException | ExecutionException ex) {
			java.util.logging.Logger.getLogger(BukkitConvertor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

//    /**
//     * We don't want to allow scripts to clear other plugin's tasks
//     * on accident, so only ids registered through our interface
//     * can also be cancelled.
//     */
//    private static final Set<Integer> validIDs = new TreeSet<Integer>();
//
//	@Override
//    public synchronized int SetFutureRunnable(DaemonManager dm, long ms, Runnable r) {
//        int id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CommandHelperPlugin.self, r, Static.msToTicks(ms));
//        validIDs.add(id);
//        return id;
//    }
//
//	@Override
//    public synchronized int SetFutureRepeater(DaemonManager dm, long ms, long initialDelay, Runnable r){
//        int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CommandHelperPlugin.self, r, Static.msToTicks(initialDelay), Static.msToTicks(ms));
//        validIDs.add(id);
//        return id;
//    }
//
//	@Override
//    public synchronized void ClearAllRunnables() {
//		//Doing cancelTasks apparently does not work, so let's just manually cancel each task, which does appear to work.
//		//Anyways, it's better that way anyhow, because we actually remove IDs from validIDs that way.
//        //((BukkitMCServer)Static.getServer()).__Server().getScheduler().cancelTasks(CommandHelperPlugin.self);
//		Set<Integer> ids = new TreeSet<Integer>(validIDs);
//		for(int id : ids){
//			try{
//				//If this doesn't work, it shouldn't kill everything.
//				ClearFutureRunnable(id);
//			} catch(Exception e){
//				Logger.getLogger(BukkitConvertor.class.getName()).log(null, Level.SEVERE, e);
//			}
//		}
//    }
//
//	@Override
//    public void ClearFutureRunnable(int id) {
//        if(validIDs.contains(id)){
//            Bukkit.getServer().getScheduler().cancelTask(id);
//            validIDs.remove(id);
//        }
//    }
	public static MCEntity BukkitGetCorrectEntity(Entity be) {
		if (be == null) {
			return null;
		}

		BukkitMCEntityType type = BukkitMCEntityType.valueOfConcrete(be.getType());
		if (type.getWrapperClass() != null) {
			return ReflectionUtils.newInstance(type.getWrapperClass(), new Class[]{Entity.class}, new Object[]{be});
		}

		if (be instanceof Hanging) {
			type.setWrapperClass(BukkitMCHanging.class);
			return new BukkitMCHanging(be);
		}

		if (be instanceof Minecart) {
			// Must come before Vehicle
			type.setWrapperClass(BukkitMCMinecart.class);
			return new BukkitMCMinecart(be);
		}

		if (be instanceof Projectile) {
			type.setWrapperClass(BukkitMCProjectile.class);
			return new BukkitMCProjectile(be);
		}

		if (be instanceof Tameable) {
			// Must come before Ageable
			type.setWrapperClass(BukkitMCTameable.class);
			return new BukkitMCTameable(be);
		}

		if (be instanceof Ageable) {
			// Must come before LivingEntity
			type.setWrapperClass(BukkitMCAgeable.class);
			return new BukkitMCAgeable(be);
		}

		if (be instanceof HumanEntity) {
			// Must come before LivingEntity
			type.setWrapperClass(BukkitMCHumanEntity.class);
			return new BukkitMCHumanEntity(be);
		}

		if (be instanceof ComplexEntityPart) {
			type.setWrapperClass(BukkitMCComplexEntityPart.class);
			return new BukkitMCComplexEntityPart(be);
		}

		if (be instanceof ComplexLivingEntity) {
			// Must come before LivingEntity
			type.setWrapperClass(BukkitMCComplexLivingEntity.class);
			return new BukkitMCComplexLivingEntity(be);
		}

		if (be instanceof LivingEntity) {
			type.setWrapperClass(BukkitMCLivingEntity.class);
			return new BukkitMCLivingEntity(be);
		}

		if (be instanceof Vehicle) {
			type.setWrapperClass(BukkitMCVehicle.class);
			return new BukkitMCVehicle(be);
		}

		// Handle generically if we can't find a more specific type
		type.setWrapperClass(BukkitMCEntity.class);
		return new BukkitMCEntity(be);
	}

	@Override
	public MCEntity GetCorrectEntity(MCEntity e) {

		Entity be = ((BukkitMCEntity) e).getHandle();
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
		if (location == null) {
			return Collections.EMPTY_LIST;
		}
		if (radius <= 0) {
			radius = 1;
		}
		Entity tempEntity = ((BukkitMCEntity) location.getWorld().spawn(location, MCEntityType.MCVanillaEntityType.ARROW)).getHandle();
		List<Entity> near = tempEntity.getNearbyEntities(radius, radius, radius);
		tempEntity.remove();
		List<MCEntity> entities = new ArrayList<MCEntity>();
		for (Entity e : near) {
			entities.add(BukkitGetCorrectEntity(e));
		}
		return entities;
	}

	public static MCItemMeta BukkitGetCorrectMeta(ItemMeta im) {
		if (Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_8) && im instanceof BannerMeta) {
			return new BukkitMCBannerMeta((BannerMeta) im);
		}
		if (im instanceof BookMeta) {
			return new BukkitMCBookMeta((BookMeta) im);
		}
		if (im instanceof EnchantmentStorageMeta) {
			return new BukkitMCEnchantmentStorageMeta((EnchantmentStorageMeta) im);
		}
		if (im instanceof FireworkEffectMeta) {
			return new BukkitMCFireworkEffectMeta((FireworkEffectMeta) im);
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
	public MCInventory GetEntityInventory(MCEntity e) {
		Entity entity = ((BukkitMCEntity) e).getHandle();
		if (entity instanceof InventoryHolder) {
			if (entity instanceof Player) {
				return new BukkitMCPlayerInventory(((Player) entity).getInventory());
			} else {
				return new BukkitMCInventory(((InventoryHolder) entity).getInventory());
			}
		} else {
			return null;
		}
	}

	@Override
	public MCInventory GetLocationInventory(MCLocation location) {
		Block b = ((Location) (location.getHandle())).getBlock();
		if (b.getState() instanceof InventoryHolder) {
			if (b.getState() instanceof DoubleChest) {
				DoubleChest dc = (DoubleChest) (b.getState());
				return new BukkitMCDoubleChest(dc.getLeftSide().getInventory(), dc.getRightSide().getInventory());
			} else {
				return new BukkitMCInventory(((InventoryHolder) b.getState()).getInventory());
			}
		} else {
			return null;
		}
	}

	@Override
	public void runOnMainThreadLater(DaemonManager dm, final Runnable r) {
		if(!CommandHelperPlugin.self.isEnabled()) {
			throw new CancelCommandException(Implementation.GetServerType().getBranding()
					+ " tried to schedule a task while the plugin was disabled (is the server shutting down?).", Target.UNKNOWN);
		}
		Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				r.run();
				return null;
			}
		});
	}

	@Override
	public <T> T runOnMainThreadAndWait(Callable<T> callable) throws InterruptedException, ExecutionException {
		if(!CommandHelperPlugin.self.isEnabled()) {
			throw new CancelCommandException(Implementation.GetServerType().getBranding()
					+ " tried to schedule a task while the plugin was disabled (is the server shutting down?).", Target.UNKNOWN);
		}
		return Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, callable).get();
	}

	@Override
	public MCWorldCreator getWorldCreator(String worldName) {
		return new BukkitMCWorldCreator(worldName);
	}

	@Override
	public MCNote GetNote(int octave, MCTone tone, boolean sharp) {
		return new BukkitMCNote(octave, tone, sharp);
	}

	@Override
	public MCColor GetColor(int red, int green, int blue) {
		return BukkitMCColor.GetMCColor(Color.fromRGB(red, green, blue));
	}

	@Override
	public MCPattern GetPattern(MCDyeColor color, MCPatternShape shape) {
		return new BukkitMCPattern(new Pattern(BukkitMCDyeColor.getConvertor().getConcreteEnum(color),
				BukkitMCPatternShape.getConvertor().getConcreteEnum(shape)));
	}

	@Override
	public MCFireworkBuilder GetFireworkBuilder() {
		return new BukkitMCFireworkBuilder();
	}

	@Override
	public MCPluginMeta GetPluginMeta() {
		if (pluginMeta == null) {
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
		Material match = Material.matchMaterial(name);
		if (match == null) {
			return null;
		}
		return new BukkitMCMaterial(match);
	}

	@Override
	public String GetPluginName() {
		return (String) new Yaml().loadAs(getClass().getResourceAsStream("/plugin.yml"), Map.class).get("name");
	}

	@Override
	public MCPlugin GetPlugin() {
		return new BukkitMCPlugin(CommandHelperPlugin.self);
	}

	@Override
	public MCColor GetColor(String colorName, Target t) throws CREFormatException {
		return ConvertorHelper.GetColor(colorName, t);
	}

	@Override
	public String GetUser(Environment env) {
		MCCommandSender cs = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
		if(cs == null){
			return null;
		} else {
			String name = cs.getName();
			if("CONSOLE".equals(name)){
				name = "~console";
			}
			return name;
		}
	}
}
