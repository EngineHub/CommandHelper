package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCDecoratedPot;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import org.bukkit.Material;
import org.bukkit.block.DecoratedPot;

import java.util.HashMap;
import java.util.Map;

public class BukkitMCDecoratedPot extends BukkitMCBlockState implements MCDecoratedPot {

	DecoratedPot dp;

	public BukkitMCDecoratedPot(DecoratedPot pot) {
		super(pot);
		this.dp = pot;
	}

	@Override
	public DecoratedPot getHandle() {
		return this.dp;
	}

	@Override
	public Map<MCDecoratedPot.Side, MCMaterial> getSherds() {
		Map<MCDecoratedPot.Side, MCMaterial> sherds = new HashMap<>();
		for(Map.Entry<DecoratedPot.Side, Material> sherd : this.dp.getSherds().entrySet()) {
			sherds.put(MCDecoratedPot.Side.valueOf(sherd.getKey().name()), BukkitMCMaterial.valueOfConcrete(sherd.getValue()));
		}
		return sherds;
	}

	@Override
	public void setSherd(MCDecoratedPot.Side side, MCMaterial sherd) {
		DecoratedPot.Side concreteSide = DecoratedPot.Side.valueOf(side.name());
		this.dp.setSherd(concreteSide, (Material) sherd.getHandle());
	}
}
