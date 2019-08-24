package com.laytonsmith.tools.langserv;

/**
 * Text document specific client capabilities.
 * <p>
 * TextDocumentClientCapabilities define capabilities the editor / tool provides on text documents.
 */
public class TextDocumentClientCapabilities {

	public static class Synchronization {
		private boolean dynamicRegistration;
		/**
		 * Whether text document synchronization supports dynamic registration.
		 * @return
		 */
		public boolean getDynamicRegistration() {
			return dynamicRegistration;
		}

		private boolean willSave;
		/**
		 * The client supports sending will save notifications.
		 * @return
		 */
		public boolean getWillSave() {
			return willSave;
		}

		private boolean willSaveWaitUntil;
		/**
		 * The client supports sending a will save request and
		 * waits for a response providing text edits which will
		 * be applied to the document before it is saved.
		 * @return
		 */
		public boolean getWillSaveWaitUntil() {
			return willSaveWaitUntil;
		}

		private boolean didSave;
		/**
		 * The client supports did save notifications.
		 * @return
		 */
		public boolean getDidSave() {
			return didSave;
		}

	}

	private Synchronization synchronization;

	public Synchronization getSynchronization() {
		return synchronization;
	}

	public static class CompletionItem {

		private boolean snippetSupport;
		/**
		 * The client supports snippets as insert text.
		 *
		 * A snippet can define tab stops and placeholders with `$1`, `$2`
		 * and `${3:foo}`. `$0` defines the final tab stop, it defaults to
		 * the end of the snippet. Placeholders with equal identifiers are linked,
		 * that is typing in one will update others too.
		 * @return
		 */
		public boolean getSnippetSupport() {
			return snippetSupport;
		}

		private boolean commitCharactersSupport;
		/**
		 * The client supports commit characters on a completion item.
		 * @return
		 */
		public boolean getCommitCharactersSupport() {
			return commitCharactersSupport;
		}

		private MarkupKind[] documentationFormat;
		/**
		 * The client supports the following content formats for the documentation
		 * property. The order describes the preferred format of the client.
		 * @return
		 */
		public MarkupKind[] getDocumentationFormat() {
			return documentationFormat;
		}

		private boolean deprecatedSupport;
		/**
		 * The client supports the deprecated property on a completion item.
		 * @return
		 */
		public boolean getDeprecatedSupport() {
			return deprecatedSupport;
		}

		private boolean preselectSupport;
		/**
		 * The client supports the preselect property on a completion item.
		 * @return
		 */
		public boolean getPreselectSupport() {
			return preselectSupport;
		}
	}

	public static class CompletionItemKindSet {
		private CompletionItemKind[] valueSet;
		/**
		 * The completion item kind values the client supports. When this
		 * property exists the client also guarantees that it will
		 * handle values outside its set gracefully and falls back
		 * to a default value when unknown.
		 *
		 * If this property is not present the client only supports
		 * the completion items kinds from `Text` to `Reference` as defined in
		 * the initial version of the protocol.
		 * @return
		 */
		public CompletionItemKind[] getValueSet() {
			return valueSet;
		}

	}

	public static class Completion {

		private boolean dynamicRegistration;
		/**
		 * Whether completion supports dynamic registration.
		 * @return
		 */
		public boolean getDynamicRegistration() {
			return dynamicRegistration;
		}

		private CompletionItem completionItem;
		/**
		 * The client supports the following `CompletionItem` specific
		 * capabilities.
		 * @return
		 */
		public CompletionItem getCompletionItem() {
			return completionItem;
		}

		private CompletionItemKindSet completionItemKind;
		public CompletionItemKindSet getCompletionItemKind() {
			return completionItemKind;
		}

		private boolean contextSupport;
		/**
		 * The client supports to send additional context information for a
		 * `textDocument/completion` request.
		 * @return
		 */
		public boolean getContextSupport() {
			return contextSupport;
		}
	}

	private Completion completion;
	/**
	 * Capabilities specific to the `textDocument/completion`
	 * @return
	 */
	public Completion getCompletion() {
		return completion;
	}

	public static class Hover {

		private boolean dynamicRegistration;
		/**
		 * Whether hover supports dynamic registration.
		 * @return
		 */
		public boolean getDynamicRegistration() {
			return dynamicRegistration;
		}

		private MarkupKind[] contentFormat;
		/**
		 * The client supports the follow content formats for the content
		 * property. The order describes the preferred format of the client.
		 * @return
		 */
		public MarkupKind[] getContentFormat() {
			return contentFormat;
		}
	}

	private Hover hover;
	/**
	 * Capabilities specific to the `textDocument/hover`
	 * @return
	 */
	public Hover getHover() {
		return hover;
	}

