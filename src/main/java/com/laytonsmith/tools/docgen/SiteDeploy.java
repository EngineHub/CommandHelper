package com.laytonsmith.tools.docgen;

import com.jcraft.jsch.MAC;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.SSHWrapper;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.extensions.ExtensionTracker;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.json.simple.JSONValue;

/**
 * This class is responsible for deploying documentation to an html webserver. It converts wiki markup to html, and uses
 * scp to transfer the complete website structure to the specified location.
 *
 * @author cailin
 */
public class SiteDeploy {

    public static void main(String[] args) throws Exception {
	Implementation.setServerType(Implementation.Type.SHELL);
	run(false, null);
    }

    private static final String USERNAME = "username";
    private static final String HOSTNAME = "hostname";
    private static final String PORT = "port";
    private static final String DIRECTORY = "directory";
    private static final String PASSWORD = "use-password";
    private static final String SITEBASE = "site-base";

    public static void run(boolean generate_prefs, File config) throws Exception {
	run(generate_prefs, config, "");
    }

    public static void run(boolean generate_prefs, File sitedeploy, String password) throws Exception {
	List<Preferences.Preference> defaults = new ArrayList<>();
	defaults.add(new Preferences.Preference(USERNAME, "", Preferences.Type.STRING, "The username to scp with"));
	defaults.add(new Preferences.Preference(HOSTNAME, "", Preferences.Type.STRING, "The hostname to connect to. If"
		+ " the hostname is \"localhost\", this triggers special handling, which skips the upload, and simply"
		+ " saves to the specified location on local disk. This should work with all OSes, otherwise the host"
		+ " that this connects to must support ssh, though it does not necessarily have to be a unix based system."
		+ " If the value is localhost, the values " + USERNAME + ", " + PORT + ", and " + PASSWORD + " are irrelevant, and not used."));
	defaults.add(new Preferences.Preference(PORT, "22", Preferences.Type.INT, "The port to use for SCP"));
	defaults.add(new Preferences.Preference(DIRECTORY, "/var/www", Preferences.Type.STRING, "The root location of the remote web server."
		+ " This must be an absolute path."));
	defaults.add(new Preferences.Preference(PASSWORD, "false", Preferences.Type.BOOLEAN, "Whether or not to use password authentication. If false,"
		+ " public key authentication will be used instead, and your system must be pre-configured for that. The password is interactively"
		+ " prompted for. If you wish to use non-interactive mode, you must use public key authentication."));
	defaults.add(new Preferences.Preference(SITEBASE, "", Preferences.Type.STRING, "The base url of the site"));
	Preferences prefs = new Preferences("Site-Deploy", Logger.getLogger(SiteDeploy.class.getName()), defaults);
	if (generate_prefs) {
	    prefs.init(sitedeploy);
	    System.out.println("Preferences file is now located at " + sitedeploy.getAbsolutePath() + ". Please fill in the"
		    + " values, then re-run this command without the --generate-prefs option.");
	    System.exit(0);
	}
	prefs.init(sitedeploy);

	String username = (String) prefs.getPreference(USERNAME);
	String hostname = (String) prefs.getPreference(HOSTNAME);
	Integer port = (Integer) prefs.getPreference(PORT);
	String directory = (String) prefs.getPreference(DIRECTORY);
	Boolean use_password = (Boolean) prefs.getPreference(PASSWORD);
	String siteBase = (String) prefs.getPreference(SITEBASE);
	if (("localhost".equals(hostname) && ("".equals(directory) || "".equals(siteBase)))
		|| (!"localhost".equals(hostname) && ("".equals(hostname) || port < 0 || port > 65535 || "".equals(directory) || "".equals(username) || "".equals(siteBase)))) {
	    System.out.println("Invalid input. Check preferences in " + sitedeploy.getAbsolutePath() + " and re-run");
	    System.exit(1);
	}

	if (!directory.endsWith("/")) {
	    directory += "/";
	}
	if (!siteBase.endsWith("/")) {
	    siteBase += "/";
	}
	directory += CHVersion.LATEST;
	siteBase += CHVersion.LATEST + "/";
	System.out.println("Using the following settings:");
	System.out.println("username: " + username);
	System.out.println("hostname: " + hostname);
	System.out.println("port: " + port);
	System.out.println("directory: " + directory);
	System.out.println("site-base: " + siteBase);

	if (use_password && password != null) {
	    jline.console.ConsoleReader reader = null;
	    try {
		Character cha = (char) 0;
		reader = new jline.console.ConsoleReader();
		reader.setExpandEvents(false);
		password = reader.readLine("Please enter your password: ", cha);
	    } finally {
		if (reader != null) {
		    reader.shutdown();
		}
	    }
	}

	String remote = username + "@" + hostname + ":" + port + (password == null || "".equals(password) ? "" : (":" + password)) + ":" + directory + "/";

	// Ok, all the configuration details are input and correct, so lets deploy now.
	deploy(remote, siteBase);
    }

