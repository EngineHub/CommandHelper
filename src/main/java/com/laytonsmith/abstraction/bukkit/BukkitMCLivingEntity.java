package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntityProjectileSource;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

/**
 *
 * 
 */
public class BukkitMCLivingEntity extends BukkitMCEntityProjectileSource implements MCLivingEntity {

	LivingEntity le;

	public BukkitMCLivingEntity(LivingEntity le) {
		super(le);
		this.le = le;
	}

	@Override
	public double getHealth() {
		return le.getHealth();
	}

	@Override
	public void setHealth(double i) {
		le.setHealth(i);
	}

	@Override
	public double getMaxHealth() {
		return le.getMaxHealth();
	}
	
	@Override
	public void setMaxHealth(double health) {
		le.setMaxHealth(health);
	}
	
	@Override
	public void resetMaxHealth() {
		le.resetMaxHealth();
	}

	@Override
	public void damage(double i) {
		le.damage(i);
	}

	@Override
	public void damage(double amount, MCEntity source) {
		le.damage(amount, ((BukkitMCEntity) source).getHandle());
	}

	@Override
	public double getEyeHeight() {
		return le.getEyeHeight();
	}

	@Override
	public double getEyeHeight(boolean ignoreSneaking) {
		return le.getEyeHeight(ignoreSneaking);
	}

	@Override
	public MCLocation getEyeLocation() {
		return new BukkitMCLocation(le.getEyeLocation());
	}

	@Override
	public MCPlayer getKiller() {
		return new BukkitMCPlayer(le.getKiller());
	}

	@Override
	public double getLastDamage() {
		return le.getLastDamage();
	}

	@Override
	public List<MCBlock> getLastTwoTargetBlocks(HashSet<Byte> transparent,
			int maxDistance) {
		List<Block> lst = le.getLastTwoTargetBlocks(transparent, maxDistance);
		List<MCBlock> retn = new ArrayList<MCBlock>();

		for (Block b : lst) {
			retn.add(new BukkitMCBlock(b));
		}

		return retn;
	}

	@Override
	public List<MCBlock> getLineOfSight(HashSet<Byte> transparent,
			int maxDistance) {
		List<Block> lst = le.getLineOfSight(transparent, maxDistance);
		List<MCBlock> retn = new ArrayList<MCBlock>();

		for (Block b : lst) {
			retn.add(new BukkitMCBlock(b));
		}

		return retn;
	}

	@Override
	public boolean hasLineOfSight(MCEntity other) {
		return le.hasLineOfSight(((BukkitMCEntity) other).getHandle());
	}

	@Override
	public int getMaximumAir() {
		return le.getMaximumAir();
	}

	@Override
	public int getMaximumNoDamageTicks() {
		return le.getMaximumNoDamageTicks();
	}

	@Override
	public int getNoDamageTicks() {
		return le.getNoDamageTicks();
	}

	@Override
	public int getRemainingAir() {
		return le.getRemainingAir();
	}

	@Override
	public MCBlock getTargetBlock(HashSet<Byte> b, int i) {
		return new BukkitMCBlock(le.getTargetBlock(b, i));
	}

	@Override
	public MCBlock getTargetBlock(HashSet<Short> b, int i, boolean castToByte) {
		if (castToByte) {
			if (b == null) {
				return getTargetBlock(null, i);
			}
			HashSet<Byte> bb = new HashSet<Byte>();
			for (int id : b) {
				bb.add((byte) id);
			}
			return getTargetBlock(bb, i);
		}
		return new BukkitMCBlock(getFirstTargetBlock(b, i));
	}

	private Block getFirstTargetBlock(HashSet<Short> transparent, int maxDistance) {
		List<Block> blocks = getLineOfSight(transparent, maxDistance, 1);
		return blocks.get(0);
	}

