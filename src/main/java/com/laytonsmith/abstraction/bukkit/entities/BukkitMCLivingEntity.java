package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCEntityEquipment;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCAttributeModifier;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntityEquipment;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCAttribute;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCPotionEffectType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREBadEntityException;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class BukkitMCLivingEntity extends BukkitMCEntityProjectileSource implements MCLivingEntity {

	LivingEntity le;

	public BukkitMCLivingEntity(Entity ent) {
		super(ent);
		this.le = (LivingEntity) ent;
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
		AttributeInstance maxHealth = le.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if(maxHealth == null) {
			return le.getHealth();
		}
		return maxHealth.getValue();
	}

	@Override
	public void setMaxHealth(double health) {
		AttributeInstance maxHealth = le.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if(maxHealth == null) {
			le.setHealth(health);
			return;
		}
		maxHealth.setBaseValue(health);
		if(le.getHealth() > health) {
			le.setHealth(health);
		}
	}

	@Override
	public void resetMaxHealth() {
		AttributeInstance maxHealth = le.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if(maxHealth == null) {
			return;
		}
		double defaultHealth = maxHealth.getDefaultValue();
		maxHealth.setBaseValue(defaultHealth);
		if(le.getHealth() > defaultHealth) {
			le.setHealth(defaultHealth);
		}
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
	public MCBlock getTargetSpace(int maxDistance) {
		List<Block> lst = getLineOfSight(null, maxDistance, 2);
		return new BukkitMCBlock(lst.get(0));
	}

	@Override
	public List<MCBlock> getLineOfSight(HashSet<MCMaterial> transparent, int maxDistance) {
		List<Block> lst = getLineOfSight(transparent, maxDistance, 512);
		List<MCBlock> retn = new ArrayList<>();

		for(Block b : lst) {
			retn.add(new BukkitMCBlock(b));
		}
		return retn;
	}

	private List<Block> getLineOfSight(HashSet<MCMaterial> transparent, int maxDistance, int maxLength) {
		if(maxDistance > 512) {
			maxDistance = 512;
		}

		HashSet<Material> ignored = new HashSet<>();
		if(transparent != null) {
			for(MCMaterial mat : transparent) {
				ignored.add((Material) mat.getHandle());
			}
		}

		ArrayList<Block> blocks = new ArrayList<>();
		Iterator<Block> itr = new BlockIterator(le, maxDistance);

		while(itr.hasNext()) {
			Block block = itr.next();
			blocks.add(block);
			if(maxLength != 0 && blocks.size() > maxLength) {
				blocks.remove(0);
			}
			if(transparent == null) {
				if(!block.isEmpty()) {
					break;
				}
			} else {
				Material id = block.getType();
				if(!ignored.contains(id)) {
					break;
				}
			}
		}
		return blocks;
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
	public MCBlock getTargetBlock(HashSet<MCMaterial> b, int i) {
		List<Block> blocks = getLineOfSight(b, i, 1);
		return new BukkitMCBlock(blocks.get(0));
	}

	@Override
	public boolean hasAI() {
		return le.hasAI();
	}

	@Override
	public boolean addEffect(MCPotionEffectType type, int strength, int ticks, boolean ambient, boolean particles, boolean icon) {
		PotionEffect pe = new PotionEffect((PotionEffectType) type.getConcrete(), ticks, strength, ambient, particles, icon);
		return le.addPotionEffect(pe, true);
	}

	@Override
	public boolean removeEffect(MCPotionEffectType type) {
		PotionEffectType t = (PotionEffectType) type.getConcrete();
		boolean hasIt = false;
		for(PotionEffect pe : le.getActivePotionEffects()) {
			if(pe.getType() == t) {
				hasIt = true;
				break;
			}
		}
		le.removePotionEffect(t);
		return hasIt;
	}

	@Override
	public void removeEffects() {
		for(PotionEffect pe : le.getActivePotionEffects()) {
			le.removePotionEffect(pe.getType());
		}
	}

	@Override
	public List<MCEffect> getEffects() {
		List<MCEffect> effects = new ArrayList<>();
		for(PotionEffect pe : le.getActivePotionEffects()) {
			MCEffect e = new MCEffect(BukkitMCPotionEffectType.valueOfConcrete(pe.getType()), pe.getAmplifier(), pe.getDuration(),
					pe.isAmbient(), pe.hasParticles(), pe.hasIcon());
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
		if(le.getEquipment() == null) {
			return null;
		}
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
		if(!(le instanceof Mob)) {
			throw new CREBadEntityException("This type of entity does not have a target API", t);
		}
		LivingEntity target = ((Mob) le).getTarget();
		if(target == null) {
			return null;
		}
		return new BukkitMCLivingEntity(target);
	}

	@Override
	public void setTarget(MCLivingEntity target, Target t) {
		if(!(le instanceof Mob)) {
			throw new CREBadEntityException("This type of entity not have a target API", t);
		}
		if(target == null) {
			((Mob) le).setTarget(null);
		} else {
			((Mob) le).setTarget(((BukkitMCLivingEntity) target).asLivingEntity());
		}
	}

	@Override
	public void kill() {
		le.setLastDamageCause(new EntityDamageEvent(le, EntityDamageEvent.DamageCause.CUSTOM, le.getHealth()));
		le.setHealth(0D);
	}

	@Override
	public MCEntity getLeashHolder() {
		if(le.isLeashed()) {
			return BukkitConvertor.BukkitGetCorrectEntity(le.getLeashHolder());
		}
		return null;
	}

	@Override
	public boolean isLeashed() {
		return le.isLeashed();
	}

	@Override
	public void setLeashHolder(MCEntity holder) {
		if(holder == null) {
			le.setLeashHolder(null);
		} else {
			le.setLeashHolder(((BukkitMCEntity) holder).getHandle());
		}
	}

	@Override
	public boolean isGliding() {
		return le.isGliding();
	}

	@Override
	public void setGliding(Boolean glide) {
		le.setGliding(glide);
	}

	@Override
	public void setAI(Boolean ai) {
		le.setAI(ai);
	}

	@Override
	public boolean isCollidable() {
		return le.isCollidable();
	}

	@Override
	public void setCollidable(boolean collidable) {
		le.setCollidable(collidable);
	}

	@Override
	public boolean isTameable() {
		return false;
	}

	private AttributeInstance getAttributeInstance(MCAttribute attr) {
		AttributeInstance instance = le.getAttribute(BukkitMCAttribute.getConvertor().getConcreteEnum(attr));
		if(instance == null) {
			throw new IllegalArgumentException("This attribute is not applicable to this entity type.");
		}
		return instance;
	}

	@Override
	public double getAttributeValue(MCAttribute attr) {
		return getAttributeInstance(attr).getValue();
	}

	@Override
	public double getAttributeDefault(MCAttribute attr) {
		return getAttributeInstance(attr).getDefaultValue();
	}

	@Override
	public double getAttributeBase(MCAttribute attr) {
		return getAttributeInstance(attr).getBaseValue();
	}

	@Override
	public void setAttributeBase(MCAttribute attr, double base) {
		getAttributeInstance(attr).setBaseValue(base);
	}

	@Override
	public void resetAttributeBase(MCAttribute attr) {
		AttributeInstance instance = getAttributeInstance(attr);
		instance.setBaseValue(instance.getDefaultValue());
	}

	@Override
	public List<MCAttributeModifier> getAttributeModifiers(MCAttribute attr) {
		Attribute bukkitAttribute = BukkitMCAttribute.getConvertor().getConcreteEnum(attr);
		AttributeInstance instance = le.getAttribute(bukkitAttribute);
		if(instance == null) {
			throw new IllegalArgumentException("This attribute is not applicable to this entity type.");
		}
		Collection<AttributeModifier> modifiers = instance.getModifiers();
		List<MCAttributeModifier> ret = new ArrayList<>();
		for(AttributeModifier modifier : modifiers) {
			ret.add(new BukkitMCAttributeModifier(bukkitAttribute, modifier));
		}
		return ret;
	}

	@Override
	public void addAttributeModifier(MCAttributeModifier modifier) {
		getAttributeInstance(modifier.getAttribute()).addModifier((AttributeModifier) modifier.getHandle());
	}

	@Override
	public void removeAttributeModifier(MCAttributeModifier modifier) {
		getAttributeInstance(modifier.getAttribute()).removeModifier((AttributeModifier) modifier.getHandle());
	}
}
