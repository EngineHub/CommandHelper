package com.laytonsmith.tools.langserv;

/**
 * Workspace specific client capabilities. WorkspaceClientCapabilities define capabilities the editor / tool provides
 * on the workspace
 */
public class WorkspaceClientCapabilities {
	private boolean applyEdit;

	/**
	 * The client supports applying batch edits to the workspace by supporting
	 * the request 'workspace/applyEdit'
	 * @return
	 */
	public boolean getApplyEdit() {
		return applyEdit;
	}

	public static class WorkspaceEdit {
		private boolean documentChanges;

		/**
		 * The client supports versioned document changes in `WorkspaceEdit`s
		 * @return
		 */
		public boolean getDocumentChanges() {
			return documentChanges;
		}

		private ResourceOperationKind[] resourceOperations;
		/**
		 * The resource operations the client supports. Clients should at least
		 * support 'create', 'rename' and 'delete' files and folders.
		 * @return
		 */
		public ResourceOperationKind[] getResourceOperations() {
			return resourceOperations;
		}

		private FailureHandlingKind failureHandling;
		/**
		 * The failure handling strategy of a client if applying the workspace edit
		 * fails.
		 * @return
		 */
		public FailureHandlingKind getFailureHandling() {
			return failureHandling;
		}
	}

	private WorkspaceEdit workspaceEdit;
	/**
	 * Capabilities specific to `WorkspaceEdit`s
	 * @return
	 */
	public WorkspaceEdit getWorkspaceEdit() {
		return workspaceEdit;
	}

	public static class DidChangeConfiguration {
		private boolean dynamicRegistration;

		/**
		 * Did change configuration notification supports dynamic registration.
		 * @return
		 */
		public boolean getDynamicRegistration() {
			return dynamicRegistration;
		}
	}

	private DidChangeConfiguration didChangeConfiguration;
	/**
	 * Capabilities specific to the `workspace/didChangeConfiguration` notification.
	 * @return
	 */
	public DidChangeConfiguration getDidChangeConfiguration() {
		return didChangeConfiguration;
	}

	public static class DidChangeWatchedFiles {
		private boolean dynamicRegistration;
		/**
		 * Did change watched files notification supports dynamic registration. Please note
		 * that the current protocol doesn't support static configuration for file changes
		 * from the server side.
		 * @return
		 */
		public boolean getDynamicRegistration() {
			return dynamicRegistration;
		}

	}

	private DidChangeWatchedFiles didChangeWatchedFiles;

	/**
	 * Capabilities specific to the `workspace/didChangeWatchedFiles` notification.
	 * @return
	 */
	public DidChangeWatchedFiles getDidChangeWatchedFiles() {
		return didChangeWatchedFiles;
	}

	public static class SymbolKindSet {
		private SymbolKind[] valueSet;
		/**
		 * The symbol kind values the client supports. When this
		 * property exists the client also guarantees that it will
		 * handle values outside its set gracefully and falls back
		 * to a default value when unknown.
		 *
		 * If this property is not present the client only supports
		 * the symbol kinds from `File` to `Array` as defined in
		 * the initial version of the protocol.
		 * @return
		 */
		public SymbolKind[] getValueSet() {
			return valueSet;
		}
	}

	public static class Symbol {
		private boolean dynamicRegistration;
		/**
		 * Symbol request supports dynamic registration.
		 * @return
		 */
		public boolean getDynamicRegistration() {
			return dynamicRegistration;
		}

		private SymbolKindSet symbolKind;
		/**
		 * Specific capabilities for the `SymbolKind` in the `workspace/symbol` request.
		 * @return
		 */
		public SymbolKindSet getSymbolKind() {
			return symbolKind;
		}

	}

	private Symbol symbol;

	/**
	 * Capabilities specific to the `workspace/symbol` request.
	 * @return
	 */
	public Symbol getSymbol() {
		return symbol;
	}

	public static class ExecuteCommand {

		private boolean dynamicRegistration;

		/**
		 * Execute command supports dynamic registration.
		 * @return
		 */
		public boolean getDynamicregistration() {
			return dynamicRegistration;
		}
	}

	private ExecuteCommand executeCommand;
	/**
	 * Capabilities specific to the `workspace/executeCommand` request.
	 * @return
	 */
	public ExecuteCommand getExecuteCommand() {
		return executeCommand;
	}

	private boolean workspaceFolders;
	/**
	 * The client has support for workspace folders.
	 *
	 * Since 3.6.0
	 * @return
	 */
	public boolean getWorkspaceFolders() {
		return workspaceFolders;
	}

	private boolean configuration;
	/**
	 * The client supports `workspace/configuration` requests.
	 *
	 * Since 3.6.0
	 */
	public boolean getConfiguration() {
		return configuration;
	}

}
