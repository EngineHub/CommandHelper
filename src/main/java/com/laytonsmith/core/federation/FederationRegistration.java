package com.laytonsmith.core.federation;

import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * A FederationRegistration represents a listening connection. It is serializable to JSON, which can be easily stored in
 * many mediums.
 */
public final class FederationRegistration {

	private final String serverName;
	private int port;
	private long lastUpdated;

	/**
	 * Creates a new registration.
	 *
	 * @param serverName The name of the server this connection is registered under.
	 */
	public FederationRegistration(String serverName) {
		this.serverName = serverName;
		this.lastUpdated = System.currentTimeMillis();
	}

	/**
	 * Gets the server name.
	 *
	 * @return
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * Returns the unix time this connection was last updated. The connection is updated if the heartbeat thread for
	 * this registration is still active.
	 *
	 * @return
	 */
	public long getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * The heartbeat thread should call this while the connection is still alive. {@link #getLastUpdated()} will then
	 * return the current unix time.
	 */
	public void updateLastUpdated() {
		this.lastUpdated = System.currentTimeMillis();
	}

	/**
	 * Gets the port this connection is registered to.
	 *
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns true if this registration was updated {@code time} ms ago, or sooner.
	 *
	 * @param time
	 * @return
	 */
	public boolean updatedSince(long time) {
		return lastUpdated > (System.currentTimeMillis() - time);
	}

	/**
	 * Sets the port this connection is registered on.
	 *
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns a json string, which can later be used to reconstruct this object using
	 * {@link #fromJSON(java.lang.String)}.
	 *
	 * @return The JSON string.
	 */
	public String toJSON() {
		Map<String, String> values = new HashMap<>();
		values.put("server_name", serverName);
		values.put("port", Integer.toString(port));
		values.put("last_updated", Long.toString(System.currentTimeMillis()));
		return JSONObject.toJSONString(values);
	}

	/**
	 * Creates a new FederationRegistration object given the representing json, which was created via the
	 * {@link #toJSON()} method.
	 *
	 * @param json
	 * @return
	 */
	public static FederationRegistration fromJSON(String json) {
		Map<String, String> map = (Map<String, String>) JSONValue.parse(json);
		String serverName = map.get("server_name");
		int port = Integer.parseInt(map.get("port"));
		long lastUpdated = Long.parseLong(map.get("last_updated"));
		FederationRegistration reg = new FederationRegistration(serverName);
		reg.setPort(port);
		reg.lastUpdated = lastUpdated;
		return reg;
	}
}
