package com.laytonsmith.PureUtilities.Web;

/**
 * This interface should be implemented to respond to async web requests that are managed from within the WebUtility
 * class.
 */
public interface HTTPResponseCallback {

	/**
	 * If the call is successful, the HTTPResponse is returned here.
	 *
	 * @param response
	 */
	public void response(HTTPResponse response);

	/**
	 * If the call is unsuccessful, the Throwable should be handled here.
	 *
	 * @param error
	 */
	public void error(Throwable error);

}
