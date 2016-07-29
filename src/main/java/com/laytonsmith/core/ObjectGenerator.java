package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCBannerMeta;
import com.laytonsmith.abstraction.MCBookMeta;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCEnchantmentStorageMeta;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.MCFireworkEffectMeta;
import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.MCFurnaceRecipe;
import com.laytonsmith.abstraction.MCItemFactory;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLeatherArmorMeta;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCPotionMeta;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCShapedRecipe;
import com.laytonsmith.abstraction.MCShapelessRecipe;
import com.laytonsmith.abstraction.MCSkullMeta;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.MCItemFlag;
import com.laytonsmith.abstraction.enums.MCPatternShape;
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
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This file is responsible for converting CH objects into server objects, and
 * vice versa
 *
 *
 */
public class ObjectGenerator {

    private static ObjectGenerator pog = null;

    public static ObjectGenerator GetGenerator() {
        if (pog == null) {
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
        CArray ca = CArray.GetAssociativeArray(Target.UNKNOWN);
        Construct x = new CDouble(l.getX(), Target.UNKNOWN);
        Construct y = new CDouble(l.getY(), Target.UNKNOWN);
        Construct z = new CDouble(l.getZ(), Target.UNKNOWN);
        Construct world = new CString(l.getWorld().getName(), Target.UNKNOWN);
		float yawRaw = l.getYaw();
		if (yawRaw < 0) {
			yawRaw = (((yawRaw) % 360) + 360);
		}
        Construct yaw = new CDouble(yawRaw, Target.UNKNOWN);
        Construct pitch = new CDouble(l.getPitch(), Target.UNKNOWN);
        ca.set("0", x, Target.UNKNOWN);
        ca.set("1", y, Target.UNKNOWN);
        ca.set("2", z, Target.UNKNOWN);
        ca.set("3", world, Target.UNKNOWN);
        ca.set("4", yaw, Target.UNKNOWN);
        ca.set("5", pitch, Target.UNKNOWN);
        ca.set("x", x, Target.UNKNOWN);
        ca.set("y", y, Target.UNKNOWN);
        ca.set("z", z, Target.UNKNOWN);
        ca.set("world", world, Target.UNKNOWN);
        ca.set("yaw", yaw, Target.UNKNOWN);
        ca.set("pitch", pitch, Target.UNKNOWN);
        return ca;
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
		if (includeYawAndPitch) {
			Construct yaw = new CDouble(l.getYaw(), Target.UNKNOWN);
			Construct pitch = new CDouble(l.getPitch(), Target.UNKNOWN);
			ca.set("4", yaw, Target.UNKNOWN);
			ca.set("5", pitch, Target.UNKNOWN);
			ca.set("yaw", yaw, Target.UNKNOWN);
			ca.set("pitch", pitch, Target.UNKNOWN);
		}
		return ca;
	}

    /**
     * Given a Location Object, returns a MCLocation. If the optional world is
     * not specified in the object, the world provided is used instead. Location
     * "objects" are MethodScript arrays that represent a location in game. There are
     * 4 usages: <ul> <li>(x, y, z)</li> <li>(x, y, z, world)</li> <li>(x, y, z,
     * yaw, pitch)</li> <li>(x, y, z, world, yaw, pitch)</li> </ul> In all
     * cases, the pitch and yaw default to 0, and the world defaults to the
     * specified world. <em>More conveniently: ([world], x, y, z, [yaw,
     * pitch])</em>
     */
    public MCLocation location(Construct c, MCWorld w, Target t) {
        if (!(c instanceof CArray)) {
            throw new CREFormatException("Expecting an array, received " + c.getCType(), t);
        }
        CArray array = (CArray) c;
        MCWorld world = w;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;
        if (!array.inAssociativeMode()) {
            if (array.size() == 3) {
                //Just the xyz, with default yaw and pitch, and given world
                x = Static.getNumber(array.get(0, t), t);
                y = Static.getNumber(array.get(1, t), t);
                z = Static.getNumber(array.get(2, t), t);
            } else if (array.size() == 4) {
                //x, y, z, world
                x = Static.getNumber(array.get(0, t), t);
                y = Static.getNumber(array.get(1, t), t);
                z = Static.getNumber(array.get(2, t), t);
                world = Static.getServer().getWorld(array.get(3, t).val());
            } else if (array.size() == 5) {
                //x, y, z, yaw, pitch, with given world
                x = Static.getNumber(array.get(0, t), t);
                y = Static.getNumber(array.get(1, t), t);
                z = Static.getNumber(array.get(2, t), t);
                yaw = (float) Static.getNumber(array.get(3, t), t);
                pitch = (float) Static.getNumber(array.get(4, t), t);
            } else if (array.size() == 6) {
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
			if (array.containsKey("x")) {
				x = Static.getNumber(array.get("x", t), t);
			}
			if (array.containsKey("y")) {
				y = Static.getNumber(array.get("y", t), t);
			}
			if (array.containsKey("z")) {
				z = Static.getNumber(array.get("z", t), t);
			}
			if (array.containsKey("world")) {
				world = Static.getServer().getWorld(array.get("world", t).val());
			}
			if (array.containsKey("yaw")) {
				yaw = (float) Static.getDouble(array.get("yaw", t), t);
			}
			if (array.containsKey("pitch")) {
				pitch = (float) Static.getDouble(array.get("pitch", t), t);
			}
		}
		//If world is still null at this point, it's an error
		if (world == null) {
			throw new CREInvalidWorldException("The specified world doesn't exist, or no world was provided", t);
		}
        return StaticLayer.GetLocation(world, x, y, z, yaw, pitch);
    }

    /**
     * An Item Object consists of data about a particular item stack.
     * Information included is: recipeType, data, qty, and an array of enchantment
 objects (labeled enchants): erecipeType (enchantment recipeType) and elevel
 (enchantment level). For backwards compatibility, this information is
 also listed in numerical slots as well as associative slots. If the
 MCItemStack is null, or the underlying item is nonexistant (or air) CNull
 is returned.
     *
     * @param is
     * @return
     */
    public Construct item(MCItemStack is, Target t) {
        if (is == null || is.getAmount() == 0 || is.getTypeId() == 0) {
            return CNull.NULL;
        }
        int type = is.getTypeId();

        int data;
        if(type < 256){
            //Use the data
            data = (is.getData() != null ? is.getData().getData() : 0);
        } else {
            //Use the durability
            data = is.getDurability();
        }

        CArray enchants = new CArray(t);
        for (Map.Entry<MCEnchantment, Integer> entry : is.getEnchantments().entrySet()) {
            CArray enchObj = CArray.GetAssociativeArray(t);
            enchObj.set("etype", new CString(entry.getKey().getName(), t), t);
            enchObj.set("elevel", new CInt(entry.getValue(), t), t);
            enchants.push(enchObj, t);
        }
		Construct meta = itemMeta(is, t);
        CArray ret = CArray.GetAssociativeArray(t);
		ret.set("name", new CString(is.getType().getName(), t), t);
		ret.set("type", new CInt(type, t), t);
		ret.set("data", new CInt(data, t), t);
		ret.set("qty", new CInt(is.getAmount(), t), t);
		ret.set("enchants", enchants, t);
		ret.set("meta", meta, t);
        return ret;
    }

    /**
     * Gets an MCItemStack from a given item "object". Supports both the old and
     * new formats currently
     *
     * @param i
     * @return
     */
    public MCItemStack item(Construct i, Target t) {
        if (i instanceof CNull) {
            return EmptyItem();
        }
        if (!(i instanceof CArray)) {
            throw new CREFormatException("Expected an array!", t);
        }
        CArray item = (CArray) i;
		MCMaterial mat = null;
		int data = 0, qty = 1;
        Map<MCEnchantment, Integer> enchants = new HashMap<MCEnchantment, Integer>();
		MCItemMeta meta = null;

		if (item.containsKey("name")) {
			mat = StaticLayer.GetConvertor().GetMaterial(item.get("name", t).val());
		} else if (item.containsKey("type")) {
			if (item.get("type", t).val().contains(":")) {
				//We're using the combo addressing method
				String[] split = item.get("type", t).val().split(":");
				item = item.deepClone(t);
				item.set("type", split[0]);
				item.set("data", split[1]);
			}
			mat = StaticLayer.GetConvertor().getMaterial(Static.getInt32(item.get("type", t), t));
		} else {
			throw new CREFormatException("Could not find item type!", t);
		}
		if (mat == null) {
			throw new CRENotFoundException("A material type could not be found based on the given id.", t);
		}
		if (item.containsKey("qty")) {
			qty = Static.getInt32(item.get("qty", t), t);
		}
		if (item.containsKey("data")) {
			data = Static.getInt32(item.get("data", t), t);
		}

        if (item.containsKey("enchants")) {
            CArray enchantArray;
            try {
				enchantArray = (CArray) item.get("enchants", t);
                if (enchantArray == null) {
                    throw new NullPointerException();
                }
            } catch (Exception e) {
                throw new CREFormatException("Enchants value must be an array of enchantment arrays.", t, e);
            }

            for (String index : enchantArray.stringKeySet()) {
                try {
                    CArray enchantment = (CArray) enchantArray.get(index, t);
                    String setype = null;
                    String selevel = null;
                    if (enchantment.containsKey("etype")) {
                        setype = enchantment.get("etype", t).val();
                    }

                    if (enchantment.containsKey("elevel")) {
                        selevel = enchantment.get("elevel", t).val();
                    }
                    if (setype == null || selevel == null) {
                        throw new CREFormatException("An enchantment array must have an etype and elevel.", t);
                    }
                    int elevel = 0;
                    try {
                        elevel = Integer.parseInt(selevel);
                    } catch (NumberFormatException e) {
                        throw new CREFormatException("Expecting enchantment array elevel to be an integer but got \"" + selevel + "\" instead.", t);
                    }
                    MCEnchantment etype = StaticLayer.GetEnchantmentByName(setype);
					if(etype == null){
						throw new CREFormatException("Invalid enchantment array etype: \"" + setype + "\".", t);
					}
                    enchants.put(etype, elevel);
                } catch (ClassCastException e) {
                    throw new CREFormatException("Enchants value must be an array of enchantment arrays.", t, e);
                }
            }
        }
		if (item.containsKey("meta")) {
			meta = itemMeta(item.get("meta", t), mat, t);
		}
		MCItemStack ret = StaticLayer.GetItemStack(mat, data, qty);
		if (meta != null) {
			ret.setItemMeta(meta);
		}
		for (Map.Entry<MCEnchantment, Integer> entry : enchants.entrySet()) {
			ret.addUnsafeEnchantment(entry.getKey(), entry.getValue());
		}

        //Giving them air crashes the client, so just clear the inventory slot
        if (ret.getTypeId() == 0) {
            ret = EmptyItem();
        }
        return ret;
    }

    private static MCItemStack EmptyItem() {
        return StaticLayer.GetItemStack(0, 1);
    }

	public Construct itemMeta(MCItemStack is, Target t) {
		Construct ret, display, lore, color, title, author, pages, owner, stored;
		CArray enchants, effects;
		if (!is.hasItemMeta()) {
			ret = CNull.NULL;
		} else {
			CArray ma = CArray.GetAssociativeArray(t);
			MCItemMeta meta = is.getItemMeta();
			if (meta.hasDisplayName()) {
				display = new CString(meta.getDisplayName(), t);
			} else {
				display = CNull.NULL;
			}
			if (meta.hasLore()) {
				lore = new CArray(t);
				for (String l : meta.getLore()) {
					((CArray) lore).push(new CString(l, t), t);
				}
			} else {
				lore = CNull.NULL;
			}
			enchants = enchants(meta.getEnchants(), t);
			ma.set("display", display, t);
			ma.set("lore", lore, t);
			ma.set("enchants", enchants, t);
			ma.set("repair", new CInt(meta.getRepairCost(), t), t);
			if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_8)) {
				Set<MCItemFlag> itemFlags = meta.getItemFlags();
				CArray flagArray = new CArray(t);
				if (itemFlags.size() > 0) {
					for (MCItemFlag flag : itemFlags) {
						flagArray.push(new CString(flag.name(), t), t);
					}
				}
				ma.set("flags", flagArray, t);
			}

			// Specific ItemMeta

			if(meta instanceof  MCFireworkEffectMeta){
				MCFireworkEffectMeta mcfem = (MCFireworkEffectMeta) meta;
				MCFireworkEffect effect = mcfem.getEffect();
				if(effect == null){
					ma.set("effect", CNull.NULL, t);
				} else {
					ma.set("effect", fireworkEffect(effect, t), t);
				}
			} else if(meta instanceof MCFireworkMeta){
				MCFireworkMeta mcfm = (MCFireworkMeta) meta;
				CArray firework = CArray.GetAssociativeArray(t);
				firework.set("strength", new CInt(mcfm.getStrength(), t), t);
				CArray fe = new CArray(t);
				for(MCFireworkEffect effect : mcfm.getEffects()) {
					fe.push(fireworkEffect(effect, t), t);
				}
				firework.set("effects", fe, t);
				ma.set("firework", firework, t);
			} else if (meta instanceof MCLeatherArmorMeta) {
				color = color(((MCLeatherArmorMeta) meta).getColor(), t);
				ma.set("color", color, t);
			} else if (meta instanceof MCBookMeta) {
				if (((MCBookMeta) meta).hasTitle()) {
					title = new CString(((MCBookMeta) meta).getTitle(), t);
				} else {
					title = CNull.NULL;
				}
				if (((MCBookMeta) meta).hasAuthor()) {
					author = new CString(((MCBookMeta) meta).getAuthor(), t);
				} else {
					author = CNull.NULL;
				}
				if (((MCBookMeta) meta).hasPages()) {
					pages = new CArray(t);
					for (String p : ((MCBookMeta) meta).getPages()) {
						((CArray) pages).push(new CString(p, t), t);
					}
				} else {
					pages = CNull.NULL;
				}
				ma.set("title", title, t);
				ma.set("author", author, t);
				ma.set("pages", pages, t);
			} else if (meta instanceof MCSkullMeta) {
				if (((MCSkullMeta) meta).hasOwner()) {
					owner = new CString(((MCSkullMeta) meta).getOwner(), t);
				} else {
					owner = CNull.NULL;
				}
				ma.set("owner", owner, t);
			} else if (meta instanceof MCEnchantmentStorageMeta) {
				if (((MCEnchantmentStorageMeta) meta).hasStoredEnchants()) {
					stored = enchants(((MCEnchantmentStorageMeta) meta).getStoredEnchants(), t);
				} else {
					stored = CNull.NULL;
				}
				ma.set("stored", stored, t);
			} else if (meta instanceof MCPotionMeta) {
				MCPotionMeta potionmeta = (MCPotionMeta) meta;
				effects = potions(potionmeta.getCustomEffects(), t);
				ma.set("potions", effects, t);
				if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_9)){
					MCPotionData potiondata = potionmeta.getBasePotionData();
					if(potiondata != null){
						ma.set("base", potionData(potiondata, t), t);
					}
				} else if(effects.size() > 0){
					ma.set("main", ((CArray) effects.get(0, t)).get("id", t), t);
				}
			} else if (meta instanceof MCBannerMeta) {
				MCBannerMeta bannermeta = (MCBannerMeta) meta;
				CArray patterns = new CArray(t, bannermeta.numberOfPatterns());
				for (MCPattern p : bannermeta.getPatterns()) {
					CArray pattern = CArray.GetAssociativeArray(t);
					pattern.set("shape", new CString(p.getShape().toString(), t), t);
					pattern.set("color", new CString(p.getColor().toString(), t), t);
					patterns.push(pattern, t);
				}
				ma.set("patterns", patterns, t);
				MCDyeColor dyeColor = bannermeta.getBaseColor();
				if(dyeColor != null) {
					ma.set("basecolor", new CString(dyeColor.toString(), t), t);
				}
			}
			ret = ma;
		}
		return ret;
	}

	public MCItemMeta itemMeta(Construct c, MCMaterial mat, Target t) throws ConfigRuntimeException {
		MCItemFactory itemFactory = Static.getServer().getItemFactory();
		if (itemFactory == null) {
			throw new CRENotFoundException("Could not find the internal MCItemFactory object (are you running in cmdline mode?)", t);
		}
		MCItemMeta meta = itemFactory.getItemMeta(mat);
		if (c instanceof CNull) {
			return meta;
		}
		CArray ma;
		if (c instanceof CArray) {
			ma = (CArray) c;
			try {
				if (ma.containsKey("display")) {
					Construct dni = ma.get("display", t);
					if (!(dni instanceof CNull)) {
						meta.setDisplayName(dni.val());
					}
				}
				if (ma.containsKey("lore")) {
					Construct li = ma.get("lore", t);
					if(li instanceof CString){
						li = new CArray(t, li);
					}
					if (li instanceof CNull) {
						//do nothing
					} else if (li instanceof CArray) {
						CArray la = (CArray) li;
						List<String> ll = new ArrayList<>();
						for (int j = 0; j < la.size(); j++) {
							ll.add(la.get(j, t).val());
						}
						meta.setLore(ll);
					} else {
						throw new CREFormatException("Lore was expected to be an array or a string.", t);
					}
				}
				if (ma.containsKey("enchants")) {
					Construct enchants = ma.get("enchants", t);
					if (enchants instanceof CArray) {
						for (Map.Entry<MCEnchantment, Integer> ench : enchants((CArray) enchants, t).entrySet()) {
							meta.addEnchant(ench.getKey(), ench.getValue(), true);
						}
					} else {
						throw new CREFormatException("Enchants field was expected to be an array of Enchantment arrays", t);
					}
				}
				if (ma.containsKey("repair") && !(ma.get("repair", t) instanceof CNull)) {
					meta.setRepairCost(Static.getInt32(ma.get("repair", t), t));
				}
				if (ma.containsKey("flags") && Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_8)) {
					Construct flags = ma.get("flags", t);
					if (flags instanceof CArray) {
						CArray flagArray = (CArray) flags;
						for (int i = 0; i < flagArray.size(); i++) {
							Construct flag = flagArray.get(i, t);
							meta.addItemFlags(MCItemFlag.valueOf(flag.getValue().toUpperCase()));
						}
					} else {
						throw new CREFormatException("Itemflags was expected to be an array of flags.", t);
					}
				}

				// Specific Item Meta

				if(meta instanceof MCFireworkEffectMeta){
					MCFireworkEffectMeta femeta = (MCFireworkEffectMeta) meta;
					if(ma.containsKey("effect")){
						Construct cfem = ma.get("effect", t);
						if(cfem instanceof CArray){
							femeta.setEffect(fireworkEffect((CArray) cfem, t));
						} else if(!(cfem instanceof CNull)){
							throw new CREFormatException("FireworkCharge effect was expected to be an array or null.", t);
						}
					}
				} else if(meta instanceof MCFireworkMeta){
					MCFireworkMeta fmeta = (MCFireworkMeta) meta;
					if(ma.containsKey("firework")){
						Construct construct = ma.get("firework", t);
						if(construct instanceof CArray){
							CArray firework = (CArray) construct;
							if(firework.containsKey("strength")){
								fmeta.setStrength(Static.getInt32(firework.get("strength", t), t));
							}
							if(firework.containsKey("effects")){
								// New style (supports multiple effects)
								Construct effects = firework.get("effects", t);
								if(effects instanceof CArray){
									for(Construct effect : ((CArray) effects).asList()){
										if(effect instanceof CArray){
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
				} else if (meta instanceof MCLeatherArmorMeta) {
					if (ma.containsKey("color")) {
						Construct ci = ma.get("color", t);
						if (ci instanceof CNull) {
							//nothing
						} else if (ci instanceof CArray) {
							((MCLeatherArmorMeta) meta).setColor(color((CArray) ci, t));
						} else {
							throw new CREFormatException("Color was expected to be an array.", t);
						}
					}
				} else if (meta instanceof MCBookMeta) {
					if (ma.containsKey("title")) {
						Construct title = ma.get("title", t);
						if (!(title instanceof CNull)) {
							((MCBookMeta) meta).setTitle(title.val());
						}
					}
					if (ma.containsKey("author")) {
						Construct author = ma.get("author", t);
						if (!(author instanceof CNull)) {
							((MCBookMeta) meta).setAuthor(author.val());
						}
					}
					if (ma.containsKey("pages")) {
						Construct pages = ma.get("pages", t);
						if (pages instanceof CNull) {
							//nothing
						} else if (pages instanceof CArray) {
							CArray pa = (CArray) pages;
							List<String> pl = new ArrayList<String>();
							for (int j = 0; j < pa.size(); j++) {
								pl.add(pa.get(j, t).val());
							}
							((MCBookMeta) meta).setPages(pl);
						} else {
							throw new CREFormatException("Pages field was expected to be an array.", t);
						}
					}
				} else if (meta instanceof MCSkullMeta) {
					if (ma.containsKey("owner")) {
						Construct owner = ma.get("owner", t);
						if (!(owner instanceof CNull)) {
							((MCSkullMeta) meta).setOwner(owner.val());
						}
					}
				} else if (meta instanceof MCEnchantmentStorageMeta) {
					if (ma.containsKey("stored")) {
						Construct stored = ma.get("stored", t);
						if (stored instanceof CNull) {
							//Still doing nothing
						} else if (stored instanceof CArray) {
							for (Map.Entry<MCEnchantment, Integer> ench : enchants((CArray) stored, t).entrySet()) {
								((MCEnchantmentStorageMeta) meta).addStoredEnchant(ench.getKey(), ench.getValue(), true);
							}
						} else {
							throw new CREFormatException("Stored field was expected to be an array of Enchantment arrays", t);
						}
					}
				} else if (meta instanceof MCPotionMeta) {
					if (ma.containsKey("potions")) {
						Construct effects = ma.get("potions", t);
						if (effects instanceof CArray) {
							for (MCLivingEntity.MCEffect e : potions((CArray) effects, t)) {
								((MCPotionMeta) meta).addCustomEffect(e.getPotionID(), e.getStrength(),
										e.getSecondsRemaining(), e.isAmbient(), true, t);
							}
						} else {
							throw new CREFormatException("Effects was expected to be an array of potion arrays.", t);
						}
					}
					if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_9)){
						if(ma.containsKey("base")){
							Construct potiondata = ma.get("base", t);
							if(potiondata instanceof CArray){
								CArray pd = (CArray) potiondata;
								((MCPotionMeta) meta).setBasePotionData(potionData((CArray) potiondata, t));
							}
						}
					} else if (ma.containsKey("main")) {
						((MCPotionMeta) meta).setMainEffect(Static.getInt32(ma.get("main", t), t));
					}
				} else if (meta instanceof MCBannerMeta) {
					if (ma.containsKey("basecolor")) {
						((MCBannerMeta) meta).setBaseColor(MCDyeColor.valueOf(ma.get("basecolor", t).val().toUpperCase()));
					}
					if (ma.containsKey("patterns")) {
						CArray array = ArgumentValidation.getArray(ma.get("patterns", t), t);
						for (String key : array.stringKeySet()) {
							CArray pattern = ArgumentValidation.getArray(array.get(key, t), t);
							MCPatternShape shape = MCPatternShape.valueOf(pattern.get("shape", t).val().toUpperCase());
							MCDyeColor color = MCDyeColor.valueOf(pattern.get("color", t).val().toUpperCase());
							((MCBannerMeta) meta).addPattern(StaticLayer.GetConvertor().GetPattern(color, shape));
						}
					}
				}
			} catch(Exception ex) {
				throw new CREFormatException(ex.getMessage(), t, ex);
			}
		} else {
			throw new CREFormatException("An array was expected but recieved " + c + " instead.", t);
		}
		return meta;
	}

    public CArray exception(ConfigRuntimeException e, Environment env, Target t) {
		AbstractCREException ex = AbstractCREException.getAbstractCREException(e);
		return ex.getExceptionObject();
    }
	
	public AbstractCREException exception(CArray exception, Target t) throws ClassNotFoundException{
		return AbstractCREException.getFromCArray(exception, t);
	}

	/**
	 * Returns a CArray given an MCColor. It will be in the format
	 * array(r: 0, g: 0, b: 0)
	 * @param color
	 * @param t
	 * @return
	 */
	public CArray color(MCColor color, Target t){
		CArray ca = CArray.GetAssociativeArray(t);
		ca.set("r", new CInt(color.getRed(), t), t);
		ca.set("g", new CInt(color.getGreen(), t), t);
		ca.set("b", new CInt(color.getBlue(), t), t);
		return ca;
	}

	/**
	 * Returns an MCColor given a colorArray, which supports the following
 three format recipeTypes (in this order of priority)
 array(r: 0, g: 0, b: 0)
 array(red: 0, green: 0, blue: 0)
 array(0, 0, 0)
	 * @param color
	 * @param t
	 * @return
	 */
	public MCColor color(CArray color, Target t){
		int red;
		int green;
		int blue;
		if(color.containsKey("r")){
			red = Static.getInt32(color.get("r", t), t);
		} else if(color.containsKey("red")){
			red = Static.getInt32(color.get("red", t), t);
		} else {
			red = Static.getInt32(color.get(0, t), t);
		}
		if(color.containsKey("g")){
			green = Static.getInt32(color.get("g", t), t);
		} else if(color.containsKey("green")){
			green = Static.getInt32(color.get("green", t), t);
		} else {
			green = Static.getInt32(color.get(1, t), t);
		}
		if(color.containsKey("b")){
			blue = Static.getInt32(color.get("b", t), t);
		} else if(color.containsKey("blue")){
			blue = Static.getInt32(color.get("blue", t), t);
		} else {
			blue = Static.getInt32(color.get(2, t), t);
		}
		try {
			return StaticLayer.GetConvertor().GetColor(red, green, blue);
		} catch(IllegalArgumentException ex){
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
	@Deprecated
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
	 * A vector has three parts: the X, Y, and Z.
	 * If the vector object is missing the Z part, then we will assume it is zero.
	 * If the vector object is missing the X and/or Y part, then we will assume it is not a vector.
	 *
	 * Furthermore, the string keys ("x", "y" and "z") take precedence over the integral ones.
	 * For example, in a case of <code>array(0, 1, 2, x: 3, y: 4, z: 5)</code>, the
	 * resultant Vector will be of the value <code>Vector(3, 4, 5)</code>.
	 *
	 * For consistency, the method will accept any Construct, but it requires a CArray.
	 *
	 * @param c the vector array
	 * @param t the target
	 * @return the Vector
	 */
	public Vector3D vector(Construct c, Target t) {
		return vector(Vector3D.ZERO, c, t);
	}

    /**
     * Modifies an existing vector using a given vector object.
	 * Because Vector3D is immutable, this method does not actually modify the existing vector,
	 * but creates a new one.
	 *
     * @param v the original vector
     * @param c the vector array
     * @param t the target
     * @return the Vector
     */
    public Vector3D vector(Vector3D v, Construct c, Target t) {
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
				} else if(va.size() == 2){ // 2nd dimension vector
					x = Static.getNumber(va.get(0, t), t);
					y = Static.getNumber(va.get(1, t), t);
				} else if(va.size() == 1){
					x = Static.getNumber(va.get(0, t), t);
				}
			} else {
				if(va.containsKey("x")){
					x = Static.getNumber(va.get("x", t), t);
				}
				if(va.containsKey("y")){
					y = Static.getNumber(va.get("y", t), t);
				}
				if(va.containsKey("z")){
					z = Static.getNumber(va.get("z", t), t);
				}
			}

			return new Vector3D(x, y, z);
		} else if(c instanceof CNull) {
			// fulfilling the todo?
			return v;
		} else {
			throw new CREFormatException("Expecting an array, received " + c.getCType(), t);
		}
	}

	public CArray enchants(Map<MCEnchantment, Integer> map, Target t) {
		CArray ret = new CArray(t);
		for (Map.Entry<MCEnchantment, Integer> entry : map.entrySet()) {
			CArray eObj = CArray.GetAssociativeArray(t);
			eObj.set("etype", new CString(entry.getKey().getName(), t), t);
			eObj.set("elevel", new CInt(entry.getValue(), t), t);
			ret.push(eObj, t);
		}
		return ret;
	}

	public Map<MCEnchantment,Integer> enchants(CArray enchantArray, Target t) {
		Map<MCEnchantment,Integer> ret = new HashMap<MCEnchantment,Integer>();
		for (String key : enchantArray.stringKeySet()) {
			try {
				CArray ea = (CArray) enchantArray.get(key, t);
				MCEnchantment etype = StaticLayer.GetConvertor().GetEnchantmentByName(ea.get("etype", t).val());
				int elevel = Static.getInt32(ea.get("elevel", t), t);
				if (etype == null) {
					throw new CREEnchantmentException("Unknown enchantment type at " + key, t);
				}
				ret.put(etype, elevel);
			} catch (ClassCastException cce) {
				throw new CREFormatException("Expected an array at index " + key, t);
			}
		}
		return ret;
	}

	public CArray potions(List<MCLivingEntity.MCEffect> effectList, Target t) {
		CArray ea = new CArray(t);
		for (MCLivingEntity.MCEffect eff : effectList) {
			CArray effect = CArray.GetAssociativeArray(t);
			effect.set("id", new CInt(eff.getPotionID(), t), t);
			effect.set("strength", new CInt(eff.getStrength(), t), t);
			effect.set("seconds", new CInt(eff.getSecondsRemaining(), t), t);
			effect.set("ambient", CBoolean.get(eff.isAmbient()), t);
			effect.set("particles", CBoolean.get(eff.hasParticles()), t);
			ea.push(effect, t);
		}
		return ea;
	}

	public List<MCLivingEntity.MCEffect> potions(CArray ea, Target t) {
		List<MCLivingEntity.MCEffect> ret = new ArrayList<MCLivingEntity.MCEffect>();
		for (String key : ea.stringKeySet()) {
			if (ea.get(key, t) instanceof CArray) {
				CArray effect = (CArray) ea.get(key, t);
				int potionID = 0, strength = 0, seconds = 30;
				boolean ambient = false;
				boolean particles = true;
				if (effect.containsKey("id")) {
					potionID = Static.getInt32(effect.get("id", t), t);
				} else {
					throw new CREFormatException("No potion ID was given at index " + key, t);
				}
				if (effect.containsKey("strength")) {
					strength = Static.getInt32(effect.get("strength", t), t);
				}
				if (effect.containsKey("seconds")) {
					seconds = Static.getInt32(effect.get("seconds", t), t);
				}
				if (effect.containsKey("ambient")) {
					ambient = Static.getBoolean(effect.get("ambient", t));
				}
				if (effect.containsKey("particles")) {
					particles = Static.getBoolean(effect.get("particles", t));
				}
				ret.add(new MCLivingEntity.MCEffect(potionID, strength, seconds, ambient, particles));
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
		MCPotionType type = MCPotionType.valueOf(pd.get("type", t).val().toUpperCase());
		boolean extended = false;
		boolean upgraded = false;
		if(pd.containsKey("extended")){
			Construct cext = pd.get("extended", t);
			if(cext instanceof CBoolean){
				extended = ((CBoolean) cext).getBoolean();
			} else {
				throw new CREFormatException(
						"Expected potion value for key \"extended\" to be a boolean", t);
			}
		}
		if(pd.containsKey("upgraded")){
			Construct cupg = pd.get("upgraded", t);
			if(cupg instanceof CBoolean){
				upgraded = ((CBoolean) cupg).getBoolean();
			} else {
				throw new CREFormatException(
						"Expected potion value for key \"upgraded\" to be a boolean", t);
			}
		}
		try {
			return StaticLayer.GetPotionData(type, extended, upgraded);
		} catch(IllegalArgumentException ex){
			throw new CREFormatException(ex.getMessage(), t, ex);
		}
	}

	public CArray fireworkEffect(MCFireworkEffect mcfe, Target t) {
		CArray fe = CArray.GetAssociativeArray(t);
		fe.set("flicker", CBoolean.get(mcfe.hasFlicker()), t);
		fe.set("trail", CBoolean.get(mcfe.hasTrail()), t);
		MCFireworkType type = mcfe.getType();
		if(type != null){
			fe.set("type", new CString(mcfe.getType().name(), t), t);
		} else {
			fe.set("type", CNull.NULL, t);
		}
		CArray colors = new CArray(t);
		for(MCColor c : mcfe.getColors()){
			colors.push(ObjectGenerator.GetGenerator().color(c, t), t);
		}
		fe.set("colors", colors, t);
		CArray fadeColors = new CArray(t);
		for(MCColor c : mcfe.getFadeColors()){
			fadeColors.push(ObjectGenerator.GetGenerator().color(c, t), t);
		}
		fe.set("fade", fadeColors, t);
		return fe;
	}

	public MCFireworkEffect fireworkEffect(CArray fe, Target t) {
		MCFireworkBuilder builder = StaticLayer.GetConvertor().GetFireworkBuilder();
		if(fe.containsKey("flicker")){
			builder.setFlicker(Static.getBoolean(fe.get("flicker", t)));
		}
		if(fe.containsKey("trail")){
			builder.setTrail(Static.getBoolean(fe.get("trail", t)));
		}
		if(fe.containsKey("colors")){
			Construct colors = fe.get("colors", t);
			if(colors instanceof CArray){
				CArray ccolors = (CArray) colors;
				if(ccolors.size() == 0) {
					builder.addColor(MCColor.WHITE);
				} else {
					for(Construct color : ccolors.asList()) {
						MCColor mccolor;
						if(color instanceof CString){
							mccolor = StaticLayer.GetConvertor().GetColor(color.val(), t);
						} else if(color instanceof CArray){
							mccolor = color((CArray) color, t);
						} else if(color instanceof CInt && ccolors.size() == 3){
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
			} else if(colors instanceof CString){
				String split[] = colors.val().split("\\|");
				if(split.length == 0){
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
		if(fe.containsKey("fade")){
			Construct colors = fe.get("fade", t);
			if(colors instanceof CArray){
				CArray ccolors = (CArray) colors;
				for(Construct color : ccolors.asList()) {
					MCColor mccolor;
					if(color instanceof CArray){
						mccolor = color((CArray) color, t);
					} else if(color instanceof CString){
						mccolor = StaticLayer.GetConvertor().GetColor(color.val(), t);
					} else if(color instanceof CInt && ccolors.size() == 3){
						// Appears to be a single color
						builder.addFadeColor(color(ccolors, t));
						break;
					} else {
						throw new CREFormatException("Expecting individual color to be an array or string, but found "
								+ color.typeof(), t);
					}
					builder.addFadeColor(mccolor);
				}
			} else if(colors instanceof CString){
				String split[] = colors.val().split("\\|");
				for(String s : split) {
					builder.addFadeColor(StaticLayer.GetConvertor().GetColor(s, t));
				}
			} else {
				throw new CREFormatException("Expecting an array or string for fade parameter, but found "
						+ colors.typeof(), t);
			}
		}
		if(fe.containsKey("type")){
			try {
				builder.setType(MCFireworkType.valueOf(fe.get("type", t).val().toUpperCase()));
			} catch(IllegalArgumentException ex){
				throw new CREFormatException(ex.getMessage(), t, ex);
			}
		}
		return builder.build();
	}

	public Construct recipe(MCRecipe r, Target t) {
		if (r == null) {
			return CNull.NULL;
		}
		CArray ret = CArray.GetAssociativeArray(t);
		ret.set("type", new CString(r.getRecipeType().name(), t), t);
		ret.set("result", item(r.getResult(), t), t);
		if (r instanceof MCFurnaceRecipe) {
			ret.set("input", item(((MCFurnaceRecipe) r).getInput(), t), t);
		} else if (r instanceof MCShapelessRecipe) {
			CArray il = new CArray(t);
			for (MCItemStack i : ((MCShapelessRecipe) r).getIngredients()) {
				il.push(item(i, t), t);
			}
			ret.set("ingredients", il, t);
		} else if (r instanceof MCShapedRecipe) {
			MCShapedRecipe sr = (MCShapedRecipe) r;
			CArray shape = new CArray(t);
			for (String line : sr.getShape()) {
				shape.push(new CString(line, t), t);
			}
			CArray imap = CArray.GetAssociativeArray(t);
			for (Map.Entry<Character, MCItemStack> entry : sr.getIngredientMap().entrySet()) {
				imap.set(entry.getKey().toString(), item(entry.getValue(), t), t);
			}
			ret.set("shape", shape, t);
			ret.set("ingredients", imap, t);
		}
		return ret;
	}

	public MCMaterial material(String name, Target t) {
		try {
			return StaticLayer.GetMaterial(name.toUpperCase());
		} catch (IllegalArgumentException exception) {
			throw new CREFormatException("Unknown material type: " + name, t);
		}
	}

	public MCMaterial material(Construct name, Target t) {
		return material(name.val(), t);
	}

	public MCRecipe recipe(Construct c, Target t) {
		if (c instanceof CArray) {
			CArray recipe = (CArray) c;
			MCItemStack result = EmptyItem();
			if (recipe.containsKey("result") && (recipe.get("result", t) instanceof CArray)) {
				result = item(recipe.get("result", t), t);

				if (recipe.containsKey("type") && (recipe.get("type", t) instanceof CString)) {
					MCRecipeType recipeType;
					try {
						recipeType = MCRecipeType.valueOf(recipe.get("type", t).val());
					} catch (IllegalArgumentException e) {
						throw new CREFormatException("Invalid recipe type.", t);
					}

					MCRecipe ret;
					switch(recipeType) {
						case SHAPED:
						ret = StaticLayer.GetNewRecipe(MCRecipeType.SHAPED, result);

						if(recipe.containsKey("shape") && (recipe.get("shape", t) instanceof CArray)) {
							CArray sh = (CArray) recipe.get("shape", t);
							String[] shape = new String[(int) sh.size()];
							if (sh.size() >= 1 && sh.size() <= 3 && !sh.inAssociativeMode()) {
								int i = 0;
								for(Construct row : sh.asList()) {
									if(row instanceof CString && ((CString) row).val().length() >= 1 && ((CString) row).val().length() <= 3) {
										shape[i] = row.val();
										i++;
									} else {
										throw new CREFormatException("Shape array is invalid.", t);
									}
								}
							} else {
								throw new CREFormatException("Shape array is invalid.", t);
							}
							((MCShapedRecipe) ret).setShape(shape);
						} else {
							throw new CREFormatException("Could not find recipe shape array.", t);
						}

						if(recipe.containsKey("ingredients") && (recipe.get("ingredients", t) instanceof CArray)) {
							CArray ingredients = (CArray) recipe.get("ingredients", t);
							if(ingredients.inAssociativeMode()) {
								for(String key : ingredients.stringKeySet()) {
									int type = 0;
									int data = 0;
									if (ingredients.get(key, t) instanceof CString) {
										CString item = (CString) ingredients.get(key, t);
										if (item.val().contains(":")) {
											String[] split = item.val().split(":");
											type = Integer.valueOf(split[0]);
											data = Integer.valueOf(split[1]);
										} else {
											type = Integer.valueOf(item.val());
										}
									} else if (ingredients.get(key, t) instanceof CInt) {
										type = Integer.valueOf(((CInt) ingredients.get(key, t)).val());
									} else if (ingredients.get(key, t) instanceof CArray) {
										MCItemStack item = item(ingredients.get(key, t), t);
										type = item.getTypeId();
										data = item.getDurability();
									} else if (ingredients.get(key, t) instanceof CNull) {
										type = 0;
										data = 0;
									} else {
										throw new CREFormatException("Item type was not found", t);
									}
									((MCShapedRecipe) ret).setIngredient(key.charAt(0), type, data);
								}
							} else {
								throw new CREFormatException("Ingredients array is invalid.", t);
							}
						} else {
							throw new CREFormatException("Could not find recipe ingredient array.", t);
						}
						return ret;

						case SHAPELESS:
						ret = StaticLayer.GetNewRecipe(MCRecipeType.SHAPELESS, result);

						if(recipe.containsKey("ingredients") && (recipe.get("ingredients", t) instanceof CArray)) {
							CArray ingredients = (CArray) recipe.get("ingredients", t);
							if(!ingredients.inAssociativeMode()) {
								for(Construct item : ingredients.asList()) {
									int type = 0;
									int data = 0;
									if (item instanceof CString) {
										item = (CString) item;
										if (item.val().contains(":")) {
											String[] split = item.val().split(":");
											type = Integer.valueOf(split[0]);
											data = Integer.valueOf(split[1]);
										} else {
											type = Integer.valueOf(item.val());
										}
									} else {
										throw new CREFormatException("Item type was not found", t);
									}
									((MCShapelessRecipe) ret).addIngredient(type, data, 1);
								}
							} else {
								throw new CREFormatException("Ingredients array is invalid.", t);
							}
						} else {
							throw new CREFormatException("Could not find recipe ingredient array.", t);
						}
						return ret;

						case FURNACE:
						ret = StaticLayer.GetNewRecipe(MCRecipeType.FURNACE, result);

						if (recipe.containsKey("input") && (recipe.get("input", t) instanceof CArray)) {
							((MCFurnaceRecipe) ret).setInput(item(recipe.get("input", t), t));
						} else {
							throw new CREFormatException("Could not find input item array.", t);
						}
						return ret;
					default:
						throw new CREFormatException("Could not find valid recipe type.", t);
					}
				} else {
						throw new CREFormatException("Could not find recipe type.", t);
				}
			} else {
				throw new CREFormatException("Could not find result item array.", t);
			}
		} else {
			throw new CRECastException("Expected array but recieved " + c, t);
		}
	}

	/**
	 * Gets a MetadataValue, given a construct and a plugin.
	 *
	 * @param value
	 * @param plugin
	 * @return
	 */
	public MCMetadataValue metadataValue(Construct value, MCPlugin plugin) {
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
