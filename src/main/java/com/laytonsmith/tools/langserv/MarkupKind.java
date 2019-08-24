package com.laytonsmith.tools.langserv;

import com.laytonsmith.PureUtilities.JSONUtil;

/**
 *
 */
public enum MarkupKind implements JSONUtil.CustomStringEnum<MarkupKind> {
	/**
	 * Plain text is supported as a content format
	 */
	PlainText("plaintext"),

	/**
	 * Markdown is supported as a content format
	 */
	Markdown("markdown");

	private final String id;

	private MarkupKind(String id) {
		this.id = id;
	}

	@Override
	public MarkupKind getFromValue(String value) {
		for(MarkupKind i : values()) {
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
