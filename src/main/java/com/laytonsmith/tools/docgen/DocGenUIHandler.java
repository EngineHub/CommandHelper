package com.laytonsmith.tools.docgen;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Web.CookieJar;
import com.laytonsmith.PureUtilities.Web.HTTPMethod;
import com.laytonsmith.PureUtilities.Web.HTTPResponse;
import com.laytonsmith.PureUtilities.Web.RequestSettings;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import com.laytonsmith.PureUtilities.XMLDocument;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Crypto;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.persistance.DataSourceException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 *
 * @author lsmith
 */
public class DocGenUIHandler {
	
	private final String pagePrefix = "<!-- This page is maintained automatically. If you would like to"
			+ " make changes to it, the proper way to do this is to make a pull request for the plugin itself. -->\n\n";
	
	public static class QuickStop extends RuntimeException{
		//
	}
	public static interface ProgressManager{
		void setProgress(Integer i);
		void setStatus(String status);
	}
	public static void main(String [] args){
		DocGenUI.main(args);
	}
	
	private static final Map<String, List<String>> baseHeaders = new HashMap<String, List<String>>();
	static{
		baseHeaders.put("User-Agent", Arrays.asList(new String[]{"CommandHelper-DocUploader"}));
	}

	URL url;
	String username;
	String password;
	String prefix;
	String rootPath;
	boolean isStaged;
	boolean doFunctions;
	boolean doExamples;
	boolean doEvents;
	boolean doTemplates;
	int totalPages = 0;
	int atPage = 0;
	
	private ProgressManager progress;
	
	boolean stop = false;
	URL endpoint;

	public DocGenUIHandler(ProgressManager progress, URL url, String username, String password, String prefix,
			String rootPath,
			boolean isStaged, boolean doFunctions,
			boolean doExamples, boolean doEvents, boolean doTemplates) {
		
		this.progress = progress;
		
		this.url = url;
		this.username = username;
		this.password = password;
		this.prefix = prefix;
		this.rootPath = rootPath;
		this.isStaged = isStaged;
		this.doFunctions = doFunctions;
		this.doExamples = doExamples;
		this.doEvents = doEvents;
		this.doTemplates = doTemplates;
		if(!this.prefix.endsWith("/")){
			this.prefix += "/";
		}
		if(this.prefix.startsWith("/")){
			this.prefix = this.prefix.substring(1);
		}
	}
	
	public void stop(){
		stop = true;
	}
	
	public void checkStop(){
		if(stop){
			throw new QuickStop();
		}
	}
	
	public void go() throws Exception{
		try{
			endpoint = new URL(url.toString() + "/w/api.php");
			if(getPage(endpoint).getResponseCode() != 200){
				throw new Exception("Unable to reach wiki API.");
			}
			if(doExamples){
				testCompileExamples();
			}
			doLogin();
			//Now, gather up the page count, so we can set our progress bar correctly
			if(doFunctions){
				totalPages += getFunctionCount();
			}
			if(doExamples){
				totalPages += getExampleCount();
			}
			if(doEvents){
				totalPages += getEventCount();
			}
			if(doTemplates){
				totalPages += getTemplateCount();
			}
			totalPages += getMiscCount();
			progress.setProgress(0);
			if(doFunctions){
				doFunctions();
			}
			if(doExamples){
				doExamples();
			}
			if(doEvents){
				doEvents();
			}
			if(doTemplates){
				doTemplates();
			}
		} finally{
			progress.setProgress(0);
		}
	}
	
	private void doFunctions() throws XPathExpressionException, ConfigCompileException{
		doUpload(DocGen.functions(DocGen.MarkupType.WIKI, api.Platforms.INTERPRETER_JAVA, isStaged), "/API", true);
	}
	
	private void doExamples() throws ConfigCompileException, XPathExpressionException, IOException, DataSourceException, Exception{
		//So they are alphabetical, so we always have a consistent upload order, to
		//facilitate tracing problems.
		SortedSet<String> names = new TreeSet<String>();
		for(FunctionBase base : FunctionList.getFunctionList(api.Platforms.INTERPRETER_JAVA)){
			String name = base.getName();
			if(base.appearInDocumentation()){
				names.add(name);
			}
		}
		for(String name : names){			
			String docs = DocGen.examples(name, isStaged);
			doUpload(docs, "/API/" + name, true);
		}
	}
	
	private void doEvents() throws XPathExpressionException{
		doUpload(DocGen.events(DocGen.MarkupType.WIKI), "/Event_API", true);
	}
	
