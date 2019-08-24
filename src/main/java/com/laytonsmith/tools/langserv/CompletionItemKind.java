package com.laytonsmith.tools.langserv;

import com.laytonsmith.PureUtilities.JSONUtil;

/**
 *
 */
public enum CompletionItemKind implements JSONUtil.CustomLongEnum<CompletionItemKind> {
	Text(1),
	Method(2),
	Function(3),
	Constructor(4),
	Field(5),
	Variable(6),
	Class(7),
	Interface(8),
	Module(9),
	Property(10),
	Unit(11),
	Value(12),
	Enum(13),
	Keyword(14),
	Snippet(15),
	Color(16),
	File(17),
	Reference(18),
	Folder(19),
	EnumMember(20),
	Constant(21),
	Struct(22),
	Event(23),
	Operator(24),
	TypeParameter(25);

	private final long id;

	private CompletionItemKind(long id) {
		this.id = id;
	}

	@Override
	public CompletionItemKind getFromValue(Long value) {
		for(CompletionItemKind i : values()) {
			if(i.id == value) {
				return i;
			}
		}
		return null;
	}

	@Override
	public Long getValue() {
		return id;
	}
}
