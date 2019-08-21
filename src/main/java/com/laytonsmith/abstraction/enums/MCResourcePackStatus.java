package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.ResourcePackStatus")
public enum MCResourcePackStatus {
	ACCEPTED,
	DECLINED,
	FAILED_DOWNLOAD,
	SUCCESSFULLY_LOADED
}
