package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCCommandBlock;
import com.laytonsmith.abstraction.blocks.MCDispenser;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCMetadatable;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCBlockFace;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.block.Banner;

public class BukkitMCBlock extends BukkitMCMetadatable implements MCBlock {

	Block b;

	public BukkitMCBlock(Block b) {
		super(b);
		this.b = b;
	}

	public Block __Block() {
		return b;
	}

	@Override
	public String toString() {
		return b.toString();
	}

	@Override
	public MCMaterial getType() {
		return BukkitMCMaterial.valueOfConcrete(b.getType());
	}

	@Override
	public void setType(MCMaterial mat) {
		b.setType((Material) mat.getHandle());
	}

	@Override
	public void setType(MCMaterial mat, boolean physics) {
		b.setType((Material) mat.getHandle(), physics);
	}

	@Override
	public MCBlockData getBlockData() {
		return new BukkitMCBlockData(b.getBlockData());
	}

	@Override
	public void setBlockData(MCBlockData data, boolean physics) {
		b.setBlockData((BlockData) data.getHandle(), physics);
	}

	@Override
	public MCBlockState getState() {
		return BukkitConvertor.BukkitGetCorrectBlockState(b.getState());
	}

	@Override
	public MCWorld getWorld() {
		return new BukkitMCWorld(b.getWorld());
	}

	@Override
	public int getX() {
		return b.getX();
	}

	@Override
	public int getY() {
		return b.getY();
	}

	@Override
	public int getZ() {
		return b.getZ();
	}

	@Override
	public MCSign getSign() {
		return new BukkitMCSign((Sign) b.getState());
	}

	@Override
	public boolean isSign() {
		return b.getState() instanceof Sign;
	}

	@Override
	public MCCommandBlock getCommandBlock() {
		return new BukkitMCCommandBlock((CommandBlock) b.getState());
	}

	@Override
	public boolean isCommandBlock() {
		return b.getState() instanceof CommandBlock;
	}

	@Override
	public MCDispenser getDispenser() {
		return new BukkitMCDispenser((Dispenser) b.getState());
	}

	@Override
	public boolean isDispenser() {
		return (b.getType() == Material.DISPENSER);
	}

	@Override
	public Collection<MCItemStack> getDrops() {
		Collection<MCItemStack> collection = new ArrayList<>();
		for(ItemStack is : b.getDrops()) {
			collection.add(new BukkitMCItemStack(is));
		}
		return collection;
	}

	@Override
	public Collection<MCItemStack> getDrops(MCItemStack tool) {
		Collection<MCItemStack> collection = new ArrayList<>();
		for(ItemStack is : b.getDrops(((BukkitMCItemStack) tool).asItemStack())) {
			collection.add(new BukkitMCItemStack(is));
		}
		return collection;
	}

	@Override
	public boolean isSolid() {
		return b.getType().isSolid();
	}

	@Override
	public boolean isFlammable() {
		return b.getType().isFlammable();
	}

	@Override
	public boolean isTransparent() {
		return b.getType().isTransparent();
	}

	@Override
	public boolean isOccluding() {
		return b.getType().isOccluding();
	}

	@Override
	public boolean isBurnable() {
		return b.getType().isBurnable();
	}

	@Override
	public boolean isPassable() {
		return b.isPassable();
	}

	@Override
	public boolean isBanner() {
		return b.getState() instanceof Banner;
	}

	@Override
	public MCLocation getLocation() {
		return new BukkitMCLocation(b.getLocation());
	}

	@Override
	public double getTemperature() {
		return b.getTemperature();
	}

	@Override
	public int getLightLevel() {
		return b.getLightLevel();
	}

	@Override
	public int getLightFromSky() {
		return b.getLightFromSky();
	}

	@Override
	public int getLightFromBlocks() {
		return b.getLightFromBlocks();
	}

	@Override
	public int getBlockPower() {
		// this is not useful
		return b.getBlockPower();
	}

	@Override
	public boolean isBlockPowered() {
		return b.isBlockPowered();
	}

	@Override
	public boolean isBlockIndirectlyPowered() {
		return b.isBlockIndirectlyPowered();
	}

	@Override
	public MCBlock getRelative(MCBlockFace face) {
		return new BukkitMCBlock(b.getRelative(face.getModX(), face.getModY(), face.getModZ()));
	}

	@Override
	public MCBlockFace getFace(MCBlock block) {
		return BukkitMCBlockFace.getConvertor().getAbstractedEnum(b.getFace(((BukkitMCBlock) block).b));
	}

	@Override
	public boolean isEmpty() {
		return b == null || b.isEmpty();
	}

	@Override
	public boolean applyBoneMeal() {
		return b.applyBoneMeal(BlockFace.UP);
	}

	@Override
	public boolean breakNaturally(MCItemStack item) {
		if(item == null) {
			return b.breakNaturally(null);
		}
		return b.breakNaturally((ItemStack) item.getHandle());
	}
}
