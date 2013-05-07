

package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.bukkit.BukkitMCCreatureSpawner;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.annotations.WrappedItem;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author layton
 */
public class BukkitMCBlock implements MCBlock{
    @WrappedItem Block b;
    
    public int getTypeId(){
        if(b == null){
            return 0;
        }
        return b.getTypeId();
    }
    
    public byte getData(){
        return b.getData();
    }

    public void setTypeId(int idata) {
        b.setTypeId(idata);
    }

    public void setData(byte imeta) {
        b.setData(imeta);
    }

    public MCBlockState getState() {
        if(b.getState() == null){
            return null;
        }
		if(b.getState() instanceof CreatureSpawner){
			return AbstractionUtils.wrap((CreatureSpawner)b.getState());
		}
        return AbstractionUtils.wrap(b.getState());
    }

    public MCMaterial getType() {
        if(b.getType() == null){
            return null;
        }
        return AbstractionUtils.wrap(b.getType());
    }

    public MCWorld getWorld() {
        return AbstractionUtils.wrap(b.getWorld());
    }

    public int getX() {
        return b.getX();
    }

    public int getY() {
        return b.getY();
    }

    public int getZ() {
        return b.getZ();
    }

    public Block __Block() {
        return b;
    }

    public MCSign getSign() {
        return AbstractionUtils.wrap((Sign)b.getState());
    }

    public boolean isSign() {
        return (b.getType() == Material.SIGN || b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN);
    }

    public boolean isNull() {
        return b == null;
    }

    public Collection<MCItemStack> getDrops() {
        Collection<MCItemStack> collection = new ArrayList<MCItemStack>();
        for(ItemStack is : b.getDrops()){
            collection.add((MCItemStack)AbstractionUtils.wrap(is));
        }
        return collection;
    }
	
	public Collection<MCItemStack> getDrops(MCItemStack tool) {
		Collection<MCItemStack> collection = new ArrayList<MCItemStack>();
		for(ItemStack is : b.getDrops(((BukkitMCItemStack) tool).asItemStack())){
			collection.add((MCItemStack)AbstractionUtils.wrap(is));
		}
		return collection;
	}

	@Override
	public String toString() {
		return b.toString();
	}

	public boolean isSolid() {
		return b.getType().isSolid();
	}

	public boolean isFlammable() {
		return b.getType().isFlammable();
	}

	public boolean isTransparent() {
		return b.getType().isTransparent();
	}

	public boolean isOccluding() {
		return b.getType().isOccluding();
	}

	public boolean isBurnable() {
		return b.getType().isBurnable();
	}

	public MCLocation getLocation() {
		return AbstractionUtils.wrap(b.getLocation());
	}

	public int getLightLevel() {
		return b.getLightLevel();
	}

	public int getBlockPower() {
		return b.getBlockPower();
	}

	public MCBlock getRelative(MCBlockFace face) {
		return AbstractionUtils.wrap(b.getRelative(face.getModX(), face.getModY(), face.getModZ()));
	}

	public <T> T getHandle() {
		return (T) b;
	}
}
