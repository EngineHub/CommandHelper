package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.EnterBedResult")
public enum MCEnterBedResult {
	NOT_POSSIBLE_HERE,
	NOT_POSSIBLE_NOW,
	NOT_SAFE,
	OK,
	OTHER_PROBLEM,
	TOO_FAR_AWAY
}
