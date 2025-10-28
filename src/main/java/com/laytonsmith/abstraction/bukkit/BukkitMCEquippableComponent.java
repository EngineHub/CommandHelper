package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCEquippableComponent;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEquipmentSlot;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.components.EquippableComponent;

import java.util.ArrayList;
import java.util.Collection;

public class BukkitMCEquippableComponent implements MCEquippableComponent {

	private final EquippableComponent equippableComponent;

	public BukkitMCEquippableComponent(EquippableComponent foodComponent) {
		this.equippableComponent = foodComponent;
	}

	@Override
	public MCEquipmentSlot getSlot() {
		return BukkitMCEquipmentSlot.getConvertor().getAbstractedEnum(this.equippableComponent.getSlot());
	}

	@Override
	public void setSlot(MCEquipmentSlot slot) {
		this.equippableComponent.setSlot(BukkitMCEquipmentSlot.getConvertor().getConcreteEnum(slot));
	}

	@Override
	public Collection<MCEntityType> getAllowedEntities() {
		Collection<EntityType> allowedEntities = this.equippableComponent.getAllowedEntities();
		if(allowedEntities == null) {
			return null;
		}
		Collection<MCEntityType> ret = new ArrayList<>();
		for(EntityType type : allowedEntities) {
			ret.add(BukkitMCEntityType.valueOfConcrete(type));
		}
		return ret;
	}

	@Override
	public void setAllowedEntities(Collection<MCEntityType> types) {
		if(types == null) {
			this.equippableComponent.setAllowedEntities((EntityType) null);
		} else {
			Collection<EntityType> entityTypes = new ArrayList<>();
			for(MCEntityType type : types) {
				entityTypes.add((EntityType) type.getConcrete());
			}
			this.equippableComponent.setAllowedEntities(entityTypes);
		}
	}

	@Override
	public String getCameraOverlay() {
		if(this.equippableComponent.getCameraOverlay() == null) {
			return null;
		}
		return this.equippableComponent.getCameraOverlay().toString();
	}

	@Override
	public void setCameraOverlay(String overlay) {
		if(overlay == null) {
			this.equippableComponent.setCameraOverlay(null);
		} else {
			this.equippableComponent.setCameraOverlay(NamespacedKey.fromString(overlay));
		}
	}

	@Override
	public String getAssetId() {
		if(this.equippableComponent.getModel() == null) {
			return null;
		}
		return this.equippableComponent.getModel().toString();
	}

	@Override
	public void setAssetId(String assetId) {
		if(assetId == null) {
			this.equippableComponent.setModel(null);
		} else {
			this.equippableComponent.setModel(NamespacedKey.fromString(assetId));
		}
	}

	@Override
	public String getEquipSound() {
		Sound sound = this.equippableComponent.getEquipSound();
		// API may say this cannot be null, but it can be at runtime.
		if(sound == null) {
			return null;
		}
		try {
			return sound.getKey().toString();
		} catch(NullPointerException | IllegalStateException ex) {
			// Probably a new sound event definition instead of a key, so we have no choice but to return null for now.
			// The ISException is probably a server bug. NPE catch is just in case getKey() nullability is changed.
			// Registry.SOUNDS.getKey(sound) is cleaner, as it can return null, but it's Paper only.
			return null;
		}
	}

	@Override
	public void setEquipSound(String sound) {
		if(sound == null) {
			this.equippableComponent.setEquipSound(null);
		} else {
			NamespacedKey key = NamespacedKey.fromString(sound);
			if(key != null) {
				this.equippableComponent.setEquipSound(Registry.SOUNDS.get(key));
			}
		}
	}

	@Override
	public boolean isDispensable() {
		return this.equippableComponent.isDispensable();
	}

	@Override
	public void setDispensable(boolean dispensable) {
		this.equippableComponent.setDispensable(dispensable);
	}

	@Override
	public boolean isEquipOnInteract() {
		return this.equippableComponent.isEquipOnInteract();
	}

	@Override
	public void setEquipOnInteract(boolean equipOnInteract) {
		this.equippableComponent.setEquipOnInteract(equipOnInteract);
	}

	@Override
	public boolean isSwappable() {
		return this.equippableComponent.isSwappable();
	}

	@Override
	public void setSwappable(boolean swappable) {
		this.equippableComponent.setSwappable(swappable);
	}

	@Override
	public boolean isDamageOnHurt() {
		return this.equippableComponent.isDamageOnHurt();
	}

	@Override
	public void setDamageOnHurt(boolean damagedOnHurt) {
		this.equippableComponent.setDamageOnHurt(damagedOnHurt);
	}

	@Override
	public Object getHandle() {
		return equippableComponent;
	}

	@Override
	public String toString() {
		return equippableComponent.toString();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof BukkitMCEquippableComponent && equippableComponent.equals(((BukkitMCEquippableComponent) o).equippableComponent);
	}

	@Override
	public int hashCode() {
		return equippableComponent.hashCode();
	}
}
