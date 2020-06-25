package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.WorldType")
public enum MCWorldType {
	NORMAL,
	FLAT,
	VERSION_1_1(false),
	LARGE_BIOMES,
	AMPLIFIED,
	CUSTOMIZED(false),
	BUFFET(false);

	private final boolean create;

	MCWorldType() {
		this.create = true;
	}

	MCWorldType(boolean create) {
		this.create = create;
	}

	public boolean canCreate() {
		return create;
	}
}
