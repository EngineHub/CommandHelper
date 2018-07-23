package com.laytonsmith.PureUtilities.Web;

import com.laytonsmith.PureUtilities.PublicSuffix;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class wraps a single HTTP cookie.
 */
public final class Cookie implements Comparable<Cookie> {

	private String name;
	private String value;
	private String domain;
	private String path;
	private long expiration = 0;
	private boolean httpOnly = false;
	private boolean secureOnly = false;

	/**
	 * Given an unparsed value, this adds a new cookie to this cookie list after parsing the string into a proper
	 * cookie, and setting the fields appropriately.
	 *
	 * @param unparsedValue
	 * @param currentURL
	 */
	public Cookie(String unparsedValue, URL currentURL) {
		//Split on ;
		String[] parts = unparsedValue.split(";");
		for(int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if(i == 0) {
				//This is the actual cookie value
				String[] nameVal = part.split("=", 2);
				name = nameVal[0].trim();
				value = nameVal[1].trim();
				continue;
			}
			//The rest of the fields are standard fields
			String[] keyval = part.split("=", 2);
			String key = keyval[0].trim().toLowerCase();
			String val = null;
			if(keyval.length >= 2) {
				val = keyval[1].trim();
			}
			if("expires".equals(key)) {
				DateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy kk:mm:ss zzz");
				try {
					expiration = formatter.parse(val).getTime();
				} catch (ParseException ex) {
					Logger.getLogger(WebUtility.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else if("path".equals(key)) {
				path = val;
			} else if("domain".equals(key)) {
				//Finish the PublicSuffix stuff to validate this domain.
				if(PublicSuffix.get().getEffectiveTLDLength(val) != -1) {
					domain = val;
				} else {
					Logger.getLogger(WebUtility.class.getName()).log(Level.SEVERE, "Possible attack cookie being set from " + currentURL + ". Attempted" + " to set " + val + " as the domain.");
				}
			} else if("httponly".equals(key)) {
				httpOnly = true;
			} else if("secureonly".equals(key)) {
				secureOnly = true;
			}
		}
		if(domain == null) {
			domain = currentURL.getHost();
		}
		if(path == null) {
			path = currentURL.getPath();
		}
	}

	/**
	 * Creates a cookie with only the required parameters set. That is, it creates a session cookie with httpOnly and
	 * secure set to false.
	 *
	 * @param domain The domain under which this cookie applies
	 * @param name The name of this cookie
	 * @param value The value of this cookie
	 * @param path The path under which this cookie applies in the domain
	 */
	public Cookie(String name, String value, String domain, String path) {
		this(name, value, domain, path, 0, false, false);
	}

	/**
	 * Creates a cookie with all of the various parameters set.
	 *
	 * @param domain The domain under which this cookie applies
	 * @param name The name of this cookie
	 * @param value The value of this cookie
	 * @param path The path under which this cookie applies in the domain
	 * @param expiration Sets the expiration date of the cookie. 0 indicates a session cookie.
	 * @param httpOnly Sets whether or not this cookie is httpOnly. Generally, this is an unused field
	 * @param secureOnly Sets whether or not this cookie should only be send via https.
	 */
	public Cookie(String name, String value, String domain, String path, long expiration, boolean httpOnly, boolean secureOnly) {
		this.name = name;
		this.value = value;
		this.domain = domain;
		this.path = path;
		this.expiration = expiration;
		this.httpOnly = httpOnly;
		this.secureOnly = secureOnly;
	}

	@Override
	public int compareTo(Cookie o) {
		return (this.domain + this.name + this.path).compareTo(o.domain + o.name + o.path);
	}

	/**
	 * Returns the domain of this cookie.
	 *
	 * @return
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Returns the name of this cookie.
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the value of this cookie.
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the path under which this cookie applies.
	 *
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the expiration time of this cookie, in unix time. If the expiration is 0, the cookie never expires.
	 *
	 * @return
	 */
	public long getExpiration() {
		return expiration;
	}

	/**
	 * Returns true if this cookie only applies in http, not https requests.
	 *
	 * @return
	 */
	public boolean isHttpOnly() {
		return httpOnly;
	}

	/**
	 * Returns true if this cookie is only applicable in https, not http requests.
	 *
	 * @return
	 */
	public boolean isSecureOnly() {
		return secureOnly;
	}

	/**
	 * Returns true if the cookie is currently expired.
	 *
	 * @return
	 */
	public boolean isExpired() {
		return isExpired(System.currentTimeMillis());
	}

	/**
	 * Returns true if the cookie will be or was expired at the given time.
	 *
	 * @param time The time to check against to determine if the cookie will be expired at that time.
	 * @return
	 */
	public boolean isExpired(long time) {
		return expiration != 0 && expiration < time;
	}

	@Override
	public String toString() {
		return domain + path + ": " + name + "=" + value;
	}

}
