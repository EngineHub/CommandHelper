package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.enums.MCIgniteCause;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.*;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
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
                    + "location: the locationArray of this block | drops | xp} "
                    + "{drops: an array of arrays (with keys 'type' (string), "
                    + "'qty' (int), 'data' (int), 'enchants' (array)) of items the block will drop | "
                    + "xp: the xp that this block will drop, if any} "
                    + "{drops|xp} "
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
			
			CArray location = ObjectGenerator.GetGenerator()
					.location(StaticLayer.GetLocation(event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ()));
			map.put("location", location);

            CArray drops = new CArray(Target.UNKNOWN);
            Collection<MCItemStack> items = event.getBlock().getDrops(event.getPlayer().getItemInHand());
            for (Iterator<MCItemStack> iter = items.iterator(); iter.hasNext();) {
                MCItemStack stack = new BukkitMCItemStack((MCItemStack) iter.next());
                CArray item = (CArray) ObjectGenerator.GetGenerator().item(stack, Target.UNKNOWN);
                drops.push(item);
            }
            map.put("drops", drops);
			
			map.put("xp", new CInt(event.getExpToDrop(), Target.UNKNOWN));

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
			
			if (key.equals("xp")) {
				if (value instanceof CInt) {
					
					int xp = Integer.parseInt(value.val());
					
					event.setExpToDrop(xp);
					
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
    public static class block_burn extends AbstractEvent {

        public String getName() {
            return "block_burn";
        }

        public String docs() {
            return "{type: <string match> | data: <string match>} "
                    + "This event is called when a block is burned. "
                    + "Cancelling the event cancels the burn. "
					+ "{block: An array with "
                    + "keys 'type' (int), 'data' (int), 'X' (int), 'Y' (int), 'Z' (int) "
                    + "and 'world' (string) for the physical location of the block | "
                    + "location: the locationArray of this block} "
					+ "{block}"
					+ "{block|location}";
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        public Driver driver() {
            return Driver.BLOCK_BURN;
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
                throws PrefilterNonMatchException {
            if (e instanceof MCBlockBurnEvent) {
                MCBlockBurnEvent event = (MCBlockBurnEvent) e;

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

            MCBlockBurnEvent event = (MCBlockBurnEvent) e;
            Map<String, Construct> map = evaluate_helper(event);

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

			CArray location = ObjectGenerator.GetGenerator()
					.location(StaticLayer.GetLocation(event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ()));
			map.put("location", location);

            return map;
        }

        public boolean modifyEvent(String key, Construct value,
                BindableEvent e) {

            MCBlockBreakEvent event = (MCBlockBreakEvent) e;
            MCBlock blk = event.getBlock();

            return false;
        }
    }

	@api
	public static class block_ignite extends AbstractEvent {

		@Override
		public String getName() {
			return "block_ignite";
		}

		@Override
		public String docs() {
			return "{player: <string match> | cause: <macro> | world: <macro>} "
					+ "This event is called when a block or entity is ignited."
					+ "{player: The player's name | ignitingentity: entity ID, if entity is ignited | ignitingblock:"
					+ " block ID, if block is ignited | location: the locationArray of block or entity | cause:"
					+ " the cause of ignition, one of: " + StringUtils.Join(MCIgniteCause.values(), ", ") + "}"
					+ "{}"
					+ "{player|cause|world}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_IGNITE;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if (e instanceof MCBlockIgniteEvent) {
				MCBlockIgniteEvent event = (MCBlockIgniteEvent) e;

				if (event.getPlayer() != null) {
					Prefilters.match(prefilter, "player", event.getPlayer().getName(), Prefilters.PrefilterType.MACRO);
				}

				Prefilters.match(prefilter, "cause", event.getCause().name(), Prefilters.PrefilterType.MACRO);
				Prefilters.match(prefilter, "world", event.getBlock().getWorld().getName(), Prefilters.PrefilterType.STRING_MATCH);
			}

			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if (e instanceof MCBlockIgniteEvent) {
				MCBlockIgniteEvent event = (MCBlockIgniteEvent) e;
				Map<String, Construct> map = evaluate_helper(e);

				if (event.getPlayer() != null) {
					map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
				}

				if (event.getIgnitingEntity() != null) {
					map.put("ignitingentity", new CInt(event.getIgnitingEntity().getEntityId(), Target.UNKNOWN));
				}

				if (event.getIgnitingBlock() != null) {
					map.put("ignitingblock", new CInt(event.getIgnitingBlock().getTypeId(), Target.UNKNOWN));
				}

				map.put("location", ObjectGenerator.GetGenerator().location(event.getBlock().getLocation()));
				map.put("cause", new CString(event.getCause().name(), Target.UNKNOWN));

				return map;
			} else {
				throw new EventException("Cannot convert e to MCBlockIgniteEvent");
			}
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
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
                    + "{player: The player's name | location: an array usable as a locationArray while also "
                    + "compatible with X,Y,Z,world indices | text: An array with keys 0 thru 3 defining "
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

                MCBlock blc = sce.getBlock();
                CArray location = ObjectGenerator.GetGenerator()
    					.location(StaticLayer.GetLocation(blc.getWorld(), blc.getX(), blc.getY(), blc.getZ()));
    			map.put("location", location);

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

	@api
	public static class block_dispense extends AbstractEvent {

		public String getName() {
			return "block_dispense";
		}

		public String docs() {
			return "{type: <string match> Type of dispenser | item: <item match> Item which is dispensed}"
					+ " This event is called when a dispenser dispense an item. Cancelling the event cancels dispensing."
					+ "{type: Type of dispenser | item: Item which is dispensed | velocity: Returns an associative array"
					+ " indicating the x/y/z components of item velocity. As a convenience, the magnitude is also included."
					+ " | location: Location of dispenser} "
					+ "{item|velocity} "
					+ "{type|item|velocity|location}";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public Driver driver() {
			return Driver.BLOCK_DISPENSE;
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if (e instanceof MCBlockDispenseEvent) {
				MCBlockDispenseEvent event = (MCBlockDispenseEvent) e;
				Prefilters.match(prefilter, "type",
						StaticLayer.GetConvertor().LookupMaterialName(event.getBlock().getTypeId()), PrefilterType.STRING_MATCH);
				Prefilters.match(prefilter, "item", Static.ParseItemNotation(event.getItem()), PrefilterType.ITEM_MATCH);
				return true;
			}
			return false;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			MCBlockDispenseEvent event = (MCBlockDispenseEvent) e;
			Map<String, Construct> map = evaluate_helper(e);
			MCBlock blk = event.getBlock();

			map.put("type", new CString(StaticLayer.GetConvertor().LookupMaterialName(event.getBlock().getTypeId()), Target.UNKNOWN));

			map.put("item", ObjectGenerator.GetGenerator().item(event.getItem(), Target.UNKNOWN));

			map.put("velocity", ObjectGenerator.GetGenerator().velocity(event.getVelocity(), Target.UNKNOWN));

			CArray location = ObjectGenerator.GetGenerator()
					.location(StaticLayer.GetLocation(blk.getWorld(), blk.getX(), blk.getY(), blk.getZ()));
			map.put("location", location);

			return map;
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if (event instanceof MCBlockDispenseEvent) {
				if ("item".equals(key)) {
					((MCBlockDispenseEvent) event).setItem(ObjectGenerator.GetGenerator().item(value, Target.UNKNOWN));
					return true;
				}
				if ("velocity".equals(key)) {
					((MCBlockDispenseEvent) event).setVelocity(ObjectGenerator.GetGenerator().velocity(value, Target.UNKNOWN));
					return true;
				}
			}
			return false;
		}
	}

	@api
	public static class block_grow extends AbstractEvent {

		public String getName() {
			return "block_grow";
		}

		public Driver driver() {
			return Driver.BLOCK_GROW;
		}

		public String docs() {
			return "{oldtype: <string match> The block type before the growth | olddata: <string match> The block data before the growth |"
					+ " newtype: <string match> The block type after the growth | newdata: <string match> The block data after the growth |"
					+ " world: <macro>}"
					+ " This event is called when a block grows naturally. If the event is cancelled, the block will not grow."
					+ " {oldblock: The block before the growth (an array with keys 'type' and 'data') | newblock: The block after the growth (an array with keys 'type' and 'data') |"
					+ " location: the location of the block that will grow}"
					+ " {}"
					+ " {}";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if (event instanceof MCBlockGrowEvent) {
				MCBlockGrowEvent blockGrowEvent = (MCBlockGrowEvent) event;
				MCBlock oldBlock = blockGrowEvent.getBlock();
				Prefilters.match(prefilter, "oldtype", oldBlock.getTypeId(), PrefilterType.STRING_MATCH);
				Prefilters.match(prefilter, "olddata", oldBlock.getData(), PrefilterType.STRING_MATCH);
				MCBlockState newBlock = blockGrowEvent.getNewState();
				Prefilters.match(prefilter, "newtype", newBlock.getTypeId(), PrefilterType.STRING_MATCH);
				Prefilters.match(prefilter, "newdata", newBlock.getData().getData(), PrefilterType.STRING_MATCH);
				Prefilters.match(prefilter, "world", oldBlock.getWorld().getName(), PrefilterType.MACRO);
				return true;
			} else {
				return false;
			}
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if (event instanceof MCBlockGrowEvent) {
				MCBlockGrowEvent blockGrowEvent = (MCBlockGrowEvent) event;
				Map<String, Construct> mapEvent = evaluate_helper(event);
				MCBlock oldBlock = blockGrowEvent.getBlock();
				CArray oldBlockArray = new CArray(Target.UNKNOWN);
				oldBlockArray.set("type", new CInt(oldBlock.getTypeId(), Target.UNKNOWN), Target.UNKNOWN);
				oldBlockArray.set("data", new CInt(oldBlock.getData(), Target.UNKNOWN), Target.UNKNOWN);
				mapEvent.put("oldblock", oldBlockArray);
				MCBlockState newBlock = blockGrowEvent.getNewState();
				CArray newBlockArray = new CArray(Target.UNKNOWN);
				newBlockArray.set("type", new CInt(newBlock.getTypeId(), Target.UNKNOWN), Target.UNKNOWN);
				newBlockArray.set("data", new CInt(newBlock.getData().getData(), Target.UNKNOWN), Target.UNKNOWN);
				mapEvent.put("newblock", newBlockArray);
				mapEvent.put("location", ObjectGenerator.GetGenerator().location(oldBlock.getLocation(), false));
				return mapEvent;
			} else {
				throw new EventException("Cannot convert event to BlockGrowEvent");
			}
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent e) {
			return false;
		}
	}
}
