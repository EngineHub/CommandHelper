package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCProjectile;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.enums.MCProjectileType;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

/**
 *
 * @author layton
 */
public class BukkitMCLivingEntity extends BukkitMCEntity implements MCLivingEntity {

	LivingEntity le;

	public BukkitMCLivingEntity(LivingEntity le) {
		super(le);
		this.le = le;
	}

	public double getHealth() {
		return le.getHealth();
	}

	public void setHealth(double i) {
		le.setHealth(i);
	}

	public double getMaxHealth() {
		return le.getMaxHealth();
	}
	
	public void setMaxHealth(double health) {
		le.setMaxHealth(health);
	}
	
	public void resetMaxHealth() {
		le.resetMaxHealth();
	}

	public void damage(double i) {
		le.damage(i);
	}

	public void damage(double amount, MCEntity source) {
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

	public double getLastDamage() {
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

	public boolean hasLineOfSight(MCEntity other) {
		return le.hasLineOfSight(((BukkitMCEntity) other).asEntity());
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
		return new BukkitMCBlock(le.getTargetBlock(b, i));
	}

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
	
	public int getMaxEffect(){
		try {
			PotionEffectType[] arr = (PotionEffectType[])ReflectionUtils.get(PotionEffectType.class, "byId");
			return arr.length - 1;
		} catch(ReflectionUtils.ReflectionException e){
			return Integer.MAX_VALUE;
		}
	}

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

	public List<MCEffect> getEffects(){
		List<MCEffect> effects = new ArrayList<MCEffect>();
		for(PotionEffect pe : le.getActivePotionEffects()){
			MCEffect e = new MCEffect(pe.getType().getId(), pe.getAmplifier(), 
					(int)(Static.ticksToMs(pe.getDuration()) / 1000), pe.isAmbient());
			effects.add(e);
		}
		return effects;
	}

	public MCProjectile launchProjectile(MCProjectileType projectile) {
		EntityType et = EntityType.valueOf(projectile.name());
		Class<? extends Entity> c = et.getEntityClass();
		Projectile proj = le.launchProjectile(c.asSubclass(Projectile.class));

		MCEntity e = BukkitConvertor.BukkitGetCorrectEntity(proj);

		if (e instanceof MCProjectile) {
			return (MCProjectile) e;
		} else {
			return null;
		}
	}

	public void setLastDamage(double damage) {
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

	public MCEntityEquipment getEquipment() {
		return new BukkitMCEntityEquipment(le.getEquipment());
	}
	
	public boolean getCanPickupItems() {
		return le.getCanPickupItems();
	}
	
	public void setCanPickupItems(boolean pickup) {
		le.setCanPickupItems(pickup);
	}
	
	public boolean getRemoveWhenFarAway() {
			return le.getRemoveWhenFarAway();
	}
	
	public void setRemoveWhenFarAway(boolean remove) {
		le.setRemoveWhenFarAway(remove);
	}

	public MCLivingEntity getTarget(Target t) {
		if (!(le instanceof Creature)) {
			throw new ConfigRuntimeException("This type of mob does not have a target API", 
					ExceptionType.BadEntityException, t);
		}
		LivingEntity target = ((Creature) le).getTarget();
		return target == null ? null : new BukkitMCLivingEntity(target);
	}

	public void setTarget(MCLivingEntity target, Target t) {
		if (!(le instanceof Creature)) {
			throw new ConfigRuntimeException("This type of mob does not have a target API", 
					ExceptionType.BadEntityException, t);
		}
		((Creature) le).setTarget(target == null ? null : ((BukkitMCLivingEntity) target).asLivingEntity());
	}

	public String getCustomName() {
		return le.getCustomName();
	}

	public boolean isCustomNameVisible() {
		return le.isCustomNameVisible();
	}

	public void setCustomName(String name) {
		le.setCustomName(name);
	}

	public void setCustomNameVisible(boolean visible) {
		le.setCustomNameVisible(visible);
	}
	
	public void kill(){
		le.setLastDamageCause(new EntityDamageEvent(le, EntityDamageEvent.DamageCause.CUSTOM, le.getHealth()));
		le.setHealth(0D);
	}

	public MCEntity getLeashHolder() {
		return le.isLeashed() ? BukkitConvertor.BukkitGetCorrectEntity(le.getLeashHolder()) : null;
	}

	public boolean isLeashed() {
		return le.isLeashed();
	}

	public void setLeashHolder(MCEntity holder) {
		le.setLeashHolder(holder == null ? null : ((BukkitMCEntity) holder).asEntity());
	}
}
