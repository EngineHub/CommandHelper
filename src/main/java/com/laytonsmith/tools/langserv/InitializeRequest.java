package com.laytonsmith.tools.langserv;

/**
 * The initialize request is sent as the first request from the client to the server. If the server receives a request
 * or notification before the initialize request it should act as follows:
 *
 * <ul>
 * <li>For a request the response should be an error with code: -32002. The message can be picked by the server.</li>
 * <li>Notifications should be dropped, except for the exit notification. This will allow the exit of a server without
 * an initialize request.</li>
 * </ul>
 * Until the server has responded to the initialize request with an InitializeResult, the client must not send any
 * additional requests or notifications to the server. In addition the server is not allowed to send any requests or
 * notifications to the client until it has responded with an InitializeResult, with the exception that during the
 * initialize request the server is allowed to send the notifications window/showMessage, window/logMessage and
 * telemetry/event as well as the window/showMessageRequest request to the client.
 * <p>
 * The initialize request may only be sent once.
 */
@LangServMethod("initialize")
public class InitializeRequest implements RequestMessage<String, InitializeParams> {

	private String id;
	private String method;
	private InitializeParams params;
	private String jsonrpc;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public InitializeParams getParams() {
		return params;
	}

	@Override
	public String getJsonrpc() {
		return jsonrpc;
	}

}
