package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCCommandBlock;
import com.laytonsmith.abstraction.blocks.MCDispenser;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.bukkit.BukkitMCCreatureSpawner;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCMetadatable;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCBlockFace;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Command;

import java.util.ArrayList;
import java.util.Collection;

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
	public boolean isNull() {
		return b == null;
	}

	@Override
	public MCMaterial getType() {
		Material type = b.getType();
		return type == null ? null : new BukkitMCMaterial(type);
	}

	@Override
	public int getTypeId() {
		if(b == null) {
			return 0;
		}
		return b.getTypeId();
	}

	@Override
	public byte getData() {
		return b.getData();
	}

	@Override
	public void setType(MCMaterial mat) {
		b.setType((Material) mat.getHandle());
	}

	@Override
	public void setTypeId(int idata) {
		b.setTypeId(idata);
	}

	@Override
	public void setData(byte imeta) {
		b.setData(imeta);
	}

	@Override
	public void setTypeAndData(int type, byte data, boolean physics) {
		b.setTypeIdAndData(type, data, physics);
	}

	@Override
	public MCBlockState getState() {
		BlockState bs = b.getState();
		if(bs instanceof CreatureSpawner) {
			return new BukkitMCCreatureSpawner((CreatureSpawner) bs);
		}
		return new BukkitMCBlockState(bs);
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
		Material type = b.getType();
		return (type == Material.SIGN || type == Material.SIGN_POST || type == Material.WALL_SIGN);
	}

	@Override
	public MCCommandBlock getCommandBlock() {
		return new BukkitMCCommandBlock((CommandBlock) b.getState());
	}

	@Override
	public boolean isCommandBlock() {
		return Command.class.isAssignableFrom(b.getType().getData());
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
		Collection<MCItemStack> collection = new ArrayList<MCItemStack>();
		for(ItemStack is : b.getDrops()) {
			collection.add(new BukkitMCItemStack(is));
		}
		return collection;
	}

	@Override
	public Collection<MCItemStack> getDrops(MCItemStack tool) {
		Collection<MCItemStack> collection = new ArrayList<MCItemStack>();
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
}
