package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.*;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCNote;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockState;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.enums.MCIgniteCause;
import com.laytonsmith.abstraction.enums.MCInstrument;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCBlockFace;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCIgniteCause;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCInstrument;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.abstraction;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
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

		@Override
		public List<MCBlock> getPushedBlocks() {
			List<MCBlock> blocks = new ArrayList<>();

			for(Block b : event.getBlocks()) {
				blocks.add(new BukkitMCBlock(b));
			}

			return blocks;
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

	public static class BukkitMCBlockPhysicsEvent implements MCBlockPhysicsEvent {

		BlockPhysicsEvent bpe;

		public BukkitMCBlockPhysicsEvent(BlockPhysicsEvent e){ this.bpe = e; }

		@Override
		public Object _GetObject() {
			return bpe;
		}

		@Override
		public MCMaterial getChangedType() {
			return new BukkitMCMaterial(bpe.getChangedType());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(bpe.getBlock());
		}

		@Override
		public boolean isCancelled() {
			return bpe.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancel) {
			bpe.setCancelled(cancel);
		}

	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockDamageEvent implements MCBlockDamageEvent {

		BlockDamageEvent bde;
		public BukkitMCBlockDamageEvent(BlockDamageEvent e){ this.bde = e; }

		@Override
		public boolean getInstaBreak() {
			return bde.getInstaBreak();
		}

		@Override
		public MCItemStack getItemInHand() {
			return new BukkitMCItemStack(bde.getItemInHand());
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(bde.getPlayer());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(bde.getBlock());
		}

		@Override
		public boolean isCancelled() {
			return bde.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancel) {
			bde.setCancelled(cancel);
		}

		@Override
		public void setInstaBreak(boolean bool) {
			bde.setInstaBreak(bool);
		}

		@Override
		public Object _GetObject() {
			return bde;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockCanBuildEvent implements MCBlockCanBuildEvent {

		BlockCanBuildEvent bcbe;

		public BukkitMCBlockCanBuildEvent(BlockCanBuildEvent e) {
			this.bcbe = e;
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(bcbe.getBlock());
		}

		@Override
		public boolean isBuildable() {
			return bcbe.isBuildable();
		}

		@Override
		public void setBuildable(boolean cancel) {
			bcbe.setBuildable(cancel);
		}

		@Override
		public Object _GetObject() {
			return bcbe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockExplodeEvent implements MCBlockExplodeEvent {

		BlockExplodeEvent bee;

		public BukkitMCBlockExplodeEvent(BlockExplodeEvent e){
			this.bee = e;
		}

		@Override
		public List<MCBlock> getBlockList() {
			List<MCBlock> list = new ArrayList<>();
			for(Block b : bee.blockList())
				list.add(new BukkitMCBlock(b));
			return list;
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(bee.getBlock());
		}

		@Override
		public CDouble getYield() {
			return new CDouble(bee.getYield(), Target.UNKNOWN);
		}

		@Override
		public boolean isCancelled() {
			return bee.isCancelled();
		}

		@Override
		public void setYield(float yield) {
			bee.setYield(yield);
		}

		@Override
		public void setCancelled(boolean cancel) {
			bee.setCancelled(cancel);
		}

		@Override
		public Object _GetObject() {
			return bee;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockFertilizeEvent implements MCBlockFertilizeEvent {

		BlockFertilizeEvent bfe;

		public BukkitMCBlockFertilizeEvent(BlockFertilizeEvent e){
			this.bfe = e;
		}

		@Override
		public List<MCBlockState> getBlocks() {
			List<MCBlockState> list = new ArrayList<>();
			for(BlockState bs : bfe.getBlocks())
				list.add(new BukkitMCBlockState(bs));
			return list;
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(bfe.getPlayer());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(bfe.getBlock());
		}

		@Override
		public boolean isCancelled() {
			return bfe.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancel) {
			bfe.setCancelled(cancel);
		}

		@Override
		public Object _GetObject() {
			return bfe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockRedstoneEvent implements MCBlockRedstoneEvent {

		BlockRedstoneEvent bre;

		public BukkitMCBlockRedstoneEvent(BlockRedstoneEvent e){ this.bre = e; }

		@Override
		public CInt getNewCurrent() {
			return new CInt(bre.getNewCurrent(), Target.UNKNOWN);
		}

		@Override
		public CInt getOldCurrent() {
			return new CInt(bre.getOldCurrent(), Target.UNKNOWN);
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(bre.getBlock());
		}

		@Override
		public void setNewCurrent(int newCurrent) {
			bre.setNewCurrent(newCurrent);
		}

		@Override
		public Object _GetObject() {
			return bre;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBrewingStandFuelEvent implements MCBrewingStandFuelEvent {

		BrewingStandFuelEvent bsfe;

		public BukkitMCBrewingStandFuelEvent(BrewingStandFuelEvent e){ this.bsfe = e; }

		@Override
		public MCItemStack getFuel() {
			return new BukkitMCItemStack(bsfe.getFuel());
		}

		@Override
		public CInt getFuelPower() {
			return new CInt(bsfe.getFuelPower(), Target.UNKNOWN);
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(bsfe.getBlock());
		}

		@Override
		public boolean isConsuming() {
			return bsfe.isConsuming();
		}

		@Override
		public boolean isCancelled() {
			return bsfe.isCancelled();
		}

		@Override
		public void setFuelPower(int fuelPower) {
			bsfe.setFuelPower(fuelPower);
		}

		@Override
		public void setConsuming(boolean consuming) {
			bsfe.setConsuming(consuming);
		}

		@Override
		public void setCancelled(boolean cancel) {
			bsfe.setCancelled(cancel);
		}

		@Override
		public Object _GetObject() {
			return bsfe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBrewEvent implements MCBrewEvent {

		BrewEvent be;

		public BukkitMCBrewEvent(BrewEvent be) {
			this.be = be;
		}

		@Override
		public MCInventory getContents() {
			return new BukkitMCInventory(be.getContents());
		}

		@Override
		public CInt getFuelLevel() {
			return new CInt(be.getFuelLevel(), Target.UNKNOWN);
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(be.getBlock());
		}

		@Override
		public boolean isCancelled() {
			return be.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			be.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return be;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCCauldronLevelChangeEvent implements MCCauldronLevelChangeEvent{

		CauldronLevelChangeEvent clce;

		public BukkitMCCauldronLevelChangeEvent(CauldronLevelChangeEvent e){ this.clce = e; }

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(clce.getEntity());
		}

		@Override
		public CInt getNewLevel() {
			return new CInt(clce.getNewLevel(), Target.UNKNOWN);
		}

		@Override
		public CInt getOldLevel() {
			return new CInt(clce.getOldLevel(), Target.UNKNOWN);
		}

		@Override
		public CString getReason() {
			return new CString(clce.getReason().name(), Target.UNKNOWN);
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(clce.getBlock());
		}

		@Override
		public boolean isCancelled() {
			return clce.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			clce.setCancelled(cancelled);
		}

		@Override
		public void setNewLevel(int newLevel) {
			clce.setNewLevel(newLevel);
		}

		@Override
		public Object _GetObject() {
			return clce;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCFurnaceBurnEvent implements MCFurnaceBurnEvent {

		FurnaceBurnEvent fbe;

		public BukkitMCFurnaceBurnEvent(FurnaceBurnEvent e){ this.fbe = e; }

		@Override
		public CInt getBurnTine() {
			return new CInt(fbe.getBurnTime(), Target.UNKNOWN);
		}

		@Override
		public MCItemStack getFuel() {
			return new BukkitMCItemStack(fbe.getFuel());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(fbe.getBlock());
		}

		@Override
		public boolean isBurning() {
			return fbe.isBurning();
		}

		@Override
		public boolean isCancelled() {
			return fbe.isCancelled();
		}

		@Override
		public void setBurning(boolean burning) {
			fbe.setBurning(burning);
		}

		@Override
		public void setBurnTime(int burnTime) {
			fbe.setBurnTime(burnTime);
		}

		@Override
		public void setCancelled(boolean cancel) {
			fbe.setCancelled(cancel);
		}

		@Override
		public Object _GetObject() {
			return fbe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCFurnaceExtractEvent implements MCFurnaceExtractEvent {

		FurnaceExtractEvent fee;

		public BukkitMCFurnaceExtractEvent(FurnaceExtractEvent e){
			this.fee = e;
		}

		@Override
		public CInt getExpToDrop() {
			return new CInt(fee.getExpToDrop(), Target.UNKNOWN);
		}

		@Override
		public CInt getItemAmount() {
			return new CInt(fee.getItemAmount(), Target.UNKNOWN);
		}

		@Override
		public MCMaterial getItemType() {
			return new BukkitMCMaterial(fee.getItemType());
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(fee.getPlayer());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(fee.getBlock());
		}

		@Override
		public void setExpToDrop(int exp) {
			fee.setExpToDrop(exp);
		}

		@Override
		public Object _GetObject() {
			return fee;
		}
	}

	@abstraction(type= Implementation.Type.BUKKIT)
	public static class BukkitMCFurnaceSmeltEvent implements MCFurnaceSmeltEvent {

		FurnaceSmeltEvent fse;

		public BukkitMCFurnaceSmeltEvent(FurnaceSmeltEvent e){ this.fse = e; }

		@Override
		public MCItemStack getResult() {
			return new BukkitMCItemStack(fse.getResult());
		}

		@Override
		public MCItemStack getSource() {
			return new BukkitMCItemStack(fse.getSource());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(fse.getBlock());
		}

		@Override
		public boolean isCancelled() {
			return fse.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancel) {
			fse.setCancelled(cancel);
		}

		@Override
		public void setResult(ItemStack result) {
			fse.setResult(result);
		}

		@Override
		public Object _GetObject() {
			return fse;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCLeavesDeacyEvent implements MCLeavesDecayEvent {

		LeavesDecayEvent lde;

		public BukkitMCLeavesDeacyEvent(LeavesDecayEvent e){ this.lde = e; }

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(lde.getBlock());
		}

		@Override
		public boolean isCancelled() {
			return lde.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancel) {
			lde.setCancelled(cancel);
		}

		@Override
		public Object _GetObject() {
			return lde;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCMoistureChangeEvent implements MCMoistureChangeEvent {

		MoistureChangeEvent mce;

		public BukkitMCMoistureChangeEvent(MoistureChangeEvent e){ this.mce = e;}

		@Override
		public MCBlockState getNewState() {
			return new BukkitMCBlockState(mce.getNewState());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(mce.getBlock());
		}

		@Override
		public boolean isCancelled() {
			return mce.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancel) {
			mce.setCancelled(cancel);
		}

		@Override
		public Object _GetObject() {
			return mce;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCSpongeAbsorbEvent implements MCSpongeAbsorbEvent {

		SpongeAbsorbEvent sae;

		public BukkitMCSpongeAbsorbEvent(SpongeAbsorbEvent e){ this.sae = e; }

		@Override
		public List<MCBlockState> getBlocks() {
			List<MCBlockState> list = new ArrayList<>();
			for(BlockState bs : sae.getBlocks())
				list.add(new BukkitMCBlockState(bs));
			return list;
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(sae.getBlock());
		}

		@Override
		public boolean isCancelled() {
			return sae.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancel) {
			sae.setCancelled(cancel);
		}

		@Override
		public Object _GetObject() {
			return sae;
		}
	}


}
