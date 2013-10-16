package com.laytonsmith.PureUtilities.Web;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import org.apache.commons.codec.binary.Base64;

/**
 * Contains methods to simplify web connections.
 *
 * @author lsmith
 */
public final class WebUtility {
	
	public static void main(String[] args) throws Exception {
		CookieJar stash = new CookieJar();
		HTTPResponse resp = GetPage(new URL("http://www.google.com/"), HTTPMethod.GET, null, null, stash, true, 60000);
		System.out.println(stash.getCookies(new URL("http://www.google.com")));
	}

	private WebUtility() {
	}
	private static int urlRetrieverPoolId = 0;
	private static ExecutorService urlRetrieverPool = Executors.newCachedThreadPool(new ThreadFactory() {
		public Thread newThread(Runnable r) {
			return new Thread(r, "URLRetrieverThread-" + (++urlRetrieverPoolId));
		}
	});

	/**
	 * Gets a web page based on the parameters specified. This is a blocking
	 * call, if you wish for it to be event driven, consider using the GetPage
	 * that requires a HTTPResponseCallback.
	 *
	 * @param url The url to navigate to
	 * @param method The HTTP method to use
	 * @param parameters The parameters to be sent. Parameters can be also
	 * specified directly in the URL, and they will be merged. May be null.
	 * @param cookieStash An instance of a cookie stash to use, or null if none
	 * is needed. Cookies will automatically be added and used from this
	 * instance.
	 * @param followRedirects If 300 code responses should automatically be
	 * followed.
	 * @param timeout Sets the timeout in ms for this connection. 0 means no timeout. If the timeout
	 * is reached, a SocketTimeoutException will be thrown.
	 * @return
	 * @throws IOException
	 */
	public static HTTPResponse GetPage(URL url, HTTPMethod method, Map<String, List<String>> headers, 
			Map<String, String> parameters, CookieJar cookieStash, boolean followRedirects, int timeout) throws SocketTimeoutException, IOException {
		RequestSettings settings = new RequestSettings()
				.setMethod(method).setHeaders(headers).setParameters(parameters)
				.setCookieJar(cookieStash).setFollowRedirects(followRedirects).setTimeout(timeout);
		return GetPage(url, settings);
	}
	/**
	 * Gets a web page based on the parameters specified. This is a blocking
	 * call, if you wish for it to be event driven, consider using the GetPage
	 * that requires a HTTPResponseCallback.
	 *
	 * @param url The url to navigate to
	 * @param method The HTTP method to use
	 * @param parameters The parameters to be sent. Parameters can be also
	 * specified directly in the URL, and they will be merged. May be null.
	 * @param cookieStash An instance of a cookie stash to use, or null if none
	 * is needed. Cookies will automatically be added and used from this
	 * instance.
	 * @param followRedirects If 300 code responses should automatically be
	 * followed.
	 * @param timeout Sets the timeout in ms for this connection. 0 means no timeout. If the timeout
	 * is reached, a SocketTimeoutException will be thrown.
	 * @param username The username to use in response to HTTP Basic authentication. Null ignores this parameter.
	 * @param password The password to use in response to HTTP Basic authentication. Null ignores this parameter.
	 * @return
	 * @throws IOException
	 */
	public static HTTPResponse GetPage(URL url, RequestSettings settings) throws SocketTimeoutException, IOException {
		CookieJar cookieStash = settings.getCookieJar();
		RawHTTPResponse response = getWebStream(url, settings);
		StringBuilder b = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(response.getStream()));
		if(settings.getDownloadTo() == null){
			b = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				b.append(line).append("\n");
			}
			in.close();
		} else {
			int r;
			OutputStream out = new BufferedOutputStream(new FileOutputStream(settings.getDownloadTo()));
			while((r = in.read()) != -1){
				out.write(r);
			}
			try{
				out.close();
			} finally {
				in.close();
			}
		}
		//Assume 1.0 if something breaks
		String httpVersion = "1.0";
		Matcher m = Pattern.compile("HTTP/(\\d\\+.\\d+).*").matcher(response.getConnection().getHeaderField(0));
		if(m.find()){
			httpVersion = m.group(1);
		}
		HTTPResponse resp = new HTTPResponse(response.getConnection().getResponseMessage(), 
				response.getConnection().getResponseCode(), response.getConnection().getHeaderFields(), b==null?null:b.toString(), httpVersion);
		if (cookieStash != null && resp.getHeaderNames().contains("Set-Cookie")) {
			//We need to add the cookie to the stash
			for (String h : resp.getHeaders("Set-Cookie")) {
				cookieStash.addCookie(new Cookie(h, url));
			}
		}
		return resp;
	}
	
	/**
	 * Returns the raw web stream. Cookies are used to initiate the request, but
	 * the cookie jar isn't updated with the received cookies.
	 * @param url
	 * @param settings
	 * @return
	 * @throws SocketTimeoutException
	 * @throws IOException 
	 */
	public static RawHTTPResponse getWebStream(URL url, RequestSettings settings) throws SocketTimeoutException, IOException{
		if(settings == null){
			settings = new RequestSettings();
		}
		HTTPMethod method = settings.getMethod();
		Map<String, List<String>> headers = settings.getHeaders();
		Map<String, List<String>> parameters = settings.getParameters();
		CookieJar cookieStash = settings.getCookieJar();
		boolean followRedirects = settings.getFollowRedirects();
		final int timeout = settings.getTimeout();
		String username = settings.getUsername();
		String password = settings.getPassword();
		//First, let's check to see that the url is properly formatted. If there are parameters,
		//and this is a GET request, we want to tack them on to the end.
		if (parameters != null && !parameters.isEmpty() && method == HTTPMethod.GET) {
			StringBuilder b = new StringBuilder(url.getQuery() == null ? "" : url.getQuery());
			if (b.length() != 0) {
				b.append("&");
			}
			b.append(encodeParameters(parameters));
			String query = b.toString();
			url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + "?" + query);
		}
		
		Proxy proxy;
		if(settings.getProxy() == null){
			proxy = Proxy.NO_PROXY;
		} else {
			proxy = settings.getProxy();
		}
		InetSocketAddress addr = (InetSocketAddress)proxy.address();
		if(addr != null){
			if(addr.isUnresolved()){
				throw new IOException("Could not resolve the proxy address: " + addr.toString());
			}
		}
		//FIXME: When given a bad proxy, this causes it to stall forever
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(/*proxy*/);
		conn.setConnectTimeout(timeout);
		conn.setInstanceFollowRedirects(followRedirects);
		if (cookieStash != null) {
			String cookies = cookieStash.getCookies(url);
			if (cookies != null) {
				conn.setRequestProperty("Cookie", cookies);
			}
		}
		if(username != null && password != null){
			conn.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64((username + ":" + password).getBytes("UTF-8")), "UTF-8"));
		}
		if (headers != null) {
			for (String key : headers.keySet()) {
				conn.setRequestProperty(key, StringUtils.Join(headers.get(key), ","));
			}
		}
		conn.setRequestMethod(method.name());
		if (method == HTTPMethod.POST) {
			conn.setDoOutput(true);
			String params = "";
			if(parameters != null && !parameters.isEmpty()){
				params = encodeParameters(parameters);
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			} else if(settings.getRawParameter() != null){
				params = settings.getRawParameter();
			}
			conn.setRequestProperty("Content-Length", Integer.toString(params.length()));
			OutputStream os = new BufferedOutputStream(conn.getOutputStream());
			WriteStringToOutputStream(params, os);
			os.close();
		}

		InputStream is;
		try{
			is = conn.getInputStream();
		} catch(UnknownHostException e){
			throw e;
		} catch(Exception e){
			is = conn.getErrorStream();
		}
		if("x-gzip".equals(conn.getContentEncoding()) || "gzip".equals(conn.getContentEncoding())){
			is = new GZIPInputStream(is);
		} else if("deflate".equals(conn.getContentEncoding())){
			is = new InflaterInputStream(is);
		} else if("identity".equals(conn.getContentEncoding())){
			//This is the default, meaning no transformation is needed.
		}
		if(is == null){
			throw new IOException("Could not connnect to " + url);
		}
		return new RawHTTPResponse(conn, is);
	}

	/**
	 * Returns a properly encoded string of parameters.
	 *
	 * @param parameters
	 * @return
	 */
	public static String encodeParameters(Map<String, List<String>> parameters) {
		if (parameters == null) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (String key : parameters.keySet()) {
			if (!first) {
				b.append("&");
			}
			first = false;
			
			List<String> values = parameters.get(key);
			try {
				if(values.size() == 1){
						String value = values.get(0);
						b.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
				} else {
						for(String value : values){
							b.append(URLEncoder.encode(key + "[]", "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
						}
				}
			} catch (UnsupportedEncodingException ex) {
				throw new Error(ex);
			}
		}
		return b.toString();
	}

	private static void WriteStringToOutputStream(String data, OutputStream os) throws IOException {
		for (Character c : data.toCharArray()) {
			os.write((int) c.charValue());
		}
	}

	/**
	 * A very simple convenience method to get a page, using all the default settings
	 * found in {@link RequestSettings}.
	 *
	 * @param url
	 * @return
	 */
	public static HTTPResponse GetPage(URL url) throws IOException {
		return GetPage(url, null);
	}

	/**
	 * A very simple convenience method to get a page using a string url.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static HTTPResponse GetPage(String url) throws IOException {
		return GetPage(new URL(url));
	}

	/**
	 * A very simple convenience method to get a page. Only the contents are
	 * returned by this method.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String GetPageContents(URL url) throws IOException {
		return GetPage(url).getContent();
	}

	/**
	 * A very simple convenience method to get a page. Only the contents are
	 * returned by this method.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String GetPageContents(String url) throws IOException {
		return GetPage(url).getContent();
	}

	/**
	 * Makes an asynchronous call to a URL, and runs the callback when finished.
	 */
	public static void GetPage(final URL url, final RequestSettings settings, final HTTPResponseCallback callback) {
		urlRetrieverPool.submit(new Runnable() {
			public void run() {
				try {
					HTTPResponse response = GetPage(url, settings);
					if (callback == null) {
						return;
					}
					callback.response(response);
				} catch (IOException ex) {
					if (callback == null) {
						return;
					}
					callback.error(ex);
				}
			}
		});
	}

	public static Map<String, String> getQueryMap(String query) {
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			map.put(name, value);
		}
		return map;
	}
}
