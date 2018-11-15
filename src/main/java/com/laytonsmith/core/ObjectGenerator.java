package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCBannerMeta;
import com.laytonsmith.abstraction.MCBlockStateMeta;
import com.laytonsmith.abstraction.MCBookMeta;
import com.laytonsmith.abstraction.MCBrewerInventory;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCCreatureSpawner;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCEnchantmentStorageMeta;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.MCFireworkEffectMeta;
import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.MCFurnaceInventory;
import com.laytonsmith.abstraction.MCFurnaceRecipe;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemFactory;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLeatherArmorMeta;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMapMeta;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCPotionMeta;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCShapedRecipe;
import com.laytonsmith.abstraction.MCShapelessRecipe;
import com.laytonsmith.abstraction.MCSkullMeta;
import com.laytonsmith.abstraction.MCTropicalFishBucketMeta;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.blocks.MCBanner;
import com.laytonsmith.abstraction.blocks.MCBrewingStand;
import com.laytonsmith.abstraction.blocks.MCChest;
import com.laytonsmith.abstraction.blocks.MCDispenser;
import com.laytonsmith.abstraction.blocks.MCDropper;
import com.laytonsmith.abstraction.blocks.MCFurnace;
import com.laytonsmith.abstraction.blocks.MCHopper;
import com.laytonsmith.abstraction.blocks.MCShulkerBox;
import com.laytonsmith.abstraction.entities.MCTropicalFish;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.MCItemFlag;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREEnchantmentException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This file is responsible for converting CH objects into server objects, and vice versa
 */
public class ObjectGenerator {

	private static ObjectGenerator pog = null;

	public static ObjectGenerator GetGenerator() {
		if(pog == null) {
			pog = new ObjectGenerator();
		}
		return pog;
	}

	/**
	 * Gets a Location Object, given a MCLocation
	 *
	 * @param l
	 * @return
	 */
	public CArray location(MCLocation l) {
		return location(l, true);
	}

	/**
	 * Gets a Location Object, optionally with yaw and pitch, given a MCLocation
	 *
	 * @param l
	 * @param includeYawAndPitch
	 * @return
	 */
	public CArray location(MCLocation l, boolean includeYawAndPitch) {
		CArray ca = CArray.GetAssociativeArray(Target.UNKNOWN);
		Construct x = new CDouble(l.getX(), Target.UNKNOWN);
		Construct y = new CDouble(l.getY(), Target.UNKNOWN);
		Construct z = new CDouble(l.getZ(), Target.UNKNOWN);
		Construct world = new CString(l.getWorld().getName(), Target.UNKNOWN);
		ca.set("0", x, Target.UNKNOWN);
		ca.set("1", y, Target.UNKNOWN);
		ca.set("2", z, Target.UNKNOWN);
		ca.set("3", world, Target.UNKNOWN);
		ca.set("x", x, Target.UNKNOWN);
		ca.set("y", y, Target.UNKNOWN);
		ca.set("z", z, Target.UNKNOWN);
		ca.set("world", world, Target.UNKNOWN);
		if(includeYawAndPitch) {
			// guarantee yaw in the 0 - 359.9~ range
			float yawRaw = l.getYaw() % 360.0f;
			if(yawRaw < 0.0f) {
				yawRaw += 360.0f;
			}
			Construct yaw = new CDouble(yawRaw, Target.UNKNOWN);
			Construct pitch = new CDouble(l.getPitch(), Target.UNKNOWN);
			ca.set("4", yaw, Target.UNKNOWN);
			ca.set("5", pitch, Target.UNKNOWN);
			ca.set("yaw", yaw, Target.UNKNOWN);
			ca.set("pitch", pitch, Target.UNKNOWN);
		}
		return ca;
	}

	/**
	 * Given a Location Object, returns a MCLocation. If the optional world is not specified in the object, the world
	 * provided is used instead. Location "objects" are MethodScript arrays that represent a location in game. There are
	 * 4 usages: <ul> <li>(x, y, z)</li> <li>(x, y, z, world)</li> <li>(x, y, z, yaw, pitch)</li> <li>(x, y, z, world,
	 * yaw, pitch)</li> </ul> In all cases, the pitch and yaw default to 0, and the world defaults to the specified
	 * world. <em>More conveniently: ([world], x, y, z, [yaw, pitch])</em>
	 */
	public MCLocation location(Mixed c, MCWorld w, Target t) {
		if(!(c instanceof CArray)) {
			throw new CREFormatException("Expecting an array, received " + c.typeof().getSimpleName(), t);
		}
		CArray array = (CArray) c;
		MCWorld world = w;
		double x = 0;
		double y = 0;
		double z = 0;
		float yaw = 0;
		float pitch = 0;
		if(!array.inAssociativeMode()) {
			if(array.size() == 3) {
				//Just the xyz, with default yaw and pitch, and given world
				x = Static.getNumber(array.get(0, t), t);
				y = Static.getNumber(array.get(1, t), t);
				z = Static.getNumber(array.get(2, t), t);
			} else if(array.size() == 4) {
				//x, y, z, world
				x = Static.getNumber(array.get(0, t), t);
				y = Static.getNumber(array.get(1, t), t);
				z = Static.getNumber(array.get(2, t), t);
				world = Static.getServer().getWorld(array.get(3, t).val());
			} else if(array.size() == 5) {
				//x, y, z, yaw, pitch, with given world
				x = Static.getNumber(array.get(0, t), t);
				y = Static.getNumber(array.get(1, t), t);
				z = Static.getNumber(array.get(2, t), t);
				yaw = (float) Static.getNumber(array.get(3, t), t);
				pitch = (float) Static.getNumber(array.get(4, t), t);
			} else if(array.size() == 6) {
				//All have been given
				x = Static.getNumber(array.get(0, t), t);
				y = Static.getNumber(array.get(1, t), t);
				z = Static.getNumber(array.get(2, t), t);
				world = Static.getServer().getWorld(array.get(3, t).val());
				yaw = (float) Static.getNumber(array.get(4, t), t);
				pitch = (float) Static.getNumber(array.get(5, t), t);
			} else {
				throw new CREFormatException("Expecting a Location array, but the array did not meet the format specifications", t);
			}
		} else {
			if(array.containsKey("x")) {
				x = Static.getNumber(array.get("x", t), t);
			}
			if(array.containsKey("y")) {
				y = Static.getNumber(array.get("y", t), t);
			}
			if(array.containsKey("z")) {
				z = Static.getNumber(array.get("z", t), t);
			}
			if(array.containsKey("world")) {
				world = Static.getServer().getWorld(array.get("world", t).val());
			}
			if(array.containsKey("yaw")) {
				yaw = (float) Static.getDouble(array.get("yaw", t), t);
			}
			if(array.containsKey("pitch")) {
				pitch = (float) Static.getDouble(array.get("pitch", t), t);
			}
		}
		//If world is still null at this point, it's an error
		if(world == null) {
			throw new CREInvalidWorldException("The specified world doesn't exist, or no world was provided", t);
		}
		return StaticLayer.GetLocation(world, x, y, z, yaw, pitch);
	}

	/**
	 * An Item Object consists of data about a particular item stack. Information included is: recipeType, data, qty,
	 * and an array of enchantment objects (labeled enchants): erecipeType (enchantment recipeType) and elevel
	 * (enchantment level). For backwards compatibility, this information is also listed in numerical slots as well as
	 * associative slots. If the MCItemStack is null, or the underlying item is nonexistant (or air) CNull is returned.
	 *
	 * @param is
	 * @return An item array or CNull
	 */
	public Construct item(MCItemStack is, Target t) {
		if(is == null || is.isEmpty()) {
			return CNull.NULL;
		}

		CArray ret = CArray.GetAssociativeArray(t);
		ret.set("name", new CString(is.getType().getName(), t), t);
		ret.set("qty", new CInt(is.getAmount(), t), t);
		ret.set("meta", itemMeta(is, t), t);
		return ret;
	}

