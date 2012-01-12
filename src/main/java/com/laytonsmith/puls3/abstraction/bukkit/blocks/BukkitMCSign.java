/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.abstraction.bukkit.blocks;

import com.laytonsmith.puls3.abstraction.blocks.MCSign;
import org.bukkit.block.Sign;

/**
 *
 * @author layton
 */
class BukkitMCSign implements MCSign {
    
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
    
}
