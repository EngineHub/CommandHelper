package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.annotations.MEnum;

import java.util.UUID;

public interface MCAttributeModifier extends AbstractionObject {
	String getAttributeName();
	MCAttribute getAttribute();
	MCEquipmentSlot getEquipmentSlot();
	Operation getOperation();
	double getAmount();
	UUID getUniqueId();

	@MEnum("com.commandhelper.AttributeModifierOperation")
	enum Operation {
		ADD_NUMBER,
		ADD_SCALAR,
		MULTIPLY_SCALAR_1
	}
}
