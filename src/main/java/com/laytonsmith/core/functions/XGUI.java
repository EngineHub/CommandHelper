package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.Common.OSUtils.OS;
import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.Web.HTTPMethod;
import com.laytonsmith.PureUtilities.Web.HTTPResponse;
import com.laytonsmith.PureUtilities.Web.RequestSettings;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREOAuthException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

/**
 *
 */
@core
public class XGUI {

    public static String docs() {
	return "This provides extremely limited gui control functions. This entire class is experimental, and will probably be removed at"
		+ " some point.";
    }

    private static Map<Integer, Window> windows = new HashMap<>();
    private static AtomicInteger windowIDs = new AtomicInteger(0);

    static {
	StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

	    @Override
	    public void run() {
		for (Window w : windows.values()) {
		    w.dispose();
		}
		windows.clear();
	    }
	});
    }

    @api
    @hide("experimental")
    @noboilerplate
    public static class x_create_window extends AbstractFunction {

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
	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    JFrame frame = new JFrame();
	    int id = windowIDs.incrementAndGet();
	    String title = "";
	    int width = 300;
	    int height = 300;
	    if (args.length > 0) {
		title = args[0].val();
	    }
	    if (args.length > 1) {
		width = Static.getInt32(args[1], t);
	    }
	    if (args.length > 2) {
		height = Static.getInt32(args[2], t);
	    }
	    frame.setTitle(title);
	    frame.setSize(width, height);
	    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	    JPanel panel = new JPanel();
	    frame.add(panel);
	    windows.put(id, frame);
	    return new CInt(id, t);
	}

	@Override
	public String getName() {
	    return "x_create_window";
	}

	@Override
	public Integer[] numArgs() {
	    return new Integer[]{0, 1, 2, 3};
	}

	@Override
	public String docs() {
	    return "int {[title], [width], [height]} Creates a window with the specified title, width and height. All are optional"
		    + " parameters, and they default to reasonable defaults. The id, which represents the window can be used for "
		    + " manipulating the window in future calls. The contents of the window will be blank. The window will initially"
		    + " not be visible. You'll need to call x_show_window to make it visible.";
	}

	@Override
	public Version since() {
	    return CHVersion.V0_0_0;
	}

    }

    @api
    @hide("Expreimental")
    @noboilerplate
    public static class x_show_window extends AbstractFunction {

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
	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    int id = Static.getInt32(args[0], t);
	    boolean show = true;
	    if (args.length > 1) {
		show = Static.getBoolean(args[1]);
	    }
	    Window w = windows.get(id);
	    w.setVisible(show);
	    return CVoid.VOID;
	}

	@Override
	public String getName() {
	    return "x_show_window";
	}

	@Override
	public Integer[] numArgs() {
	    return new Integer[]{1, 2};
	}

	@Override
	public String docs() {
	    return "void {windowID, [show]} Shows (or hides, if \"show\" is false) the specified window.";
	}

	@Override
	public Version since() {
	    return CHVersion.V0_0_0;
	}

    }

    @api
    @hide("experimental")
    @noboilerplate
    public static class x_set_window_pixel extends AbstractFunction {

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
	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    int windowID = Static.getInt32(args[0], t);
	    int x = Static.getInt32(args[1], t);
	    int y = Static.getInt32(args[2], t);
	    int red = Static.getInt32(args[3], t);
	    int green = Static.getInt32(args[4], t);
	    int blue = Static.getInt32(args[5], t);
	    Window w = windows.get(windowID);
	    while (true) {
		try {
		    JPanel panel = (JPanel) w.findComponentAt(x, y);
		    panel.getGraphics().setColor(new Color(red, green, blue));
		    panel.getGraphics().draw3DRect(x, y, 1, 1, true);
		    return CVoid.VOID;
		} catch (ClassCastException ex) {
		    //?
		    return CVoid.VOID;
		}
	    }
	}

	@Override
	public String getName() {
	    return "x_set_window_pixel";
	}

	@Override
	public Integer[] numArgs() {
	    return new Integer[]{6};
	}

	@Override
	public String docs() {
	    return "void {windowID, x, y, red, green, blue} Sets a pixel in the specified window. x and y are relative to the top"
		    + " left of the window.";
	}

	@Override
	public Version since() {
	    return CHVersion.V0_0_0;
	}

    }

    @api
    @hide("experimental")
    @noboilerplate
    public static class x_launch_browser extends AbstractFunction {

	@Override
	public Class<? extends CREThrowable>[] thrown() {
	    return new Class[]{CREIOException.class};
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
	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    String url = args[0].val();
	    try {
		if (Desktop.isDesktopSupported()) {
		    Desktop.getDesktop().browse(new URI(url));
		}
	    } catch (URISyntaxException ex1) {
		throw new CREFormatException(ex1.getMessage(), t);
	    } catch (IOException ex) {
		try {
		    // Last ditch effort
		    Runtime rt = Runtime.getRuntime();
		    switch (OSUtils.GetOS()) {
			case WINDOWS:
			    rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
			    break;
			case MAC:
			    rt.exec("open " + url);
			    break;
			case LINUX:
			default:
			    // Try the other OSes as linux
			    String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror",
				"netscape", "opera", "links", "lynx"};

			    StringBuilder cmd = new StringBuilder();
			    for (int i = 0; i < browsers.length; i++) {
				cmd.append(i == 0 ? "" : " || ").append(browsers[i]).append(" \"").append(url).append("\" ");
			    }

			    rt.exec(new String[]{"sh", "-c", cmd.toString()});
			    break;
		    }
		} catch (IOException ex1) {
		    throw new CREIOException(ex1.getMessage(), t, ex1);
		}
	    }
	    return CVoid.VOID;
	}

	@Override
	public String getName() {
	    return "x_launch_browser";
	}

	@Override
	public Integer[] numArgs() {
	    return new Integer[]{1};
	}

	@Override
	public String docs() {
	    return "void {url} Launches the desktop's default browser with the given url. On headless systems, this"
		    + " will throw an exception.";
	}

	@Override
	public Version since() {
	    return CHVersion.V3_3_2;
	}

    }

    @api
    @hide("experimental")
    @noboilerplate
    public static class x_get_oauth_token extends AbstractFunction {

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
	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
	    // TODO: Make this part support profiles
	    CArray options = Static.getArray(args[0], t);
	    String authorizationUrl = options.get("authorizationUrl", t).val();
	    String tokenUrl = options.get("tokenUrl", t).val();
	    String clientId = options.get("clientId", t).val();
	    String clientSecret = options.get("clientSecret", t).val();
	    String scope = options.get("scope", t).val();
	    String successText = options.get("successText", t).nval();
	    CArray extraHeaders1 = null;
	    if (options.containsKey("extraHeaders")) {
		extraHeaders1 = Static.getArray(options.get("extraHeaders", t), t);
	    }
	    Map<String, String> extraHeaders = new HashMap<>();
	    if (extraHeaders1 != null) {
		for (String key : extraHeaders1.stringKeySet()) {
		    extraHeaders.put(key, extraHeaders1.get(key, t).val());
		}
	    }
	    String accessToken = getAccessToken();
	    if (accessToken == null) {
		try {
		    String refreshToken;
		    if (!hasRefreshToken()) {
			MutableObject<String> lock = startServer(successText);
			String redirectUrl;
			synchronized (lock) {
			    if (lock.getObject() == null) {
				lock.wait();
			    }
			    redirectUrl = lock.getObject();
			    lock.setObject(null);
			}
			String requestURI = generateRequestURI(authorizationUrl, clientId, scope, redirectUrl,
				extraHeaders);
			new x_launch_browser().exec(t, environment, new CString(requestURI, t));
			synchronized (lock) {
			    if (lock.getObject() == null) {
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
			settings.setParameters(tokenParameters);
			HTTPResponse tokenResponse = WebUtility.GetPage(new URL(tokenUrl), settings);
			CArray tokenJson = (CArray) new DataTransformations.json_decode().exec(t, environment, new CString(tokenResponse.getContent(), t));
			storeRefreshToken(tokenJson.get("refresh_token", t).val());
			accessToken = tokenJson.get("access_token", t).val();
			storeAccessToken(new AccessToken(accessToken, Static.getInt32(tokenJson.get("expires_in", t), t)));
		    }
		    if (accessToken == null) {
			refreshToken = getRefreshToken();
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
			CArray tokenJson = (CArray) new DataTransformations.json_decode().exec(t, environment, new CString(tokenResponse.getContent(), t));
			storeAccessToken(new AccessToken(tokenJson.get("access_token", t).val(), Static.getInt32(tokenJson.get("expires_in", t), t)));
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
	}

	private static class AccessToken {

	    private final String accessToken;
	    private final Date expiry;

	    public AccessToken(String accessToken, int expiresIn) {
		this.accessToken = accessToken;
		this.expiry = new Date((System.currentTimeMillis() / 1000) + expiresIn);
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

	//TODO: Delete these
	private static String refreshToken = null;
	private static AccessToken aT = null;

	private static boolean hasRefreshToken() {
	    //TODO
	    return refreshToken != null;
	}

	private static void storeRefreshToken(String requestToken) {
	    //TODO
	    x_get_oauth_token.refreshToken = requestToken;
	}

	private static void storeAccessToken(AccessToken token) {
	    //TODO
	    aT = token;
	}

	/**
	 * Retrieves the access token from cache. If it is expired (or isn't there at all), it deletes it from cache and
	 * returns null, indicating that a new token is required.
	 *
	 * @return
	 */
	private String getAccessToken() {
	    //TODO
	    if (aT == null) {
		return null;
	    }
	    if (aT.getExpiry().before(new Date())) {
		return aT.getAccessToken();
	    } else {
		return null;
	    }
	}

	private static String getRefreshToken() {
	    //TODO
	    return x_get_oauth_token.refreshToken;
	}

	private static MutableObject<String> startServer(String successText1) {
	    final String successText;
	    if (successText1 == null) {
		successText = "OAuth request successful. You may now close your browser window.";
	    } else {
		successText = successText1;
	    }
	    final MutableObject<String> ret = new MutableObject<>(null);
	    new Thread(new Runnable() {
		@Override
		public void run() {
		    try {
			ServerSocket s = new ServerSocket(0);
			ret.setObject("http://localhost:" + s.getLocalPort());
			synchronized (ret) {
			    ret.notifyAll();
			}
			Socket ss = s.accept();
			InputStream is = new BufferedInputStream(ss.getInputStream());
			BufferedReader input = new BufferedReader(new InputStreamReader(is));
			List<String> headers = new ArrayList<>();
			while (input.ready()) {
			    String line = input.readLine();
			    if ("".equals(line)) {
				break;
			    }
			    headers.add(line);
			}
			int contentLength = 0;
			for (String header : headers) {
			    if (header.matches("(?i)content-length.*")) {
				contentLength = Integer.parseInt(header.split(":")[1].trim());
			    }
			}
			char[] cbuf = new char[contentLength];
			input.read(cbuf);
			ss.shutdownInput();
			String body = new String(cbuf);
			String queryS = headers.get(0).split(" ")[1];
			queryS = queryS.split("\\?", 2)[1];
			Map<String, String> query = WebUtility.getQueryMap(queryS);
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
			ss.close();
			s.close();

			ret.setObject(query.get("code"));
			synchronized (ret) {
			    ret.notifyAll();
			}
		    } catch (IOException ex) {
			ex.printStackTrace();
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
	    for (String key : extraHeaders.keySet()) {
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
	    return CHVersion.V3_3_2;
	}

    }

}
