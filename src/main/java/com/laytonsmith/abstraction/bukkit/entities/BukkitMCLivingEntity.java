package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.entities.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.entities.MCPlayer;
import com.laytonsmith.abstraction.entities.MCProjectile;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntityEquipment;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.enums.MCProjectileType;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.block.Block;
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
public abstract class BukkitMCLivingEntity extends BukkitMCDamageable implements MCLivingEntity {

	public BukkitMCLivingEntity(LivingEntity living) {
		super(living);
	}

	@Override
	public LivingEntity getHandle() {
		return (LivingEntity) metadatable;
	}

	public double getEyeHeight() {
		return getHandle().getEyeHeight();
	}

	public double getEyeHeight(boolean ignoreSneaking) {
		return getHandle().getEyeHeight(ignoreSneaking);
	}

	public MCLocation getEyeLocation() {
		return new BukkitMCLocation(getHandle().getEyeLocation());
	}

	public MCPlayer getKiller() {
		return new BukkitMCPlayer(getHandle().getKiller());
	}

	public double getLastDamage() {
		return getHandle().getLastDamage();
	}

	public List<MCBlock> getLastTwoTargetBlocks(HashSet<Byte> transparent,
			int maxDistance) {
		List<Block> lst = getHandle().getLastTwoTargetBlocks(transparent, maxDistance);
		List<MCBlock> retn = new ArrayList<MCBlock>();

		for (Block b : lst) {
			retn.add(new BukkitMCBlock(b));
		}

		return retn;
	}

	public List<MCBlock> getLineOfSight(HashSet<Byte> transparent,
			int maxDistance) {
		List<Block> lst = getHandle().getLineOfSight(transparent, maxDistance);
		List<MCBlock> retn = new ArrayList<MCBlock>();

		for (Block b : lst) {
			retn.add(new BukkitMCBlock(b));
		}

		return retn;
	}

	public boolean hasLineOfSight(MCEntity other) {
		return getHandle().hasLineOfSight((Entity) other.getHandle());
	}

	public int getMaximumAir() {
		return getHandle().getMaximumAir();
	}

	public int getMaximumNoDamageTicks() {
		return getHandle().getMaximumNoDamageTicks();
	}

	public int getNoDamageTicks() {
		return getHandle().getNoDamageTicks();
	}

	public int getRemainingAir() {
		return getHandle().getRemainingAir();
	}

	public MCBlock getTargetBlock(HashSet<Byte> b, int i) {
		return new BukkitMCBlock(getHandle().getTargetBlock(b, i));
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
		Iterator<Block> itr = new BlockIterator(getHandle(), maxDistance);

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
			if(getHandle() != null){
				getHandle().addPotionEffect(pe, true);
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
		for(PotionEffect pe : getHandle().getActivePotionEffects()) {
			if (pe.getType() == t) {
				hasIt = true;
				break;
			}
		}
		getHandle().removePotionEffect(t);
		return hasIt;
    }

	public List<MCEffect> getEffects(){
		List<MCEffect> effects = new ArrayList<MCEffect>();
		for(PotionEffect pe : getHandle().getActivePotionEffects()){
			MCEffect e = new MCEffect(pe.getType().getId(), pe.getAmplifier(), 
					(int)(Static.ticksToMs(pe.getDuration()) / 1000), pe.isAmbient());
			effects.add(e);
		}
		return effects;
	}

	public MCProjectile launchProjectile(MCProjectileType projectile) {
		EntityType et = EntityType.valueOf(projectile.name());
		Class<? extends Entity> c = et.getEntityClass();
		Projectile proj = getHandle().launchProjectile(c.asSubclass(Projectile.class));

		MCEntity e = BukkitConvertor.BukkitGetCorrectEntity(proj);

		if (e instanceof MCProjectile) {
			return (MCProjectile) e;
		} else {
			return null;
		}
	}

	public void setLastDamage(double damage) {
		getHandle().setLastDamage(damage);
	}

	public void setMaximumAir(int ticks) {
		getHandle().setMaximumAir(ticks);
	}

	public void setMaximumNoDamageTicks(int ticks) {
		getHandle().setMaximumNoDamageTicks(ticks);
	}

	public void setNoDamageTicks(int ticks) {
		getHandle().setNoDamageTicks(ticks);
	}

	public void setRemainingAir(int ticks) {
		getHandle().setRemainingAir(ticks);
	}

	public MCEntityEquipment getEquipment() {
		return new BukkitMCEntityEquipment(getHandle().getEquipment());
	}
	
	public boolean getCanPickupItems() {
		return getHandle().getCanPickupItems();
	}
	
	public void setCanPickupItems(boolean pickup) {
		getHandle().setCanPickupItems(pickup);
	}
	
	public boolean getRemoveWhenFarAway() {
			return getHandle().getRemoveWhenFarAway();
	}
	
	public void setRemoveWhenFarAway(boolean remove) {
		getHandle().setRemoveWhenFarAway(remove);
	}

	public String getCustomName() {
		return getHandle().getCustomName();
	}

	public boolean isCustomNameVisible() {
		return getHandle().isCustomNameVisible();
	}

	public void setCustomName(String name) {
		getHandle().setCustomName(name);
	}

	public void setCustomNameVisible(boolean visible) {
		getHandle().setCustomNameVisible(visible);
	}
	
	public void kill() {
		getHandle().setLastDamageCause(new EntityDamageEvent(getHandle(), EntityDamageEvent.DamageCause.CUSTOM, getHandle().getHealth()));
		getHandle().setHealth(0D);
	}

	public MCEntity getLeashHolder() {
		return getHandle().isLeashed() ? BukkitConvertor.BukkitGetCorrectEntity(getHandle().getLeashHolder()) : null;
	}

	public boolean isLeashed() {
		return getHandle().isLeashed();
	}

	public void setLeashHolder(MCEntity holder) {
		getHandle().setLeashHolder(holder == null ? null : ((Entity) holder.getHandle()));
	}
}