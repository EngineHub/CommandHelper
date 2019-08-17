package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.Web.HTTPMethod;
import com.laytonsmith.PureUtilities.Web.HTTPResponse;
import com.laytonsmith.PureUtilities.Web.RequestSettings;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREOAuthException;
import com.laytonsmith.core.exceptions.CRE.CREReadOnlyException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.ReadOnlyException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

/**
 *
 * @author cailin
 */
@core
public class OAuth {

	public static String docs() {
		return "This class provides methods for interfacing with OAuth providers.";
	}

	@api
	@hide("experimental")
	@noboilerplate
	public static class x_get_oauth_token extends AbstractFunction {

		public static class OAuthOptions {
			public final String authorizationUrl;
			public final String clientId;
			public final String clientSecret;
			public final String scope;
			public final String tokenUrl;
			public String successText;
			public Map<String, String> extraHeaders;
			public String refreshToken;
			public Integer forcePort;

			public OAuthOptions(String authorizationUrl, String clientId, String clientSecret, String scope, String tokenUrl) {
				this.authorizationUrl = authorizationUrl;
				this.clientId = clientId;
				this.clientSecret = clientSecret;
				this.scope = scope;
				this.tokenUrl = tokenUrl;
			}

			CArray toOptionsArray() {
				CArray ret = CArray.GetAssociativeArray(Target.UNKNOWN);
				ret.set("authorizationUrl", authorizationUrl);
				ret.set("clientId", clientId);
				ret.set("scope", scope);
				ret.set("tokenUrl", tokenUrl);
				ret.set("clientSecret", clientSecret == null ? "" : clientSecret);
				ret.set("successText", successText == null ? CNull.NULL : new CString(successText, Target.UNKNOWN),
						Target.UNKNOWN);
				if(extraHeaders != null) {
					CArray eh = CArray.GetAssociativeArray(Target.UNKNOWN);
					for(Map.Entry<String, String> e : extraHeaders.entrySet()) {
						eh.set(e.getKey(), e.getValue());
					}
					ret.set("extraHeaders", eh, Target.UNKNOWN);
				}
				if(refreshToken != null) {
					ret.set("refreshToken", refreshToken);
				}
				if(forcePort != null) {
					ret.set("forcePort", new CInt(forcePort, Target.UNKNOWN), Target.UNKNOWN);
				}
				return ret;
			}
		}

