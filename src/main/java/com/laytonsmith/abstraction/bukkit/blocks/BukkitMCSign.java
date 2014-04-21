

package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterialData;
import org.bukkit.block.Sign;

/**
 *
 * 
 */
public class BukkitMCSign implements MCSign {
    
    Sign s;

    public BukkitMCSign(Sign sign) {
        this.s = sign;
    }

	@Override
    public void setLine(int i, String line1) {
        s.setLine(i, line1);
        s.update();
    }

	@Override
    public String getLine(int i) {
        return s.getLine(i);
    }

	@Override
    public MCMaterialData getData() {
        return new BukkitMCMaterialData(s.getData());
    }

	@Override
    public int getTypeId() {
        return s.getTypeId();
    }
    
}
