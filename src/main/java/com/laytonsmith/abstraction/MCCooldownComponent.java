package com.laytonsmith.abstraction;

public interface MCCooldownComponent extends AbstractionObject {
	float getSeconds();
	void setSeconds(float seconds);
	String getCooldownGroup();
	void setCooldownGroup(String cooldownGroup);
}
