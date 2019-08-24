package com.laytonsmith.tools.langserv;

/**
 * Many tools support more than one root folder per workspace. Examples for this are VS Code’s multi-root support,
 * Atom’s project folder support or Sublime’s project support. If a client workspace consists of multiple roots then a
 * server typically needs to know about this. The protocol up to now assumes one root folder which is announced to the
 * server by the rootUri property of the InitializeParams. If the client supports workspace folders and announces them
 * via the corresponding workspaceFolders client capability, the InitializeParams contain an additional property
 * workspaceFolders with the configured workspace folders when the server starts.
 *
 * The workspace/workspaceFolders request is sent from the server to the client to fetch the current open list of
 * workspace folders. Returns null in the response if only a single file is open in the tool. Returns an empty array if
 * a workspace is open but no folders are configured.
 */
public class WorkspaceFolder {
	private String uri;
	/**
	 * The associated URI for this workspace folder.
	 * @return
	 */
	public String getUri() {
		return uri;
	}

	private String name;
	/**
	 * The name of the workspace folder. Used to refer to this
	 * workspace folder in the user interface.
	 * @return
	 */
	public String getName() {
		return name;
	}
}
