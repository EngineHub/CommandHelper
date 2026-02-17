package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.ResourcePackStatus")
public enum MCResourcePackStatus {
	ACCEPTED,
	DECLINED,
	DISCARDED,
	DOWNLOADED,
	FAILED_DOWNLOAD,
	FAILED_RELOAD,
	INVALID_URL,
	SUCCESSFULLY_LOADED
}