    private static void deploy(String remote, String siteBase) throws IOException, InterruptedException {
	new SiteDeploy(remote, siteBase).deploy();
    }

    /**
     * Remote will be in the format user@remote:port[:password]:/directory/ using the inputs from the user.
     */
    private final String remote;
    private final String siteBase;
    private final jline.console.ConsoleReader reader;
    private final ExecutorService generateQueue;
    private final ExecutorService uploadQueue;
    private final AtomicInteger currentUploadTask = new AtomicInteger(0);
    private final AtomicInteger totalUploadTasks = new AtomicInteger(0);
    private final AtomicInteger currentGenerateTask = new AtomicInteger(0);
    private final AtomicInteger totalGenerateTasks = new AtomicInteger(0);

    private SiteDeploy(String remote, String siteBase) throws IOException {
	this.remote = remote;
	this.siteBase = siteBase;
	this.reader = new jline.console.ConsoleReader();
	this.generateQueue = Executors.newSingleThreadExecutor();
	this.uploadQueue = Executors.newSingleThreadExecutor();
    }

    private void resetLine() throws IOException {
	reader.getOutput().write("\u001b[1G\u001b[K");
	reader.flush();
    }

    private synchronized void writeStatus() {
	int generatePercent = 0;
	if (totalGenerateTasks.get() != 0) {
	    generatePercent = (int) (((double) currentGenerateTask.get()) / ((double) totalGenerateTasks.get()) * 100.0);
	}
	int uploadPercent = 0;
	if (totalUploadTasks.get() != 0) {
	    uploadPercent = (int) (((double) currentUploadTask.get()) / ((double) totalUploadTasks.get()) * 100.0);
	}
	String message = "Generate progress: " + currentGenerateTask.get() + "/" + totalGenerateTasks.get()
		+ " (" + generatePercent + "%)"
		+ "; Upload progress: " + currentUploadTask.get() + "/" + totalUploadTasks.get()
		+ " (" + uploadPercent + "%)"
		+ ";";
	try {
	    resetLine();
	    reader.getOutput().write(message);
	    reader.flush();
	} catch (IOException ex) {
	    System.out.println(message);
	}
    }