	public static class ParameterInformation {
		private boolean labelOffsetSupport;
		/**
		 * The client supports processing label offsets instead of a
		 * simple label string.
		 *
		 * Since 3.14.0
		 * @return
		 */
		public boolean getLabelOffsetSupport() {
			return labelOffsetSupport;
		}

	}

	public static class SignatureInformation {

		private MarkupKind[] documentationFormat;
		/**
		 * The client supports the follow content formats for the documentation
		 * property. The order describes the preferred format of the client.
		 * @return
		 */
		public MarkupKind[] getDocumentationFormat() {
			return documentationFormat;
		}

		private ParameterInformation parameterInformation;
		/**
		 * Client capabilities specific to parameter information.
		 * @return
		 */
		public ParameterInformation getParameterInformation() {
			return parameterInformation;
		}
	}

	public static class SignatureHelp {

		private boolean dynamicRegistration;
		/**
		 * Whether signature help supports dynamic registration.
		 * @return
		 */
		public boolean getDynamicRegistration() {
			return dynamicRegistration;
		}

		private SignatureInformation signatureInformation;
		/**
		 * The client supports the following `SignatureInformation`
		 * specific properties.
		 * @return
		 */
		public SignatureInformation getSignatureInformation() {
			return signatureInformation;
		}
	}

	private SignatureHelp signatureHelp;
	/**
	 * Capabilities specific to the `textDocument/signatureHelp`
	 * @return
	 */
	public SignatureHelp getSignatureHelp() {
		return signatureHelp;
	}

	public static class References {

		private boolean dynamicRegistration;
		/**
		 * Whether references supports dynamic registration.
		 * @return
		 */
		public boolean getDynamicRegistration() {
			return dynamicRegistration;
		}
	}

	private References references;

	/**
	 * Capabilities specific to the `textDocument/references`
	 * @return
	 */
	public References getReferences() {
		return references;
	}

	public static class DocumentHighlight {

		private boolean dynamicRegistration;
		/**
		 * Whether document highlight supports dynamic registration.
		 * @return
		 */
		public boolean getDynamicRegistration() {
			return dynamicRegistration;
		}
	}

	private DocumentHighlight documentHighlight;

