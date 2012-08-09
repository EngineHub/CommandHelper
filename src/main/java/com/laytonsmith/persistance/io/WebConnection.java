package com.laytonsmith.persistance.io;

import com.laytonsmith.PureUtilities.WebUtility;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 *
 * @author lsmith
 */
public class WebConnection implements ConnectionMixin{
	URL source;
	public WebConnection(URI uri) throws MalformedURLException{		
		source = uri.toURL();
	}

	public String getData() throws IOException {
		return WebUtility.GetPageContents(source);
	}

	public void writeData(String data) throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