    private Map<String, DocGenTemplates.Generator> getStandardGenerators() {
	Map<String, DocGenTemplates.Generator> g = new HashMap<>();
	g.put("resourceBase", new DocGenTemplates.Generator() {
	    @Override
	    public String generate(String... args) {
		return SiteDeploy.this.siteBase + "resources/";
	    }
	});
	g.put("branding", new DocGenTemplates.Generator() {
	    @Override
	    public String generate(String... args) {
		return Implementation.GetServerType().getBranding();
	    }
	});
	g.put("siteRoot", new DocGenTemplates.Generator() {
	    @Override
	    public String generate(String... args) {
		return SiteDeploy.this.siteBase;
	    }
	});
	final DocGenTemplates.Generator learningTrailGen = new DocGenTemplates.Generator() {
	    @Override
	    public String generate(String... args) {
		String learning_trail = StreamUtils.GetString(SiteDeploy.class.getResourceAsStream("/siteDeploy/LearningTrail.json"));
		List<Map<String, List<Map<String, String>>>> ret = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Map<String, List<Object>>> lt = (List<Map<String, List<Object>>>) JSONValue.parse(learning_trail);
		for (Map<String, List<Object>> l : lt) {
		    for (Map.Entry<String, List<Object>> e : l.entrySet()) {
			String category = e.getKey();
			List<Map<String, String>> catInfo = new ArrayList<>();
			for (Object ll : e.getValue()) {
			    Map<String, String> pageInfo = new LinkedHashMap<>();
			    String page = null;
			    String name = null;
			    if (ll instanceof String) {
				name = page = (String) ll;
			    } else if (l instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, String> p = (Map<String, String>) ll;
				if (p.entrySet().size() != 1) {
				    throw new RuntimeException("Invalid JSON for learning trail");
				}
				for (Map.Entry<String, String> ee : p.entrySet()) {
				    page = ee.getKey();
				    name = ee.getValue();
				}
				assert page != null && name != null;
			    } else {
				throw new RuntimeException("Invalid JSON for learning trail");
			    }
			    boolean exists = SiteDeploy.class.getResourceAsStream("/docs/" + page) != null;
			    pageInfo.put("name", name);
			    pageInfo.put("page", page);
			    pageInfo.put("category", category);
			    pageInfo.put("exists", Boolean.toString(exists));
			    catInfo.add(pageInfo);
			}
			Map<String, List<Map<String, String>>> m = new HashMap<>();
			m.put(category, catInfo);
			ret.add(m);
		    }
		}
		return JSONValue.toJSONString(ret);
	    }
	};
	g.put("js_string_learning_trail", new DocGenTemplates.Generator() {
	    @Override
	    public String generate(String... args) {
		String g = learningTrailGen.generate(args);
		g = g.replaceAll("\\\\", "\\\\");
		g = g.replaceAll("\"", "\\\\\"");
		return g;
	    }
	});
	g.put("learning_trail", learningTrailGen);
	return g;
    }

    private void deploy() throws InterruptedException, IOException {
	deployResources();
	deployFrontPage();
	deployLearningTrail();
	deployAPI();
	deployEventAPI();
	deployFunctions();
	deployEvents();
	deployJSON();
	generateQueue.submit(new Runnable() {
	    @Override
	    public void run() {
		uploadQueue.shutdown();
	    }
	});
	generateQueue.shutdown();
	generateQueue.awaitTermination(1, TimeUnit.DAYS);
	uploadQueue.awaitTermination(1, TimeUnit.DAYS);
	SSHWrapper.closeSessions();
	System.out.println();
	System.out.println("Done!");
    }

    /**
     * Writes an arbitrary string to a file on the remote.
     *
     * @param contents The string to write
     * @param toLocation The location of the remote file
     */
    private void writeFromString(final String contents, final String toLocation) {
	writeFromStream(StreamUtils.GetInputStream(contents), toLocation);
    }

