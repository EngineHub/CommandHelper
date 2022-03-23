package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCAxolotlBucketMeta;
import com.laytonsmith.abstraction.MCBannerMeta;
import com.laytonsmith.abstraction.MCBlockStateMeta;
import com.laytonsmith.abstraction.MCBookMeta;
import com.laytonsmith.abstraction.MCBrewerInventory;
import com.laytonsmith.abstraction.MCBundleMeta;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCCompassMeta;
import com.laytonsmith.abstraction.MCCookingRecipe;
import com.laytonsmith.abstraction.MCCreatureSpawner;
import com.laytonsmith.abstraction.MCCrossbowMeta;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCEnchantmentStorageMeta;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.MCFireworkEffectMeta;
import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.MCFurnaceInventory;
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
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.MCPlayerProfile;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCPotionMeta;
import com.laytonsmith.abstraction.MCProfileProperty;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCShapedRecipe;
import com.laytonsmith.abstraction.MCShapelessRecipe;
import com.laytonsmith.abstraction.MCSkullMeta;
import com.laytonsmith.abstraction.MCSmithingRecipe;
import com.laytonsmith.abstraction.MCStonecuttingRecipe;
import com.laytonsmith.abstraction.MCTropicalFishBucketMeta;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBanner;
import com.laytonsmith.abstraction.blocks.MCBeehive;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCBrewingStand;
import com.laytonsmith.abstraction.blocks.MCFurnace;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.entities.MCTropicalFish;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCAxolotlType;
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
import com.laytonsmith.core.constructs.generics.GenericParameters;
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
	public CArray location(MCLocation l, Environment env) {
		return location(l, true, env);
	}

	/**
	 * Gets a Location Object, optionally with yaw and pitch, given a MCLocation
	 *
	 * @param l
	 * @param includeYawAndPitch
	 * @return
	 */
	public CArray location(MCLocation l, boolean includeYawAndPitch, Environment env) {
		CArray ca = CArray.GetAssociativeArray(Target.UNKNOWN, null, env);
		Construct x = new CDouble(l.getX(), Target.UNKNOWN);
		Construct y = new CDouble(l.getY(), Target.UNKNOWN);
		Construct z = new CDouble(l.getZ(), Target.UNKNOWN);
		Construct world = (l.getWorld() != null ? new CString(l.getWorld().getName(), Target.UNKNOWN) : CNull.NULL);
		ca.set("0", x, Target.UNKNOWN, env);
		ca.set("1", y, Target.UNKNOWN, env);
		ca.set("2", z, Target.UNKNOWN, env);
		ca.set("3", world, Target.UNKNOWN, env);
		ca.set("x", x, Target.UNKNOWN, env);
		ca.set("y", y, Target.UNKNOWN, env);
		ca.set("z", z, Target.UNKNOWN, env);
		ca.set("world", world, Target.UNKNOWN, env);
		if(includeYawAndPitch) {
			// guarantee yaw in the 0 - 359.9~ range
			float yawRaw = l.getYaw() % 360.0f;
			if(yawRaw < 0.0f) {
				yawRaw += 360.0f;
			}
			Construct yaw = new CDouble(yawRaw, Target.UNKNOWN);
			Construct pitch = new CDouble(l.getPitch(), Target.UNKNOWN);
			ca.set("4", yaw, Target.UNKNOWN, env);
			ca.set("5", pitch, Target.UNKNOWN, env);
			ca.set("yaw", yaw, Target.UNKNOWN, env);
			ca.set("pitch", pitch, Target.UNKNOWN, env);
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
	public MCLocation location(Mixed c, MCWorld w, Target t, Environment env) {
		if(!(c.isInstanceOf(CArray.TYPE, null, env))) {
			throw new CREFormatException("Expecting an array, received " + c.typeof(env).getSimpleName(), t);
		}
		CArray array = (CArray) c;
		MCWorld world = w;
		double x = 0;
		double y = 0;
		double z = 0;
		float yaw = 0;
		float pitch = 0;
		if(!array.inAssociativeMode()) {
			if(array.size(env) == 3) {
				//Just the xyz, with default yaw and pitch, and given world
				x = ArgumentValidation.getNumber(array.get(0, t, env), t, env);
				y = ArgumentValidation.getNumber(array.get(1, t, env), t, env);
				z = ArgumentValidation.getNumber(array.get(2, t, env), t, env);
			} else if(array.size(env) == 4) {
				//x, y, z, world
				x = ArgumentValidation.getNumber(array.get(0, t, env), t, env);
				y = ArgumentValidation.getNumber(array.get(1, t, env), t, env);
				z = ArgumentValidation.getNumber(array.get(2, t, env), t, env);
				world = Static.getServer().getWorld(array.get(3, t, env).val());
			} else if(array.size(env) == 5) {
				//x, y, z, yaw, pitch, with given world
				x = ArgumentValidation.getNumber(array.get(0, t, env), t, env);
				y = ArgumentValidation.getNumber(array.get(1, t, env), t, env);
				z = ArgumentValidation.getNumber(array.get(2, t, env), t, env);
				yaw = (float) ArgumentValidation.getNumber(array.get(3, t, env), t, env);
				pitch = (float) ArgumentValidation.getNumber(array.get(4, t, env), t, env);
			} else if(array.size(env) == 6) {
				//All have been given
				x = ArgumentValidation.getNumber(array.get(0, t, env), t, env);
				y = ArgumentValidation.getNumber(array.get(1, t, env), t, env);
				z = ArgumentValidation.getNumber(array.get(2, t, env), t, env);
				world = Static.getServer().getWorld(array.get(3, t, env).val());
				yaw = (float) ArgumentValidation.getNumber(array.get(4, t, env), t, env);
				pitch = (float) ArgumentValidation.getNumber(array.get(5, t, env), t, env);
			} else {
				throw new CREFormatException("Expecting a Location array, but the array did not meet the format specifications", t);
			}
		} else {
			if(array.containsKey("x")) {
				x = ArgumentValidation.getNumber(array.get("x", t, env), t, env);
			}
			if(array.containsKey("y")) {
				y = ArgumentValidation.getNumber(array.get("y", t, env), t, env);
			}
			if(array.containsKey("z")) {
				z = ArgumentValidation.getNumber(array.get("z", t, env), t, env);
			}
			if(array.containsKey("world")) {
				world = Static.getServer().getWorld(array.get("world", t, env).val());
			}
			if(array.containsKey("yaw")) {
				yaw = (float) ArgumentValidation.getDouble(array.get("yaw", t, env), t, env);
			}
			if(array.containsKey("pitch")) {
				pitch = (float) ArgumentValidation.getDouble(array.get("pitch", t, env), t, env);
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
	public Construct item(MCItemStack is, Target t, Environment env) {
		if(is == null || is.isEmpty()) {
			return CNull.NULL;
		}

		CArray ret = CArray.GetAssociativeArray(t, null, env);
		ret.set("name", new CString(is.getType().getName(), t), t, env);
		ret.set("qty", new CInt(is.getAmount(), t), t, env);
		ret.set("meta", itemMeta(is, t, env), t, env);
		return ret;
	}

	/**
	 * Gets an MCItemStack from a given item "object". Supports both the old and new formats currently
	 *
	 * @param i
	 * @param t
	 * @return An abstract item stack
	 */
	public MCItemStack item(Mixed i, Target t, Environment env) {
		return item(i, t, false, env);
	}

	public MCItemStack item(Mixed i, Target t, boolean legacy, Environment env) {
		if(i instanceof CNull) {
			return EmptyItem();
		}
		if(!(i.isInstanceOf(CArray.TYPE, null, env))) {
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
			qty = ArgumentValidation.getInt32(item.get("qty", t, env), t, env);
			if(qty <= 0) {
				return EmptyItem();
			}
		}

		legacy = legacy || item.containsKey("type") || item.containsKey("data");

		if(legacy) {
			// Do legacy item conversion
			if(item.containsKey("data")) {
				data = ArgumentValidation.getInt32(item.get("data", t, env), t, env);
			}
			MCMaterial material;
			if(item.containsKey("name")) {
				mat = item.get("name", t, env).val();
				if(mat.equals("MAP") || mat.equals("POTION")) {
					// special handling, ignore data here
					material = StaticLayer.GetMaterialFromLegacy(mat, 0);
				} else {
					material = StaticLayer.GetMaterialFromLegacy(mat, data);
				}
			} else {
				Mixed type = item.get("type", t, env);
				if(type.isInstanceOf(CString.TYPE, null, env)) {
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
				int id = ArgumentValidation.getInt32(type, t, env);
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
				Mixed meta = item.get("meta", t, env);
				if(meta.isInstanceOf(CArray.TYPE, null, env)
						&& ((CArray) meta).containsKey("spawntype")) {
					Mixed spawntype = ((CArray) meta).get("spawntype", t, env);
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
			mat = item.get("name", t, env).val();
			ret = StaticLayer.GetItemStack(mat, qty);
		}

		if(ret == null) {
			throw new CREFormatException("Could not find item material from \"" + mat + "\"", t);
		}

		if(ret.isEmpty()) {
			return ret;
		}

		if(item.containsKey("meta")) {
			ret.setItemMeta(itemMeta(item.get("meta", t, env), ret.getType(), t, env));
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
				Map<MCEnchantment, Integer> enchants = enchants((CArray) item.get("enchants", t, env), t, env);
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
		return StaticLayer.GetItemStack("AIR", 0);
	}

	public Construct itemMeta(MCItemStack is, Target t, Environment env) {
		if(!is.hasItemMeta()) {
			return CNull.NULL;
		} else {
			Construct display;
			Construct lore;
			CArray ma = CArray.GetAssociativeArray(t, null, env);
			MCItemMeta meta = is.getItemMeta();
			if(meta.hasDisplayName()) {
				display = new CString(meta.getDisplayName(), t);
			} else {
				display = CNull.NULL;
			}
			if(meta.hasLore()) {
				lore = new CArray(t, null, env);
				for(String l : meta.getLore()) {
					((CArray) lore).push(new CString(l, t), t, env);
				}
			} else {
				lore = CNull.NULL;
			}
			ma.set("display", display, t, env);
			ma.set("lore", lore, t, env);
			ma.set("enchants", enchants(meta.getEnchants(), t, env), t, env);
			ma.set("repair", new CInt(meta.getRepairCost(), t), t, env);

			if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_14)) {
				if(meta.hasCustomModelData()) {
					ma.set("model", new CInt(meta.getCustomModelData(), t), t, env);
				} else {
					ma.set("model", CNull.NULL, t, env);
				}
			}

			Set<MCItemFlag> itemFlags = meta.getItemFlags();
			CArray flagArray = new CArray(t, null, env);
			if(itemFlags.size() > 0) {
				for(MCItemFlag flag : itemFlags) {
					flagArray.push(new CString(flag.name(), t), t, env);
				}
			}
			ma.set("flags", flagArray, t, env);

			List<MCAttributeModifier> modifierList = meta.getAttributeModifiers();
			if(modifierList == null) {
				ma.set("modifiers", CNull.NULL, t, env);
			} else {
				CArray modifiers = new CArray(t, null, env);
				for(MCAttributeModifier m : meta.getAttributeModifiers()) {
					modifiers.push(attributeModifier(m, t, env), t, env);
				}
				ma.set("modifiers", modifiers, t, env);
			}

			// Damageable items only
			if(is.getType().getMaxDurability() > 0) {
				ma.set("damage", new CInt(meta.getDamage(), t), t, env);
				ma.set("unbreakable", CBoolean.get(meta.isUnbreakable()), t, env);
			}

			// Specific ItemMeta
			if(meta instanceof MCBlockStateMeta) {
				MCBlockState bs = ((MCBlockStateMeta) meta).getBlockState(true);
				if(bs instanceof MCBanner) {
					// This is a shield that may or may not have a banner attached, but if get get the BlockState when
					// no banner exists, it gives us a default one. By first checking hasBlockState(),
					// we can ensure we don't populate this meta array with the default banner data.
					if(((MCBlockStateMeta) meta).hasBlockState()) {
						MCBanner banner = (MCBanner) bs;
						ma.set("basecolor", banner.getBaseColor().name(), t, env);
						CArray patterns = new CArray(t, banner.numberOfPatterns(), null, env);
						for(MCPattern p : banner.getPatterns()) {
							CArray pattern = CArray.GetAssociativeArray(t, null, env);
							pattern.set("shape", new CString(p.getShape().toString(), t), t, env);
							pattern.set("color", new CString(p.getColor().toString(), t), t, env);
							patterns.push(pattern, t, env);
						}
						ma.set("patterns", patterns, t, env);
					}
				} else if(bs instanceof MCCreatureSpawner) {
					MCCreatureSpawner mccs = (MCCreatureSpawner) bs;
					ma.set("spawntype", mccs.getSpawnedType().name(), env);
					ma.set("delay", new CInt(mccs.getDelay(), t), t, env);
				} else if(bs instanceof MCBrewingStand) {
					MCBrewingStand brewStand = (MCBrewingStand) bs;
					ma.set("brewtime", new CInt(brewStand.getBrewingTime(), t), t, env);
					ma.set("fuel", new CInt(brewStand.getFuelLevel(), t), t, env);
					MCBrewerInventory inv = brewStand.getInventory();
					CArray invData = CArray.GetAssociativeArray(t, null, env);
					if(inv.getFuel().getAmount() != 0) {
						invData.set("fuel", ObjectGenerator.GetGenerator().item(inv.getFuel(), t, env), t, env);
					}
					if(inv.getIngredient().getAmount() != 0) {
						invData.set("ingredient", ObjectGenerator.GetGenerator().item(inv.getIngredient(), t, env), t, env);
					}
					if(inv.getLeftBottle().getAmount() != 0) {
						invData.set("leftbottle", ObjectGenerator.GetGenerator().item(inv.getLeftBottle(), t, env), t, env);
					}
					if(inv.getMiddleBottle().getAmount() != 0) {
						invData.set("middlebottle", ObjectGenerator.GetGenerator().item(inv.getMiddleBottle(), t, env), t, env);
					}
					if(inv.getRightBottle().getAmount() != 0) {
						invData.set("rightbottle", ObjectGenerator.GetGenerator().item(inv.getRightBottle(), t, env), t, env);
					}
					ma.set("inventory", invData, t, env);
				} else if(bs instanceof MCFurnace) {
					MCFurnace furnace = (MCFurnace) bs;
					ma.set("burntime", new CInt(furnace.getBurnTime(), t), t, env);
					ma.set("cooktime", new CInt(furnace.getCookTime(), t), t, env);
					MCFurnaceInventory inv = furnace.getInventory();
					CArray invData = CArray.GetAssociativeArray(t, null, env);
					if(inv.getResult().getAmount() != 0) {
						invData.set("result", ObjectGenerator.GetGenerator().item(inv.getResult(), t, env), t, env);
					}
					if(inv.getFuel().getAmount() != 0) {
						invData.set("fuel", ObjectGenerator.GetGenerator().item(inv.getFuel(), t, env), t, env);
					}
					if(inv.getSmelting().getAmount() != 0) {
						invData.set("smelting", ObjectGenerator.GetGenerator().item(inv.getSmelting(), t, env), t, env);
					}
					ma.set("inventory", invData, t, env);
				} else if(bs instanceof MCBeehive) {
					MCBeehive hive = (MCBeehive) bs;
					MCLocation flowerLoc = hive.getFlowerLocation();
					if(flowerLoc == null) {
						ma.set("flowerlocation", CNull.NULL, t, env);
					} else {
						ma.set("flowerlocation", location(flowerLoc, false, env), t, env);
					}
				} else if(bs instanceof MCInventoryHolder) {
					// Finally, handle InventoryHolders with inventory slots that do not have a special meaning.
					MCInventory inv = ((MCInventoryHolder) bs).getInventory();
					CArray box = CArray.GetAssociativeArray(t, null, env);
					for(int i = 0; i < inv.getSize(); i++) {
						Construct item = ObjectGenerator.GetGenerator().item(inv.getItem(i), t, env);
						if(!(item instanceof CNull)) {
							box.set(i, item, t, env);
						}
					}
					ma.set("inventory", box, t, env);
				}
			} else if(meta instanceof MCFireworkEffectMeta) {
				MCFireworkEffectMeta mcfem = (MCFireworkEffectMeta) meta;
				MCFireworkEffect effect = mcfem.getEffect();
				if(effect == null) {
					ma.set("effect", CNull.NULL, t, env);
				} else {
					ma.set("effect", fireworkEffect(effect, t, env), t, env);
				}
			} else if(meta instanceof MCFireworkMeta) {
				MCFireworkMeta mcfm = (MCFireworkMeta) meta;
				CArray firework = CArray.GetAssociativeArray(t, null, env);
				firework.set("strength", new CInt(mcfm.getStrength(), t), t, env);
				CArray fe = new CArray(t, null, env);
				for(MCFireworkEffect effect : mcfm.getEffects()) {
					fe.push(fireworkEffect(effect, t, env), t, env);
				}
				firework.set("effects", fe, t, env);
				ma.set("firework", firework, t, env);
			} else if(meta instanceof MCLeatherArmorMeta) {
				CArray color = color(((MCLeatherArmorMeta) meta).getColor(), t, env);
				ma.set("color", color, t, env);
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
					pages = new CArray(t, null, env);
					for(String p : ((MCBookMeta) meta).getPages()) {
						((CArray) pages).push(new CString(p, t), t, env);
					}
				} else {
					pages = CNull.NULL;
				}
				ma.set("title", title, t, env);
				ma.set("author", author, t, env);
				ma.set("pages", pages, t, env);
			} else if(meta instanceof MCSkullMeta) {
				MCPlayerProfile profile = ((MCSkullMeta) meta).getProfile();
				// If a profile doesn't exist, it either doesn't have one (plain head) or it's not supported by server.
				// Either way we fall back to old behavior.
				if(profile != null) {
					if(profile.getName() != null) {
						ma.set("owner", new CString(profile.getName(), t), t, env);
					} else {
						ma.set("owner", CNull.NULL, t, env);
					}
					if(profile.getId() != null) {
						ma.set("owneruuid", new CString(profile.getId().toString(), t), t, env);
					} else {
						ma.set("owneruuid", CNull.NULL, t, env);
					}
					MCProfileProperty texture = profile.getProperty("textures");
					if(texture != null) {
						ma.set("texture", new CString(texture.getValue(), t), t, env);
					} else {
						ma.set("texture", CNull.NULL, t, env);
					}
				} else {
					if(((MCSkullMeta) meta).hasOwner()) {
						ma.set("owner", new CString(((MCSkullMeta) meta).getOwner(), t), t, env);
						MCOfflinePlayer ofp = ((MCSkullMeta) meta).getOwningPlayer();
						if(ofp != null) {
							ma.set("owneruuid", new CString(ofp.getUniqueID().toString(), t), t, env);
						} else {
							ma.set("owneruuid", CNull.NULL, t, env);
						}
					} else {
						ma.set("owner", CNull.NULL, t, env);
						ma.set("owneruuid", CNull.NULL, t, env);
					}
				}
			} else if(meta instanceof MCEnchantmentStorageMeta) {
				Construct stored;
				if(((MCEnchantmentStorageMeta) meta).hasStoredEnchants()) {
					stored = enchants(((MCEnchantmentStorageMeta) meta).getStoredEnchants(), t, env);
				} else {
					stored = CNull.NULL;
				}
				ma.set("stored", stored, t, env);
			} else if(meta instanceof MCPotionMeta) {
				MCPotionMeta potionmeta = (MCPotionMeta) meta;
				CArray effects = potions(potionmeta.getCustomEffects(), t, env);
				ma.set("potions", effects, t, env);
				MCPotionData potiondata = potionmeta.getBasePotionData();
				if(potiondata != null) {
					ma.set("base", potionData(potiondata, t, env), t, env);
				}
				if(potionmeta.hasColor()) {
					ma.set("color", color(potionmeta.getColor(), t, env), t, env);
				} else {
					ma.set("color", CNull.NULL, t, env);
				}
			} else if(meta instanceof MCBannerMeta) {
				MCBannerMeta bannermeta = (MCBannerMeta) meta;
				CArray patterns = new CArray(t, bannermeta.numberOfPatterns(), null, env);
				for(MCPattern p : bannermeta.getPatterns()) {
					CArray pattern = CArray.GetAssociativeArray(t, null, env);
					pattern.set("shape", new CString(p.getShape().toString(), t), t, env);
					pattern.set("color", new CString(p.getColor().toString(), t), t, env);
					patterns.push(pattern, t, env);
				}
				ma.set("patterns", patterns, t, env);
			} else if(meta instanceof MCMapMeta) {
				MCMapMeta mm = ((MCMapMeta) meta);
				MCColor mapcolor = mm.getColor();
				if(mapcolor == null) {
					ma.set("color", CNull.NULL, t, env);
				} else {
					ma.set("color", color(mapcolor, t, env), t, env);
				}
				if(mm.hasMapId()) {
					ma.set("mapid", new CInt(mm.getMapId(), t), t, env);
				} else {
					ma.set("mapid", CNull.NULL, t, env);
				}
			} else if(meta instanceof MCTropicalFishBucketMeta) {
				MCTropicalFishBucketMeta fm = (MCTropicalFishBucketMeta) meta;
				if(fm.hasVariant()) {
					ma.set("fishcolor", new CString(fm.getBodyColor().name(), t), t, env);
					ma.set("fishpatterncolor", new CString(fm.getPatternColor().name(), t), t, env);
					ma.set("fishpattern", new CString(fm.getPattern().name(), t), t, env);
				} else {
					ma.set("fishcolor", CNull.NULL, t, env);
					ma.set("fishpatterncolor", CNull.NULL, t, env);
					ma.set("fishpattern", CNull.NULL, t, env);
				}
			} else if(meta instanceof MCCrossbowMeta) {
				MCCrossbowMeta cbm = (MCCrossbowMeta) meta;
				if(cbm.hasChargedProjectiles()) {
					CArray projectiles = new CArray(t, null, env);
					for(MCItemStack projectile : cbm.getChargedProjectiles()) {
						projectiles.push(item(projectile, t, env), t, env);
					}
					ma.set("projectiles", projectiles, t, env);
				} else {
					ma.set("projectiles", CNull.NULL, t, env);
				}
			} else if(meta instanceof MCCompassMeta) {
				MCCompassMeta cm = (MCCompassMeta) meta;
				if(cm.getTargetLocation() == null) {
					ma.set("target", CNull.NULL, t, env);
				} else {
					ma.set("target", location(cm.getTargetLocation(), false, env), t, env);
				}
				ma.set("lodestone", CBoolean.get(cm.isLodestoneTracked()), t, env);
			} else if(meta instanceof MCBundleMeta) {
				MCBundleMeta bm = (MCBundleMeta) meta;
				List<MCItemStack> items = bm.getItems();
				CArray arrayItems = new CArray(t, null, env);
				for(MCItemStack item : items) {
					arrayItems.push(ObjectGenerator.GetGenerator().item(item, t, env), t, env);
				}
				ma.set("items", arrayItems, t, env);
			} else if(meta instanceof MCAxolotlBucketMeta) {
				ma.set("variant", ((MCAxolotlBucketMeta) meta).getAxolotlType().name(), env);
			}
			return ma;
		}
	}

	public MCItemMeta itemMeta(Mixed c, MCMaterial mat, Target t, Environment env) throws ConfigRuntimeException {
		MCItemFactory itemFactory = Static.getServer().getItemFactory();
		if(itemFactory == null) {
			throw new CRENotFoundException("Could not find the internal MCItemFactory object (are you running in cmdline mode?)", t);
		}
		MCItemMeta meta = itemFactory.getItemMeta(mat);
		if(c instanceof CNull) {
			return meta;
		}
		CArray ma;
		if(c.isInstanceOf(CArray.TYPE, null, env)) {
			ma = (CArray) c;
			try {
				if(ma.containsKey("display")) {
					Mixed dni = ma.get("display", t, env);
					if(!(dni instanceof CNull)) {
						meta.setDisplayName(dni.val());
					}
				}
				if(ma.containsKey("lore")) {
					Mixed li = ma.get("lore", t, env);
					if(li instanceof CNull) {
						//do nothing
					} else if(li.isInstanceOf(CString.TYPE, null, env)) {
						List<String> ll = new ArrayList<>();
						ll.add(li.val());
						meta.setLore(ll);
					} else if(li.isInstanceOf(CArray.TYPE, null, env)) {
						CArray la = (CArray) li;
						List<String> ll = new ArrayList<>();
						for(int j = 0; j < la.size(env); j++) {
							ll.add(la.get(j, t, env).val());
						}
						meta.setLore(ll);
					} else {
						throw new CREFormatException("Lore was expected to be an array or a string.", t);
					}
				}
				if(ma.containsKey("enchants")) {
					Mixed enchants = ma.get("enchants", t, env);
					if(enchants.isInstanceOf(CArray.TYPE, null, env)) {
						for(Map.Entry<MCEnchantment, Integer> ench : enchants((CArray) enchants, t, env).entrySet()) {
							meta.addEnchant(ench.getKey(), ench.getValue(), true);
						}
					} else {
						throw new CREFormatException("Enchants field was expected to be an array of Enchantment arrays", t);
					}
				}
				if(ma.containsKey("repair")) {
					Mixed r = ma.get("repair", t, env);
					if(!(r instanceof CNull)) {
						meta.setRepairCost(ArgumentValidation.getInt32(r, t, env));
					}
				}
				if(ma.containsKey("model")) {
					Mixed m = ma.get("model", t, env);
					if(!(m instanceof CNull)) {
						meta.setCustomModelData(ArgumentValidation.getInt32(m, t, env));
					}
				}
				if(ma.containsKey("flags")) {
					Mixed flags = ma.get("flags", t, env);
					if(flags.isInstanceOf(CArray.TYPE, null, env)) {
						CArray flagArray = (CArray) flags;
						for(int i = 0; i < flagArray.size(env); i++) {
							Mixed flag = flagArray.get(i, t, env);
							meta.addItemFlags(MCItemFlag.valueOf(flag.val().toUpperCase()));
						}
					} else {
						throw new CREFormatException("Itemflags was expected to be an array of flags.", t);
					}
				}

				if(ma.containsKey("modifiers")) {
					Mixed modifiers = ma.get("modifiers", t, env);
					if(modifiers instanceof CNull) {
						// no modifiers
					} else if(modifiers.isInstanceOf(CArray.TYPE, null, env)) {
						CArray modifierArray = (CArray) modifiers;
						if(modifierArray.isAssociative()) {
							throw new CREFormatException("Array of attribute modifiers cannot be associative.", t);
						}
						List<MCAttributeModifier> modifierList = new ArrayList<>();
						for(String key : modifierArray.stringKeySet()) {
							modifierList.add(attributeModifier(ArgumentValidation.getArray(modifierArray.get(key, t, env), t, env), t, env));
						}
						meta.setAttributeModifiers(modifierList);
					} else {
						throw new CREFormatException("Attribute modifiers were expected to be an array.", t);
					}
				}

				// Damageable items only
				if(mat.getMaxDurability() > 0) {
					if(ma.containsKey("damage")) {
						meta.setDamage(ArgumentValidation.getInt32(ma.get("damage", t, env), t, env));
					}
					if(ma.containsKey("unbreakable")) {
						meta.setUnbreakable(ArgumentValidation.getBoolean(ma.get("unbreakable", t, env), t, env));
					}
				}

				// Specific ItemMeta
				if(meta instanceof MCBlockStateMeta) {
					MCBlockStateMeta bsm = (MCBlockStateMeta) meta;
					MCBlockState bs = bsm.getBlockState();
					if(bs instanceof MCBanner) {
						MCBanner banner = (MCBanner) bs;
						if(ma.containsKey("basecolor")) {
							String baseString = ma.get("basecolor", t, env).val().toUpperCase();
							try {
								banner.setBaseColor(MCDyeColor.valueOf(baseString));
							} catch (IllegalArgumentException ex) {
								if(baseString.equals("SILVER")) {
									// convert old DyeColor
									banner.setBaseColor(MCDyeColor.LIGHT_GRAY);
								} else {
									throw ex;
								}
							}
							if(ma.containsKey("patterns")) {
								CArray array = ArgumentValidation.getArray(ma.get("patterns", t, env), t, env);
								for(String key : array.stringKeySet()) {
									CArray pattern = ArgumentValidation.getArray(array.get(key, t, env), t, env);
									MCPatternShape shape = MCPatternShape.valueOf(pattern.get("shape", t, env).val().toUpperCase());
									String color = pattern.get("color", t, env).val().toUpperCase();
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
						}
					} else if(bs instanceof MCCreatureSpawner) {
						MCCreatureSpawner mccs = (MCCreatureSpawner) bs;
						if(ma.containsKey("spawntype")) {
							MCEntityType type = MCEntityType.valueOf(ma.get("spawntype", t, env).val().toUpperCase());
							mccs.setSpawnedType(type);
						}
						if(ma.containsKey("delay")) {
							int delay = ArgumentValidation.getInt32(ma.get("delay", t, env), t, env);
							mccs.setDelay(delay);
						}
						bsm.setBlockState(bs);
					} else if(bs instanceof MCBrewingStand) {
						MCBrewingStand brewStand = (MCBrewingStand) bs;
						if(ma.containsKey("brewtime")) {
							brewStand.setBrewingTime(ArgumentValidation.getInt32(ma.get("brewtime", t, env), t, env));
						}
						if(ma.containsKey("fuel")) {
							brewStand.setFuelLevel(ArgumentValidation.getInt32(ma.get("fuel", t, env), t, env));
						}
						if(ma.containsKey("inventory")) {
							CArray invData = ArgumentValidation.getArray(ma.get("inventory", t, env), t, env);
							MCBrewerInventory inv = brewStand.getInventory();
							if(invData.containsKey("fuel")) {
								inv.setFuel(ObjectGenerator.GetGenerator().item(invData.get("fuel", t, env), t, env));
							}
							if(invData.containsKey("ingredient")) {
								inv.setIngredient(ObjectGenerator.GetGenerator().item(invData.get("ingredient", t, env), t, env));
							}
							if(invData.containsKey("leftbottle")) {
								inv.setLeftBottle(ObjectGenerator.GetGenerator().item(invData.get("leftbottle", t, env), t, env));
							}
							if(invData.containsKey("middlebottle")) {
								inv.setMiddleBottle(ObjectGenerator.GetGenerator().item(invData.get("middlebottle", t, env), t, env));
							}
							if(invData.containsKey("rightbottle")) {
								inv.setRightBottle(ObjectGenerator.GetGenerator().item(invData.get("rightbottle", t, env), t, env));
							}
						}
						bsm.setBlockState(bs);
					} else if(bs instanceof MCFurnace) {
						MCFurnace furnace = (MCFurnace) bs;
						if(ma.containsKey("burntime")) {
							furnace.setBurnTime(ArgumentValidation.getInt16(ma.get("burntime", t, env), t, env));
						}
						if(ma.containsKey("cooktime")) {
							furnace.setCookTime(ArgumentValidation.getInt16(ma.get("cooktime", t, env), t, env));
						}
						if(ma.containsKey("inventory")) {
							CArray invData = ArgumentValidation.getArray(ma.get("inventory", t, env), t, env);
							MCFurnaceInventory inv = furnace.getInventory();
							if(invData.containsKey("result")) {
								inv.setResult(ObjectGenerator.GetGenerator().item(invData.get("result", t, env), t, env));
							}
							if(invData.containsKey("fuel")) {
								inv.setFuel(ObjectGenerator.GetGenerator().item(invData.get("fuel", t, env), t, env));
							}
							if(invData.containsKey("smelting")) {
								inv.setSmelting(ObjectGenerator.GetGenerator().item(invData.get("smelting", t, env), t, env));
							}
						}
						bsm.setBlockState(bs);
					} else if(bs instanceof MCBeehive) {
						MCBeehive hive = (MCBeehive) bs;
						if(ma.containsKey("flowerlocation")) {
							Mixed possibleLoc = ma.get("flowerlocation", t, env);
							if(!(possibleLoc instanceof CNull)) {
								MCLocation flowerLoc = location(possibleLoc, null, t, env);
								hive.setFlowerLocation(flowerLoc);
							}
						}
						bsm.setBlockState(bs);
					} else if(bs instanceof MCInventoryHolder) {
						// Finally, handle InventoryHolders with inventory slots that do not have a special meaning.
						if(ma.containsKey("inventory")) {
							MCInventory inv = ((MCInventoryHolder) bs).getInventory();
							Mixed cInvRaw = ma.get("inventory", t, env);
							if(cInvRaw.isInstanceOf(CArray.TYPE, null, env)) {
								CArray cinv = (CArray) cInvRaw;
								for(String key : cinv.stringKeySet()) {
									try {
										int index = Integer.parseInt(key);
										if(index < 0 || index >= inv.getSize()) {
											ConfigRuntimeException.DoWarning("Out of range value (" + index + ") found"
													+ " in " + bs.getClass().getSimpleName().replaceFirst("MC", "")
													+ " inventory array, so ignoring.");
										}
										MCItemStack is = ObjectGenerator.GetGenerator().item(cinv.get(key, t, env), t, env);
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
					}
				} else if(meta instanceof MCFireworkEffectMeta) {
					MCFireworkEffectMeta femeta = (MCFireworkEffectMeta) meta;
					if(ma.containsKey("effect")) {
						Mixed cfem = ma.get("effect", t, env);
						if(cfem.isInstanceOf(CArray.TYPE, null, env)) {
							femeta.setEffect(fireworkEffect((CArray) cfem, t, env));
						} else if(!(cfem instanceof CNull)) {
							throw new CREFormatException("FireworkCharge effect was expected to be an array or null.", t);
						}
					}
				} else if(meta instanceof MCFireworkMeta) {
					MCFireworkMeta fmeta = (MCFireworkMeta) meta;
					if(ma.containsKey("firework")) {
						Mixed construct = ma.get("firework", t, env);
						if(construct.isInstanceOf(CArray.TYPE, null, env)) {
							CArray firework = (CArray) construct;
							if(firework.containsKey("strength")) {
								fmeta.setStrength(ArgumentValidation.getInt32(firework.get("strength", t, env), t, env));
							}
							if(firework.containsKey("effects")) {
								// New style (supports multiple effects)
								Mixed effects = firework.get("effects", t, env);
								if(effects.isInstanceOf(CArray.TYPE, null, env)) {
									for(Mixed effect : ((CArray) effects).asList(env)) {
										if(effect.isInstanceOf(CArray.TYPE, null, env)) {
											fmeta.addEffect(fireworkEffect((CArray) effect, t, env));
										} else {
											throw new CREFormatException("Firework effect was expected to be an array.", t);
										}
									}
								} else {
									throw new CREFormatException("Firework effects was expected to be an array.", t);
								}
							} else {
								// Old style (supports only one effect)
								fmeta.addEffect(fireworkEffect(firework, t, env));
							}
						} else {
							throw new CREFormatException("Firework was expected to be an array.", t);
						}
					}
				} else if(meta instanceof MCLeatherArmorMeta) {
					if(ma.containsKey("color")) {
						Mixed ci = ma.get("color", t, env);
						if(ci instanceof CNull) {
							//nothing
						} else if(ci.isInstanceOf(CArray.TYPE, null, env)) {
							((MCLeatherArmorMeta) meta).setColor(color((CArray) ci, t, env));
						} else {
							throw new CREFormatException("Color was expected to be an array.", t);
						}
					}
				} else if(meta instanceof MCBookMeta) {
					if(ma.containsKey("title")) {
						Mixed title = ma.get("title", t, env);
						if(!(title instanceof CNull)) {
							((MCBookMeta) meta).setTitle(title.val());
						}
					}
					if(ma.containsKey("author")) {
						Mixed author = ma.get("author", t, env);
						if(!(author instanceof CNull)) {
							((MCBookMeta) meta).setAuthor(author.val());
						}
					}
					if(ma.containsKey("pages")) {
						Mixed pages = ma.get("pages", t, env);
						if(pages instanceof CNull) {
							//nothing
						} else if(pages.isInstanceOf(CArray.TYPE, null, env)) {
							CArray pa = (CArray) pages;
							List<String> pl = new ArrayList<>();
							for(int j = 0; j < pa.size(env); j++) {
								pl.add(pa.get(j, t, env).val());
							}
							((MCBookMeta) meta).setPages(pl);
						} else {
							throw new CREFormatException("Pages field was expected to be an array.", t);
						}
					}
				} else if(meta instanceof MCSkullMeta) {
					String name = null;
					UUID id = null;
					if(ma.containsKey("owner")) {
						name = Construct.nval(ma.get("owner", t, env));
					}
					if(ma.containsKey("owneruuid")) {
						Mixed uuid = ma.get("owneruuid", t, env);
						if(uuid instanceof CString) {
							id = Static.GetUUID(uuid, t);
						}
					}
					if(name != null && !name.isEmpty() || id != null) {
						MCPlayerProfile profile = Static.getServer().getPlayerProfile(id, name);
						if(profile != null) {
							if(ma.containsKey("texture")) {
								Mixed texture = ma.get("texture", t, env);
								if(texture instanceof CString) {
									profile.setProperty(new MCProfileProperty("textures", texture.val(), null));
								}
							}
							((MCSkullMeta) meta).setProfile(profile);
						} else {
							// No profile, but we might still be able to set the owner.
							MCOfflinePlayer ofp = null;
							if(id != null) {
								ofp = Static.getServer().getOfflinePlayer(id);
							}
							if(ofp != null) {
								((MCSkullMeta) meta).setOwningPlayer(ofp);
							} else if(name != null && !name.isEmpty()) {
								// No offline player found by UUID, but we can fallback to owner by name.
								((MCSkullMeta) meta).setOwner(name);
							}
						}
					}
				} else if(meta instanceof MCEnchantmentStorageMeta) {
					if(ma.containsKey("stored")) {
						Mixed stored = ma.get("stored", t, env);
						if(stored instanceof CNull) {
							//Still doing nothing
						} else if(stored.isInstanceOf(CArray.TYPE, null, env)) {
							for(Map.Entry<MCEnchantment, Integer> ench : enchants((CArray) stored, t, env).entrySet()) {
								((MCEnchantmentStorageMeta) meta).addStoredEnchant(ench.getKey(), ench.getValue(), true);
							}
						} else {
							throw new CREFormatException("Stored field was expected to be an array of Enchantment arrays", t);
						}
					}
				} else if(meta instanceof MCPotionMeta) {
					if(ma.containsKey("potions")) {
						Mixed effects = ma.get("potions", t, env);
						if(effects.isInstanceOf(CArray.TYPE, null, env)) {
							for(MCLivingEntity.MCEffect e : potions((CArray) effects, t, env)) {
								((MCPotionMeta) meta).addCustomEffect(e.getPotionEffectType(), e.getStrength(),
										e.getTicksRemaining(), e.isAmbient(), e.hasParticles(), e.showIcon(), true, t);
							}
						} else {
							throw new CREFormatException("Effects was expected to be an array of potion arrays.", t);
						}
					}
					if(ma.containsKey("base")) {
						Mixed potiondata = ma.get("base", t, env);
						if(potiondata.isInstanceOf(CArray.TYPE, null, env)) {
							((MCPotionMeta) meta).setBasePotionData(potionData((CArray) potiondata, t, env));
						}
					}
					if(ma.containsKey("color")) {
						Mixed color = ma.get("color", t, env);
						if(color.isInstanceOf(CArray.TYPE, null, env)) {
							((MCPotionMeta) meta).setColor(color((CArray) color, t, env));
						} else if(color.isInstanceOf(CString.TYPE, null, env)) {
							((MCPotionMeta) meta).setColor(StaticLayer.GetConvertor().GetColor(color.val(), t));
						}
					}
				} else if(meta instanceof MCBannerMeta) {
					if(ma.containsKey("patterns")) {
						CArray array = ArgumentValidation.getArray(ma.get("patterns", t, env), t, env);
						for(String key : array.stringKeySet()) {
							CArray pattern = ArgumentValidation.getArray(array.get(key, t, env), t, env);
							MCPatternShape shape = MCPatternShape.valueOf(pattern.get("shape", t, env).val().toUpperCase());
							String color = pattern.get("color", t, env).val().toUpperCase();
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
						Mixed ci = ma.get("color", t, env);
						if(ci.isInstanceOf(CArray.TYPE, null, env)) {
							((MCMapMeta) meta).setColor(color((CArray) ci, t, env));
						} else if(!(ci instanceof CNull)) {
							throw new CREFormatException("Color was expected to be an array.", t);
						}
					}
					if(ma.containsKey("mapid")) {
						Mixed cid = ma.get("mapid", t, env);
						if(!(cid instanceof CNull)) {
							((MCMapMeta) meta).setMapId(ArgumentValidation.getInt32(cid, t, env));
						}
					}
				} else if(meta instanceof MCTropicalFishBucketMeta) {
					if(ma.containsKey("fishpatterncolor")) {
						Mixed patterncolor = ma.get("fishpatterncolor", t, env);
						if(!(patterncolor instanceof CNull)) {
							MCDyeColor color = MCDyeColor.valueOf(patterncolor.val().toUpperCase());
							((MCTropicalFishBucketMeta) meta).setPatternColor(color);
						}
					}
					if(ma.containsKey("fishcolor")) {
						Mixed fishcolor = ma.get("fishcolor", t, env);
						if(!(fishcolor instanceof CNull)) {
							MCDyeColor color = MCDyeColor.valueOf(fishcolor.val().toUpperCase());
							((MCTropicalFishBucketMeta) meta).setBodyColor(color);
						}
					}
					if(ma.containsKey("fishpattern")) {
						Mixed pa = ma.get("fishpattern", t, env);
						if(!(pa instanceof CNull)) {
							MCTropicalFish.MCPattern pattern = MCTropicalFish.MCPattern.valueOf(pa.val().toUpperCase());
							((MCTropicalFishBucketMeta) meta).setPattern(pattern);
						}
					}
				} else if(meta instanceof MCCrossbowMeta) {
					if(ma.containsKey("projectiles")) {
						Mixed value = ma.get("projectiles", t, env);
						if(!(value instanceof CNull)) {
							List<MCItemStack> projectiles = new ArrayList<>();
							for(Mixed m : ArgumentValidation.getArray(value, t, env).asList(env)) {
								projectiles.add(item(m, t, env));
							}
							((MCCrossbowMeta) meta).setChargedProjectiles(projectiles);
						}
					}
				} else if(meta instanceof MCCompassMeta) {
					if(ma.containsKey("target")) {
						Mixed loc = ma.get("target", t, env);
						if(!(loc instanceof CNull)) {
							((MCCompassMeta) meta).setTargetLocation(location(loc, null, t, env));
						}
					}
					if(ma.containsKey("lodestone")) {
						((MCCompassMeta) meta).setLodestoneTracked(
								ArgumentValidation.getBooleanObject(ma.get("lodestone", t, env), t, env));
					}
				} else if(meta instanceof MCBundleMeta) {
					if(ma.containsKey("items")) {
						Mixed value = ma.get("items", t, env);
						if(value instanceof CArray) {
							MCBundleMeta bm = (MCBundleMeta) meta;
							CArray items = (CArray) value;
							for(String key : items.stringKeySet()) {
								Mixed entry = items.get(key, t, env);
								if(!(entry instanceof CNull)) {
									bm.addItem(ObjectGenerator.GetGenerator().item(entry, t, env));
								}
							}
						} else if(!(value instanceof CNull)) {
							throw new CREFormatException("Items was expected to be an array or null.", t);
						}
					}
				} else if(meta instanceof MCAxolotlBucketMeta) {
					if(ma.containsKey("variant")) {
						Mixed value = ma.get("variant", t, env);
						if(!(value instanceof CNull)) {
							((MCAxolotlBucketMeta) meta).setAxolotlType(MCAxolotlType.valueOf(value.val().toUpperCase()));
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
		return ex.getExceptionObject(env);
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
	public CArray color(MCColor color, Target t, Environment env) {
		CArray ca = CArray.GetAssociativeArray(t, null, env);
		ca.set("r", new CInt(color.getRed(), t), t, env);
		ca.set("g", new CInt(color.getGreen(), t), t, env);
		ca.set("b", new CInt(color.getBlue(), t), t, env);
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
	public MCColor color(CArray color, Target t, Environment env) {
		int red;
		int green;
		int blue;
		if(color.containsKey("r")) {
			red = ArgumentValidation.getInt32(color.get("r", t, env), t, env);
		} else if(color.containsKey("red")) {
			red = ArgumentValidation.getInt32(color.get("red", t, env), t, env);
		} else {
			red = ArgumentValidation.getInt32(color.get(0, t, env), t, env);
		}
		if(color.containsKey("g")) {
			green = ArgumentValidation.getInt32(color.get("g", t, env), t, env);
		} else if(color.containsKey("green")) {
			green = ArgumentValidation.getInt32(color.get("green", t, env), t, env);
		} else {
			green = ArgumentValidation.getInt32(color.get(1, t, env), t, env);
		}
		if(color.containsKey("b")) {
			blue = ArgumentValidation.getInt32(color.get("b", t, env), t, env);
		} else if(color.containsKey("blue")) {
			blue = ArgumentValidation.getInt32(color.get("blue", t, env), t, env);
		} else {
			blue = ArgumentValidation.getInt32(color.get(2, t, env), t, env);
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
	public CArray vector(Vector3D vector, Environment env) {
		return vector(vector, Target.UNKNOWN, env);
	}

	/**
	 * Gets a vector object, given a Vector and a Target.
	 *
	 * @param vector the Vector
	 * @param t the Target
	 * @return the vector array
	 */
	public CArray vector(Vector3D vector, Target t, Environment env) {
		CArray ca = CArray.GetAssociativeArray(t, GenericParameters
			.addParameter(CDouble.TYPE, null).build(), env);
		//Integral keys first
		ca.set(0, new CDouble(vector.X(), t), t, env);
		ca.set(1, new CDouble(vector.Y(), t), t, env);
		ca.set(2, new CDouble(vector.Z(), t), t, env);
		//Then string keys
		ca.set("x", new CDouble(vector.X(), t), t, env);
		ca.set("y", new CDouble(vector.Y(), t), t, env);
		ca.set("z", new CDouble(vector.Z(), t), t, env);
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
	public Vector3D vector(Mixed c, Target t, Environment env) {
		return vector(Vector3D.ZERO, c, t, env);
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
	public Vector3D vector(Vector3D v, Mixed c, Target t, Environment env) {
		if(c.isInstanceOf(CArray.TYPE, null, env)) {
			CArray va = (CArray) c;
			double x = v.X();
			double y = v.Y();
			double z = v.Z();

			if(!va.isAssociative()) {
				if(va.size(env) == 3) { // 3rd dimension vector
					x = ArgumentValidation.getNumber(va.get(0, t, env), t, env);
					y = ArgumentValidation.getNumber(va.get(1, t, env), t, env);
					z = ArgumentValidation.getNumber(va.get(2, t, env), t, env);
				} else if(va.size(env) == 2) { // 2nd dimension vector
					x = ArgumentValidation.getNumber(va.get(0, t, env), t, env);
					y = ArgumentValidation.getNumber(va.get(1, t, env), t, env);
				} else if(va.size(env) == 1) {
					x = ArgumentValidation.getNumber(va.get(0, t, env), t, env);
				}
			} else {
				if(va.containsKey("x")) {
					x = ArgumentValidation.getNumber(va.get("x", t, env), t, env);
				}
				if(va.containsKey("y")) {
					y = ArgumentValidation.getNumber(va.get("y", t, env), t, env);
				}
				if(va.containsKey("z")) {
					z = ArgumentValidation.getNumber(va.get("z", t, env), t, env);
				}
			}

			return new Vector3D(x, y, z);
		} else if(c instanceof CNull) {
			// fulfilling the todo?
			return v;
		} else {
			throw new CREFormatException("Expecting an array, received " + c.typeof(env).getSimpleName(), t);
		}
	}

	public CArray enchants(Map<MCEnchantment, Integer> map, Target t, Environment env) {
		CArray ret = CArray.GetAssociativeArray(t, null, env);
		for(Map.Entry<MCEnchantment, Integer> entry : map.entrySet()) {
			CArray enchant = CArray.GetAssociativeArray(t, null, env);
			enchant.set("etype", new CString(entry.getKey().getName(), t), t, env);
			enchant.set("elevel", new CInt(entry.getValue(), t), t, env);
			ret.set(entry.getKey().getKey(), enchant, t, env);
		}
		return ret;
	}

	public Map<MCEnchantment, Integer> enchants(CArray enchantArray, Target t, Environment env) {
		Map<MCEnchantment, Integer> ret = new HashMap<>();
		for(String key : enchantArray.stringKeySet()) {
			MCEnchantment etype = null;
			int elevel;

			Mixed value = enchantArray.get(key, t, env);
			if(enchantArray.isAssociative()) {
				etype = StaticLayer.GetEnchantmentByName(key);
				if(etype != null && value.isInstanceOf(CInt.TYPE, null, env)) {
					ret.put(etype, ArgumentValidation.getInt32(value, t, env));
					continue;
				}
			}

			CArray ea = ArgumentValidation.getArray(value, t, env);
			if(etype == null) {
				String setype = ea.get("etype", t, env).val();
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
			elevel = ArgumentValidation.getInt32(ea.get("elevel", t, env), t, env);
			ret.put(etype, elevel);
		}
		return ret;
	}

	public CArray attributeModifier(MCAttributeModifier m, Target t, Environment env) {
		CArray modifier = CArray.GetAssociativeArray(t, null, env);
		modifier.set("attribute", m.getAttribute().name(), env);
		modifier.set("name", m.getAttributeName(), env);
		modifier.set("operation", m.getOperation().name(), env);
		modifier.set("uuid", m.getUniqueId().toString(), env);
		modifier.set("amount", new CDouble(m.getAmount(), t), t, env);

		MCEquipmentSlot slot = m.getEquipmentSlot();
		if(slot == null) {
			modifier.set("slot", CNull.NULL, t, env);
		} else {
			modifier.set("slot", slot.name(), env);
		}
		return modifier;
	}

	public MCAttributeModifier attributeModifier(CArray m, Target t, Environment env) {
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
			attribute = MCAttribute.valueOf(m.get("attribute", t, env).val());
		} catch (IllegalArgumentException ex) {
			throw new CREFormatException("Invalid attribute name: " + m.get("attribute", t, env), t);
		}

		try {
			operation = MCAttributeModifier.Operation.valueOf(m.get("operation", t, env).val());
		} catch (IllegalArgumentException ex) {
			throw new CREFormatException("Invalid operation name: " + m.get("operation", t, env), t);
		}

		amount = ArgumentValidation.getDouble(m.get("amount", t, env), t, env);

		if(m.containsKey("name")) {
			name = m.get("name", t, env).val();
		}

		if(m.containsKey("uuid")) {
			try {
				uuid = UUID.fromString(m.get("uuid", t, env).val());
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException("Invalid UUID format: " + m.get("uuid", t, env), t);
			}
		}

		if(m.containsKey("slot")) {
			Mixed s = m.get("slot", t, env);
			if(!(s instanceof CNull)) {
				try {
					slot = MCEquipmentSlot.valueOf(s.val());
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException("Invalid equipment slot name: " + m.get("slot", t, env), t);
				}
			}
		}

		return StaticLayer.GetConvertor().GetAttributeModifier(attribute, uuid, name, amount, operation, slot);
	}

	public CArray potion(MCLivingEntity.MCEffect eff, Target t, Environment env) {
		CArray effect = CArray.GetAssociativeArray(t, null, env);
		effect.set("id", new CInt(eff.getPotionEffectType().getId(), t), t, env);
		effect.set("strength", new CInt(eff.getStrength(), t), t, env);
		effect.set("seconds", new CDouble(eff.getTicksRemaining() / 20.0, t), t, env);
		effect.set("ambient", CBoolean.get(eff.isAmbient()), t, env);
		effect.set("particles", CBoolean.get(eff.hasParticles()), t, env);
		effect.set("icon", CBoolean.get(eff.showIcon()), t, env);

		return effect;
	}

	public CArray potions(List<MCLivingEntity.MCEffect> effectList, Target t, Environment env) {
		CArray ea = CArray.GetAssociativeArray(t, null, env);
		for(MCLivingEntity.MCEffect eff : effectList) {
			CArray effect = potion(eff, t, env);
			ea.set(eff.getPotionEffectType().name().toLowerCase(), effect, t, env);
		}
		return ea;
	}

	public List<MCLivingEntity.MCEffect> potions(CArray ea, Target t, Environment env) {
		List<MCLivingEntity.MCEffect> ret = new ArrayList<>();
		for(String key : ea.stringKeySet()) {
			if(ea.get(key, t, env).isInstanceOf(CArray.TYPE, null, env)) {
				CArray effect = (CArray) ea.get(key, t, env);
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
						type = MCPotionEffectType.getById(ArgumentValidation.getInt32(effect.get("id", t, env), t, env));
					} else {
						throw new CREFormatException("No potion type was given.", t);
					}
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException(ex.getMessage(), t);
				}

				if(effect.containsKey("strength")) {
					strength = ArgumentValidation.getInt32(effect.get("strength", t, env), t, env);
				}
				if(effect.containsKey("seconds")) {
					seconds = ArgumentValidation.getDouble(effect.get("seconds", t, env), t, env);
					if(seconds < 0.0) {
						throw new CRERangeException("Seconds cannot be less than 0", t);
					} else if(seconds * 20 > Integer.MAX_VALUE) {
						throw new CRERangeException("Seconds cannot be greater than 107374182", t);
					}
				}
				if(effect.containsKey("ambient")) {
					ambient = ArgumentValidation.getBoolean(effect.get("ambient", t, env), t, env);
				}
				if(effect.containsKey("particles")) {
					particles = ArgumentValidation.getBoolean(effect.get("particles", t, env), t, env);
				}
				if(effect.containsKey("icon")) {
					icon = ArgumentValidation.getBoolean(effect.get("icon", t, env), t, env);
				}
				ret.add(new MCLivingEntity.MCEffect(type, strength, (int) (seconds * 20), ambient, particles, icon));
			} else {
				throw new CREFormatException("Expected a potion array at index" + key, t);
			}
		}
		return ret;
	}

	public CArray potionData(MCPotionData mcpd, Target t, Environment env) {
		CArray base = CArray.GetAssociativeArray(t, null, env);
		base.set("type", mcpd.getType().name(), t, env);
		base.set("extended", CBoolean.get(mcpd.isExtended()), t, env);
		base.set("upgraded", CBoolean.get(mcpd.isUpgraded()), t, env);
		return base;
	}

	public MCPotionData potionData(CArray pd, Target t, Environment env) {
		MCPotionType type;
		try {
			type = MCPotionType.valueOf(pd.get("type", t, env).val().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new CREFormatException("Invalid potion type: " + pd.get("type", t, env).val(), t);
		}
		boolean extended = false;
		boolean upgraded = false;
		if(pd.containsKey("extended")) {
			Mixed cext = pd.get("extended", t, env);
			if(cext.isInstanceOf(CBoolean.TYPE, null, env)) {
				extended = ((CBoolean) cext).getBoolean();
			} else {
				throw new CREFormatException(
						"Expected potion value for key \"extended\" to be a boolean", t);
			}
		}
		if(pd.containsKey("upgraded")) {
			Mixed cupg = pd.get("upgraded", t, env);
			if(cupg.isInstanceOf(CBoolean.TYPE, null, env)) {
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

	public CArray fireworkEffect(MCFireworkEffect mcfe, Target t, Environment env) {
		CArray fe = CArray.GetAssociativeArray(t, null, env);
		fe.set("flicker", CBoolean.get(mcfe.hasFlicker()), t, env);
		fe.set("trail", CBoolean.get(mcfe.hasTrail()), t, env);
		MCFireworkType type = mcfe.getType();
		if(type != null) {
			fe.set("type", new CString(mcfe.getType().name(), t), t, env);
		} else {
			fe.set("type", CNull.NULL, t, env);
		}
		CArray colors = new CArray(t, null, env);
		for(MCColor c : mcfe.getColors()) {
			colors.push(ObjectGenerator.GetGenerator().color(c, t, env), t, env);
		}
		fe.set("colors", colors, t, env);
		CArray fadeColors = new CArray(t, null, env);
		for(MCColor c : mcfe.getFadeColors()) {
			fadeColors.push(ObjectGenerator.GetGenerator().color(c, t, env), t, env);
		}
		fe.set("fade", fadeColors, t, env);
		return fe;
	}

	public MCFireworkEffect fireworkEffect(CArray fe, Target t, Environment env) {
		MCFireworkBuilder builder = StaticLayer.GetConvertor().GetFireworkBuilder();
		if(fe.containsKey("flicker")) {
			builder.setFlicker(ArgumentValidation.getBoolean(fe.get("flicker", t, env), t, env));
		}
		if(fe.containsKey("trail")) {
			builder.setTrail(ArgumentValidation.getBoolean(fe.get("trail", t, env), t, env));
		}
		if(fe.containsKey("colors")) {
			Mixed colors = fe.get("colors", t, env);
			if(colors.isInstanceOf(CArray.TYPE, null, env)) {
				CArray ccolors = (CArray) colors;
				if(ccolors.size(env) == 0) {
					builder.addColor(MCColor.WHITE);
				} else {
					for(Mixed color : ccolors.asList(env)) {
						MCColor mccolor;
						if(color.isInstanceOf(CString.TYPE, null, env)) {
							mccolor = StaticLayer.GetConvertor().GetColor(color.val(), t);
						} else if(color.isInstanceOf(CArray.TYPE, null, env)) {
							mccolor = color((CArray) color, t, env);
						} else if(color.isInstanceOf(CInt.TYPE, null, env) && ccolors.size(env) == 3) {
							// Appears to be a single color
							builder.addColor(color(ccolors, t, env));
							break;
						} else {
							throw new CREFormatException("Expecting individual color to be an array or string, but found "
									+ color.typeof(env), t);
						}
						builder.addColor(mccolor);
					}
				}
			} else if(colors.isInstanceOf(CString.TYPE, null, env)) {
				String[] split = colors.val().split("\\|");
				if(split.length == 0) {
					builder.addColor(MCColor.WHITE);
				} else {
					for(String s : split) {
						builder.addColor(StaticLayer.GetConvertor().GetColor(s, t));
					}
				}
			} else {
				throw new CREFormatException("Expecting an array or string for colors parameter, but found "
						+ colors.typeof(env), t);
			}
		} else {
			builder.addColor(MCColor.WHITE);
		}
		if(fe.containsKey("fade")) {
			Mixed colors = fe.get("fade", t, env);
			if(colors.isInstanceOf(CArray.TYPE, null, env)) {
				CArray ccolors = (CArray) colors;
				for(Mixed color : ccolors.asList(env)) {
					MCColor mccolor;
					if(color.isInstanceOf(CArray.TYPE, null, env)) {
						mccolor = color((CArray) color, t, env);
					} else if(color.isInstanceOf(CString.TYPE, null, env)) {
						mccolor = StaticLayer.GetConvertor().GetColor(color.val(), t);
					} else if(color.isInstanceOf(CInt.TYPE, null, env) && ccolors.size(env) == 3) {
						// Appears to be a single color
						builder.addFadeColor(color(ccolors, t, env));
						break;
					} else {
						throw new CREFormatException("Expecting individual color to be an array or string, but found "
								+ color.typeof(env), t);
					}
					builder.addFadeColor(mccolor);
				}
			} else if(colors.isInstanceOf(CString.TYPE, null, env)) {
				String[] split = colors.val().split("\\|");
				for(String s : split) {
					builder.addFadeColor(StaticLayer.GetConvertor().GetColor(s, t));
				}
			} else {
				throw new CREFormatException("Expecting an array or string for fade parameter, but found "
						+ colors.typeof(env), t);
			}
		}
		if(fe.containsKey("type")) {
			try {
				builder.setType(MCFireworkType.valueOf(fe.get("type", t, env).val().toUpperCase()));
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException(ex.getMessage(), t, ex);
			}
		}
		return builder.build();
	}

	public Construct recipe(MCRecipe r, Target t, Environment env) {
		if(r == null) {
			return CNull.NULL;
		}
		CArray ret = CArray.GetAssociativeArray(t, null, env);
		ret.set("type", new CString(r.getRecipeType().name(), t), t, env);
		ret.set("result", item(r.getResult(), t, env), t, env);
		ret.set("key", r.getKey(), t, env);
		ret.set("group", r.getGroup(), t, env);
		if(r instanceof MCCookingRecipe) {
			MCCookingRecipe recipe = (MCCookingRecipe) r;
			MCMaterial[] list = recipe.getInput();
			if(list.length == 1) {
				ret.set("input", new CString(list[0].getName(), t), t, env);
			} else {
				CArray mats = new CArray(t, GenericParameters
						.addParameter(CString.TYPE, null).build(), env);
				for(MCMaterial mat : recipe.getInput()) {
					mats.push(new CString(mat.getName(), t), t, env);
				}
				ret.set("input", mats, t, env);
			}
			ret.set("experience", new CDouble(recipe.getExperience(), t), t, env);
			ret.set("cookingtime", new CInt(recipe.getCookingTime(), t), t, env);
		} else if(r instanceof MCShapelessRecipe) {
			MCShapelessRecipe shapeless = (MCShapelessRecipe) r;
			CArray il = new CArray(t, null, env);
			for(MCMaterial[] list : shapeless.getIngredients()) {
				if(list.length == 1) {
					il.push(new CString(list[0].getName(), t), t, env);
				} else {
					CArray materials = new CArray(t, null, env);
					for(MCMaterial mat : list) {
						materials.push(new CString(mat.getName(), t), t, env);
					}
					il.push(materials, t, env);
				}
			}
			ret.set("ingredients", il, t, env);
		} else if(r instanceof MCShapedRecipe) {
			MCShapedRecipe shaped = (MCShapedRecipe) r;
			CArray shape = new CArray(t, null, env);
			for(String line : shaped.getShape()) {
				shape.push(new CString(line, t), t, env);
			}
			ret.set("shape", shape, t, env);
			CArray imap = CArray.GetAssociativeArray(t, null, env);
			for(Map.Entry<Character, MCMaterial[]> entry : shaped.getIngredientMap().entrySet()) {
				if(entry.getValue() == null) {
					imap.set(entry.getKey().toString(), CNull.NULL, t, env);
				} else if(entry.getValue().length == 1) {
					imap.set(entry.getKey().toString(), entry.getValue()[0].getName(), t, env);
				} else {
					CArray materials = new CArray(t, GenericParameters
							.addParameter(CString.TYPE, null).build(), env);
					for(MCMaterial mat : entry.getValue()) {
						materials.push(new CString(mat.getName(), t), t, env);
					}
					imap.set(entry.getKey().toString(), materials, t, env);
				}
			}
			ret.set("ingredients", imap, t, env);
		} else if(r instanceof MCStonecuttingRecipe) {
			MCStonecuttingRecipe recipe = (MCStonecuttingRecipe) r;
			MCMaterial[] list = recipe.getInput();
			if(list.length == 1) {
				ret.set("input", new CString(list[0].getName(), t), t, env);
			} else {
				CArray mats = new CArray(t, GenericParameters
						.addParameter(CString.TYPE, null).build(), env);
				for(MCMaterial mat : list) {
					mats.push(new CString(mat.getName(), t), t, env);
				}
				ret.set("input", mats, t, env);
			}
		} else if(r instanceof MCSmithingRecipe) {
			MCSmithingRecipe recipe = (MCSmithingRecipe) r;
			MCMaterial[] base = recipe.getBase();
			if(base.length == 1) {
				ret.set("base", new CString(base[0].getName(), t), t, env);
			} else {
				CArray mats = new CArray(t, GenericParameters
						.addParameter(CString.TYPE, null).build(), env);
				for(MCMaterial mat : base) {
					mats.push(new CString(mat.getName(), t), t, env);
				}
				ret.set("base", mats, t, env);
			}
			MCMaterial[] additions = recipe.getAddition();
			if(additions.length == 1) {
				ret.set("addition", new CString(additions[0].getName(), t), t, env);
			} else {
				CArray mats = new CArray(t, GenericParameters
						.addParameter(CString.TYPE, null).build(), env);
				for(MCMaterial mat : additions) {
					mats.push(new CString(mat.getName(), t), t, env);
				}
				ret.set("addition", mats, t, env);
			}
		}
		return ret;
	}

	public MCRecipe recipe(Mixed c, Target t, Environment env) {
		if(!(c.isInstanceOf(CArray.TYPE, null, env))) {
			throw new CRECastException("Expected array but received " + c.typeof(env).getSimpleName(), t);
		}
		CArray recipe = (CArray) c;

		String recipeKey = recipe.get("key", t, env).val();

		MCRecipeType recipeType;
		try {
			recipeType = MCRecipeType.valueOf(recipe.get("type", t, env).val());
		} catch (IllegalArgumentException e) {
			throw new CREIllegalArgumentException("Invalid recipe type.", t);
		}

		MCItemStack result = item(recipe.get("result", t, env), t, env);

		MCRecipe ret;
		try {
			ret = StaticLayer.GetNewRecipe(recipeKey, recipeType, result);
		} catch (IllegalArgumentException ex) {
			throw new CREIllegalArgumentException(ex.getMessage(), t);
		}

		if(recipe.containsKey("group")) {
			ret.setGroup(recipe.get("group", t, env).val());
		}

		switch(recipeType) {
			case SHAPED:
				CArray shaped = ArgumentValidation.getArray(recipe.get("shape", t, env), t, env);
				String[] shape = new String[(int) shaped.size(env)];
				if(shaped.size(env) < 1 || shaped.size(env) > 3 || shaped.inAssociativeMode()) {
					throw new CREFormatException("Shape array is invalid.", t);
				}
				int i = 0;
				for(Mixed row : shaped.asList(env)) {
					if(row.isInstanceOf(CString.TYPE, null, env) && row.val().length() >= 1 && row.val().length() <= 3) {
						shape[i] = row.val();
						i++;
					} else {
						throw new CREFormatException("Shape array is invalid.", t);
					}
				}
				((MCShapedRecipe) ret).setShape(shape);

				CArray shapedIngredients = ArgumentValidation.getArray(recipe.get("ingredients", t, env), t, env);
				if(!shapedIngredients.inAssociativeMode()) {
					throw new CREIllegalArgumentException("Ingredients array is invalid.", t);
				}
				for(String key : shapedIngredients.stringKeySet()) {
					Mixed ingredient = shapedIngredients.get(key, t, env);
					if(ingredient.isInstanceOf(CArray.TYPE, null, env)) {
						if(((CArray) ingredient).isAssociative()) {
							((MCShapedRecipe) ret).setIngredient(key.charAt(0), item(ingredient, t, env).getType());
						} else {
							CArray list = (CArray) ingredient;
							MCMaterial[] mats = new MCMaterial[(int) list.size(env)];
							for(int index = 0; index < list.size(env); index++) {
								MCMaterial mat = StaticLayer.GetMaterial(list.get(index, t, env).val());
								if(mat == null) {
									throw new CREIllegalArgumentException("Recipe input is invalid: "
											+ list.get(index, t, env).val(), t);
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
				CArray ingredients = ArgumentValidation.getArray(recipe.get("ingredients", t, env), t, env);
				if(ingredients.inAssociativeMode()) {
					throw new CREIllegalArgumentException("Ingredients array is invalid.", t);
				}
				for(Mixed ingredient : ingredients.asList(env)) {
					if(ingredient.isInstanceOf(CArray.TYPE, null, env)) {
						if(((CArray) ingredient).isAssociative()) {
							((MCShapelessRecipe) ret).addIngredient(item(ingredient, t, env));
						} else {
							CArray list = (CArray) ingredient;
							MCMaterial[] mats = new MCMaterial[(int) list.size(env)];
							for(int index = 0; index < list.size(env); index++) {
								MCMaterial mat = StaticLayer.GetMaterial(list.get(index, t, env).val());
								if(mat == null) {
									throw new CREIllegalArgumentException("Recipe input is invalid: "
											+ list.get(index, t, env).val(), t);
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
				Mixed input = recipe.get("input", t, env);
				if(input.isInstanceOf(CArray.TYPE, null, env)) {
					if(((CArray) input).isAssociative()) {
						((MCCookingRecipe) ret).setInput(item(input, t, env));
					} else {
						CArray list = (CArray) input;
						MCMaterial[] mats = new MCMaterial[(int) list.size(env)];
						for(int index = 0; index < list.size(env); index++) {
							MCMaterial mat = StaticLayer.GetMaterial(list.get(index, t, env).val());
							if(mat == null) {
								throw new CREIllegalArgumentException("Recipe input is invalid: "
										+ list.get(index, t, env).val(), t);
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
					((MCCookingRecipe) ret).setExperience(ArgumentValidation.getDouble32(recipe.get("experience", t, env), t, env));
				}
				if(recipe.containsKey("cookingtime")) {
					((MCCookingRecipe) ret).setCookingTime(ArgumentValidation.getInt32(recipe.get("cookingtime", t, env), t, env));
				}
				return ret;

			case STONECUTTING:
				Mixed stoneCutterInput = recipe.get("input", t, env);
				if(stoneCutterInput.isInstanceOf(CArray.TYPE, null, env)) {
					if(((CArray) stoneCutterInput).isAssociative()) {
						((MCStonecuttingRecipe) ret).setInput(item(stoneCutterInput, t, env));
					} else {
						CArray list = (CArray) stoneCutterInput;
						MCMaterial[] mats = new MCMaterial[(int) list.size(env)];
						for(int index = 0; index < list.size(env); index++) {
							MCMaterial mat = StaticLayer.GetMaterial(list.get(index, t, env).val());
							if(mat == null) {
								throw new CREIllegalArgumentException("Recipe input is invalid: "
										+ list.get(index, t, env).val(), t);
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

	public MCBlockData blockData(CArray ca, Target t, Environment env) {
		StringBuilder b = new StringBuilder().append("[");
		boolean first = true;
		for(String key : ca.stringKeySet()) {
			if(key.equals("block")) {
				String block = ca.get("block", t, env).val();
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
				b.append(key).append('=').append(ca.get(key, t, env).val());
			}
		}
		b.append("]");
		return Static.getServer().createBlockData(b.toString());
	}

	public CArray blockData(MCBlockData blockdata, Target t, Environment env) {
		CArray ca = CArray.GetAssociativeArray(t, null, env);
		String full = blockdata.getAsString().substring(10); // ignore "minecraft:"
		int bracketPos = full.indexOf('[', 3);
		if(bracketPos != -1) {
			ca.set("block", new CString(full.substring(0, bracketPos), t), t, env);
			String[] states = full.substring(bracketPos + 1, full.length() - 1).split(",");
			for(String s : states) {
				int equalsPos = s.indexOf('=');
				ca.set(s.substring(0, equalsPos), blockState(s.substring(equalsPos + 1)), t, env);
			}
		} else {
			ca.set("block", new CString(full, t), t, env);
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
	public MCMetadataValue metadataValue(Mixed value, MCPlugin plugin, Environment env) {
		return metadataValue(Static.getJavaObject(value, env), plugin);
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
