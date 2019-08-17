package com.laytonsmith.PureUtilities.Web;

import com.laytonsmith.PureUtilities.Common.FileWriteMode;
import java.io.File;
import java.net.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * An object that wraps the HTTP request settings used in the WebUtility class.
 */
public class RequestSettings {

	private HTTPMethod method = HTTPMethod.GET;
	private Map<String, List<String>> headers = null;
	private Map<String, List<String>> parameters = null;
	private Map<String, List<String>> queryParameters = null;
	private CookieJar cookieJar = null;
	private boolean followRedirects = true;
	private int timeout = 60000;
	private String username = null;
	private String password = null;
	private Proxy proxy = null;
	private byte[] rawParameter;
	private File downloadTo;
	private FileWriteMode downloadStrategy = FileWriteMode.SAFE_WRITE;
	private boolean blocking = false;
	private boolean disableCertChecking = false;
	private boolean useDefaultTrustStore = true;
	private LinkedHashMap<String, String> trustStore = new LinkedHashMap<>();
	@SuppressWarnings("NonConstantLogger")
	private Logger logger;
	private boolean disableDecompressionHandling = false;

	/**
	 *
	 * @param method The HTTP method to use
	 * @return
	 */
	public RequestSettings setMethod(HTTPMethod method) {
		this.method = method;
		return this;
	}

	/**
	 *
	 * @return The HTTP method to use
	 */
	public HTTPMethod getMethod() {
		return method;
	}

	/**
	 *
	 * @param username The username to use in response to HTTP Basic authentication. Null ignores this parameter.
	 * @param password The password to use in response to HTTP Basic authentication. Null ignores this parameter.
	 * @return
	 */
	public RequestSettings setAuthenticationDetails(String username, String password) {
		this.username = username;
		this.password = password;
		return this;
	}

