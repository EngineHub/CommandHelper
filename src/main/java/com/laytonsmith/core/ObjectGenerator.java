

package com.laytonsmith.core;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.HashMap;
import java.util.Map;

/**
 * This file is responsible for converting CH objects into server objects, and
 * vice versa
 *
 * @author layton
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
        CArray ca = new CArray(Target.UNKNOWN);
        Construct x = new CDouble(l.getX(), Target.UNKNOWN);
        Construct y = new CDouble(l.getY(), Target.UNKNOWN);
        Construct z = new CDouble(l.getZ(), Target.UNKNOWN);
        Construct world = new CString(l.getWorld().getName(), Target.UNKNOWN);
        Construct yaw = new CDouble(l.getYaw(), Target.UNKNOWN);
        Construct pitch = new CDouble(l.getPitch(), Target.UNKNOWN);
        ca.forceAssociativeMode();
        ca.set("0", x);
        ca.set("1", y);
        ca.set("2", z);
        ca.set("3", world);
        ca.set("4", yaw);
        ca.set("5", pitch);
        ca.set("x", x);
        ca.set("y", y);
        ca.set("z", z);
        ca.set("world", world);
        ca.set("yaw", yaw);
        ca.set("pitch", pitch);
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
            throw new ConfigRuntimeException("Expecting an array, received " + c.getCType(), ExceptionType.FormatException, t);
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
                x = Static.getNumber(array.get(0, t));
                y = Static.getNumber(array.get(1, t));
                z = Static.getNumber(array.get(2, t));
            } else if (array.size() == 4) {
                //x, y, z, world
                x = Static.getNumber(array.get(0, t));
                y = Static.getNumber(array.get(1, t));
                z = Static.getNumber(array.get(2, t));
                world = Static.getServer().getWorld(array.get(3, t).val());
            } else if (array.size() == 5) {
                //x, y, z, yaw, pitch, with given world
                x = Static.getNumber(array.get(0, t));
                y = Static.getNumber(array.get(1, t));
                z = Static.getNumber(array.get(2, t));
                yaw = (float) Static.getNumber(array.get(3, t));
                pitch = (float) Static.getNumber(array.get(4, t));
            } else if (array.size() == 6) {
                //All have been given
                x = Static.getNumber(array.get(0, t));
                y = Static.getNumber(array.get(1, t));
                z = Static.getNumber(array.get(2, t));
                world = Static.getServer().getWorld(array.get(3, t).val());
                yaw = (float) Static.getNumber(array.get(4, t));
                pitch = (float) Static.getNumber(array.get(5, t));
            } else {
                throw new ConfigRuntimeException("Expecting a Location array, but the array did not meet the format specifications", ExceptionType.FormatException, t);
            }
        }
        if (array.containsKey("x")) {
            x = Static.getNumber(array.get("x"));
        }
        if (array.containsKey("y")) {
            y = Static.getNumber(array.get("y"));
        }
        if (array.containsKey("z")) {
            z = Static.getNumber(array.get("z"));
        }
        if (array.containsKey("world")) {
            world = Static.getServer().getWorld(array.get("world").val());
        }
        if (array.containsKey("yaw")) {
            yaw = (float) Static.getDouble(array.get("yaw"));
        }
        if (array.containsKey("pitch")) {
            pitch = (float) Static.getDouble(array.get("pitch"));
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
            return new CNull(t);
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
            CArray enchObj = new CArray(t);
            enchObj.forceAssociativeMode();
            enchObj.set("etype", new CString(entry.getKey().getName(), t));
            enchObj.set("elevel", new CInt(entry.getValue(), t));
            enchants.push(enchObj);
        }
        CArray ret = new CArray(t);
        ret.forceAssociativeMode();
        ret.set("type", Integer.toString(type));
        ret.set("data", Integer.toString(data));
        ret.set("qty", Integer.toString(qty));
        ret.set("enchants", enchants);
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
    public MCItemStack item(Construct i, Target t) {
        if (i instanceof CNull) {
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
        MCItemStack ret = StaticLayer.GetItemStack(type, qty);
        ret.setData(data);
        ret.setDurability((short) data);
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
    
    public CArray exception(ConfigRuntimeException e, Target t) {
		CArray ex = new CArray(t);
		ex.push(new CString(e.getExceptionType().toString(), t));
		ex.push(new CString(e.getMessage(), t));
		ex.push(new CString((e.getFile() != null ? e.getFile().getAbsolutePath() : "null"), t));
		ex.push(new CInt(e.getLineNum(), t));
		return ex;
    }
}