	private void doTemplates() throws IOException, XPathExpressionException{
		try {
			File root = new File(DocGenUIHandler.class.getResource("/docs").toURI());
			ZipReader reader = new ZipReader(root);
			Queue<File> q = new LinkedList<File>();
			q.addAll(Arrays.asList(reader.listFiles()));
			while(q.peek() != null){
				ZipReader r = new ZipReader(q.poll());
				if(r.isDirectory()){
					q.addAll(Arrays.asList(r.listFiles()));
				} else {
					String articleName = "/" + r.getFile().getName();
					doUpload(DocGen.Template(r.getFile().getName(), isStaged), articleName, true);
				}
			}
		} catch (URISyntaxException ex) {
			Logger.getLogger(DocGenUIHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Uploads a file to a page. If protect is null, the protections are left as is. If protect is false,
	 * protections are removed if they are present. If protect is true, protections are added if they are
	 * not present.
	 * @param wikiMarkup
	 * @param page
	 * @param protect
	 * @throws XPathExpressionException 
	 */
	void doUpload(String wikiMarkup, String page, Boolean protect) throws XPathExpressionException{
		checkStop();
		if(page.startsWith("/")){
			//The prefix already has this
			page = page.substring(1);
		}
		wikiMarkup = pagePrefix + wikiMarkup;
		//The full path 
		String fullPath = prefix + page;
		progress.setStatus("Uploading " + fullPath);
		//First we need to get the edit token
		XMLDocument content = getXML(endpoint, mapCreator(
				"action", "query",
				"titles", fullPath,
				"prop", "revisions",
				"rvprop", "sha1",
				"format", "xml"
		));
		checkStop();
		String sha1 = content.getNode("/api/query/pages/page/revisions/rev/@sha1");
		String sha1local = getSha1(wikiMarkup);
		if(!sha1.equals(sha1local)){			
			XMLDocument query = getXML(endpoint, mapCreator(
					"action", "query",
					"titles", fullPath,
					"prop", "info",
					"intoken", "edit",
					"format", "xml"
			));
			checkStop();
			String edittoken = query.getNode("/api/query/pages/page/@edittoken");			
			XMLDocument edit = getXML(endpoint, mapCreator(
					"action", "edit",
					"title", fullPath,
					"text", wikiMarkup,
					"summary", "Automatic documentation update. (This is a bot edit)",
					"bot", "true",
					"format", "xml",

					//This must always come last
					"token", edittoken
			), false);
		}
		if(protect != null){
			XMLDocument query = getXML(endpoint, mapCreator(
					"action", "query",
					"titles", fullPath,
					"prop", "info",
					"inprop", "protection",
					"intoken", "protect",
					"format", "xml"
			));
			checkStop();
			String protectToken = query.getNode("/api/query/pages/page/@protecttoken");
			boolean isProtectedEdit = false;
			boolean isProtectedMove = false;
			if(query.nodeExists("/api/query/pages/page/protection/pr")){
				for(int i = 1; i <= query.countChildren("/api/query/pages/page/protection"); i++){
					//If only sysops can edit and move
					if(query.getNode("/api/query/pages/page/protection/pr[" + i + "]/@level").equals("sysop")
							&& query.getNode("/api/query/pages/page/protection/pr[" + i + "]/@type").equals("edit")){
						isProtectedEdit = true;
					}
					if(query.getNode("/api/query/pages/page/protection/pr[" + i + "]/@level").equals("sysop")
							&& query.getNode("/api/query/pages/page/protection/pr[" + i + "]/@type").equals("move")){
						isProtectedMove = true;
					}
				}
			}
			boolean isProtected = false;
			if(isProtectedEdit && isProtectedMove){
				isProtected = true;
			}
			if(protect && !isProtected){
				//Protect it
				getXML(endpoint, mapCreator(
						"action", "protect",
						"title", fullPath,
						"token", protectToken,
						"protections", "edit=sysop|move=sysop",
						"expiry", "infinite",
						"reason", "Autoprotecting page (This is a bot edit)"
				));
			} else if(!protect && isProtected){
				//Unprotect it
				getXML(endpoint, mapCreator(
						"action", "protect",
						"title", fullPath,
						"token", protectToken,
						"protections", "edit=autoconfirmed|move=autoconfirmed",
						"expiry", "infinite",
						"reason", "Autoprotecting page (This is a bot edit)"
				));
			}

		}
		incProgress();
	}
	
	private void incProgress(){
		atPage++;
		if(totalPages == 0){
			progress.setProgress(null);
		} else {
			progress.setProgress((int)((float)atPage / (float)totalPages * 100.0));
		}
	}
	
	private int getFunctionCount(){
		return 1;
	}
	
	private int getExampleCount(){
		return FunctionList.getFunctionList(api.Platforms.INTERPRETER_JAVA).size();
	}
	
	private int getEventCount(){
		return 1;
	}
	
	private int getTemplateCount() throws IOException{
		try {
			int count = 0;
			ZipReader reader = new ZipReader(new File(DocGenUIHandler.class.getResource("/docs").toURI()));
			Queue<File> q = new LinkedList<File>();
			q.addAll(Arrays.asList(reader.listFiles()));
			while(q.peek() != null){
				ZipReader r = new ZipReader(q.poll());
				if(r.isDirectory()){
					q.addAll(Arrays.asList(r.listFiles()));
				} else {
					count++;
				}
			}
			return count;
		} catch (URISyntaxException ex) {
			throw new APIException(ex);
		}
	}
	
	private int getMiscCount(){
		int total = 0;

		return total;
	}
	
	private void doLogin() throws MalformedURLException, XPathExpressionException{
		checkStop();
		XMLDocument login = getXML(endpoint, mapCreator(
				"format", "xml",
				"action", "login",
				"lgname", username,
				"lgpassword", password
		));
		if("NeedToken".equals(login.getNode("/api/login/@result"))){
			XMLDocument login2 = getXML(endpoint, mapCreator(
				"format", "xml",
				"action", "login",
				"lgname", username,
				"lgpassword", password,
				"lgtoken", login.getNode("/api/login/@token")
			));
			if(!"Success".equals(login2.getNode("/api/login/@result"))){
				if("WrongPass".equals(login2.getNode("/api/login/@result"))){
					throw new APIException("Wrong password.");
				}
				throw new APIException("Could not log in successfully.");
			}
		}
		progress.setStatus("Logged in");
		password = null;
	}
	
	public static class APIException extends RuntimeException{
		
		public APIException(String message){
			super(message);
		}

		public APIException(Throwable cause) {
			super("API responded incorrectly.", cause);
		}
		
	}
	
	private static String getSha1(String content){
		try {
                MessageDigest digest = java.security.MessageDigest.getInstance("SHA1");
                digest.update(content.getBytes());
                String hash = StringUtils.toHex(digest.digest()).toLowerCase();
                return hash;
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException("An error occured while trying to hash your data", ex);
            }
	}
	
	private static Map<String, String> mapCreator(String ... strings){
		if(strings.length % 2 != 0){
			throw new Error("Only an even number of parameters may be passed to mapCreator");
		}
		Map<String, String> map = new HashMap<String, String>();
		for(int i = 0; i < strings.length; i+=2){
			map.put(strings[i], strings[i + 1]);
		}
		return map;
	}
	private static CookieJar cookieStash = new CookieJar();
	private static XMLDocument getXML(URL url, Map<String, String> params) throws APIException{
		return getXML(url, params, true);
	}
	private static XMLDocument getXML(URL url, Map<String, String> params, boolean useURL) throws APIException{
		try{
			XMLDocument doc = new XMLDocument(getPage(url, params, useURL).getContent());
			if(doc.nodeExists("/api/error")){
				//Doh.
				throw new APIException(doc.getNode("/api/error/@info"));
			}
			return doc;
		} catch(XPathExpressionException e){
			throw new APIException(e);
		} catch(SAXException e){
			throw new APIException(e);
		} catch(IOException e){
			throw new APIException(e);
		}
	}
	private static HTTPResponse getPage(URL url) throws IOException{
		return getPage(url, null);
	}
	private static HTTPResponse getPage(URL url, Map<String, String> params) throws IOException{
		return getPage(url, params, false);
	}
	/**
	 * The wiki apparently wants the parameters in the URL, but the method set to post. If useURL is
	 * true, it will merge the params into the url.
	 * @param url
	 * @param params
	 * @param useURL
	 * @return
	 * @throws IOException 
	 */
	private static HTTPResponse getPage(URL url, Map<String, String> params, boolean useURL) throws IOException{
		Map<String, List<String>> headers = new HashMap<String, List<String>>(baseHeaders);
		if (params != null && !params.isEmpty() && useURL) {
            StringBuilder b = new StringBuilder(url.getQuery() == null ? "" : url.getQuery());
            if (b.length() != 0) {
                b.append("&");
            }
			RequestSettings temp = new RequestSettings().setParameters(params);
            b.append(WebUtility.encodeParameters(temp.getParameters()));
            String query = b.toString();
            url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + "?" + query);
        }
		headers.put("Host", Arrays.asList(new String[]{url.getHost()}));
		RequestSettings settings = new RequestSettings().setMethod(HTTPMethod.POST).setHeaders(headers)
				.setParameters(params).setCookieJar(cookieStash).setFollowRedirects(true);
		return WebUtility.GetPage(url, settings);
	}
	
	public static void testCompileExamples(){
		for(FunctionBase fb : FunctionList.getFunctionList(api.Platforms.INTERPRETER_JAVA)){
			if(fb instanceof Function){
				Function f = (Function)fb;
				try{
					f.examples();
				} catch(ConfigCompileException e){
					throw new RuntimeException("Compilation error while compiling examples for " + f.getName(), e);
				}
			}
		}
	}
	
}
