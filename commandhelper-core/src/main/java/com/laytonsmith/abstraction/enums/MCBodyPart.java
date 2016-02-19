package com.laytonsmith.abstraction.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * This enum intends to be forward thinking, in anticipation of the ability to control body parts of mobs.
 */
public enum MCBodyPart {
	ArmLeft(true),
	ArmLeft1(false),
	ArmLeft2(false),
	ArmLeft3(false),
	ArmRight(true),
	ArmRight1(false),
	ArmRight2(false),
	ArmRight3(false),
	Head(true),
	LegLeft(true),
	LegLeft1(false),
	LegLeft2(false),
	LegLeft3(false),
	LegRight(true),
	LegRight1(false),
	LegRight2(false),
	LegRight3(false),
	Mouth(false),
	Tail(false),
	Torso(true),
	WingLeft(false),
	WingRight(false);

	private boolean existsForHumanoids;
	private MCBodyPart(boolean existsForHumanoids) {
		this.existsForHumanoids = existsForHumanoids;
	}

	public boolean isHumanoidPart() {
		return existsForHumanoids;
	}

	public static boolean isHumanoidPart(MCBodyPart part) {
		return part.existsForHumanoids;
	}

	public static List<MCBodyPart> humanoidParts() {
		List<MCBodyPart> bodyPartArrayList = new ArrayList<MCBodyPart>();
		for (MCBodyPart part : values()) {
			if (part.isHumanoidPart()) {
				bodyPartArrayList.add(part);
			}
		}
		return bodyPartArrayList;
	}
}
