package com.laytonsmith.PureUtilities.Web;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.FileWriteMode;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.codec.binary.Base64;
import org.brotli.dec.BrotliInputStream;

/**
 * Contains methods to simplify web connections.
 *
 *
 */
public final class WebUtility {

	public static void main(String[] args) throws Exception {
		CookieJar stash = new CookieJar();
		HTTPResponse resp = GetPage(new URL("http://www.google.com/"), HTTPMethod.GET, null, null, stash, true, 60000);
		StreamUtils.GetSystemOut().println(stash.getCookies(new URL("http://www.google.com")));
	}

	/**
	 * This is the list of encodings that this class supports. Generally speaking, this is the list that you should
	 * provide in the Accept-Encoding list. If you wish to support something other than this list, you should
	 * disable encoding support, and manage the decompression entirely yourself.
	 */
	public static final Set<String> SUPPORTED_ENCODINGS
			= Collections.unmodifiableSet(new HashSet<>(
					Arrays.asList(new String[]{"gzip", "deflate", "br", "identity"})));

	private WebUtility() {
	}
	private static int urlRetrieverPoolId = 0;
	private static final ExecutorService URL_RETRIEVER_POOL = Executors.newCachedThreadPool(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "URLRetrieverThread-" + (++urlRetrieverPoolId));
		}
	});

	/**
	 * Gets a web page based on the parameters specified. This is a blocking call, if you wish for it to be event
	 * driven, consider using the GetPage that requires a HTTPResponseCallback.
	 *
	 * @param url The url to navigate to
	 * @param method The HTTP method to use
	 * @param parameters The parameters to be sent. Parameters can be also specified directly in the URL, and they will
	 * be merged. May be null.
	 * @param cookieStash An instance of a cookie stash to use, or null if none is needed. Cookies will automatically be
	 * added and used from this instance.
	 * @param followRedirects If 300 code responses should automatically be followed.
	 * @param timeout Sets the timeout in ms for this connection. 0 means no timeout. If the timeout is reached, a
	 * SocketTimeoutException will be thrown.
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
	 * Gets a web page based on the parameters specified. This is a blocking call, if you wish for it to be event
	 * driven, consider using the GetPage that requires a HTTPResponseCallback.
	 * <p>
	 * The settings may have the following parameters:
	 * <ul>
	 * <li>method - The HTTP method to use</li>
	 * <li>parameters - The parameters to be sent. Parameters can be also specified directly in the URL, and they will
	 * be merged. May be null.</li>
	 * <li>cookieStash - An instance of a cookie stash to use, or null if none is needed. Cookies will automatically be
	 * added and used from this instance.</li>
	 * <li>followRedirects - If 300 code responses should automatically be followed.</li>
	 * <li>timeout - Sets the timeout in ms for this connection. 0 means no timeout. If the timeout is reached, a
	 * SocketTimeoutException will be thrown.</li>
	 * <li>username - The username to use in response to HTTP Basic authentication. Null ignores this parameter.</li>
	 * <li>password - The password to use in response to HTTP Basic authentication. Null ignores this parameter.</li>
	 * </ul>
	 * @param url The url to navigate to
	 * @param settings The settings to use for this request
	 * @return
	 * @throws SocketTimeoutException If the request took longer than the configured timeout
	 * @throws IOException If the connection could not be made properly
	 */
	public static HTTPResponse GetPage(URL url, RequestSettings settings) throws SocketTimeoutException, IOException {
		// If SAFE_WRITE is set, there's no reason to do the download given we know it will fail later, so let's fail
		// fast, and do the check here
		if(settings.getDownloadTo() != null && settings.getDownloadStrategy() == FileWriteMode.SAFE_WRITE) {
			if(settings.getDownloadTo().exists()) {
				throw new IOException("Refusing to download file, destination path already exists ["
						+ settings.getDownloadTo() + "]");
			}
		}
		CookieJar cookieStash = settings.getCookieJar();
		RawHTTPResponse response = getWebStream(url, settings);
		byte[] b = null;
		InputStream in = new BufferedInputStream(response.getStream());
		if(settings.getDownloadTo() == null) {
			if(settings.getLogger() != null) {
				settings.getLogger().log(Level.INFO, "Reading in response body");
			}
			try {
				b = StreamUtils.GetBytes(in);
			} finally {
				in.close();
			}
		} else {
			if(settings.getLogger() != null) {
				settings.getLogger().log(Level.INFO, "Saving file to [{0}] using {1} strategy",
						new Object[]{settings.getDownloadTo(), settings.getDownloadStrategy()});
			}
			try {
				FileUtil.write(StreamUtils.GetBytes(in), settings.getDownloadTo(), settings.getDownloadStrategy(), true);
			} finally {
				in.close();
			}
		}
		//Assume 1.0 if something breaks
		String httpVersion = "1.0";
		Matcher m = Pattern.compile("HTTP/(\\d+.\\d+).*").matcher(response.getConnection().getHeaderField(0));
		if(m.find()) {
			httpVersion = m.group(1);
		}
		HTTPResponse resp = new HTTPResponse(response.getConnection().getResponseMessage(),
				response.getConnection().getResponseCode(), response.getConnection().getHeaderFields(), b, httpVersion);
		if(cookieStash != null && resp.getHeaderNames().contains("Set-Cookie")) {
			//We need to add the cookie to the stash
			for(String h : resp.getHeaders("Set-Cookie")) {
				cookieStash.addCookie(new Cookie(h, url));
			}
		}
		return resp;
	}

	/**
	 * Makes an asynchronous call to a URL, and runs the callback when finished.
	 */
	public static void GetPage(final URL url, final RequestSettings settings, final HTTPResponseCallback callback) {
		URL_RETRIEVER_POOL.submit(new Runnable() {
			@Override
			public void run() {
				try {
					HTTPResponse response = GetPage(url, settings);
					if(callback == null) {
						return;
					}
					callback.response(response);
				} catch (IOException ex) {
					if(callback == null) {
						return;
					}
					callback.error(ex);
				}
			}
		});
	}

	/**
	 * A very simple convenience method to get a page, using all the default settings found in {@link RequestSettings}.
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
	 * Returns the raw web stream. Cookies are used to initiate the request, but the cookie jar isn't updated with the
	 * received cookies.
	 *
	 * @param url
	 * @param settings
	 * @return
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	public static RawHTTPResponse getWebStream(URL url, RequestSettings requestSettings)
			throws SocketTimeoutException, IOException {
		if(requestSettings == null) {
			requestSettings = new RequestSettings();
		}
		final RequestSettings settings = requestSettings;
		Logger logger = settings.getLogger();
		HTTPMethod method = settings.getMethod();
		Map<String, List<String>> headers = settings.getHeaders();
		Map<String, List<String>> parameters = settings.getParameters();
		Map<String, List<String>> queryParameters = settings.getQueryParameters();
		CookieJar cookieStash = settings.getCookieJar();
		boolean followRedirects = settings.getFollowRedirects();
		final int timeout = settings.getTimeout();
		String username = settings.getUsername();
		String password = settings.getPassword();
		if(logger != null) {
			logger.log(Level.INFO, "Using the following settings:\n"
					+ "HTTP method: {0}\n"
					+ "Headers: {1}\n"
					+ "Parameters: {2}\n"
					+ "Raw parameter Length: {3}\n"
					+ "Cookie stash: {4}\n"
					+ "Follow redirects? {5}\n"
					+ "Timeout: {6}\n"
					+ "Username: {7}\n"
					+ "Password length: {8}\n",
					new Object[]{method, headers, parameters,
						settings.getRawParameter() == null ? "null" : settings.getRawParameter().length, cookieStash,
						followRedirects, timeout, username, password == null ? "null" : password.length()});
		}
		//First, let's check to see that the url is properly formatted. If there are parameters,
		//and this is a GET request, we want to tack them on to the end. OR, if there is a raw parameter and parameters,
		//and this is a post reqest, put the parameters on anyways.
		if(parameters != null && !parameters.isEmpty()
				&& (method == HTTPMethod.GET
				|| (method != HTTPMethod.GET
					&& settings.getRawParameter() != null && settings.getRawParameter().length != 0))) {
			StringBuilder b = new StringBuilder(url.getQuery() == null ? "" : url.getQuery());
			if(b.length() != 0) {
				b.append("&");
			}
			b.append(encodeListParameters(parameters));
			// Setting this to null avoids further processing below
			parameters = null;
			String query = b.toString();
			url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + "?" + query);
		}

		if(queryParameters != null && !queryParameters.isEmpty()) {
			String query = url.getQuery();
			if(query == null) {
				query = "?";
			}
			query += encodeListParameters(queryParameters);
			url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + query);
		}
		if(logger != null) {
			logger.log(Level.INFO, "Using url: {0}", url);
		}

		Proxy proxy;
		if(settings.getProxy() == null) {
			proxy = Proxy.NO_PROXY;
		} else {
			proxy = settings.getProxy();
		}
		if(logger != null) {
			logger.log(Level.INFO, "Using proxy: {0}", proxy);
		}
		InetSocketAddress addr = (InetSocketAddress) proxy.address();
		if(addr != null) {
			if(addr.isUnresolved()) {
				throw new IOException("Could not resolve the proxy address: " + addr.toString());
			}
		}
		if(logger != null) {
			logger.log(Level.INFO, "Opening connection...");
		}
		//FIXME: When given a bad proxy, this causes it to stall forever
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(/*proxy*/);
		if(conn instanceof HttpsURLConnection
				&& (settings.getDisableCertChecking() || settings.getUseDefaultTrustStore() == false
				|| !settings.getTrustStore().isEmpty())) {
			HttpsURLConnection conns = (HttpsURLConnection) conn;
			// User has requested special handling in the certificates.

			final SSLContext sslc;
			try {
				sslc = SSLContext.getInstance("SSL");
			} catch (NoSuchAlgorithmException ex) {
				throw new IOException(ex);
			}
			TrustManager defaultTrustManager = null;
			{
				if(settings.getUseDefaultTrustStore()) {
					TrustManagerFactory tmf;
					try {
						tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					} catch (NoSuchAlgorithmException ex) {
						throw new RuntimeException(ex);
					}
					try {
						tmf.init((KeyStore) null);
					} catch (KeyStoreException ex) {
						throw new IOException(ex);
					}
					for(TrustManager tm : tmf.getTrustManagers()) {
						if(tm instanceof X509TrustManager) {
							defaultTrustManager = tm;
							break;
						}
					}
				} else {
					defaultTrustManager = null;
				}
			}
			final X509TrustManager finalDefaultTrustManager = (X509TrustManager) defaultTrustManager;
			final TrustManager[] overrideTrustManager = new TrustManager[]{
				new X509TrustManager() {
					@Override
					public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
						// Hmm. Not sure when this would be used. Always throw for now.
						throw new CertificateException("Not supported yet");
					}

					@Override
					public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
						if(settings.getDisableCertChecking()) {
							// No cert checking, all pass
							return;
						}
						boolean trusted = true;
						if(finalDefaultTrustManager != null) {
							try {
								finalDefaultTrustManager.checkClientTrusted(xcs, string);
							} catch (CertificateException ex) {
								trusted = false;
							}
						}
						if(trusted) {
							return;
						}
						// If any of the certificates are trusted, then the whole chain is trusted
						for(X509Certificate c : xcs) {
							// Unfortunately, we do not know what schemes to use, so we must walk through each
							// trust store item one at a time. We have a documented guarantee that we will walk
							// this list from top to bottom, so we have a linked hash map.
							LinkedHashMap<String, String> ts = settings.getTrustStore();
							for(String fingerprint : ts.keySet()) {
								fingerprint = fingerprint.toLowerCase().replace(" ", "");
								try {
									String scheme = ts.get(fingerprint);
									String fp = getThumbPrint(c, scheme).toLowerCase().replace(" ", "");
									if(fp.equals(fingerprint)) {
										return;
									}
								} catch (NoSuchAlgorithmException | CertificateEncodingException ex) {
									throw new RuntimeException(ex);
								}
							}
						}
						// None of the certificates matched, so throw an exception
						throw new CertificateException();
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						if(settings.getDisableCertChecking()) {
							return new X509Certificate[0];
						}
						if(finalDefaultTrustManager != null) {
							return finalDefaultTrustManager.getAcceptedIssuers();
						}
						return new X509Certificate[0];
					}
				}
			};
			try {
				sslc.init(null, overrideTrustManager, new java.security.SecureRandom());
			} catch (KeyManagementException ex) {
				throw new IOException(ex);
			}
			final SSLSocketFactory ssf;
			ssf = sslc.getSocketFactory();
			conns.setSSLSocketFactory(new SSLSocketFactory() {

				@Override
				public String[] getDefaultCipherSuites() {
					return ssf.getDefaultCipherSuites();
				}

				@Override
				public String[] getSupportedCipherSuites() {
					return ssf.getSupportedCipherSuites();
				}

				@Override
				public Socket createSocket(Socket socket, String string, int i, boolean bln) throws IOException {
					return ssf.createSocket(socket, string, i, bln);
				}

				@Override
				public Socket createSocket(String string, int i) throws IOException, UnknownHostException {
					return ssf.createSocket(string, i);
				}

				@Override
				public Socket createSocket(String string, int i, InetAddress ia, int i1) throws IOException, UnknownHostException {
					return ssf.createSocket(string, i, ia, i1);
				}

				@Override
				public Socket createSocket(InetAddress ia, int i) throws IOException {
					return ssf.createSocket(ia, i);
				}

				@Override
				public Socket createSocket(InetAddress ia, int i, InetAddress ia1, int i1) throws IOException {
					return ssf.createSocket(ia, i, ia1, i1);
				}
			});
		}
		conn.setConnectTimeout(timeout);
		conn.setInstanceFollowRedirects(followRedirects);
		if(cookieStash != null) {
			String cookies = cookieStash.getCookies(url);
			if(cookies != null) {
				conn.setRequestProperty("Cookie", cookies);
			}
		}
		if(username != null && password != null) {
			if(logger != null) {
				logger.log(Level.INFO, "Using Username/Password authentication, adding Authorization header");
			}
			conn.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64((username + ":" + password).getBytes("UTF-8")), "UTF-8"));
		}
		if(headers != null) {
			for(String key : headers.keySet()) {
				conn.setRequestProperty(key, StringUtils.Join(headers.get(key), ","));
			}
		}
		conn.setRequestMethod(method.name());
		if((parameters != null && !parameters.isEmpty() && !method.equals(HTTPMethod.GET))
				|| settings.getRawParameter() != null) {
			if(logger != null) {
				if(method.equals(HTTPMethod.GET)) {
					logger.log(Level.WARNING, "Method was set to GET, but raw parameter data was provided, so method"
							+ " is changing to POST.");
				}
			}
			conn.setDoOutput(true);
			byte[] params = ArrayUtils.EMPTY_BYTE_ARRAY;
			if(parameters != null && !parameters.isEmpty()) {
				if(logger != null) {
					logger.log(Level.INFO, "Parameters are added, and content type set to application/x-www-form-urlencoded");
				}
				params = encodeListParameters(parameters).getBytes("UTF-8");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			} else if(settings.getRawParameter() != null) {
				if(logger != null) {
					logger.log(Level.INFO, "Raw parameter is added");
				}
				params = settings.getRawParameter();
			}
			conn.setRequestProperty("Content-Length", Integer.toString(params.length));
			if(logger != null) {
				logger.log(Level.INFO, "Content length is {0}", params.length);
				logger.log(Level.INFO, "Writing out request now");
			}
			OutputStream os = new BufferedOutputStream(conn.getOutputStream());
			os.write(params);
			os.close();
		}
		if(logger != null) {
			logger.log(Level.INFO, "Output sent");
		}
		InputStream is;
		try {
			is = conn.getInputStream();
		} catch (UnknownHostException e) {
			throw e;
		} catch (Exception e) {
			if(logger != null) {
				logger.log(Level.SEVERE, "Exception occurred, {0} response from server", conn.getResponseCode());
			}
			if(e instanceof SSLHandshakeException) {
				// The certificate was not valid, and the input stream will be null anyways, so just throw at this
				// point.
				throw new IOException("Invalid SSL certificate for " + url.getHost() + ". Refusing to connect.");
			}
			is = conn.getErrorStream();
		}

		if(!settings.getDisableCompressionHandling() && conn.getContentEncoding() != null) {
			/*
			The HTTP spec for Content-Encoding specifies that multiple comma separated values can be provided. Where
			more than one is provided, this means that the content was compressed multiple times, in the specified order.
			Given that, we must loop through the list, wrapping the input stream in the given decompression handlers.
			In practice, this will only loop once though.
			*/
			List<String> compression
					= Stream.of(conn.getContentEncoding().split(",")).map((e) -> e.trim()).collect(Collectors.toList());
			for(String scheme : compression) {
				if("x-gzip".equals(scheme) || "gzip".equals(scheme)) {
					if(logger != null) {
						logger.log(Level.INFO, "Response is gzipped, using a GZIPInputStream");
					}
					is = new GZIPInputStream(is);
				} else if("deflate".equals(scheme)) {
					if(logger != null) {
						logger.log(Level.INFO, "Response is zipped, using an InflaterInputStream");
					}
					is = new InflaterInputStream(is);
				} else if("br".equals(scheme)) {
					if(logger != null) {
						logger.log(Level.INFO, "Response is Brotli compressed, using a BrotliInputStream");
					}
					is = new BrotliInputStream(is);
				} else if("identity".equals(scheme)) {
					//This is the default, meaning no transformation is needed.
					if(logger != null) {
						logger.log(Level.INFO, "Response is not compressed");
					}
				}
			}
		}
		if(is == null) {
			throw new IOException("Could not connect to " + url);
		}
		return new RawHTTPResponse(conn, is);
	}

	/**
	 * Returns a properly encoded string of parameters.
	 *
	 * @param parameters
	 * @return
	 */
	public static String encodeParameters(Map<String, String> parameters) {
		Map<String, List<String>> p = new HashMap<>();
		for(Map.Entry<String, String> e : parameters.entrySet()) {
			List<String> list = new ArrayList<>();
			list.add(e.getValue());
			p.put(e.getKey(), list);
		}
		return encodeListParameters(p);
	}

	/**
	 * Returns a properly encoded string of parameters. Array types are returned using bracket notation,
	 * for instance, if the input is {@code {a: [1, 2]}, then the output will be {@code a[]=1&a[]=2}.
	 *
	 * @param parameters
	 * @return
	 */
	public static String encodeListParameters(Map<String, List<String>> parameters) {
		if(parameters == null) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for(String key : parameters.keySet()) {
			if(!first) {
				b.append("&");
			}
			first = false;
			List<String> values = parameters.get(key);
			try {
				if(values.size() == 1) {
					String value = values.get(0);
					b.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
				} else {
					boolean innerFirst = true;
					for(String value : values) {
						if(!innerFirst) {
							b.append("&");
						}
						innerFirst = false;
						b.append(URLEncoder.encode(key + "[]", "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
					}
				}
			} catch (UnsupportedEncodingException ex) {
				throw new Error(ex);
			}
		}
		return b.toString();
	}

	/**
	 * A very simple convenience method to get a page. Only the contents are returned by this method.
	 * It is assumed that the content is a UTF-8 formatted string, and is not binary content.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String GetPageContents(URL url) throws IOException {
		return new String(GetPage(url).getContent(), "UTF-8");
	}

	/**
	 * A very simple convenience method to get a page. Only the contents are returned by this method.
	 * It is assumed that the content is a UTF-8 formatted string, and is not binary content.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String GetPageContents(String url) throws IOException {
		return new String(GetPage(url).getContent(), "UTF-8");
	}

	/**
	 * A very simple convenience method to get a page. Only the contents are returned by this method.
	 * This supports returning binary content.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static byte[] GetPageContentsBinary(URL url) throws IOException {
		return GetPage(url).getContent();
	}

	/**
	 * A very simple convenience method to get a page. Only the contents are returned by this method.
	 * This supports returning binary content.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static byte[] GetPageContentsBinary(String url) throws IOException {
		return GetPage(url).getContent();
	}

	/**
	 * Given a query string "a=1&b=2", returns a map of that data. Note that this method does not properly
	 * support array values, which are generally supported in the url format, so "a[]=1&a[]=2" and "a=1&a=2",
	 * while technically
	 * allowed in the specification, will not be returned correctly here, and the key will be a[]/a, and the value
	 * will be either 1 or 2, which one is selected is undefined. If you are 100% certain the query string
	 * will not contain array values, it is safe to use this method anyways, but if there is the possibility
	 * of array values being present, use {@link #getQueryMapList} instead.
	 *
	 * @param query
	 * @return
	 */
	public static Map<String, String> getQueryMap(String query) {
		Map<String, String> map = new HashMap<>();
		if(query == null) {
			return map;
		}
		String[] params = query.split("&");
		for(String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			map.put(name, value);
		}
		return map;
	}

	/**
	 * Given a query string "a=1&b=2", returns a map of that data. Note that this method properly
	 * supports array values, so "a[]=1&a[]=2" and "a=1&a=2", will both return a map like
	 * {@code {a: [1, 2]}}. In any case, multidimensional arrays are not supported.
	 *
	 * @param query
	 * @return
	 */
	public static Map<String, List<String>> getQueryMapList(String query) {
		Map<String, List<String>> map = new HashMap<>();
		if(query == null) {
			return map;
		}
		String[] params = query.split("&");
		for(String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			if(name.endsWith("[]")) {
				name = name.substring(0, name.length() - 2);
			}
			List<String> values;
			if(map.containsKey(name)) {
				values = map.get(name);
			} else {
				values = new ArrayList<>();
				map.put(name, values);
			}
			values.add(value);
		}
		return map;
	}

	/**
	 * Given an X509Certificate, calculates and returns the fingerprint in the given encryption scheme
	 *
	 * @param cert The certificate to get the fingerprint from
	 * @param encryptionScheme The encryption scheme, for instance "SHA-1".
	 * @return The hex fingerprint
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateEncodingException
	 */
	public static String getThumbPrint(X509Certificate cert, String encryptionScheme)
			throws NoSuchAlgorithmException, CertificateEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] der = cert.getEncoded();
		md.update(der);
		byte[] digest = md.digest();
		return StringUtils.toHex(digest);

	}

}