	/**
	 * Gets an MCItemStack from a given item "object". Supports both the old and new formats currently
	 *
	 * @param i
	 * @param t
	 * @return An abstract item stack
	 */
	public MCItemStack item(Mixed i, Target t) {
		return item(i, t, false);
	}

	public MCItemStack item(Mixed i, Target t, boolean legacy) {
		if(i instanceof CNull) {
			return EmptyItem();
		}
		if(!(i instanceof CArray)) {
			throw new CREFormatException("Expected an array!", t);
		}
		CArray item = (CArray) i;
		String mat;
		MCItemStack ret;
		int data = 0;
		int qty = 1;

		if(item.containsKey("qty")) {
			qty = Static.getInt32(item.get("qty", t), t);
			if(qty <= 0) {
				return EmptyItem();
			}
		}

		legacy = legacy || item.containsKey("type") || item.containsKey("data");

		if(legacy) {
			// Do legacy item conversion
			if(item.containsKey("data")) {
				data = Static.getInt32(item.get("data", t), t);
			}
			MCMaterial material;
			if(item.containsKey("name")) {
				mat = item.get("name", t).val();
				if(mat.equals("MAP") || mat.equals("POTION")) {
					// special handling, ignore data here
					material = StaticLayer.GetMaterialFromLegacy(mat, 0);
				} else {
					material = StaticLayer.GetMaterialFromLegacy(mat, data);
				}
			} else {
				Mixed type = item.get("type", t);
				if(type instanceof CString) {
					int seperatorIndex = type.val().indexOf(':');
					if(seperatorIndex != -1) {
						try {
							data = Integer.parseInt(type.val().substring(seperatorIndex + 1));
						} catch (NumberFormatException e) {
							throw new CRERangeException("The item data \"" + type.val().substring(seperatorIndex + 1)
									+ "\" is not a valid integer.", t);
						}
						type = new CString(type.val().substring(0, seperatorIndex), t);
					}
				}
				mat = type.val();
				int id = Static.getInt32(type, t);
				if(id == 358 || id == 373) {
					// special map handling, ignore data here
					material = StaticLayer.GetMaterialFromLegacy(id, 0);
				} else {
					material = StaticLayer.GetMaterialFromLegacy(id, data);
				}
			}
			if(material == null || material.getName().equals("AIR")) {
				material = StaticLayer.GetMaterial(mat); // try modern material just in case
				if(material == null) {
					throw new CREFormatException("Could not find legacy item material from \"" + mat + "\""
							+ " with data \"" + data + "\"", t);
				}
			}

			// convert legacy meta to material
			if(material.getName().equals("PIG_SPAWN_EGG") && item.containsKey("meta")) {
				Mixed meta = item.get("meta", t);
				if(meta instanceof CArray && ((CArray) meta).containsKey("spawntype")) {
					Mixed spawntype = ((CArray) meta).get("spawntype", t);
					if(!(spawntype instanceof CNull)) {
						MCMaterial newmaterial;
						String entityName = spawntype.val().toUpperCase();
						if(entityName.equals("MUSHROOM_COW")) {
							newmaterial = StaticLayer.GetMaterial("MOOSHROOM_SPAWN_EGG");
						} else if(entityName.equals("PIG_ZOMBIE")) {
							newmaterial = StaticLayer.GetMaterial("ZOMBIE_PIGMAN_SPAWN_EGG");
						} else {
							newmaterial = StaticLayer.GetMaterial(entityName + "_SPAWN_EGG");
						}
						if(newmaterial != null) {
							material = newmaterial;
						}
					}
				}
			}

			ret = StaticLayer.GetItemStack(material, qty);
			CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "Converted \"" + mat + "\" with data \""
					+ data + "\" to " + material.getName(), t);

		} else {
			mat = item.get("name", t).val();
			ret = StaticLayer.GetItemStack(mat, qty);
		}

		if(ret == null) {
			throw new CREFormatException("Could not find item material from \"" + mat + "\"", t);
		}

		if(ret.isEmpty()) {
			return ret;
		}

		if(item.containsKey("meta")) {
			ret.setItemMeta(itemMeta(item.get("meta", t), ret.getType(), t));
		}

		if(legacy) {
			// convert legacy data to meta
			if(ret.getType().getName().equals("FILLED_MAP")) {
				MCMapMeta meta = (MCMapMeta) ret.getItemMeta();
				meta.setMapId(data);
				ret.setItemMeta(meta);
			} else if(data > 0 && ret.getType().getMaxDurability() > 0) {
				MCItemMeta meta = ret.getItemMeta();
				meta.setDamage(data);
				ret.setItemMeta(meta);
			}
		}

		// Deprecated fallback to enchants in item array if not in meta
		if(item.containsKey("enchants")) {
			try {
				Map<MCEnchantment, Integer> enchants = enchants((CArray) item.get("enchants", t), t);
				for(Map.Entry<MCEnchantment, Integer> entry : enchants.entrySet()) {
					ret.addUnsafeEnchantment(entry.getKey(), entry.getValue());
				}
			} catch (ClassCastException ex) {
				throw new CREFormatException("Enchants must be an array of enchantment arrays.", t);
			}
		}

