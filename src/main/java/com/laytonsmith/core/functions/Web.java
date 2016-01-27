package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.Web.Cookie;
import com.laytonsmith.PureUtilities.Web.CookieJar;
import com.laytonsmith.PureUtilities.Web.HTTPMethod;
import com.laytonsmith.PureUtilities.Web.HTTPResponse;
import com.laytonsmith.PureUtilities.Web.RequestSettings;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.EmailProfile;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 */
@core
public class Web {
	public static String docs(){
		return "Contains various methods to make HTTP requests.";
	}

	private static void getCookieJar(CArray arrayJar, CookieJar cookieJar, Target t){
		CArray ret = arrayJar;
		for(Cookie cookie : cookieJar.getAllCookies()){
			boolean update = false;
			CArray aCookie = null;
			for(Construct ac : arrayJar.asList()){
				aCookie = Static.getArray(ac, t);
				if(cookie.getName().equals(aCookie.get("name", t).val())
						&& cookie.getDomain().equals(aCookie.get("domain", t).val())
						&& cookie.getPath().equals(aCookie.get("path", t).val())){
					//This is just an update, not a new cookie
					update = true;
					break;
				}
			}
			CArray c;
			if(!update){
				c = CArray.GetAssociativeArray(t);
			} else {
				c = aCookie;
			}
			c.set("name", cookie.getName());
			c.set("value", cookie.getValue());
			c.set("domain", cookie.getDomain());
			c.set("path", cookie.getPath());
			c.set("expiration", new CInt(cookie.getExpiration(), t), t);
			c.set("httpOnly", CBoolean.get(cookie.isHttpOnly()), t);
			c.set("secureOnly", CBoolean.get(cookie.isSecureOnly()), t);
			if(!update){
				ret.push(c, t);
			}
		}
	}

	private static CookieJar getCookieJar(CArray cookieJar, Target t){
		CookieJar ret = new CookieJar();
		for(String key : cookieJar.stringKeySet()){
			CArray cookie = Static.getArray(cookieJar.get(key, t), t);
			String name;
			String value;
			String domain;
			String path;
			long expiration = 0;
			boolean httpOnly = false;
			boolean secureOnly = false;
			if(cookie.containsKey("name") && cookie.containsKey("value")
					&& cookie.containsKey("domain") && cookie.containsKey("path")){
				name = cookie.get("name", t).val();
				value = cookie.get("value", t).val();
				domain = cookie.get("domain", t).val();
				path = cookie.get("path", t).val();
			} else {
				throw ConfigRuntimeException.BuildException("The name, value, domain, and path keys are required"
						+ " in all cookies.", CREFormatException.class, t);
			}
			if(cookie.containsKey("expiration")){
				expiration = Static.getInt(cookie.get("expiration", t), t);
			}
			if(cookie.containsKey("httpOnly")){
				httpOnly = Static.getBoolean(cookie.get("httpOnly", t));
			}
			if(cookie.containsKey("secureOnly")){
				secureOnly = Static.getBoolean(cookie.get("secureOnly", t));
			}
			Cookie c = new Cookie(name, value, domain, path, expiration, httpOnly, secureOnly);
			ret.addCookie(c);
		}
		return ret;
	}

