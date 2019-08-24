package com.laytonsmith.tools.langserv;

import com.laytonsmith.PureUtilities.JSONUtil;

/**
 *
 */
public enum ResourceOperationKind implements JSONUtil.CustomStringEnum<ResourceOperationKind> {
	/**
	 * Supports creating new files and folders.
	 */
	Create("create"),

	/**
	 * Supports renaming existing files and folders.
	 */
	Rename("rename"),

	/**
	 * Supports deleting existing files and folders.
	 */
	Delete("delete");

	private final String id;

	private ResourceOperationKind(String id) {
		this.id = id;
	}

	@Override
	public ResourceOperationKind getFromValue(String value) {
		for(ResourceOperationKind i : values()) {
			if(i.id.equals(value)) {
				return i;
			}
		}
		return null;
	}

	@Override
	public String getValue() {
		return id;
	}


}
