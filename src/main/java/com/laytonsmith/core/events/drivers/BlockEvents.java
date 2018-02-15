package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCIgniteCause;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventBuilder;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockEvents {

	public static String docs() {
		return "Contains events related to a block";
	}

	// Stub for actual events below.
	public static abstract class piston_event extends AbstractEvent {

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent e) {
			return false;
		}

		public Map<String, Construct> evaluate_stub(BindableEvent e) throws EventException {
			MCBlockPistonEvent event = (MCBlockPistonEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = evaluate_helper(event);

			MCBlock block = event.getBlock();
			MCMaterial mat = block.getType();

			CArray blk = CArray.GetAssociativeArray(t);
			blk.set("name", mat.getName());
			blk.set("type", new CInt(mat.getType(), t), t);
			blk.set("data", new CInt(block.getData(), t), t);
			blk.set("X", new CInt(block.getX(), t), t);
			blk.set("Y", new CInt(block.getY(), t), t);
			blk.set("Z", new CInt(block.getZ(), t), t);
			blk.set("world", new CString(block.getWorld().getName(), t), t);
			map.put("block", blk);

			map.put("location", ObjectGenerator.GetGenerator().location(block.getLocation(), false));
			map.put("isSticky", CBoolean.get(event.isSticky()));
			map.put("direction", new CString(event.getDirection().name(), t));

			return map;
		}
	}

	@api
	public static class piston_extend extends piston_event {

		@Override
		public String getName() {
			return "piston_extend";
		}

		@Override
		public String docs() {
			return "{} "
					+ "This event is called when a piston is extended. Cancelling the event cancels the move."
					+ "{block: An array with keys 'type' (int; deprecated), 'name' (string), 'data' (int), 'X' (int),"
					+ " 'Y' (int), 'Z' (int) and 'world' (string) for the physical location of the block"
					+ " | location: the locationArray of this block | direction: direction of travel"
					+ " | sticky: true if the piston is sticky | affectedBlocks: blocks pushed}"
					+ "{} "
					+ "{} "
					+ "{}";
		}

		@Override
		public Driver driver() {
			return Driver.PISTON_EXTEND;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			MCBlockPistonExtendEvent event = (MCBlockPistonExtendEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = evaluate_stub(e);

			CArray affected = new CArray(t);
			for(MCBlock block : event.getPushedBlocks()) {
				MCMaterial mat = block.getType();
				CArray blk = CArray.GetAssociativeArray(t);
				blk.set("name", mat.getName(), t);
				blk.set("type", new CInt(mat.getType(), t), t);
				blk.set("data", new CInt(block.getData(), t), t);
				blk.set("X", new CInt(block.getX(), t), t);
				blk.set("Y", new CInt(block.getY(), t), t);
				blk.set("Z", new CInt(block.getZ(), t), t);
				blk.set("world", new CString(block.getWorld().getName(), t), t);
				affected.push(blk, t);
			}
			map.put("affectedBlocks", affected);
			return map;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class piston_retract extends piston_event {

		@Override
		public String getName() {
			return "piston_retract";
		}

		@Override
		public String docs() {
			return "{} "
					+ "This event is called when a piston is retracted. Cancelling the event cancels the move."
					+ "{block: An array with keys 'type' (int; deprecated), 'name' (string), 'data' (int), 'X' (int),"
					+ " 'Y' (int), 'Z' (int) and 'world' (string) for the physical location of the block"
					+ " | location: the locationArray of this block | direction: direction of travel"
					+ " | sticky: true if the piston is sticky | retractedLocation: if the piston"
					+ " is sticky and attached to a block, where the attached block would end up }"
					+ "{} "
					+ "{} "
					+ "{}";
		}

		@Override
		public Driver driver() {
			return Driver.PISTON_RETRACT;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			MCBlockPistonRetractEvent event = (MCBlockPistonRetractEvent) e;
			Map<String, Construct> map = evaluate_stub(e);
			map.put("retractedLocation", ObjectGenerator.GetGenerator().location(event.getRetractedLocation(), false));
			return map;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class block_break extends AbstractEvent {

		@Override
		public String getName() {
			return "block_break";
		}

		@Override
		public String docs() {
			return "{player: <string match> | name: <string match> | type: <math match> | data: <string match>}"
					+ "This event is called when a block is broken. Cancelling the event cancels the breakage."
					+ "{player: The player's name | block: An array with"
					+ " keys 'type' (int; deprecated), 'name' (string), 'data' (int), 'X' (int), 'Y' (int), 'Z' (int)"
					+ " and 'world' (string) for the physical location of the block"
					+ " | location: the locationArray of this block | drops | xp}"
					+ "{drops: an array of arrays (with keys 'type' (string),"
					+ " 'qty' (int), 'data' (int), 'enchants' (array)) of items the block will drop"
					+ " | xp: the xp that this block will drop, if any}"
					+ "{drops|xp} "
					+ "{}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_BREAK;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCBlockBreakEvent)) {
				return false;
			}
			MCBlockBreakEvent event = (MCBlockBreakEvent) e;

			if(prefilter.containsKey("player")) {
				if(!event.getPlayer().getName().equals(prefilter.get("player").val())) {
					return false;
				}
			}

			if(prefilter.containsKey("name")) {
				if(!prefilter.get("name").val().equals(event.getBlock().getType().getName())) {
					return false;
				}
			} else if(prefilter.containsKey("type")) {
				Construct v = prefilter.get("type");
				if(v instanceof CInt) {
					if(event.getBlock().getTypeId() != ((CInt) v).getInt()) {
						return false;
					}
				} else {
					return false;
				}
			}

			if(prefilter.containsKey("data")) {
				Construct v = prefilter.get("data");

				if(v instanceof CInt) {
					if((int) event.getBlock().getData() != ((CInt) v).getInt()) {
						return false;
					}
				} else {
					return false;
				}
			}
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			MCBlockBreakEvent event = (MCBlockBreakEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = evaluate_helper(event);

			MCBlock block = event.getBlock();
			MCMaterial mat = block.getType();

			map.put("player", new CString(event.getPlayer().getName(), t));

			CArray blk = CArray.GetAssociativeArray(t);
			blk.set("name", mat.getName(), t);
			blk.set("type", new CInt(mat.getType(), t), t);
			blk.set("data", new CInt(block.getData(), t), t);
			blk.set("X", new CInt(block.getX(), t), t);
			blk.set("Y", new CInt(block.getY(), t), t);
			blk.set("Z", new CInt(block.getZ(), t), t);
			blk.set("world", new CString(block.getWorld().getName(), t), t);
			map.put("block", blk);

			CArray drops = new CArray(t);
			Collection<MCItemStack> items = event.getDrops();
			if(items == null) {
				items = event.getBlock().getDrops(event.getPlayer().getItemInHand());
			}
			for(MCItemStack stack : items) {
				CArray item = (CArray) ObjectGenerator.GetGenerator().item(stack, t);
				drops.push(item, t);
			}
			map.put("drops", drops);

			map.put("location", ObjectGenerator.GetGenerator().location(block.getLocation(), false));
			map.put("xp", new CInt(event.getExpToDrop(), t));

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent e) {
			MCBlockBreakEvent event = (MCBlockBreakEvent) e;

			if(key.equals("drops")) {
				List<MCItemStack> drops = new ArrayList<>();
				if(value instanceof CArray) {
					CArray arr = (CArray) value;
					for(int i = 0; i < arr.size(); i++) {
						CArray item = (CArray) arr.get(i, value.getTarget());
						drops.add(ObjectGenerator.GetGenerator().item(item, value.getTarget()));
					}
				}
				event.setDrops(drops);
				return true;
			}

			if(key.equals("xp")) {
				if(value instanceof CInt) {
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

		@Override
		public String getName() {
			return "block_place";
		}

		@Override
		public String docs() {
			return "{player: <string match> | name: <string match> | type: <math match> | data: <string match>} "
					+ "This event is called when a player places a block. Cancelling the event cancels placing the block."
					+ "{player: The player's name | type: (deprecated) numerical type id of the block being placed"
					+ " | name: the material name for the block being placed"
					+ " | X: the X coordinate of the block | Y: the Y coordinate of the block"
					+ " | Z: the Z coordinate of the block | world: the world of the block"
					+ " | data: the data value for the block being placed | against: the block"
					+ " being placed against | oldblock: the blocktype and blockdata being replaced"
					+ " | location: A locationArray for this block} "
					+ "{name|type|data} "
					+ "{}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_PLACE;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCBlockPlaceEvent)) {
				return false;
			}
			MCBlockPlaceEvent event = (MCBlockPlaceEvent) e;

			if(prefilter.containsKey("player")) {
				if(!event.getPlayer().getName().equals(prefilter.get("player").val())) {
					return false;
				}
			}

			if(prefilter.containsKey("name")) {
				if(!prefilter.get("name").val().equals(event.getBlock().getType().getName())) {
					return false;
				}
			} else if(prefilter.containsKey("type")) {
				Construct v = prefilter.get("type");

				if(v instanceof CInt) {
					if(event.getBlock().getTypeId() != ((CInt) v).getInt()) {
						return false;
					}
				} else {
					return false;
				}
			}

			if(prefilter.containsKey("data")) {
				Construct v = prefilter.get("data");

				if(v instanceof CInt) {
					if((int) event.getBlock().getData() != ((CInt) v).getInt()) {
						return false;
					}
				} else {
					return false;
				}
			}

			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			MCBlockPlaceEvent event = (MCBlockPlaceEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = evaluate_helper(e);

			MCBlock block = event.getBlock();
			MCMaterial mat = block.getType();

			map.put("player", new CString(event.getPlayer().getName(), t));

			map.put("name", new CString(mat.getName(), t));
			map.put("type", new CInt(mat.getType(), t));
			map.put("data", new CInt(block.getData(), t));
			map.put("X", new CInt(block.getX(), t));
			map.put("Y", new CInt(block.getY(), t));
			map.put("Z", new CInt(block.getZ(), t));
			map.put("world", new CString(block.getWorld().getName(), t));
			map.put("location", ObjectGenerator.GetGenerator().location(block.getLocation(), false));

			MCBlock agstblock = event.getBlockAgainst();
			MCMaterial agstmat = agstblock.getType();
			CArray agst = CArray.GetAssociativeArray(t);
			agst.set("name", agstmat.getName(), t);
			agst.set("type", new CInt(agstmat.getType(), t), t);
			agst.set("data", new CInt(agstblock.getData(), t), t);
			agst.set("X", new CInt(agstblock.getX(), t), t);
			agst.set("Y", new CInt(agstblock.getY(), t), t);
			agst.set("Z", new CInt(agstblock.getZ(), t), t);
			map.put("against", agst);

			MCBlockState old = event.getBlockReplacedState();
			MCMaterial oldmat = old.getType();
			CArray oldarr = CArray.GetAssociativeArray(t);
			oldarr.set("name", oldmat.getName(), t);
			oldarr.set("type", new CInt(oldmat.getType(), t), t);
			oldarr.set("data", new CInt(old.getData().getData(), t), t);
			map.put("oldblock", oldarr);

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent e) {
			MCBlockPlaceEvent event = (MCBlockPlaceEvent) e;

			if(key.equals("name")) {
				MCMaterial mat = StaticLayer.GetMaterial(value.val());
				if(mat == null) {
					throw new CREFormatException("Material name \"" + value.val() + "\" not found.", value.getTarget());
				}
				event.getBlock().setType(mat);
				return true;
			} else if(key.equals("type")) {
				if(value instanceof CInt) {
					int i = Integer.parseInt(value.val());
					event.getBlock().setTypeId(i);
					return true;
				}
			} else if(key.equals("data")) {
				if(value instanceof CInt) {
					byte b;
					try {
						b = Byte.parseByte(value.val());
					} catch (NumberFormatException exc) {
						if(Integer.parseInt(value.val()) < 0) {
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

		@Override
		public String getName() {
			return "block_burn";
		}

		@Override
		public String docs() {
			return "{name: <string match> | type: <math match> | data: <string match>}"
					+ "This event is called when a block is burned. Cancelling the event cancels the burn. "
					+ "{block: An array with keys 'type' (int; deprecated), 'name' (string), 'data' (int), 'X' (int),"
					+ " 'Y' (int), 'Z' (int) and 'world' (string) for the physical location of the block"
					+ " | location: the locationArray of this block}"
					+ "{block}"
					+ "{}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_BURN;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCBlockBurnEvent)) {
				return false;
			}
			MCBlockBurnEvent event = (MCBlockBurnEvent) e;

			if(prefilter.containsKey("name")) {
				if(!prefilter.get("name").val().equals(event.getBlock().getType().getName())) {
					return false;
				}
			} else if(prefilter.containsKey("type")) {
				Construct v = prefilter.get("type");
				if(v instanceof CInt) {
					if(event.getBlock().getTypeId() != ((CInt) v).getInt()) {
						return false;
					}
				} else {
					return false;
				}
			}

			if(prefilter.containsKey("data")) {
				Construct v = prefilter.get("data");
				if(v instanceof CInt) {
					if((int) event.getBlock().getData() != ((CInt) v).getInt()) {
						return false;
					}
				} else {
					return false;
				}
			}

			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			MCBlockBurnEvent event = (MCBlockBurnEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = evaluate_helper(event);

			MCBlock block = event.getBlock();
			MCMaterial mat = block.getType();

			CArray blk = CArray.GetAssociativeArray(t);
			blk.set("name", mat.getName(), t);
			blk.set("type", new CInt(mat.getType(), t), t);
			blk.set("data", new CInt(block.getData(), t), t);
			blk.set("X", new CInt(block.getX(), t), t);
			blk.set("Y", new CInt(block.getY(), t), t);
			blk.set("Z", new CInt(block.getZ(), t), t);
			blk.set("world", new CString(block.getWorld().getName(), t), t);
			map.put("block", blk);

			map.put("location", ObjectGenerator.GetGenerator().location(block.getLocation(), false));

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent e) {
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
			return "{player: <macro> | cause: <macro> | world: <string match>}"
					+ "This event is called when a block or entity is ignited."
					+ "{player: The player's name | ignitingentity: entity ID, if entity is ignited"
					+ " | ignitingblock: (deprecated) block ID, if block is ignited"
					+ " | ignitingblockname: block material name, if block is ignited"
					+ " | location: the locationArray of block or entity"
					+ " | cause: the cause of ignition, one of: " + StringUtils.Join(MCIgniteCause.values(), ", ") + "}"
					+ "{}"
					+ "{}";
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
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCBlockIgniteEvent)) {
				return false;
			}
			MCBlockIgniteEvent event = (MCBlockIgniteEvent) e;

			if(event.getPlayer() != null) {
				Prefilters.match(prefilter, "player", event.getPlayer().getName(), Prefilters.PrefilterType.MACRO);
			}

			Prefilters.match(prefilter, "cause", event.getCause().name(), Prefilters.PrefilterType.MACRO);
			Prefilters.match(prefilter, "world", event.getBlock().getWorld().getName(), Prefilters.PrefilterType.STRING_MATCH);
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if(!(e instanceof MCBlockIgniteEvent)) {
				throw new EventException("Cannot convert e to MCBlockIgniteEvent");
			}
			MCBlockIgniteEvent event = (MCBlockIgniteEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = evaluate_helper(e);

			if(event.getPlayer() != null) {
				map.put("player", new CString(event.getPlayer().getName(), t));
			}

			if(event.getIgnitingEntity() != null) {
				map.put("ignitingentity", new CString(event.getIgnitingEntity().getUniqueId().toString(), t));
			}

			if(event.getIgnitingBlock() != null) {
				MCMaterial ignitingmat = event.getIgnitingBlock().getType();
				map.put("ignitingblock", new CInt(ignitingmat.getType(), t));
				map.put("ignitingblockname", new CString(ignitingmat.getName(), t));
			}

			map.put("location", ObjectGenerator.GetGenerator().location(event.getBlock().getLocation(), false));
			map.put("cause", new CString(event.getCause().name(), t));

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}
	}

	@api
	public static class block_from_to extends AbstractEvent {

		@Override
		public String getName() {
			return "block_from_to";
		}

		@Override
		public String docs() {
			return "{name: <string match> | type: <math match> | data: <string match> | world: <string match>"
					+ " | toname: <string match> | totype: <math match> | todata: <string match>"
					+ " | toworld: <string match> | face: <string match>}"
					+ "This event is called when a water or lava is flowed and ender dragon egg is teleported."
					+ " Cancelling the event cancels the flow or teleport."
					+ "{block: An array with keys 'type' (int; deprecated), 'name' (string), 'data' (int), 'X' (int),"
					+ " 'Y' (int), 'Z' (int) and 'world' (string) for the physical location of the block"
					+ " | location: the locationArray of this block"
					+ " | toblock: target block"
					+ " | tolocation: target block's locationArray}"
					+ "{block|toblock}"
					+ "{}";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_2;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCBlockFromToEvent)) {
				return false;
			}
			MCBlockFromToEvent event = (MCBlockFromToEvent) e;
			Prefilters.match(prefilter, "world", event.getBlock().getWorld().getName(), PrefilterType.STRING_MATCH);
			if(prefilter.containsKey("name")) {
				if(!prefilter.get("name").val().equals(event.getBlock().getType().getName())) {
					return false;
				}
			} else if(prefilter.containsKey("type")) {
				Construct v = prefilter.get("type");
				if(v instanceof CInt) {
					if(event.getBlock().getTypeId() != ((CInt) v).getInt()) {
						return false;
					}
				} else {
					return false;
				}
			}
			Prefilters.match(prefilter, "data", (int) event.getBlock().getData(), PrefilterType.STRING_MATCH);
			if(prefilter.containsKey("toname")) {
				if(!prefilter.get("toname").val().equals(event.getToBlock().getType().getName())) {
					return false;
				}
			} else if(prefilter.containsKey("totype")) {
				Construct v = prefilter.get("totype");
				if(v instanceof CInt) {
					if(event.getToBlock().getTypeId() != ((CInt) v).getInt()) {
						return false;
					}
				} else {
					return false;
				}
			}
			Prefilters.match(prefilter, "todata", (int) event.getToBlock().getData(), PrefilterType.STRING_MATCH);
			Prefilters.match(prefilter, "toworld", event.getToBlock().getWorld().getName(), PrefilterType.STRING_MATCH);
			Prefilters.match(prefilter, "face", event.getBlockFace().toString(), PrefilterType.STRING_MATCH);
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if(!(e instanceof MCBlockFromToEvent)) {
				throw new EventException("Cannot convert e to MCBlockFromToEvent");
			}
			MCBlockFromToEvent event = (MCBlockFromToEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = evaluate_helper(e);

			MCBlock block = event.getBlock();
			MCMaterial mat = block.getType();

			CArray blk = CArray.GetAssociativeArray(t);
			blk.set("name", mat.getName());
			blk.set("type", new CInt(mat.getType(), t), t);
			blk.set("data", new CInt(block.getData(), t), t);
			map.put("block", blk);
			map.put("location", ObjectGenerator.GetGenerator().location(block.getLocation(), false));

			MCBlock toblock = event.getToBlock();
			MCMaterial tomat = toblock.getType();
			CArray toblk = CArray.GetAssociativeArray(t);
			toblk.set("name", tomat.getName(), t);
			toblk.set("type", new CInt(tomat.getType(), t), t);
			toblk.set("data", new CInt(toblock.getData(), t), t);
			map.put("toblock", toblk);
			map.put("tolocation", ObjectGenerator.GetGenerator().location(toblock.getLocation(), false));

			map.put("face", new CString(event.getBlockFace().toString(), t));
			return map;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_FROM_TO;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if(!(event instanceof MCBlockFromToEvent)) {
				return false;
			}
			MCBlockFromToEvent e = (MCBlockFromToEvent) event;
			if(key.equals("block") && value instanceof CArray) {
				CArray blockArray = (CArray) value;
				MCBlock block = e.getBlock();
				Construct name = blockArray.get("name", value.getTarget());
				if(name != null) {
					MCMaterial mat = StaticLayer.GetMaterial(name.val());
					if(mat == null) {
						throw new CREFormatException("Material name \"" + name.val() + "\" not found.", value.getTarget());
					}
					block.setType(mat);
				} else {
					try {
						block.setTypeId(Integer.parseInt(blockArray.get("type", value.getTarget()).val()));
					} catch (Exception ex) {
						throw new CREFormatException("blockArray is invalid", value.getTarget());
					}
				}
				if(blockArray.containsKey("data")) {
					try {
						block.setData((byte) Integer.parseInt(blockArray.get("data", value.getTarget()).val()));
					} catch (Exception ex) {
						throw new CREFormatException("blockArray is invalid", value.getTarget());
					}
				}
				return true;
			}
			if(key.equals("toblock") && value instanceof CArray) {
				CArray blockArray = (CArray) value;
				MCBlock block = e.getToBlock();
				Construct name = blockArray.get("name", value.getTarget());
				if(name != null) {
					MCMaterial mat = StaticLayer.GetMaterial(name.val());
					if(mat == null) {
						throw new CREFormatException("Material name \"" + name.val() + "\" not found.", value.getTarget());
					}
					block.setType(mat);
				} else {
					try {
						block.setTypeId(Integer.parseInt(blockArray.get("type", value.getTarget()).val()));
					} catch (Exception ex) {
						throw new CREFormatException("blockArray is invalid", value.getTarget());
					}
				}
				if(blockArray.containsKey("data")) {
					try {
						block.setData((byte) Integer.parseInt(blockArray.get("data", value.getTarget()).val()));
					} catch (Exception ex) {
						throw new CREFormatException("blockArray is invalid", value.getTarget());
					}
				}
				return true;
			}
			return false;
		}
	}

	@api
	public static class sign_changed extends AbstractEvent {

		@Override
		public String getName() {
			return "sign_changed";
		}

		@Override
		public String docs() {
			return "{player: <string match> | 1: <regex> | 2: regex> | 3: <regex> | 4: <regex> }"
					+ "This event is called when a player changes a sign. Cancelling the event cancels any edits completely."
					+ "{player: The player's name | location: an array usable as a locationArray while also compatible"
					+ " with X,Y,Z,world indices | text: An array with keys 0 thru 3 defining every line on the sign}"
					+ "{1|2|3|4|text: An array with keys 0 thru 3 defining every line on the sign}"
					+ "{}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.SIGN_CHANGED;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCSignChangeEvent)) {
				return false;
			}
			MCSignChangeEvent sce = (MCSignChangeEvent) e;

			if(prefilter.containsKey("player")) {
				if(!sce.getPlayer().getName().equals(prefilter.get("player").val())) {
					return false;
				}
			}

			Prefilters.match(prefilter, "1", sce.getLine(0), Prefilters.PrefilterType.REGEX);
			Prefilters.match(prefilter, "2", sce.getLine(1), Prefilters.PrefilterType.REGEX);
			Prefilters.match(prefilter, "3", sce.getLine(2), Prefilters.PrefilterType.REGEX);
			Prefilters.match(prefilter, "4", sce.getLine(3), Prefilters.PrefilterType.REGEX);

			return true;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if(!(e instanceof MCSignChangeEvent)) {
				throw new EventException("Cannot convert e to MCSignChangeEvent");
			}
			MCSignChangeEvent event = (MCSignChangeEvent) e;
			Map<String, Construct> map = evaluate_helper(e);

			map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
			map.put("text", event.getLines());
			map.put("location", ObjectGenerator.GetGenerator().location(event.getBlock().getLocation(), false));

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if(!(event instanceof MCSignChangeEvent)) {
				return false;
			}
			MCSignChangeEvent sce = (MCSignChangeEvent) event;

			// Allow changing everything at once.
			if(key.equals("text")) {
				if(!(value instanceof CArray)) {
					return false;
				}

				CArray val = (CArray) value;
				if(val.size() != 4) {
					return false;
				}

				String[] lines = {"", "", "", ""};

				for(int i = 0; i < 4; i++) {
					lines[i] = val.get(i, value.getTarget()).toString();
				}

				sce.setLines(lines);

				return true;
			}

			int index;
			// Allow changing just one line at a time.
			if(key.equals("1")) {
				index = 0;
			} else if(key.equals("2")) {
				index = 1;
			} else if(key.equals("3")) {
				index = 2;
			} else if(key.equals("4")) {
				index = 3;
			} else {
				return false;
			}

			if(value instanceof CNull) {
				sce.setLine(index, "");
				return "".equals(sce.getLine(index).toString());
			} else {
				sce.setLine(index, value.val());
				return (sce.getLine(index).toString() == null ? value.val() == null : sce.getLine(index).toString().equals(value.val()));
			}
		}

		@Override
		public BindableEvent convert(CArray manual, Target t) {
			MCSignChangeEvent e = EventBuilder.instantiate(
					MCSignChangeEvent.class,
					Static.GetPlayer(manual.get("player", Target.UNKNOWN).val(), Target.UNKNOWN),
					manual.get("1", Target.UNKNOWN).val(), manual.get("2", Target.UNKNOWN).val(),
					manual.get("3", Target.UNKNOWN).val(), manual.get("4", Target.UNKNOWN).val());
			return e;
		}
	}

	@api
	public static class block_dispense extends AbstractEvent {

		@Override
		public String getName() {
			return "block_dispense";
		}

		@Override
		public String docs() {
			return "{type: <string match> Type of dispenser | item: <item match> Item which is dispensed}"
					+ "This event is called when a dispenser dispense an item. Cancelling the event cancels dispensing."
					+ "{type: Type of dispenser | item: Item which is dispensed | velocity: Returns an associative array"
					+ " indicating the x/y/z components of item velocity. As a convenience, the magnitude is also included."
					+ " | location: Location of dispenser} "
					+ "{item|velocity} "
					+ "{}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_DISPENSE;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCBlockDispenseEvent) {
				MCBlockDispenseEvent event = (MCBlockDispenseEvent) e;
				Prefilters.match(prefilter, "type", event.getBlock().getType().getName(), PrefilterType.STRING_MATCH);
				Prefilters.match(prefilter, "item", Static.ParseItemNotation(event.getItem()), PrefilterType.ITEM_MATCH);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			MCBlockDispenseEvent event = (MCBlockDispenseEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = evaluate_helper(e);

			MCBlock block = event.getBlock();

			map.put("type", new CString(block.getType().getName(), t));
			map.put("item", ObjectGenerator.GetGenerator().item(event.getItem(), t));
			map.put("location", ObjectGenerator.GetGenerator().location(block.getLocation(), false));

			CArray velocity = ObjectGenerator.GetGenerator().vector(event.getVelocity(), t);
			velocity.set("magnitude", new CDouble(event.getVelocity().length(), t), t);
			map.put("velocity", velocity);

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			if(event instanceof MCBlockDispenseEvent) {
				if("item".equals(key)) {
					((MCBlockDispenseEvent) event).setItem(ObjectGenerator.GetGenerator().item(value, value.getTarget()));
					return true;
				}
				if("velocity".equals(key)) {
					((MCBlockDispenseEvent) event).setVelocity(ObjectGenerator.GetGenerator().vector(value, value.getTarget()));
					return true;
				}
			}
			return false;
		}
	}

	@api
	public static class block_grow extends AbstractEvent {

		@Override
		public String getName() {
			return "block_grow";
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_GROW;
		}

		@Override
		public String docs() {
			return "{oldname: <string match> The block name before the growth"
					+ " | oldtype: <math match> (deprecated) The block numeric id before the growth"
					+ " | olddata: <string match> The block data before the growth"
					+ " | newname: <string match> The block name after the growth"
					+ " | newtype: <math match> (deprecated) The block numeric id after the growth"
					+ " | newdata: <string match> The block data after the growth"
					+ " | world: <macro>}"
					+ " This event is called when a block grows naturally. If the event is cancelled, the block will not grow."
					+ " {oldblock: The block before the growth (an array with keys 'type' (deprecated), 'name', and 'data')"
					+ " | newblock: The block after the growth (an array with keys 'type' (deprecated), 'name', and 'data')"
					+ " | location: the location of the block that will grow}"
					+ " {}"
					+ " {}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(!(event instanceof MCBlockGrowEvent)) {
				return false;
			}
			MCBlockGrowEvent blockGrowEvent = (MCBlockGrowEvent) event;
			MCBlock oldBlock = blockGrowEvent.getBlock();
			if(prefilter.containsKey("oldname")) {
				if(!prefilter.get("oldname").val().equals(oldBlock.getType().getName())) {
					return false;
				}
			} else if(prefilter.containsKey("oldtype")) {
				Construct v = prefilter.get("oldtype");
				if(v instanceof CInt) {
					if(oldBlock.getTypeId() != ((CInt) v).getInt()) {
						return false;
					}
				} else {
					return false;
				}
			}
			Prefilters.match(prefilter, "olddata", oldBlock.getData(), PrefilterType.STRING_MATCH);
			MCBlockState newBlock = blockGrowEvent.getNewState();
			if(prefilter.containsKey("newname")) {
				if(!prefilter.get("newname").val().equals(newBlock.getType().getName())) {
					return false;
				}
			} else if(prefilter.containsKey("newtype")) {
				Construct v = prefilter.get("newtype");
				if(v instanceof CInt) {
					if(newBlock.getType().getType() != ((CInt) v).getInt()) {
						return false;
					}
				} else {
					return false;
				}
			}
			Prefilters.match(prefilter, "newdata", newBlock.getData().getData(), PrefilterType.STRING_MATCH);
			Prefilters.match(prefilter, "world", oldBlock.getWorld().getName(), PrefilterType.MACRO);
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if(!(event instanceof MCBlockGrowEvent)) {
				throw new EventException("Cannot convert event to BlockGrowEvent");
			}
			MCBlockGrowEvent blockGrowEvent = (MCBlockGrowEvent) event;
			Target t = Target.UNKNOWN;
			Map<String, Construct> mapEvent = evaluate_helper(event);

			MCBlock oldBlock = blockGrowEvent.getBlock();
			MCMaterial oldMat = oldBlock.getType();

			CArray oldBlockArray = CArray.GetAssociativeArray(t);
			oldBlockArray.set("name", oldMat.getName());
			oldBlockArray.set("type", new CInt(oldMat.getType(), t), t);
			oldBlockArray.set("data", new CInt(oldBlock.getData(), t), t);
			mapEvent.put("oldblock", oldBlockArray);

			MCBlockState newBlock = blockGrowEvent.getNewState();
			MCMaterial newMat = newBlock.getType();
			CArray newBlockArray = CArray.GetAssociativeArray(t);
			newBlockArray.set("name", newMat.getName());
			newBlockArray.set("type", new CInt(newMat.getType(), t), t);
			newBlockArray.set("data", new CInt(newBlock.getData().getData(), t), t);
			mapEvent.put("newblock", newBlockArray);

			mapEvent.put("location", ObjectGenerator.GetGenerator().location(oldBlock.getLocation(), false));
			return mapEvent;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent e) {
			return false;
		}
	}

	@api
	public static class note_play extends AbstractEvent {

		@Override
		public String getName() {
			return "note_play";
		}

		@Override
		public Driver driver() {
			return Driver.NOTE_PLAY;
		}

		@Override
		public String docs() {
			return "{}"
					+ " This event is called when a noteblock is activated via player interaction or redstone."
					+ " The instrument may be one of: " + StringUtils.Join(MCInstrument.values(), ", ", ", or ") + "."
					+ " {location: The location of the noteblock | instrument: The name of the sound"
					+ " | tone: The note played (eg. F#) | octave: The octave the tone was played (0 - 2)}"
					+ " {instrument|tone|octave}"
					+ " {}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			return event instanceof MCNotePlayEvent;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent event) throws EventException {
			if(!(event instanceof MCNotePlayEvent)) {
				throw new EventException("Cannot convert event to NotePlayEvent");
			}
			MCNotePlayEvent e = (MCNotePlayEvent) event;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = new HashMap<>();

			map.put("location", ObjectGenerator.GetGenerator().location(e.getBlock().getLocation(), false));
			map.put("instrument", new CString(e.getInstrument().name(), t));
			map.put("tone", new CString(e.getNote().getTone().name() + (e.getNote().isSharped() ? "#" : ""), t));
			map.put("octave", new CInt(e.getNote().getOctave(), t));

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent e) {
			if(e instanceof MCNotePlayEvent) {
				MCNotePlayEvent event = (MCNotePlayEvent) e;
				try {
					if("instrument".equals(key)) {
						event.setInstrument(MCInstrument.valueOf(value.val()));
						return true;
					}
					if("tone".equals(key)) {
						if(value.val().length() == 0) {
							return false;
						}
						int octave = event.getNote().getOctave();
						MCTone tone = MCTone.valueOf(value.val().substring(0, 1));
						boolean sharp = value.val().endsWith("#");
						event.setNote(StaticLayer.GetConvertor().GetNote(octave, tone, sharp));
						return true;
					}
					if("octave".equals(key)) {
						int octave = Static.getInt32(value, value.getTarget());
						MCTone tone = event.getNote().getTone();
						boolean sharp = event.getNote().isSharped();
						event.setNote(StaticLayer.GetConvertor().GetNote(octave, tone, sharp));
						return true;
					}
				} catch (IllegalArgumentException ex) {
					throw new CREIllegalArgumentException("No " + key + " with the value " + value + " exists", value.getTarget(), ex);
				}
			}
			return false;
		}
	}

	@api
	public static class block_fade extends AbstractEvent {

		@Override
		public String getName() {
			return "block_fade";
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_FADE;
		}

		@Override
		public String docs() {
			return "{oldname: <string match> Block name before it fades"
					+ " | oldtype: <math match> (deprecated) The block numerical id before it fades"
					+ " | world: <string match>}"
					+ "Called when a block fades, melts or disappears based on world conditions."
					+ "{oldblock: The block before the fades (an array with keys 'type' (deprecated), 'name', and 'data')"
					+ " | newblock: The block after the fades (an array with keys 'type' (deprecated), 'name', and 'data')"
					+ " | location: the location of the block that will fade}"
					+ "{}"
					+ "{}";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_2;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCBlockFadeEvent)) {
				return false;
			}
			MCBlockFadeEvent event = (MCBlockFadeEvent) e;
			MCBlock oldBlock = event.getBlock();
			if(prefilter.containsKey("oldname")) {
				if(!prefilter.get("oldname").val().equals(oldBlock.getType().getName())) {
					return false;
				}
			} else if(prefilter.containsKey("oldtype")) {
				Construct v = prefilter.get("oldtype");
				if(v instanceof CInt) {
					if(oldBlock.getTypeId() != ((CInt) v).getInt()) {
						return false;
					}
				} else {
					return false;
				}
			}
			Construct world = prefilter.get("world");
			if(world != null && !world.val().equals(oldBlock.getWorld().getName())) {
				return false;
			}
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if(!(e instanceof MCBlockFadeEvent)) {
				throw new EventException("Cannot convert event to BlockFadeEvent");
			}
			MCBlockFadeEvent event = (MCBlockFadeEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> mapEvent = evaluate_helper(event);

			MCBlock oldBlock = event.getBlock();
			MCMaterial oldMat = oldBlock.getType();

			CArray oldBlockArray = CArray.GetAssociativeArray(t);
			oldBlockArray.set("name", oldMat.getName());
			oldBlockArray.set("type", new CInt(oldMat.getType(), t), t);
			oldBlockArray.set("data", new CInt(oldBlock.getData(), t), t);
			mapEvent.put("oldblock", oldBlockArray);

			MCBlockState newBlock = event.getNewState();
			MCMaterial newMat = newBlock.getType();
			CArray newBlockArray = CArray.GetAssociativeArray(t);
			newBlockArray.set("name", newMat.getName());
			newBlockArray.set("type", new CInt(newMat.getType(), t), t);
			newBlockArray.set("data", new CInt(newBlock.getData().getData(), t), t);
			mapEvent.put("newblock", newBlockArray);

			mapEvent.put("location", ObjectGenerator.GetGenerator().location(oldBlock.getLocation(), false));
			return mapEvent;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}
	}
}
