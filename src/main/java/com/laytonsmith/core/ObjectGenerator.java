package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCArmorMeta;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCAxolotlBucketMeta;
import com.laytonsmith.abstraction.MCBannerMeta;
import com.laytonsmith.abstraction.MCBlockStateMeta;
import com.laytonsmith.abstraction.MCBookMeta;
import com.laytonsmith.abstraction.MCBrewerInventory;
import com.laytonsmith.abstraction.MCBundleMeta;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCColorableArmorMeta;
import com.laytonsmith.abstraction.MCCompassMeta;
import com.laytonsmith.abstraction.MCCooldownComponent;
import com.laytonsmith.abstraction.MCCreatureSpawner;
import com.laytonsmith.abstraction.MCCrossbowMeta;
import com.laytonsmith.abstraction.MCEnchantmentStorageMeta;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.MCFireworkEffectMeta;
import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.MCFoodComponent;
import com.laytonsmith.abstraction.MCFurnaceInventory;
import com.laytonsmith.abstraction.MCCookingRecipe;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemFactory;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCKnowledgeBookMeta;
import com.laytonsmith.abstraction.MCLeatherArmorMeta;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMapMeta;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCMusicInstrumentMeta;
import com.laytonsmith.abstraction.MCNamespacedKey;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCOminousBottleMeta;
import com.laytonsmith.abstraction.MCParticleData;
import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.MCPlayerProfile;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCPotionMeta;
import com.laytonsmith.abstraction.MCProfileProperty;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCRecipeChoice;
import com.laytonsmith.abstraction.MCShapedRecipe;
import com.laytonsmith.abstraction.MCShapelessRecipe;
import com.laytonsmith.abstraction.MCSkullMeta;
import com.laytonsmith.abstraction.MCSmithingRecipe;
import com.laytonsmith.abstraction.MCStonecuttingRecipe;
import com.laytonsmith.abstraction.MCSuspiciousStewMeta;
import com.laytonsmith.abstraction.MCTropicalFishBucketMeta;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBeehive;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCCommandBlock;
import com.laytonsmith.abstraction.blocks.MCDecoratedPot;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.blocks.MCBanner;
import com.laytonsmith.abstraction.blocks.MCBrewingStand;
import com.laytonsmith.abstraction.blocks.MCFurnace;
import com.laytonsmith.abstraction.blocks.MCMaterial.MCVanillaMaterial;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.blocks.MCSignText;
import com.laytonsmith.abstraction.entities.MCTropicalFish;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCAxolotlType;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEnchantment;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCEquipmentSlotGroup;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.MCItemFlag;
import com.laytonsmith.abstraction.enums.MCItemRarity;
import com.laytonsmith.abstraction.enums.MCParticle;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTagType;
import com.laytonsmith.abstraction.enums.MCTrimMaterial;
import com.laytonsmith.abstraction.enums.MCTrimPattern;
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
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.MatchResult;

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
		Construct world = (l.getWorld() != null ? new CString(l.getWorld().getName(), Target.UNKNOWN) : CNull.NULL);
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
				x = ArgumentValidation.getNumber(array.get(0, t), t);
				y = ArgumentValidation.getNumber(array.get(1, t), t);
				z = ArgumentValidation.getNumber(array.get(2, t), t);
			} else if(array.size() == 4) {
				//x, y, z, world
				x = ArgumentValidation.getNumber(array.get(0, t), t);
				y = ArgumentValidation.getNumber(array.get(1, t), t);
				z = ArgumentValidation.getNumber(array.get(2, t), t);
				world = Static.getServer().getWorld(array.get(3, t).val());
			} else if(array.size() == 5) {
				//x, y, z, yaw, pitch, with given world
				x = ArgumentValidation.getNumber(array.get(0, t), t);
				y = ArgumentValidation.getNumber(array.get(1, t), t);
				z = ArgumentValidation.getNumber(array.get(2, t), t);
				yaw = (float) ArgumentValidation.getNumber(array.get(3, t), t);
				pitch = (float) ArgumentValidation.getNumber(array.get(4, t), t);
			} else if(array.size() == 6) {
				//All have been given
				x = ArgumentValidation.getNumber(array.get(0, t), t);
				y = ArgumentValidation.getNumber(array.get(1, t), t);
				z = ArgumentValidation.getNumber(array.get(2, t), t);
				world = Static.getServer().getWorld(array.get(3, t).val());
				yaw = (float) ArgumentValidation.getNumber(array.get(4, t), t);
				pitch = (float) ArgumentValidation.getNumber(array.get(5, t), t);
			} else {
				throw new CREFormatException("Expecting a Location array, but the array did not meet the format specifications", t);
			}
		} else {
			if(array.containsKey("x")) {
				x = ArgumentValidation.getNumber(array.get("x", t), t);
			}
			if(array.containsKey("y")) {
				y = ArgumentValidation.getNumber(array.get("y", t), t);
			}
			if(array.containsKey("z")) {
				z = ArgumentValidation.getNumber(array.get("z", t), t);
			}
			if(array.containsKey("world")) {
				world = Static.getServer().getWorld(array.get("world", t).val());
			}
			if(array.containsKey("yaw")) {
				yaw = (float) ArgumentValidation.getDouble(array.get("yaw", t), t);
			}
			if(array.containsKey("pitch")) {
				pitch = (float) ArgumentValidation.getDouble(array.get("pitch", t), t);
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
			qty = ArgumentValidation.getInt32(item.get("qty", t), t);
			if(qty <= 0) {
				return EmptyItem();
			}
		}

		legacy = legacy || item.containsKey("type") || item.containsKey("data");

		if(legacy) {
			// Do legacy item conversion
			if(item.containsKey("data")) {
				data = ArgumentValidation.getInt32(item.get("data", t), t);
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
				int id = ArgumentValidation.getInt32(type, t);
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

			if(!material.isItem()) {
				material = MCMaterial.get("AIR");
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
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Converted legacy enchants array not in meta.", t);
			} catch (ClassCastException ex) {
				throw new CREFormatException("Enchants must be an array of enchantment arrays.", t);
			}
		}

		return ret;
	}

	private static MCItemStack EmptyItem() {
		return StaticLayer.GetItemStack("AIR", 0);
	}

	public Construct itemMeta(MCItemStack is, Target t) {
		if(is == null || !is.hasItemMeta()) {
			return CNull.NULL;
		} else {
			Construct display;
			Construct lore;
			CArray ma = CArray.GetAssociativeArray(t);
			MCItemMeta meta = is.getItemMeta();
			MCMaterial material = is.getType();
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

			if(meta.hasCustomModelData()) {
				ma.set("model", new CInt(meta.getCustomModelData(), t), t);
			} else {
				ma.set("model", CNull.NULL, t);
			}

			Set<MCItemFlag> itemFlags = meta.getItemFlags();
			CArray flagArray = new CArray(t);
			if(!itemFlags.isEmpty()) {
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
				for(MCAttributeModifier m : modifierList) {
					modifiers.push(attributeModifier(m, t), t);
				}
				ma.set("modifiers", modifiers, t);
			}

			if(meta.hasCustomTags()) {
				ma.set("tags", MCTagType.TAG_CONTAINER.construct(meta.getCustomTags()), t);
			} else {
				ma.set("tags", CNull.NULL, t);
			}

			if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
				if(meta.hasMaxStackSize()) {
					ma.set("maxstacksize", new CInt(meta.getMaxStackSize(), t), t);
				}
				if(meta.hasEnchantmentGlintOverride()) {
					ma.set("glint", CBoolean.get(meta.getEnchantmentGlintOverride()), t);
				}
				if(meta.hasRarity()) {
					ma.set("rarity", new CString(meta.getRarity().name(), t), t);
				}
				if(meta.hasMaxDamage()) {
					ma.set("maxdamage", new CInt(meta.getMaxDamage(), t), t);
				}
				if(meta.hasDamage() || material.getMaxDurability() > 0) {
					ma.set("damage", new CInt(meta.getDamage(), t), t);
					ma.set("unbreakable", CBoolean.get(meta.isUnbreakable()), t);
				} else if(meta.isUnbreakable()) {
					ma.set("unbreakable", CBoolean.TRUE, t);
				}
				if(material.isBlock()) {
					if(meta.hasBlockData()) {
						Map<String, String> blockData = meta.getExistingBlockData();
						if(blockData != null) {
							ma.set("blockdata", blockData(material, blockData, t), t);
						} else {
							ma.set("blockdata", CNull.NULL, t);
						}
					} else {
						ma.set("blockdata", CNull.NULL, t);
					}
				}
				if(meta.hasFood()) {
					MCFoodComponent foodComponent = meta.getFood();
					CArray food = CArray.GetAssociativeArray(t);
					food.set("nutrition", new CInt(foodComponent.getNutrition(), t), t);
					food.set("saturation", new CDouble(foodComponent.getSaturation(), t), t);
					food.set("always", CBoolean.get(foodComponent.getCanAlwaysEat()), t);
					ma.set("food", food, t);
				}

				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21)) {
					if(meta.hasJukeboxPlayable()) {
						ma.set("jukeboxsong", new CString(meta.getJukeboxPlayable(), t), t);
					}
				}

				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_3)) {
					if(meta.hasEnchantable()) {
						ma.set("enchantability", new CInt(meta.getEnchantable(), t), t);
					}
					if(meta.isGlider()) {
						ma.set("glider", CBoolean.TRUE, t);
					}
					if(meta.hasUseRemainder()) {
						ma.set("remainder", item(meta.getUseRemainder(), t), t);
					}
					if(meta.hasUseCooldown()) {
						MCCooldownComponent cooldownComponent = meta.getUseCooldown();
						CArray cooldown = CArray.GetAssociativeArray(t);
						cooldown.set("seconds", new CDouble(cooldownComponent.getSeconds(), t), t);
						String group = cooldownComponent.getCooldownGroup();
						if(group != null) {
							cooldown.set("group", new CString(group, t), t);
						}
						ma.set("cooldown", cooldown, t);
					}
					if(meta.hasItemModel()) {
						ma.set("itemmodel", new CString(meta.getItemModel(), t), t);
					}
					if(meta.hasTooltipStyle()) {
						ma.set("tooltipstyle", new CString(meta.getTooltipStyle(), t), t);
					}
				}

			} else { // versions before 1.20.6
				if(material.getMaxDurability() > 0) {
					// Damageable items only
					ma.set("damage", new CInt(meta.getDamage(), t), t);
					ma.set("unbreakable", CBoolean.get(meta.isUnbreakable()), t);
				} else if(material.isBlock()) {
					if(meta.hasBlockData()) {
						ma.set("blockdata", blockData(meta.getBlockData(material), t), t);
					} else {
						ma.set("blockdata", CNull.NULL, t);
					}
				}
			}

			// Specific ItemMeta
			if(meta instanceof MCBlockStateMeta) {
				MCBlockState bs = ((MCBlockStateMeta) meta).getBlockState(true);
				if(bs instanceof MCBanner) {
					// This is a shield that may or may not have a banner attached, but if we get the BlockState when
					// no banner exists, it gives us a default one. By first checking hasBlockState(),
					// we can ensure we don't populate this meta array with the default banner data.
					if(((MCBlockStateMeta) meta).hasBlockState()) {
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
					}
				} else if(bs instanceof MCCreatureSpawner) {
					MCCreatureSpawner mccs = (MCCreatureSpawner) bs;
					MCEntityType type = mccs.getSpawnedType();
					if(type == null) {
						ma.set("spawntype", CNull.NULL, t);
					} else {
						ma.set("spawntype", type.name());
					}
					ma.set("delay", new CInt(mccs.getDelay(), t), t);
					ma.set("mindelay", new CInt(mccs.getMinDelay(), t), t);
					ma.set("maxdelay", new CInt(mccs.getMaxDelay(), t), t);
					ma.set("spawncount", new CInt(mccs.getSpawnCount(), t), t);
					ma.set("maxnearbyentities", new CInt(mccs.getMaxNearbyEntities(), t), t);
					ma.set("playerrange", new CInt(mccs.getPlayerRange(), t), t);
					ma.set("spawnrange", new CInt(mccs.getSpawnRange(), t), t);
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
				} else if(bs instanceof MCDecoratedPot decoratedPot) {
					CArray sherds = CArray.GetAssociativeArray(t);
					Map<MCDecoratedPot.Side, MCMaterial> potSherds = decoratedPot.getSherds();
					for(Map.Entry<MCDecoratedPot.Side, MCMaterial> side : potSherds.entrySet()) {
						sherds.set(side.getKey().name().toLowerCase(), side.getValue().name());
					}
					ma.set("sherds", sherds, t);
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_4)) {
						ma.set("item", item(decoratedPot.getItemStack(), t), t);
					}
				} else if(bs instanceof MCInventoryHolder) {
					// Finally, handle InventoryHolders with inventory slots that do not have a special meaning.
					MCInventory inv = ((MCInventoryHolder) bs).getInventory();
					CArray box = CArray.GetAssociativeArray(t);
					for(int i = 0; i < inv.getSize(); i++) {
						Construct item = ObjectGenerator.GetGenerator().item(inv.getItem(i), t);
						if(!(item instanceof CNull)) {
							box.set(i, item, t);
						}
					}
					ma.set("inventory", box, t);
				} else if(bs instanceof MCSign sign) {
					ma.set("waxed", CBoolean.get(sign.isWaxed()), t);
					CArray lines = new CArray(t);
					for(String line : sign.getLines()) {
						lines.push(new CString(line, t), t);
					}
					ma.set("signtext", lines, t);
					ma.set("glowing", CBoolean.get(sign.isGlowingText()), t);
					MCDyeColor color = sign.getDyeColor();
					if(color == null) {
						ma.set("color", CNull.NULL, t);
					} else {
						ma.set("color", color.name(), t);
					}
					MCSignText backText = sign.getBackText();
					if(backText != null) {
						CArray back = new CArray(t);
						for(String line : backText.getLines()) {
							back.push(new CString(line, t), t);
						}
						ma.set("backtext", back, t);
						ma.set("backglowing", CBoolean.get(backText.isGlowingText()), t);
						MCDyeColor backColor = backText.getDyeColor();
						if(backColor == null) {
							ma.set("backcolor", CNull.NULL, t);
						} else {
							ma.set("backcolor", backColor.name(), t);
						}
					}
				} else if(bs instanceof MCCommandBlock cmdBlock) {
					ma.set("command", cmdBlock.getCommand());
					ma.set("customname", cmdBlock.getName());
				} else if(bs instanceof MCBeehive beehive) {
					ma.set("beecount", new CInt(beehive.getEntityCount(), t), t);
				}
			} else if(meta instanceof MCArmorMeta armorMeta) { // Must be before MCLeatherArmorMeta
				if(armorMeta.hasTrim()) {
					CArray trim = CArray.GetAssociativeArray(t);
					trim.set("material", armorMeta.getTrimMaterial().name());
					trim.set("pattern", armorMeta.getTrimPattern().name());
					ma.set("trim", trim, t);
				} else {
					ma.set("trim", CNull.NULL, t);
				}
				if(armorMeta instanceof MCColorableArmorMeta) {
					ma.set("color", color(((MCColorableArmorMeta) armorMeta).getColor(), t), t);
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
			} else if(meta instanceof MCBookMeta bookMeta) {
				if(material.getAbstracted() == MCVanillaMaterial.WRITTEN_BOOK) {
					Construct title;
					Construct author;
					if(bookMeta.hasTitle()) {
						title = new CString(bookMeta.getTitle(), t);
					} else {
						title = CNull.NULL;
					}
					if(bookMeta.hasAuthor()) {
						author = new CString(bookMeta.getAuthor(), t);
					} else {
						author = CNull.NULL;
					}
					ma.set("title", title, t);
					ma.set("author", author, t);
					ma.set("generation", bookMeta.getGeneration().name(), t);
				}
				Construct pages;
				if(bookMeta.hasPages()) {
					pages = new CArray(t);
					for(String p : bookMeta.getPages()) {
						((CArray) pages).push(new CString(p, t), t);
					}
				} else {
					pages = CNull.NULL;
				}
				ma.set("pages", pages, t);
			} else if(meta instanceof MCSkullMeta) {
				MCPlayerProfile profile = ((MCSkullMeta) meta).getProfile();
				// If a profile doesn't exist, it either doesn't have one (plain head) or it's not supported by server.
				// Either way we fall back to old behavior.
				if(profile != null) {
					if(profile.getName() != null) {
						ma.set("owner", new CString(profile.getName(), t), t);
					} else {
						ma.set("owner", CNull.NULL, t);
					}
					if(profile.getId() != null) {
						ma.set("owneruuid", new CString(profile.getId().toString(), t), t);
					} else {
						ma.set("owneruuid", CNull.NULL, t);
					}
					MCProfileProperty texture = profile.getProperty("textures");
					if(texture != null) {
						ma.set("texture", new CString(texture.getValue(), t), t);
					} else {
						ma.set("texture", CNull.NULL, t);
					}
				} else {
					if(((MCSkullMeta) meta).hasOwner()) {
						ma.set("owner", new CString(((MCSkullMeta) meta).getOwner(), t), t);
						MCOfflinePlayer ofp = ((MCSkullMeta) meta).getOwningPlayer();
						if(ofp != null) {
							ma.set("owneruuid", new CString(ofp.getUniqueID().toString(), t), t);
						} else {
							ma.set("owneruuid", CNull.NULL, t);
						}
					} else {
						ma.set("owner", CNull.NULL, t);
						ma.set("owneruuid", CNull.NULL, t);
					}
				}
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19_3)) {
					String sound = ((MCSkullMeta) meta).getNoteBlockSound();
					if(sound == null) {
						ma.set("noteblocksound", CNull.NULL, t);
					} else {
						ma.set("noteblocksound", new CString(sound, t), t);
					}
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
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
					MCPotionType potionType = potionmeta.getBasePotionType();
					if(potionType == null) {
						ma.set("potiontype", CNull.NULL, t);
					} else {
						ma.set("potiontype", potionType.name());
					}
				} else {
					MCPotionData potiondata = potionmeta.getBasePotionData();
					if(potiondata != null) {
						ma.set("base", potionData(potiondata, t), t);
					}
				}
				if(potionmeta.hasColor()) {
					ma.set("color", color(potionmeta.getColor(), t), t);
				} else {
					ma.set("color", CNull.NULL, t);
				}
			} else if(meta instanceof MCSuspiciousStewMeta susstew) {
				CArray effects = potions(susstew.getCustomEffects(), t);
				ma.set("potions", effects, t);
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
			} else if(meta instanceof MCCompassMeta) {
				MCCompassMeta cm = (MCCompassMeta) meta;
				if(cm.getTargetLocation() == null) {
					ma.set("target", CNull.NULL, t);
				} else {
					ma.set("target", location(cm.getTargetLocation(), false), t);
				}
				ma.set("lodestone", CBoolean.get(cm.isLodestoneTracked()), t);
			} else if(meta instanceof MCBundleMeta) {
				MCBundleMeta bm = (MCBundleMeta) meta;
				List<MCItemStack> items = bm.getItems();
				CArray arrayItems = new CArray(t);
				for(MCItemStack item : items) {
					arrayItems.push(ObjectGenerator.GetGenerator().item(item, t), t);
				}
				ma.set("items", arrayItems, t);
			} else if(meta instanceof MCAxolotlBucketMeta) {
				ma.set("variant", ((MCAxolotlBucketMeta) meta).getAxolotlType().name());
			} else if(meta instanceof MCMusicInstrumentMeta) {
				String instrumentKey = ((MCMusicInstrumentMeta) meta).getInstrument();
				if(instrumentKey == null) {
					ma.set("instrument", CNull.NULL, t);
				} else {
					ma.set("instrument", instrumentKey);
				}
			} else if(meta instanceof MCKnowledgeBookMeta knowledgeBookMeta) {
				if(knowledgeBookMeta.hasRecipes()) {
					CArray recipes = new CArray(t);
					for(MCNamespacedKey key : knowledgeBookMeta.getRecipes()) {
						recipes.push(new CString(key.toString(), t), t);
					}
					ma.set("recipes", recipes, t);
				} else {
					ma.set("recipes", CNull.NULL, t);
				}
			} else if(meta instanceof MCOminousBottleMeta ominousBottleMeta) {
				if(ominousBottleMeta.hasAmplifier()) {
					ma.set("ominousamplifier", new CInt(ominousBottleMeta.getAmplifier(), t), t);
				} else {
					ma.set("ominousamplifier", CNull.NULL, t);
				}
			}
			return ma;
		}
	}

	/**
	 * Generates item meta from the provided item material before applying the data from the specified fields
	 * in the provided array. Returns null when given null data.
	 * @param c an associative CArray representation of item meta
	 * @param mat the item material from which to generate the item meta
	 * @param t
	 * @return abstract item meta
	 * @throws ConfigRuntimeException
	 */
	public MCItemMeta itemMeta(Mixed c, MCMaterial mat, Target t) throws ConfigRuntimeException {
		if(c instanceof CNull) {
			return null;
		}
		if(!c.isInstanceOf(CArray.TYPE)) {
			throw new CREFormatException("An array was expected but received " + c + " instead.", t);
		} else {
			MCItemFactory itemFactory = Static.getServer().getItemFactory();
			MCItemMeta meta = itemFactory.getItemMeta(mat);
			if(meta == null) {
				return null;
			}
			CArray ma = (CArray) c;
			if(!ma.isAssociative()) {
				return meta;
			}
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
						meta.setRepairCost(ArgumentValidation.getInt32(r, t));
					}
				}
				if(ma.containsKey("model")) {
					Mixed m = ma.get("model", t);
					if(!(m instanceof CNull)) {
						meta.setCustomModelData(ArgumentValidation.getInt32(m, t));
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
							modifierList.add(attributeModifier(ArgumentValidation.getArray(modifierArray.get(key, t), t), t));
						}
						meta.setAttributeModifiers(modifierList);
					} else {
						throw new CREFormatException("Attribute modifiers were expected to be an array.", t);
					}
				}

				if(ma.containsKey("tags")) {
					Mixed tagArray = ma.get("tags", t);
					if(tagArray instanceof CNull) {
						// no custom tags
					} else {
						MCTagType.TAG_CONTAINER.convert(meta.getCustomTags(), tagArray);
					}
				}

				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
					// Item data components
					if(ma.containsKey("maxstacksize")) {
						Mixed maxstacksize = ma.get("maxstacksize", t);
						if(maxstacksize instanceof CNull) {
							// not yet supported
						} else {
							meta.setMaxStackSize(ArgumentValidation.getInt32(maxstacksize, t));
						}
					}
					if(ma.containsKey("glint")) {
						Mixed glint = ma.get("glint", t);
						if(glint instanceof CNull) {
							// not yet supported
						} else {
							meta.setEnchantmentGlintOverride(ArgumentValidation.getBooleanObject(glint, t));
						}
					}
					if(ma.containsKey("maxdamage")) {
						Mixed maxdamage = ma.get("maxdamage", t);
						if(maxdamage instanceof CNull) {
							// not yet supported
						} else {
							meta.setMaxDamage(ArgumentValidation.getInt32(maxdamage, t));
						}
					}
					if(ma.containsKey("rarity")) {
						Mixed rarity = ma.get("rarity", t);
						if(rarity instanceof CNull) {
							// not yet supported
						} else {
							try {
								meta.setRarity(MCItemRarity.valueOf(rarity.val()));
							} catch (IllegalArgumentException ex) {
								throw new CREFormatException("Rarity does not exist: " + rarity.val(), t);
							}
						}
					}
					if(ma.containsKey("food")) {
						Mixed mixedFood = ma.get("food", t);
						if(mixedFood instanceof CNull) {
							// not supported yet
						} else if(mixedFood.isInstanceOf(CArray.TYPE)) {
							CArray foodArray = (CArray) mixedFood;
							if(!foodArray.isAssociative()) {
								throw new CREFormatException("Food array must be associative.", t);
							}
							MCFoodComponent food = meta.getFood();
							if(foodArray.containsKey("nutrition")) {
								food.setNutrition(ArgumentValidation.getInt32(foodArray.get("nutrition", t), t));
							}
							if(foodArray.containsKey("saturation")) {
								food.setSaturation(ArgumentValidation.getDouble32(foodArray.get("saturation", t), t));
							}
							if(foodArray.containsKey("always")) {
								food.setCanAlwaysEat(ArgumentValidation.getBooleanObject(foodArray.get("always", t), t));
							}
							meta.setFood(food);
						} else {
							throw new CREFormatException("Expected food to be an associative array.", t);
						}
					}
				}
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21)) {
					if(ma.containsKey("jukeboxsong")) {
						Mixed playable = ma.get("jukeboxsong", t);
						if(playable instanceof CNull) {
							// not yet supported
						} else {
							meta.setJukeboxPlayable(playable.val());
						}
					}
				}
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21_3)) {
					if(ma.containsKey("enchantability")) {
						Mixed enchantability = ma.get("enchantability", t);
						if(enchantability instanceof CNull) {
							// not yet supported
						} else {
							meta.setEnchantable(ArgumentValidation.getInt32(enchantability, t));
						}
					}
					if(ma.containsKey("glider")) {
						Mixed glider = ma.get("glider", t);
						if(glider instanceof CNull) {
							meta.setGlider(false);
						} else {
							meta.setGlider(ArgumentValidation.getBooleanObject(glider, t));
						}
					}
					if(ma.containsKey("remainder")) {
						Mixed remainder = ma.get("remainder", t);
						if(remainder instanceof CNull) {
							// not yet supported
						} else {
							meta.setUseRemainder(item(remainder, t));
						}
					}
					if(ma.containsKey("cooldown")) {
						Mixed mixedCooldown =  ma.get("cooldown", t);
						if(mixedCooldown instanceof CNull) {
							// not yet supported
						} else if(mixedCooldown.isInstanceOf(CArray.TYPE)) {
							CArray cooldownArray = (CArray) mixedCooldown;
							if(!cooldownArray.isAssociative()) {
								throw new CREFormatException("Cooldown array must be associative.", t);
							}
							MCCooldownComponent cooldown = meta.getUseCooldown();
							if(cooldownArray.containsKey("seconds")) {
								cooldown.setSeconds(ArgumentValidation.getDouble32(cooldownArray.get("seconds", t), t));
							}
							if(cooldownArray.containsKey("group")) {
								Mixed group = cooldownArray.get("group", t);
								if(!(group instanceof CNull)) {
									cooldown.setCooldownGroup(group.val());
								}
							}
							meta.setUseCooldown(cooldown);
						} else {
							throw new CREFormatException("Expected an array for item cooldown.", t);
						}
					}
					if(ma.containsKey("itemmodel")) {
						Mixed itemmodel = ma.get("itemmodel", t);
						if(itemmodel instanceof CNull) {
							// not yet supported
						} else {
							meta.setItemModel(itemmodel.val());
						}
					}
					if(ma.containsKey("tooltipstyle")) {
						Mixed tooltipstyle = ma.get("tooltipstyle", t);
						if(tooltipstyle instanceof CNull) {
							// not yet supported
						} else {
							meta.setTooltipStyle(tooltipstyle.val());
						}
					}
				}
				if(ma.containsKey("damage")) {
					Mixed damage = ma.get("damage", t);
					if(damage instanceof CNull) {
						// not yet supported
					} else {
						meta.setDamage(ArgumentValidation.getInt32(damage, t));
					}
				}
				if(ma.containsKey("unbreakable")) {
					meta.setUnbreakable(ArgumentValidation.getBooleanish(ma.get("unbreakable", t), t));
				}
				if(ma.containsKey("blockdata")) {
					Mixed mBlockData = ma.get("blockdata", t);
					if(mBlockData instanceof CArray) {
						meta.setBlockData(blockData((CArray) mBlockData, mat, t));
					}
				}

				// Specific ItemMeta
				if(meta instanceof MCBlockStateMeta) {
					MCBlockStateMeta bsm = (MCBlockStateMeta) meta;
					MCBlockState bs = bsm.getBlockState();
					if(bs instanceof MCBanner) {
						MCBanner banner = (MCBanner) bs;
						if(ma.containsKey("basecolor")) {
							String baseString = ma.get("basecolor", t).val().toUpperCase();
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
						}
					} else if(bs instanceof MCCreatureSpawner) {
						MCCreatureSpawner mccs = (MCCreatureSpawner) bs;
						if(ma.containsKey("spawntype")) {
							Mixed m = ma.get("spawntype", t);
							if(m != CNull.NULL) {
								MCEntityType type = MCEntityType.valueOf(m.val().toUpperCase());
								mccs.setSpawnedType(type);
							}
						}
						if(ma.containsKey("delay")) {
							int delay = ArgumentValidation.getInt32(ma.get("delay", t), t);
							mccs.setDelay(delay);
						}
						if(ma.containsKey("mindelay")) {
							int delay = ArgumentValidation.getInt32(ma.get("mindelay", t), t);
							mccs.setMinDelay(delay);
						}
						if(ma.containsKey("maxdelay")) {
							int delay = ArgumentValidation.getInt32(ma.get("maxdelay", t), t);
							mccs.setMaxDelay(delay);
						}
						if(ma.containsKey("spawncount")) {
							int count = ArgumentValidation.getInt32(ma.get("spawncount", t), t);
							mccs.setSpawnCount(count);
						}
						if(ma.containsKey("maxnearbyentities")) {
							int max = ArgumentValidation.getInt32(ma.get("maxnearbyentities", t), t);
							mccs.setMaxNearbyEntities(max);
						}
						if(ma.containsKey("spawnrange")) {
							int range = ArgumentValidation.getInt32(ma.get("spawnrange", t), t);
							mccs.setSpawnRange(range);
						}
						if(ma.containsKey("playerrange")) {
							int range = ArgumentValidation.getInt32(ma.get("playerrange", t), t);
							mccs.setPlayerRange(range);
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
					} else if(bs instanceof MCDecoratedPot decoratedPot) {
						if(ma.containsKey("sherds")) {
							Mixed sherds = ma.get("sherds", t);
							if(sherds.isInstanceOf(CArray.TYPE)) {
								CArray sherdArray = (CArray) sherds;
								if(!sherdArray.isAssociative()) {
									throw new CREFormatException("Expected associative array for decorated pot meta.", t);
								}
								for(String key : sherdArray.stringKeySet()) {
									decoratedPot.setSherd(MCDecoratedPot.Side.valueOf(key.toUpperCase()),
											MCMaterial.valueOf(sherdArray.get(key, t).val()));
								}
							} else {
								throw new CREFormatException("Expected associative array for decorated pot meta.", t);
							}
						}
						if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_4)) {
							if(ma.containsKey("item")) {
								decoratedPot.setItemStack(item(ma.get("item", t), t));
							}
						}
						bsm.setBlockState(bs);
					} else if(bs instanceof MCInventoryHolder) {
						// Finally, handle InventoryHolders with inventory slots that do not have a special meaning.
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
										inv.setItem(index, ObjectGenerator.GetGenerator().item(cinv.get(key, t), t));
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
					} else if(bs instanceof MCSign sign) {
						if(ma.containsKey("waxed")) {
							sign.setWaxed(ArgumentValidation.getBooleanObject(ma.get("waxed", t), t));
						}
						if(ma.containsKey("signtext")) {
							Mixed possibleLines = ma.get("signtext", t);
							if(possibleLines.isInstanceOf(CArray.TYPE)) {
								CArray lines = (CArray) possibleLines;
								for(int i = 0; i < lines.size(); i++) {
									sign.setLine(i, lines.get(i, t).val());
								}
							} else {
								throw new CREFormatException("Expected array for sign text", t);
							}
						}
						if(ma.containsKey("glowing")) {
							sign.setGlowingText(ArgumentValidation.getBooleanObject(ma.get("glowing", t), t));
						}
						if(ma.containsKey("color")) {
							Mixed dye = ma.get("color", t);
							if(!(dye instanceof CNull)) {
								sign.setDyeColor(MCDyeColor.valueOf(dye.val()));
							}
						}
						MCSignText backText = sign.getBackText();
						if(backText != null) {
							if(ma.containsKey("backtext")) {
								Mixed possibleLines = ma.get("backtext", t);
								if(possibleLines.isInstanceOf(CArray.TYPE)) {
									CArray lines = (CArray) possibleLines;
									for(int i = 0; i < lines.size(); i++) {
										backText.setLine(i, lines.get(i, t).val());
									}
								} else {
									throw new CREFormatException("Expected array for sign back text", t);
								}
							}
							if(ma.containsKey("backglowing")) {
								backText.setGlowingText(ArgumentValidation.getBooleanObject(ma.get("backglowing", t), t));
							}
							if(ma.containsKey("backcolor")) {
								Mixed dye = ma.get("backcolor", t);
								if(!(dye instanceof CNull)) {
									backText.setDyeColor(MCDyeColor.valueOf(dye.val()));
								}
							}
						}
						bsm.setBlockState(bs);
					} else if(bs instanceof MCCommandBlock cmdBlock) {
						if(ma.containsKey("command")) {
							cmdBlock.setCommand(ma.get("command", t).val());
						}
						if(ma.containsKey("customname")) {
							cmdBlock.setName(ma.get("customname", t).val());
						}
						bsm.setBlockState(bs);
					} else if(bs instanceof MCBeehive beehive
							&& Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
						if(ma.containsKey("beecount")) {
							int beeCount = ArgumentValidation.getInt32(ma.get("beecount", t), t);
							beehive.addBees(beeCount);
						}
						bsm.setBlockState(bs);
					}
				} else if(meta instanceof MCArmorMeta armorMeta) { // Must be before MCLeatherArmorMeta
					if(ma.containsKey("trim")) {
						Mixed mtrim = ma.get("trim", t);
						if(mtrim instanceof CNull) {
							// nothing
						} else if(mtrim.isInstanceOf(CArray.TYPE)) {
							CArray trim = (CArray) mtrim;
							if(!trim.isAssociative()) {
								throw new CREFormatException("Expected associative array for armor trim meta.", t);
							}
							MCTrimPattern pattern = MCTrimPattern.valueOf(trim.get("pattern", t).val());
							MCTrimMaterial material = MCTrimMaterial.valueOf(trim.get("material", t).val());
							armorMeta.setTrim(pattern, material);
						} else {
							throw new CREFormatException("Expected an array or null for armor trim meta.", t);
						}
					}
					if(armorMeta instanceof MCColorableArmorMeta) {
						if(ma.containsKey("color")) {
							Mixed ci = ma.get("color", t);
							if(ci instanceof CNull) {
								//nothing
							} else if(ci.isInstanceOf(CArray.TYPE)) {
								((MCColorableArmorMeta) armorMeta).setColor(color((CArray) ci, t));
							} else {
								throw new CREFormatException("Color was expected to be an array.", t);
							}
						}
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
								fmeta.setStrength(ArgumentValidation.getInt32(firework.get("strength", t), t));
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
				} else if(meta instanceof MCBookMeta bookMeta) {
					if(mat.getAbstracted() == MCVanillaMaterial.WRITTEN_BOOK) {
						// written books must have a title and author
						bookMeta.setTitle("");
						bookMeta.setAuthor("");
						if(ma.containsKey("title")) {
							Mixed title = ma.get("title", t);
							if(!(title instanceof CNull)) {
								bookMeta.setTitle(title.val());
							}
						}
						if(ma.containsKey("author")) {
							Mixed author = ma.get("author", t);
							if(!(author instanceof CNull)) {
								bookMeta.setAuthor(author.val());
							}
						}
						if(ma.containsKey("generation")) {
							Mixed generation = ma.get("generation", t);
							bookMeta.setGeneration(MCBookMeta.Generation.valueOf(generation.val()));
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
							bookMeta.setPages(pl);
						} else {
							throw new CREFormatException("Pages field was expected to be an array.", t);
						}
					}
				} else if(meta instanceof MCSkullMeta) {
					String name = null;
					UUID id = null;
					if(ma.containsKey("owner")) {
						name = Construct.nval(ma.get("owner", t));
					}
					if(ma.containsKey("owneruuid")) {
						Mixed uuid = ma.get("owneruuid", t);
						if(uuid instanceof CString) {
							id = Static.GetUUID(uuid, t);
						}
					}
					if(name != null && !name.isEmpty() || id != null) {
						MCPlayerProfile profile = Static.getServer().getPlayerProfile(id, name);
						if(profile != null) {
							if(ma.containsKey("texture")) {
								Mixed texture = ma.get("texture", t);
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
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_19_3)) {
						if(ma.containsKey("noteblocksound")) {
							Mixed sound = ma.get("noteblocksound", t);
							if(!(sound instanceof CNull)) {
								((MCSkullMeta) meta).setNoteBlockSound(sound.val());
							}
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
					if(ma.containsKey("potiontype") && Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_2)) {
						Mixed potiontype = ma.get("potiontype", t);
						if(!(potiontype instanceof CNull)) {
							((MCPotionMeta) meta).setBasePotionType(MCPotionType.valueOf(potiontype.val()));
						}
					} else if(ma.containsKey("base")) {
						Mixed potiondata = ma.get("base", t);
						if(potiondata.isInstanceOf(CArray.TYPE)) {
							if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
								((MCPotionMeta) meta).setBasePotionType(legacyPotionData((CArray) potiondata, t));
							} else {
								((MCPotionMeta) meta).setBasePotionData(potionData((CArray) potiondata, t));
							}
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
				} else if(meta instanceof MCSuspiciousStewMeta) {
					if(ma.containsKey("potions")) {
						Mixed effects = ma.get("potions", t);
						if(effects.isInstanceOf(CArray.TYPE)) {
							for(MCLivingEntity.MCEffect e : potions((CArray) effects, t)) {
								((MCSuspiciousStewMeta) meta).addCustomEffect(e.getPotionEffectType(), e.getStrength(),
										e.getTicksRemaining(), e.isAmbient(), e.hasParticles(), e.showIcon(), true, t);
							}
						} else {
							throw new CREFormatException("Expected an array of potion arrays.", t);
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
							((MCMapMeta) meta).setMapId(ArgumentValidation.getInt32(cid, t));
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
							for(Mixed m : ArgumentValidation.getArray(value, t).asList()) {
								projectiles.add(item(m, t));
							}
							((MCCrossbowMeta) meta).setChargedProjectiles(projectiles);
						}
					}
				} else if(meta instanceof MCCompassMeta) {
					if(ma.containsKey("target")) {
						Mixed loc = ma.get("target", t);
						if(!(loc instanceof CNull)) {
							((MCCompassMeta) meta).setTargetLocation(location(loc, null, t));
						}
					}
					if(ma.containsKey("lodestone")) {
						((MCCompassMeta) meta).setLodestoneTracked(
								ArgumentValidation.getBooleanObject(ma.get("lodestone", t), t));
					}
				} else if(meta instanceof MCBundleMeta) {
					if(ma.containsKey("items")) {
						Mixed value = ma.get("items", t);
						if(value instanceof CArray) {
							MCBundleMeta bm = (MCBundleMeta) meta;
							CArray items = (CArray) value;
							for(String key : items.stringKeySet()) {
								Mixed entry = items.get(key, t);
								if(!(entry instanceof CNull)) {
									bm.addItem(ObjectGenerator.GetGenerator().item(entry, t));
								}
							}
						} else if(!(value instanceof CNull)) {
							throw new CREFormatException("Items was expected to be an array or null.", t);
						}
					}
				} else if(meta instanceof MCAxolotlBucketMeta) {
					if(ma.containsKey("variant")) {
						Mixed value = ma.get("variant", t);
						if(!(value instanceof CNull)) {
							((MCAxolotlBucketMeta) meta).setAxolotlType(MCAxolotlType.valueOf(value.val().toUpperCase()));
						}
					}
				} else if(meta instanceof MCMusicInstrumentMeta) {
					if(ma.containsKey("instrument")) {
						Mixed value = ma.get("instrument", t);
						if(!(value instanceof CNull)) {
							((MCMusicInstrumentMeta) meta).setInstrument(value.val());
						}
					}
				} else if(meta instanceof MCKnowledgeBookMeta knowledgeBookMeta) {
					if(ma.containsKey("recipes")) {
						Mixed value = ma.get("recipes", t);
						if(value.isInstanceOf(CArray.TYPE)) {
							CArray array = ((CArray) value);
							List<MCNamespacedKey> keys = new ArrayList<>((int) array.size());
							for(Mixed entry : ((CArray) value).asList()) {
								keys.add(StaticLayer.GetConvertor().GetNamespacedKey(entry.val()));
							}
							knowledgeBookMeta.setRecipes(keys);
						} else if(!(value instanceof CNull)) {
							throw new CREFormatException("Expected array or null for recipes but got " + value.val(), t);
						}
					}
				} else if(meta instanceof MCOminousBottleMeta) {
					if(ma.containsKey("ominousamplifier")) {
						Mixed value = ma.get("ominousamplifier", t);
						if(!(value instanceof CNull)) {
							((MCOminousBottleMeta) meta).setAmplifier(ArgumentValidation.getInt32(value, t));
						}
					}
				}
			} catch (Exception ex) {
				throw new CREFormatException(ex.getMessage(), t, ex);
			}
			return meta;
		}
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
	 * priority) array(r: 0, g: 0, b: 0) array(red: 0, green: 0, blue: 0) array(0, 0, 0).
	 * Optionally accepts an alpha channel for the keys: 'a', 'alpha', and 3 respectively.
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
			red = ArgumentValidation.getInt32(color.get("r", t), t);
		} else if(color.containsKey("red")) {
			red = ArgumentValidation.getInt32(color.get("red", t), t);
		} else {
			red = ArgumentValidation.getInt32(color.get(0, t), t);
		}
		if(color.containsKey("g")) {
			green = ArgumentValidation.getInt32(color.get("g", t), t);
		} else if(color.containsKey("green")) {
			green = ArgumentValidation.getInt32(color.get("green", t), t);
		} else {
			green = ArgumentValidation.getInt32(color.get(1, t), t);
		}
		if(color.containsKey("b")) {
			blue = ArgumentValidation.getInt32(color.get("b", t), t);
		} else if(color.containsKey("blue")) {
			blue = ArgumentValidation.getInt32(color.get("blue", t), t);
		} else {
			blue = ArgumentValidation.getInt32(color.get(2, t), t);
		}
		if(color.size() > 3) {
			int alpha;
			if(color.containsKey("a")) {
				alpha = ArgumentValidation.getInt32(color.get("a", t), t);
			} else if(color.containsKey("alpha")) {
				alpha = ArgumentValidation.getInt32(color.get("alpha", t), t);
			} else {
				alpha = ArgumentValidation.getInt32(color.get(3, t), t);
			}
			try {
				return StaticLayer.GetConvertor().GetColor(red, green, blue, alpha);
			} catch (IllegalArgumentException ex) {
				throw new CRERangeException(ex.getMessage(), t, ex);
			}
		}
		try {
			return StaticLayer.GetConvertor().GetColor(red, green, blue);
		} catch (IllegalArgumentException ex) {
			throw new CRERangeException(ex.getMessage(), t, ex);
		}
	}

	/**
	 * Returns a CArray with an alpha channel from a given MCColor. It will be in the format array(a: 0, r: 0, g: 0, b: 0)
	 *
	 * @param color
	 * @param t
	 * @return
	 */
	public CArray transparentColor(MCColor color, Target t) {
		CArray ca = CArray.GetAssociativeArray(t);
		ca.set("r", new CInt(color.getRed(), t), t);
		ca.set("g", new CInt(color.getGreen(), t), t);
		ca.set("b", new CInt(color.getBlue(), t), t);
		ca.set("a", new CInt(color.getAlpha(), t), t);
		return ca;
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
					x = ArgumentValidation.getNumber(va.get(0, t), t);
					y = ArgumentValidation.getNumber(va.get(1, t), t);
					z = ArgumentValidation.getNumber(va.get(2, t), t);
				} else if(va.size() == 2) { // 2nd dimension vector
					x = ArgumentValidation.getNumber(va.get(0, t), t);
					y = ArgumentValidation.getNumber(va.get(1, t), t);
				} else if(va.size() == 1) {
					x = ArgumentValidation.getNumber(va.get(0, t), t);
				}
			} else {
				if(va.containsKey("x")) {
					x = ArgumentValidation.getNumber(va.get("x", t), t);
				}
				if(va.containsKey("y")) {
					y = ArgumentValidation.getNumber(va.get("y", t), t);
				}
				if(va.containsKey("z")) {
					z = ArgumentValidation.getNumber(va.get("z", t), t);
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
			enchant.set("elevel", new CInt(entry.getValue(), t), t);
			ret.set(entry.getKey().name().toLowerCase(), enchant, t);
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
				try {
					etype = MCEnchantment.valueOf(key.toUpperCase());
					if(value.isInstanceOf(CInt.TYPE)) {
						ret.put(etype, ArgumentValidation.getInt32(value, t));
						continue;
					}
				} catch(IllegalArgumentException ex) {
					throw new CREEnchantmentException("Unknown enchantment type: " + key, t);
				}
			}

			// legacy format
			if(value.isInstanceOf(CArray.TYPE)) {
				CArray ea = (CArray) value;
				if(etype == null) {
					String setype = ea.get("etype", t).val();
					etype = MCEnchantment.valueOf(setype);
					if(etype == null) {
						throw new CREEnchantmentException("Unknown enchantment type: " + setype, t);
					}
				}
				elevel = ArgumentValidation.getInt32(ea.get("elevel", t), t);
				ret.put(etype, elevel);
			}
		}
		return ret;
	}

	public CArray attributeModifier(MCAttributeModifier m, Target t) {
		CArray modifier = CArray.GetAssociativeArray(t);

		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21)) {
			modifier.set("id", m.getKey().toString());
		} else {
			modifier.set("name", m.getAttributeName());
			modifier.set("uuid", m.getUniqueId().toString());
		}

		modifier.set("attribute", m.getAttribute().name());
		modifier.set("operation", m.getOperation().name());
		modifier.set("amount", new CDouble(m.getAmount(), t), t);

		Mixed slot = CNull.NULL;
		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
			MCEquipmentSlotGroup equipmentSlotGroup = m.getEquipmentSlotGroup();
			if(equipmentSlotGroup != null) {
				slot = new CString(equipmentSlotGroup.name(), t);
			}
		}
		if(slot == CNull.NULL) {
			MCEquipmentSlot equipmentSlot = m.getEquipmentSlot();
			if(equipmentSlot != null) {
				slot = new CString(equipmentSlot.name(), t);
			}
		}
		modifier.set("slot", slot, t);
		return modifier;
	}

	public MCAttributeModifier attributeModifier(CArray m, Target t) {
		if(!m.isAssociative()) {
			throw new CREFormatException("Attribute modifier array must be associative.", t);
		}

		MCAttribute attribute;
		MCAttributeModifier.Operation operation;
		double amount;
		MCNamespacedKey id = null;
		UUID uuid = null;
		String name = "";
		MCEquipmentSlotGroup slotGroup = null;
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

		amount = ArgumentValidation.getDouble(m.get("amount", t), t);

		if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_21) && m.containsKey("id")) {
			id = StaticLayer.GetConvertor().GetNamespacedKey(m.get("id", t).val());
		} else {
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
		}

		if(m.containsKey("slot")) {
			Mixed s = m.get("slot", t);
			if(!(s instanceof CNull)) {
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_6)) {
					// check new slots groups first
					slotGroup = switch(s.val()) {
						case "ANY" -> MCEquipmentSlotGroup.ANY;
						case "HAND" -> MCEquipmentSlotGroup.HAND;
						case "ARMOR" -> MCEquipmentSlotGroup.ARMOR;
						case "BODY" -> MCEquipmentSlotGroup.BODY;
						case "SADDLE" -> MCEquipmentSlotGroup.SADDLE;
						default -> null;
					};
				}
				if(slotGroup == null) {
					try {
						slot = MCEquipmentSlot.valueOf(s.val());
					} catch (IllegalArgumentException ex) {
						throw new CREFormatException("Invalid equipment slot name: " + m.get("slot", t), t);
					}
				}
			}
		}

		if(slotGroup != null) {
			if(id != null) {
				return StaticLayer.GetConvertor().GetAttributeModifier(attribute, id, amount, operation, slotGroup);
			}
			return StaticLayer.GetConvertor().GetAttributeModifier(attribute, uuid, name, amount, operation, slotGroup);
		}
		if(id != null) {
			return StaticLayer.GetConvertor().GetAttributeModifier(attribute, id, amount, operation, slot);
		}
		return StaticLayer.GetConvertor().GetAttributeModifier(attribute, uuid, name, amount, operation, slot);
	}

	public CArray potion(MCLivingEntity.MCEffect eff, Target t) {
		CArray effect = CArray.GetAssociativeArray(t);
		effect.set("id", new CInt(eff.getPotionEffectType().getId(), t), t);
		effect.set("strength", new CInt(eff.getStrength(), t), t);
		effect.set("seconds", new CDouble(eff.getTicksRemaining() / 20.0, t), t);
		effect.set("ambient", CBoolean.get(eff.isAmbient()), t);
		effect.set("particles", CBoolean.get(eff.hasParticles()), t);
		effect.set("icon", CBoolean.get(eff.showIcon()), t);

		return effect;
	}

	public CArray potions(List<MCLivingEntity.MCEffect> effectList, Target t) {
		CArray ea = CArray.GetAssociativeArray(t);
		for(MCLivingEntity.MCEffect eff : effectList) {
			CArray effect = potion(eff, t);
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
						type = MCPotionEffectType.getById(ArgumentValidation.getInt32(effect.get("id", t), t));
					} else {
						throw new CREFormatException("No potion type was given.", t);
					}
				} catch (IllegalArgumentException ex) {
					throw new CREFormatException(ex.getMessage(), t);
				}

				if(effect.containsKey("strength")) {
					strength = ArgumentValidation.getInt32(effect.get("strength", t), t);
				}
				if(effect.containsKey("seconds")) {
					seconds = ArgumentValidation.getDouble(effect.get("seconds", t), t);
					if(seconds * 20 > Integer.MAX_VALUE) {
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

	public MCPotionType legacyPotionData(CArray potionArray, Target t) {
		MCPotionType type;
		try {
			// need to get converted type first before extending/upgrading
			type = MCPotionType.valueOf(potionArray.get("type", t).val().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new CREFormatException("Invalid potion type: " + potionArray.get("type", t).val(), t);
		}
		if(type == null) {
			return null;
		}
		boolean extended = false;
		boolean upgraded = false;
		if(potionArray.containsKey("extended")) {
			Mixed cext = potionArray.get("extended", t);
			if(cext.isInstanceOf(CBoolean.TYPE)) {
				extended = ((CBoolean) cext).getBoolean();
			}
		}
		if(potionArray.containsKey("upgraded")) {
			Mixed cupg = potionArray.get("upgraded", t);
			if(cupg.isInstanceOf(CBoolean.TYPE)) {
				upgraded = ((CBoolean) cupg).getBoolean();
			}
		}
		if(extended) {
			try {
				type = MCPotionType.valueOf("LONG_" + type.name());
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException("Could not find extended potion type for: "
						+ potionArray.get("type", t).val(), t);
			}
		} else if(upgraded) {
			try {
				type = MCPotionType.valueOf("STRONG_" + type.name());
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException("Could not find upgraded potion type for: "
						+ potionArray.get("type", t).val(), t);
			}
		}
		return type;
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
				String[] split = colors.val().split("\\|");
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
		if(r instanceof MCCookingRecipe recipe) {
			ret.set("input", recipeChoice(recipe.getInput()), t);
			ret.set("experience", new CDouble(recipe.getExperience(), t), t);
			ret.set("cookingtime", new CInt(recipe.getCookingTime(), t), t);
		} else if(r instanceof MCShapelessRecipe shapeless) {
			CArray il = new CArray(t);
			for(MCRecipeChoice choice : shapeless.getIngredients()) {
				il.push(recipeChoice(choice), t);
			}
			ret.set("ingredients", il, t);
		} else if(r instanceof MCShapedRecipe shaped) {
			CArray shape = new CArray(t);
			for(String line : shaped.getShape()) {
				shape.push(new CString(line, t), t);
			}
			ret.set("shape", shape, t);
			CArray imap = CArray.GetAssociativeArray(t);
			for(Map.Entry<Character, MCRecipeChoice> entry : shaped.getIngredientMap().entrySet()) {
				if(entry.getValue() == null) {
					imap.set(entry.getKey().toString(), CNull.NULL, t);
				} else {
					imap.set(entry.getKey().toString(), recipeChoice(entry.getValue()), t);
				}
			}
			ret.set("ingredients", imap, t);
		} else if(r instanceof MCStonecuttingRecipe recipe) {
			ret.set("input", recipeChoice(recipe.getInput()), t);
		} else if(r instanceof MCSmithingRecipe recipe) {
			MCMaterial[] base = recipe.getBase();
			if(base.length == 1) {
				ret.set("base", new CString(base[0].getName(), t), t);
			} else {
				CArray mats = new CArray(t);
				for(MCMaterial mat : base) {
					mats.push(new CString(mat.getName(), t), t);
				}
				ret.set("base", mats, t);
			}
			MCMaterial[] additions = recipe.getAddition();
			if(additions.length == 1) {
				ret.set("addition", new CString(additions[0].getName(), t), t);
			} else {
				CArray mats = new CArray(t);
				for(MCMaterial mat : additions) {
					mats.push(new CString(mat.getName(), t), t);
				}
				ret.set("addition", mats, t);
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
				CArray shaped = ArgumentValidation.getArray(recipe.get("shape", t), t);
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

				CArray shapedIngredients = ArgumentValidation.getArray(recipe.get("ingredients", t), t);
				if(!shapedIngredients.inAssociativeMode()) {
					throw new CREIllegalArgumentException("Ingredients array is invalid.", t);
				}
				for(String key : shapedIngredients.stringKeySet()) {
					Mixed ingredient = shapedIngredients.get(key, t);
					if(ingredient.isInstanceOf(CArray.TYPE)) {
						if(((CArray) ingredient).isAssociative()) {
							// Single exact item ingredient
							((MCShapedRecipe) ret).setIngredient(key.charAt(0), recipeItem(ingredient, t));
						} else {
							// Multiple ingredient choices
							((MCShapedRecipe) ret).setIngredient(key.charAt(0), recipeChoice((CArray) ingredient, t));
						}
					} else if(ingredient instanceof CNull) {
						// empty
					} else {
						((MCShapedRecipe) ret).setIngredient(key.charAt(0), recipeMaterial(ingredient, t));
					}
				}
				return ret;

			case SHAPELESS:
				CArray ingredients = ArgumentValidation.getArray(recipe.get("ingredients", t), t);
				if(ingredients.inAssociativeMode()) {
					throw new CREIllegalArgumentException("Ingredients array is invalid.", t);
				}
				for(Mixed ingredient : ingredients.asList()) {
					if(ingredient.isInstanceOf(CArray.TYPE)) {
						if(((CArray) ingredient).isAssociative()) {
							// Single ingredient item array
							if(((CArray) ingredient).containsKey("meta")) {
								// Single exact ingredient item choice
								MCRecipeChoice.ExactChoice exactChoice = new MCRecipeChoice.ExactChoice();
								exactChoice.addItem(recipeItem(ingredient, t));
								((MCShapelessRecipe) ret).addIngredient(exactChoice);
							} else {
								// Single ingredient material with optional qty
								MCItemStack stack = recipeItem(ingredient, t);
								((MCShapelessRecipe) ret).addIngredient(stack.getType(), stack.getAmount());
							}
						} else {
							// Multiple ingredient choices
							((MCShapelessRecipe) ret).addIngredient(recipeChoice((CArray) ingredient, t));
						}
					} else {
						// single ingredient material
						((MCShapelessRecipe) ret).addIngredient(recipeMaterial(ingredient, t));
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
						((MCCookingRecipe) ret).setInput(recipeItem(input, t));
					} else {
						((MCCookingRecipe) ret).setInput(recipeChoice((CArray) input, t));
					}
				} else {
					((MCCookingRecipe) ret).setInput(recipeMaterial(input, t));
				}
				if(recipe.containsKey("experience")) {
					((MCCookingRecipe) ret).setExperience(ArgumentValidation.getDouble32(recipe.get("experience", t), t));
				}
				if(recipe.containsKey("cookingtime")) {
					((MCCookingRecipe) ret).setCookingTime(ArgumentValidation.getInt32(recipe.get("cookingtime", t), t));
				}
				return ret;

			case STONECUTTING:
				Mixed stoneCutterInput = recipe.get("input", t);
				if(stoneCutterInput.isInstanceOf(CArray.TYPE)) {
					if(((CArray) stoneCutterInput).isAssociative()) {
						((MCStonecuttingRecipe) ret).setInput(recipeItem(stoneCutterInput, t));
					} else {
						((MCStonecuttingRecipe) ret).setInput(recipeChoice((CArray) stoneCutterInput, t));
					}
				} else {
					((MCStonecuttingRecipe) ret).setInput(recipeMaterial(stoneCutterInput, t));
				}
				return ret;

			default:
				throw new CREIllegalArgumentException("Could not find valid recipe type.", t);
		}
	}

	/**
	 * Returns a recipe ingredient material from a material name,
	 * or throws an exception if invalid. Ingredient material must exist and cannot be air.
	 *
	 * @param arg material name
	 * @param t
	 * @return
	 * @throws CREIllegalArgumentException
	 */
	private MCMaterial recipeMaterial(Mixed arg, Target t) {
		MCMaterial mat = StaticLayer.GetMaterial(arg.val());
		if(mat == null || mat.isAir() || !mat.isItem()) {
			throw new CREIllegalArgumentException("Recipe input ingredient is invalid: " + arg.val(), t);
		}
		return mat;
	}

	/**
	 * Returns an MCRecipeChoice from an array of exact items or material names,
	 * or throws an exception if invalid. Item array must not be empty.
	 * Ingredient material must exist and cannot be air.
	 *
	 * @param ingredient a CArray of material names or item arrays
	 * @param t
	 * @return
	 * @throws CREIllegalArgumentException
	 */
	private MCRecipeChoice recipeChoice(CArray ingredient, Target t) {
		// Multiple ingredient choices
		MCRecipeChoice.MaterialChoice materialChoice = new MCRecipeChoice.MaterialChoice();
		MCRecipeChoice.ExactChoice exactChoice = new MCRecipeChoice.ExactChoice();
		for(Mixed choice : ingredient.asList()) {
			if(choice.isInstanceOf(CArray.TYPE)) {
				exactChoice.addItem(recipeItem(choice, t));
			} else {
				materialChoice.addMaterial(recipeMaterial(choice, t));
			}
		}
		if(!exactChoice.getItems().isEmpty()) {
			// Multiple exact item ingredient choices
			for(MCMaterial mat : materialChoice.getMaterials()) {
				exactChoice.addItem(StaticLayer.GetItemStack(mat, 1));
			}
			return exactChoice;
		}
		// Multiple material ingredient choices
		return materialChoice;
	}

	/**
	 * Returns a Mixed representation of a recipe choice.
	 *
	 * @param ingredient
	 * @return
	 */
	private Mixed recipeChoice(MCRecipeChoice ingredient) {
		if(ingredient instanceof MCRecipeChoice.MaterialChoice materialChoice) {
			if(materialChoice.getMaterials().size() == 1) {
				return new CString(materialChoice.getMaterials().get(0).getName(), Target.UNKNOWN);
			} else {
				CArray materialArray = new CArray(Target.UNKNOWN);
				for(MCMaterial mat : materialChoice.getMaterials()) {
					materialArray.push(new CString(mat.getName(), Target.UNKNOWN), Target.UNKNOWN);
				}
				return materialArray;
			}
		} else if(ingredient instanceof MCRecipeChoice.ExactChoice exactChoice) {
			if(exactChoice.getItems().size() == 1) {
				return item(exactChoice.getItems().get(0), Target.UNKNOWN);
			} else {
				CArray itemArray = new CArray(Target.UNKNOWN);
				for(MCItemStack itemStack : exactChoice.getItems()) {
					itemArray.push(item(itemStack, Target.UNKNOWN), Target.UNKNOWN);
				}
				return itemArray;
			}
		} else {
			throw new RuntimeException("Invalid MCRecipeChoice type");
		}
	}

	/**
	 * Returns a recipe ingredient item stack, or throws an exception if invalid.
	 * Argument cannot be null; the material must exist and cannot be air; and quantity cannot be zero.
	 *
	 * @param arg
	 * @param t
	 * @return
	 * @throws CREIllegalArgumentException
	 */
	private MCItemStack recipeItem(Mixed arg, Target t) {
		MCItemStack item = item(arg, t);
		if(item.isEmpty()) {
			throw new CREIllegalArgumentException("Recipe input ingredient is invalid: " + arg.val(), t);
		}
		return item;
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
		return blockData(ca, null, t);
	}

	public MCBlockData blockData(CArray ca, MCMaterial blockType, Target t) {
		StringBuilder b = new StringBuilder().append("[");
		boolean first = true;
		String block = null;
		for(String key : ca.stringKeySet()) {
			if(key.equals("block")) {
				block = ca.get("block", t).val();
				if(Character.isUpperCase(block.charAt(0))) {
					// support material enum input
					block = block.toLowerCase();
				}
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
		if(block == null) {
			if(blockType == null) {
				throw new CREFormatException("Missing block type for block data.", t);
			}
			block = blockType.name().toLowerCase();
		}
		b.insert(0, block);
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

	public CArray blockData(MCMaterial mat, Map<String, String> blockData, Target t) {
		CArray ca = CArray.GetAssociativeArray(t);
		ca.set("block", new CString(mat.getName().toLowerCase(Locale.ROOT), t), t);
		for(Entry<String, String> entry : blockData.entrySet()) {
			ca.set(entry.getKey(), blockState(entry.getValue()), t);
		}
		return ca;
	}

	private Construct blockState(String value) {
		int ch = value.charAt(0);
		if(ch >= '0' && ch <= '9') {
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

	public Object particleData(MCParticle particleType, MCLocation l, CArray pa, Target t) {
		switch(particleType.getAbstracted()) {
			case BLOCK_DUST:
			case BLOCK_CRACK:
			case BLOCK_CRUMBLE:
			case BLOCK_MARKER:
			case DUST_PILLAR:
			case FALLING_DUST:
				if(pa.containsKey("block")) {
					String value = pa.get("block", t).val();
					MCMaterial mat = StaticLayer.GetMaterial(value);
					if(mat != null) {
						try {
							return mat.createBlockData();
						} catch (IllegalArgumentException ex) {
							throw new CREIllegalArgumentException(value + " is not a block.", t);
						}
					} else {
						throw new CREIllegalArgumentException("Could not find material from " + value, t);
					}
				}
				break;
			case ITEM_CRACK:
				if(pa.containsKey("item")) {
					Mixed value = pa.get("item", t);
					if(value.isInstanceOf(CArray.TYPE)) {
						return item(pa.get("item", t), t);
					} else {
						MCMaterial mat = StaticLayer.GetMaterial(value.val());
						if(mat != null) {
							if(mat.isItem()) {
								return StaticLayer.GetItemStack(mat, 1);
							} else {
								throw new CREIllegalArgumentException(value + " is not an item type.", t);
							}
						} else {
							throw new CREIllegalArgumentException("Could not find material from " + value, t);
						}
					}
				}
				break;
			case SPELL_MOB:
			case REDSTONE:
			case FLASH:
				if(pa.containsKey("color")) {
					Mixed c = pa.get("color", t);
					if(c.isInstanceOf(CArray.TYPE)) {
						return color((CArray) c, t);
					} else {
						return StaticLayer.GetConvertor().GetColor(c.val(), t);
					}
				}
				break;
			case DRAGON_BREATH:
				if(pa.containsKey("power")) {
					Mixed d = pa.get("power", t);
					if(d.isInstanceOf(CDouble.TYPE)) {
						return (float) ((CDouble) d).getDouble();
					} else if(!(d instanceof CNull)) {
						throw new CREIllegalArgumentException("Expected double for power but found " + d, t);
					}
				}
				break;
			case SPELL:
			case SPELL_INSTANT:
				MCColor spellColor = MCColor.WHITE;
				if(pa.containsKey("color")) {
					Mixed c = pa.get("color", t);
					if(c.isInstanceOf(CArray.TYPE)) {
						spellColor = color((CArray) c, t);
					} else {
						spellColor = StaticLayer.GetConvertor().GetColor(c.val(), t);
					}
				}
				float power = 1.0F;
				if(pa.containsKey("power")) {
					Mixed d = pa.get("power", t);
					if(d.isInstanceOf(CDouble.TYPE)) {
						power = (float) ((CDouble) d).getDouble();
					} else if(!(d instanceof CNull)) {
						throw new CREIllegalArgumentException("Expected double for power but found " + d, t);
					}
				}
				return new MCParticleData.Spell(spellColor, power);
			case DUST_COLOR_TRANSITION:
				if(pa.containsKey("color")) {
					Mixed c = pa.get("color", t);
					MCColor fromColor;
					if(c.isInstanceOf(CArray.TYPE)) {
						fromColor = color((CArray) c, t);
					} else {
						fromColor = StaticLayer.GetConvertor().GetColor(c.val(), t);
					}
					if(pa.containsKey("tocolor")) {
						Mixed sc = pa.get("tocolor", t);
						if(sc.isInstanceOf(CArray.TYPE)) {
							return new MCParticleData.DustTransition(fromColor, color((CArray) sc, t));
						} else {
							return new MCParticleData.DustTransition(fromColor,
									StaticLayer.GetConvertor().GetColor(sc.val(), t));
						}
					}
					return new MCParticleData.DustTransition(fromColor, MCColor.WHITE);
				} else if(pa.containsKey("tocolor")) {
					Mixed sc = pa.get("tocolor", t);
					if(sc.isInstanceOf(CArray.TYPE)) {
						return new MCParticleData.DustTransition(MCColor.WHITE, color((CArray) sc, t));
					} else {
						return new MCParticleData.DustTransition(MCColor.WHITE,
								StaticLayer.GetConvertor().GetColor(sc.val(), t));
					}
				}
				break;
			case VIBRATION:
				if(pa.containsKey("destination")) {
					Mixed d = pa.get("destination", t);
					if(d.isInstanceOf(CArray.TYPE)) {
						return new MCParticleData.VibrationBlockDestination(location(d, l.getWorld(), t), 5);
					} else {
						return new MCParticleData.VibrationEntityDestination(Static.getEntity(d, t), 5);
					}
				}
				break;
			case SHRIEK:
				if(pa.containsKey("delay")) {
					Mixed d = pa.get("delay", t);
					if(d.isInstanceOf(CInt.TYPE)) {
						return (int) ((CInt) d).getInt();
					} else if(!(d instanceof CNull)) {
						throw new CREIllegalArgumentException("Expected integer for delay but found " + d, t);
					}
				}
				break;
			case SCULK_CHARGE:
				if(pa.containsKey("angle")) {
					Mixed d = pa.get("angle", t);
					if(d.isInstanceOf(CDouble.TYPE)) {
						return (float) ((CDouble) d).getDouble();
					} else if(!(d instanceof CNull)) {
						throw new CREIllegalArgumentException("Expected double for angle but found " + d, t);
					}
				}
				break;
			case TRAIL:
				MCLocation target = l;
				if(pa.containsKey("target")) {
					Mixed d = pa.get("target", t);
					if(d.isInstanceOf(CArray.TYPE)) {
						target = location(d, l.getWorld(), t);
					}
				}
				MCColor color;
				if(pa.containsKey("color")) {
					Mixed c = pa.get("color", t);
					if(c.isInstanceOf(CArray.TYPE)) {
						color = color((CArray) c, t);
					} else {
						color = StaticLayer.GetConvertor().GetColor(c.val(), t);
					}
				} else {
					// Default colors are 0xFC,0x78,0x12 (orange) and 0x5F,0x5F,0x5F (gray)
					// when moving towards or away from the Creaking, respectively.
					color = StaticLayer.GetConvertor().GetColor(0xFC, 0x78, 0x12);
				}
				int duration;
				if(pa.containsKey("duration")) {
					duration = ArgumentValidation.getInt32(pa.get("duration", t), t);
				} else {
					// Default duration is a random value from 0.5 to 2.5 seconds
					duration = new Random().nextInt(40) + 10;
				}
				return new MCParticleData.Trail(target, color, duration);
		}
		return null;
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

	/**
	 * Return match result in MethodScript variable value presentation
	 * 
	 * @param matchResult match result
	 * @param t the target
	 * @return match array
	 */
	public CArray regMatchValue(MatchResult matchResult, Target t) {
		CArray ret = CArray.GetAssociativeArray(t);
		ret.set(0, new CString(matchResult.group(0), t), t);
		for(int i = 1; i <= matchResult.groupCount(); i++) {
			if(matchResult.group(i) == null) {
				ret.set(i, CNull.NULL, t);
			} else {
				ret.set(i, new CString(matchResult.group(i), t), t);
			}
		}

		return ret;
	}
}