		public static String execute(GlobalEnv gEnv, OAuthOptions options) {
			Environment env = Environment.createEnvironment(gEnv);
			return new x_get_oauth_token().exec(Target.UNKNOWN, env, options.toOptionsArray()).val();
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, com.laytonsmith.core.environments.Environment env, Mixed... args) throws ConfigRuntimeException {
			// TODO: Make this part support profiles
			CArray options = Static.getArray(args[0], t);
			String authorizationUrl = options.get("authorizationUrl", t).val();
			String tokenUrl = options.get("tokenUrl", t).val();
			String clientId = options.get("clientId", t).val();
			String clientSecret = options.get("clientSecret", t).val();
			String scope = options.get("scope", t).val();
			String successText = Construct.nval(options.get("successText", t));
			CArray extraHeaders1 = null;
			if(options.containsKey("extraHeaders")) {
				extraHeaders1 = Static.getArray(options.get("extraHeaders", t), t);
			}
			Map<String, String> extraHeaders = new HashMap<>();
			if(extraHeaders1 != null) {
				for(String key : extraHeaders1.stringKeySet()) {
					extraHeaders.put(key, extraHeaders1.get(key, t).val());
				}
			}
			Integer forcePort = null;
			if(options.containsKey("forcePort")) {
				forcePort = ArgumentValidation.getInt32(options.get("forcePort", t), t);
			}
			try { // Persistence errors
				String accessToken = getAccessToken(env, clientId);
				if(accessToken == null) {
					try {
						if(options.containsKey("refreshToken")) {
							String refreshToken = options.get("refreshToken", t).val();
							storeRefreshToken(env, clientId, refreshToken);
						}
						String refreshToken;
						if(!hasRefreshToken(env, clientId)) {
							MutableObject<String> lock = startServer(successText, forcePort);
							String redirectUrl;
							synchronized(lock) {
								if(lock.getObject() == null) {
									lock.wait();
								}
								redirectUrl = lock.getObject();
								lock.setObject(null);
							}
							String requestURI = generateRequestURI(authorizationUrl, clientId, scope, redirectUrl,
									extraHeaders);
							new XGUI.x_launch_browser().exec(t, env, new CString(requestURI, t));
							synchronized(lock) {
								if(lock.getObject() == null) {
									lock.wait();
								}
							}
							String authorizationCode = lock.getObject();
							RequestSettings settings = new RequestSettings();
							settings.setFollowRedirects(true);
							settings.setMethod(HTTPMethod.POST);
							settings.setBlocking(true);
							Map<String, String> tokenParameters = new HashMap<>();
							tokenParameters.put("client_id", clientId);
							tokenParameters.put("client_secret", clientSecret);
							tokenParameters.put("code", authorizationCode);
							tokenParameters.put("grant_type", "authorization_code");
							tokenParameters.put("redirect_uri", redirectUrl);
							Map<String, List<String>> headers = new HashMap<>();
							headers.put("Accept",
									Arrays.asList("application/json", "application/x-www-form-urlencoded"));
							settings.setHeaders(headers);
							settings.setParameters(tokenParameters);
							HTTPResponse tokenResponse = WebUtility.GetPage(new URL(tokenUrl), settings);
							String response = tokenResponse.getContentAsString();
							String responseType = tokenResponse.getHeaderObject().getContentType().mimeType.mediaType;
							switch(responseType) {
								case "application/json": {
									CArray tokenJson = (CArray) new DataTransformations.json_decode()
											.exec(t, env, new CString(tokenResponse.getContentAsString(), t));
									if(tokenJson.containsKey("refresh_token")) {
										storeRefreshToken(env, clientId, tokenJson.get("refresh_token", t).val());
									}
									accessToken = tokenJson.get("access_token", t).val();
									int expiresIn;
									if(tokenJson.containsKey("expires_in")) {
										expiresIn = Static.getInt32(tokenJson.get("expires_in", t), t) * 1000;
									} else {
										expiresIn = Integer.MAX_VALUE;
									}
									storeAccessToken(env, clientId, new AccessToken(accessToken, expiresIn));
									break;
								}
								case "application/x-www-form-urlencoded": {
									Map<String, String> resp = WebUtility.getQueryMap(response);
									accessToken = resp.get("access_token");
									int expiresIn = Integer.parseInt(resp.get("expires_in")) * 1000;
									storeAccessToken(env, clientId, new AccessToken(accessToken, expiresIn));
									break;
								}
								default:
									throw new CREIOException("Received unsupported response from server of type "
											+ responseType + ": " + response, t);
							}
						}
						if(accessToken == null) {
							refreshToken = getRefreshToken(env, clientId);
							RequestSettings settings = new RequestSettings();
							settings.setFollowRedirects(true);
							settings.setMethod(HTTPMethod.POST);
							settings.setBlocking(true);
							Map<String, String> tokenParameters = new HashMap<>();
							tokenParameters.put("client_id", clientId);
							tokenParameters.put("client_secret", clientSecret);
							tokenParameters.put("refresh_token", refreshToken);
							tokenParameters.put("grant_type", "refresh_token");
							settings.setParameters(tokenParameters);
							HTTPResponse tokenResponse = WebUtility.GetPage(new URL(tokenUrl), settings);
							CArray tokenJson = (CArray) new DataTransformations.json_decode().exec(t, env, new CString(tokenResponse.getContentAsString(), t));
							accessToken = tokenJson.get("access_token", t).val();
							storeAccessToken(env, clientId, new AccessToken(accessToken, Static.getInt32(tokenJson.get("expires_in", t), t) * 1000));
						}
					} catch (InterruptedException ex) {
						return CNull.NULL;
					} catch (OAuthSystemException ex) {
						// TODO
						throw new CREOAuthException(ex.getMessage(), t, ex);
					} catch (MalformedURLException ex) {
						throw new CREFormatException(ex.getMessage(), t, ex);
					} catch (IOException ex) {
						throw new CREIOException(ex.getMessage(), t, ex);
					}
				}
				return new CString(accessToken, t);
			} catch (DataSourceException ex) {
				throw new CREIOException(ex.getMessage(), t, ex);
			} catch (ReadOnlyException ex) {
				throw new CREReadOnlyException(ex.getMessage(), t, ex);
			}
		}

		private static class AccessToken {

			private final String accessToken;
			private final Date expiry;

			public AccessToken(String accessToken, int expiresIn) {
				this.accessToken = accessToken;
				this.expiry = new Date((System.currentTimeMillis()) + expiresIn);
			}

