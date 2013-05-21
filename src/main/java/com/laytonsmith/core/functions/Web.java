package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StackTraceUtils;
import com.laytonsmith.PureUtilities.StringUtils;
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
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Web {
	public static String docs(){
		return "Contains various methods to make HTTP requests.";
	}

	private static CArray getCookieJar(CookieJar cookieJar, Target t){
		CArray ret = new CArray(t);
		for(Cookie cookie : cookieJar.getAllCookies()){
			CArray c = new CArray(t);
			c.set("name", cookie.getName());
			c.set("value", cookie.getValue());
			c.set("domain", cookie.getDomain());
			c.set("path", cookie.getPath());
			c.set("expiration", new CInt(cookie.getExpiration(), t), t);
			if(!cookie.isHttpOnly() && !cookie.isSecureOnly()){
				c.set("httpOnly", new CNull(t), t);
			} else {
				c.set("httpOnly", new CBoolean(cookie.isHttpOnly(), t), t);
			}
			ret.push(c);
		}
		return ret;
	}
	
	private static CookieJar getCookieJar(CArray cookieJar, Target t){
		CookieJar ret = new CookieJar();
		for(String key : cookieJar.keySet()){
			CArray cookie = Static.getArray(cookieJar.get(key), t);
			String name;
			String value;
			String domain;
			String path;
			long expiration = 0;
			Boolean httpOnly = null;
			if(cookie.containsKey("name") && cookie.containsKey("value")
					&& cookie.containsKey("domain") && cookie.containsKey("path")){
				name = cookie.get("name").val();
				value = cookie.get("value").val();
				domain = cookie.get("domain").val();
				path = cookie.get("path").val();
			} else {
				throw new ConfigRuntimeException("The name, value, domain, and path keys are required"
						+ " in all cookies.", ExceptionType.FormatException, t);
			}
			if(cookie.containsKey("expiration")){
				expiration = Static.getInt(cookie.get("expiration"), t);
			}
			if(cookie.containsKey("httpOnly")){
				if(cookie.get("expiration") instanceof CNull){
					httpOnly = null;
				} else {
					httpOnly = Static.getBoolean(cookie.get("expiration"));
				}
			}
			Cookie c = new Cookie(name, value, domain, path, expiration, httpOnly);
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
		private static final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
			
			
			public Thread newThread(Runnable r) {
				return new Thread(r, Implementation.GetServerType().getBranding() + "-web-request-" + (threadCount++));
			}
		});

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
			final URL url;
			try {
				url = new URL(args[0].val());
			} catch (MalformedURLException ex) {
				throw new Exceptions.FormatException(ex.getMessage(), t);
			}
			final RequestSettings settings = new RequestSettings();
			final CClosure success;
			final CClosure error;
			if(args[1] instanceof CClosure){
				success = (CClosure) args[1];
				error = null;
			} else {
				CArray csettings = Static.getArray(args[1], t);
				if(csettings.containsKey("method")){
					try{
						settings.setMethod(HTTPMethod.valueOf(csettings.get("method").val()));
					} catch(IllegalArgumentException e){
						throw new Exceptions.FormatException(e.getMessage(), t);
					}
				}
				if(csettings.containsKey("headers") && !(csettings.get("headers") instanceof CNull)){
					CArray headers = Static.getArray(csettings.get("headers"), t);
					Map<String, List<String>> mheaders = new HashMap<String, List<String>>();
					for(String key : headers.keySet()){
						List<String> h = new ArrayList<String>();
						Construct c = headers.get(key);
						if(c instanceof CArray){
							for(String kkey : ((CArray)c).keySet()){
								h.add(((CArray)c).get(kkey).val());
							}
						} else {
							h.add(c.val());
						}
						mheaders.put(key, h);
					}
					settings.setHeaders(mheaders);
				}
				if(csettings.containsKey("params") && !(csettings.get("params") instanceof CNull)){
					CArray params = Static.getArray(csettings.get("params"), t);
					Map<String, List<String>> mparams = new HashMap<String, List<String>>();
					for(String key : params.keySet()){
						Construct c = params.get(key);
						List<String> l = new ArrayList<String>();
						if(c instanceof CArray){
							for(String kkey : ((CArray)c).keySet()){
								l.add(((CArray)c).get(kkey).val());
							}
						} else {
							l.add(c.val());
						}
						mparams.put(key, l);
					}
					settings.setComplexParameters(mparams);
				}
				if(csettings.containsKey("cookiejar") && !(csettings.get("cookiejar") instanceof CNull)){
					settings.setCookieJar(getCookieJar(Static.getArray(csettings.get("cookiejar"), t), t));
				}
				if(csettings.containsKey("followRedirects")){
					settings.setFollowRedirects(Static.getBoolean(csettings.get("followRedirects")));
				}
				//Only required parameter
				if(csettings.containsKey("success")){
					if(csettings.get("success") instanceof CClosure){
						success = (CClosure) csettings.get("success");
					} else {
						throw new Exceptions.CastException("Expecting the success parameter to be a closure.", t);
					}
				} else {
					throw new ConfigRuntimeException("Missing the success parameter, which is required.", ExceptionType.CastException, t);
				}
				if(csettings.containsKey("error")){
					if(csettings.get("error") instanceof CClosure){
						error = (CClosure) csettings.get("error");
					} else {
						throw new Exceptions.CastException("Expecting the error parameter to be a closure.", t);
					}
				} else {
					error = null;
				}
				if(csettings.containsKey("timeout")){
					settings.setTimeout(Static.getInt32(csettings.get("timeout"), t));
				}
				String username = null;
				String password = null;
				if(csettings.containsKey("username")){
					username = csettings.get("username").val();
				}
				if(csettings.containsKey("password")){
					password = csettings.get("password").val(); 
				}
				settings.setAuthenticationDetails(username, password);
			}
			threadPool.submit(new Runnable() {

				public void run() {
					try{
						HTTPResponse resp = WebUtility.GetPage(url, settings);
						final CArray array = new CArray(t);
						array.set("body", new CString(resp.getContent(), t), t);
						CArray headers = new CArray(t);
						for(String key : resp.getHeaderNames()){
							CArray h = new CArray(t);
							for(String val : resp.getHeaders(key)){
								h.push(new CString(val, t));
							}
							headers.set(key, h, t);
						}
						array.set("headers", headers, t);
						array.set("responseCode", new CInt(resp.getResponseCode(), t), t);
						array.set("responseText", resp.getResponseText());
						array.set("httpVersion", resp.getHttpVersion());
						array.set("error", new CBoolean(resp.getResponseCode() >= 400 && resp.getResponseCode() < 600, t), t);
						StaticLayer.GetConvertor().runOnMainThreadLater(new Runnable() {

							public void run() {
								executeFinish(success, array, t, environment);
							}
						});
					} catch(IOException e){
						final ConfigRuntimeException ex = new ConfigRuntimeException(e.getMessage(), ExceptionType.IOException, t);
						if(error != null){
							StaticLayer.GetConvertor().runOnMainThreadLater(new Runnable() {

								public void run() {
									executeFinish(error, ObjectGenerator.GetGenerator().exception(ex, t), t, environment);
								}
							});
						} else {
							ConfigRuntimeException.HandleUncaughtException(ex, environment);
						}
					}
				}
			});
			return new CVoid(t);
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
				ConfigRuntimeException.React(e, environment);
			} catch(Throwable e){
				//Other throwables we just need to report
				CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, LogLevel.ERROR, "An unexpected exception has occurred. No extra"
						+ " information is available, but please report this error:\n" + StackTraceUtils.GetStacktrace(e), t);
			}
		}

		public String getName() {
			return "http_request";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			Map<String, DocGenTemplates.Generator> templates = new HashMap<String, DocGenTemplates.Generator>();
			templates.put("enum", new DocGenTemplates.Generator() {

				public String generate(String... args) {
					if("HTTPMethod".equals(args[0])){
						return StringUtils.Join(HTTPMethod.values(), ", ");
					} else {
						return "";
					}
				}
			});
			return super.getBundledDocs(templates);
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class http_clear_session_cookies extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
		}

		public boolean isRestricted() {
			return false;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray array = Static.getArray(args[0], t);
			CookieJar jar = getCookieJar(array, t);
			jar.clearSessionCookies();
			return new CVoid(t);
		}

		public String getName() {
			return "http_clear_session_cookies";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "void {cookieJar} Clears out \"session\" cookies, that is cookies that weren't set with an expiration"
					+ " (which translates to 0 in an individual cookie).";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
		
	}

}
