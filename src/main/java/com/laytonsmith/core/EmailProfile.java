package com.laytonsmith.core;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Profiles.ProfileType(type = "email")
public class EmailProfile extends Profiles.Profile {

	private String host = "localhost";
	private String user = null;
	private String password = null;
	private int port = 587;
	private boolean use_ssl = false;
	private boolean use_start_tls = false;
	private int timeout = 10000;

	public EmailProfile(String id, Map<String, String> elements) throws Profiles.InvalidProfileException {
		super(id);
		if(elements.containsKey("host")) {
			host = elements.get("host");
		}
		if(elements.containsKey("user")) {
			user = elements.get("user");
		}
		if(elements.containsKey("password")) {
			password = elements.get("password");
		}
		if(elements.containsKey("port")) {
			try {
				port = Integer.parseInt(elements.get("port"));
			} catch(NumberFormatException ex) {
				throw new Profiles.InvalidProfileException(ex.getMessage());
			}
		}
		if(elements.containsKey("use_ssl")) {
			use_ssl = Boolean.parseBoolean(elements.get("use_ssl"));
		}
		if(elements.containsKey("use_start_tls")) {
			use_start_tls = Boolean.parseBoolean(elements.get("use_start_tls"));
		}
		if(elements.containsKey("timeout")) {
			try {
				timeout = Integer.parseInt(elements.get("timeout"));
			} catch(NumberFormatException ex) {
				throw new Profiles.InvalidProfileException(ex.getMessage());
			}
		}
	}

	public String getHost() {
		return host;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	public boolean getUseSSL() {
		return use_ssl;
	}

	public boolean getUseStartTLS() {
		return use_start_tls;
	}

	public int getTimeout() {
		return timeout;
	}

	public Map<String, Object> getMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("host", getHost());
		map.put("user", getUser());
		map.put("password", getPassword());
		map.put("port", getPort());
		map.put("use_ssl", getUseSSL());
		map.put("use_start_tls", getUseStartTLS());
		map.put("timeout", getTimeout());
		return map;
	}

	@Override
	public String toString() {
		return super.toString() + " " + user + "@" + host + ":" + port;
	}

}
