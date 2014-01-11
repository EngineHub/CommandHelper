package com.laytonsmith.persistence.io;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * @author lsmith
 */
public class WebConnection implements ConnectionMixin{
	URL source;
	public WebConnection(URI uri, boolean useHTTPS) throws MalformedURLException{
		URI newURI;
		try {
			newURI = new URI("http" + (useHTTPS?"s":"") + "://" + uri.getHost() + uri.getPath()
				+ (uri.getQuery()==null?"":"?" + uri.getQuery())
				+ (uri.getFragment()==null?"":"#" + uri.getFragment()));
		} catch (URISyntaxException ex) {
			//This shouldn't happen, because the URI we received should be correct. If this happens, it's my fault :x
			throw new Error("Bad URI?");
		}
		source = newURI.toURL();
	}

	@Override
	public String getData() throws IOException {
		return WebUtility.GetPageContents(source);
	}

	@Override
	public void writeData(DaemonManager dm, String data) throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getPath() throws UnsupportedOperationException {
		return source.toString();
	}
}
