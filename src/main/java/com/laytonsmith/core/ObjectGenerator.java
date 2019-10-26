package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCBannerMeta;
import com.laytonsmith.abstraction.MCBlockStateMeta;
import com.laytonsmith.abstraction.MCBookMeta;
import com.laytonsmith.abstraction.MCBrewerInventory;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCCreatureSpawner;
import com.laytonsmith.abstraction.MCCrossbowMeta;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCEnchantmentStorageMeta;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.MCFireworkEffectMeta;
import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.MCFurnaceInventory;
import com.laytonsmith.abstraction.MCCookingRecipe;
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
import com.laytonsmith.abstraction.MCStonecuttingRecipe;
import com.laytonsmith.abstraction.MCTropicalFishBucketMeta;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.blocks.MCBanner;
import com.laytonsmith.abstraction.blocks.MCBrewingStand;
import com.laytonsmith.abstraction.blocks.MCContainer;
import com.laytonsmith.abstraction.blocks.MCDispenser;
import com.laytonsmith.abstraction.blocks.MCDropper;
import com.laytonsmith.abstraction.blocks.MCFurnace;
import com.laytonsmith.abstraction.entities.MCTropicalFish;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.MCItemFlag;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCVersion;
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
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
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
import java.util.UUID;

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
		if(!(c.isInstanceOf(CArray.TYPE))) {
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
		if(!(i.isInstanceOf(CArray.TYPE))) {
			throw new CREFormatException("Expected an array!", t);
		}
		CArray item = (CArray) i;
		if(!item.isAssociative()) {
			throw new CREFormatException("Expected an associative array!", t);
		}
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
				if(type.isInstanceOf(CString.TYPE)) {
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
				if(meta.isInstanceOf(CArray.TYPE) && ((CArray) meta).containsKey("spawntype")) {
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
			MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Converted \"" + mat + "\" with data \""
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

			if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_14)) {
				if(meta.hasCustomModelData()) {
					ma.set("model", new CInt(meta.getCustomModelData(), t), t);
				} else {
					ma.set("model", CNull.NULL, t);
				}
			}

			Set<MCItemFlag> itemFlags = meta.getItemFlags();
			CArray flagArray = new CArray(t);
			if(itemFlags.size() > 0) {
				for(MCItemFlag flag : itemFlags) {
					flagArray.push(new CString(flag.name(), t), t);
				}
			}
			ma.set("flags", flagArray, t);

			List<MCAttributeModifier> modifierList = meta.getAttributeModifiers();
			if(modifierList == null) {
				ma.set("modifiers", CNull.NULL, t);
			} else {
				CArray modifiers = new CArray(t);
				for(MCAttributeModifier m : meta.getAttributeModifiers()) {
					modifiers.push(attributeModifier(m, t), t);
				}
				ma.set("modifiers", modifiers, t);
			}

			// Damageable items only
			if(is.getType().getMaxDurability() > 0) {
				ma.set("damage", new CInt(meta.getDamage(), t), t);
				ma.set("unbreakable", CBoolean.get(meta.isUnbreakable()), t);
			}

			// Specific ItemMeta
			if(meta instanceof MCBlockStateMeta) {
				MCBlockState bs = ((MCBlockStateMeta) meta).getBlockState();
				if(bs instanceof MCContainer || bs instanceof MCDispenser || bs instanceof MCDropper) {
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
				if(potionmeta.hasColor()) {
					ma.set("color", color(potionmeta.getColor(), t), t);
				} else {
					ma.set("color", CNull.NULL, t);
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
			} else if(meta instanceof MCCrossbowMeta) {
				MCCrossbowMeta cbm = (MCCrossbowMeta) meta;
				if(cbm.hasChargedProjectiles()) {
					CArray projectiles = new CArray(t);
					for(MCItemStack projectile : cbm.getChargedProjectiles()) {
						projectiles.push(item(projectile, t), t);
					}
					ma.set("projectiles", projectiles, t);
				} else {
					ma.set("projectiles", CNull.NULL, t);
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
		if(c.isInstanceOf(CArray.TYPE)) {
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
					} else if(li.isInstanceOf(CString.TYPE)) {
						List<String> ll = new ArrayList<>();
						ll.add(li.val());
						meta.setLore(ll);
					} else if(li.isInstanceOf(CArray.TYPE)) {
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
					if(enchants.isInstanceOf(CArray.TYPE)) {
						for(Map.Entry<MCEnchantment, Integer> ench : enchants((CArray) enchants, t).entrySet()) {
							meta.addEnchant(ench.getKey(), ench.getValue(), true);
						}
					} else {
						throw new CREFormatException("Enchants field was expected to be an array of Enchantment arrays", t);
					}
				}
				if(ma.containsKey("repair")) {
					Mixed r = ma.get("repair", t);
					if(!(r instanceof CNull)) {
						meta.setRepairCost(Static.getInt32(r, t));
					}
				}
				if(ma.containsKey("model")) {
					Mixed m = ma.get("model", t);
					if(!(m instanceof CNull)) {
						meta.setCustomModelData(Static.getInt32(m, t));
					}
				}
				if(ma.containsKey("flags")) {
					Mixed flags = ma.get("flags", t);
					if(flags.isInstanceOf(CArray.TYPE)) {
						CArray flagArray = (CArray) flags;
						for(int i = 0; i < flagArray.size(); i++) {
							Mixed flag = flagArray.get(i, t);
							meta.addItemFlags(MCItemFlag.valueOf(flag.val().toUpperCase()));
						}
					} else {
						throw new CREFormatException("Itemflags was expected to be an array of flags.", t);
					}
				}

				if(ma.containsKey("modifiers")) {
					Mixed modifiers = ma.get("modifiers", t);
					if(modifiers instanceof CNull) {
						// no modifiers
					} else if(modifiers.isInstanceOf(CArray.TYPE)) {
						CArray modifierArray = (CArray) modifiers;
						if(modifierArray.isAssociative()) {
							throw new CREFormatException("Array of attribute modifiers cannot be associative.", t);
						}
						List<MCAttributeModifier> modifierList = new ArrayList<>();
						for(String key : modifierArray.stringKeySet()) {
							modifierList.add(attributeModifier(Static.getArray(modifierArray.get(key, t), t), t));
						}
						meta.setAttributeModifiers(modifierList);
					} else {
						throw new CREFormatException("Attribute modifiers were expected to be an array.", t);
					}
				}

				// Damageable items only
				if(mat.getMaxDurability() > 0) {
					if(ma.containsKey("damage")) {
						meta.setDamage(Static.getInt32(ma.get("damage", t), t));
					}
					if(ma.containsKey("unbreakable")) {
						meta.setUnbreakable(ArgumentValidation.getBoolean(ma.get("unbreakable", t), t));
					}
				}

				// Specific ItemMeta
				if(meta instanceof MCBlockStateMeta) {
					MCBlockStateMeta bsm = (MCBlockStateMeta) meta;
					MCBlockState bs = bsm.getBlockState();
					if(bs instanceof MCContainer || bs instanceof MCDispenser || bs instanceof MCDropper) {
						if(ma.containsKey("inventory")) {
							MCInventory inv = ((MCInventoryHolder) bs).getInventory();
							Mixed cInvRaw = ma.get("inventory", t);
							if(cInvRaw.isInstanceOf(CArray.TYPE)) {
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
						if(cfem.isInstanceOf(CArray.TYPE)) {
							femeta.setEffect(fireworkEffect((CArray) cfem, t));
						} else if(!(cfem instanceof CNull)) {
							throw new CREFormatException("FireworkCharge effect was expected to be an array or null.", t);
						}
					}
				} else if(meta instanceof MCFireworkMeta) {
					MCFireworkMeta fmeta = (MCFireworkMeta) meta;
					if(ma.containsKey("firework")) {
						Mixed construct = ma.get("firework", t);
						if(construct.isInstanceOf(CArray.TYPE)) {
							CArray firework = (CArray) construct;
							if(firework.containsKey("strength")) {
								fmeta.setStrength(Static.getInt32(firework.get("strength", t), t));
							}
							if(firework.containsKey("effects")) {
								// New style (supports multiple effects)
								Mixed effects = firework.get("effects", t);
								if(effects.isInstanceOf(CArray.TYPE)) {
									for(Mixed effect : ((CArray) effects).asList()) {
										if(effect.isInstanceOf(CArray.TYPE)) {
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
						} else if(ci.isInstanceOf(CArray.TYPE)) {
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
						} else if(pages.isInstanceOf(CArray.TYPE)) {
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
						} else if(stored.isInstanceOf(CArray.TYPE)) {
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
						if(effects.isInstanceOf(CArray.TYPE)) {
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
						if(potiondata.isInstanceOf(CArray.TYPE)) {
							CArray pd = (CArray) potiondata;
							((MCPotionMeta) meta).setBasePotionData(potionData((CArray) potiondata, t));
						}
					}
					if(ma.containsKey("color")) {
						Mixed color = ma.get("color", t);
						if(color.isInstanceOf(CArray.TYPE)) {
							((MCPotionMeta) meta).setColor(color((CArray) color, t));
						} else if(color.isInstanceOf(CString.TYPE)) {
							((MCPotionMeta) meta).setColor(StaticLayer.GetConvertor().GetColor(color.val(), t));
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
						if(ci.isInstanceOf(CArray.TYPE)) {
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
				} else if(meta instanceof MCCrossbowMeta) {
					if(ma.containsKey("projectiles")) {
						Mixed value = ma.get("projectiles", t);
						if(!(value instanceof CNull)) {
							List<MCItemStack> projectiles = new ArrayList<>();
							for(Mixed m : Static.getArray(value, t).asList()) {
								projectiles.add(item(m, t));
							}
							((MCCrossbowMeta) meta).setChargedProjectiles(projectiles);
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

	public AbstractCREException exception(CArray exception, Target t, Environment env) throws ClassNotFoundException {
		return AbstractCREException.getFromCArray(exception, t, env);
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
		if(c.isInstanceOf(CArray.TYPE)) {
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
				if(etype != null && value.isInstanceOf(CInt.TYPE)) {
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

	public CArray attributeModifier(MCAttributeModifier m, Target t) {
		CArray modifier = CArray.GetAssociativeArray(t);
		modifier.set("attribute", m.getAttribute().name());
		modifier.set("name", m.getAttributeName());
		modifier.set("operation", m.getOperation().name());
		modifier.set("uuid", m.getUniqueId().toString());
		modifier.set("amount", new CDouble(m.getAmount(), t), t);

		MCEquipmentSlot slot = m.getEquipmentSlot();
		if(slot == null) {
			modifier.set("slot", CNull.NULL, t);
		} else {
			modifier.set("slot", slot.name());
		}
		return modifier;
	}

	public MCAttributeModifier attributeModifier(CArray m, Target t) {
		if(!m.isAssociative()) {
			throw new CREFormatException("Attribute modifier array must be associative.", t);
		}

		MCAttribute attribute;
		MCAttributeModifier.Operation operation;
		double amount;
		UUID uuid = null;
		String name = null;
		MCEquipmentSlot slot = null;

		try {
			attribute = MCAttribute.valueOf(m.get("attribute", t).val());
		} catch (IllegalArgumentException ex) {
			throw new CREFormatException("Invalid attribute name: " + m.get("attribute", t), t);
		}

		try {
			operation = MCAttributeModifier.Operation.valueOf(m.get("operation", t).val());
		} catch (IllegalArgumentException ex) {
			throw new CREFormatException("Invalid operation name: " + m.get("operation", t), t);
		}

		amount = Static.getDouble(m.get("amount", t), t);

		if(m.containsKey("name")) {
			name = m.get("name", t).val();
		}

		if(m.containsKey("uuid")) {
			try {
				uuid = UUID.fromString(m.get("uuid", t).val());
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException("Invalid UUID format: " + m.get("uuid", t), t);
			}
		}

		if(m.containsKey("slot")) {
			Mixed s = m.get("slot", t);
			if(!(s instanceof CNull)) {
				try {
					slot = MCEquipmentSlot.valueOf(s.val());
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException("Invalid equipment slot name: " + m.get("slot", t), t);
				}
			}
		}

		return StaticLayer.GetConvertor().GetAttributeModifier(attribute, uuid, name, amount, operation, slot);
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
			if(ea.get(key, t).isInstanceOf(CArray.TYPE)) {
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
					ambient = ArgumentValidation.getBoolean(effect.get("ambient", t), t);
				}
				if(effect.containsKey("particles")) {
					particles = ArgumentValidation.getBoolean(effect.get("particles", t), t);
				}
				if(effect.containsKey("icon")) {
					icon = ArgumentValidation.getBoolean(effect.get("icon", t), t);
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
			if(cext.isInstanceOf(CBoolean.TYPE)) {
				extended = ((CBoolean) cext).getBoolean();
			} else {
				throw new CREFormatException(
						"Expected potion value for key \"extended\" to be a boolean", t);
			}
		}
		if(pd.containsKey("upgraded")) {
			Mixed cupg = pd.get("upgraded", t);
			if(cupg.isInstanceOf(CBoolean.TYPE)) {
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
			builder.setFlicker(ArgumentValidation.getBoolean(fe.get("flicker", t), t));
		}
		if(fe.containsKey("trail")) {
			builder.setTrail(ArgumentValidation.getBoolean(fe.get("trail", t), t));
		}
		if(fe.containsKey("colors")) {
			Mixed colors = fe.get("colors", t);
			if(colors.isInstanceOf(CArray.TYPE)) {
				CArray ccolors = (CArray) colors;
				if(ccolors.size() == 0) {
					builder.addColor(MCColor.WHITE);
				} else {
					for(Mixed color : ccolors.asList()) {
						MCColor mccolor;
						if(color.isInstanceOf(CString.TYPE)) {
							mccolor = StaticLayer.GetConvertor().GetColor(color.val(), t);
						} else if(color.isInstanceOf(CArray.TYPE)) {
							mccolor = color((CArray) color, t);
						} else if(color.isInstanceOf(CInt.TYPE) && ccolors.size() == 3) {
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
			} else if(colors.isInstanceOf(CString.TYPE)) {
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
			if(colors.isInstanceOf(CArray.TYPE)) {
				CArray ccolors = (CArray) colors;
				for(Mixed color : ccolors.asList()) {
					MCColor mccolor;
					if(color.isInstanceOf(CArray.TYPE)) {
						mccolor = color((CArray) color, t);
					} else if(color.isInstanceOf(CString.TYPE)) {
						mccolor = StaticLayer.GetConvertor().GetColor(color.val(), t);
					} else if(color.isInstanceOf(CInt.TYPE) && ccolors.size() == 3) {
						// Appears to be a single color
						builder.addFadeColor(color(ccolors, t));
						break;
					} else {
						throw new CREFormatException("Expecting individual color to be an array or string, but found "
								+ color.typeof(), t);
					}
					builder.addFadeColor(mccolor);
				}
			} else if(colors.isInstanceOf(CString.TYPE)) {
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
		ret.set("group", r.getGroup(), t);
		if(r instanceof MCCookingRecipe) {
			MCCookingRecipe recipe = (MCCookingRecipe) r;
			MCMaterial[] list = recipe.getInput();
			if(list.length == 1) {
				ret.set("input", new CString(list[0].getName(), t), t);
			} else {
				CArray mats = new CArray(t);
				for(MCMaterial mat : recipe.getInput()) {
					mats.push(new CString(mat.getName(), t), t);
				}
				ret.set("input", mats, t);
			}
			ret.set("experience", new CDouble(recipe.getExperience(), t), t);
			ret.set("cookingtime", new CInt(recipe.getCookingTime(), t), t);
		} else if(r instanceof MCShapelessRecipe) {
			MCShapelessRecipe shapeless = (MCShapelessRecipe) r;
			CArray il = new CArray(t);
			for(MCMaterial[] list : shapeless.getIngredients()) {
				if(list.length == 1) {
					il.push(new CString(list[0].getName(), t), t);
				} else {
					CArray materials = new CArray(t);
					for(MCMaterial mat : list) {
						materials.push(new CString(mat.getName(), t), t);
					}
					il.push(materials, t);
				}
			}
			ret.set("ingredients", il, t);
		} else if(r instanceof MCShapedRecipe) {
			MCShapedRecipe shaped = (MCShapedRecipe) r;
			CArray shape = new CArray(t);
			for(String line : shaped.getShape()) {
				shape.push(new CString(line, t), t);
			}
			ret.set("shape", shape, t);
			CArray imap = CArray.GetAssociativeArray(t);
			for(Map.Entry<Character, MCMaterial[]> entry : shaped.getIngredientMap().entrySet()) {
				if(entry.getValue() == null) {
					imap.set(entry.getKey().toString(), CNull.NULL, t);
				} else if(entry.getValue().length == 1) {
					imap.set(entry.getKey().toString(), entry.getValue()[0].getName(), t);
				} else {
					CArray materials = new CArray(t);
					for(MCMaterial mat : entry.getValue()) {
						materials.push(new CString(mat.getName(), t), t);
					}
					imap.set(entry.getKey().toString(), materials, t);
				}
			}
			ret.set("ingredients", imap, t);
		} else if(r instanceof MCStonecuttingRecipe) {
			MCStonecuttingRecipe recipe = (MCStonecuttingRecipe) r;
			MCMaterial[] list = recipe.getInput();
			if(list.length == 1) {
				ret.set("input", new CString(list[0].getName(), t), t);
			} else {
				CArray mats = new CArray(t);
				for(MCMaterial mat : recipe.getInput()) {
					mats.push(new CString(mat.getName(), t), t);
				}
				ret.set("input", mats, t);
			}
		}
		return ret;
	}

	public MCRecipe recipe(Mixed c, Target t) {
		if(!(c.isInstanceOf(CArray.TYPE))) {
			throw new CRECastException("Expected array but received " + c.typeof().getSimpleName(), t);
		}
		CArray recipe = (CArray) c;

		String recipeKey = recipe.get("key", t).val();

		MCRecipeType recipeType;
		try {
			recipeType = MCRecipeType.valueOf(recipe.get("type", t).val());
		} catch (IllegalArgumentException e) {
			throw new CREIllegalArgumentException("Invalid recipe type.", t);
		}

		MCItemStack result = item(recipe.get("result", t), t);

		MCRecipe ret;
		try {
			ret = StaticLayer.GetNewRecipe(recipeKey, recipeType, result);
		} catch (IllegalArgumentException ex) {
			throw new CREIllegalArgumentException(ex.getMessage(), t);
		}

		if(recipe.containsKey("group")) {
			ret.setGroup(recipe.get("group", t).val());
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
					if(row.isInstanceOf(CString.TYPE) && row.val().length() >= 1 && row.val().length() <= 3) {
						shape[i] = row.val();
						i++;
					} else {
						throw new CREFormatException("Shape array is invalid.", t);
					}
				}
				((MCShapedRecipe) ret).setShape(shape);

				CArray shapedIngredients = Static.getArray(recipe.get("ingredients", t), t);
				if(!shapedIngredients.inAssociativeMode()) {
					throw new CREIllegalArgumentException("Ingredients array is invalid.", t);
				}
				for(String key : shapedIngredients.stringKeySet()) {
					Mixed ingredient = shapedIngredients.get(key, t);
					if(ingredient.isInstanceOf(CArray.TYPE)) {
						if(((CArray) ingredient).isAssociative()) {
							((MCShapedRecipe) ret).setIngredient(key.charAt(0), item(ingredient, t).getType());
						} else {
							CArray list = (CArray) ingredient;
							MCMaterial[] mats = new MCMaterial[(int) list.size()];
							for(int index = 0; index < list.size(); index++) {
								MCMaterial mat = StaticLayer.GetMaterial(list.get(index, t).val());
								if(mat == null) {
									throw new CREIllegalArgumentException("Recipe input is invalid: "
											+ list.get(index, t).val(), t);
								}
								mats[index] = mat;
							}
							((MCShapedRecipe) ret).setIngredient(key.charAt(0), mats);
						}
					} else if(ingredient instanceof CNull) {
						((MCShapedRecipe) ret).setIngredient(key.charAt(0), EmptyItem());
					} else {
						MCMaterial mat = StaticLayer.GetMaterial(ingredient.val());
						if(mat == null) {
							throw new CREIllegalArgumentException("Ingredient is invalid: " + ingredient.val(), t);
						}
						((MCShapedRecipe) ret).setIngredient(key.charAt(0), mat);
					}
				}
				return ret;

			case SHAPELESS:
				CArray ingredients = Static.getArray(recipe.get("ingredients", t), t);
				if(ingredients.inAssociativeMode()) {
					throw new CREIllegalArgumentException("Ingredients array is invalid.", t);
				}
				for(Mixed ingredient : ingredients.asList()) {
					if(ingredient.isInstanceOf(CArray.TYPE)) {
						if(((CArray) ingredient).isAssociative()) {
							((MCShapelessRecipe) ret).addIngredient(item(ingredient, t));
						} else {
							CArray list = (CArray) ingredient;
							MCMaterial[] mats = new MCMaterial[(int) list.size()];
							for(int index = 0; index < list.size(); index++) {
								MCMaterial mat = StaticLayer.GetMaterial(list.get(index, t).val());
								if(mat == null) {
									throw new CREIllegalArgumentException("Recipe input is invalid: "
											+ list.get(index, t).val(), t);
								}
								mats[index] = mat;
							}
							((MCShapelessRecipe) ret).addIngredient(mats);
						}
					} else {
						MCMaterial mat = StaticLayer.GetMaterial(ingredient.val());
						if(mat == null) {
							throw new CREIllegalArgumentException("Ingredient is invalid: " + ingredient.val(), t);
						}
						((MCShapelessRecipe) ret).addIngredient(mat);
					}
				}
				return ret;

			case BLASTING:
			case CAMPFIRE:
			case FURNACE:
			case SMOKING:
				Mixed input = recipe.get("input", t);
				if(input.isInstanceOf(CArray.TYPE)) {
					if(((CArray) input).isAssociative()) {
						((MCCookingRecipe) ret).setInput(item(input, t));
					} else {
						CArray list = (CArray) input;
						MCMaterial[] mats = new MCMaterial[(int) list.size()];
						for(int index = 0; index < list.size(); index++) {
							MCMaterial mat = StaticLayer.GetMaterial(list.get(index, t).val());
							if(mat == null) {
								throw new CREIllegalArgumentException("Recipe input is invalid: "
										+ list.get(index, t).val(), t);
							}
							mats[index] = mat;
						}
						((MCCookingRecipe) ret).setInput(mats);
					}
				} else {
					MCMaterial mat = StaticLayer.GetMaterial(input.val());
					if(mat == null) {
						throw new CREIllegalArgumentException("Recipe input is invalid: " + input.val(), t);
					}
					((MCCookingRecipe) ret).setInput(mat);
				}
				if(recipe.containsKey("experience")) {
					((MCCookingRecipe) ret).setExperience(Static.getDouble32(recipe.get("experience", t), t));
				}
				if(recipe.containsKey("cookingtime")) {
					((MCCookingRecipe) ret).setCookingTime(Static.getInt32(recipe.get("cookingtime", t), t));
				}
				return ret;

			case STONECUTTING:
				Mixed stoneCutterInput = recipe.get("input", t);
				if(stoneCutterInput.isInstanceOf(CArray.TYPE)) {
					if(((CArray) stoneCutterInput).isAssociative()) {
						((MCStonecuttingRecipe) ret).setInput(item(stoneCutterInput, t));
					} else {
						CArray list = (CArray) stoneCutterInput;
						MCMaterial[] mats = new MCMaterial[(int) list.size()];
						for(int index = 0; index < list.size(); index++) {
							MCMaterial mat = StaticLayer.GetMaterial(list.get(index, t).val());
							if(mat == null) {
								throw new CREIllegalArgumentException("Recipe input is invalid: "
										+ list.get(index, t).val(), t);
							}
							mats[index] = mat;
						}
						((MCStonecuttingRecipe) ret).setInput(mats);
					}
				} else {
					MCMaterial mat = StaticLayer.GetMaterial(stoneCutterInput.val());
					if(mat == null) {
						throw new CREIllegalArgumentException("Recipe input is invalid: " + stoneCutterInput.val(), t);
					}
					((MCStonecuttingRecipe) ret).setInput(mat);
				}
				return ret;

			default:
				throw new CREIllegalArgumentException("Could not find valid recipe type.", t);
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

	public MCBlockData blockData(CArray ca, Target t) {
		StringBuilder b = new StringBuilder().append("[");
		boolean first = true;
		for(String key : ca.stringKeySet()) {
			if(key.equals("block")) {
				String block = ca.get("block", t).val();
				if(Character.isUpperCase(block.charAt(0))) {
					// support material enum input
					block = block.toLowerCase();
				}
				b.insert(0, block);
			} else {
				if(first) {
					first = false;
				} else {
					b.append(',');
				}
				b.append(key).append('=').append(ca.get(key, t).val());
			}
		}
		b.append("]");
		return Static.getServer().createBlockData(b.toString());
	}

	public CArray blockData(MCBlockData blockdata, Target t) {
		CArray ca = CArray.GetAssociativeArray(t);
		String full = blockdata.getAsString().substring(10); // ignore "minecraft:"
		int bracketPos = full.indexOf('[', 3);
		if(bracketPos != -1) {
			ca.set("block", new CString(full.substring(0, bracketPos), t), t);
			String[] states = full.substring(bracketPos + 1, full.length() - 1).split(",");
			for(String s : states) {
				int equalsPos = s.indexOf('=');
				ca.set(s.substring(0, equalsPos), blockState(s.substring(equalsPos + 1)), t);
			}
		} else {
			ca.set("block", new CString(full, t), t);
		}
		return ca;
	}

	private Construct blockState(String value) {
		if(value.length() < 3 && Character.isDigit(value.charAt(0))) {
			// integer states range from 0-25
			try {
				return new CInt(Long.parseLong(value), Target.UNKNOWN);
			} catch (NumberFormatException e) {
			}
		} else if(value.equals("true")) {
			return CBoolean.TRUE;
		} else if(value.equals("false")) {
			return CBoolean.FALSE;
		}
		return new CString(value, Target.UNKNOWN);
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
