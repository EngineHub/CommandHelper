package com.laytonsmith.tools.langserv;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.JSONUtil;
import com.laytonsmith.core.AbstractCommandLineTool;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.tool;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 */
public class LanguageServerUtil {

	@tool("lang-serv")
	public static class LangServMode extends AbstractCommandLineTool {

		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Starts up the language server, which implements the Language Server Protocol")
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("The host location that the client is running on.")
						.setUsageName("host")
						.setRequired()
						.setName("host")
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.STRING))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("The port the client is running on.")
						.setUsageName("port")
						.setRequired()
						.setName("port")
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.NUMBER))

					.setErrorOnUnknownArgs(false)
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("For future compatibility reasons, unrecognized arguments are not an error,"
								+ " but they are not supported unless otherwise noted.")
						.setUsageName("unrecognizedArgs")
						.setOptionalAndDefault()
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.ARRAY_OF_STRINGS));
		}

		@Override
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String hostname = parsedArgs.getStringArgument("host");
			int port = parsedArgs.getNumberArgument("port").intValue();

			log("Starting up Language Server: " + parsedArgs.getRawArguments(), LogLevel.INFO);
			try {
				new LanguageServerUtil(hostname, port).start();
			} catch (Throwable t) {
				t.printStackTrace(System.err);
				System.exit(1);
			}
		}

	}

	@MSLog.LogTag
	public static final MSLog.Tag LANGSERVLOGTAG = new MSLog.Tag() {
		@Override
		public String getName() {
			return "langserv";
		}

		@Override
		public String getDescription() {
			return "Logs events related to the Language Server";
		}

		@Override
		public LogLevel getLevel() {
			return LogLevel.WARNING;
		}
	};

	private final String hostname;
	private final int port;
	private Socket socket;

	/**
	 * Maps the method names to the classes that the request is deserialized into.
	 */
	private final Map<String, Class<? extends Message>> messageTypes;

	/**
	 * Maps the method names to the methods themselves.
	 */
	private final Map<String, Method> methods;

	public LanguageServerUtil(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;

		this.messageTypes = new HashMap<>();
		this.methods = new HashMap<>();

		for(Class<? extends Message> c : ClassDiscovery.getDefaultInstance()
				.loadClassesWithAnnotationThatExtend(LangServMethod.class, Message.class)) {
			String method = c.getAnnotation(LangServMethod.class).value();
			messageTypes.put(method, c);
		}
		for(Method m : ClassDiscovery.getDefaultInstance().loadMethodsWithAnnotation(LangServMethod.class)) {
			if((m.getModifiers() & Modifier.STATIC) == 0) {
				System.err.println("Method handler " + m + " is not static!");
				continue;
			}
			String method = m.getAnnotation(LangServMethod.class).value();
			methods.put(method, m);
		}
	}

	@SuppressWarnings("UseSpecificCatch")
	public void start() throws IOException {
		socket = new Socket(hostname, port);
		socket.setKeepAlive(true);
		// Don't do a buffered input stream, or we'll eventually run out of memory. Because of that, we have
		// a slightly more complicated implementation.
		InputStream is = socket.getInputStream();
		int v;
		StringBuilder b = new StringBuilder();
		int contentLength = 0;
		while((v = is.read()) != -1) {
			if(v == '\r') {
				// Headers use \r\n, but so we can stay stateless, just ignore this value.
				continue;
			}
			if(v == '\n') {
				if(b.toString().equals("")) {
					// We can now read the message in. It should be contentLength long
					b = new StringBuilder();
					byte[] message = new byte[contentLength];
					is.read(message);
					try {
						processMessage(message, socket.getOutputStream());
					} catch (Throwable t) {
						System.err.println("Unhandled exception:");
						t.printStackTrace(System.err);
					}
					continue;
				} else {
					// It's a header. Currently, we only care about Content-Length.
					String header = b.toString();
					b = new StringBuilder();
					String[] parts = header.split(":");
					if(parts[0].trim().equalsIgnoreCase("content-length")) {
						contentLength = Integer.parseInt(parts[1].trim());
					}
					continue;
				}
			}
			b.append((char) v);
		}
	}

	/**
	 * Processes a message
	 * @param message The message sent from the client
	 * @param out The output stream, so we can respond to it
	 */
	private void processMessage(byte[] message, OutputStream out) throws UnsupportedEncodingException,
			JSONUtil.JSONException,
			IllegalAccessException,
			IllegalArgumentException,
			InvocationTargetException {
		String msg = new String(message, "UTF-8");
		log("Got message from client. Length: " + message.length, LogLevel.DEBUG);
		log("Message was: " + msg, LogLevel.VERBOSE);
		JSONObject obj = (JSONObject) JSONValue.parse(msg);
		String method = obj.get("method").toString();
		Class<?> methodClass = messageTypes.get(method);

		if(methodClass == null) {
			throw new RuntimeException("Unsupported method: " + method);
		}
		Object paramsObject = new JSONUtil().deserialize(msg, methodClass);
		Method m = methods.get(method);
		if(m == null) {
			throw new RuntimeException("No method handler found for " + method);
		}
		Object result = m.invoke(null, paramsObject);
	}

	private static void log(String s, LogLevel level) {
		s = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + s;
		MSLog.GetLogger().Log(LANGSERVLOGTAG, level, s, Target.UNKNOWN);
	}

}
