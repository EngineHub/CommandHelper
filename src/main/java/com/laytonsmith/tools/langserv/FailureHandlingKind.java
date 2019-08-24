package com.laytonsmith.tools.langserv;

import com.laytonsmith.PureUtilities.JSONUtil;

/**
 *
 */
public enum FailureHandlingKind implements JSONUtil.CustomStringEnum<FailureHandlingKind> {
	/**
	 * Applying the workspace change is simply aborted if one of the changes provided
	 * fails. All operations executed before the failing operation stay executed.
	 */
	Abort("abort"),

	/**
	 * All operations are executed transactionally. That means they either all
	 * succeed or no changes at all are applied to the workspace.
	 */
	Transactional("transactional"),


	/**
	 * If the workspace edit contains only textual file changes they are executed transactionally.
	 * If resource changes (create, rename or delete file) are part of the change the failure
	 * handling strategy is abort.
	 */
	TextOnlyTransactionald("textOnlyTransactional"),

	/**
	 * The client tries to undo the operations already executed. But there is no
	 * guarantee that this succeeds.
	 */
	Undo("undo");

	private final String id;

	private FailureHandlingKind(String id) {
		this.id = id;
	}

	@Override
	public FailureHandlingKind getFromValue(String value) {
		for(FailureHandlingKind i : values()) {
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
