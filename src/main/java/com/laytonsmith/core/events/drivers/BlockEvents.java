package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCIgniteCause;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.events.MCBlockBreakEvent;
import com.laytonsmith.abstraction.events.MCBlockBurnEvent;
import com.laytonsmith.abstraction.events.MCBlockDispenseEvent;
import com.laytonsmith.abstraction.events.MCBlockFadeEvent;
import com.laytonsmith.abstraction.events.MCBlockFromToEvent;
import com.laytonsmith.abstraction.events.MCBlockGrowEvent;
import com.laytonsmith.abstraction.events.MCBlockIgniteEvent;
import com.laytonsmith.abstraction.events.MCBlockPistonEvent;
import com.laytonsmith.abstraction.events.MCBlockPistonExtendEvent;
import com.laytonsmith.abstraction.events.MCBlockPistonRetractEvent;
import com.laytonsmith.abstraction.events.MCBlockPlaceEvent;
import com.laytonsmith.abstraction.events.MCNotePlayEvent;
import com.laytonsmith.abstraction.events.MCSignChangeEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventBuilder;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.exceptions.CRE.CREBindException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.natives.interfaces.Mixed;

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
	public abstract static class piston_event extends AbstractEvent {

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent e) {
			return false;
		}

		Map<String, Mixed> evaluate_stub(BindableEvent e) throws EventException {
			MCBlockPistonEvent event = (MCBlockPistonEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = evaluate_helper(event);

			MCBlock block = event.getBlock();

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
					+ "{location: the locationArray of this piston | direction: direction of travel"
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
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			MCBlockPistonExtendEvent event = (MCBlockPistonExtendEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = evaluate_stub(e);

			CArray affected = new CArray(t);
			for(MCBlock block : event.getPushedBlocks()) {
				MCMaterial mat = block.getType();
				CArray blk = CArray.GetAssociativeArray(t);
				blk.set("name", mat.getName(), t);
				blk.set("x", new CInt(block.getX(), t), t);
				blk.set("y", new CInt(block.getY(), t), t);
				blk.set("z", new CInt(block.getZ(), t), t);
				blk.set("world", new CString(block.getWorld().getName(), t), t);
				affected.push(blk, t);
			}
			map.put("affectedBlocks", affected);
			return map;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
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
					+ "{location: the locationArray of this piston | direction: direction of travel"
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
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			MCBlockPistonRetractEvent event = (MCBlockPistonRetractEvent) e;
			Map<String, Mixed> map = evaluate_stub(e);
			map.put("retractedLocation", ObjectGenerator.GetGenerator().location(event.getRetractedLocation(), false));
			return map;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
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
			return "{player: <string match> | block: <string match>}"
					+ "This event is called when a block is broken. Cancelling the event cancels the breakage."
					+ "{player: The player's name | block: the block type that was broken"
					+ " | location: the locationArray of this block | drops | xp}"
					+ "{drops: an array of arrays of items the block will drop"
					+ " | xp: the xp that this block will drop, if any}"
					+ "{drops|xp} "
					+ "{}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_BREAK;
		}

		@Override
		public void bind(BoundEvent event) {
			// handle deprecated prefilter
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("name")) {
				MCMaterial mat = StaticLayer.GetMaterialFromLegacy(prefilter.get("name").val(), 0);
				prefilter.put("block", new CString(mat.getName(), event.getTarget()));
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"name\" prefilter in " + getName()
						+ " is deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
			} else if(prefilter.containsKey("type")) {
				Mixed cid = prefilter.get("type");
				if(cid.isInstanceOf(CInt.TYPE)) {
					int id = (int) ((CInt) cid).getInt();
					int data = 0;
					if(prefilter.containsKey("data")) {
						Mixed cdata = prefilter.get("data");
						if(cdata.isInstanceOf(CInt.TYPE)) {
							data = (int) ((CInt) cdata).getInt();
						}
					}
					MCMaterial mat = StaticLayer.GetMaterialFromLegacy(id, data);
					if(mat == null) {
						throw new CREBindException("Invalid material id '" + id + "'", event.getTarget());
					}
					prefilter.put("block", new CString(mat.getName(), event.getTarget()));
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"type\" and \"data\" prefilters in " + getName()
							+ " are deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
				}
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCBlockBreakEvent)) {
				return false;
			}
			MCBlockBreakEvent event = (MCBlockBreakEvent) e;

			if(prefilter.containsKey("player")) {
				if(!event.getPlayer().getName().equals(prefilter.get("player").val())) {
					return false;
				}
			}

			if(prefilter.containsKey("block")) {
				if(!event.getBlock().getType().getName().equals(prefilter.get("block").val())) {
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
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			MCBlockBreakEvent event = (MCBlockBreakEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = evaluate_helper(event);

			MCBlock block = event.getBlock();

			map.put("player", new CString(event.getPlayer().getName(), t));
			map.put("block", new CString(block.getType().getName(), t));

			CArray drops = new CArray(t);
			Collection<MCItemStack> items = event.getDrops();
			if(items == null) {
				items = block.getDrops(event.getPlayer().getInventory().getItemInMainHand());
			}
			for(MCItemStack stack : items) {
				drops.push(ObjectGenerator.GetGenerator().item(stack, t), t);
			}
			map.put("drops", drops);

			map.put("location", ObjectGenerator.GetGenerator().location(block.getLocation(), false));
			map.put("xp", new CInt(event.getExpToDrop(), t));

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent e) {
			MCBlockBreakEvent event = (MCBlockBreakEvent) e;

			if(key.equals("drops")) {
				List<MCItemStack> drops = new ArrayList<>();
				if(value.isInstanceOf(CArray.TYPE)) {
					CArray arr = (CArray) value;
					for(int i = 0; i < arr.size(); i++) {
						CArray item = Static.getArray(arr.get(i, value.getTarget()), value.getTarget());
						MCItemStack stack = ObjectGenerator.GetGenerator().item(item, value.getTarget());
						if(!stack.isEmpty()) {
							drops.add(stack);
						}
					}
				}
				event.setDrops(drops);
				return true;
			}

			if(key.equals("xp")) {
				if(value.isInstanceOf(CInt.TYPE)) {
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
			return "{player: <string match> | block: <string match>} "
					+ "This event is called when a player places a block. Cancelling the event cancels placing the block."
					+ "{player: The player's name | block: the block type that was placed"
					+ " | against: a block array of the block being placed against"
					+ " | oldblock: the old block type that was replaced"
					+ " | location: A locationArray for this block} "
					+ "{block} "
					+ "{}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_PLACE;
		}

		@Override
		public void bind(BoundEvent event) {
			// handle deprecated prefilter
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("name")) {
				MCMaterial mat = StaticLayer.GetMaterialFromLegacy(prefilter.get("name").val(), 0);
				prefilter.put("block", new CString(mat.getName(), event.getTarget()));
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"name\" prefilter in " + getName()
						+ " is deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
			} else if(prefilter.containsKey("type")) {
				Mixed cid = prefilter.get("type");
				if(cid.isInstanceOf(CInt.TYPE)) {
					int id = (int) ((CInt) cid).getInt();
					int data = 0;
					if(prefilter.containsKey("data")) {
						Mixed cdata = prefilter.get("data");
						if(cdata.isInstanceOf(CInt.TYPE)) {
							data = (int) ((CInt) cdata).getInt();
						}
					}
					MCMaterial mat = StaticLayer.GetMaterialFromLegacy(id, data);
					if(mat == null) {
						throw new CREBindException("Invalid material id '" + id + "'", event.getTarget());
					}
					prefilter.put("block", new CString(mat.getName(), event.getTarget()));
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"type\" and \"data\" prefilters in " + getName()
							+ " are deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
				}
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCBlockPlaceEvent)) {
				return false;
			}
			MCBlockPlaceEvent event = (MCBlockPlaceEvent) e;

			if(prefilter.containsKey("player")) {
				if(!event.getPlayer().getName().equals(prefilter.get("player").val())) {
					return false;
				}
			}

			if(prefilter.containsKey("block")) {
				if(!event.getBlock().getType().getName().equals(prefilter.get("block").val())) {
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
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			MCBlockPlaceEvent event = (MCBlockPlaceEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = evaluate_helper(e);

			MCBlock block = event.getBlock();
			MCMaterial mat = block.getType();

			map.put("player", new CString(event.getPlayer().getName(), t));
			map.put("block", new CString(mat.getName(), t));
			map.put("location", ObjectGenerator.GetGenerator().location(block.getLocation(), false));

			MCBlock agstblock = event.getBlockAgainst();
			MCMaterial agstmat = agstblock.getType();
			CArray agst = CArray.GetAssociativeArray(t);
			agst.set("name", agstmat.getName(), t);
			agst.set("x", new CInt(agstblock.getX(), t), t);
			agst.set("y", new CInt(agstblock.getY(), t), t);
			agst.set("z", new CInt(agstblock.getZ(), t), t);
			map.put("against", agst);

			map.put("oldblock", new CString(event.getBlockReplacedState().getType().getName(), t));

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent e) {
			MCBlockPlaceEvent event = (MCBlockPlaceEvent) e;

			if(key.equals("block")) {
				MCMaterial mat = StaticLayer.GetMaterial(value.val());
				if(mat == null) {
					throw new CREFormatException("Material name \"" + value.val() + "\" not found.", value.getTarget());
				}
				event.getBlock().setType(mat);
				return true;
			} else if(key.equals("name")) {
				MCMaterial mat = StaticLayer.GetMaterial(value.val());
				if(mat == null) {
					throw new CREFormatException("Material name \"" + value.val() + "\" not found.", value.getTarget());
				}
				event.getBlock().setType(mat);
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Mutable data key \"name\" in " + getName()
						+ " is deprecated for \"block\". Converted to " + mat.getName(), value.getTarget());
				return true;
			} else if(key.equals("type")) {
				if(value.isInstanceOf(CInt.TYPE)) {
					MCMaterial mat = StaticLayer.GetMaterialFromLegacy((int) ((CInt) value).getInt(), 0);
					event.getBlock().setType(mat);
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Mutable data key \"type\" in " + getName()
							+ " is deprecated for \"block\". Converted to " + mat.getName(), value.getTarget());
					return true;
				}
			} else if(key.equals("data")) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Mutable data key \"data\" in " + getName()
						+ " is deprecated for \"block\".", value.getTarget());
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
			return "{block: <string match>}"
					+ "This event is called when a block is burned. Cancelling the event cancels the burn. "
					+ "{block: the block type that was burned"
					+ " | location: the locationArray of this block}"
					+ "{block}"
					+ "{}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_BURN;
		}

		@Override
		public void bind(BoundEvent event) {
			// handle deprecated prefilter
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("name")) {
				MCMaterial mat = StaticLayer.GetMaterialFromLegacy(prefilter.get("name").val(), 0);
				prefilter.put("block", new CString(mat.getName(), event.getTarget()));
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"name\" prefilter in " + getName()
						+ " is deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
			} else if(prefilter.containsKey("type")) {
				Mixed cid = prefilter.get("type");
				if(cid.isInstanceOf(CInt.TYPE)) {
					int id = (int) ((CInt) cid).getInt();
					int data = 0;
					if(prefilter.containsKey("data")) {
						Mixed cdata = prefilter.get("data");
						if(cdata.isInstanceOf(CInt.TYPE)) {
							data = (int) ((CInt) cdata).getInt();
						}
					}
					MCMaterial mat = StaticLayer.GetMaterialFromLegacy(id, data);
					if(mat == null) {
						throw new CREBindException("Invalid material id '" + id + "'", event.getTarget());
					}
					prefilter.put("block", new CString(mat.getName(), event.getTarget()));
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"type\" and \"data\" prefilters in " + getName()
							+ " are deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
				}
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCBlockBurnEvent)) {
				return false;
			}
			MCBlockBurnEvent event = (MCBlockBurnEvent) e;

			if(prefilter.containsKey("block")) {
				if(!event.getBlock().getType().getName().equals(prefilter.get("block").val())) {
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
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			MCBlockBurnEvent event = (MCBlockBurnEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = evaluate_helper(event);

			MCBlock block = event.getBlock();

			map.put("block", new CString(block.getType().getName(), t));
			map.put("location", ObjectGenerator.GetGenerator().location(block.getLocation(), false));

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent e) {
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
					+ "This event is called when a block ignited by a block or entity."
					+ "{player: The player's name | ignitingentity: entity ID, if caused by entity"
					+ " | ignitingblock: the block's type, if caused by block"
					+ " | ignitingblocklocation: the block's location that ignited"
					+ " | location: the locationArray that got ignited"
					+ " | cause: the cause of ignition, one of " + StringUtils.Join(MCIgniteCause.values(), ", ") + "}"
					+ "{}"
					+ "{}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_IGNITE;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
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
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if(!(e instanceof MCBlockIgniteEvent)) {
				throw new EventException("Cannot convert e to MCBlockIgniteEvent");
			}
			MCBlockIgniteEvent event = (MCBlockIgniteEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = evaluate_helper(e);

			if(event.getPlayer() != null) {
				map.put("player", new CString(event.getPlayer().getName(), t));
			}

			if(event.getIgnitingEntity() != null) {
				map.put("ignitingentity", new CString(event.getIgnitingEntity().getUniqueId().toString(), t));
			}

			MCBlock b = event.getIgnitingBlock();
			if(b != null) {
				map.put("ignitingblock", new CString(b.getType().getName(), t));
				map.put("ignitingblocklocation",  ObjectGenerator.GetGenerator().location(b.getLocation(), false));
			}

			map.put("location", ObjectGenerator.GetGenerator().location(event.getBlock().getLocation(), false));
			map.put("cause", new CString(event.getCause().name(), t));

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
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
			return "{block: <string match> | world: <string match>"
					+ " | toblock: <string match> | toworld: <string match> | face: <string match>}"
					+ "This event is called when a water or lava is flowed and ender dragon egg is teleported."
					+ " Cancelling the event cancels the flow or teleport."
					+ "{block: the source block type"
					+ " | location: the locationArray of the source block"
					+ " | toblock: the target block type"
					+ " | tolocation: the target block's locationArray}"
					+ "{block|toblock}"
					+ "{}";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public void bind(BoundEvent event) {
			// handle deprecated prefilter
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("name")) {
				MCMaterial mat = StaticLayer.GetMaterialFromLegacy(prefilter.get("name").val(), 0);
				prefilter.put("block", new CString(mat.getName(), event.getTarget()));
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"name\" prefilter in " + getName()
						+ " is deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
			} else if(prefilter.containsKey("type")) {
				Mixed cid = prefilter.get("type");
				if(cid.isInstanceOf(CInt.TYPE)) {
					int id = (int) ((CInt) cid).getInt();
					int data = 0;
					if(prefilter.containsKey("data")) {
						Mixed cdata = prefilter.get("data");
						if(cdata.isInstanceOf(CInt.TYPE)) {
							data = (int) ((CInt) cdata).getInt();
						}
					}
					MCMaterial mat = StaticLayer.GetMaterialFromLegacy(id, data);
					if(mat == null) {
						throw new CREBindException("Invalid material id '" + id + "'", event.getTarget());
					}
					prefilter.put("block", new CString(mat.getName(), event.getTarget()));
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"type\" and \"data\" prefilters in " + getName()
							+ " are deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
				}
			}
			if(prefilter.containsKey("toname")) {
				MCMaterial mat = StaticLayer.GetMaterialFromLegacy(prefilter.get("toname").val(), 0);
				prefilter.put("toblock", new CString(mat.getName(), event.getTarget()));
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"toname\" prefilter in " + getName()
						+ " is deprecated for \"toblock\". Converted to " + mat.getName(), event.getTarget());
			} else if(prefilter.containsKey("totype")) {
				Mixed cid = prefilter.get("totype");
				if(cid.isInstanceOf(CInt.TYPE)) {
					int id = (int) ((CInt) cid).getInt();
					int data = 0;
					if(prefilter.containsKey("todata")) {
						Mixed cdata = prefilter.get("todata");
						if(cdata.isInstanceOf(CInt.TYPE)) {
							data = (int) ((CInt) cdata).getInt();
						}
					}
					MCMaterial mat = StaticLayer.GetMaterialFromLegacy(id, data);
					if(mat == null) {
						throw new CREBindException("Invalid material id '" + id + "'", event.getTarget());
					}
					prefilter.put("toblock", new CString(mat.getName(), event.getTarget()));
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"totype\" and \"todata\" prefilters in " + getName()
							+ " are deprecated for \"toblock\". Converted to " + mat.getName(), event.getTarget());
				}
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCBlockFromToEvent)) {
				return false;
			}
			MCBlockFromToEvent event = (MCBlockFromToEvent) e;
			Prefilters.match(prefilter, "world", event.getBlock().getWorld().getName(), PrefilterType.STRING_MATCH);
			if(prefilter.containsKey("block")) {
				if(!event.getBlock().getType().getName().equals(prefilter.get("block").val())) {
					return false;
				}
			}
			if(prefilter.containsKey("toblock")) {
				if(!event.getToBlock().getType().getName().equals(prefilter.get("toblock").val())) {
					return false;
				}
			}
			Prefilters.match(prefilter, "toworld", event.getToBlock().getWorld().getName(), PrefilterType.STRING_MATCH);
			Prefilters.match(prefilter, "face", event.getBlockFace().toString(), PrefilterType.STRING_MATCH);
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if(!(e instanceof MCBlockFromToEvent)) {
				throw new EventException("Cannot convert e to MCBlockFromToEvent");
			}
			MCBlockFromToEvent event = (MCBlockFromToEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = evaluate_helper(e);

			MCBlock block = event.getBlock();

			map.put("block", new CString(block.getType().getName(), t));
			map.put("location", ObjectGenerator.GetGenerator().location(block.getLocation(), false));

			MCBlock toblock = event.getToBlock();

			map.put("toblock", new CString(toblock.getType().getName(), t));
			map.put("tolocation", ObjectGenerator.GetGenerator().location(toblock.getLocation(), false));

			map.put("face", new CString(event.getBlockFace().toString(), t));
			return map;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_FROM_TO;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(!(event instanceof MCBlockFromToEvent)) {
				return false;
			}
			MCBlockFromToEvent e = (MCBlockFromToEvent) event;
			if(key.equals("block")) {
				MCBlock block = e.getBlock();
				if(value.isInstanceOf(CArray.TYPE)) {
					CArray blockArray = (CArray) value;
					if(blockArray.containsKey("name")) {
						Mixed name = blockArray.get("name", value.getTarget());
						int data = 0;
						if(blockArray.containsKey("data")) {
							try {
								data = Integer.parseInt(blockArray.get("data", value.getTarget()).val());
							} catch (Exception ex) {
								throw new CREFormatException("blockArray is invalid", value.getTarget());
							}
						}
						MCMaterial mat = StaticLayer.GetMaterialFromLegacy(name.val(), data);
						if(mat == null) {
							throw new CREFormatException("Material name \"" + name.val() + "\" not found.", value.getTarget());
						}
						block.setType(mat);
						MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Mutable data key \"block\" in " + getName()
								+ " is deprecated when using an array. Converted to " + mat.getName(), value.getTarget());
						return true;
					}
					if(blockArray.containsKey("type")) {
						int type;
						int data = 0;
						try {
							type = Integer.parseInt(blockArray.get("type", value.getTarget()).val());
						} catch (Exception ex) {
							throw new CREFormatException("blockArray is invalid", value.getTarget());
						}
						if(blockArray.containsKey("data")) {
							try {
								data = Integer.parseInt(blockArray.get("data", value.getTarget()).val());
							} catch (Exception ex) {
								throw new CREFormatException("blockArray is invalid", value.getTarget());
							}
						}
						MCMaterial mat = StaticLayer.GetMaterialFromLegacy(type, data);
						if(mat == null) {
							throw new CREFormatException("Material type \"" + type + "\" not found.", value.getTarget());
						}
						block.setType(mat);
						MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Mutable data key \"block\" in " + getName()
								+ " is deprecated when using an array. Converted to " + mat.getName(), value.getTarget());
						return true;
					}
				} else {
					MCMaterial mat = StaticLayer.GetMaterial(value.val());
					if(mat == null) {
						throw new CREFormatException("Material type \"" + value.val() + "\" not found.", value.getTarget());
					}
					block.setType(mat);
					return true;
				}
			}
			if(key.equals("toblock")) {
				MCBlock block = e.getToBlock();
				if(value.isInstanceOf(CArray.TYPE)) {
					CArray blockArray = (CArray) value;
					if(blockArray.containsKey("name")) {
						Mixed name = blockArray.get("name", value.getTarget());
						int data = 0;
						if(blockArray.containsKey("data")) {
							try {
								data = Integer.parseInt(blockArray.get("data", value.getTarget()).val());
							} catch (Exception ex) {
								throw new CREFormatException("blockArray is invalid", value.getTarget());
							}
						}
						MCMaterial mat = StaticLayer.GetMaterialFromLegacy(name.val(), data);
						if(mat == null) {
							throw new CREFormatException("Material name \"" + name.val() + "\" not found.", value.getTarget());
						}
						block.setType(mat);
						MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Mutable data key \"toblock\" in " + getName()
								+ " is deprecated when using an array. Converted to " + mat.getName(), value.getTarget());
						return true;
					}
					if(blockArray.containsKey("type")) {
						int type;
						int data = 0;
						try {
							type = Integer.parseInt(blockArray.get("type", value.getTarget()).val());
						} catch (Exception ex) {
							throw new CREFormatException("blockArray is invalid", value.getTarget());
						}
						if(blockArray.containsKey("data")) {
							try {
								data = Integer.parseInt(blockArray.get("data", value.getTarget()).val());
							} catch (Exception ex) {
								throw new CREFormatException("blockArray is invalid", value.getTarget());
							}
						}
						MCMaterial mat = StaticLayer.GetMaterialFromLegacy(type, data);
						if(mat == null) {
							throw new CREFormatException("Material type \"" + type + "\" not found.", value.getTarget());
						}
						block.setType(mat);
						MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Mutable data key \"toblock\" in " + getName()
								+ " is deprecated when using an array. Converted to " + mat.getName(), value.getTarget());
						return true;
					}
				} else {
					MCMaterial mat = StaticLayer.GetMaterial(value.val());
					if(mat == null) {
						throw new CREFormatException("Material type \"" + value.val() + "\" not found.", value.getTarget());
					}
					block.setType(mat);
					return true;
				}
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
			return "{player: <string match> | 1: <regex> | 2: <regex> | 3: <regex> | 4: <regex> }"
					+ "This event is called when a player changes a sign. Cancelling the event cancels any edits completely."
					+ "{player: The player's name | location: an array usable as a locationArray while also compatible"
					+ " with X,Y,Z,world indices | text: An array with keys 0 thru 3 defining every line on the sign}"
					+ "{1|2|3|4|text: An array with keys 0 thru 3 defining every line on the sign}"
					+ "{}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.SIGN_CHANGED;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
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
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if(!(e instanceof MCSignChangeEvent)) {
				throw new EventException("Cannot convert e to MCSignChangeEvent");
			}
			MCSignChangeEvent event = (MCSignChangeEvent) e;
			Map<String, Mixed> map = evaluate_helper(e);

			map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
			map.put("text", event.getLines());
			map.put("location", ObjectGenerator.GetGenerator().location(event.getBlock().getLocation(), false));

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			if(!(event instanceof MCSignChangeEvent)) {
				return false;
			}
			MCSignChangeEvent sce = (MCSignChangeEvent) event;

			// Allow changing everything at once.
			if(key.equals("text")) {
				if(!(value.isInstanceOf(CArray.TYPE))) {
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
			return "{type: <string match> Type of dispenser | itemname: <string match> Item type which is dispensed}"
					+ "This event is called when a dispenser dispense an item. Cancelling the event cancels dispensing."
					+ "{type: Type of dispenser | item: Item which is dispensed | velocity: Returns an associative array"
					+ " indicating the x/y/z components of item velocity. As a convenience, the magnitude is also included."
					+ " | location: Location of dispenser} "
					+ "{item|velocity} "
					+ "{}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Driver driver() {
			return Driver.BLOCK_DISPENSE;
		}

		@Override
		@SuppressWarnings("deprecation")
		public void bind(BoundEvent event) {
			// handle deprecated prefilter
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("item")) {
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"item\" prefilter in " + getName()
						+ " is deprecated for \"itemname\".", event.getTarget());
				MCItemStack is = Static.ParseItemNotation(null, prefilter.get("item").val(), 1, event.getTarget());
				prefilter.put("itemname", new CString(is.getType().getName(), event.getTarget()));
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCBlockDispenseEvent) {
				MCBlockDispenseEvent event = (MCBlockDispenseEvent) e;
				Prefilters.match(prefilter, "type", event.getBlock().getType().getName(), PrefilterType.STRING_MATCH);
				Prefilters.match(prefilter, "itemname", event.getItem().getType().getName(), PrefilterType.STRING_MATCH);
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			MCBlockDispenseEvent event = (MCBlockDispenseEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = evaluate_helper(e);

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
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
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
		public void bind(BoundEvent event) {
			// handle deprecated prefilter
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("oldname")) {
				MCMaterial mat = StaticLayer.GetMaterialFromLegacy(prefilter.get("oldname").val(), 0);
				prefilter.put("block", new CString(mat.getName(), event.getTarget()));
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"oldname\" prefilter in " + getName()
						+ " is deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
			} else if(prefilter.containsKey("oldtype")) {
				Mixed cid = prefilter.get("oldtype");
				if(cid.isInstanceOf(CInt.TYPE)) {
					int id = (int) ((CInt) cid).getInt();
					int data = 0;
					if(prefilter.containsKey("olddata")) {
						Mixed cdata = prefilter.get("olddata");
						if(cdata.isInstanceOf(CInt.TYPE)) {
							data = (int) ((CInt) cdata).getInt();
						}
					}
					MCMaterial mat = StaticLayer.GetMaterialFromLegacy(id, data);
					if(mat == null) {
						throw new CREBindException("Invalid material id '" + id + "'", event.getTarget());
					}
					prefilter.put("block", new CString(mat.getName(), event.getTarget()));
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"oldtype\" and \"olddata\" prefilters in " + getName()
							+ " are deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
				}
			}
			if(prefilter.containsKey("newname")) {
				MCMaterial mat = StaticLayer.GetMaterialFromLegacy(prefilter.get("newname").val(), 0);
				prefilter.put("newblock", new CString(mat.getName(), event.getTarget()));
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"newname\" prefilter in " + getName()
						+ " is deprecated for \"newblock\". Converted to " + mat.getName(), event.getTarget());
			} else if(prefilter.containsKey("newtype")) {
				Mixed cid = prefilter.get("newtype");
				if(cid.isInstanceOf(CInt.TYPE)) {
					int id = (int) ((CInt) cid).getInt();
					int data = 0;
					if(prefilter.containsKey("newdata")) {
						Mixed cdata = prefilter.get("newdata");
						if(cdata.isInstanceOf(CInt.TYPE)) {
							data = (int) ((CInt) cdata).getInt();
						}
					}
					MCMaterial mat = StaticLayer.GetMaterialFromLegacy(id, data);
					if(mat == null) {
						throw new CREBindException("Invalid material id '" + id + "'", event.getTarget());
					}
					prefilter.put("newname", new CString(mat.getName(), event.getTarget()));
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"newtype\" and \"newdata\" prefilters in " + getName()
							+ " are deprecated for \"newname\". Converted to " + mat.getName(), event.getTarget());
				}
			}
		}

		@Override
		public String docs() {
			return "{block: <string match> The block name before the growth"
					+ " | newblock: <string match> The block name after the growth"
					+ " | world: <macro>}"
					+ " This event is called when a block grows naturally. If the event is cancelled, the block will not grow."
					+ " {block: The block type before the growth"
					+ " | newblock: The block type after the growth"
					+ " | location: the location of the block that will grow}"
					+ " {}"
					+ " {}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			if(!(event instanceof MCBlockGrowEvent)) {
				return false;
			}
			MCBlockGrowEvent e = (MCBlockGrowEvent) event;
			if(prefilter.containsKey("block")) {
				if(!e.getBlock().getType().getName().equals(prefilter.get("block").val())) {
					return false;
				}
			}
			if(prefilter.containsKey("newblock")) {
				if(!e.getNewState().getType().getName().equals(prefilter.get("newblock").val())) {
					return false;
				}
			}
			Prefilters.match(prefilter, "world", e.getBlock().getWorld().getName(), PrefilterType.MACRO);
			return true;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(!(event instanceof MCBlockGrowEvent)) {
				throw new EventException("Cannot convert event to BlockGrowEvent");
			}
			MCBlockGrowEvent e = (MCBlockGrowEvent) event;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> mapEvent = evaluate_helper(e);

			mapEvent.put("block", new CString(e.getBlock().getType().getName(), t));
			mapEvent.put("newblock", new CString(e.getNewState().getType().getName(), t));
			mapEvent.put("location", ObjectGenerator.GetGenerator().location(e.getBlock().getLocation(), false));
			return mapEvent;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent e) {
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
					+ " The instrument may be one of " + StringUtils.Join(MCInstrument.values(), ", ", ", or ") + "."
					+ " {location: The location of the noteblock | instrument: The name of the sound"
					+ " | tone: The note played (eg. F#) | octave: The octave the tone was played (0 - 2)}"
					+ " {}"
					+ " {}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent event) throws PrefilterNonMatchException {
			return event instanceof MCNotePlayEvent;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent event) throws EventException {
			if(!(event instanceof MCNotePlayEvent)) {
				throw new EventException("Cannot convert event to NotePlayEvent");
			}
			MCNotePlayEvent e = (MCNotePlayEvent) event;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = new HashMap<>();

			map.put("location", ObjectGenerator.GetGenerator().location(e.getBlock().getLocation(), false));
			map.put("instrument", new CString(e.getInstrument().name(), t));
			map.put("tone", new CString(e.getNote().getTone().name() + (e.getNote().isSharped() ? "#" : ""), t));
			map.put("octave", new CInt(e.getNote().getOctave(), t));

			return map;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent e) {
			MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "Modifying the instrument or note for note_play"
					+ " events is no longer supported.", value.getTarget());
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
			return "{block: <string match> Block type that is fading"
					+ " | world: <string match>}"
					+ "Called when a block fades, melts or disappears based on world conditions."
					+ "{block: The block type that is fading"
					+ " | newblock: The block type after the fades"
					+ " | location: the location of the block that will fade}"
					+ "{}"
					+ "{}";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public void bind(BoundEvent event) {
			// handle deprecated prefilter
			Map<String, Mixed> prefilter = event.getPrefilter();
			if(prefilter.containsKey("oldname")) {
				MCMaterial mat = StaticLayer.GetMaterialFromLegacy(prefilter.get("oldname").val(), 0);
				prefilter.put("block", new CString(mat.getName(), event.getTarget()));
				MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"oldname\" prefilter in " + getName()
						+ " is deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
			} else if(prefilter.containsKey("oldtype")) {
				Mixed cid = prefilter.get("oldtype");
				if(cid.isInstanceOf(CInt.TYPE)) {
					int id = (int) ((CInt) cid).getInt();
					MCMaterial mat = StaticLayer.GetMaterialFromLegacy(id, 0);
					if(mat == null) {
						throw new CREBindException("Invalid material id '" + id + "'", event.getTarget());
					}
					prefilter.put("block", new CString(mat.getName(), event.getTarget()));
					MSLog.GetLogger().w(MSLog.Tags.DEPRECATION, "The \"oldtype\" prefilter in " + getName()
							+ " is deprecated for \"block\". Converted to " + mat.getName(), event.getTarget());
				}
			}
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(!(e instanceof MCBlockFadeEvent)) {
				return false;
			}
			MCBlockFadeEvent event = (MCBlockFadeEvent) e;
			MCBlock oldBlock = event.getBlock();
			if(prefilter.containsKey("block")) {
				if(!oldBlock.getType().getName().equals(prefilter.get("block").val())) {
					return false;
				}
			}
			Mixed world = prefilter.get("world");
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
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if(!(e instanceof MCBlockFadeEvent)) {
				throw new EventException("Cannot convert event to BlockFadeEvent");
			}
			MCBlockFadeEvent event = (MCBlockFadeEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> mapEvent = evaluate_helper(event);

			mapEvent.put("block", new CString(event.getBlock().getType().getName(), t));
			mapEvent.put("newblock", new CString(event.getNewState().getType().getName(), t));

			mapEvent.put("location", ObjectGenerator.GetGenerator().location(event.getBlock().getLocation(), false));
			return mapEvent;
		}

		@Override
		public boolean modifyEvent(String key, Mixed value, BindableEvent event) {
			return false;
		}
	}
}
