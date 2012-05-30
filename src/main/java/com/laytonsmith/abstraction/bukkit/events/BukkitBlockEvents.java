/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.abstraction;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * @author EntityReborn
 */
public class BukkitBlockEvents {
	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCBlockBreakEvent implements MCBlockBreakEvent {
		BlockBreakEvent event;
		
		public BukkitMCBlockBreakEvent(BlockBreakEvent e){
            event = e;
        }
		
		public Object _GetObject() {
			return event;
		}

		public MCPlayer getPlayer() {
            return new BukkitMCPlayer(event.getPlayer());
        }

		public Block getBlock() {
        	return event.getBlock();
        }
		
	}
	
	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCBlockPlaceEvent implements MCBlockPlaceEvent {
		BlockPlaceEvent event;
		
		public BukkitMCBlockPlaceEvent(BlockPlaceEvent e){
            event = e;
        }
		
		public Object _GetObject() {
			return event;
		}

		public MCPlayer getPlayer() {
            return new BukkitMCPlayer(event.getPlayer());
        }

		public Block getBlock() {
        	return event.getBlock();
        }

		public Block getBlockAgainst() {
			return event.getBlockAgainst();
		}

		public MCItemStack getItemInHand() {
			return new BukkitMCItemStack(event.getItemInHand());
		}

		public boolean canBuild() {
			return event.canBuild();
		}

		public BlockState getBlockReplacedState() {
			return event.getBlockReplacedState();
		}
	}
	
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
        
        public CArray getLines() {
        	CArray retn = new CArray(Target.UNKNOWN);
        	
        	for (int i=0; i<4; i++) {
        		retn.push(new CString(pie.getLine(i), Target.UNKNOWN));
        	}
        	
        	return retn;
        }
        
        public void setLine(int index, String text) {
        	pie.setLine(index, text);
        }
        
        public void setLines(String[] text) {
        	for (int i=0; i<4; i++) {
        		pie.setLine(i, text[i]);
        	}
        }
        
        public Block getBlock() {
        	return pie.getBlock();
        }

        public Object _GetObject() {
            return pie;
        }
        
    }
}