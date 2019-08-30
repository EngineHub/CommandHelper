package com.laytonsmith.core.functions;

import com.laytonsmith.core.ParseTree;
import java.util.List;
import java.util.Set;

/**
 * A function that implements DocumentLinkProvider indicates that it has one or more parameters that, if a constant
 * string, links to a file that could or must be in the local file system. This is used by the language server to
 * display a link to the file.
 */
public interface DocumentLinkProvider {

	/**
	 * Given the children, returns the ones that are potentially links to files. The caller is responsible for
	 * determining if the nodes are constant, and if they actually exist in the file system.
	 * @param children
	 * @return
	 */
	Set<ParseTree> getDocumentLinks(List<ParseTree> children);
}
