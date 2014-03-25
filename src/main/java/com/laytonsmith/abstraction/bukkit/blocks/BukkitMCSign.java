

package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterialData;
import org.bukkit.block.Sign;

/**
 *
 * @author layton
 */
public class BukkitMCSign extends BukkitMCBlockState implements MCSign {
    
    Sign s;

    public BukkitMCSign(Sign sign) {
		super(sign);
        this.s = sign;
    }

	@Override
	public Sign getHandle() {
		return s;
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
