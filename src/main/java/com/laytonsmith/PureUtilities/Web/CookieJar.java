package com.laytonsmith.PureUtilities.Web;

import com.laytonsmith.PureUtilities.LinkedComparatorSet;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages a list of several cookies. There are several ways to add a new cookie to this manager, and the
 * getCookies method returns the pre-parsed string that is suitable for direct use in an HTTP request header.
 */
public final class CookieJar {

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(Cookie cookie : cookies) {
			if(!cookie.isExpired()) {
				b.append(cookie.getName()).append("=")
						.append(cookie.getValue()).append("; used in ")
						.append(cookie.getDomain()).append(cookie.getPath())
						.append("\n");
			}
		}
		return b.toString();
	}
	private final Set<Cookie> cookies = new LinkedComparatorSet<Cookie>(new LinkedComparatorSet.EqualsComparator<Cookie>() {

		@Override
		public boolean checkIfEquals(Cookie val1, Cookie val2) {
			return val1.compareTo(val2) == 0;
		}
	});

	/**
	 * Adds a new, pre-made cookie to this list. The constructors for Cookie are capable of parsing the cookies found in
	 * a HTTP request.
	 *
	 * @param cookie
	 */
	public void addCookie(Cookie cookie) {
		if(this.cookies.contains(cookie)) {
			//This is an update, so remove it first
			this.cookies.remove(cookie);
		}
		this.cookies.add(cookie);
	}

	/**
	 * Returns a string that is suitable to send as is with the Cookie HTTP header. The URL is the search url, which
	 * will look through this cookie jar, and only use the cookies that are applicable to this domain, not expired, etc.
	 *
	 * @param url
	 * @return
	 */
	public String getCookies(URL url) {
		List<Cookie> usable = new ArrayList<Cookie>();
		//So we can iterate linearly
		List<Cookie> foundCookies = new ArrayList<Cookie>(this.cookies);
		for(int i = 0; i < foundCookies.size(); i++) {
			Cookie cookie = foundCookies.get(i);
			if(cookie.isExpired()) {
				//This cookie is expired. Remove it from our list, and continue.
				this.cookies.remove(cookie);
				foundCookies.remove(i);
				i--;
				continue;
			}
			//Or it's secure only, and we aren't in https, continue.
			if(cookie.isSecureOnly() && !url.getProtocol().equals("https")) {
				continue;
			}
			//If we aren't in the correct domain
			String domain = cookie.getDomain();
			if(domain.startsWith(".")) {
				domain = domain.substring(1);
			}
			if(!url.getHost().endsWith(domain)) {
				continue;
			}
			//Or if we aren't in the right path
			String path = (url.getPath().startsWith("/") ? "" : "/") + url.getPath();
			if(!path.startsWith(cookie.getPath())) {
				continue;
			}
			//If we're still here, it's good.
			usable.add(cookie);
		}
		if(usable.isEmpty()) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		for(Cookie cookie : usable) {
			if(b.length() != 0) {
				b.append("; ");
			}
			try {
				b.append(URLEncoder.encode(cookie.getName(), "UTF-8")).append("=").append(cookie.getValue());
			} catch (UnsupportedEncodingException ex) {
				Logger.getLogger(WebUtility.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return b.toString();
	}

	/**
	 * Clears out all session cookies from this cookie jar. That is, all cookies with an expiration of 0 set.
	 */
	public void clearSessionCookies() {
		Iterator<Cookie> it = cookies.iterator();
		while(it.hasNext()) {
			Cookie c = it.next();
			if(c.getExpiration() == 0) {
				it.remove();
			}
		}
	}

	/**
	 * Clears all cookies from this cookie jar.
	 */
	public void clearAllCookies() {
		cookies.clear();
	}

	/**
	 * Returns a copy of all the cookies in this cookie jar.
	 *
	 * @return
	 */
	public Set<Cookie> getAllCookies() {
		return new TreeSet<Cookie>(cookies);
	}

}
