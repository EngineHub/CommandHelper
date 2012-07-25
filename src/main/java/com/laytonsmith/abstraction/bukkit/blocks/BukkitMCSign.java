

package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterialData;
import org.bukkit.block.Sign;

/**
 *
 * @author layton
 */
public class BukkitMCSign implements MCSign {
    
    Sign s;

    public BukkitMCSign(Sign sign) {
        this.s = sign;
    }

    public void setLine(int i, String line1) {
        s.setLine(i, line1);
        s.update();
    }

    public String getLine(int i) {
        return s.getLine(i);
    }

    public MCMaterialData getData() {
        return new BukkitMCMaterialData(s.getData());
    }

    public int getTypeId() {
        return s.getTypeId();
    }
    
}