    /**
     * Writes an arbitrary stream to a file on the remote.
     *
     * @param contents
     * @param toLocation
     */
    private void writeFromStream(final InputStream contents, final String toLocation) {
	uploadQueue.submit(new Runnable() {
	    @Override
	    public void run() {
		try {
		    SSHWrapper.SCPWrite(contents, remote + toLocation);
		    currentUploadTask.addAndGet(1);
		    writeStatus();
		} catch (IOException ex) {
		    Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, "Failed while uploading " + toLocation, ex);
		    generateQueue.shutdownNow();
		    uploadQueue.shutdownNow();
		}
	    }
	});
	totalUploadTasks.addAndGet(1);
    }

    /**
     * Writes a resource as is (no template replacements) to
     *
     * @param resource
     * @param toLocation
     */
    private void writeResource(final String resource, final String toLocation) {
	writeFromStream(SiteDeploy.class.getResourceAsStream(resource), toLocation);
    }

    /**
     * Most pages should use this method instead of the other methods. This takes care of all the steps, including
     * substituting the body into the frame, and handling all the other connections.
     *
     * @param body The content body
     * @param toLocation the location on the remote server
     */
    private void writePage(final String title, final String body, final String toLocation) {
	generateQueue.submit(new Runnable() {
	    @Override
	    public void run() {
		try {
		    // First, substitute the templates in the body
		    final String b;
		    try {
			b = DocGenTemplates.DoTemplateReplacement(body, DocGenTemplates.GetGenerators());
		    } catch (Exception ex) {
			Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, null, ex);
			generateQueue.shutdownNow();
			uploadQueue.shutdownNow();
			return;
		    }
		    // Second, add the template %%body%% and replace that in the frame
		    Map<String, DocGenTemplates.Generator> g = new HashMap<>();
		    g.put("body", new DocGenTemplates.Generator() {
			@Override
			public String generate(String... args) {
			    return b;
			}
		    });
		    g.put("title", new DocGenTemplates.Generator() {
			@Override
			public String generate(String... args) {
			    return title;
			}
		    });
		    g.putAll(getStandardGenerators());
		    String frame = StreamUtils.GetString(SiteDeploy.class.getResourceAsStream("/siteDeploy/frame.html"));
		    final String bb = DocGenTemplates.DoTemplateReplacement(frame, g);
		    // Write out using writeFromString
		    writeFromString(bb, toLocation);
		    currentGenerateTask.addAndGet(1);
		    writeStatus();
		} catch (Exception ex) {
		    Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, "While writing " + toLocation + " the following error occured:", ex);
		}
	    }
	});
	totalGenerateTasks.addAndGet(1);
    }

    /**
     * Most pages should use this method instead of the other methods. This takes care of all the steps, including
     * substituting the body into the frame, and handling all the other connections.
     *
     * @param resource
     * @param toLocation
     * @return
     */
    private void writePageFromResource(String title, String resource, String toLocation) {
	String s = StreamUtils.GetString(SiteDeploy.class.getResourceAsStream(resource));
	writePage(title, s, toLocation);
    }

    /**
     * Pages deployed: All pages under the siteDeploy/resources folder. They are transferred as is, including
     * recursively looking through the folder structure. Binary files are also supported. index.js (after template
     * replacement)
     */
    private void deployResources() {
	try {
	    File root = new File(SiteDeploy.class.getResource("/siteDeploy/resources").toExternalForm());
	    ZipReader reader = new ZipReader(root);
	    Queue<File> q = new LinkedList<File>();
	    q.addAll(Arrays.asList(reader.listFiles()));
	    while (q.peek() != null) {
		ZipReader r = new ZipReader(q.poll());
		if (r.isDirectory()) {
		    q.addAll(Arrays.asList(r.listFiles()));
		} else {
		    String fileName = r.getFile().getAbsolutePath().replaceFirst(Pattern.quote(reader.getFile().getAbsolutePath()), "");
		    writeFromStream(r.getInputStream(), "resources" + fileName);
		}
	    }
	} catch (IOException ex) {
	    Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, null, ex);
	}
	String index_js = StreamUtils.GetString(SiteDeploy.class.getResourceAsStream("/siteDeploy/index.js"));
	writeFromString(DocGenTemplates.DoTemplateReplacement(index_js, getStandardGenerators()), "resources/js/index.js");
    }

    /**
     * Pages deployed: index.html privacy_policy.html
     */
    private void deployFrontPage() {
	writePageFromResource("Welcome", "/siteDeploy/FrontPage", "index.html");
	writePageFromResource("Privacy Policy", "/siteDeploy/privacy_policy.html", "privacy_policy.html");
    }

    /**
     * Pages deployed: All files from /docs/*
     */
    private void deployLearningTrail() throws IOException {
	File root = new File(SiteDeploy.class.getResource("/docs").toExternalForm());
	ZipReader zReader = new ZipReader(root);
	for (File r : zReader.listFiles()) {
	    String filename = r.getAbsolutePath().replaceFirst(Pattern.quote(zReader.getFile().getAbsolutePath()), "");
	    writePageFromResource(r.getName(), "/docs" + filename, r.getName() + ".html");
	}
    }

    private void deployAPI() {

    }

    private void deployEventAPI() {

    }

    private void deployFunctions() {

    }

    private void deployEvents() {

    }

    /**
     * Pages deployed: api.json - This page is the json version of the api
     */
    private void deployJSON() {
	generateQueue.submit(new Runnable() {
	    @Override
	    public void run() {
		Map<String, Object> json = new TreeMap<>();
		{
		    // functions
		    Map<String, Map<String, Object>> API = new TreeMap<>();
		    for (FunctionBase f : FunctionList.getFunctionList(api.Platforms.INTERPRETER_JAVA)) {
			if (f instanceof Function) {
			    Function ff = (Function) f;
			    Map<String, Object> function = new TreeMap<>();
			    DocGen.DocInfo di = new DocGen.DocInfo(ff.docs());
			    function.put("name", ff.getName());
			    function.put("ret", di.ret);
			    function.put("args", di.originalArgs);
			    List<String> thrown = new ArrayList<>();
			    try {
				if (ff.thrown() != null) {
				    for (Class<? extends CREThrowable> c : ff.thrown()) {
					thrown.add(c.getAnnotation(typeof.class).value());
				    }
				}
			    } catch (Throwable t) {
				Logger.getLogger("default").log(Level.SEVERE, null, t);
			    }
			    function.put("thrown", thrown);
			    function.put("desc", di.desc);
			    function.put("extdesc", di.extendedDesc);
			    function.put("shortdesc", di.topDesc);
			    function.put("since", ff.since().toString());
			    function.put("restricted", ff.isRestricted());
			    function.put("coreFunction", ff.isCore());
			    List<String> optimizations = new ArrayList<>();
			    if (ff instanceof Optimizable) {
				for (Optimizable.OptimizationOption o : ((Optimizable) ff).optimizationOptions()) {
				    optimizations.add(o.name());
				}
			    }
			    function.put("optimizations", optimizations);
			    hide athide = ff.getClass().getAnnotation(hide.class);
			    String hidden = athide == null ? null : athide.value();
			    function.put("hidden", hidden);
			    String extId = ExtensionManager.getTrackers().get(ff.getSourceJar()).getIdentifier();
			    function.put("source", extId);
			    API.put(ff.getName(), function);
			}
		    }
		    json.put("functions", API);
		}
		{
		    // events
		    Map<String, Map<String, Object>> events = new TreeMap<>();
		    for (Event e : EventList.GetEvents()) {
			try {
			    Map<String, Object> event = new TreeMap<>();
			    event.put("name", e.getName());
			    DocGen.EventDocInfo edi = new DocGen.EventDocInfo(e.docs());
			    event.put("desc", edi.description);
			    Map<String, String> ed = new TreeMap<>();
			    for (DocGen.EventDocInfo.EventData edd : edi.eventData) {
				ed.put(edd.name, edd.description);
			    }
			    event.put("eventData", ed);
			    Map<String, String> md = new TreeMap<>();
			    for (DocGen.EventDocInfo.MutabilityData mdd : edi.mutability) {
				md.put(mdd.name, mdd.description);
			    }
			    event.put("mutability", md);
			    Map<String, String> pd = new TreeMap<>();
			    for (DocGen.EventDocInfo.PrefilterData pdd : edi.prefilter) {
				pd.put(pdd.name, pdd.formatDescription(DocGen.MarkupType.TEXT));
			    }
			    event.put("prefilters", pd);
			    event.put("since", e.since().toString());
			    events.put(e.getName(), event);
			} catch (Exception ex) {
			    Logger.getLogger("default").log(Level.SEVERE, e.getName(), ex);
			}
		    }
		    json.put("events", events);
		}
		{
		    Map<String, Map<String, String>> extensions = new TreeMap<>();
		    // extensions
		    for (ExtensionTracker t : ExtensionManager.getTrackers().values()) {
			Map<String, String> ext = new TreeMap<>();
			ext.put("id", t.getIdentifier());
			ext.put("version", t.getVersion().toString());
			extensions.put(t.getIdentifier(), ext);
		    }
		    json.put("extensions", extensions);
		}

		writeFromString(JSONValue.toJSONString(json), "api.json");
		currentGenerateTask.addAndGet(1);
	    }
	});
	totalGenerateTasks.addAndGet(1);
    }
}