	/**
	 *
	 * @param headers The HTTP headers to set in the request.
	 * @return
	 */
	public RequestSettings setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
		return this;
	}

	/**
	 *
	 * @return The HTTP headers to set in the request.
	 */
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 *
	 * @param parameters The parameters to be sent. Parameters can be also specified directly in the URL, and they will
	 * be merged. May be null. This is a convenience method for setComplexParameters, because that is technically the
	 * only way to set the parameters, because array parameters are supported, but often times this isn't needed, so
	 * this is a simpler setter.
	 * @return
	 */
	public RequestSettings setParameters(Map<String, String> parameters) {
		if(parameters == null) {
			this.parameters = null;
			return this;
		} else {
			Map<String, List<String>> p = new HashMap<>();
			for(String key : parameters.keySet()) {
				p.put(key, Arrays.asList(new String[]{parameters.get(key)}));
			}
			return setComplexParameters(p);
		}
	}

	/**
	 *
	 * @param parameters The parameters to be sent. Parameters can be also specified directly in the URL, and they will
	 * be merged. May be null.
	 * @return
	 */
	public RequestSettings setComplexParameters(Map<String, List<String>> parameters) {
		this.parameters = parameters;
		return this;
	}

	/**
	 *
	 * @return The parameters to be sent. Parameters can be also specified directly in the URL, and they will be merged.
	 * May be null.
	 */
	public Map<String, List<String>> getParameters() {
		return parameters;
	}

	/**
	 *
	 * @param parameters The parameters to be sent. Parameters can be also specified directly in the URL, and they will
	 * be merged. May be null. This is a convenience method for setComplexParameters, because that is technically the
	 * only way to set the parameters, because array parameters are supported, but often times this isn't needed, so
	 * this is a simpler setter.
	 * @return
	 */
	public RequestSettings setQueryParameters(Map<String, String> parameters) {
		if(parameters == null) {
			this.queryParameters = null;
			return this;
		} else {
			Map<String, List<String>> p = new HashMap<>();
			for(String key : parameters.keySet()) {
				p.put(key, Arrays.asList(new String[]{parameters.get(key)}));
			}
			return setComplexQueryParameters(p);
		}
	}

	/**
	 * Sets the query parameters. Unlike the normal parameters, these are ALWAYS put in the query, regardless
	 * of whether or not the method is GET or POST, which can be useful when a protocol requires an empty post body
	 * as well as query parameters. If you are explicitely setting a post body, you may use either this or
	 * setParameters, as the effect will be the same.
	 * @param parameters
	 * @return
	 */
	public RequestSettings setComplexQueryParameters(Map<String, List<String>> parameters) {
		this.queryParameters = parameters;
		return this;
	}

	/**
	 * Returns the query parameters.
	 * @return
	 */
	public Map<String, List<String>> getQueryParameters() {
		return this.queryParameters;
	}

	/**
	 *
	 * @param cookieJar An instance of a cookie jar to use, or null if none is needed. Cookies will automatically be
	 * added and used from this instance.
	 * @return
	 */
	public RequestSettings setCookieJar(CookieJar cookieJar) {
		this.cookieJar = cookieJar;
		return this;
	}

	/**
	 *
	 * @param proxyAddress The proxy for this connection to use
	 * @return
	 */
	public RequestSettings setProxy(Proxy proxy) {
		this.proxy = proxy;
		return this;
	}

	/**
	 *
	 * @return An instance of a cookie jar to use, or null if none is needed. Cookies will automatically be added and
	 * used from this instance.
	 */
	public CookieJar getCookieJar() {
		return cookieJar;
	}

	/**
	 *
	 * @param followRedirects If 300 code responses should automatically be followed.
	 * @return
	 */
	public RequestSettings setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
		return this;
	}

	/**
	 *
	 * @return If 300 code responses should automatically be followed.
	 */
	public boolean getFollowRedirects() {
		return followRedirects;
	}

	/**
	 *
	 * @param timeout Sets the timeout in ms for this connection. 0 means no timeout. If the timeout is reached, a
	 * SocketTimeoutException will be thrown.
	 * @return
	 */
	public RequestSettings setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 *
	 * @return Sets the timeout in ms for this connection. 0 means no timeout. If the timeout is reached, a
	 * SocketTimeoutException will be thrown.
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 *
	 * @return The username to use in response to HTTP Basic authentication.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 *
	 * @return The password to use in response to HTTP Basic authentication. Null ignores this parameter.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 *
	 * @return The proxy address for this connection to use
	 */
	public Proxy getProxy() {
		return proxy;
	}

	/**
	 *
	 * @return The raw parameter to send in a post request
	 */
	public byte[] getRawParameter() {
		return rawParameter;
	}

	/**
	 *
	 * @param rawParamter The raw parameter to send in a post request
	 * @return
	 */
	public RequestSettings setRawParameter(byte[] rawParamter) {
		this.rawParameter = rawParamter;
		return this;
	}

	/**
	 * If this is not null, the resulting page will be downloaded to the specified file location.
	 *
	 * @param downloadTo The file location to download to, or null.
	 * @return
	 */
	public RequestSettings setDownloadTo(File downloadTo) {
		this.downloadTo = downloadTo;
		return this;
	}

	/**
	 * Sets the download strategy.
	 * @param downloadStrategy
	 * @return
	 */
	public RequestSettings setDownloadStrategy(FileWriteMode downloadStrategy) {
		this.downloadStrategy = downloadStrategy;
		return this;
	}

	/**
	 *
	 * @return The file location to download to, or null if this shouldn't save the request as a file.
	 */
	public File getDownloadTo() {
		return this.downloadTo;
	}

	/**
	 * Returns the configured download strategy. The default is {@link FileWriteMode#SAFE_WRITE}
	 * @return
	 */
	public FileWriteMode getDownloadStrategy() {
		return this.downloadStrategy;
	}

	/**
	 * Sets the logger to use. If null, disables logging.
	 *
	 * @param logger
	 * @return
	 */
	public RequestSettings setLogger(Logger logger) {
		this.logger = logger;
		return this;
	}

	/**
	 * @return The logger to use when making requests. May be null, in which case, it means to not do logging.
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * Sets whether or not this should be a blocking request
	 *
	 * @param blocking
	 * @return
	 */
	public RequestSettings setBlocking(boolean blocking) {
		this.blocking = blocking;
		return this;
	}

	/**
	 * Returns whether or not this should be a blocking request
	 *
	 * @return
	 */
	public boolean getBlocking() {
		return this.blocking;
	}

	/**
	 * Sets whether or not cert checking is disabled. If this is true, NO certificate checking is done, and all
	 * certificates will be considered valid. If this is true, {@link #setUseDefaultTrustStore(boolean)} and
	 * {@link #setTrustStore(java.util.Map)} are ignored.
	 *
	 * @param check
	 * @return
	 */
	public RequestSettings setDisableCertChecking(boolean check) {
		this.disableCertChecking = check;
		return this;
	}

	/**
	 * Returns whether or not the trust store should be disabled. Default is false.
	 *
	 * @return
	 */
	public boolean getDisableCertChecking() {
		return this.disableCertChecking;
	}

	/**
	 * Sets whether or not to use the default trust store. If false, then only certificates registered using
	 * {@link #setTrustStore(java.util.Map)} will be accepted. If this is false, and
	 * {@link #setTrustStore(java.util.Map)} is false, this effectively prevents any ssl connections.
	 *
	 * @param useDefaultTrustStore
	 * @return
	 */
	public RequestSettings setUseDefaultTrustStore(boolean useDefaultTrustStore) {
		this.useDefaultTrustStore = useDefaultTrustStore;
		return this;
	}

	/**
	 * Returns whether or not the default trust store should be used.
	 *
	 * @return
	 */
	public boolean getUseDefaultTrustStore() {
		return this.useDefaultTrustStore;
	}

	/**
	 * Sets the trust store. Values should be in the form: "02 79 AB D6 97 19 A2 CB E8 79 11 B2 7F AF 8D": "SHA-256"
	 * where the key is the fingerprint, and the value is the encryption scheme. Note that the map is cloned, and the
	 * original map is not used.
	 *
	 * @param trustStore The trust store to use
	 * @return
	 */
	public RequestSettings setTrustStore(LinkedHashMap<String, String> trustStore) {
		this.trustStore = new LinkedHashMap<>(trustStore);
		return this;
	}

	/**
	 * Returns the trust store in use. Note that the map is cloned, and the original map is not used.
	 *
	 * @return
	 */
	public LinkedHashMap<String, String> getTrustStore() {
		return new LinkedHashMap<>(trustStore);
	}

	/**
	 * Sets the disableCompressionHandling flag. If true, the content will be returned as is, no matter what the
	 * value of the Content-Encoding header is, and must be processed manually.
	 * @param disableCompressionHandling
	 * @return
	 */
	public RequestSettings setDisableCompressionHandling(boolean disableCompressionHandling) {
		this.disableDecompressionHandling = disableCompressionHandling;
		return this;
	}

	/**
	 * Returns the disableCompressionHandling flag. Defaults to false.
	 * @return
	 */
	public boolean getDisableCompressionHandling() {
		return this.disableDecompressionHandling;
	}

}
