package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;

import java.util.Collection;

public interface MCEquippableComponent extends AbstractionObject {
	MCEquipmentSlot getSlot();
	void setSlot(MCEquipmentSlot slot);
	Collection<MCEntityType> getAllowedEntities();
	void setAllowedEntities(Collection<MCEntityType> types);
	String getCameraOverlay();
	void setCameraOverlay(String overlay);
	String getAssetId();
	void setAssetId(String assetId);
	String getEquipSound();
	void setEquipSound(String sound);
	boolean isDispensable();
	void setDispensable(boolean dispensable);
	boolean isEquipOnInteract();
	void setEquipOnInteract(boolean equipOnInteract);
	boolean isSwappable();
	void setSwappable(boolean swappable);
	boolean isDamageOnHurt();
	void setDamageOnHurt(boolean damagedOnHurt);
}
