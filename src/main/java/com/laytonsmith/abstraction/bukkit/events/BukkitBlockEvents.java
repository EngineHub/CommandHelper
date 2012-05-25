/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents.BukkitMCPlayerInteractEvent;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.abstraction;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author EntityReborn
 */
public class BukkitBlockEvents {
	@abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCSignChangeEvent implements MCSignChangeEvent{

        SignChangeEvent pie;
        
        public BukkitMCSignChangeEvent(SignChangeEvent e){
            pie = e;
        }
        
        public static BukkitMCSignChangeEvent _instantiate(MCBlock sign, MCPlayer player, CArray signtext){
            String[] text = new String[4];
            for (int i=0; i<signtext.size(); i++) {
            	text[i] = signtext.get(i).toString();
            }
        	return new BukkitMCSignChangeEvent(new SignChangeEvent(((BukkitMCBlock)sign).__Block(), ((BukkitMCPlayer)player)._Player(), 
                    text));
        }
        
        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pie.getPlayer());
        }
        
        public CString getLine(int index){
        	return new CString(pie.getLine(index), Target.UNKNOWN);
        }
        
        public void setLine(int index, String text) {
        	pie.setLine(index, text);
        }
        
        public MCBlock getBlock() {
        	return (MCBlock)pie.getBlock();
        }

        public Object _GetObject() {
            return pie;
        }
        
    }
}