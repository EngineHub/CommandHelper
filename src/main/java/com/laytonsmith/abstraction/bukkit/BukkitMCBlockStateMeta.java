package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.MCBlockStateMeta;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.core.Static;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.logging.Level;

public class BukkitMCBlockStateMeta extends BukkitMCItemMeta implements MCBlockStateMeta {

	BlockStateMeta bsm;
	Material mat;

	public BukkitMCBlockStateMeta(BlockStateMeta meta) {
		super(meta);
		this.bsm = meta;
		this.mat = null;
	}

	public BukkitMCBlockStateMeta(BlockStateMeta meta, Material mat) {
		super(meta);
		this.bsm = meta;
		this.mat = mat;
	}

	@Override
	public boolean hasBlockState() {
		return bsm.hasBlockState();
	}

	@Override
	public MCBlockState getBlockState() {
		return getBlockState(false);
	}

	@Override
	public MCBlockState getBlockState(boolean copy) {
		BlockStateMeta meta = bsm;
		if(copy && Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_18_X)) {
			// For some types, getBlockState() writes to the block entity tags, which are not copied prior to 1.18.2.
			// Since the tags are no longer equal when compared later, unexpected behavior can occur.
			// For example, when getting a shulker box's BlockState on BlockPlaceEvent, it can duplicate the item.
			// Copying the meta before getting the block state ensures the original tags are unaffected.
			meta = (BlockStateMeta) meta.clone();
		} else if(Static.getServer().getMinecraftVersion().gte(MCVersion.MC1_20_1)
				&& mat == Material.DECORATED_POT
				&& Static.getServer().getMinecraftVersion().lte(MCVersion.MC1_20_4)) {
			// Workaround upstream bug with decorated pots missing the "id" tag in the BlockEntityTag when broken.
			// Without this fix, getting the BlockState from this meta may result in a default decorated pot.
			try {
				Class craftMetaBlockStateClass = Class.forName(((BukkitMCServer) Static.getServer()).getCraftBukkitPackage()
						+ ".inventory.CraftMetaBlockState");
				Class nbtTagCompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");
				Object nbt = ReflectionUtils.get(craftMetaBlockStateClass, meta, "blockEntityTag");
				if(nbt != null) {
					ReflectionUtils.invokeMethod(nbtTagCompoundClass, nbt, "a", new Class[]{String.class, String.class},
							new Object[]{"id", "minecraft:decorated_pot"});
				}
			} catch (Exception ex) {
				Static.getLogger().log(Level.WARNING, "Failed to fix decorated pot tag.", ex);
			}
		}
		try {
			return BukkitConvertor.BukkitGetCorrectBlockState(meta.getBlockState());
		} catch (Exception ex) {
			// Broken server implementation.
			Static.getLogger().log(Level.WARNING, ex.getMessage() + " when"
					+ " trying to get the BlockState from " + bsm.toString());
			return null;
		}
	}

	@Override
	public void setBlockState(MCBlockState state) {
		bsm.setBlockState((BlockState) state.getHandle());
	}
}
