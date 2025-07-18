package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.abstraction.AbstractConvertor;
import com.laytonsmith.abstraction.ConvertorHelper;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCNamespacedKey;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCPluginMeta;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MCWorldCreator;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBanner;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBeacon;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBeehive;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockState;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBrewingStand;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCChest;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCCommandBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCContainer;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCDecoratedPot;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCDispenser;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCDropper;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCEndGateway;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCFurnace;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCLectern;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCSign;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCSkull;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCAgeable;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCAnimal;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCBoat;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCBreedable;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCChestBoat;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCCommandMinecart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCComplexEntityPart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCComplexLivingEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFireball;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHanging;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHumanEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCMinecart;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCProjectile;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCSizedFireball;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCTameable;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCItemProjectile;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCTransformation;
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
import com.laytonsmith.abstraction.entities.MCTransformation;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCEquipmentSlotGroup;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDyeColor;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEquipmentSlot;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCLegacyMaterial;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MSLog;
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
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Beehive;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.DecoratedPot;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.EndGateway;
import org.bukkit.block.Furnace;
import org.bukkit.block.Lectern;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.ChestBoat;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SizedFireball;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.AxolotlBucketMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionType;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@convert(type = Implementation.Type.BUKKIT)
public class BukkitConvertor extends AbstractConvertor {

	private static BukkitMCPluginMeta pluginMeta = null;

