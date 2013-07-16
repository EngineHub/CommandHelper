

package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.MCEntity.Velocity;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockState;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.abstraction;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.util.Vector;

/**
 *
 * @author EntityReborn
 */
public class BukkitBlockEvents {

    @abstraction(type = Implementation.Type.BUKKIT)
    public static class BukkitMCBlockBreakEvent implements MCBlockBreakEvent {

        BlockBreakEvent event;

        public BukkitMCBlockBreakEvent(BlockBreakEvent e) {
            event = e;
        }

        public Object _GetObject() {
            return event;
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(event.getPlayer());
        }

        public MCBlock getBlock() {
            return new BukkitMCBlock(event.getBlock());
        }

		public int getExpToDrop() {
			return event.getExpToDrop();
		}

		public void setExpToDrop(int exp) {
			event.setExpToDrop(exp);
		}
    }

    @abstraction(type = Implementation.Type.BUKKIT)
    public static class BukkitMCBlockPlaceEvent implements MCBlockPlaceEvent {

        BlockPlaceEvent event;

        public BukkitMCBlockPlaceEvent(BlockPlaceEvent e) {
            event = e;
        }

        public Object _GetObject() {
            return event;
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(event.getPlayer());
        }

        public MCBlock getBlock() {
            return new BukkitMCBlock(event.getBlock());
        }

        public MCBlock getBlockAgainst() {
            return new BukkitMCBlock(event.getBlockAgainst());
        }

        public MCItemStack getItemInHand() {
            return new BukkitMCItemStack(event.getItemInHand());
        }

        public boolean canBuild() {
            return event.canBuild();
        }

        public MCBlockState getBlockReplacedState() {            
            return new BukkitMCBlockState(event.getBlockReplacedState());
        }
    }

    @abstraction(type = Implementation.Type.BUKKIT)
    public static class BukkitMCSignChangeEvent implements MCSignChangeEvent {

        SignChangeEvent pie;

        public BukkitMCSignChangeEvent(SignChangeEvent e) {
            pie = e;
        }

        public static BukkitMCSignChangeEvent _instantiate(MCBlock sign, MCPlayer player, CArray signtext) {
            String[] text = new String[4];
            for (int i = 0; i < signtext.size(); i++) {
                text[i] = signtext.get(i).toString();
            }
            return new BukkitMCSignChangeEvent(new SignChangeEvent(( (BukkitMCBlock) sign ).__Block(), ( (BukkitMCPlayer) player )._Player(),
                    text));
        }

        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(pie.getPlayer());
        }

        public CString getLine(int index) {
            return new CString(pie.getLine(index), Target.UNKNOWN);
        }

        public CArray getLines() {
            CArray retn = new CArray(Target.UNKNOWN);

            for (int i = 0; i < 4; i++) {
                retn.push(new CString(pie.getLine(i), Target.UNKNOWN));
            }

            return retn;
        }

        public void setLine(int index, String text) {
            pie.setLine(index, text);
        }

        public void setLines(String[] text) {
            for (int i = 0; i < 4; i++) {
                pie.setLine(i, text[i]);
            }
        }

        public MCBlock getBlock() {
            return new BukkitMCBlock(pie.getBlock());
        }

        public Object _GetObject() {
            return pie;
        }
    }

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockEvent implements MCBlockEvent {

		BlockEvent be;

		public BukkitMCBlockEvent(BlockEvent e) {
			be = e;
		}

		public MCBlock getBlock() {
			return new BukkitMCBlock(be.getBlock());
		}

		public Object _GetObject() {
			return be;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBlockDispenseEvent extends BukkitMCBlockEvent
			implements MCBlockDispenseEvent {

		BlockDispenseEvent bde;

		public BukkitMCBlockDispenseEvent(BlockDispenseEvent e) {
			super(e);
			bde = e;
		}

		public MCItemStack getItem() {
			return new BukkitMCItemStack(bde.getItem());
		}

		public void setItem(MCItemStack item) {
			bde.setItem(((BukkitMCItemStack) item).asItemStack());
		}

		public Velocity getVelocity() {
			Vector v = bde.getVelocity();
			return new Velocity(v.length(), v.getX(), v.getY(), v.getZ());
		}

		public void setVelocity(MCEntity.Velocity vel) {
			Vector v = new Vector(vel.x, vel.y, vel.z);
			bde.setVelocity(v);
		}

		public boolean isCancelled() {
			return bde.isCancelled();
		}

		public void setCancelled(boolean cancel) {
			bde.setCancelled(cancel);
		}
	}
}