		return ret;
	}

	private static MCItemStack EmptyItem() {
		return StaticLayer.GetItemStack("AIR", 1);
	}

	public Construct itemMeta(MCItemStack is, Target t) {
		if(!is.hasItemMeta()) {
			return CNull.NULL;
		} else {
			Construct display;
			Construct lore;
			CArray ma = CArray.GetAssociativeArray(t);
			MCItemMeta meta = is.getItemMeta();
			if(meta.hasDisplayName()) {
				display = new CString(meta.getDisplayName(), t);
			} else {
				display = CNull.NULL;
			}
			if(meta.hasLore()) {
				lore = new CArray(t);
				for(String l : meta.getLore()) {
					((CArray) lore).push(new CString(l, t), t);
				}
			} else {
				lore = CNull.NULL;
			}
			ma.set("display", display, t);
			ma.set("lore", lore, t);
			ma.set("enchants", enchants(meta.getEnchants(), t), t);
			ma.set("repair", new CInt(meta.getRepairCost(), t), t);

			Set<MCItemFlag> itemFlags = meta.getItemFlags();
			CArray flagArray = new CArray(t);
			if(itemFlags.size() > 0) {
				for(MCItemFlag flag : itemFlags) {
					flagArray.push(new CString(flag.name(), t), t);
				}
			}
			ma.set("flags", flagArray, t);

			if(is.getType().getMaxDurability() > 0) {
				ma.set("damage", new CInt(meta.getDamage(), t), t);
				ma.set("unbreakable", CBoolean.get(meta.isUnbreakable()), t);
			}

			// Specific ItemMeta
			if(meta instanceof MCBlockStateMeta) {
				MCBlockState bs = ((MCBlockStateMeta) meta).getBlockState();
				if(bs instanceof MCShulkerBox || bs instanceof MCChest
						|| bs instanceof MCDispenser || bs instanceof MCDropper || bs instanceof MCHopper) {
					// Handle InventoryHolders with inventory slots that do not have a special meaning.
					MCInventory inv = ((MCInventoryHolder) bs).getInventory();
					CArray box = CArray.GetAssociativeArray(t);
					for(int i = 0; i < inv.getSize(); i++) {
						Construct item = ObjectGenerator.GetGenerator().item(inv.getItem(i), t);
						if(!(item instanceof CNull)) {
							box.set(i, item, t);
						}
					}
					ma.set("inventory", box, t);
				} else if(bs instanceof MCBanner) {
					MCBanner banner = (MCBanner) bs;
					ma.set("basecolor", banner.getBaseColor().name(), t);
					CArray patterns = new CArray(t, banner.numberOfPatterns());
					for(MCPattern p : banner.getPatterns()) {
						CArray pattern = CArray.GetAssociativeArray(t);
						pattern.set("shape", new CString(p.getShape().toString(), t), t);
						pattern.set("color", new CString(p.getColor().toString(), t), t);
						patterns.push(pattern, t);
					}
					ma.set("patterns", patterns, t);
				} else if(bs instanceof MCCreatureSpawner) {
					MCCreatureSpawner mccs = (MCCreatureSpawner) bs;
					ma.set("spawntype", mccs.getSpawnedType().name());
					ma.set("delay", new CInt(mccs.getDelay(), t), t);
				} else if(bs instanceof MCBrewingStand) {
					MCBrewingStand brewStand = (MCBrewingStand) bs;
					ma.set("brewtime", new CInt(brewStand.getBrewingTime(), t), t);
					ma.set("fuel", new CInt(brewStand.getFuelLevel(), t), t);
					MCBrewerInventory inv = brewStand.getInventory();
					CArray invData = CArray.GetAssociativeArray(t);
					if(inv.getFuel().getAmount() != 0) {
						invData.set("fuel", ObjectGenerator.GetGenerator().item(inv.getFuel(), t), t);
					}
					if(inv.getIngredient().getAmount() != 0) {
						invData.set("ingredient", ObjectGenerator.GetGenerator().item(inv.getIngredient(), t), t);
					}
					if(inv.getLeftBottle().getAmount() != 0) {
						invData.set("leftbottle", ObjectGenerator.GetGenerator().item(inv.getLeftBottle(), t), t);
					}
					if(inv.getMiddleBottle().getAmount() != 0) {
						invData.set("middlebottle", ObjectGenerator.GetGenerator().item(inv.getMiddleBottle(), t), t);
					}
					if(inv.getRightBottle().getAmount() != 0) {
						invData.set("rightbottle", ObjectGenerator.GetGenerator().item(inv.getRightBottle(), t), t);
					}
					ma.set("inventory", invData, t);
				} else if(bs instanceof MCFurnace) {
					MCFurnace furnace = (MCFurnace) bs;
					ma.set("burntime", new CInt(furnace.getBurnTime(), t), t);
					ma.set("cooktime", new CInt(furnace.getCookTime(), t), t);
					MCFurnaceInventory inv = furnace.getInventory();
					CArray invData = CArray.GetAssociativeArray(t);
					if(inv.getResult().getAmount() != 0) {
						invData.set("result", ObjectGenerator.GetGenerator().item(inv.getResult(), t), t);
					}
					if(inv.getFuel().getAmount() != 0) {
						invData.set("fuel", ObjectGenerator.GetGenerator().item(inv.getFuel(), t), t);
					}
					if(inv.getSmelting().getAmount() != 0) {
						invData.set("smelting", ObjectGenerator.GetGenerator().item(inv.getSmelting(), t), t);
					}
					ma.set("inventory", invData, t);
				}
			} else if(meta instanceof MCFireworkEffectMeta) {
				MCFireworkEffectMeta mcfem = (MCFireworkEffectMeta) meta;
				MCFireworkEffect effect = mcfem.getEffect();
				if(effect == null) {
					ma.set("effect", CNull.NULL, t);
				} else {
					ma.set("effect", fireworkEffect(effect, t), t);
				}
			} else if(meta instanceof MCFireworkMeta) {
				MCFireworkMeta mcfm = (MCFireworkMeta) meta;
				CArray firework = CArray.GetAssociativeArray(t);
				firework.set("strength", new CInt(mcfm.getStrength(), t), t);
				CArray fe = new CArray(t);
				for(MCFireworkEffect effect : mcfm.getEffects()) {
					fe.push(fireworkEffect(effect, t), t);
				}
				firework.set("effects", fe, t);
				ma.set("firework", firework, t);
			} else if(meta instanceof MCLeatherArmorMeta) {
				CArray color = color(((MCLeatherArmorMeta) meta).getColor(), t);
				ma.set("color", color, t);
			} else if(meta instanceof MCBookMeta) {
				Construct title;
				Construct author;
				Construct pages;
				if(((MCBookMeta) meta).hasTitle()) {
					title = new CString(((MCBookMeta) meta).getTitle(), t);
				} else {
					title = CNull.NULL;
				}
				if(((MCBookMeta) meta).hasAuthor()) {
					author = new CString(((MCBookMeta) meta).getAuthor(), t);
				} else {
					author = CNull.NULL;
				}
				if(((MCBookMeta) meta).hasPages()) {
					pages = new CArray(t);
					for(String p : ((MCBookMeta) meta).getPages()) {
						((CArray) pages).push(new CString(p, t), t);
					}
				} else {
					pages = CNull.NULL;
				}
				ma.set("title", title, t);
				ma.set("author", author, t);
				ma.set("pages", pages, t);
			} else if(meta instanceof MCSkullMeta) {
				if(((MCSkullMeta) meta).hasOwner()) {
					ma.set("owner", new CString(((MCSkullMeta) meta).getOwner(), t), t);
				} else {
					ma.set("owner", CNull.NULL, t);
				}
			} else if(meta instanceof MCEnchantmentStorageMeta) {
				Construct stored;
				if(((MCEnchantmentStorageMeta) meta).hasStoredEnchants()) {
					stored = enchants(((MCEnchantmentStorageMeta) meta).getStoredEnchants(), t);
				} else {
					stored = CNull.NULL;
				}
				ma.set("stored", stored, t);
			} else if(meta instanceof MCPotionMeta) {
				MCPotionMeta potionmeta = (MCPotionMeta) meta;
				CArray effects = potions(potionmeta.getCustomEffects(), t);
				ma.set("potions", effects, t);
				MCPotionData potiondata = potionmeta.getBasePotionData();
				if(potiondata != null) {
					ma.set("base", potionData(potiondata, t), t);
				}
			} else if(meta instanceof MCBannerMeta) {
				MCBannerMeta bannermeta = (MCBannerMeta) meta;
				CArray patterns = new CArray(t, bannermeta.numberOfPatterns());
				for(MCPattern p : bannermeta.getPatterns()) {
					CArray pattern = CArray.GetAssociativeArray(t);
					pattern.set("shape", new CString(p.getShape().toString(), t), t);
					pattern.set("color", new CString(p.getColor().toString(), t), t);
					patterns.push(pattern, t);
				}
				ma.set("patterns", patterns, t);
			} else if(meta instanceof MCMapMeta) {
				MCMapMeta mm = ((MCMapMeta) meta);
				MCColor mapcolor = mm.getColor();
				if(mapcolor == null) {
					ma.set("color", CNull.NULL, t);
				} else {
					ma.set("color", color(mapcolor, t), t);
				}
				if(mm.hasMapId()) {
					ma.set("mapid", new CInt(mm.getMapId(), t), t);
				} else {
					ma.set("mapid", CNull.NULL, t);
				}
			} else if(meta instanceof MCTropicalFishBucketMeta) {
				MCTropicalFishBucketMeta fm = (MCTropicalFishBucketMeta) meta;
				if(fm.hasVariant()) {
					ma.set("fishcolor", new CString(fm.getBodyColor().name(), t), t);
					ma.set("fishpatterncolor", new CString(fm.getPatternColor().name(), t), t);
					ma.set("fishpattern", new CString(fm.getPattern().name(), t), t);
				} else {
					ma.set("fishcolor", CNull.NULL, t);
					ma.set("fishpatterncolor", CNull.NULL, t);
					ma.set("fishpattern", CNull.NULL, t);
				}
			}
			return ma;
		}
	}

	public MCItemMeta itemMeta(Mixed c, MCMaterial mat, Target t) throws ConfigRuntimeException {
		MCItemFactory itemFactory = Static.getServer().getItemFactory();
		if(itemFactory == null) {
			throw new CRENotFoundException("Could not find the internal MCItemFactory object (are you running in cmdline mode?)", t);
		}
		MCItemMeta meta = itemFactory.getItemMeta(mat);
		if(c instanceof CNull) {
			return meta;
		}
		CArray ma;
		if(c instanceof CArray) {
			ma = (CArray) c;
			try {
				if(ma.containsKey("display")) {
					Mixed dni = ma.get("display", t);
					if(!(dni instanceof CNull)) {
						meta.setDisplayName(dni.val());
					}
				}
				if(ma.containsKey("lore")) {
					Mixed li = ma.get("lore", t);
					if(li instanceof CNull) {
						//do nothing
					} else if(li instanceof CString) {
						List<String> ll = new ArrayList<>();
						ll.add(li.val());
						meta.setLore(ll);
					} else if(li instanceof CArray) {
						CArray la = (CArray) li;
						List<String> ll = new ArrayList<>();
						for(int j = 0; j < la.size(); j++) {
							ll.add(la.get(j, t).val());
						}
						meta.setLore(ll);
					} else {
						throw new CREFormatException("Lore was expected to be an array or a string.", t);
					}
				}
				if(ma.containsKey("enchants")) {
					Mixed enchants = ma.get("enchants", t);
					if(enchants instanceof CArray) {
						for(Map.Entry<MCEnchantment, Integer> ench : enchants((CArray) enchants, t).entrySet()) {
							meta.addEnchant(ench.getKey(), ench.getValue(), true);
						}
					} else {
						throw new CREFormatException("Enchants field was expected to be an array of Enchantment arrays", t);
					}
				}
				if(ma.containsKey("repair") && !(ma.get("repair", t) instanceof CNull)) {
					meta.setRepairCost(Static.getInt32(ma.get("repair", t), t));
				}
				if(ma.containsKey("flags")) {
					Mixed flags = ma.get("flags", t);
					if(flags instanceof CArray) {
						CArray flagArray = (CArray) flags;
						for(int i = 0; i < flagArray.size(); i++) {
							Mixed flag = flagArray.get(i, t);
							meta.addItemFlags(MCItemFlag.valueOf(flag.val().toUpperCase()));
						}
					} else {
						throw new CREFormatException("Itemflags was expected to be an array of flags.", t);
					}
				}

				if(mat.getMaxDurability() > 0) {
					if(ma.containsKey("damage")) {
						meta.setDamage(Static.getInt32(ma.get("damage", t), t));
					}
					if(ma.containsKey("unbreakable")) {
						meta.setUnbreakable(Static.getBoolean(ma.get("unbreakable", t), t));
					}
				}

				// Specific ItemMeta
				if(meta instanceof MCBlockStateMeta) {
					MCBlockStateMeta bsm = (MCBlockStateMeta) meta;
					MCBlockState bs = bsm.getBlockState();
					if(bs instanceof MCShulkerBox || bs instanceof MCChest
							|| bs instanceof MCDispenser || bs instanceof MCDropper || bs instanceof MCHopper) {
						if(ma.containsKey("inventory")) {
							MCInventory inv = ((MCInventoryHolder) bs).getInventory();
							Mixed cInvRaw = ma.get("inventory", t);
							if(cInvRaw instanceof CArray) {
								CArray cinv = (CArray) cInvRaw;
								for(String key : cinv.stringKeySet()) {
									try {
										int index = Integer.parseInt(key);
										if(index < 0 || index >= inv.getSize()) {
											ConfigRuntimeException.DoWarning("Out of range value (" + index + ") found"
													+ " in " + bs.getClass().getSimpleName().replaceFirst("MC", "")
													+ " inventory array, so ignoring.");
										}
										MCItemStack is = ObjectGenerator.GetGenerator().item(cinv.get(key, t), t);
										inv.setItem(index, is);
									} catch (NumberFormatException ex) {
										ConfigRuntimeException.DoWarning("Expecting integer value for key in "
												+ bs.getClass().getSimpleName().replaceFirst("MC", "")
												+ " inventory array, but \"" + key + "\" was found. Ignoring.");
									}
								}
								bsm.setBlockState(bs);
							} else if(!(cInvRaw instanceof CNull)) {
								throw new CREFormatException(bs.getClass().getSimpleName().replaceFirst("MC", "")
										+ " inventory expected to be an array or null.", t);
							}
						}
					} else if(bs instanceof MCBanner) {
						MCBanner banner = (MCBanner) bs;
						if(ma.containsKey("basecolor")) {
							String baseString = ma.get("basecolor", t).val().toUpperCase();
							try {
								MCDyeColor base = MCDyeColor.valueOf(baseString);
								banner.setBaseColor(base);
							} catch (IllegalArgumentException ex) {
								if(baseString.equals("SILVER")) {
									// convert old DyeColor
									banner.setBaseColor(MCDyeColor.LIGHT_GRAY);
								} else {
									throw ex;
								}
							}
						} else {
							banner.setBaseColor(MCDyeColor.WHITE);
						}
						if(ma.containsKey("patterns")) {
							CArray array = ArgumentValidation.getArray(ma.get("patterns", t), t);
							for(String key : array.stringKeySet()) {
								CArray pattern = ArgumentValidation.getArray(array.get(key, t), t);
								MCPatternShape shape = MCPatternShape.valueOf(pattern.get("shape", t).val().toUpperCase());
								String color = pattern.get("color", t).val().toUpperCase();
								try {
									MCDyeColor dyecolor = MCDyeColor.valueOf(color);
									banner.addPattern(StaticLayer.GetConvertor().GetPattern(dyecolor, shape));
								} catch (IllegalArgumentException ex) {
									if(color.equals("SILVER")) {
										// convert old DyeColor
										banner.addPattern(StaticLayer.GetConvertor().GetPattern(MCDyeColor.LIGHT_GRAY, shape));
									} else {
										throw ex;
									}
								}
							}
						}
						bsm.setBlockState(banner);
					} else if(bs instanceof MCCreatureSpawner) {
						MCCreatureSpawner mccs = (MCCreatureSpawner) bs;
						if(ma.containsKey("spawntype")) {
							MCEntityType type = MCEntityType.valueOf(ma.get("spawntype", t).val().toUpperCase());
							mccs.setSpawnedType(type);
						}
						if(ma.containsKey("delay")) {
							int delay = Static.getInt32(ma.get("delay", t), t);
							mccs.setDelay(delay);
						}
						bsm.setBlockState(bs);
					} else if(bs instanceof MCBrewingStand) {
						MCBrewingStand brewStand = (MCBrewingStand) bs;
						if(ma.containsKey("brewtime")) {
							brewStand.setBrewingTime(ArgumentValidation.getInt32(ma.get("brewtime", t), t));
						}
						if(ma.containsKey("fuel")) {
							brewStand.setFuelLevel(ArgumentValidation.getInt32(ma.get("fuel", t), t));
						}
						if(ma.containsKey("inventory")) {
							CArray invData = ArgumentValidation.getArray(ma.get("inventory", t), t);
							MCBrewerInventory inv = brewStand.getInventory();
							if(invData.containsKey("fuel")) {
								inv.setFuel(ObjectGenerator.GetGenerator().item(invData.get("fuel", t), t));
							}
							if(invData.containsKey("ingredient")) {
								inv.setIngredient(ObjectGenerator.GetGenerator().item(invData.get("ingredient", t), t));
							}
							if(invData.containsKey("leftbottle")) {
								inv.setLeftBottle(ObjectGenerator.GetGenerator().item(invData.get("leftbottle", t), t));
							}
							if(invData.containsKey("middlebottle")) {
								inv.setMiddleBottle(ObjectGenerator.GetGenerator().item(invData.get("middlebottle", t), t));
							}
							if(invData.containsKey("rightbottle")) {
								inv.setRightBottle(ObjectGenerator.GetGenerator().item(invData.get("rightbottle", t), t));
							}
						}
						bsm.setBlockState(bs);
					} else if(bs instanceof MCFurnace) {
						MCFurnace furnace = (MCFurnace) bs;
						if(ma.containsKey("burntime")) {
							furnace.setBurnTime(ArgumentValidation.getInt16(ma.get("burntime", t), t));
						}
						if(ma.containsKey("cooktime")) {
							furnace.setCookTime(ArgumentValidation.getInt16(ma.get("cooktime", t), t));
						}
						if(ma.containsKey("inventory")) {
							CArray invData = ArgumentValidation.getArray(ma.get("inventory", t), t);
							MCFurnaceInventory inv = furnace.getInventory();
							if(invData.containsKey("result")) {
								inv.setResult(ObjectGenerator.GetGenerator().item(invData.get("result", t), t));
							}
							if(invData.containsKey("fuel")) {
								inv.setFuel(ObjectGenerator.GetGenerator().item(invData.get("fuel", t), t));
							}
							if(invData.containsKey("smelting")) {
								inv.setSmelting(ObjectGenerator.GetGenerator().item(invData.get("smelting", t), t));
							}
						}
						bsm.setBlockState(bs);
					}
				} else if(meta instanceof MCFireworkEffectMeta) {
					MCFireworkEffectMeta femeta = (MCFireworkEffectMeta) meta;
					if(ma.containsKey("effect")) {
						Mixed cfem = ma.get("effect", t);
						if(cfem instanceof CArray) {
							femeta.setEffect(fireworkEffect((CArray) cfem, t));
						} else if(!(cfem instanceof CNull)) {
							throw new CREFormatException("FireworkCharge effect was expected to be an array or null.", t);
						}
					}
				} else if(meta instanceof MCFireworkMeta) {
					MCFireworkMeta fmeta = (MCFireworkMeta) meta;
					if(ma.containsKey("firework")) {
						Mixed construct = ma.get("firework", t);
						if(construct instanceof CArray) {
							CArray firework = (CArray) construct;
							if(firework.containsKey("strength")) {
								fmeta.setStrength(Static.getInt32(firework.get("strength", t), t));
							}
							if(firework.containsKey("effects")) {
								// New style (supports multiple effects)
								Mixed effects = firework.get("effects", t);
								if(effects instanceof CArray) {
									for(Mixed effect : ((CArray) effects).asList()) {
										if(effect instanceof CArray) {
											fmeta.addEffect(fireworkEffect((CArray) effect, t));
										} else {
											throw new CREFormatException("Firework effect was expected to be an array.", t);
										}
									}
								} else {
									throw new CREFormatException("Firework effects was expected to be an array.", t);
								}
							} else {
								// Old style (supports only one effect)
								fmeta.addEffect(fireworkEffect(firework, t));
							}
						} else {
							throw new CREFormatException("Firework was expected to be an array.", t);
						}
					}
				} else if(meta instanceof MCLeatherArmorMeta) {
					if(ma.containsKey("color")) {
						Mixed ci = ma.get("color", t);
						if(ci instanceof CNull) {
							//nothing
						} else if(ci instanceof CArray) {
							((MCLeatherArmorMeta) meta).setColor(color((CArray) ci, t));
						} else {
							throw new CREFormatException("Color was expected to be an array.", t);
						}
					}
				} else if(meta instanceof MCBookMeta) {
					if(ma.containsKey("title")) {
						Mixed title = ma.get("title", t);
						if(!(title instanceof CNull)) {
							((MCBookMeta) meta).setTitle(title.val());
						}
					}
					if(ma.containsKey("author")) {
						Mixed author = ma.get("author", t);
						if(!(author instanceof CNull)) {
							((MCBookMeta) meta).setAuthor(author.val());
						}
					}
					if(ma.containsKey("pages")) {
						Mixed pages = ma.get("pages", t);
						if(pages instanceof CNull) {
							//nothing
						} else if(pages instanceof CArray) {
							CArray pa = (CArray) pages;
							List<String> pl = new ArrayList<>();
							for(int j = 0; j < pa.size(); j++) {
								pl.add(pa.get(j, t).val());
							}
							((MCBookMeta) meta).setPages(pl);
						} else {
							throw new CREFormatException("Pages field was expected to be an array.", t);
						}
					}
				} else if(meta instanceof MCSkullMeta) {
					if(ma.containsKey("owner")) {
						Mixed owner = ma.get("owner", t);
						if(!(owner instanceof CNull) && !owner.val().isEmpty()) {
							((MCSkullMeta) meta).setOwner(owner.val());
						}
					}
				} else if(meta instanceof MCEnchantmentStorageMeta) {
					if(ma.containsKey("stored")) {
						Mixed stored = ma.get("stored", t);
						if(stored instanceof CNull) {
							//Still doing nothing
						} else if(stored instanceof CArray) {
							for(Map.Entry<MCEnchantment, Integer> ench : enchants((CArray) stored, t).entrySet()) {
								((MCEnchantmentStorageMeta) meta).addStoredEnchant(ench.getKey(), ench.getValue(), true);
							}
						} else {
							throw new CREFormatException("Stored field was expected to be an array of Enchantment arrays", t);
						}
					}
				} else if(meta instanceof MCPotionMeta) {
					if(ma.containsKey("potions")) {
						Mixed effects = ma.get("potions", t);
						if(effects instanceof CArray) {
							for(MCLivingEntity.MCEffect e : potions((CArray) effects, t)) {
								((MCPotionMeta) meta).addCustomEffect(e.getPotionEffectType(), e.getStrength(),
										e.getTicksRemaining(), e.isAmbient(), e.hasParticles(), e.showIcon(), true, t);
							}
						} else {
							throw new CREFormatException("Effects was expected to be an array of potion arrays.", t);
						}
					}
					if(ma.containsKey("base")) {
						Mixed potiondata = ma.get("base", t);
						if(potiondata instanceof CArray) {
							CArray pd = (CArray) potiondata;
							((MCPotionMeta) meta).setBasePotionData(potionData((CArray) potiondata, t));
						}
					}
				} else if(meta instanceof MCBannerMeta) {
					if(ma.containsKey("patterns")) {
						CArray array = ArgumentValidation.getArray(ma.get("patterns", t), t);
						for(String key : array.stringKeySet()) {
							CArray pattern = ArgumentValidation.getArray(array.get(key, t), t);
							MCPatternShape shape = MCPatternShape.valueOf(pattern.get("shape", t).val().toUpperCase());
							String color = pattern.get("color", t).val().toUpperCase();
							try {
								MCDyeColor dyecolor = MCDyeColor.valueOf(color);
								((MCBannerMeta) meta).addPattern(StaticLayer.GetConvertor().GetPattern(dyecolor, shape));
							} catch (IllegalArgumentException ex) {
								if(color.equals("SILVER")) {
									// convert old DyeColor
									((MCBannerMeta) meta).addPattern(StaticLayer.GetConvertor().GetPattern(MCDyeColor.LIGHT_GRAY, shape));
								} else {
									throw ex;
								}
							}
						}
					}
				} else if(meta instanceof MCMapMeta) {
					if(ma.containsKey("color")) {
						Mixed ci = ma.get("color", t);
						if(ci instanceof CArray) {
							((MCMapMeta) meta).setColor(color((CArray) ci, t));
						} else if(!(ci instanceof CNull)) {
							throw new CREFormatException("Color was expected to be an array.", t);
						}
					}
					if(ma.containsKey("mapid")) {
						Mixed cid = ma.get("mapid", t);
						if(!(cid instanceof CNull)) {
							((MCMapMeta) meta).setMapId(Static.getInt32(cid, t));
						}
					}
				} else if(meta instanceof MCTropicalFishBucketMeta) {
					if(ma.containsKey("fishpatterncolor")) {
						Mixed patterncolor = ma.get("fishpatterncolor", t);
						if(!(patterncolor instanceof CNull)) {
							MCDyeColor color = MCDyeColor.valueOf(patterncolor.val().toUpperCase());
							((MCTropicalFishBucketMeta) meta).setPatternColor(color);
						}
					}
					if(ma.containsKey("fishcolor")) {
						Mixed fishcolor = ma.get("fishcolor", t);
						if(!(fishcolor instanceof CNull)) {
							MCDyeColor color = MCDyeColor.valueOf(fishcolor.val().toUpperCase());
							((MCTropicalFishBucketMeta) meta).setBodyColor(color);
						}
					}
					if(ma.containsKey("fishpattern")) {
						Mixed pa = ma.get("fishpattern", t);
						if(!(pa instanceof CNull)) {
							MCTropicalFish.MCPattern pattern = MCTropicalFish.MCPattern.valueOf(pa.val().toUpperCase());
							((MCTropicalFishBucketMeta) meta).setPattern(pattern);
						}
					}
				}
			} catch (Exception ex) {
				throw new CREFormatException(ex.getMessage(), t, ex);
			}
		} else {
			throw new CREFormatException("An array was expected but received " + c + " instead.", t);
		}
		return meta;
	}

	public CArray exception(ConfigRuntimeException e, Environment env, Target t) {
		AbstractCREException ex = AbstractCREException.getAbstractCREException(e);
		return ex.getExceptionObject();
	}

	public AbstractCREException exception(CArray exception, Target t) throws ClassNotFoundException {
		return AbstractCREException.getFromCArray(exception, t);
	}

	/**
	 * Returns a CArray given an MCColor. It will be in the format array(r: 0, g: 0, b: 0)
	 *
	 * @param color
	 * @param t
	 * @return
	 */
	public CArray color(MCColor color, Target t) {
		CArray ca = CArray.GetAssociativeArray(t);
		ca.set("r", new CInt(color.getRed(), t), t);
		ca.set("g", new CInt(color.getGreen(), t), t);
		ca.set("b", new CInt(color.getBlue(), t), t);
		return ca;
	}

	/**
	 * Returns an MCColor given a colorArray, which supports the following three format recipeTypes (in this order of
	 * priority) array(r: 0, g: 0, b: 0) array(red: 0, green: 0, blue: 0) array(0, 0, 0)
	 *
	 * @param color
	 * @param t
	 * @return
	 */
	public MCColor color(CArray color, Target t) {
		int red;
		int green;
		int blue;
		if(color.containsKey("r")) {
			red = Static.getInt32(color.get("r", t), t);
		} else if(color.containsKey("red")) {
			red = Static.getInt32(color.get("red", t), t);
		} else {
			red = Static.getInt32(color.get(0, t), t);
		}
		if(color.containsKey("g")) {
			green = Static.getInt32(color.get("g", t), t);
		} else if(color.containsKey("green")) {
			green = Static.getInt32(color.get("green", t), t);
		} else {
			green = Static.getInt32(color.get(1, t), t);
		}
		if(color.containsKey("b")) {
			blue = Static.getInt32(color.get("b", t), t);
		} else if(color.containsKey("blue")) {
			blue = Static.getInt32(color.get("blue", t), t);
		} else {
			blue = Static.getInt32(color.get(2, t), t);
		}
		try {
			return StaticLayer.GetConvertor().GetColor(red, green, blue);
		} catch (IllegalArgumentException ex) {
			throw new CRERangeException(ex.getMessage(), t, ex);
		}
	}

	/**
	 * Gets a vector object, given a Vector.
	 *
	 * @param vector the Vector
	 * @return the vector array
	 */
	public CArray vector(Vector3D vector) {
		return vector(vector, Target.UNKNOWN);
	}

	/**
	 * Gets a vector object, given a Vector and a Target.
	 *
	 * @param vector the Vector
	 * @param t the Target
	 * @return the vector array
	 */
	public CArray vector(Vector3D vector, Target t) {
		CArray ca = CArray.GetAssociativeArray(t);
		//Integral keys first
		ca.set(0, new CDouble(vector.X(), t), t);
		ca.set(1, new CDouble(vector.Y(), t), t);
		ca.set(2, new CDouble(vector.Z(), t), t);
		//Then string keys
		ca.set("x", new CDouble(vector.X(), t), t);
		ca.set("y", new CDouble(vector.Y(), t), t);
		ca.set("z", new CDouble(vector.Z(), t), t);
		return ca;
	}

	/**
	 * Gets a Vector, given a vector object.
	 *
	 * A vector has three parts: the X, Y, and Z. If the vector object is missing the Z part, then we will assume it is
	 * zero. If the vector object is missing the X and/or Y part, then we will assume it is not a vector.
	 *
	 * Furthermore, the string keys ("x", "y" and "z") take precedence over the integral ones. For example, in a case of
	 * <code>array(0, 1, 2, x: 3, y: 4, z: 5)</code>, the resultant Vector will be of the value
	 * <code>Vector(3, 4, 5)</code>.
	 *
	 * For consistency, the method will accept any Construct, but it requires a CArray.
	 *
	 * @param c the vector array
	 * @param t the target
	 * @return the Vector
	 */
	public Vector3D vector(Mixed c, Target t) {
		return vector(Vector3D.ZERO, c, t);
	}

	/**
	 * Modifies an existing vector using a given vector object. Because Vector3D is immutable, this method does not
	 * actually modify the existing vector, but creates a new one.
	 *
	 * @param v the original vector
	 * @param c the vector array
	 * @param t the target
	 * @return the Vector
	 */
	public Vector3D vector(Vector3D v, Mixed c, Target t) {
		if(c instanceof CArray) {
			CArray va = (CArray) c;
			double x = v.X();
			double y = v.Y();
			double z = v.Z();

			if(!va.isAssociative()) {
				if(va.size() == 3) { // 3rd dimension vector
					x = Static.getNumber(va.get(0, t), t);
					y = Static.getNumber(va.get(1, t), t);
					z = Static.getNumber(va.get(2, t), t);
				} else if(va.size() == 2) { // 2nd dimension vector
					x = Static.getNumber(va.get(0, t), t);
					y = Static.getNumber(va.get(1, t), t);
				} else if(va.size() == 1) {
					x = Static.getNumber(va.get(0, t), t);
				}
			} else {
				if(va.containsKey("x")) {
					x = Static.getNumber(va.get("x", t), t);
				}
				if(va.containsKey("y")) {
					y = Static.getNumber(va.get("y", t), t);
				}
				if(va.containsKey("z")) {
					z = Static.getNumber(va.get("z", t), t);
				}
			}

			return new Vector3D(x, y, z);
		} else if(c instanceof CNull) {
			// fulfilling the todo?
			return v;
		} else {
			throw new CREFormatException("Expecting an array, received " + c.typeof().getSimpleName(), t);
		}
	}

	public CArray enchants(Map<MCEnchantment, Integer> map, Target t) {
		CArray ret = CArray.GetAssociativeArray(t);
		for(Map.Entry<MCEnchantment, Integer> entry : map.entrySet()) {
			CArray enchant = CArray.GetAssociativeArray(t);
			enchant.set("etype", new CString(entry.getKey().getName(), t), t);
			enchant.set("elevel", new CInt(entry.getValue(), t), t);
			ret.set(entry.getKey().getKey(), enchant, t);
		}
		return ret;
	}

	public Map<MCEnchantment, Integer> enchants(CArray enchantArray, Target t) {
		Map<MCEnchantment, Integer> ret = new HashMap<>();
		for(String key : enchantArray.stringKeySet()) {
			MCEnchantment etype = null;
			int elevel;

			Mixed value = enchantArray.get(key, t);
			if(enchantArray.isAssociative()) {
				etype = StaticLayer.GetEnchantmentByName(key);
				if(etype != null && value instanceof CInt) {
					ret.put(etype, Static.getInt32(value, t));
					continue;
				}
			}

			CArray ea = Static.getArray(value, t);
			if(etype == null) {
				String setype = ea.get("etype", t).val();
				etype = StaticLayer.GetEnchantmentByName(setype);
				if(etype == null) {
					if(setype.equals("SWEEPING")) {
						// data from 1.11.2, changed in 1.12
						etype = StaticLayer.GetEnchantmentByName("SWEEPING_EDGE");
					} else {
						throw new CREEnchantmentException("Unknown enchantment type: " + setype, t);
					}
				}
			}
			elevel = Static.getInt32(ea.get("elevel", t), t);
			ret.put(etype, elevel);
		}
		return ret;
	}

	public CArray potions(List<MCLivingEntity.MCEffect> effectList, Target t) {
		CArray ea = CArray.GetAssociativeArray(t);
		for(MCLivingEntity.MCEffect eff : effectList) {
			CArray effect = CArray.GetAssociativeArray(t);
			effect.set("id", new CInt(eff.getPotionEffectType().getId(), t), t);
			effect.set("strength", new CInt(eff.getStrength(), t), t);
			effect.set("seconds", new CDouble(eff.getTicksRemaining() / 20.0, t), t);
			effect.set("ambient", CBoolean.get(eff.isAmbient()), t);
			effect.set("particles", CBoolean.get(eff.hasParticles()), t);
			effect.set("icon", CBoolean.get(eff.showIcon()), t);
			ea.set(eff.getPotionEffectType().name().toLowerCase(), effect, t);
		}
		return ea;
	}

	public List<MCLivingEntity.MCEffect> potions(CArray ea, Target t) {
		List<MCLivingEntity.MCEffect> ret = new ArrayList<>();
		for(String key : ea.stringKeySet()) {
			if(ea.get(key, t) instanceof CArray) {
				CArray effect = (CArray) ea.get(key, t);
				MCPotionEffectType type;
				int strength = 0;
				double seconds = 30.0;
				boolean ambient = false;
				boolean particles = true;
				boolean icon = true;

				try {
					if(ea.isAssociative()) {
						type = MCPotionEffectType.valueOf(key.toUpperCase());
					} else if(effect.containsKey("id")) {
						type = MCPotionEffectType.getById(Static.getInt32(effect.get("id", t), t));
					} else {
						throw new CREFormatException("No potion type was given.", t);
					}
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException(ex.getMessage(), t);
				}

				if(effect.containsKey("strength")) {
					strength = Static.getInt32(effect.get("strength", t), t);
				}
				if(effect.containsKey("seconds")) {
					seconds = Static.getDouble(effect.get("seconds", t), t);
					if(seconds < 0.0) {
						throw new CRERangeException("Seconds cannot be less than 0", t);
					} else if(seconds * 20 > Integer.MAX_VALUE) {
						throw new CRERangeException("Seconds cannot be greater than 107374182", t);
					}
				}
				if(effect.containsKey("ambient")) {
					ambient = Static.getBoolean(effect.get("ambient", t), t);
				}
				if(effect.containsKey("particles")) {
					particles = Static.getBoolean(effect.get("particles", t), t);
				}
				if(effect.containsKey("icon")) {
					icon = Static.getBoolean(effect.get("icon", t), t);
				}
				ret.add(new MCLivingEntity.MCEffect(type, strength, (int) (seconds * 20), ambient, particles, icon));
			} else {
				throw new CREFormatException("Expected a potion array at index" + key, t);
			}
		}
		return ret;
	}

	public CArray potionData(MCPotionData mcpd, Target t) {
		CArray base = CArray.GetAssociativeArray(t);
		base.set("type", mcpd.getType().name(), t);
		base.set("extended", CBoolean.get(mcpd.isExtended()), t);
		base.set("upgraded", CBoolean.get(mcpd.isUpgraded()), t);
		return base;
	}

	public MCPotionData potionData(CArray pd, Target t) {
		MCPotionType type;
		try {
			type = MCPotionType.valueOf(pd.get("type", t).val().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new CREFormatException("Invalid potion type: " + pd.get("type", t).val(), t);
		}
		boolean extended = false;
		boolean upgraded = false;
		if(pd.containsKey("extended")) {
			Mixed cext = pd.get("extended", t);
			if(cext instanceof CBoolean) {
				extended = ((CBoolean) cext).getBoolean();
			} else {
				throw new CREFormatException(
						"Expected potion value for key \"extended\" to be a boolean", t);
			}
		}
		if(pd.containsKey("upgraded")) {
			Mixed cupg = pd.get("upgraded", t);
			if(cupg instanceof CBoolean) {
				upgraded = ((CBoolean) cupg).getBoolean();
			} else {
				throw new CREFormatException(
						"Expected potion value for key \"upgraded\" to be a boolean", t);
			}
		}
		try {
			return StaticLayer.GetPotionData(type, extended, upgraded);
		} catch (IllegalArgumentException ex) {
			throw new CREFormatException(ex.getMessage(), t, ex);
		}
	}

	public CArray fireworkEffect(MCFireworkEffect mcfe, Target t) {
		CArray fe = CArray.GetAssociativeArray(t);
		fe.set("flicker", CBoolean.get(mcfe.hasFlicker()), t);
		fe.set("trail", CBoolean.get(mcfe.hasTrail()), t);
		MCFireworkType type = mcfe.getType();
		if(type != null) {
			fe.set("type", new CString(mcfe.getType().name(), t), t);
		} else {
			fe.set("type", CNull.NULL, t);
		}
		CArray colors = new CArray(t);
		for(MCColor c : mcfe.getColors()) {
			colors.push(ObjectGenerator.GetGenerator().color(c, t), t);
		}
		fe.set("colors", colors, t);
		CArray fadeColors = new CArray(t);
		for(MCColor c : mcfe.getFadeColors()) {
			fadeColors.push(ObjectGenerator.GetGenerator().color(c, t), t);
		}
		fe.set("fade", fadeColors, t);
		return fe;
	}

	public MCFireworkEffect fireworkEffect(CArray fe, Target t) {
		MCFireworkBuilder builder = StaticLayer.GetConvertor().GetFireworkBuilder();
		if(fe.containsKey("flicker")) {
			builder.setFlicker(Static.getBoolean(fe.get("flicker", t), t));
		}
		if(fe.containsKey("trail")) {
			builder.setTrail(Static.getBoolean(fe.get("trail", t), t));
		}
		if(fe.containsKey("colors")) {
			Mixed colors = fe.get("colors", t);
			if(colors instanceof CArray) {
				CArray ccolors = (CArray) colors;
				if(ccolors.size() == 0) {
					builder.addColor(MCColor.WHITE);
				} else {
					for(Mixed color : ccolors.asList()) {
						MCColor mccolor;
						if(color instanceof CString) {
							mccolor = StaticLayer.GetConvertor().GetColor(color.val(), t);
						} else if(color instanceof CArray) {
							mccolor = color((CArray) color, t);
						} else if(color instanceof CInt && ccolors.size() == 3) {
							// Appears to be a single color
							builder.addColor(color(ccolors, t));
							break;
						} else {
							throw new CREFormatException("Expecting individual color to be an array or string, but found "
									+ color.typeof(), t);
						}
						builder.addColor(mccolor);
					}
				}
			} else if(colors instanceof CString) {
				String split[] = colors.val().split("\\|");
				if(split.length == 0) {
					builder.addColor(MCColor.WHITE);
				} else {
					for(String s : split) {
						builder.addColor(StaticLayer.GetConvertor().GetColor(s, t));
					}
				}
			} else {
				throw new CREFormatException("Expecting an array or string for colors parameter, but found "
						+ colors.typeof(), t);
			}
		} else {
			builder.addColor(MCColor.WHITE);
		}
		if(fe.containsKey("fade")) {
			Mixed colors = fe.get("fade", t);
			if(colors instanceof CArray) {
				CArray ccolors = (CArray) colors;
				for(Mixed color : ccolors.asList()) {
					MCColor mccolor;
					if(color instanceof CArray) {
						mccolor = color((CArray) color, t);
					} else if(color instanceof CString) {
						mccolor = StaticLayer.GetConvertor().GetColor(color.val(), t);
					} else if(color instanceof CInt && ccolors.size() == 3) {
						// Appears to be a single color
						builder.addFadeColor(color(ccolors, t));
						break;
					} else {
						throw new CREFormatException("Expecting individual color to be an array or string, but found "
								+ color.typeof(), t);
					}
					builder.addFadeColor(mccolor);
				}
			} else if(colors instanceof CString) {
				String split[] = colors.val().split("\\|");
				for(String s : split) {
					builder.addFadeColor(StaticLayer.GetConvertor().GetColor(s, t));
				}
			} else {
				throw new CREFormatException("Expecting an array or string for fade parameter, but found "
						+ colors.typeof(), t);
			}
		}
		if(fe.containsKey("type")) {
			try {
				builder.setType(MCFireworkType.valueOf(fe.get("type", t).val().toUpperCase()));
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException(ex.getMessage(), t, ex);
			}
		}
		return builder.build();
	}

	public Construct recipe(MCRecipe r, Target t) {
		if(r == null) {
			return CNull.NULL;
		}
		CArray ret = CArray.GetAssociativeArray(t);
		ret.set("type", new CString(r.getRecipeType().name(), t), t);
		ret.set("result", item(r.getResult(), t), t);
		ret.set("key", r.getKey(), t);
		if(r instanceof MCFurnaceRecipe) {
			MCFurnaceRecipe furnace = (MCFurnaceRecipe) r;
			ret.set("input", item(furnace.getInput(), t), t);
		} else if(r instanceof MCShapelessRecipe) {
			MCShapelessRecipe shapeless = (MCShapelessRecipe) r;
			CArray il = new CArray(t);
			for(MCItemStack i : shapeless.getIngredients()) {
				il.push(item(i, t), t);
			}
			ret.set("ingredients", il, t);
		} else if(r instanceof MCShapedRecipe) {
			MCShapedRecipe shaped = (MCShapedRecipe) r;
			CArray shape = new CArray(t);
			for(String line : shaped.getShape()) {
				shape.push(new CString(line, t), t);
			}
			CArray imap = CArray.GetAssociativeArray(t);
			for(Map.Entry<Character, MCItemStack> entry : shaped.getIngredientMap().entrySet()) {
				imap.set(entry.getKey().toString(), item(entry.getValue(), t), t);
			}
			ret.set("shape", shape, t);
			ret.set("ingredients", imap, t);
		}
		return ret;
	}

	public MCRecipe recipe(Mixed c, Target t) {
		if(!(c instanceof CArray)) {
			throw new CRECastException("Expected array but received " + c.typeof().getSimpleName(), t);
		}
		CArray recipe = (CArray) c;

		String recipeKey = recipe.get("key", t).val();

		MCRecipeType recipeType;
		try {
			recipeType = MCRecipeType.valueOf(recipe.get("type", t).val());
		} catch (IllegalArgumentException e) {
			throw new CREFormatException("Invalid recipe type.", t);
		}

		MCItemStack result = item(recipe.get("result", t), t);

		MCRecipe ret;
		try {
			ret = StaticLayer.GetNewRecipe(recipeKey, recipeType, result);
		} catch (IllegalArgumentException ex) {
			throw new CREFormatException(ex.getMessage(), t);
		}
		switch(recipeType) {
			case SHAPED:
				CArray shaped = Static.getArray(recipe.get("shape", t), t);
				String[] shape = new String[(int) shaped.size()];
				if(shaped.size() < 1 || shaped.size() > 3 || shaped.inAssociativeMode()) {
					throw new CREFormatException("Shape array is invalid.", t);
				}
				int i = 0;
				for(Mixed row : shaped.asList()) {
					if(row instanceof CString && row.val().length() >= 1 && row.val().length() <= 3) {
						shape[i] = row.val();
						i++;
					} else {
						throw new CREFormatException("Shape array is invalid.", t);
					}
				}
				((MCShapedRecipe) ret).setShape(shape);

				CArray shapedIngredients = Static.getArray(recipe.get("ingredients", t), t);
				if(!shapedIngredients.inAssociativeMode()) {
					throw new CREFormatException("Ingredients array is invalid.", t);
				}
				for(String key : shapedIngredients.stringKeySet()) {
					MCMaterial mat = null;
					Mixed ingredient = shapedIngredients.get(key, t);
					if(ingredient instanceof CString) {
						mat = StaticLayer.GetMaterial(ingredient.val());
						if(mat == null) {
							// maybe legacy item format
							try {
								if(ingredient.val().contains(":")) {
									String[] split = ingredient.val().split(":");
									mat = StaticLayer.GetMaterialFromLegacy(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
								} else {
									mat = StaticLayer.GetMaterialFromLegacy(Integer.valueOf(ingredient.val()), 0);
								}
								CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "Numeric item formats (eg. \"0:0\" are deprecated.", t);
							} catch (NumberFormatException ex) {}
						}
					} else if(ingredient instanceof CInt) {
						mat = StaticLayer.GetMaterialFromLegacy(Static.getInt32(ingredient, t), 0);
						CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "Numeric item ingredients are deprecated.", t);
					} else if(ingredient instanceof CArray) {
						mat = item(ingredient, t).getType();
					}
					if(mat == null) {
						throw new CREFormatException("Ingredient is invalid: " + ingredient.val(), t);
					}
					((MCShapedRecipe) ret).setIngredient(key.charAt(0), mat);
				}
				return ret;

			case SHAPELESS:
				CArray ingredients = Static.getArray(recipe.get("ingredients", t), t);
				if(ingredients.inAssociativeMode()) {
					throw new CREFormatException("Ingredients array is invalid.", t);
				}
				for(Mixed ingredient : ingredients.asList()) {
					if(ingredient instanceof CString) {
						MCMaterial mat = StaticLayer.GetMaterial(ingredient.val());
						if(mat == null) {
							// maybe legacy item format
							try {
								if(ingredient.val().contains(":")) {
									String[] split = ingredient.val().split(":");
									mat = StaticLayer.GetMaterialFromLegacy(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
								} else {
									mat = StaticLayer.GetMaterialFromLegacy(Integer.valueOf(ingredient.val()), 0);
								}
								CHLog.GetLogger().w(CHLog.Tags.DEPRECATION, "Numeric item formats (eg. \"0:0\" are deprecated.", t);
							} catch (NumberFormatException ex) {}
							if(mat == null) {
								throw new CREFormatException("Ingredient is invalid: " + ingredient.val(), t);
							}
						}
						((MCShapelessRecipe) ret).addIngredient(mat);
					} else if(ingredient instanceof CArray) {
						((MCShapelessRecipe) ret).addIngredient(item(ingredient, t));
					} else {
						throw new CREFormatException("Item was not found", t);
					}
				}
				return ret;

			case FURNACE:
				Mixed input = recipe.get("input", t);
				if(input instanceof CString) {
					MCMaterial mat = StaticLayer.GetMaterial(input.val());
					if(mat == null) {
						throw new CREFormatException("Furnace input is invalid: " + input.val(), t);
					}
					((MCFurnaceRecipe) ret).setInput(mat);
				} else if(input instanceof CArray) {
					((MCFurnaceRecipe) ret).setInput(item(input, t));
				} else {
					throw new CREFormatException("Item was not found", t);
				}
				return ret;

			default:
				throw new CREFormatException("Could not find valid recipe type.", t);
		}
	}

	public MCMaterial material(String name, Target t) {
		MCMaterial mat = StaticLayer.GetMaterial(name.toUpperCase());
		if(mat == null) {
			throw new CREFormatException("Unknown material type: " + name, t);
		}
		return mat;
	}

	public MCMaterial material(Mixed name, Target t) {
		return material(name.val(), t);
	}

	/**
	 * Gets a MetadataValue, given a construct and a plugin.
	 *
	 * @param value
	 * @param plugin
	 * @return
	 */
	public MCMetadataValue metadataValue(Mixed value, MCPlugin plugin) {
		return metadataValue(Static.getJavaObject(value), plugin);
	}

	/**
	 * Gets a MetadataValue, given an object and a plugin.
	 *
	 * @param value
	 * @param plugin
	 * @return
	 */
	public MCMetadataValue metadataValue(Object value, MCPlugin plugin) {
		return StaticLayer.GetMetadataValue(value, plugin);
	}
}