	@Override
	public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
		World w2 = null;
		if(w != null) {
			w2 = ((BukkitMCWorld) w).__World();
		}
		return new BukkitMCLocation(new Location(w2, x, y, z, yaw, pitch));
	}

	@Override
	public Class GetServerEventMixin() {
		return BukkitAbstractEventMixin.class;
	}

	@Override
	public MCServer GetServer() {
		return BukkitMCServer.Get();
	}

	@Override
	public MCMaterial[] GetMaterialValues() {
		Material[] mats = Material.values();
		MCMaterial[] ret = new MCMaterial[mats.length];
		for(int i = 0; i < mats.length; i++) {
			ret[i] = BukkitMCMaterial.valueOfConcrete(mats[i]);
		}
		return ret;
	}

	@Override
	public MCMaterial GetMaterialFromLegacy(String mat, int data) {
		Material m = BukkitMCLegacyMaterial.getMaterial(mat, data);
		return m == null ? null : BukkitMCMaterial.valueOfConcrete(m);
	}

	@Override
	public MCMaterial GetMaterialFromLegacy(int id, int data) {
		Material m = BukkitMCLegacyMaterial.getMaterial(id, data);
		return m == null ? null : BukkitMCMaterial.valueOfConcrete(m);
	}

	@Override
	public MCMaterial GetMaterial(String name) {
		MCMaterial ret = MCMaterial.get(name);
		if(ret != null) {
			return ret;
		}
		// Try fuzzy match
		Material match = Material.matchMaterial(name);
		if(match != null) {
			return BukkitMCMaterial.valueOfConcrete(match);
		}
		// Try legacy
		match = BukkitMCLegacyMaterial.getMaterial(name);
		if(match != null) {
			return BukkitMCMaterial.valueOfConcrete(match);
		}
		return null;
	}

	@Override
	public MCItemStack GetItemStack(MCMaterial type, int qty) {
		return new BukkitMCItemStack(new ItemStack(((BukkitMCMaterial) type).getHandle(), qty));
	}

	@Override
	public MCItemStack GetItemStack(String type, int qty) {
		Material mat = Material.getMaterial(type);
		if(mat == null) {
			mat = BukkitMCLegacyMaterial.getMaterial(type);
		}
		if(mat == null) {
			mat = Material.matchMaterial(type);
		}
		if(mat == null || !mat.isItem()) {
			return null;
		}
		return new BukkitMCItemStack(new ItemStack(mat, qty));
	}

	@Override
	public MCPotionData GetPotionData(MCPotionType type, boolean extended, boolean upgraded) {
		try {
			Class clz = Class.forName("org.bukkit.potion.PotionData");
			return new BukkitMCPotionData(ReflectionUtils.newInstance(clz,
					new Class[]{PotionType.class, boolean.class, boolean.class},
					new Object[]{type.getConcrete(), extended, upgraded}));
		} catch (ClassNotFoundException ex) {
			// probably after 1.20.5
			// use PotionType instead
			return null;
		}
	}

	@Override
	public MCAttributeModifier GetAttributeModifier(MCAttribute attr, UUID id, String name, double amt, MCAttributeModifier.Operation op, MCEquipmentSlot slot) {
		if(id == null) {
			id = UUID.randomUUID();
		}
		AttributeModifier mod = new AttributeModifier(id, name, amt,
				BukkitMCAttributeModifier.Operation.getConvertor().getConcreteEnum(op),
				BukkitMCEquipmentSlot.getConvertor().getConcreteEnum(slot));
		return new BukkitMCAttributeModifier((Attribute) attr.getConcrete(), mod);
	}

	@Override
	public MCAttributeModifier GetAttributeModifier(MCAttribute attr, UUID id, String name, double amt, MCAttributeModifier.Operation op, MCEquipmentSlotGroup slot) {
		if(!((BukkitMCServer) Static.getServer()).isPaper()) {
			// BODY is missing from Spigot, so this falls back to ARMOR just like EquipmentSlot.BODY
			if(slot == MCEquipmentSlotGroup.BODY) {
				slot = MCEquipmentSlotGroup.ARMOR;
			}
		}
		if(id == null) {
			id = UUID.randomUUID();
		}
		AttributeModifier mod = new AttributeModifier(id, name, amt,
				BukkitMCAttributeModifier.Operation.getConvertor().getConcreteEnum(op),
				EquipmentSlotGroup.getByName(slot.name()));
		return new BukkitMCAttributeModifier((Attribute) attr.getConcrete(), mod);
	}

	@Override
	public MCAttributeModifier GetAttributeModifier(MCAttribute attr, MCNamespacedKey key, double amt, MCAttributeModifier.Operation op, MCEquipmentSlot slot) {
		EquipmentSlot es = BukkitMCEquipmentSlot.getConvertor().getConcreteEnum(slot);
		AttributeModifier mod = new AttributeModifier((NamespacedKey) key.getHandle(), amt,
				BukkitMCAttributeModifier.Operation.getConvertor().getConcreteEnum(op),
				es == null ? EquipmentSlotGroup.ANY : es.getGroup());
		return new BukkitMCAttributeModifier((Attribute) attr.getConcrete(), mod);
	}

	@Override
	public MCAttributeModifier GetAttributeModifier(MCAttribute attr, MCNamespacedKey key, double amt, MCAttributeModifier.Operation op, MCEquipmentSlotGroup slot) {
		if(!((BukkitMCServer) Static.getServer()).isPaper()) {
			// BODY is missing from Spigot, so this falls back to ARMOR just like EquipmentSlot.BODY
			if(slot == MCEquipmentSlotGroup.BODY) {
				slot = MCEquipmentSlotGroup.ARMOR;
			}
		}
		AttributeModifier mod = new AttributeModifier((NamespacedKey) key.getHandle(), amt,
				BukkitMCAttributeModifier.Operation.getConvertor().getConcreteEnum(op),
				EquipmentSlotGroup.getByName(slot.name()));
		return new BukkitMCAttributeModifier((Attribute) attr.getConcrete(), mod);
	}

	@Override
	public MCMetadataValue GetMetadataValue(Object value, MCPlugin plugin) {
		return new BukkitMCMetadataValue(new FixedMetadataValue(((BukkitMCPlugin) plugin).getHandle(), value));
	}

	public static final BukkitBlockListener BLOCK_LISTENER = new BukkitBlockListener();
	public static final BukkitEntityListener ENTITY_LISTENER = new BukkitEntityListener();
	public static final BukkitInventoryListener INVENTORY_LISTENER = new BukkitInventoryListener();
	public static final BukkitPlayerListener PLAYER_LISTENER = new BukkitPlayerListener();
	public static final BukkitServerListener SERVER_LISTENER = new BukkitServerListener();
	public static final BukkitVehicleListener VEHICLE_LISTENER = new BukkitVehicleListener();
	public static final BukkitWeatherListener WEATHER_LISTENER = new BukkitWeatherListener();
	public static final BukkitWorldListener WORLD_LISTENER = new BukkitWorldListener();

	@Override
	public void Startup(CommandHelperPlugin chp) {
		chp.registerEvents(BLOCK_LISTENER);
		chp.registerEvents(ENTITY_LISTENER);
		chp.registerEventsDynamic(INVENTORY_LISTENER);
		chp.registerEventsDynamic(PLAYER_LISTENER);
		chp.registerEvents(SERVER_LISTENER);
		chp.registerEvents(VEHICLE_LISTENER);
		chp.registerEvents(WEATHER_LISTENER);
		chp.registerEvents(WORLD_LISTENER);
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
		} catch (CancellationException ex) {
			// Ignore the Exception when the plugin is disabled (server shutting down).
			if(CommandHelperPlugin.self.isEnabled()) {
				java.util.logging.Logger.getLogger(BukkitConvertor.class.getName()).log(Level.SEVERE, null, ex);
			}
		} catch (InterruptedException | ExecutionException ex) {
			java.util.logging.Logger.getLogger(BukkitConvertor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

//	/**
//	 * We don't want to allow scripts to clear other plugin's tasks
//	 * on accident, so only ids registered through our interface
//	 * can also be cancelled.
//	 */
//	private static final Set<Integer> validIDs = new TreeSet<Integer>();
//
//	@Override
//	public synchronized int SetFutureRunnable(DaemonManager dm, long ms, Runnable r) {
//		int id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(CommandHelperPlugin.self, r, Static.msToTicks(ms));
//		validIDs.add(id);
//		return id;
//	}
//
//	@Override
//	public synchronized int SetFutureRepeater(DaemonManager dm, long ms, long initialDelay, Runnable r){
//		int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CommandHelperPlugin.self, r, Static.msToTicks(initialDelay), Static.msToTicks(ms));
//		validIDs.add(id);
//		return id;
//	}
//
//	@Override
//	public synchronized void ClearAllRunnables() {
//		//Doing cancelTasks apparently does not work, so let's just manually cancel each task, which does appear to work.
//		//Anyways, it's better that way anyhow, because we actually remove IDs from validIDs that way.
//		//((BukkitMCServer)Static.getServer()).__Server().getScheduler().cancelTasks(CommandHelperPlugin.self);
//		Set<Integer> ids = new TreeSet<Integer>(validIDs);
//		for(int id : ids){
//			try {
//				//If this doesn't work, it shouldn't kill everything.
//				ClearFutureRunnable(id);
//			} catch (Exception e){
//				Logger.getLogger(BukkitConvertor.class.getName()).log(null, Level.SEVERE, e);
//			}
//		}
//	}
//
//	@Override
//	public void ClearFutureRunnable(int id) {
//		if(validIDs.contains(id)){
//			Bukkit.getServer().getScheduler().cancelTask(id);
//			validIDs.remove(id);
//		}
//	}
	public static MCEntity BukkitGetCorrectEntity(Entity be) {
		if(be == null) {
			return null;
		}

		BukkitMCEntityType type = BukkitMCEntityType.valueOfConcrete(be.getType());
		if(type.getWrapperClass() != null) {
			return ReflectionUtils.newInstance(type.getWrapperClass(), new Class[]{Entity.class}, new Object[]{be});
		}

		if(be instanceof Hanging) {
			type.setWrapperClass(BukkitMCHanging.class);
			return new BukkitMCHanging(be);
		}

		if(be instanceof Minecart) {
			// Must come before Vehicle
			type.setWrapperClass(BukkitMCMinecart.class);
			return new BukkitMCMinecart(be);
		}

		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_3)) {
			// boats were split into different classes by wood type
			if(be instanceof ChestBoat) {
				// Must come before Boat
				type.setWrapperClass(BukkitMCChestBoat.class);
				return new BukkitMCChestBoat(be);
			}

			if(be instanceof Boat) {
				// Must come before Vehicle
				type.setWrapperClass(BukkitMCBoat.class);
				return new BukkitMCBoat(be);
			}
		}

		if(be instanceof SizedFireball) {
			// Must come before Fireball
			type.setWrapperClass(BukkitMCSizedFireball.class);
			return new BukkitMCSizedFireball(be);
		}

		if(be instanceof Fireball) {
			// Must come before Projectile
			type.setWrapperClass(BukkitMCFireball.class);
			return new BukkitMCFireball(be);
		}

		if(be instanceof ThrowableProjectile) {
			// Must come before Projectile
			type.setWrapperClass(BukkitMCItemProjectile.class);
			return new BukkitMCItemProjectile(be);
		}

		if(be instanceof Projectile) {
			type.setWrapperClass(BukkitMCProjectile.class);
			return new BukkitMCProjectile(be);
		}

		if(be instanceof Tameable) {
			// Must come before Ageable
			type.setWrapperClass(BukkitMCTameable.class);
			return new BukkitMCTameable(be);
		}

		if(be instanceof Animals) {
			// Must come before Ageable
			type.setWrapperClass(BukkitMCAnimal.class);
			return new BukkitMCAnimal(be);
		}

		if(be instanceof Breedable) {
			// Must come before Ageable
			type.setWrapperClass(BukkitMCBreedable.class);
			return new BukkitMCBreedable(be);
		}

		if(be instanceof Ageable) {
			// Must come before LivingEntity
			type.setWrapperClass(BukkitMCAgeable.class);
			return new BukkitMCAgeable(be);
		}

		if(be instanceof HumanEntity) {
			// Must come before LivingEntity
			type.setWrapperClass(BukkitMCHumanEntity.class);
			return new BukkitMCHumanEntity(be);
		}

		if(be instanceof ComplexEntityPart) {
			type.setWrapperClass(BukkitMCComplexEntityPart.class);
			return new BukkitMCComplexEntityPart(be);
		}

		if(be instanceof ComplexLivingEntity) {
			// Must come before LivingEntity
			type.setWrapperClass(BukkitMCComplexLivingEntity.class);
			return new BukkitMCComplexLivingEntity(be);
		}

		if(be instanceof LivingEntity) {
			type.setWrapperClass(BukkitMCLivingEntity.class);
			return new BukkitMCLivingEntity(be);
		}

		if(be instanceof Vehicle) {
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
			MSLog.GetLogger().Log(MSLog.Tags.RUNTIME, LogLevel.INFO, iae.getMessage(), Target.UNKNOWN);
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
		if(location == null) {
			return Collections.EMPTY_LIST;
		}
		if(radius <= 0) {
			radius = 1;
		}
		Location l = (Location) location.getHandle();
		Collection<Entity> near = l.getWorld().getNearbyEntities(l, radius, radius, radius);
		List<MCEntity> entities = new ArrayList<>();
		for(Entity e : near) {
			entities.add(BukkitGetCorrectEntity(e));
		}
		return entities;
	}

	public static MCBlockState BukkitGetCorrectBlockState(BlockState bs) {
		if(bs instanceof Container) {
			// This code block should only contain checks for blockstates that implement Container.
			if(bs instanceof Chest) {
				return new BukkitMCChest((Chest) bs);
			}
			if(bs instanceof BrewingStand) {
				return new BukkitMCBrewingStand((BrewingStand) bs);
			}
			if(bs instanceof Dispenser) {
				return new BukkitMCDispenser((Dispenser) bs);
			}
			if(bs instanceof Dropper) {
				return new BukkitMCDropper((Dropper) bs);
			}
			if(bs instanceof Furnace) {
				return new BukkitMCFurnace((Furnace) bs);
			}
			return new BukkitMCContainer((Container) bs);
		}
		if(bs instanceof Banner) {
			return new BukkitMCBanner((Banner) bs);
		}
		if(bs instanceof CreatureSpawner) {
			return new BukkitMCCreatureSpawner((CreatureSpawner) bs);
		}
		if(bs instanceof Beacon) {
			return new BukkitMCBeacon((Beacon) bs);
		}
		if(bs instanceof Skull) {
			return new BukkitMCSkull((Skull) bs);
		}
		if(bs instanceof Lectern) {
			return new BukkitMCLectern((Lectern) bs);
		}
		if(bs instanceof Beehive) {
			return new BukkitMCBeehive((Beehive) bs);
		}
		if(bs instanceof Sign) {
			return new BukkitMCSign((Sign) bs);
		}
		if(bs instanceof CommandBlock) {
			return new BukkitMCCommandBlock((CommandBlock) bs);
		}
		if(bs instanceof EndGateway) {
			return new BukkitMCEndGateway((EndGateway) bs);
		}
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_1) && bs instanceof DecoratedPot) {
			return new BukkitMCDecoratedPot((DecoratedPot) bs);
		}
		return new BukkitMCBlockState(bs);
	}

	public static MCItemMeta BukkitGetCorrectMeta(ItemMeta im) {
		if(im instanceof BlockStateMeta) {
			return new BukkitMCBlockStateMeta((BlockStateMeta) im);
		}
		if(im instanceof BannerMeta) {
			return new BukkitMCBannerMeta((BannerMeta) im);
		}
		if(im instanceof BookMeta) {
			return new BukkitMCBookMeta((BookMeta) im);
		}
		if(im instanceof EnchantmentStorageMeta) {
			return new BukkitMCEnchantmentStorageMeta((EnchantmentStorageMeta) im);
		}
		if(im instanceof FireworkEffectMeta) {
			return new BukkitMCFireworkEffectMeta((FireworkEffectMeta) im);
		}
		if(im instanceof FireworkMeta) {
			return new BukkitMCFireworkMeta((FireworkMeta) im);
		}
		if(im instanceof PotionMeta) {
			return new BukkitMCPotionMeta((PotionMeta) im);
		}
		if(im instanceof SkullMeta) {
			return new BukkitMCSkullMeta((SkullMeta) im);
		}
		if(im instanceof MapMeta) {
			return new BukkitMCMapMeta((MapMeta) im);
		}
		if(im instanceof TropicalFishBucketMeta) {
			return new BukkitMCTropicalFishBucketMeta((TropicalFishBucketMeta) im);
		}
		if(im instanceof CrossbowMeta) {
			return new BukkitMCCrossbowMeta((CrossbowMeta) im);
		}
		if(im instanceof CompassMeta) {
			return new BukkitMCCompassMeta((CompassMeta) im);
		}
		if(im instanceof SuspiciousStewMeta) {
			return new BukkitMCSuspiciousStewMeta((SuspiciousStewMeta) im);
		}
		if(im instanceof KnowledgeBookMeta) {
			return new BukkitMCKnowledgeBookMeta((KnowledgeBookMeta) im);
		}
		MCVersion version = Static.getServer().getMinecraftVersion();
		if(version.gte(MCVersion.MC1_17)) {
			if(im instanceof BundleMeta) {
				return new BukkitMCBundleMeta((BundleMeta) im);
			}
			if(version.gte(MCVersion.MC1_17_X)) {
				if(im instanceof AxolotlBucketMeta) {
					return new BukkitMCAxolotlBucketMeta((AxolotlBucketMeta) im);
				}
				if(version.gte(MCVersion.MC1_19_3)) {
					if(im instanceof MusicInstrumentMeta) {
						return new BukkitMCMusicInstrumentMeta((MusicInstrumentMeta) im);
					}
					if(version.gte(MCVersion.MC1_20)) {
						if(im instanceof ColorableArmorMeta) { // Must be before ArmorMeta and LeatherArmorMeta
							return new BukkitMCColorableArmorMeta((ColorableArmorMeta) im);
						}
						if(im instanceof ArmorMeta) {
							return new BukkitMCArmorMeta((ArmorMeta) im);
						}
						if(version.gte(MCVersion.MC1_20_6)) {
							if(im instanceof OminousBottleMeta) {
								return new BukkitMCOminousBottleMeta((OminousBottleMeta) im);
							}
						}
					}
				}
			}
		}
		if(im instanceof LeatherArmorMeta) { // Must be after ColorableArmorMeta
			return new BukkitMCLeatherArmorMeta((LeatherArmorMeta) im);
		}
		return new BukkitMCItemMeta(im);
	}

	@Override
	public MCInventory GetEntityInventory(MCEntity e) {
		Entity entity = ((BukkitMCEntity) e).getHandle();
		if(entity instanceof InventoryHolder) {
			if(entity instanceof Player p) {
				return new BukkitMCPlayerInventory(p.getInventory());
			}
			return new BukkitMCInventory(((InventoryHolder) entity).getInventory());
		}
		return null;
	}

	@Override
	public MCInventory GetLocationInventory(MCLocation location) {
		BlockState bs = ((Location) location.getHandle()).getBlock().getState();
		if(bs instanceof InventoryHolder) {
			return new BukkitMCInventory(((InventoryHolder) bs).getInventory());
		}
		return null;
	}

	@Override
	public MCInventoryHolder CreateInventoryHolder(String id, String title) {
		return new BukkitMCVirtualInventoryHolder(id, title);
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

		if(Bukkit.isPrimaryThread()) {
			try {
				return callable.call();
			} catch(Exception e) {
				throw new ExecutionException(e);
			}
		} else {
			return Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, callable).get();
		}
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
	public MCColor GetColor(int red, int green, int blue, int alpha) {
		return BukkitMCColor.GetMCColor(Color.fromARGB(alpha, red, green, blue));
	}

	@Override
	public MCColor GetColor(String colorName, Target t) throws CREFormatException {
		return ConvertorHelper.GetColor(colorName, t);
	}

	@Override
	public MCPattern GetPattern(MCDyeColor color, MCPatternShape shape) {
		return new BukkitMCPattern(new Pattern(BukkitMCDyeColor.getConvertor().getConcreteEnum(color),
				(PatternType) shape.getConcrete()));
	}

	@Override
	public MCFireworkBuilder GetFireworkBuilder() {
		return new BukkitMCFireworkBuilder();
	}

	@Override
	public MCPluginMeta GetPluginMeta() {
		if(pluginMeta == null) {
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
	public MCRecipe GetNewRecipe(String key, MCRecipeType type, MCItemStack result) {

		ItemStack is = ((BukkitMCItemStack) result).asItemStack();
		if(type == MCRecipeType.MERCHANT) {
			return new BukkitMCMerchantRecipe(new MerchantRecipe(is, Integer.MAX_VALUE));
		}
		NamespacedKey nskey = new NamespacedKey(CommandHelperPlugin.self, key);
		try {
			switch(type) {
				case BLASTING:
					return new BukkitMCCookingRecipe(new BlastingRecipe(nskey, is, Material.STRUCTURE_VOID, 0.0F, 100), type);
				case CAMPFIRE:
					return new BukkitMCCookingRecipe(new CampfireRecipe(nskey, is, Material.STRUCTURE_VOID, 0.0F, 100), type);
				case FURNACE:
					return new BukkitMCCookingRecipe(new FurnaceRecipe(nskey, is, Material.STRUCTURE_VOID, 0.0F, 200), type);
				case SHAPED:
					return new BukkitMCShapedRecipe(new ShapedRecipe(nskey, is));
				case SHAPELESS:
					return new BukkitMCShapelessRecipe(new ShapelessRecipe(nskey, is));
				case SMOKING:
					return new BukkitMCCookingRecipe(new SmokingRecipe(nskey, is, Material.STRUCTURE_VOID, 0.0F, 200), type);
				case STONECUTTING:
					return new BukkitMCStonecuttingRecipe(new StonecuttingRecipe(nskey, is, Material.STRUCTURE_VOID));
				case SMITHING:
				case COMPLEX:
					throw new IllegalArgumentException("Unable to generate recipe type: " + type.name());
			}
		} catch (NoClassDefFoundError ex) {
			// doesn't exist on this version.
			// eg. 1.14 recipe type on 1.13
		}
		throw new IllegalArgumentException("Server version does not support this recipe type: " + type.name());
	}

	@Override
	public MCRecipe GetRecipe(MCRecipe unspecific) {
		Recipe r = (Recipe) unspecific.getHandle();
		return BukkitGetRecipe(r);
	}

	public static MCRecipe BukkitGetRecipe(Recipe r) {
		if(r instanceof BlastingRecipe) {
			return new BukkitMCCookingRecipe(r, MCRecipeType.BLASTING);
		} else if(r instanceof CampfireRecipe) {
			return new BukkitMCCookingRecipe(r, MCRecipeType.CAMPFIRE);
		} else if(r instanceof SmokingRecipe) {
			return new BukkitMCCookingRecipe(r, MCRecipeType.SMOKING);
		} else if(r instanceof FurnaceRecipe) {
			return new BukkitMCCookingRecipe(r, MCRecipeType.FURNACE);
		} else if(r instanceof StonecuttingRecipe) {
			return new BukkitMCStonecuttingRecipe((StonecuttingRecipe) r);
		} else if(r instanceof ComplexRecipe) {
			return new BukkitMCComplexRecipe(r);
		} else if(r instanceof SmithingRecipe) {
			return new BukkitMCSmithingRecipe((SmithingRecipe) r);
		} else if(r instanceof ShapelessRecipe) {
			return new BukkitMCShapelessRecipe((ShapelessRecipe) r);
		} else if(r instanceof ShapedRecipe) {
			return new BukkitMCShapedRecipe((ShapedRecipe) r);
		} else if(r instanceof MerchantRecipe) {
			return new BukkitMCMerchantRecipe((MerchantRecipe) r);
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
		if(unspecific == null) {
			return null;
		}
		return BukkitGetCorrectSender(((BukkitMCCommandSender) unspecific)._CommandSender());
	}

	public static MCCommandSender BukkitGetCorrectSender(CommandSender sender) {
		if(sender instanceof Player player) {
			return new BukkitMCPlayer(player);
		} else if(sender instanceof ConsoleCommandSender consoleCommandSender) {
			return new BukkitMCConsoleCommandSender(consoleCommandSender);
		} else if(sender instanceof RemoteConsoleCommandSender remoteConsoleCommandSender) {
			return new BukkitMCRemoteConsoleCommandSender(remoteConsoleCommandSender);
		} else if(sender instanceof BlockCommandSender blockCommandSender) {
			return new BukkitMCBlockCommandSender(blockCommandSender);
		} else if(sender instanceof CommandMinecart commandMinecart) {
			return new BukkitMCCommandMinecart(commandMinecart);
		} else {
			return new BukkitMCCommandSender(sender);
		}
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
	public String GetUser(Environment env) {
		MCCommandSender cs = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
		if(cs == null) {
			return null;
		} else {
			String name = cs.getName();
			if("CONSOLE".equals(name)) {
				name = "~console";
			}
			return name;
		}
	}

	@Override
	public MCNamespacedKey GetNamespacedKey(String key) {
		return new BukkitMCNamespacedKey(NamespacedKey.fromString(key, CommandHelperPlugin.self));
	}

	@Override
	public MCTransformation GetTransformation(Quaternionf leftRotation, Quaternionf rightRotation, Vector3f scale, Vector3f translation) {
		return new BukkitMCTransformation(new Transformation(translation, leftRotation, scale, rightRotation));
	}

	@Override
	public boolean IsMainThread() {
		return Bukkit.isPrimaryThread();
	}

}
