

package com.laytonsmith.core.events.drivers;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.*;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author EntityReborn
 */
public class BlockEvents {

    public static String docs() {
        return "Contains events related to a block";
    }

    @api
    public static class block_break extends AbstractEvent {

        public String getName() {
            return "block_break";
        }

        public String docs() {
            return "{player: <string match> | type: <string match> | data: <string match>} "
                    + "This event is called when a block is broken. "
                    + "Cancelling the event cancels the breakage. "
                    + "{player: The player's name | block: An array with "
                    + "keys 'type' (int), 'data' (int), 'X' (int), 'Y' (int), 'Z' (int) "
                    + "and 'world' (string) for the physical location of the block | "
                    + "drops: an array of arrays (with keys 'type' (string), "
                    + "'qty' (int), 'data' (int), 'enchants' (array)) of items the block will drop} "
                    + "{drops} "
                    + "{player|block|drops}";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public Driver driver() {
            return Driver.BLOCK_BREAK;
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
                throws PrefilterNonMatchException {
            if (e instanceof MCBlockBreakEvent) {
                MCBlockBreakEvent event = (MCBlockBreakEvent) e;

                if (prefilter.containsKey("player")) {
                    if (!event.getPlayer().getName().equals(prefilter.get("player").val())) {
                        return false;
                    }
                }

                if (prefilter.containsKey("type")) {
                    Construct v = prefilter.get("type");

                    if (v instanceof CInt) {
                        int val = Integer.parseInt(v.val());

                        if (event.getBlock().getTypeId() != val) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                if (prefilter.containsKey("data")) {
                    Construct v = prefilter.get("data");

                    if (v instanceof CInt) {
                        int val = Integer.parseInt(v.val());

                        if ((int) event.getBlock().getData() != val) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }

            return true;
        }

        public BindableEvent convert(CArray manualObject) {
            return null;
        }

        public Map<String, Construct> evaluate(BindableEvent e)
                throws EventException {

            MCBlockBreakEvent event = (MCBlockBreakEvent) e;
            Map<String, Construct> map = evaluate_helper(event);

            map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));

            CArray blk = new CArray(Target.UNKNOWN);

            int blktype = event.getBlock().getTypeId();
            blk.set("type", new CInt(blktype, Target.UNKNOWN), Target.UNKNOWN);

            int blkdata = event.getBlock().getData();
            blk.set("data", new CInt(blkdata, Target.UNKNOWN), Target.UNKNOWN);

            blk.set("X", new CInt(event.getBlock().getX(), Target.UNKNOWN), Target.UNKNOWN);
            blk.set("Y", new CInt(event.getBlock().getY(), Target.UNKNOWN), Target.UNKNOWN);
            blk.set("Z", new CInt(event.getBlock().getZ(), Target.UNKNOWN), Target.UNKNOWN);
            blk.set("world", new CString(event.getBlock().getWorld().getName(), Target.UNKNOWN), Target.UNKNOWN);

            map.put("block", blk);

            CArray drops = new CArray(Target.UNKNOWN);
            Collection<MCItemStack> items = event.getBlock().getDrops();
            for (Iterator<MCItemStack> iter = items.iterator(); iter.hasNext();) {
                MCItemStack stack = new BukkitMCItemStack((MCItemStack) iter.next());
                CArray item = (CArray) ObjectGenerator.GetGenerator().item(stack, Target.UNKNOWN);
                drops.push(item);
            }
            map.put("drops", drops);

            return map;
        }

        public boolean modifyEvent(String key, Construct value,
                BindableEvent e) {

            MCBlockBreakEvent event = (MCBlockBreakEvent) e;
            MCBlock blk = event.getBlock();

            if (key.equals("drops")) {
                blk.setTypeId(0);

                if (value instanceof CArray) {
                    CArray arr = (CArray) value;

                    for (int i = 0; i < arr.size(); i++) {
                        CArray item = (CArray) arr.get(i);
                        MCItemStack stk = ObjectGenerator.GetGenerator().item(item, Target.UNKNOWN);
                        
                        blk.getWorld().dropItemNaturally(
                            StaticLayer.GetLocation(
                                    blk.getWorld(), 
                                    blk.getX(), 
                                    blk.getY(), 
                                    blk.getZ()), 
                            stk);
                    }

                    return true;
                }
            }

            return false;
        }
    }

    @api
    public static class block_place extends AbstractEvent {

        public String getName() {
            return "block_place";
        }

        public String docs() {
            return "{player: <string match> | type: <string match> | data: <string match>} "
                    + "This event is called when a player places a block. "
                    + "Cancelling the event cancels placing the block."
                    + "{player: The player's name | type: numerical type id of the block being "
                    + "placed | X: the X coordinate of the block | Y: the Y coordinate of the block | "
                    + "Z: the Z coordinate of the block| world: the world of the block | "
                    + "data: the data value for the block being placed | against: the block "
                    + "being placed against | oldblock: the blocktype and blockdata being replaced"
					+ " | location: A locationArray for this block} "
                    + "{type|data} "
                    + "{player|X|Y|Z|world|type|data|against|oldblock}";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public Driver driver() {
            return Driver.BLOCK_PLACE;
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
                throws PrefilterNonMatchException {
            if (e instanceof MCBlockPlaceEvent) {
                MCBlockPlaceEvent event = (MCBlockPlaceEvent) e;

                if (prefilter.containsKey("player")) {
                    if (!event.getPlayer().getName().equals(prefilter.get("player").val())) {
                        return false;
                    }
                }

                if (prefilter.containsKey("type")) {
                    Construct v = prefilter.get("type");

                    if (v instanceof CInt) {
                        int val = Integer.parseInt(v.val());

                        if (event.getBlock().getTypeId() != val) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                if (prefilter.containsKey("data")) {
                    Construct v = prefilter.get("data");

                    if (v instanceof CInt) {
                        int val = Integer.parseInt(v.val());

                        if ((int) event.getBlock().getData() != val) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }

            return true;
        }

        public BindableEvent convert(CArray manualObject) {
            return null;
        }

        public Map<String, Construct> evaluate(BindableEvent e)
                throws EventException {
            MCBlockPlaceEvent event = (MCBlockPlaceEvent) e;
            Map<String, Construct> map = evaluate_helper(e);
            MCBlock blk = event.getBlock();

            map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));

            map.put("X", new CInt(blk.getX(), Target.UNKNOWN));
            map.put("Y", new CInt(blk.getY(), Target.UNKNOWN));
            map.put("Z", new CInt(blk.getZ(), Target.UNKNOWN));
            map.put("world", new CString(blk.getWorld().getName(), Target.UNKNOWN));
			
			CArray location = ObjectGenerator.GetGenerator()
					.location(StaticLayer.GetLocation(blk.getWorld(), blk.getX(), blk.getY(), blk.getZ()));
			map.put("location", location);

            int blktype = event.getBlock().getTypeId();
            map.put("type", new CInt(blktype, Target.UNKNOWN));

            int blkdata = event.getBlock().getData();
            map.put("data", new CInt(blkdata, Target.UNKNOWN));

            CArray agst = new CArray(Target.UNKNOWN);
            MCBlock agstblk = event.getBlockAgainst();
            int againsttype = agstblk.getTypeId();
            agst.set("type", new CInt(againsttype, Target.UNKNOWN), Target.UNKNOWN);
            int againstdata = agstblk.getData();
            agst.set("data", new CInt(againstdata, Target.UNKNOWN), Target.UNKNOWN);
            agst.set("X", new CInt(agstblk.getX(), Target.UNKNOWN), Target.UNKNOWN);
            agst.set("Y", new CInt(agstblk.getY(), Target.UNKNOWN), Target.UNKNOWN);
            agst.set("Z", new CInt(agstblk.getZ(), Target.UNKNOWN), Target.UNKNOWN);
            map.put("against", agst);

            MCBlockState old = event.getBlockReplacedState();
            CArray oldarr = new CArray(Target.UNKNOWN);
            oldarr.set("type", new CInt(old.getTypeId(), Target.UNKNOWN), Target.UNKNOWN);
            oldarr.set("data", new CInt(old.getData().getData(), Target.UNKNOWN), Target.UNKNOWN);
            map.put("oldblock", oldarr);

            return map;
        }

        public boolean modifyEvent(String key, Construct value,
                BindableEvent e) {
            MCBlockPlaceEvent event = (MCBlockPlaceEvent) e;

            if (key.equals("type")) {
                if (value instanceof CInt) {
                    int i = Integer.parseInt(value.val());
                    event.getBlock().setTypeId(i);

                    return true;
                }
            } else if (key.equals("data")) {
                if (value instanceof CInt) {
                    byte b;

                    try {
                        b = Byte.parseByte(value.val());
                    }
                    catch (NumberFormatException exc) {
                        if (Integer.parseInt(value.val()) < 0) {
                            b = 0;
                        } else {
                            b = Byte.MAX_VALUE;
                        }
                    }

                    event.getBlock().setData(b);
                    return true;
                }
            }
            return false;
        }
    }

    @api
    public static class sign_changed extends AbstractEvent {

        public String getName() {
            return "sign_changed";
        }

        public String docs() {
            return "{player: <string match> | 1: <macro> | 2: <macro> | "
                    + "3: <macro> | 4: <macro> } "
                    + "This event is called when a player changes a sign. "
                    + "Cancelling the event cancels any edits completely."
                    + "{player: The player's name | location: An array with keys 'X', 'Y', 'Z' and 'world' "
                    + "for the physical location of the sign | text: An array with keys 0 thru 3 defining "
                    + "every line on the sign}"
                    + "{1|2|3|4|text: An array with keys 0 thru 3 defining every line on the sign}"
                    + "{player|location|text}";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public Driver driver() {
            return Driver.SIGN_CHANGED;
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCSignChangeEvent) {
                MCSignChangeEvent sce = (MCSignChangeEvent) e;

                if (prefilter.containsKey("player")) {
                    if (!sce.getPlayer().getName().equals(prefilter.get("player").val())) {
                        return false;
                    }
                }

                Prefilters.match(prefilter, "1", sce.getLine(0), Prefilters.PrefilterType.REGEX);
                Prefilters.match(prefilter, "2", sce.getLine(1), Prefilters.PrefilterType.REGEX);
                Prefilters.match(prefilter, "3", sce.getLine(2), Prefilters.PrefilterType.REGEX);
                Prefilters.match(prefilter, "4", sce.getLine(3), Prefilters.PrefilterType.REGEX);

                return true;
            }
            return false;
        }

        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCSignChangeEvent) {
                MCSignChangeEvent sce = (MCSignChangeEvent) e;
                Map<String, Construct> map = evaluate_helper(e);

                map.put("player", new CString(sce.getPlayer().getName(), Target.UNKNOWN));

                map.put("text", sce.getLines());

                CArray blk = new CArray(Target.UNKNOWN);
                blk.set("X", new CInt(sce.getBlock().getX(), Target.UNKNOWN), Target.UNKNOWN);
                blk.set("Y", new CInt(sce.getBlock().getY(), Target.UNKNOWN), Target.UNKNOWN);
                blk.set("Z", new CInt(sce.getBlock().getZ(), Target.UNKNOWN), Target.UNKNOWN);
                blk.set("world", new CString(sce.getBlock().getWorld().getName(), Target.UNKNOWN), Target.UNKNOWN);
                map.put("location", blk);

                return map;
            } else {
                throw new EventException("Cannot convert e to MCSignChangeEvent");
            }
        }

        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCSignChangeEvent) {
                MCSignChangeEvent sce = (MCSignChangeEvent) event;

                // Allow changing everything at once.
                if (key.equals("text")) {
                    if (!( value instanceof CArray )) {
                        return false;
                    }

                    CArray val = (CArray) value;
                    if (val.size() != 4) {
                        return false;
                    }

                    String[] lines = {"","","",""};

                    for (int i = 0; i < 4; i++) {
                        lines[i] = val.get(i).toString();
                    }

                    sce.setLines(lines);

                    return true;
                }

                int index;
                // Allow changing just one line at a time.
                if (key.equals("1")) {
                    index = 0;
                } else if (key.equals("2")) {
                    index = 1;
                } else if (key.equals("3")) {
                    index = 2;
                } else if (key.equals("4")) {
                    index = 3;
                } else {
                    return false;
                }

                if (value instanceof CNull) {
                    sce.setLine(index, "");
                    return "".equals(sce.getLine(index).toString());
                } else {
                    sce.setLine(index, value.val());
                    return ( sce.getLine(index).toString() == null ? value.val() == null : sce.getLine(index).toString().equals(value.val()) );
                }
            }

            return false;
        }

        public BindableEvent convert(CArray manual) {
            MCSignChangeEvent e = EventBuilder.instantiate(
                    MCSignChangeEvent.class,
                    Static.GetPlayer(manual.get("player").val(), Target.UNKNOWN),
                    manual.get("1").val(), manual.get("2").val(),
                    manual.get("3").val(), manual.get("4").val());
            return e;
        }
    }
}
