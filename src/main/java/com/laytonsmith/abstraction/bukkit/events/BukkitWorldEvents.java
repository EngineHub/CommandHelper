package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockState;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.enums.MCTreeType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCTreeType;
import com.laytonsmith.abstraction.events.MCStructureGrowEvent;
import com.laytonsmith.abstraction.events.MCWorldEvent;
import com.laytonsmith.abstraction.events.MCWorldLoadEvent;
import com.laytonsmith.abstraction.events.MCWorldSaveEvent;
import com.laytonsmith.abstraction.events.MCWorldUnloadEvent;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author KingFisher
 */
public final class BukkitWorldEvents {

	private BukkitWorldEvents() {
	}

	public static abstract class BukkitMCWorldEvent implements MCWorldEvent {

		private final WorldEvent _event;

		public BukkitMCWorldEvent(WorldEvent event) {
			_event = event;
		}

		@Override
		public Object _GetObject() {
			return _event;
		}

		@Override
		public MCWorld getWorld() {
			return new BukkitMCWorld(_event.getWorld());
		}
	}

	public static class BukkitMCStructureGrowEvent extends BukkitMCWorldEvent implements MCStructureGrowEvent {

		private final StructureGrowEvent _event;

		public BukkitMCStructureGrowEvent(StructureGrowEvent event) {
			super(event);
			_event = event;
		}

		@Override
		public List<MCBlockState> getBlocks() {
			List<BlockState> blocks = _event.getBlocks();
			ArrayList<MCBlockState> r = new ArrayList<>(blocks.size());
			for (BlockState block : blocks) {
				r.add(new BukkitMCBlockState(block));
			}
			return r;
		}

		@Override
		public MCLocation getLocation() {
			return new BukkitMCLocation(_event.getLocation());
		}

		@Override
		public MCPlayer getPlayer() {
			Player player = _event.getPlayer();
			return player == null ? null : new BukkitMCPlayer(_event.getPlayer());
		}

		@Override
		public MCTreeType getSpecies() {
			return BukkitMCTreeType.getConvertor().getAbstractedEnum(_event.getSpecies());
		}

		@Override
		public boolean isFromBonemeal() {
			return _event.isFromBonemeal();
		}
	}

	public static class BukkitMCWorldSaveEvent extends BukkitMCWorldEvent implements MCWorldSaveEvent {

		private final WorldSaveEvent _event;

		public BukkitMCWorldSaveEvent(WorldSaveEvent event) {
			super(event);
			_event = event;
		}
	}

	public static class BukkitMCWorldUnloadEvent extends BukkitMCWorldEvent implements MCWorldUnloadEvent {

		private final WorldUnloadEvent _event;

		public BukkitMCWorldUnloadEvent(WorldUnloadEvent event) {
			super(event);
			_event = event;
		}
	}

	public static class BukkitMCWorldLoadEvent extends BukkitMCWorldEvent implements MCWorldLoadEvent {

		private final WorldLoadEvent _event;

		public BukkitMCWorldLoadEvent(WorldLoadEvent event) {
			super(event);
			_event = event;
		}
	}
}