package com.laytonsmith.tools.langserv;

import com.laytonsmith.PureUtilities.JSONUtil;

/**
 *
 */
public class InitializeParams  {

	private int processId;

	/**
	 * The process Id of the parent process that started
	 * the server. Is null if the process has not been started by another process.
	 * If the parent process is not alive then the server should exit (see exit notification) its process.
	 * @return
	 */
	public int getProcessId() {
		return processId;
	}

	private String rootPath;

	/**
	 * The rootPath of the workspace. Is null
	 * if no folder is open.
	 *
	 * @return
	 * @deprecated in favour of rootUri.
	 */
	@Deprecated
	public String getRootPath() {
		return rootPath;
	}

	private String rootUri;
	/**
	 * The rootUri of the workspace. Is null if no
	 * folder is open. If both `rootPath` and `rootUri` are set
	 * `rootUri` wins.
	 * @return
	 */
	public String getRootUri() {
		return rootUri;
	}

	/**
	 * User provided initialization options.
	 */
//	initializationOptions?: any;

	private ClientCapabilities capabilities;
	/**
	 * The capabilities provided by the client (editor or tool)
	 * @return
	 */
	public ClientCapabilities getCapabilities() {
		return capabilities;
	}

	public static enum Trace implements JSONUtil.CustomStringEnum<Trace> {
		off, messages, verbose;

		@Override
		public Trace getFromValue(String value) {
			if(value == null) {
				return off;
			}
			return Trace.valueOf(value);
		}

		@Override
		public String getValue() {
			return name();
		}


	}

	private Trace trace;
	/**
	 * The initial trace setting. If omitted trace is disabled ('off').
	 * @return
	 */
	public Trace getTrace() {
		return trace;
	}

	private WorkspaceFolder[] workspaceFolders;
	/**
	 * The workspace folders configured in the client when the server starts.
	 * This property is only available if the client supports workspace folders.
	 * It can be `null` if the client supports workspace folders but none are
	 * configured.
	 *
	 * Since 3.6.0
	 * @return
	 */
	public WorkspaceFolder[] getWorkspaceFolders() {
		return workspaceFolders;
	}

}
