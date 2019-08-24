package com.laytonsmith.tools.langserv;

/**
 * A general message as defined by JSON-RPC. The language server protocol always uses “2.0” as the jsonrpc version.
 */
public interface Message {
	/**
	 * Should always be "2.0"
	 */
	String getJsonrpc();
}