	private List<Block> getLineOfSight(HashSet<Short> transparent, int maxDistance, int maxLength) {
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
				if (!transparent.contains((short) id)) {
					break;
				}
			}
		}
		return blocks;
	}

	@Override
	public void addEffect(int potionID, int strength, int seconds, boolean ambient, Target t) {
		PotionEffect pe = new PotionEffect(PotionEffectType.getById(potionID), (int)Static.msToTicks(seconds * 1000), 
				strength, ambient);
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
	
	@Override
	public int getMaxEffect(){
		try {
			PotionEffectType[] arr = (PotionEffectType[])ReflectionUtils.get(PotionEffectType.class, "byId");
			return arr.length - 1;
		} catch(ReflectionUtils.ReflectionException e){
			return Integer.MAX_VALUE;
		}
	}

	@Override
    public boolean removeEffect(int potionID) {
		PotionEffectType t = PotionEffectType.getById(potionID);
		boolean hasIt = false;
		for(PotionEffect pe : le.getActivePotionEffects()) {
			if (pe.getType() == t) {
				hasIt = true;
				break;
			}
		}
		le.removePotionEffect(t);
		return hasIt;
    }

	@Override
	public List<MCEffect> getEffects(){
		List<MCEffect> effects = new ArrayList<MCEffect>();
		for(PotionEffect pe : le.getActivePotionEffects()){
			MCEffect e = new MCEffect(pe.getType().getId(), pe.getAmplifier(), 
					(int)(Static.ticksToMs(pe.getDuration()) / 1000), pe.isAmbient());
			effects.add(e);
		}
		return effects;
	}

	@Override
	public void setLastDamage(double damage) {
		le.setLastDamage(damage);
	}

	@Override
	public void setMaximumAir(int ticks) {
		le.setMaximumAir(ticks);
	}

	@Override
	public void setMaximumNoDamageTicks(int ticks) {
		le.setMaximumNoDamageTicks(ticks);
	}

	@Override
	public void setNoDamageTicks(int ticks) {
		le.setNoDamageTicks(ticks);
	}

	@Override
	public void setRemainingAir(int ticks) {
		le.setRemainingAir(ticks);
	}

	public LivingEntity asLivingEntity() {
		return le;
	}

	@Override
	public MCEntityEquipment getEquipment() {
		return new BukkitMCEntityEquipment(le.getEquipment());
	}
	
	@Override
	public boolean getCanPickupItems() {
		return le.getCanPickupItems();
	}
	
	@Override
	public void setCanPickupItems(boolean pickup) {
		le.setCanPickupItems(pickup);
	}
	
	@Override
	public boolean getRemoveWhenFarAway() {
			return le.getRemoveWhenFarAway();
	}
	
	@Override
	public void setRemoveWhenFarAway(boolean remove) {
		le.setRemoveWhenFarAway(remove);
	}

	@Override
	public MCLivingEntity getTarget(Target t) {
		if (!(le instanceof Creature)) {
			throw new ConfigRuntimeException("This type of mob does not have a target API", 
					ExceptionType.BadEntityException, t);
		}
		LivingEntity target = ((Creature) le).getTarget();
		return target == null ? null : new BukkitMCLivingEntity(target);
	}

	@Override
	public void setTarget(MCLivingEntity target, Target t) {
		if (!(le instanceof Creature)) {
			throw new ConfigRuntimeException("This type of mob does not have a target API", 
					ExceptionType.BadEntityException, t);
		}
		((Creature) le).setTarget(target == null ? null : ((BukkitMCLivingEntity) target).asLivingEntity());
	}

	@Override
	public String getCustomName() {
		return le.getCustomName();
	}

	@Override
	public boolean isCustomNameVisible() {
		return le.isCustomNameVisible();
	}

	@Override
	public void setCustomName(String name) {
		le.setCustomName(name);
	}

	@Override
	public void setCustomNameVisible(boolean visible) {
		le.setCustomNameVisible(visible);
	}
	
	@Override
	public void kill(){
		le.setLastDamageCause(new EntityDamageEvent(le, EntityDamageEvent.DamageCause.CUSTOM, le.getHealth()));
		le.setHealth(0D);
	}

	@Override
	public MCEntity getLeashHolder() {
		return le.isLeashed() ? BukkitConvertor.BukkitGetCorrectEntity(le.getLeashHolder()) : null;
	}

	@Override
	public boolean isLeashed() {
		return le.isLeashed();
	}

	@Override
	public void setLeashHolder(MCEntity holder) {
		le.setLeashHolder(holder == null ? null : ((BukkitMCEntity) holder).getHandle());
	}
}
