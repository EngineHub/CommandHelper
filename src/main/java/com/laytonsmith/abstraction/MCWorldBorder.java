package com.laytonsmith.abstraction;

public interface MCWorldBorder {
	void reset();
	double getSize();
	void setSize(double size);
	void setSize(double size, int seconds);
	MCLocation getCenter();
	void setCenter(MCLocation location);
	double getDamageBuffer();
	void setDamageBuffer(double blocks);
	double getDamageAmount();
	void setDamageAmount(double damage);
	int getWarningTime();
	void setWarningTime(int seconds);
	int getWarningDistance();
	void setWarningDistance(int distance);
}
