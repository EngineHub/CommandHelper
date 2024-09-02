package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCNamespacedKey;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCEquipmentSlotGroup;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCAttribute;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEquipmentSlot;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.UUID;

public class BukkitMCAttributeModifier implements MCAttributeModifier {

	private Attribute a;
	private AttributeModifier am;

	public BukkitMCAttributeModifier(Attribute a, AttributeModifier am) {
		this.a = a;
		this.am = am;
	}

	@Override
	public Object getHandle() {
		return am;
	}

	@Override
	public MCNamespacedKey getKey() {
		return new BukkitMCNamespacedKey(am.getKey());
	}

	@Override
	public String getAttributeName() {
		return am.getName();
	}

	@Override
	public MCAttribute getAttribute() {
		return BukkitMCAttribute.getConvertor().getAbstractedEnum(a);
	}

	@Override
	public MCEquipmentSlot getEquipmentSlot() {
		EquipmentSlot slot = am.getSlot();
		if(slot == null) {
			return null;
		}
		return BukkitMCEquipmentSlot.getConvertor().getAbstractedEnum(slot);
	}

	@Override
	public MCEquipmentSlotGroup getEquipmentSlotGroup() {
		EquipmentSlotGroup slotGroup = am.getSlotGroup();
		if(slotGroup == EquipmentSlotGroup.ANY) {
			return MCEquipmentSlotGroup.ANY;
		} else if(slotGroup == EquipmentSlotGroup.ARMOR) {
			return MCEquipmentSlotGroup.ARMOR;
		} else if(slotGroup == EquipmentSlotGroup.HAND) {
			return MCEquipmentSlotGroup.HAND;
		} else if(slotGroup.toString().equals("body")) { // BODY slot group is missing from Spigot
			return MCEquipmentSlotGroup.BODY;
		}
		return null;
	}

	@Override
	public MCAttributeModifier.Operation getOperation() {
		return Operation.getConvertor().getAbstractedEnum(am.getOperation());
	}

	@Override
	public double getAmount() {
		return am.getAmount();
	}

	@Override
	public UUID getUniqueId() {
		return am.getUniqueId();
	}

	@Override
	public String toString() {
		return am.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof MCAttributeModifier && am.equals(((MCAttributeModifier) obj).getHandle());
	}

	@Override
	public int hashCode() {
		return am.hashCode();
	}

	@abstractionenum(
			implementation = Implementation.Type.BUKKIT,
			forAbstractEnum = MCAttributeModifier.Operation.class,
			forConcreteEnum = AttributeModifier.Operation.class
	)
	public static class Operation extends EnumConvertor<MCAttributeModifier.Operation, AttributeModifier.Operation> {

		private static Operation instance;

		public static Operation getConvertor() {
			if(instance == null) {
				instance = new Operation();
			}
			return instance;
		}
	}
}
