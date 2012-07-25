package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

/**
 *
 * @author layton
 */
public class InventoryManagement {
    public static String docs(){
        return "Provides methods for managing inventory related tasks.";
    }
    
    @api
    public static class pinv extends AbstractFunction {

        public String getName() {
            return "pinv";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

        public String docs() {
            return "mixed {[player, [index]]} Gets the inventory information for the specified player, or the current player if none specified. If the index is specified, only the slot "
                    + " given will be returned."
                    + " The index of the array in the array is 0 - 35, 100 - 103, which corresponds to the slot in the players inventory. To access armor"
                    + " slots, you may also specify the index. (100 - 103). The quick bar is 0 - 8. If index is null, the item in the player's hand is returned, regardless"
                    + " of what slot is selected. If there is no item at the slot specified, null is returned."
                    + " If all slots are requested, an associative array of item objects is returned, and if"
                    + " only one item is requested, just that single item object is returned. An item object"
                    + " consists of the following associative array(type: The id of the item, data: The data value of the item,"
                    + " or the damage if a damagable item, qty: The number of items in their inventory, enchants: An array"
                    + " of enchant objects, with 0 or more associative arrays which look like:"
                    + " array(etype: The type of enchantment, elevel: The strength of the enchantment))";
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.PlayerOfflineException, Exceptions.ExceptionType.CastException, Exceptions.ExceptionType.RangeException};
        }

        public boolean isRestricted() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_1_3;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            Integer index = -1;
            boolean all = false;
            MCPlayer m = null;
            if (args.length == 0) {
                all = true;
                if (p instanceof MCPlayer) {
                    m = (MCPlayer) p;
                }
            } else if (args.length == 1) {
                all = true;
                m = Static.GetPlayer(args[0]);
            } else if (args.length == 2) {
                if (args[1] instanceof CNull) {
                    index = null;
                } else {
                    index = (int) Static.getInt(args[1]);
                }
                all = false;
                m = Static.GetPlayer(args[0]);
            }

            if(all){
                CArray ret = new CArray(t);
                ret.forceAssociativeMode();
                for(int i = 0; i < 36; i++){
                    ret.set(i, getInvSlot(m, i, t));
                }
                for(int i = 100; i < 104; i++){
                    ret.set(i, getInvSlot(m, i, t));
                }
                return ret;
            } else {
                return getInvSlot(m, index, t);
            }
        }

