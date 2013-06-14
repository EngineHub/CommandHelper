

package com.laytonsmith.core;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This file is responsible for converting CH objects into server objects, and
 * vice versa
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
        Construct yaw = new CDouble(l.getYaw(), Target.UNKNOWN);
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
     * Given a Location Object, returns a MCLocation. If the optional world is
     * not specified in the object, the world provided is used instead. Location
     * "objects" are MethodScript arrays that represent a location in game. There are
     * 4 usages: <ul> <li>(x, y, z)</li> <li>(x, y, z, world)</li> <li>(x, y, z,
     * yaw, pitch)</li> <li>(x, y, z, world, yaw, pitch)</li> </ul> In all
     * cases, the pitch and yaw default to 0, and the world defaults to the
     * specified world. <em>More conveniently: ([world], x, y, z, [yaw,
     * pitch])</em>
     */
    public MCLocation location(Mixed c, MCWorld w, Target t) {
        if (!(c instanceof CArray)) {
            throw new ConfigRuntimeException("Expecting an array, received " + c.getClass().getSimpleName(), ExceptionType.FormatException, t);
        }
        CArray array = (CArray) c;
        MCWorld world = w;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;
        if (!array.inAssociativeMode()) {
			//Just the xyz, with default yaw and pitch, and given world
			x = array.get(0, t).primitive(t).castToDouble(t);
			y = array.get(1, t).primitive(t).castToDouble(t);
			z = array.get(2, t).primitive(t).castToDouble(t);
            if (array.size() == 4) {
                //x, y, z, world
                world = Static.getServer().getWorld(array.get(3, t).val());
            } else if (array.size() == 5) {
                //x, y, z, yaw, pitch, with given world
                yaw = array.get(3, t).primitive(t).castToDouble32(t);
                pitch = array.get(4, t).primitive(t).castToDouble32(t);
            } else if (array.size() == 6) {
                //All have been given
                world = Static.getServer().getWorld(array.get(3, t).val());
                yaw = array.get(4, t).primitive(t).castToDouble32(t);
                pitch = array.get(5, t).primitive(t).castToDouble32(t);
            } else {
                throw new ConfigRuntimeException("Expecting a Location array, but the array did not meet the format specifications", ExceptionType.FormatException, t);
            }
        }
        if (array.containsKey("x")) {
            x = array.get("x").primitive(t).castToDouble(t);
        }
        if (array.containsKey("y")) {
            y = array.get("y").primitive(t).castToDouble(t);
        }
        if (array.containsKey("z")) {
            z = array.get("z").primitive(t).castToDouble(t);
        }
        if (array.containsKey("world")) {
            world = Static.getServer().getWorld(array.get("world").val());
        }
        if (array.containsKey("yaw")) {
            yaw = array.get("yaw").primitive(t).castToDouble32(t);
        }
        if (array.containsKey("pitch")) {
            pitch = array.get("pitch").primitive(t).castToDouble32(t);
        }
		//If world is still null at this point, it's an error
		if (world == null) {
			throw new ConfigRuntimeException("The specified world doesn't exist, or no world was provided", ExceptionType.InvalidWorldException, t);
		}
        return StaticLayer.GetLocation(world, x, y, z, yaw, pitch);
    }

    /**
     * An Item Object consists of data about a particular item stack.
     * Information included is: type, data, qty, and an array of enchantment
     * objects (labeled enchants): etype (enchantment type) and elevel
     * (enchantment level). For backwards compatibility, this information is
     * also listed in numerical slots as well as associative slots. If the
     * MCItemStack is null, or the underlying item is nonexistant (or air) CNull
     * is returned.
     *
     * @param is
     * @return
     */
    public Construct item(MCItemStack is, Target t) {
        if (is == null || is.getAmount() == 0) {
            return null;
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
        int qty = is.getAmount();
        CArray enchants = new CArray(t);
        for (Map.Entry<MCEnchantment, Integer> entry : is.getEnchantments().entrySet()) {
            CArray enchObj = CArray.GetAssociativeArray(t);
            enchObj.set("etype", new CString(entry.getKey().getName(), t), t);
            enchObj.set("elevel", new CInt(entry.getValue(), t), t);
            enchants.push(enchObj);
        }
		Construct meta = itemMeta(is, t);
        CArray ret = CArray.GetAssociativeArray(t);
        ret.set("type", Integer.toString(type));
        ret.set("data", Integer.toString(data));
        ret.set("qty", Integer.toString(qty));
        ret.set("enchants", enchants, t);
		ret.set("meta", meta, t);
        return ret;
    }

    /**
     * Gets an MCItemStack from a given item "object". Supports both the old and
     * new formats currently
     *
     * @param i
     * @param line_num
     * @param f
     * @return
     */
    public MCItemStack item(Mixed i, Target t) {
        if (i == null) {
            return EmptyItem();
        }
        if (!(i instanceof CArray)) {
            throw new ConfigRuntimeException("Expected an array!", ExceptionType.FormatException, t);
        }
        CArray item = (CArray) i;
        int type = 0;
        int data = 0;
        int qty = 1;
        Map<MCEnchantment, Integer> enchants = new HashMap<MCEnchantment, Integer>();
		MCItemMeta meta = null;

        if (item.containsKey("type")) {
            try {
                if (item.get("type").val().contains(":")) {
                    //We're using the combo addressing method
                    String[] split = item.get("type").val().split(":");
                    item.set("type", split[0]);
                    item.set("data", split[1]);
                }
                type = Integer.parseInt(item.get("type").val());
            } catch (NumberFormatException e) {
                throw new ConfigRuntimeException("Could not get item information from given information (" + item.get("type").val() + ")", ExceptionType.FormatException, t, e);
            }
        } else {
            throw new ConfigRuntimeException("Could not find item type!", ExceptionType.FormatException, t);
        }
        if (item.containsKey("data")) {
            try {
                data = Integer.parseInt(item.get("data").val());
            } catch (NumberFormatException e) {
                throw new ConfigRuntimeException("Could not get item data from given information (" + item.get("data").val() + ")", ExceptionType.FormatException, t, e);
            }
        }
        if (item.containsKey("qty")) {
            //This is the qty
            String sqty = "notanumber";
            if (item.containsKey("qty")) {
                sqty = item.get("qty").val();
            }
            try {
                qty = Integer.parseInt(sqty);
            } catch (NumberFormatException e) {
                throw new ConfigRuntimeException("Could not get qty from given information (" + sqty + ")", ExceptionType.FormatException, t, e);
            }
        }

        if (item.containsKey("enchants")) {
            CArray enchantArray = null;
            try {
                if (item.containsKey("enchants")) {
                    enchantArray = (CArray) item.get("enchants");
                }
                if (enchantArray == null) {
                    throw new NullPointerException();
                }
            } catch (Exception e) {
                throw new ConfigRuntimeException("Could not get enchantment data from given information.", ExceptionType.FormatException, t, e);
            }

            for (String index : enchantArray.keySet()) {
                try {
                    CArray enchantment = (CArray) enchantArray.get(index);
                    String setype = null;
                    String selevel = null;
                    if (enchantment.containsKey("etype")) {
                        setype = enchantment.get("etype").val();
                    }

                    if (enchantment.containsKey("elevel")) {
                        selevel = enchantment.get("elevel").val();
                    }
                    if (setype == null || selevel == null) {
                        throw new ConfigRuntimeException("Could not get enchantment data from given information.", ExceptionType.FormatException, t);
                    }
                    int elevel = 0;
                    try {
                        elevel = Integer.parseInt(selevel);
                    } catch (NumberFormatException e) {
                        throw new ConfigRuntimeException("Could not get enchantment data from given information.", ExceptionType.FormatException, t);
                    }
                    MCEnchantment etype = StaticLayer.GetEnchantmentByName(setype);
                    enchants.put(etype, elevel);
                } catch (ClassCastException e) {
                    throw new ConfigRuntimeException("Could not get enchantment data from given information.", ExceptionType.FormatException, t, e);
                }
            }
        }
		if (item.containsKey("meta")) {
			meta = itemMeta(item.get("meta"), type, t);
		}
        MCItemStack ret = StaticLayer.GetItemStack(type, qty);
        ret.setData(data);
        ret.setDurability((short) data);
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
			ret = null;
		} else {
			CArray ma = CArray.GetAssociativeArray(t);
			MCItemMeta meta = is.getItemMeta();
			if (meta.hasDisplayName()) {
				display = new CString(meta.getDisplayName(), t);
			} else {
				display = null;
			}
			if (meta.hasLore()) {
				lore = new CArray(t);
				for (String l : meta.getLore()) {
					((CArray) lore).push(new CString(l, t));
				}
			} else {
				lore = null;
			}
			enchants = enchants(meta.getEnchants(), t);
			ma.set("display", display, t);
			ma.set("lore", lore, t);
			ma.set("enchants", enchants, t);
			if (meta instanceof MCLeatherArmorMeta) {
				color = color(((MCLeatherArmorMeta) meta).getColor(), t);
				ma.set("color", color, t);
			}
			if (meta instanceof MCBookMeta) {
				if (((MCBookMeta) meta).hasTitle()) {
					title = new CString(((MCBookMeta) meta).getTitle(), t);
				} else {
					title = null;
				}
				if (((MCBookMeta) meta).hasAuthor()) {
					author = new CString(((MCBookMeta) meta).getAuthor(), t);
				} else {
					author = null;
				}
				if (((MCBookMeta) meta).hasPages()) {
					pages = new CArray(t);
					for (String p : ((MCBookMeta) meta).getPages()) {
						((CArray) pages).push(new CString(p, t));
					}
				} else {
					pages = null;
				}
				ma.set("title", title, t);
				ma.set("author", author, t);
				ma.set("pages", pages, t);
			}
			if (meta instanceof MCSkullMeta) {
				if (((MCSkullMeta) meta).hasOwner()) {
					owner = new CString(((MCSkullMeta) meta).getOwner(), t);
				} else {
					owner = null;
				}
				ma.set("owner", owner, t);
			}
			if (meta instanceof MCEnchantmentStorageMeta) {
				if (((MCEnchantmentStorageMeta) meta).hasStoredEnchants()) {
					stored = enchants(((MCEnchantmentStorageMeta) meta).getStoredEnchants(), t);
				} else {
					stored = null;
				}
				ma.set("stored", stored, t);
			}
			if (meta instanceof MCPotionMeta) {
				effects = potions(((MCPotionMeta) meta).getCustomEffects(), t);
				ma.set("potions", effects, t);
				if (effects.size() > 0) {
					ma.set("main", ((CArray) effects.get(0)).get("id"), t);
				}
			}
			ret = ma;
		}
		return ret;
	}
	
	public MCItemMeta itemMeta(Mixed c, int i, Target t) {
		if (c == null) {
			return null;
		}
		MCItemMeta meta = Static.getServer().getItemFactory().getItemMeta(StaticLayer.GetConvertor().getMaterial(i));
		CArray ma;
		if (c instanceof CArray) {
			ma = (CArray) c;
			try {
				if (ma.containsKey("display")) {
					Mixed dni = ma.get("display");
					if (dni != null) {
						meta.setDisplayName(dni.val());
					}
				}
				if (ma.containsKey("lore")) {
					Mixed li = ma.get("lore");
					if (li == null) {
						//do nothing
					} else if (li instanceof CArray) {
						CArray la = (CArray) li;
						List<String> ll = new ArrayList<String>();
						for (int j = 0; j < la.size(); j++) {
							ll.add(la.get(j).val());
						}
						meta.setLore(ll);
					} else {
						throw new Exceptions.FormatException("Lore was expected to be an array.", t);
					}
				}
				if (ma.containsKey("enchants")) {
					Mixed enchants = ma.get("enchants");
					if (enchants instanceof CArray) {
						for (Map.Entry<MCEnchantment, Integer> ench : enchants((CArray) enchants, t).entrySet()) {
							meta.addEnchant(ench.getKey(), ench.getValue(), true);
						}
					} else {
						throw new Exceptions.FormatException("Enchants field was expected to be an array of Enchantment arrays", t);
					}
				}
				if (meta instanceof MCLeatherArmorMeta) {
					if (ma.containsKey("color")) {
						Mixed ci = ma.get("color");
						if (ci == null) {
							//nothing
						} else if (ci instanceof CArray) {
							((MCLeatherArmorMeta) meta).setColor(color((CArray) ci, t));
						} else {
							throw new Exceptions.FormatException("Color was expected to be an array.", t);
						}
					}
				}
				if (meta instanceof MCBookMeta) {
					if (ma.containsKey("title")) {
						Mixed title = ma.get("title");
						if (title != null) {
							((MCBookMeta) meta).setTitle(title.val());
						}
					}
					if (ma.containsKey("author")) {
						Mixed author = ma.get("author");
						if (author != null) {
							((MCBookMeta) meta).setAuthor(author.val());
						}
					}
					if (ma.containsKey("pages")) {
						Mixed pages = ma.get("pages");
						if (pages == null) {
							//nothing
						} else if (pages instanceof CArray) {
							CArray pa = (CArray) pages;
							List<String> pl = new ArrayList<String>();
							for (int j = 0; j < pa.size(); j++) {
								pl.add(pa.get(j).val());
							}
							((MCBookMeta) meta).setPages(pl);
						} else {
							throw new Exceptions.FormatException("Pages field was expected to be an array.", t);
						}
					}
				}
				if (meta instanceof MCSkullMeta) {
					if (ma.containsKey("owner")) {
						Mixed owner = ma.get("owner");
						if (owner != null) {
							((MCSkullMeta) meta).setOwner(owner.val());
						}
					}
				}
				if (meta instanceof MCEnchantmentStorageMeta) {
					if (ma.containsKey("stored")) {
						Mixed stored = ma.get("stored");
						if (stored == null) {
							//Still doing nothing
						} else if (stored instanceof CArray) {
							for (Map.Entry<MCEnchantment, Integer> ench : enchants((CArray) stored, t).entrySet()) {
								((MCEnchantmentStorageMeta) meta).addStoredEnchant(ench.getKey(), ench.getValue(), true);
							}
						} else {
							throw new Exceptions.FormatException("Stored field was expected to be an array of Enchantment arrays", t);
						}
					}
				}
				if (meta instanceof MCPotionMeta) {
					if (ma.containsKey("potions")) {
						Mixed effects = ma.get("potions", t);
						if (effects instanceof CArray) {
							for (MCLivingEntity.MCEffect e : potions((CArray) effects, t)) {
								((MCPotionMeta) meta).addCustomEffect(e.getPotionID(), e.getStrength(),
										e.getSecondsRemaining(), e.isAmbient(), true, t);
							}
						} else {
							throw new Exceptions.FormatException("Effects was expected to be an array of potion arrays.", t);
						}
					}
					if (ma.containsKey("main")) {
						((MCPotionMeta) meta).setMainEffect(ma.get("main", t).primitive(t).castToInt32(t));
					}
				}
			} catch(Exception ex) {
				throw new Exceptions.FormatException("Could not get ItemMeta from the given information.", t);
			}
		} else {
			throw new Exceptions.FormatException("An array was expected but recieved " + c + " instead.", t);
		}
		return meta;
	}

    public CArray exception(ConfigRuntimeException e, Target t) {
		CArray ex = new CArray(t);
		ex.push(new CString(e.getExceptionType().toString(), t));
		ex.push(new CString(e.getMessage(), t));
		ex.push(new CString((e.getFile() != null ? e.getFile().getAbsolutePath() : "null"), t));
		ex.push(new CInt(e.getLineNum(), t));
		return ex;
    }
	
	/**
	 * Returns a CArray given an MCColor. It will be in the format
	 * array(r: 0, g: 0, b: 0)
	 * @param color
	 * @param t
	 * @return 
	 */
	public CArray color(MCColor color, Target t){
		CArray ca = new CArray(t);
		ca.set("r", new CInt(color.getRed(), t), t);
		ca.set("g", new CInt(color.getGreen(), t), t);
		ca.set("b", new CInt(color.getBlue(), t), t);
		return ca;
	}
	
	/**
	 * Returns an MCColor given a colorArray, which supports the following
	 * three format types (in this order of priority)
	 * array(r: 0, g: 0, b: 0)
	 * array(red: 0, green: 0, blue: 0)
	 * array(0, 0, 0)
	 * @param color
	 * @param t
	 * @return 
	 */
	public MCColor color(CArray color, Target t){
		int red;
		int green;
		int blue;
		if(color.containsKey("r")){
			red = color.get("r").primitive(t).castToInt32(t);
		} else if(color.containsKey("red")){
			red = color.get("red").primitive(t).castToInt32(t);
		} else {
			red = color.get(0).primitive(t).castToInt32(t);
		}
		if(color.containsKey("g")){
			green = color.get("g").primitive(t).castToInt32(t);
		} else if(color.containsKey("green")){
			green = color.get("green").primitive(t).castToInt32(t);
		} else {
			green = color.get(1).primitive(t).castToInt32(t);
		}
		if(color.containsKey("b")){
			blue = color.get("b").primitive(t).castToInt32(t);
		} else if(color.containsKey("blue")){
			blue = color.get("blue").primitive(t).castToInt32(t);
		} else {
			blue = color.get(2).primitive(t).castToInt32(t);
		}
		return StaticLayer.GetConvertor().GetColor(red, green, blue);
	}
	
	public CArray velocity(MCEntity.Velocity v, Target t) {
		double x,y,z,mag;
		x = y = z = mag = 0;
		if (v != null) {
			x = v.x;
			y = v.y;
			z = v.z;
			mag = v.magnitude;
		}
		CArray ret = CArray.GetAssociativeArray(t);
		ret.set("magnitude", new CDouble(mag, t), t);
		ret.set("x", new CDouble(x, t), t);
		ret.set("y", new CDouble(y, t), t);
		ret.set("z", new CDouble(z, t), t);
		return ret;
	}

	public MCEntity.Velocity velocity(Mixed c, Target t) {
		CArray va;
		double x, y, z, mag;
		x = y = z = mag = 0;
		if (c instanceof CArray) {
			va = (CArray) c;
			if (va.containsKey("x")) {
				x = va.get("x").primitive(t).castToDouble(t);
			}
			if (va.containsKey("y")) {
				y = va.get("y").primitive(t).castToDouble(t);
			}
			if (va.containsKey("z")) {
				z = va.get("z").primitive(t).castToDouble(t);
			}
			if (!va.containsKey("x") && !va.containsKey("y") && !va.containsKey("z")) {
				switch ((int) va.size()) {
				case 4:
					z = va.get(3).primitive(t).castToDouble(t);
					y = va.get(2).primitive(t).castToDouble(t);
					x = va.get(1).primitive(t).castToDouble(t);
					break;
				case 3:
					z = va.get(2).primitive(t).castToDouble(t);
				case 2:
					y = va.get(1).primitive(t).castToDouble(t);
				case 1:
					x = va.get(0).primitive(t).castToDouble(t);
				}
			}
			return new MCEntity.Velocity(mag, x, y, z);
		} else {
			throw new Exceptions.FormatException("Expected an array but recieved " + c, t);
		}
	}
	
	public CArray enchants(Map<MCEnchantment, Integer> map, Target t) {
		CArray ret = new CArray(t);
		for (Map.Entry<MCEnchantment, Integer> entry : map.entrySet()) {
			CArray eObj = CArray.GetAssociativeArray(t);
			eObj.set("etype", new CString(entry.getKey().getName(), t), t);
			eObj.set("elevel", new CInt(entry.getValue(), t), t);
			ret.push(eObj);
		}
		return ret;
	}
	
	public Map<MCEnchantment,Integer> enchants(CArray enchantArray, Target t) {
		Map<MCEnchantment,Integer> ret = new HashMap<MCEnchantment,Integer>();
		for (String key : enchantArray.keySet()) {
			try {
				CArray ea = (CArray) enchantArray.get(key, t);
				MCEnchantment etype = StaticLayer.GetConvertor().GetEnchantmentByName(ea.get("etype", t).val());
				int elevel = ea.get("elevel", t).primitive(t).castToInt32(t);
				if (etype == null) {
					throw new ConfigRuntimeException("Unknown enchantment type at " + key, 
							ExceptionType.EnchantmentException, t);
				}
				ret.put(etype, elevel);
			} catch (ClassCastException cce) {
				throw new ConfigRuntimeException("Expected an array at index " + key, ExceptionType.FormatException, t);
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
			effect.set("ambient", new CBoolean(eff.isAmbient(), t), t);
			ea.push(effect);
		}
		return ea;
	}
	
	public List<MCLivingEntity.MCEffect> potions(CArray ea, Target t) {
		List<MCLivingEntity.MCEffect> ret = new ArrayList<MCLivingEntity.MCEffect>();
		for (String key : ea.keySet()) {
			if (ea.get(key, t) instanceof CArray) {
				CArray effect = (CArray) ea.get(key, t);
				int potionID = 0, strength = 0, seconds = 30;
				boolean ambient = false;
				if (effect.containsKey("id")) {
					potionID = effect.get("id", t).primitive(t).castToInt32(t);
				} else {
					throw new Exceptions.FormatException("No potion ID was given at index " + key, t);
				}
				if (effect.containsKey("strength")) {
					strength = effect.get("strength", t).primitive(t).castToInt32(t);
				}
				if (effect.containsKey("seconds")) {
					seconds = effect.get("seconds", t).primitive(t).castToInt32(t);
				}
				if (effect.containsKey("ambient")) {
					ambient = effect.get("ambient", t).primitive(t).castToBoolean();
				}
				ret.add(new MCLivingEntity.MCEffect(potionID, strength, seconds, ambient));
			} else {
				throw new Exceptions.FormatException("Expected a potion array at index" + key, t);
			}
		}
		return ret;
	}
}