			public AccessToken(String accessToken, Date expiresOn) {
				this.accessToken = accessToken;
				this.expiry = expiresOn;
			}

			@Override
			public String toString() {
				return accessToken + " " + expiry;
			}

			public String getAccessToken() {
				return this.accessToken;
			}

			public Date getExpiry() {
				return this.expiry;
			}

		}

		private static boolean hasRefreshToken(Environment env, String clientId) throws DataSourceException {
			PersistenceNetwork pn = env.getEnv(GlobalEnv.class).GetPersistenceNetwork();
			return pn.hasKey(new String[]{"oauth", getFormattedClientId(clientId), "refreshToken"});
		}

		private static void storeRefreshToken(Environment env, String clientId, String refreshToken) throws DataSourceException, ReadOnlyException, IOException {
			PersistenceNetwork pn = env.getEnv(GlobalEnv.class).GetPersistenceNetwork();
			DaemonManager dm = env.getEnv(GlobalEnv.class).GetDaemonManager();
			pn.set(dm, new String[]{"oauth", getFormattedClientId(clientId), "refreshToken"}, formatValue(refreshToken));
		}

		private static void storeAccessToken(Environment env, String clientId, AccessToken token) throws DataSourceException, ReadOnlyException, IOException {
			PersistenceNetwork pn = env.getEnv(GlobalEnv.class).GetPersistenceNetwork();
			DaemonManager dm = env.getEnv(GlobalEnv.class).GetDaemonManager();
			pn.set(dm, new String[]{"oauth", getFormattedClientId(clientId), "accessToken"},
					formatValue(token.getExpiry().getTime() + "," + token.getAccessToken()));
		}

		/**
		 * Retrieves the access token from cache. If it is expired (or isn't there at all), it deletes it from cache and
		 * returns null, indicating that a new token is required.
		 *
		 * @return
		 */
		private String getAccessToken(Environment env, String clientId) throws DataSourceException {
			PersistenceNetwork pn = env.getEnv(GlobalEnv.class).GetPersistenceNetwork();
			AccessToken aT = null;
			String aTS = pn.get(new String[]{"oauth", getFormattedClientId(clientId), "accessToken"});
			if(aTS != null) {
				String[] aTSA = unformatValue(aTS).split(",", 2);
				aT = new AccessToken(aTSA[1], new Date(Long.parseLong(aTSA[0])));
			}
			if(aT == null) {
				// No key exists in the first place
				return null;
			}
			if(aT.getExpiry().after(new Date())) {
				// The key is not expired, so return it now
				return aT.getAccessToken();
			} else {
				// The key is expired.
				return null;
			}
		}

		private static String getRefreshToken(Environment env, String clientId) throws DataSourceException {
			PersistenceNetwork pn = env.getEnv(GlobalEnv.class).GetPersistenceNetwork();
			String v = pn.get(new String[]{"oauth", getFormattedClientId(clientId), "refreshToken"});
			if(v == null) {
				return v;
			} else {
				return unformatValue(v);
			}
		}

		public static String getFormattedClientId(String clientId) {
			return clientId.replaceAll("[^a-zA-Z0-9_\\.]", "");
		}

		private static String formatValue(String value) {
			try {
				return Construct.json_encode(new CString(value, Target.UNKNOWN), Target.UNKNOWN);
			} catch (MarshalException ex) {
				throw new RuntimeException(ex);
			}
		}

		private static String unformatValue(String pnVersion) {
			try {
				return Construct.json_decode(pnVersion, Target.UNKNOWN).val();
			} catch (MarshalException ex) {
				throw new RuntimeException(ex);
			}
		}