        private Construct getInvSlot(MCPlayer m, Integer slot, Target t) {
            if(slot == null){
                return ObjectGenerator.GetGenerator().item(m.getItemInHand(), t);
            }
            MCInventory inv = m.getInventory();
            if(slot.equals(36)){
                slot = 100;
            }
            if(slot.equals(37)){
                slot = 101;
            }
            if(slot.equals(38)){
                slot = 102;
            }
            if(slot.equals(39)){
                slot = 103;
            }
            MCItemStack is;
            if(slot >= 0 && slot <= 35){
                is = inv.getItem(slot);
            } else if(slot.equals(100)){
                is = inv.getBoots();
            } else if(slot.equals(101)){
                is = inv.getLeggings();
            } else if(slot.equals(102)){
                is = inv.getChestplate();
            } else if(slot.equals(103)){
                is = inv.getHelmet();
            } else {
                throw new ConfigRuntimeException("Slot index must be 0-35, or 100-103", Exceptions.ExceptionType.RangeException, t);
            }
            return ObjectGenerator.GetGenerator().item(is, t);
        }
    }

    @api
    public static class set_pinv extends AbstractFunction {

        public String getName() {
            return "set_pinv";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3, 4, 5, 7};
        }

        public String docs() {
            return "void {[player], pinvArray} Sets a player's inventory to the specified inventory object."
                    + " An inventory object is one that matches what is returned by pinv(), so set_pinv(pinv()),"
                    + " while pointless, would be a correct call. The array must be associative, "
                    + " however, it may skip items, in which case, only the specified values will be changed. If"
                    + " a key is out of range, or otherwise improper, a warning is emitted, and it is skipped,"
                    + " but the function will not fail as a whole. A simple way to set one item in a player's"
                    + " inventory would be: set_pinv(array(2: array(type: 1, qty: 64))) This sets the player's second slot"
                    + " to be a stack of stone. set_pinv(array(103: array(type: 298))) gives them a hat. To set the"
                    + " item in hand, use something like set_pinv(array(null: array(type: 298))), where"
                    + " the key is null. If you set a null key in addition to an entire inventory set, only"
                    + " one item will be used (which one is undefined). Note that this uses the unsafe"
                    + " enchantment mechanism to add enchantments, so any enchantment value will work. If"
                    + " type uses the old format (for instance, \"35:11\"), then the second number is taken"
                    + " to be the data, making this backwards compatible (and sometimes more convenient).";

        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.PlayerOfflineException, Exceptions.ExceptionType.CastException, Exceptions.ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_2_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            MCCommandSender p = env.GetCommandSender();
            MCPlayer m = null;
            if (p instanceof MCPlayer) {
                m = (MCPlayer) p;
            }
            Construct arg;
            if(args.length == 2){
                m = Static.GetPlayer(args[0]);
                arg = args[1];
            } else if(args.length == 1){
                arg = args[0];
            } else {
                throw new ConfigRuntimeException("The old format for set_pinv has been deprecated. Please update your script.", t);
            }
            if(!(arg instanceof CArray)){
                throw new ConfigRuntimeException("Expecting an array as argument " + (args.length==1?"1":"2"), Exceptions.ExceptionType.CastException, t);
            }
            CArray array = (CArray)arg;
            for(String key : array.keySet()){
                try{
                    int index = -2;
                    try{
                        index = Integer.parseInt(key);
                    } catch(NumberFormatException e){
                        if(key.isEmpty()){
                            //It was a null key
                            index = -1;
                        } else {
                            throw e;
                        }
                    }
                    if(index == -1){
                        MCItemStack is = ObjectGenerator.GetGenerator().item(array.get(""), t);
                        m.setItemInHand(is);
                    } else {
                        MCItemStack is = ObjectGenerator.GetGenerator().item(array.get(index), t);
                        if(index >= 0 && index <= 35){
                            m.getInventory().setItem(index, is);
                        } else if(index == 100){
                            m.getInventory().setBoots(is);
                        } else if(index == 101){
                            m.getInventory().setLeggings(is);
                        } else if(index == 102){
                            m.getInventory().setChestplate(is);
                        } else if(index == 103){
                            m.getInventory().setHelmet(is);
                        } else {
                            ConfigRuntimeException.DoWarning("Out of range value (" + index + ") found in array passed to set_pinv(), so ignoring.");
                        }
                    }
                } catch(NumberFormatException e){
                    ConfigRuntimeException.DoWarning("Expecting integer value for key in array passed to set_pinv(), but \"" + key + "\" was found. Ignoring.");
                }
            }
            return new CVoid(t);
        }
    }
    
    @api public static class phas_item extends AbstractFunction{

        public String getName() {
            return "phas_item";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "int {[player], itemId} Returns the quantity of the specified item"
                    + " that the player is carrying (including armor slots)."
                    + " This counts across all slots in"
                    + " inventory. Recall that 0 is false, and anything else is true,"
                    + " so this can be used to get the total, or just see if they have"
                    + " the item. itemId can be either a plain number, or a 0:0 number,"
                    + " indicating a data value.";
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.PlayerOfflineException, Exceptions.ExceptionType.FormatException,
                Exceptions.ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }
        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.GetPlayer();
            String item;
            if(args.length == 1){
                item = args[0].val();
            } else {
                p = Static.GetPlayer(args[0]);
                item = args[1].val();
            }
            MCItemStack is = Static.ParseItemNotation(this.getName(), item, 0, t);
            MCInventory inv = p.getInventory();
            int total = 0;
            for(int i = 0; i < 36; i++){
                MCItemStack iis = inv.getItem(i);
                total += total(is, iis);
            }
            total += total(is, inv.getBoots());
            total += total(is, inv.getLeggings());
            total += total(is, inv.getChestplate());
            total += total(is, inv.getHelmet());
            return new CInt(total, t);
        }
        
        private int total(MCItemStack is, MCItemStack iis){
            if(iis.getTypeId() == is.getTypeId() && iis.getData().getData() == is.getData().getData()){
                int i = iis.getAmount();
                if(i < 0){
                    //Infinite stack
                    i = iis.maxStackSize();
                }
                return i;
            }         
            return 0;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
    }
    
    @api public static class pitem_slot extends AbstractFunction{

        public String getName() {
            return "pitem_slot";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "array {[player], itemID} Given an item id, returns the slot numbers"
                    + " that the matching item has at least one item in.";
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.CastException, Exceptions.ExceptionType.FormatException,
                Exceptions.ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }
        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.GetPlayer();
            String item;
            if(args.length == 1){
                item = args[0].val();
            } else {
                p = Static.GetPlayer(args[0]);
                item = args[1].val();
            }
            MCItemStack is = Static.ParseItemNotation(this.getName(), item, 0, t);
            MCInventory inv = p.getInventory();
            CArray ca = new CArray(t);
            for(int i = 0; i < 36; i++){
                if(match(is, inv.getItem(i))){
                    ca.push(new CInt(i, t));
                }
            }
            if(match(is, inv.getBoots())){
                ca.push(new CInt(100, t));
            }
            if(match(is, inv.getLeggings())){
                ca.push(new CInt(101, t));
            }
            if(match(is, inv.getChestplate())){
                ca.push(new CInt(102, t));
            }
            if(match(is, inv.getHelmet())){
                ca.push(new CInt(103, t));
            }
            return ca;
        }
        
        private boolean match(MCItemStack is, MCItemStack iis){
            return (is.getTypeId() == iis.getTypeId() && is.getData().getData() == iis.getData().getData());
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
    }
    
    @api public static class pgive_item extends AbstractFunction{

        public String getName() {
            return "pgive_item";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "int {[player], itemID, qty} Gives a player the specified item * qty."
                    + " Unlike set_pinv(), this does not specify a slot. The qty is distributed"
                    + " in the player's inventory, first filling up slots that have the same item"
                    + " type, up to the max stack size, then fills up empty slots, until either"
                    + " the entire inventory is filled, or the entire amount has been given."
                    + " The number of items actually given is returned, which will be less than"
                    + " or equal to the quantity provided. This function will not touch the player's"
                    + " armor slots however.";
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.CastException, Exceptions.ExceptionType.FormatException,
                Exceptions.ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }
        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.GetPlayer();
            MCItemStack is;
            if(args.length == 2){
                is = Static.ParseItemNotation(this.getName(), args[0].val(), (int)Static.getInt(args[1]), t);
            } else {
                p = Static.GetPlayer(args[0]);
                is = Static.ParseItemNotation(this.getName(), args[1].val(), (int)Static.getInt(args[2]), t);
            }
            int total = is.getAmount();
            int remaining = is.getAmount();
            MCInventory inv = p.getInventory();
            for(int i = 0; i < 36; i++){
                MCItemStack iis = inv.getItem(i);
                if(remaining <= 0){
                    break;
                }
                if(match(is, iis) || iis.getTypeId() == 0){
                    //It's either the same item stack, or air.
                    int currentQty = 0;
                    int max = is.maxStackSize();
                    if(iis.getTypeId() != 0){
                        currentQty = iis.getAmount();
                    }
                    if(currentQty < 0){
                        //Infinite stack. Assume max stack size.
                        currentQty = is.maxStackSize();
                    }
                    int left = max - currentQty;
                    int toGive;
                    if(left < remaining){
                        //We'll have to split this across more than this stack.
                        toGive = left;
                    } else {
                        //We can distribute the rest in this stack
                        toGive = remaining;
                    }
                    remaining -= toGive;
                    
                    //The total we are going to set the stack size to is toGive + currentQty
                    int replace = toGive + currentQty;
                    
                    inv.setItem(i, StaticLayer.GetItemStack(is.getTypeId(), is.getData().getData(), replace));
                }
            }
            return new CInt(total - remaining, t);
        }
       
        private boolean match(MCItemStack is, MCItemStack iis){
            return (is.getTypeId() == iis.getTypeId() && is.getData().getData() == iis.getData().getData());
        }
        
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
    }
    
    @api public static class ptake_item extends AbstractFunction{

        public String getName() {
            return "ptake_item";
        }

        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

        public String docs() {
            return "int {[player], itemID, qty} Works in reverse of pgive_item(), but"
                    + " returns the number of items actually taken, which will be"
                    + " from 0 to qty.";
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.CastException, Exceptions.ExceptionType.PlayerOfflineException,
                Exceptions.ExceptionType.FormatException};
        }

        public boolean isRestricted() {
            return true;
        }
        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            MCPlayer p = environment.GetPlayer();
            MCItemStack is;
            if(args.length == 2){
                is = Static.ParseItemNotation(this.getName(), args[0].val(), (int)Static.getInt(args[1]), t);
            } else {
                p = Static.GetPlayer(args[0]);
                is = Static.ParseItemNotation(this.getName(), args[1].val(), (int)Static.getInt(args[2]), t);
            }
            int total = is.getAmount();
            int remaining = is.getAmount();
            MCInventory inv = p.getInventory();
            for(int i = 35; i >= 0; i--){
                MCItemStack iis = inv.getItem(i);
                if(remaining <= 0){
                    break;
                }
                if(match(is, iis)){
                    //Take the minimum of either: remaining, or iis.getAmount()
                    int toTake = java.lang.Math.min(remaining, iis.getAmount());
                    remaining -= toTake;
                    int replace = iis.getAmount() - toTake;
                    if(replace == 0){
                        inv.setItem(i, StaticLayer.GetItemStack(0, 0));
                    } else {
                        inv.setItem(i, StaticLayer.GetItemStack(is.getTypeId(), is.getData().getData(), replace));
                    }
                }
            }
            return new CInt(total - remaining, t);
            
        }
        
        private boolean match(MCItemStack is, MCItemStack iis){
            return (is.getTypeId() == iis.getTypeId() && is.getData().getData() == iis.getData().getData());
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
    }
    
