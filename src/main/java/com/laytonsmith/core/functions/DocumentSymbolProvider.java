package com.laytonsmith.core.functions;

import com.laytonsmith.core.ParseTree;
import java.util.List;
import org.eclipse.lsp4j.SymbolKind;

/**
 * A function that implements DocumentSymbolProvider indicates that it is an item that is an important top level item.
 * This is used by the language server to display a link to location in the Outline View.
 */
public interface DocumentSymbolProvider {
	/**
	 * Returns the name of the symbol that should be displayed in the outline view.
	 * @param children The children being passed to this instance of the function.
	 * @return The name of the symbol, based on the arguments. May be null, indicating that this
	 * item should be skipped entirely.
	 */
	String symbolDisplayName(List<ParseTree> children);

	/**
	 * Returns the type of this symbol. This generally decides what type of icon is used in the UI.
	 * @return
	 */
	SymbolKind getSymbolKind();
}
