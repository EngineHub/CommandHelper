package com.laytonsmith.PureUtilities.Web;

import java.io.File;
import java.net.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An object that wraps the HTTP request settings used in the WebUtility class.
 */
public class RequestSettings {
	private HTTPMethod method = HTTPMethod.GET;
	private Map<String, List<String>> headers = null;
	private Map<String, List<String>> parameters = null;
	private CookieJar cookieJar = null;
	private boolean followRedirects = true;
	private int timeout = 60000;
	private String username = null;
	private String password = null;
	private Proxy proxy = null;
	private String rawParameter;
	private File downloadTo;
	
	/**
	 * 
	 * @param method The HTTP method to use
	 * @return 
	 */
	public RequestSettings setMethod(HTTPMethod method){
		this.method = method;
		return this;
	}
	
	/**
	 * 
	 * @return The HTTP method to use
	 */
	public HTTPMethod getMethod(){
		return method;
	}
	
	/**
	 * 
	 * @param username The username to use in response to HTTP Basic authentication. Null ignores this parameter.
	 * @param password The password to use in response to HTTP Basic authentication. Null ignores this parameter.
	 * @return 
	 */
	public RequestSettings setAuthenticationDetails(String username, String password){
		this.username = username;
		this.password = password;
		return this;
	}
	
	/**
	 * 
	 * @param headers The HTTP headers to set in the request.
	 * @return 
	 */
	public RequestSettings setHeaders(Map<String, List<String>> headers){
		this.headers = headers;
		return this;
	}
	
	/**
	 * 
	 * @return The HTTP headers to set in the request.
	 */
	public Map<String, List<String>> getHeaders(){
		return headers;
	}
	
	/**
	 * 
	 * @param parameters The parameters to be sent. Parameters can be also
	 * specified directly in the URL, and they will be merged. May be null.
	 * This is a convenience method for setComplexParameters, because that is
	 * technically the only way to set the parameters, because array parameters
	 * are supported, but often times this isn't needed, so this is a simpler
	 * setter.
	 * @return 
	 */
	public RequestSettings setParameters(Map<String, String> parameters){
		if(parameters == null){
			this.parameters = null;
			return this;
		} else {
			Map<String, List<String>> p = new HashMap<String, List<String>>();
			for(String key : parameters.keySet()){
				p.put(key, Arrays.asList(new String[]{parameters.get(key)}));
			}
			return setComplexParameters(p);
		}
	}
	/**
	 * 
	 * @param parameters The parameters to be sent. Parameters can be also
	 * specified directly in the URL, and they will be merged. May be null.
	 * @return 
	 */
	public RequestSettings setComplexParameters(Map<String, List<String>> parameters){
		this.parameters = parameters;
		return this;
	}
	
	/**
	 * 
	 * @return The parameters to be sent. Parameters can be also
	 * specified directly in the URL, and they will be merged. May be null.
	 */
	public Map<String, List<String>> getParameters(){
		return parameters;
	}
	
	/**
	 * 
	 * @param cookieJar An instance of a cookie jar to use, or null if none
	 * is needed. Cookies will automatically be added and used from this
	 * instance.
	 * @return 
	 */
	public RequestSettings setCookieJar(CookieJar cookieJar){
		this.cookieJar = cookieJar;
		return this;
	}
	
	/**
	 * 
	 * @param proxyAddress The proxy for this connection to use
	 * @return 
	 */
	public RequestSettings setProxy(Proxy proxy){
		this.proxy = proxy;
		return this;
	}
	
	/**
	 * 
	 * @return An instance of a cookie jar to use, or null if none
	 * is needed. Cookies will automatically be added and used from this
	 * instance.
	 */
	public CookieJar getCookieJar(){
		return cookieJar;
	}
	
	/**
	 * 
	 * @param followRedirects If 300 code responses should automatically be
	 * followed.
	 * @return 
	 */
	public RequestSettings setFollowRedirects(boolean followRedirects){
		this.followRedirects = followRedirects;
		return this;
	}
	
	/**
	 * 
	 * @return If 300 code responses should automatically be
	 * followed.
	 */
	public boolean getFollowRedirects(){
		return followRedirects;
	}
	
	/**
	 * 
	 * @param timeout Sets the timeout in ms for this connection. 0 means no timeout. If the timeout
	 * is reached, a SocketTimeoutException will be thrown.
	 * @return 
	 */
	public RequestSettings setTimeout(int timeout){
		this.timeout = timeout;
		return this;
	}
	
	/**
	 * 
	 * @return Sets the timeout in ms for this connection. 0 means no timeout. If the timeout
	 * is reached, a SocketTimeoutException will be thrown.
	 */
	public int getTimeout(){
		return timeout;
	}
	
	/**
	 * 
	 * @return The username to use in response to HTTP Basic authentication.
	 */
	public String getUsername(){
		return username;
	}
	
	/**
	 * 
	 * @return The password to use in response to HTTP Basic authentication. Null ignores this parameter.
	 */
	public String getPassword(){
		return password;
	}
	
	/**
	 * 
	 * @return The proxy address for this connection to use
	 */
	public Proxy getProxy(){
		return proxy;
	}

	/**
	 *
	 * @return The raw parameter to send in a post request
	 */
	public String getRawParameter() {
		return rawParameter;
	}
	
	/**
	 * 
	 * @param rawParamter The raw parameter to send in a post request
	 * @return 
	 */
	public RequestSettings setRawParameter(String rawParamter) {
		this.rawParameter = rawParamter;
		return this;
	}
	
	/**
	 * If this is not null, the resulting page will be downloaded to the
	 * specified file location.
	 * @param downloadTo The file location to download to, or null.
	 * @return 
	 */
	public RequestSettings setDownloadTo(File downloadTo){
		this.downloadTo = downloadTo;
		return this;
	}
	
	/**
	 * 
	 * @return The file location to download to, or null if this shouldn't save the
	 * request as a file.
	 */
	public File getDownloadTo(){
		return this.downloadTo;
	}
	
}