//    @api
//    public static class pinv_consolidate extends AbstractFunction {
//        
//        public String getName() {
//            return "pinv_consolidate";
//        }
//        
//        public Integer[] numArgs() {
//            return new Integer[]{0, 1};
//        }
//        
//        public String docs() {
//            return "void {[player]} Consolidates a player's inventory as much as possible."
//                    + " There is no guarantee anything will happen after this function"
//                    + " is called, and there is no way to specify details about how"
//                    + " consolidation occurs, however, the following heuristics are followed:"
//                    + " The hotbar items will not be moved from the hotbar, unless there are"
//                    + " two+ slots that have the same item. Items in the main inventory area"
//                    + " will be moved closer to the bottom of the main inventory. No empty slots"
//                    + " will be filled in the hotbar.";
//        }
//        
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{};
//        }
//        
//        public boolean isRestricted() {
//            return true;
//        }
//        
//        public boolean preResolveVariables() {
//            return true;
//        }
//        
//        public Boolean runAsync() {
//            return false;
//        }
//        
//        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
//            MCPlayer p = environment.GetPlayer();
//            if(args.length == 1){
//                p = Static.GetPlayer(args[0]);
//            }
//            //First, we need to address the hotbar
//            for(int i = 0; i < 10; i++){
//                //If the stack size is maxed out, we're done.
//            }
//            
//            return new CVoid(t);
//        }
//        
//        public CHVersion since() {
//            return CHVersion.V3_3_1;
//        }
//    }
}
