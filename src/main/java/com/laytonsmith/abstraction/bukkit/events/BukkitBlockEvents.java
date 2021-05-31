package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCNote;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockState;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.enums.MCIgniteCause;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCBlockFace;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCIgniteCause;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCInstrument;
import com.laytonsmith.abstraction.events.MCBlockBreakEvent;
import com.laytonsmith.abstraction.events.MCBlockBurnEvent;
import com.laytonsmith.abstraction.events.MCBlockDispenseEvent;
import com.laytonsmith.abstraction.events.MCBlockEvent;
import com.laytonsmith.abstraction.events.MCBlockExplodeEvent;
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
import com.laytonsmith.abstraction.events.MCBlockFormEvent;
import com.laytonsmith.annotations.abstraction;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BukkitBlockEvents {

	// Stub for actual events below.
	public static class BukkitMCBlockPistonEvent implements MCBlockPistonEvent {

		BlockPistonEvent event;

		public BukkitMCBlockPistonEvent(BlockPistonEvent e) {
			event = e;
		}

		@Override
		public Object _GetObject() {
			return event;
		}

		@Override
		public MCBlockFace getDirection() {
			return BukkitMCBlockFace.getConvertor().getAbstractedEnum(event.getDirection());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(event.getBlock());
		}

		@Override
		public boolean isSticky() {
			return event.isSticky();
		}

		@Override
		public List<MCBlock> getAffectedBlocks() {
			List<Block> bukkitBlocks;
			if(event instanceof BlockPistonExtendEvent) {
				bukkitBlocks = ((BlockPistonExtendEvent) event).getBlocks();
			} else if(event instanceof BlockPistonRetractEvent) {
				bukkitBlocks = ((BlockPistonRetractEvent) event).getBlocks();
			} else {
				throw new Error("Unsupported piston event: " + event.getClass().getName());
			}

			List<MCBlock> blocks = new ArrayList<>(bukkitBlocks.size());
			for(Block b : bukkitBlocks) {
				blocks.add(new BukkitMCBlock(b));
			}
			return blocks;
		}

		@Override
		public boolean isCancelled() {
			return event.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			event.setCancelled(cancelled);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockPistonExtendEvent extends BukkitMCBlockPistonEvent implements MCBlockPistonExtendEvent {

		BlockPistonExtendEvent event;

		public BukkitMCBlockPistonExtendEvent(BlockPistonExtendEvent e) {
			super(e);

			event = e;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockPistonRetractEvent extends BukkitMCBlockPistonEvent implements MCBlockPistonRetractEvent {

		BlockPistonRetractEvent event;

		public BukkitMCBlockPistonRetractEvent(BlockPistonRetractEvent e) {
			super(e);

			event = e;
		}

		@Override
		public MCLocation getRetractedLocation() {
			return new BukkitMCLocation(event.getRetractLocation());
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockBreakEvent implements MCBlockBreakEvent {

		BlockBreakEvent event;
		boolean dropsModified = false;
		List<MCItemStack> drops = null;

		public BukkitMCBlockBreakEvent(BlockBreakEvent e) {
			event = e;
		}

		@Override
		public Object _GetObject() {
			return event;
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(event.getPlayer());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(event.getBlock());
		}

		@Override
		public int getExpToDrop() {
			return event.getExpToDrop();
		}

		@Override
		public void setExpToDrop(int exp) {
			event.setExpToDrop(exp);
		}

		@Override
		public List<MCItemStack> getDrops() {
			return this.drops;
		}

		@Override
		public void setDrops(List<MCItemStack> drops) {
			dropsModified = true;
			this.drops = drops;
		}

		@Override
		public boolean isModified() {
			return dropsModified;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockPlaceEvent implements MCBlockPlaceEvent {

		BlockPlaceEvent event;

		public BukkitMCBlockPlaceEvent(BlockPlaceEvent e) {
			event = e;
		}

		@Override
		public Object _GetObject() {
			return event;
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(event.getPlayer());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(event.getBlock());
		}

		@Override
		public MCBlock getBlockAgainst() {
			return new BukkitMCBlock(event.getBlockAgainst());
		}

		@Override
		public MCItemStack getItemInHand() {
			return new BukkitMCItemStack(event.getItemInHand());
		}

		@Override
		public boolean canBuild() {
			return event.canBuild();
		}

		@Override
		public MCBlockState getBlockReplacedState() {
			return new BukkitMCBlockState(event.getBlockReplacedState());
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockBurnEvent implements MCBlockBurnEvent {

		BlockBurnEvent event;

		public BukkitMCBlockBurnEvent(BlockBurnEvent e) {
			event = e;
		}

		@Override
		public Object _GetObject() {
			return event;
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(event.getBlock());
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockIgniteEvent extends BukkitMCBlockEvent implements MCBlockIgniteEvent {

		BlockIgniteEvent event;

		public BukkitMCBlockIgniteEvent(BlockIgniteEvent e) {
			super(e);
			event = e;
		}

		@Override
		public MCIgniteCause getCause() {
			return BukkitMCIgniteCause.getConvertor().getAbstractedEnum(event.getCause());
		}

		@Override
		public MCEntity getIgnitingEntity() {
			if(event.getIgnitingEntity() != null) {
				return new BukkitMCEntity(event.getIgnitingEntity());
			}

			return null;
		}

		@Override
		public MCBlock getIgnitingBlock() {
			if(event.getIgnitingBlock() != null) {
				return new BukkitMCBlock(event.getIgnitingBlock());
			}

			return null;
		}

		@Override
		public MCPlayer getPlayer() {
			if(event.getPlayer() != null) {
				return new BukkitMCPlayer(event.getPlayer());
			}

			return null;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockFromToEvent implements MCBlockFromToEvent {

		BlockFromToEvent event;

		public BukkitMCBlockFromToEvent(BlockFromToEvent e) {
			event = e;
		}

		@Override
		public Object _GetObject() {
			return event;
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(event.getBlock());
		}

		@Override
		public MCBlock getToBlock() {
			return new BukkitMCBlock(event.getToBlock());
		}

		@Override
		public MCBlockFace getBlockFace() {
			return BukkitMCBlockFace.getConvertor().getAbstractedEnum(event.getFace());
		}

		@Override
		public boolean isCancelled() {
			return event.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			event.setCancelled(cancelled);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCSignChangeEvent implements MCSignChangeEvent {

		SignChangeEvent pie;

		public BukkitMCSignChangeEvent(SignChangeEvent e) {
			pie = e;
		}

		public static BukkitMCSignChangeEvent _instantiate(MCBlock sign, MCPlayer player, CArray signtext) {
			String[] text = new String[4];
			for(int i = 0; i < signtext.size(); i++) {
				text[i] = signtext.get(i, Target.UNKNOWN).toString();
			}
			return new BukkitMCSignChangeEvent(new SignChangeEvent(((BukkitMCBlock) sign).__Block(), ((BukkitMCPlayer) player)._Player(),
					text));
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(pie.getPlayer());
		}

		@Override
		public CString getLine(int index) {
			return new CString(pie.getLine(index), Target.UNKNOWN);
		}

		@Override
		public CArray getLines() {
			CArray retn = new CArray(Target.UNKNOWN);

			for(int i = 0; i < 4; i++) {
				retn.push(new CString(pie.getLine(i), Target.UNKNOWN), Target.UNKNOWN);
			}

			return retn;
		}

		@Override
		public void setLine(int index, String text) {
			pie.setLine(index, text);
		}

		@Override
		public void setLines(String[] text) {
			for(int i = 0; i < 4; i++) {
				pie.setLine(i, text[i]);
			}
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(pie.getBlock());
		}

		@Override
		public Object _GetObject() {
			return pie;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockEvent implements MCBlockEvent {

		BlockEvent be;

		public BukkitMCBlockEvent(BlockEvent e) {
			be = e;
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(be.getBlock());
		}

		@Override
		public Object _GetObject() {
			return be;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockDispenseEvent extends BukkitMCBlockEvent implements MCBlockDispenseEvent {

		BlockDispenseEvent bde;

		public BukkitMCBlockDispenseEvent(BlockDispenseEvent e) {
			super(e);
			bde = e;
		}

		@Override
		public MCItemStack getItem() {
			return new BukkitMCItemStack(bde.getItem());
		}

		@Override
		public void setItem(MCItemStack item) {
			if(item == null || "AIR".equals(item.getType().getName())) {
				throw new CREIllegalArgumentException("Due to Bukkit's handling of this event, the item cannot be set to null."
						+ " Until they change this, workaround by cancelling the event and manipulating the block"
						+ " using inventory functions.", Target.UNKNOWN);
			} else {
				bde.setItem(((BukkitMCItemStack) item).asItemStack());
			}
		}

		@Override
		public Vector3D getVelocity() {
			Vector v = bde.getVelocity();
			return new Vector3D(v.getX(), v.getY(), v.getZ());
		}

		@Override
		public void setVelocity(Vector3D vel) {
			Vector v = new Vector(vel.X(), vel.Y(), vel.Z());
			bde.setVelocity(v);
		}

		@Override
		public boolean isCancelled() {
			return bde.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancel) {
			bde.setCancelled(cancel);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockGrowEvent implements MCBlockGrowEvent {

		BlockGrowEvent bge;

		public BukkitMCBlockGrowEvent(BlockGrowEvent event) {
			bge = event;
		}

		@Override
		public Object _GetObject() {
			return bge;
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(bge.getBlock());
		}

		@Override
		public MCBlockState getNewState() {
			return new BukkitMCBlockState(bge.getNewState());
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCNotePlayEvent implements MCNotePlayEvent {

		NotePlayEvent npe;

		public BukkitMCNotePlayEvent(NotePlayEvent event) {
			npe = event;
		}

		@Override
		public Object _GetObject() {
			return npe;
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(npe.getBlock());
		}

		@Override
		public MCNote getNote() {
			return new BukkitMCNote(npe.getNote());
		}

		@Override
		public MCInstrument getInstrument() {
			return BukkitMCInstrument.getConvertor().getAbstractedEnum(npe.getInstrument());
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockFadeEvent implements MCBlockFadeEvent {

		BlockFadeEvent bfe;

		public BukkitMCBlockFadeEvent(BlockFadeEvent bfe) {
			this.bfe = bfe;
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(bfe.getBlock());
		}

		@Override
		public MCBlockState getNewState() {
			return new BukkitMCBlockState(bfe.getNewState());
		}

		@Override
		public Object _GetObject() {
			return bfe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockExplodeEvent implements MCBlockExplodeEvent {

		BlockExplodeEvent event;

		public BukkitMCBlockExplodeEvent(BlockExplodeEvent event) {
			this.event = event;
		}

		@Override
		public List<MCBlock> getBlocks() {
			List<MCBlock> ret = new ArrayList<>();
			for(Block b : event.blockList()) {
				ret.add(new BukkitMCBlock(b));
			}
			return ret;
		}

		@Override
		public void setBlocks(List<MCBlock> blocks) {
			event.blockList().clear();
			for(MCBlock b : blocks) {
				event.blockList().add((Block) b.getHandle());
			}
		}

		@Override
		public float getYield() {
			return event.getYield();
		}

		@Override
		public void setYield(float power) {
			event.setYield(power);
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(event.getBlock());
		}

		@Override
		public Object _GetObject() {
			return event;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitBlockFormEventEvent implements MCBlockFormEvent {
		BlockFormEvent event;

		public BukkitBlockFormEventEvent(BlockFormEvent event) {
			this.event = event;
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(event.getBlock());
		}

		@Override
		public MCBlockState getNewState() {
			return new BukkitMCBlockState(event.getNewState());
		}

		@Override
		public Object _GetObject() {
			return event;
		}
	}
}