	@api
	public static class http_request extends AbstractFunction {
		/**
		 * Defines the max number of HTTP threads that will run at any given time.
		 * Each web_request uses one thread in this pool, and so should not be used
		 * for long running requests, as other requests would be starved.
		 */
		private static final int MAX_HTTP_THREADS = 3;
		private static int threadCount = 0;
		private static final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_HTTP_THREADS, new ThreadFactory() {


			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, Implementation.GetServerType().getBranding() + "-web-request-" + (threadCount++));
			}
		});

		private static final Map<String, String> DEFAULT_HEADERS = new HashMap<String, String>();
		static{
			DEFAULT_HEADERS.put("Accept", "text/*, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8");
			DEFAULT_HEADERS.put("Accept-Encoding", "gzip, deflate, identity");
			DEFAULT_HEADERS.put("User-Agent", "Java/" + System.getProperty("java.version") + "/" + Implementation.GetServerType().getBranding());
			DEFAULT_HEADERS.put("DNT", "1");
			DEFAULT_HEADERS.put("Connection", "close");
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
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
		public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
			final URL url;
			try {
				url = new URL(args[0].val());
			} catch (MalformedURLException ex) {
				throw new CREFormatException(ex.getMessage(), t);
			}
			final RequestSettings settings = new RequestSettings();
			final CClosure success;
			final CClosure error;
			final CArray arrayJar;
			if(args[1] instanceof CClosure){
				success = (CClosure) args[1];
				error = null;
				arrayJar = null;
			} else {
				CArray csettings = Static.getArray(args[1], t);
				if(csettings.containsKey("method")){
					try{
						settings.setMethod(HTTPMethod.valueOf(csettings.get("method", t).val()));
					} catch(IllegalArgumentException e){
						throw new CREFormatException(e.getMessage(), t);
					}
				}
				boolean useDefaultHeaders = true;
				if(csettings.containsKey("useDefaultHeaders")){
					useDefaultHeaders = Static.getBoolean(csettings.get("useDefaultHeaders", t));
				}
				if(csettings.containsKey("headers") && !(csettings.get("headers", t) instanceof CNull)){
					CArray headers = Static.getArray(csettings.get("headers", t), t);
					Map<String, List<String>> mheaders = new HashMap<String, List<String>>();
					for(String key : headers.stringKeySet()){
						List<String> h = new ArrayList<String>();
						Construct c = headers.get(key, t);
						if(c instanceof CArray){
							for(String kkey : ((CArray)c).stringKeySet()){
								h.add(((CArray)c).get(kkey, t).val());
							}
						} else {
							h.add(c.val());
						}
						mheaders.put(key, h);
					}
					settings.setHeaders(mheaders);
				} else {
					settings.setHeaders(new HashMap<String, List<String>>());
				}
				if(useDefaultHeaders){
					outer: for(String key : DEFAULT_HEADERS.keySet()){
						for(String k2 : settings.getHeaders().keySet()){
							if(key.equalsIgnoreCase(k2)){
								//They have already included this header, so let's not touch it.
								continue outer;
							}
						}
						//Not found, so add ours
						settings.getHeaders().put(key, Arrays.asList(DEFAULT_HEADERS.get(key)));
					}
				}
				if(csettings.containsKey("params") && !(csettings.get("params", t) instanceof CNull)){
					if(csettings.get("params", t) instanceof CArray){
						CArray params = Static.getArray(csettings.get("params", t), t);
						Map<String, List<String>> mparams = new HashMap<String, List<String>>();
						for(String key : params.stringKeySet()){
							Construct c = params.get(key, t);
							List<String> l = new ArrayList<String>();
							if(c instanceof CArray){
								for(String kkey : ((CArray)c).stringKeySet()){
									l.add(((CArray)c).get(kkey, t).val());
								}
							} else {
								l.add(c.val());
							}
							mparams.put(key, l);
						}
						settings.setComplexParameters(mparams);
					} else {
						settings.setRawParameter(csettings.get("params", t).val());
						if(settings.getMethod() != HTTPMethod.POST && settings.getMethod() != HTTPMethod.PUT){
							throw new CREFormatException("You must set the method to POST or PUT to use raw params.", t);
						}
					}
				}
				if(csettings.containsKey("cookiejar") && !(csettings.get("cookiejar", t) instanceof CNull)){
					arrayJar = Static.getArray(csettings.get("cookiejar", t), t);
					settings.setCookieJar(getCookieJar(arrayJar, t));
				} else {
					arrayJar = null;
				}
				if(csettings.containsKey("followRedirects")){
					settings.setFollowRedirects(Static.getBoolean(csettings.get("followRedirects", t)));
				}
				//Only required parameter
				if(csettings.containsKey("success")){
					if(csettings.get("success", t) instanceof CClosure){
						success = (CClosure) csettings.get("success", t);
					} else {
						throw new CRECastException("Expecting the success parameter to be a closure.", t);
					}
				} else {
					throw ConfigRuntimeException.BuildException("Missing the success parameter, which is required.", CRECastException.class, t);
				}
				if(csettings.containsKey("error")){
					if(csettings.get("error", t) instanceof CClosure){
						error = (CClosure) csettings.get("error", t);
					} else {
						throw new CRECastException("Expecting the error parameter to be a closure.", t);
					}
				} else {
					error = null;
				}
				if(csettings.containsKey("timeout")){
					settings.setTimeout(Static.getInt32(csettings.get("timeout", t), t));
				}
				String username = null;
				String password = null;
				if(csettings.containsKey("username")){
					username = csettings.get("username", t).val();
				}
				if(csettings.containsKey("password")){
					password = csettings.get("password", t).val();
				}
				if(csettings.containsKey("proxy")){
					CArray proxySettings = Static.getArray(csettings.get("proxy", t), t);
					Proxy.Type type;
					String proxyURL;
					int port;
					try{
						type = Proxy.Type.valueOf(proxySettings.get("type", t).val());
					} catch(IllegalArgumentException e){
						throw ConfigRuntimeException.BuildException(e.getMessage(), CREFormatException.class, t, e);
					}
					proxyURL = proxySettings.get("url", t).val();
					port = Static.getInt32(proxySettings.get("port", t), t);
					SocketAddress addr = new InetSocketAddress(proxyURL, port);
					Proxy proxy = new Proxy(type, addr);
					settings.setProxy(proxy);
				}
				if(csettings.containsKey("download")){
					Construct download = csettings.get("download", t);
					if(download instanceof CNull){
						settings.setDownloadTo(null);
					} else {
						//TODO: Remove this check and tie into the VFS once that is complete.
						if(Static.InCmdLine(environment)){
							File file = new File(download.val());
							if(!file.isAbsolute()){
								file = new File(t.file(), file.getPath());
							}
							settings.setDownloadTo(file);
						}
					}
				}
				settings.setAuthenticationDetails(username, password);
			}
			environment.getEnv(GlobalEnv.class).GetDaemonManager().activateThread(null);
			threadPool.submit(new Runnable() {

				@Override
				public void run() {
					try{
						HTTPResponse resp = WebUtility.GetPage(url, settings);
						final CArray array = CArray.GetAssociativeArray(t);
						array.set("body", new CString(resp.getContent(), t), t);
						CArray headers = CArray.GetAssociativeArray(t);
						for(String key : resp.getHeaderNames()){
							CArray h = new CArray(t);
							for(String val : resp.getHeaders(key)){
								h.push(new CString(val, t), t);
							}
							headers.set(key, h, t);
						}
						array.set("headers", headers, t);
						array.set("responseCode", new CInt(resp.getResponseCode(), t), t);
						array.set("responseText", resp.getResponseText());
						array.set("httpVersion", resp.getHttpVersion());
						array.set("error", CBoolean.get(resp.getResponseCode() >= 400 && resp.getResponseCode() < 600), t);
						if(arrayJar != null){
							getCookieJar(arrayJar, settings.getCookieJar(), t);
						}
						StaticLayer.GetConvertor().runOnMainThreadLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {

							@Override
							public void run() {
								executeFinish(success, array, t, environment);
							}
						});
					} catch(IOException e){
						final ConfigRuntimeException ex = ConfigRuntimeException.BuildException((e instanceof UnknownHostException?"Unknown host: ":"")
								+ e.getMessage(), CREIOException.class, t);
						if(error != null){
							StaticLayer.GetConvertor().runOnMainThreadLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {

								@Override
								public void run() {
									executeFinish(error, ObjectGenerator.GetGenerator().exception(ex, environment, t), t, environment);
								}
							});
						} else {
							ConfigRuntimeException.HandleUncaughtException(ex, environment);
						}
					} catch(Exception e){
						e.printStackTrace();
					} finally {
						environment.getEnv(GlobalEnv.class).GetDaemonManager().deactivateThread(null);
					}
				}
			});
			return CVoid.VOID;
		}

		private void executeFinish(CClosure closure, Construct arg, Target t, Environment environment){
			try{
				closure.execute(new Construct[]{arg});
			} catch(FunctionReturnException e){
				//Just ignore this if it's returning void. Otherwise, warn.
				//TODO: Eventually, this should be taggable as a compile error
				if(!(e.getReturn() instanceof CVoid)){
					CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, LogLevel.WARNING, "Returning a value from the closure. The value is"
							+ " being ignored.", t);
				}
			} catch(ProgramFlowManipulationException e){
				//This is an error
				CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, LogLevel.WARNING, "Only return may be used inside the closure.", t);
			} catch(ConfigRuntimeException e){
				ConfigRuntimeException.HandleUncaughtException(e, environment);
			} catch(Throwable e){
				//Other throwables we just need to report
				CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, LogLevel.ERROR, "An unexpected exception has occurred. No extra"
						+ " information is available, but please report this error:\n" + StackTraceUtils.GetStacktrace(e), t);
			}
		}

		@Override
		public String getName() {
			return "http_request";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			Map<String, DocGenTemplates.Generator> templates = new HashMap<>();
			templates.put("enum", new DocGenTemplates.Generator() {

				@Override
				public String generate(String... args) {
					if("HTTPMethod".equals(args[0])){
						return StringUtils.Join(HTTPMethod.values(), ", ");
					} else {
						return "";
					}
				}
			});
			templates.put("CODE", DocGenTemplates.CODE);
			return super.getBundledDocs(templates);
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Getting headers from a website", "http_request('http://www.google.com', array(\n"
					+ "\tsuccess: closure(@response,\n"
					+ "\t\tmsg(@response['headers']['Server'][0])\n"
					+ "\t)\n"
					+ "));\n", "gws"),
				new ExampleScript("Using a cookie jar", "@cookiejar = array()\n"
					+ "http_request('http://www.google.com', array(\n"
					+ "\tcookiejar: @cookiejar, success: closure(@resp,\n"
					+ "\t\tmsg(@cookiejar)\n"
					+ "\t)\n"
					+ "));\n", "<cookie jar would now have cookies in it>"),
				new ExampleScript("Sending some json to the server",
						"http_request('http://example.com', array(\n"
								+ "\tmethod: 'POST',\n"
								+ "\theaders: array(\n"
								+ "\t\t// The content type isn't set automatically if we send a string via params,\n"
								+ "\t\t// so we have to set this manually to application/json here, since we're sending\n"
								+ "\t\t// json data. Other data types may have different MIME types.\n"
								+ "\t\t'Content-Type': 'application/json'\n"
								+ "\t),"
								+ "\tparams: json_encode(array(\n"
								+ "\t\t'arg1': 'value',\n"
								+ "\t\t'arg2': 'value',\n"
								+ "\t)),\n"
								+ "\tsuccess: closure(@response){\n"
								+ "\t\t// Handle the server's response\n"
								+ "\t}}"
								+ "));", "<A POST request with json data would be sent to the server>")
			};
		}

	}

	@api
	public static class http_clear_session_cookies extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray array = Static.getArray(args[0], t);
			CookieJar jar = getCookieJar(array, t);
			jar.clearSessionCookies();
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "http_clear_session_cookies";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {cookieJar} Clears out \"session\" cookies, that is cookies that weren't set with an expiration"
					+ " (which translates to 0 in an individual cookie).";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class url_encode extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try {
				return new CString(URLEncoder.encode(args[0].val(), "UTF-8"), t);
			} catch (UnsupportedEncodingException ex) {
				throw new Error();
			}
		}

		@Override
		public String getName() {
			return "url_encode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {param} URL Encodes the parameter given. This escapes all special characters per the"
					+ " x-www-form-urlencoded format.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "url_encode('A string with special characters: !@#$%^&*()-+')")
			};
		}

	}

	@api
	public static class url_decode extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try {
				return new CString(URLDecoder.decode(args[0].val(), "UTF-8"), t);
			} catch (UnsupportedEncodingException ex) {
				throw new Error();
			}
		}

		@Override
		public String getName() {
			return "url_decode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {param} Decodes a previously url encoded string.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "url_decode('A+string+with+special+characters%3A+%21%40%23%24%25%5E%26*%28%29-%2B')")
			};
		}

	}

	@api
	@noboilerplate
	public static class email extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREPluginInternalException.class, CREIOException.class};
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
			// Argument processing
			CArray options;
			if(args.length == 1){
				options = ArgumentValidation.getArray(args[0], t);
			} else {
				// Load the profile for transport data, if specified.
				String profileName = ArgumentValidation.getString(args[0], t);
				options = CArray.GetAssociativeArray(t);
				Profiles.Profile p;
				try {
					p = environment.getEnv(GlobalEnv.class).getProfiles().getProfileById(profileName);
				} catch(Profiles.InvalidProfileException ex){
					throw ConfigRuntimeException.BuildException(ex.getMessage(), CREFormatException.class, t, ex);
				}
				if(!(p instanceof EmailProfile)){
					throw ConfigRuntimeException.BuildException("Profile type is expected to be \"email\", but \"" + p.getType() + "\"  was found.", CRECastException.class, t);
				}
				Map<String, Object> data = ((EmailProfile)p).getMap();
				for(String key : data.keySet()){
					options.set(key, Construct.GetConstruct(data.get(key)), t);
				}
				// Override any transport data that was also specified in the options, as
				// well as adding the email settings here too.
				CArray options2 = ArgumentValidation.getArray(args[1], t);
				for(String key : options2.stringKeySet()){
					options.set(key, options2.get(key, t), t);
				}
			}


			// Transport options
			String host = ArgumentValidation.getItemFromArray(options, "host", t, new CString("localhost", t)).val();
			final String mailUser = ArgumentValidation.getItemFromArray(options, "user", t, new CString("", t)).val();
			final String mailPassword = ArgumentValidation.getItemFromArray(options, "password", t, new CString("", t)).val();
			int mailPort = ArgumentValidation.getInt32(ArgumentValidation.getItemFromArray(options, "port", t, new CInt(587, t)), t);
			boolean useSSL = ArgumentValidation.getBoolean(ArgumentValidation.getItemFromArray(options, "use_ssl", t, CBoolean.FALSE), t);
			boolean useStartTLS = ArgumentValidation.getBoolean(ArgumentValidation.getItemFromArray(options, "use_start_tls", t, CBoolean.FALSE), t);
			int timeout = ArgumentValidation.getInt32(ArgumentValidation.getItemFromArray(options, "timeout", t, new CInt(10000, t)), t);

			//Standard email options
			String from = ArgumentValidation.getItemFromArray(options, "from", t, null).val();
			String subject = ArgumentValidation.getItemFromArray(options, "subject", t, new CString("<No Subject>", t)).val();
			String body = ArgumentValidation.getItemFromArray(options, "body", t, new CString("", t)).val();
			Construct cto = ArgumentValidation.getItemFromArray(options, "to", t, null);
			CArray to;
			if(cto instanceof CString){
				to = new CArray(t);
				to.push(cto, t);
			} else {
				to = (CArray)cto;
			}
			CArray attachments = ArgumentValidation.getArray(ArgumentValidation.getItemFromArray(options, "attachments", t, new CArray(t)), t);

			// Setup and execution
			Properties properties = System.getProperties();
			properties.setProperty("mail.smtp.host", host);
			properties.setProperty("mail.smtp.port", Integer.toString(mailPort));
			properties.setProperty("mail.smtp.starttls.enable", Boolean.toString(useStartTLS));
			properties.setProperty("mail.smtp.ssl.enable", Boolean.toString(useSSL));
			if(timeout > 0){
				properties.setProperty("mail.smtp.connectiontimeout", Integer.toString(timeout));
				properties.setProperty("mail.smtp.timeout", Integer.toString(timeout));
			}


			properties.setProperty("mail.debug", Boolean.toString(Prefs.DebugMode()));

			if(!"".equals(mailUser)){
				properties.setProperty("mail.smtp.user", mailUser);
				properties.setProperty("mail.smtp.auth", "true");
			}
			if(!"".equals(mailPassword)){
				properties.setProperty("mail.smtp.password", mailPassword);
				properties.setProperty("mail.smtp.auth", "true");
			}

			Session session = Session.getInstance(properties, new Authenticator() {

				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(mailUser, mailPassword);
				}

			});

			try{
				MimeMessage message = new MimeMessage(session);

				message.setFrom(new InternetAddress(from));

				message.setSubject(subject);

				if(!"".equals(body)){
					CArray bodyAttachment = CArray.GetAssociativeArray(t);
					bodyAttachment.set("type", "text/plain");
					bodyAttachment.set("content", body);
					attachments.push(bodyAttachment, 0, t);
				}

				for(Construct c : to.asList()){
					Message.RecipientType type = Message.RecipientType.TO;
					String address;
					if(c instanceof CArray){
						CArray ca = (CArray)c;
						String stype = ArgumentValidation.getItemFromArray(ca, "type", t, new CString("TO", t)).val();
						switch(stype){
							case "TO":
								type = Message.RecipientType.TO;
								break;
							case "CC":
								type = Message.RecipientType.CC;
								break;
							case "BCC":
								type = Message.RecipientType.BCC;
								break;
							default:
								throw ConfigRuntimeException.BuildException("Recipient type must be one of either: TO, CC, or BCC, but \"" + stype + "\" was found.", CREFormatException.class, t);
						}
						address = ArgumentValidation.getItemFromArray(ca, "address", t, null).val();
					} else {
						address = c.val();
					}
					message.addRecipient(type, new InternetAddress(address));
				}

				if(attachments.size() == 1){
					CArray pattachment = ArgumentValidation.getArray(attachments.get(0, t), t);
					String type = ArgumentValidation.getItemFromArray(pattachment, "type", t, null).val();
					String fileName = ArgumentValidation.getItemFromArray(pattachment, "filename", t, new CString("", t)).val().trim();
					String description = ArgumentValidation.getItemFromArray(pattachment, "description", t, new CString("", t)).val().trim();
					String disposition = ArgumentValidation.getItemFromArray(pattachment, "disposition", t, new CString("", t)).val().trim();
					Construct content = ArgumentValidation.getItemFromArray(pattachment, "content", t, null);
					if(!"".equals(fileName)){
						message.setFileName(fileName);
					}
					if(!"".equals(description)){
						message.setDescription(description);
					}
					if(!"".equals(disposition)){
						message.setDisposition(disposition);
					}
					message.setContent(getContent(content, t), type);
				} else {
					Multipart mp = new MimeMultipart("alternative");
					for(Construct attachment : attachments.asList()){
						CArray pattachment = ArgumentValidation.getArray(attachment, t);
						final String type = ArgumentValidation.getItemFromArray(pattachment, "type", t, null).val();
						final String fileName = ArgumentValidation.getItemFromArray(pattachment, "filename", t, new CString("", t)).val().trim();
						String description = ArgumentValidation.getItemFromArray(pattachment, "description", t, new CString("", t)).val().trim();
						String disposition = ArgumentValidation.getItemFromArray(pattachment, "disposition", t, new CString("", t)).val().trim();
						final Object content = getContent(ArgumentValidation.getItemFromArray(pattachment, "content", t, null), t);
						BodyPart bp = new MimeBodyPart();
						if(!"".equals(fileName)){
							bp.setFileName(fileName);
							bp.setHeader("Content-ID", "<" + fileName + ">");
						}
						if(!"".equals(description)){
							bp.setDescription(description);
						}
						if(!"".equals(disposition)){
							bp.setDisposition(disposition);
						}

						DataSource ds = new DataSource() {

							@Override
							public InputStream getInputStream() throws IOException {
								if(content instanceof String){
									return new ByteArrayInputStream(((String)content).getBytes("UTF-8"));
								} else {
									return new ByteArrayInputStream((byte[])content);
								}
							}

							@Override
							public OutputStream getOutputStream() throws IOException {
								throw new Error("Content is immutable, this should never be called.");
							}

							@Override
							public String getContentType() {
								return type;
							}

							@Override
							public String getName() {
								if("".equals(fileName)){
									return "Untitled";
								} else {
									return fileName;
								}
							}
						};
						bp.setDataHandler(new DataHandler(ds));
						mp.addBodyPart(bp);
					}
					message.setContent(mp);
				}

				Transport tr = session.getTransport(useSSL?"smtps":"smtp");
				try {
					tr.connect(host, mailPort, mailUser, mailPassword);
					message.saveChanges();
					MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
//					mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
//					mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
//					mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
					mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
					mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
					CommandMap.setDefaultCommandMap(mc);
					tr.sendMessage(message, message.getAllRecipients());
				} finally {
					tr.close();
				}

			} catch(MessagingException ex){
				if(ex.getCause() instanceof SocketTimeoutException){
					throw ConfigRuntimeException.BuildException(ex.getCause().getMessage(), CREIOException.class, t, ex);
				}
				throw ConfigRuntimeException.BuildException(ex.getMessage(), CREPluginInternalException.class, t, ex);
			}
			return CVoid.VOID;
		}

		/**
		 * Parses the content from the construct.
		 * @param c
		 * @param t
		 * @return
		 */
		private Object getContent(Construct c, Target t){
			if(c instanceof CString){
				return c.val();
			} else if(c instanceof CByteArray){
				CByteArray cb = (CByteArray)c;
				return cb.asByteArrayCopy();
			} else {
				throw ConfigRuntimeException.BuildException("Only strings and byte_arrays may be added as attachments' content.", CREFormatException.class, t);
			}
		}

		@Override
		public String getName() {
			return "email";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return getBundledDocs();
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Sending a plain text email using default transport and email settings", "email(array(\n"
						+ "\tfrom: 'from@example.com',\n"
						+ "\tto: 'to@example.com',\n"
						+ "\tbody: 'Email body',\n"
						+ "));", "<Would send a basic email>"),
				new ExampleScript("Sending a plain text email using gmail", "email(array(\n"
						+ "\thost: 'smtp.gmail.com',\n"
						+ "\tport: 465,\n"
						+ "\tuse_start_tls: true,\n"
						+ "\tuse_ssl: true,\n"
						+ "\tuser: 'username@gmail.com',\n"
						+ "\tpassword: 'myPassword',\n"
						+ "\n"
						+ "\tfrom: 'from@gmail.com',\n"
						+ "\tto: 'to@example.com',\n"
						+ "\tsubject: 'Subject',\n"
						+ "\tbody: 'Body'\n"
						+ "));", "<Would send a basic email through gmail's smtp server>"),
				new ExampleScript("Sending a plain text email settings saved in " + MethodScriptFileLocations.getDefault().getProfilesFile().getName()
						+ " as type \"email\" and id \"myID\". This is most useful for keeping credentials out of code directly.",
						"email('myID', array(\n"
						+ "\tfrom: 'from@gmail.com',\n"
						+ "\tto: 'to@example.com',\n"
						+ "\tsubject: 'Subject',\n"
						+ "\tbody: 'Body'\n"
						+ "));", "<Would send a basic email using the transport settings specified in the profile>"),
				new ExampleScript("Sending a html email, with text fallback", "email(array(\n"
						+ "\tfrom: 'from@example.com',\n"
						+ "\tto: 'to@example.com',\n"
						+ "\tsubject: 'Test Email',\n"
						+ "\tbody: 'This is the plain text body, which would show up in some email clients, such a mobile devices',\n"
						+ "\tattachments: array(\n"
						+ "\t\tarray(\n"
						+ "\t\t\ttype: 'text/html',\n"
						+ "\t\t\tcontent: '<h1>This is the html body, which would show up in most email clients. The <span style=\"color:red;\">plain text body</span> will not show if this does.</h1>'\n"
						+ "\t\t)\n"
						+ "\t)\n"
						+ "));", "<Would send a basic html email, but would have fallback plain text>"),
				new ExampleScript("Sending an email, with multiple recipients", "email(array(\n"
						+ "\tfrom: 'from@example.com',\n"
						+ "\tto: array(\n"
						+ "\t\t'to@example.com',\n"
						+ "\t\tarray(type: 'BCC', address: 'bcc@example.com')\n"
						+ "\t),\n"
						+ "\tbody: 'Two recipients'\n"
						+ "));", "<Would send a basic email to multiple recipients>"),
				new ExampleScript("Sending an email, with a text attachment", "email(array(\n"
						+ "\tfrom: 'from@example.com',\n"
						+ "\tto: 'to@example.com',\n"
						+ "\tattachments: array(\n"
						+ "\t\tarray(\n"
						+ "\t\t\ttype: 'text/plain',\n"
						+ "\t\t\tfilename: 'test.txt',\n"
						+ "\t\t\tcontent: read('test.txt'), // This text may come from anywhere, not just the file system\n"
						+ "\t\t\tdisposition: 'attachment', // This is what tells the email client to make it downloadable\n"
						+ "\t\t\tdescription: 'A description of the file, which may or may not be shown by the email client'\n"
						+ "\t\t)\n"
						+ "\t)\n"
						+ "));", "<Would send a basic email, and the attached file would be downloadable>"),
				new ExampleScript("Sending an email, with a binary attachment", "email(array(\n"
						+ "\tfrom: 'from@example.com',\n"
						+ "\tto: 'to@example.com',\n"
						+ "\tattachments: array(\n"
						+ "\t\tarray(\n"
						+ "\t\t\ttype: 'application/pdf', // This will vary depending on the file type, and cannot be automatically determined\n"
						+ "\t\t\tfilename: 'test.pdf',\n"
						+ "\t\t\tcontent: read_binary('test.pdf'),\n"
						+ "\t\t\tdisposition: 'attachment', // This is what tells the email client to make it downloadable\n"
						+ "\t\t)\n"
						+ "\t)"
						+ "));", "<Would send a basic email, and the attached pdf would be downloadable>"),
				new ExampleScript("Sending an html email with an inline image", "email(array(\n"
						+ "\tfrom: 'from@example.com',\n"
						+ "\tto: 'to@example.com',\n"
						+ "\tsubject: 'Test Email',\n"
						+ "\tbody: 'This is the plain text body, which would show up in some email clients, such a mobile devices',\n"
						+ "\tattachments: array(\n"
						+ "\t\tarray(\n"
						+ "\t\t\ttype: 'text/html',\n"
						+ "\t\t\tcontent: '<h1>This is an inline image: <img src=\"cid:image.png\" /></h1>'\n"
						+ "\t\t), array(\n"
						+ "\t\t\ttype: 'image/png',\n"
						+ "\t\t\tfilename: 'image.png', // This needs to be unique across all attachments, and is referenced by \"cid:image.png\" in the html\n"
						+ "\t\t\tcontent: read_binary('image.png'),\n"
						+ "\t\t\tdisposition: 'inline', // Technically we could leave this off, because it defaults to inline\n"
						+ "\t\t\tdescription: 'An image',\n"
						+ "\t)\n"
						+ "));", "<Would send an html email, and the attached image would show up>"),
			};
		}

	}

}
