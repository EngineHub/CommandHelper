

package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterialData;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.block.Sign;

/**
 *
 * @author layton
 */
public class BukkitMCSign implements MCSign {
    
    @WrappedItem Sign s;

    public void setLine(int i, String line1) {
        s.setLine(i, line1);
        s.update();
    }

    public String getLine(int i) {
        return s.getLine(i);
    }

    public MCMaterialData getData() {
        return AbstractionUtils.wrap(s.getData());
    }

    public int getTypeId() {
        return s.getTypeId();
    }

	public <T> T getHandle() {
		return (T) s;
	}
    
}