		private static MutableObject<String> startServer(String successText1, Integer forcePort) {
			final String successText;
			if(successText1 == null) {
				successText = "OAuth request successful. You may now close your browser window.";
			} else {
				successText = successText1;
			}
			final MutableObject<String> ret = new MutableObject<>(null);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Map<String, String> query;
						try(ServerSocket s = new ServerSocket(forcePort == null ? 0 : forcePort)) {
							ret.setObject("http://localhost:" + (forcePort == null ? s.getLocalPort() : forcePort));
							synchronized(ret) {
								ret.notifyAll();
							}
							try(Socket ss = s.accept()) {
								InputStream is = new BufferedInputStream(ss.getInputStream());
								BufferedReader input = new BufferedReader(new InputStreamReader(is));
								List<String> headers = new ArrayList<>();
								{
									String line;
									while((line = input.readLine()) != null) {
										if("".equals(line)) {
											break;
										}
										headers.add(line);
									}
								}
								int contentLength = 0;
								for(String header : headers) {
									if(header.matches("(?i)content-length.*")) {
										contentLength = Integer.parseInt(header.split(":")[1].trim());
									}
								}
								char[] cbuf = new char[contentLength];
								input.read(cbuf);
								ss.shutdownInput();
								String body = new String(cbuf);
								String queryS = headers.get(0).split(" ")[1];
								queryS = queryS.split("\\?", 2)[1];
								query = WebUtility.getQueryMap(queryS);
								OutputStreamWriter os = new OutputStreamWriter(new BufferedOutputStream(ss.getOutputStream()));
								String style = "p {font-family: Helvetica,sans-serif;text-align: center;margin: 10em;"
										+ "background-color: rgb(103, 209, 232);padding: 2em;border-radius: 1em;"
										+ "font-size: 14pt;}";
								String script = "window.open('', '_self').close();";
								String content = "<html><head><style type=\"text/css\">" + style
										+ "</style><title>OAuth Successful</title><script type=\"text/javascript\">"
										+ script + "</script></head><body><p>" + successText + "</p></body></html>";
								os.append("HTTP/1.0 200 OK\r\n");
								os.append("Content-Length: " + content.length() + "\r\n");
								os.append("Connection: close\r\n");
								os.append("Content-Type: text/html\r\n");
								os.append("\r\n");
								os.append(content);
								os.flush();
								ss.shutdownOutput();
							}
						}

						ret.setObject(query.get("code"));
						synchronized(ret) {
							ret.notifyAll();
						}
					} catch (IOException ex) {
						ex.printStackTrace(System.err);
					}
				}
			}, "oauth-callback-" + UUID.randomUUID()).start();
			return ret;
		}

		public static String generateRequestURI(String authorizationLocation, String clientId, String scope,
				String redirectUrl, Map<String, String> extraHeaders) throws OAuthSystemException {
			OAuthClientRequest req;
			OAuthClientRequest.AuthenticationRequestBuilder requestBuilder = OAuthClientRequest
					.authorizationLocation(authorizationLocation)
					.setClientId(clientId)
					.setScope(scope)
					.setRedirectURI(redirectUrl);
			for(String key : extraHeaders.keySet()) {
				requestBuilder.setParameter(key, extraHeaders.get(key));
			}
			requestBuilder.setParameter("response_type", "code");
			req = requestBuilder.buildQueryMessage();
			return req.getLocationUri();
		}

		@Override
		public String getName() {
			return "x_get_oauth_token";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "void {}";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

	}

	@api
	public static class clear_oauth_tokens extends AbstractFunction {

		/**
		 *
		 * @param gEnv
		 * @param clientId If set, clears just the one client id, if null, clears all tokens.
		 */
		public static void execute(GlobalEnv gEnv, String clientId) {
			Mixed[] args = new Mixed[0];
			if(clientId != null) {
				args = new Mixed[]{new CString(clientId, Target.UNKNOWN)};
			}
			Environment env = Environment.createEnvironment(gEnv);
			new clear_oauth_tokens().exec(Target.UNKNOWN, env, args);
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			PersistenceNetwork pn = environment.getEnv(GlobalEnv.class).GetPersistenceNetwork();
			String namespace = "oauth";
			if(args.length >= 1) {
				namespace += "." + x_get_oauth_token.getFormattedClientId(args[0].val());
			}
			DaemonManager dm = environment.getEnv(GlobalEnv.class).GetDaemonManager();
			try {
				Map<String[], String> list = pn.getNamespace(namespace.split("\\."));
				for(String[] key : list.keySet()) {
					pn.clearKey(dm, key);
				}
			} catch (DataSourceException | IOException ex) {
				throw new CREIOException(ex.getMessage(), t, ex);
			} catch (IllegalArgumentException ex) {
				throw new CREFormatException(ex.getMessage(), t, ex);
			} catch (ReadOnlyException ex) {
				throw new CREReadOnlyException(ex.getMessage(), t, ex);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "clear_oauth_tokens";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[clientId]} Clears the oauth tokens (refresh token and access token) for the given client ID."
					+ " If the client ID is not specified, all tokens are deleted. This is useful if various oath tokens"
					+ " have been revoked, or you would specifically like to prevent caching of those tokens.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

	}
}
