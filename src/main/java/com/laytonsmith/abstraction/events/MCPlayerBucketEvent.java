package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;
import org.bukkit.inventory.ItemStack;

public interface MCPlayerBucketEvent extends BindableEvent {

	MCBlock getBlockClicked();

	MCBlockFace getBlockFace();

	MCMaterial getBucket();

	MCItemStack getItemStack();

	CString getType();

	MCPlayer getPlayer();

	boolean isCancelled();

	void setCancelled(boolean cancel);

	void setItemStack(ItemStack is);

}
