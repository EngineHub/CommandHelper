package com.laytonsmith.tools.langserv;

/**
 * A request message to describe a request between the client and the server. Every processed request must send a
 * response back to the sender of the request.
 * @param <RequestIdType> Should be either Integer or String
 * @param <ParamsType> An array or object
 */
public interface RequestMessage<RequestIdType, ParamsType> extends Message {
	/**
	 * The request id. The type should be either Integer or String
	 * @return
	 */
	RequestIdType getId();

	/**
	 * The method to be invoked.
	 * @return
	 */
	String getMethod();

	/**
	 * The method's params.
	 * @return
	 */
	ParamsType getParams();
}