	/**
	 * Capabilities specific to the `textDocument/documentHighlight`
	 * @return
	 */
	public DocumentHighlight getDocumentHighlight() {
		return documentHighlight;
	}


//	/**
//	 * Capabilities specific to the `textDocument/documentSymbol`
//	 */
//	documentSymbol?: {
//		/**
//		 * Whether document symbol supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//
//		/**
//		 * Specific capabilities for the `SymbolKind`.
//		 */
//		symbolKind?: {
//			/**
//			 * The symbol kind values the client supports. When this
//			 * property exists the client also guarantees that it will
//			 * handle values outside its set gracefully and falls back
//			 * to a default value when unknown.
//			 *
//			 * If this property is not present the client only supports
//			 * the symbol kinds from `File` to `Array` as defined in
//			 * the initial version of the protocol.
//			 */
//			valueSet?: SymbolKind[];
//		}
//
//		/**
//		 * The client supports hierarchical document symbols.
//		 */
//		hierarchicalDocumentSymbolSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/formatting`
//	 */
//	formatting?: {
//		/**
//		 * Whether formatting supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/rangeFormatting`
//	 */
//	rangeFormatting?: {
//		/**
//		 * Whether range formatting supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/onTypeFormatting`
//	 */
//	onTypeFormatting?: {
//		/**
//		 * Whether on type formatting supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//	};
//
//	/**
//		* Capabilities specific to the `textDocument/declaration`
//		*/
//	declaration?: {
//		/**
//		 * Whether declaration supports dynamic registration. If this is set to `true`
//		 * the client supports the new `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
//		 * return value for the corresponding server capability as well.
//		 */
//		dynamicRegistration?: boolean;
//
//		/**
//		 * The client supports additional metadata in the form of declaration links.
//		 *
//		 * Since 3.14.0
//		 */
//		linkSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/definition`.
//	 *
//	 * Since 3.14.0
//	 */
//	definition?: {
//		/**
//		 * Whether definition supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//
//		/**
//		 * The client supports additional metadata in the form of definition links.
//		 */
//		linkSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/typeDefinition`
//	 *
//	 * Since 3.6.0
//	 */
//	typeDefinition?: {
//		/**
//		 * Whether typeDefinition supports dynamic registration. If this is set to `true`
//		 * the client supports the new `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
//		 * return value for the corresponding server capability as well.
//		 */
//		dynamicRegistration?: boolean;
//
//		/**
//		 * The client supports additional metadata in the form of definition links.
//		 *
//		 * Since 3.14.0
//		 */
//		linkSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/implementation`.
//	 *
//	 * Since 3.6.0
//	 */
//	implementation?: {
//		/**
//		 * Whether implementation supports dynamic registration. If this is set to `true`
//		 * the client supports the new `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
//		 * return value for the corresponding server capability as well.
//		 */
//		dynamicRegistration?: boolean;
//
//		/**
//		 * The client supports additional metadata in the form of definition links.
//		 *
//		 * Since 3.14.0
//		 */
//		linkSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/codeAction`
//	 */
//	codeAction?: {
//		/**
//		 * Whether code action supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//		/**
//		 * The client support code action literals as a valid
//		 * response of the `textDocument/codeAction` request.
//		 *
//		 * Since 3.8.0
//		 */
//		codeActionLiteralSupport?: {
//			/**
//			 * The code action kind is support with the following value
//			 * set.
//			 */
//			codeActionKind: {
//
//				/**
//				 * The code action kind values the client supports. When this
//				 * property exists the client also guarantees that it will
//				 * handle values outside its set gracefully and falls back
//				 * to a default value when unknown.
//				 */
//				valueSet: CodeActionKind[];
//			};
//		};
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/codeLens`
//	 */
//	codeLens?: {
//		/**
//		 * Whether code lens supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/documentLink`
//	 */
//	documentLink?: {
//		/**
//		 * Whether document link supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/documentColor` and the
//	 * `textDocument/colorPresentation` request.
//	 *
//	 * Since 3.6.0
//	 */
//	colorProvider?: {
//		/**
//		 * Whether colorProvider supports dynamic registration. If this is set to `true`
//		 * the client supports the new `(ColorProviderOptions & TextDocumentRegistrationOptions & StaticRegistrationOptions)`
//		 * return value for the corresponding server capability as well.
//		 */
//		dynamicRegistration?: boolean;
//	}
//
//	/**
//	 * Capabilities specific to the `textDocument/rename`
//	 */
//	rename?: {
//		/**
//		 * Whether rename supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//		/**
//		 * The client supports testing for validity of rename operations
//		 * before execution.
//		 */
//		prepareSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to `textDocument/publishDiagnostics`.
//	 */
//	publishDiagnostics?: {
//		/**
//		 * Whether the clients accepts diagnostics with related information.
//		 */
//		relatedInformation?: boolean;
//	};
//	/**
//	 * Capabilities specific to `textDocument/foldingRange` requests.
//	 *
//	 * Since 3.10.0
//	 */
//	foldingRange?: {
//		/**
//		 * Whether implementation supports dynamic registration for folding range providers. If this is set to `true`
//		 * the client supports the new `(FoldingRangeProviderOptions & TextDocumentRegistrationOptions & StaticRegistrationOptions)`
//		 * return value for the corresponding server capability as well.
//		 */
//		dynamicRegistration?: boolean;
//		/**
//		 * The maximum number of folding ranges that the client prefers to receive per document. The value serves as a
//		 * hint, servers are free to follow the limit.
//		 */
//		rangeLimit?: number;
//		/**
//		 * If set, the client signals that it only supports folding complete lines. If set, client will
//		 * ignore specified `startCharacter` and `endCharacter` properties in a FoldingRange.
//		 */
//		lineFoldingOnly?: boolean;
//	};get
	/**
//	 * Capabilities specific to the `textDocument/documentSymbol`
//	 */
//	documentSymbol?: {
//		/**
//		 * Whether document symbol supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//
//		/**
//		 * Specific capabilities for the `SymbolKind`.
//		 */
//		symbolKind?: {
//			/**
//			 * The symbol kind values the client supports. When this
//			 * property exists the client also guarantees that it will
//			 * handle values outside its set gracefully and falls back
//			 * to a default value when unknown.
//			 *
//			 * If this property is not present the client only supports
//			 * the symbol kinds from `File` to `Array` as defined in
//			 * the initial version of the protocol.
//			 */
//			valueSet?: SymbolKind[];
//		}
//
//		/**
//		 * The client supports hierarchical document symbols.
//		 */
//		hierarchicalDocumentSymbolSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/formatting`
//	 */
//	formatting?: {
//		/**
//		 * Whether formatting supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/rangeFormatting`
//	 */
//	rangeFormatting?: {
//		/**
//		 * Whether range formatting supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/onTypeFormatting`
//	 */
//	onTypeFormatting?: {
//		/**
//		 * Whether on type formatting supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//	};
//
//	/**
//		* Capabilities specific to the `textDocument/declaration`
//		*/
//	declaration?: {
//		/**
//		 * Whether declaration supports dynamic registration. If this is set to `true`
//		 * the client supports the new `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
//		 * return value for the corresponding server capability as well.
//		 */
//		dynamicRegistration?: boolean;
//
//		/**
//		 * The client supports additional metadata in the form of declaration links.
//		 *
//		 * Since 3.14.0
//		 */
//		linkSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/definition`.
//	 *
//	 * Since 3.14.0
//	 */
//	definition?: {
//		/**
//		 * Whether definition supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//
//		/**
//		 * The client supports additional metadata in the form of definition links.
//		 */
//		linkSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/typeDefinition`
//	 *
//	 * Since 3.6.0
//	 */
//	typeDefinition?: {
//		/**
//		 * Whether typeDefinition supports dynamic registration. If this is set to `true`
//		 * the client supports the new `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
//		 * return value for the corresponding server capability as well.
//		 */
//		dynamicRegistration?: boolean;
//
//		/**
//		 * The client supports additional metadata in the form of definition links.
//		 *
//		 * Since 3.14.0
//		 */
//		linkSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/implementation`.
//	 *
//	 * Since 3.6.0
//	 */
//	implementation?: {
//		/**
//		 * Whether implementation supports dynamic registration. If this is set to `true`
//		 * the client supports the new `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
//		 * return value for the corresponding server capability as well.
//		 */
//		dynamicRegistration?: boolean;
//
//		/**
//		 * The client supports additional metadata in the form of definition links.
//		 *
//		 * Since 3.14.0
//		 */
//		linkSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/codeAction`
//	 */
//	codeAction?: {
//		/**
//		 * Whether code action supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//		/**
//		 * The client support code action literals as a valid
//		 * response of the `textDocument/codeAction` request.
//		 *
//		 * Since 3.8.0
//		 */
//		codeActionLiteralSupport?: {
//			/**
//			 * The code action kind is support with the following value
//			 * set.
//			 */
//			codeActionKind: {
//
//				/**
//				 * The code action kind values the client supports. When this
//				 * property exists the client also guarantees that it will
//				 * handle values outside its set gracefully and falls back
//				 * to a default value when unknown.
//				 */
//				valueSet: CodeActionKind[];
//			};
//		};
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/codeLens`
//	 */
//	codeLens?: {
//		/**
//		 * Whether code lens supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/documentLink`
//	 */
//	documentLink?: {
//		/**
//		 * Whether document link supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to the `textDocument/documentColor` and the
//	 * `textDocument/colorPresentation` request.
//	 *
//	 * Since 3.6.0
//	 */
//	colorProvider?: {
//		/**
//		 * Whether colorProvider supports dynamic registration. If this is set to `true`
//		 * the client supports the new `(ColorProviderOptions & TextDocumentRegistrationOptions & StaticRegistrationOptions)`
//		 * return value for the corresponding server capability as well.
//		 */
//		dynamicRegistration?: boolean;
//	}
//
//	/**
//	 * Capabilities specific to the `textDocument/rename`
//	 */
//	rename?: {
//		/**
//		 * Whether rename supports dynamic registration.
//		 */
//		dynamicRegistration?: boolean;
//		/**
//		 * The client supports testing for validity of rename operations
//		 * before execution.
//		 */
//		prepareSupport?: boolean;
//	};
//
//	/**
//	 * Capabilities specific to `textDocument/publishDiagnostics`.
//	 */
//	publishDiagnostics?: {
//		/**
//		 * Whether the clients accepts diagnostics with related information.
//		 */
//		relatedInformation?: boolean;
//	};
//	/**
//	 * Capabilities specific to `textDocument/foldingRange` requests.
//	 *
//	 * Since 3.10.0
//	 */
//	foldingRange?: {
//		/**
//		 * Whether implementation supports dynamic registration for folding range providers. If this is set to `true`
//		 * the client supports the new `(FoldingRangeProviderOptions & TextDocumentRegistrationOptions & StaticRegistrationOptions)`
//		 * return value for the corresponding server capability as well.
//		 */
//		dynamicRegistration?: boolean;
//		/**
//		 * The maximum number of folding ranges that the client prefers to receive per document. The value serves as a
//		 * hint, servers are free to follow the limit.
//		 */
//		rangeLimit?: number;
//		/**
//		 * If set, the client signals that it only supports folding complete lines. If set, client will
//		 * ignore specified `startCharacter` and `endCharacter` properties in a FoldingRange.
//		 */
//		lineFoldingOnly?: boolean;
//	};

}
