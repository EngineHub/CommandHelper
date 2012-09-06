package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.core.Static;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffect;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

/**
 *
 * @author layton
 */
public class BukkitMCLivingEntity extends BukkitMCEntity implements
		MCLivingEntity {

	LivingEntity le;

	public BukkitMCLivingEntity(LivingEntity le) {
		super(le);
		this.le = le;
	}

	public int getHealth() {
		return le.getHealth();
	}

	public void setHealth(int i) {
		le.setHealth(i);
	}

	public int getMaxHealth() {
		return le.getMaxHealth();
	}

	public void damage(int i) {
		le.damage(i);
	}

	public void damage(int amount, MCEntity source) {
		le.damage(amount, ((BukkitMCEntity) source).asEntity());
	}

	public double getEyeHeight() {
		return le.getEyeHeight();
	}

	public double getEyeHeight(boolean ignoreSneaking) {
		return le.getEyeHeight(ignoreSneaking);
	}

	public MCLocation getEyeLocation() {
		return new BukkitMCLocation(le.getEyeLocation());
	}

	public MCPlayer getKiller() {
		return new BukkitMCPlayer(le.getKiller());
	}

	public int getLastDamage() {
		return le.getLastDamage();
	}

	public List<MCBlock> getLastTwoTargetBlocks(HashSet<Byte> transparent,
			int maxDistance) {
		List<Block> lst = le.getLastTwoTargetBlocks(transparent, maxDistance);
		List<MCBlock> retn = new ArrayList<MCBlock>();

		for (Block b : lst) {
			retn.add(new BukkitMCBlock(b));
		}

		return retn;
	}

	public List<MCBlock> getLineOfSight(HashSet<Byte> transparent,
			int maxDistance) {
		List<Block> lst = le.getLineOfSight(transparent, maxDistance);
		List<MCBlock> retn = new ArrayList<MCBlock>();

		for (Block b : lst) {
			retn.add(new BukkitMCBlock(b));
		}

		return retn;
	}

	public int getMaximumAir() {
		return le.getMaximumAir();
	}

	public int getMaximumNoDamageTicks() {
		return le.getMaximumNoDamageTicks();
	}

	public int getNoDamageTicks() {
		return le.getNoDamageTicks();
	}

	public int getRemainingAir() {
		return le.getRemainingAir();
	}

	public MCBlock getTargetBlock(HashSet<Byte> b, int i) {
		return new BukkitMCBlock(getFirstTargetBlock(b, i));
	}

	private List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance, int maxLength) {
		if (maxDistance > 512) {
			maxDistance = 512;
		}
		ArrayList<Block> blocks = new ArrayList<Block>();
		Iterator<Block> itr = new BlockIterator(le, maxDistance);

		while (itr.hasNext()) {
			Block block = itr.next();
			blocks.add(block);
			if (maxLength != 0 && blocks.size() > maxLength) {
				blocks.remove(0);
			}
			int id = block.getTypeId();
			if (transparent == null) {
				if (id != 0) {
					break;
				}
			} else {
				if (!transparent.contains((byte) id)) {
					break;
				}
			}
		}
		return blocks;
	}

	public void addEffect(int potionID, int strength, int seconds) {
		PotionEffect pe = new PotionEffect(PotionEffectType.getById(potionID), Static.msToTicks(seconds * 1000), strength);
		try{
			if(le != null){
				le.addPotionEffect(pe, true);
			}
		} catch(NullPointerException e){
			//
			Logger.getLogger(BukkitMCLivingEntity.class.getName()).log(Level.SEVERE, 
					"Bukkit appears to have derped. This is a problem with Bukkit, not CommandHelper. The effect should have still been applied.", e);
		}
//        EntityPlayer ep = ((CraftPlayer) p).getHandle();        
//        MobEffect me = new MobEffect(potionID, seconds * 20, strength);
//        //ep.addEffect(me);
//        //ep.b(me);
//        
//        Class epc = EntityLiving.class;
//        try {
//            Method meth = epc.getDeclaredMethod("b", net.minecraft.server.MobEffect.class);
//            //ep.d(new MobEffect(effect, seconds * 20, strength));
//            //Call it reflectively, because it's deobfuscated in newer versions of CB
//            meth.invoke(ep, me);
//        } catch (Exception e) {
//            try {
//                //Look for the addEffect version                
//                Method meth = epc.getDeclaredMethod("addEffect", MobEffect.class);
//                //ep.addEffect(me);
//                meth.invoke(ep, me);
//            } catch (Exception ex) {
//                Logger.getLogger(BukkitMCPlayer.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
	}

	private Block getFirstTargetBlock(HashSet<Byte> transparent, int maxDistance) {
		List<Block> blocks = getLineOfSight(transparent, maxDistance, 1);
		return blocks.get(0);
	}

	public MCProjectile launchProjectile(MCProjectile projectile) {
		Projectile p = ((BukkitMCProjectile) projectile).asProjectile();
		Projectile proj = le.launchProjectile(p.getClass());

		MCEntity e = BukkitConvertor.BukkitGetCorrectEntity(proj);

		if (e instanceof MCProjectile) {
			return (MCProjectile) e;
		} else {
			return null;
		}
	}

	public void setLastDamage(int damage) {
		le.setLastDamage(damage);
	}

	public void setMaximumAir(int ticks) {
		le.setMaximumAir(ticks);
	}

	public void setMaximumNoDamageTicks(int ticks) {
		le.setMaximumNoDamageTicks(ticks);
	}

	public void setNoDamageTicks(int ticks) {
		le.setNoDamageTicks(ticks);
	}

	public void setRemainingAir(int ticks) {
		le.setRemainingAir(ticks);
	}

	public LivingEntity asLivingEntity() {
		return le;
	}
}
