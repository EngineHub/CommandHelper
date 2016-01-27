package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.enums.MCTreeType;
import com.laytonsmith.abstraction.events.MCStructureGrowEvent;
import com.laytonsmith.abstraction.events.MCWorldEvent;
import com.laytonsmith.abstraction.events.MCWorldLoadEvent;
import com.laytonsmith.abstraction.events.MCWorldSaveEvent;
import com.laytonsmith.abstraction.events.MCWorldUnloadEvent;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.Prefilters;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author KingFisher
 */
public class WorldEvents {

	private WorldEvents() {
	}

	public static String docs() {
		return "Contains events related to the world.";
	}

	public static abstract class WorldEvent extends AbstractEvent {

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			Map<String, Construct> r = evaluate_helper(e);
			r.put("world", new CString(((MCWorldEvent) e).getWorld().getName(), Target.UNKNOWN));
			return r;
		}
	}

	@api
	public static class world_load extends WorldEvent {

		@Override
		public String getName() {
			return "world_load";
		}

		@Override
		public Driver driver() {
			return Driver.WORLD_LOAD;
		}

		@Override
		public String docs() {
			return "{world: <macro>}"
					+ "Fires when a world is loaded."
					+ "{world: The loaded world.}"
					+ "{}"
					+ "{}";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof MCWorldLoadEvent) {
				Prefilters.match(prefilter, "world", ((MCWorldEvent) e).getWorld().getName(), Prefilters.PrefilterType.MACRO);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}
	}

	@api
	public static class world_unload extends WorldEvent {

		@Override
		public String getName() {
			return "world_unload";
		}

		@Override
		public Driver driver() {
			return Driver.WORLD_UNLOAD;
		}

		@Override
		public String docs() {
			return "{world: <macro>}"
					+ "Fires when a world is unloaded."
					+ "{world: The unloaded world.}"
					+ "{}"
					+ "{}";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof MCWorldUnloadEvent) {
				Prefilters.match(prefilter, "world", ((MCWorldEvent) e).getWorld().getName(), Prefilters.PrefilterType.MACRO);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}
	}

	@api
	public static class world_save extends WorldEvent {

		@Override
		public String getName() {
			return "world_save";
		}

		@Override
		public Driver driver() {
			return Driver.WORLD_SAVE;
		}

		@Override
		public String docs() {
			return "{world: <macro>}"
					+ "Fires when a world is saved."
					+ "{world: The saved world.}"
					+ "{}"
					+ "{}";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof MCWorldSaveEvent) {
				Prefilters.match(prefilter, "world", ((MCWorldEvent) e).getWorld().getName(), Prefilters.PrefilterType.MACRO);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}
	}

	@api
	public static class tree_grow extends WorldEvent {

		@Override
		public String getName() {
			return "tree_grow";
		}

		@Override
		public Driver driver() {
			return Driver.TREE_GROW;
		}

		@Override
		public String docs() {
			return "{world: <macro> | player: <macro> | type: <macro> | bonemeal: <boolean match>}"
					+ "Fires when a tree grows."
					+ "{world: The world where the tree grown. | type: The tree type, can be one of " + StringUtils.Join(MCTreeType.values(), ", ", ", or ", " or ")
					+ " | bonemeal: If the tree grown due to a bonemeal or not. | player: The player who used the bonemeal, or null.}"
					+ "{}"
					+ "{}";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof MCStructureGrowEvent) {
				MCStructureGrowEvent event = (MCStructureGrowEvent) e;
				Prefilters.match(prefilter, "world", event.getWorld().getName(), Prefilters.PrefilterType.MACRO);
				MCPlayer player = event.getPlayer();
				Prefilters.match(prefilter, "player", player == null ? null : player.getName(), Prefilters.PrefilterType.MACRO);
				Prefilters.match(prefilter, "type", event.getSpecies().name(), Prefilters.PrefilterType.MACRO);
				Prefilters.match(prefilter, "bonemeal", event.isFromBonemeal(), Prefilters.PrefilterType.BOOLEAN_MATCH);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			Map<String, Construct> r = super.evaluate(e);
			MCStructureGrowEvent event = (MCStructureGrowEvent) e;
			List<MCBlockState> blocks = event.getBlocks();
			CArray a = new CArray(Target.UNKNOWN, blocks.size());
			for (MCBlockState block : blocks) {
				a.push(ObjectGenerator.GetGenerator().location(block.getLocation(), false), Target.UNKNOWN);
			}
			r.put("blocks", a);
			r.put("location", ObjectGenerator.GetGenerator().location(event.getLocation(), false));
			MCPlayer player = event.getPlayer();
			r.put("player", player == null ? CNull.NULL : new CString(player.getName(), Target.UNKNOWN));
			r.put("type", new CString(event.getSpecies().name(), Target.UNKNOWN));
			r.put("bonemeal", CBoolean.get(event.isFromBonemeal()));
			return r;
		}

		@Override
		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			return false;
		}

		@Override
		public BindableEvent convert(CArray manualObject, Target t) {
			return null;
		}
	}
}