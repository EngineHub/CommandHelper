package com.laytonsmith.tools.docgen.sitedeploy;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.ReadOnlyException;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import com.laytonsmith.tools.docgen.DocGenTemplates.Generator;
import com.laytonsmith.tools.docgen.DocGenTemplates.Generator.GenerateException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
	run(false, false, null);
    }

    private static final String USERNAME = "username";
    private static final String HOSTNAME = "hostname";
    private static final String PORT = "port";
    private static final String DIRECTORY = "directory";
    private static final String PASSWORD = "use-password";
    private static final String DOCSBASE = "docs-base";
    private static final String SITEBASE = "site-base";

    public static void run(boolean generate_prefs, boolean useLocalCache, File config) throws Exception {
	run(generate_prefs, useLocalCache, config, "");
    }

    public static void run(boolean generate_prefs, boolean useLocalCache, File sitedeploy, String password) throws Exception {
	List<Preferences.Preference> defaults = new ArrayList<>();
	// SCP Options
	defaults.add(new Preferences.Preference(USERNAME, "", Preferences.Type.STRING, "The username to scp with"));
	defaults.add(new Preferences.Preference(HOSTNAME, "", Preferences.Type.STRING, "The hostname to scp to (not the"
		+ " hostname of the site). If"
		+ " the hostname is \"localhost\", this triggers special handling, which skips the upload, and simply"
		+ " saves to the specified location on local disk. This should work with all OSes, otherwise the host"
		+ " that this connects to must support ssh, though it does not necessarily have to be a unix based system."
		+ " If the value is localhost, the values " + USERNAME + ", " + PORT + ", and " + PASSWORD + " are"
		+ " irrelevant, and not used. This should NOT begin with a protocol, i.e. http://"));
	defaults.add(new Preferences.Preference(PORT, "22", Preferences.Type.INT, "The port to use for SCP"));
	defaults.add(new Preferences.Preference(DIRECTORY, "/var/www/docs", Preferences.Type.STRING, "The root location of"
		+ " the remote web server. This must be an absolute path. Note that this is the location of"
		+ " the *docs* folder. Files may be created one directory above this folder, in this folder, and in"
		+ " lower folders that are created by the site deploy tool. So if /var/www is your web"
		+ " root, then you should put /var/www/docs here. It will create an index file in /var/www, as"
		+ " well as in /var/www/docs, but the majority of files will be put in /var/www/docs/"
		+ CHVersion.LATEST.toString() + "."));
	defaults.add(new Preferences.Preference(PASSWORD, "false", Preferences.Type.BOOLEAN, "Whether or not to use"
		+ " password authentication. If false, public key authentication will be used instead, and your"
		+ " system must be pre-configured for that. The password is interactively"
		+ " prompted for. If you wish to use non-interactive mode, you must use public key authentication."));

	// General Options
	defaults.add(new Preferences.Preference(DOCSBASE, "", Preferences.Type.STRING, "The base url of the docs."
		+ " This should begin with http:// or https://"));
	defaults.add(new Preferences.Preference(SITEBASE, "", Preferences.Type.STRING, "The base url of the"
		+ " site (where \"home\" should be). This should begin with http:// or https://"));

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
	String docsBase = (String) prefs.getPreference(DOCSBASE);
	String siteBase = (String) prefs.getPreference(SITEBASE);

	{
	    // Check for config errors
	    List<String> configErrors = new ArrayList<>();
	    if("".equals(directory)){
		configErrors.add("Directory cannot be empty.");
	    }
	    if("".equals(docsBase)) {
		configErrors.add("DocsBase cannot be empty.");
	    }
	    if("".equals(hostname)) {
		configErrors.add("Hostname cannot be empty.");
	    }
	    if(!docsBase.startsWith("https://") && !docsBase.startsWith("http://")) {
		configErrors.add("DocsBase must begin with either http:// or https://");
	    }
	    if(!siteBase.startsWith("https://") && !siteBase.startsWith("http://")) {
		configErrors.add("SiteBase must begin with either http:// or https://");
	    }
	    if(!"localhost".equals(hostname)) {
		if(port < 0 || port > 65535) {
		    configErrors.add("Port must be a number between 0 and 65535.");
		}
		if("".equals(username)) {
		    configErrors.add("Username cannot be empty.");
		}
	    }
	    if (!configErrors.isEmpty()) {
		System.err.println("Invalid input. Check preferences in " + sitedeploy.getAbsolutePath() + " and re-run");
		System.err.println("Here are the following error(s):");
		System.err.println(" - " + StringUtils.Join(configErrors, "\n - "));
		System.exit(1);
	    }
	}

	if (!directory.endsWith("/")) {
	    directory += "/";
	}
	if (!docsBase.endsWith("/")) {
	    docsBase += "/";
	}
	directory += CHVersion.LATEST;
	docsBase += CHVersion.LATEST + "/";
	System.out.println("Using the following settings:");
	System.out.println("username: " + username);
	System.out.println("hostname: " + hostname);
	System.out.println("port: " + port);
	System.out.println("directory: " + directory);
	System.out.println("docs-base: " + docsBase);
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
	DeploymentMethod deploymentMethod;
	if("localhost".equals(hostname)) {
	    deploymentMethod = new LocalDeploymentMethod(directory + "/");
	} else {
	    /**
	     * Remote will be in the format user@remote:port[:password]:/directory/ using the inputs from the user.
	     */
	    String remote = username + "@" + hostname + ":" + port
		    + (password == null || "".equals(password) ? "" : (":" + password)) + ":" + directory + "/";
	    deploymentMethod = new RemoteDeploymentMethod(remote);
	}

	// Ok, all the configuration details are input and correct, so lets deploy now.
	deploy(useLocalCache, siteBase, docsBase, deploymentMethod);
    }

    private static void deploy(boolean useLocalCache, String siteBase, String docsBase,
	    DeploymentMethod deploymentMethod) throws IOException, InterruptedException {
	new SiteDeploy(siteBase, docsBase, useLocalCache, deploymentMethod).deploy();
    }

    private final String siteBase;
    private final String docsBase;
    private final String resourceBase;
    private final jline.console.ConsoleReader reader;
    private final ExecutorService generateQueue;
    private final ExecutorService uploadQueue;
    private final AtomicInteger currentUploadTask = new AtomicInteger(0);
    private final AtomicInteger totalUploadTasks = new AtomicInteger(0);
    private final AtomicInteger currentGenerateTask = new AtomicInteger(0);
    private final AtomicInteger totalGenerateTasks = new AtomicInteger(0);
    private final List<String> filesChanged = new ArrayList<>();
    private final PersistenceNetwork pn;
    private final boolean useLocalCache;
    private final DaemonManager dm = new DaemonManager();
    private Map<String, String> lc = null;
    private DeploymentMethod deploymentMethod;

    @SuppressWarnings("unchecked")
    private SiteDeploy(String siteBase, String docsBase, boolean useLocalCache,
	    DeploymentMethod deploymentMethod) throws IOException {
	this.siteBase = siteBase;
	this.docsBase = docsBase;
	this.resourceBase = docsBase + "resources/";
	this.reader = new jline.console.ConsoleReader();
	this.generateQueue = Executors.newSingleThreadExecutor();
	this.uploadQueue = Executors.newSingleThreadExecutor();
	this.useLocalCache = useLocalCache;
	this.deploymentMethod = deploymentMethod;
	pn = getPersistenceNetwork();
	if(pn != null) {
	    try {
		String localCache = pn.get(new String[]{"site_deploy", "local_cache"});
		if(localCache == null) {
		    localCache = "{}";
		}
		lc = (Map<String, String>) JSONValue.parse(localCache);
	    } catch (DataSourceException | IllegalArgumentException ex) {
		Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, "Could not read in local cache", ex);
		notificationAboutLocalCache = false;
	    }
	}
    }

    /**
     * Returns the persistence network, or null if it couldn't be generated for whatever reason.
     * @return
     */
    public static PersistenceNetwork getPersistenceNetwork() {
	PersistenceNetwork p;
	try {
	    p = new PersistenceNetwork(MethodScriptFileLocations.getDefault().getPersistenceConfig(),
		    new URI("sqlite://" + MethodScriptFileLocations.getDefault().getDefaultPersistenceDBFile()
			    .getCanonicalFile().toURI().getRawSchemeSpecificPart().replace('\\', '/')),
		    new ConnectionMixinFactory.ConnectionMixinOptions());
	} catch (DataSourceException | URISyntaxException | IOException ex) {
	    p = null;
	}
	return p;
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

    private Map<String, Generator> getStandardGenerators() {
	Map<String, Generator> g = new HashMap<>();
	g.put("resourceBase", new Generator() {
	    @Override
	    public String generate(String... args) {
		return SiteDeploy.this.resourceBase;
	    }
	});
	g.put("branding", new Generator() {
	    @Override
	    public String generate(String... args) {
		return Implementation.GetServerType().getBranding();
	    }
	});
	g.put("siteRoot", new Generator() {
	    @Override
	    public String generate(String... args) {
		return SiteDeploy.this.siteBase;
	    }
	});
	g.put("docsBase", new Generator() {
	    @Override
	    public String generate(String... args) {
		return SiteDeploy.this.docsBase;
	    }
	});
	final Generator learningTrailGen = new Generator() {
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
			    } else {
				throw new RuntimeException("Invalid JSON for learning trail");
			    }
			    assert page != null && name != null;
			    boolean exists;
			    if(page.contains(".")) {
				// We can't really check this, because it might be a synthetic page, like
				// api.json. So we just have to set it to true.
				exists = true;
			    } else {
				exists = SiteDeploy.class.getResourceAsStream("/docs/" + page) != null;
			    }
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
	g.put("js_string_learning_trail", new Generator() {
	    @Override
	    public String generate(String... args) throws GenerateException {
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
	deployFrontPages();
	deployLearningTrail();
	deployAPI();
	deployEventAPI();
	deployFunctions();
	deployEvents();
	deployAPIJSON();
	generateQueue.submit(new Runnable() {
	    @Override
	    public void run() {
		uploadQueue.shutdown();
	    }
	});
	generateQueue.shutdown();
	generateQueue.awaitTermination(1, TimeUnit.DAYS);
	uploadQueue.awaitTermination(1, TimeUnit.DAYS);
	dm.waitForThreads();
	deploymentMethod.finish();
	System.out.println();
	System.out.println("Done!");
	System.out.println("Summary of changed files (" + filesChanged.size() + ")");
	System.out.println(StringUtils.Join(filesChanged, "\n"));
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

    private boolean notificationAboutLocalCache = true;
    /**
     * Writes an arbitrary stream to a file on the remote.
     *
     * @param contents The input stream to write
     * @param toLocation The location to write to
     */
    private void writeFromStream(final InputStream contents, final String toLocation) {
	uploadQueue.submit(new Runnable() {
	    @Override
	    public void run() {
		try {
		    // Read the contents only once
		    byte [] c = StreamUtils.GetBytes(contents);
		    contents.close();
		    boolean skipUpload = false;
		    String hash = null;
		    if(pn != null) {
			if(notificationAboutLocalCache) {
			    hash = getLocalMD5(new ByteArrayInputStream(c));
			    try {
				if(lc.containsKey(toLocation)) {
				    if(useLocalCache) {
					String cacheHash = lc.get(toLocation);
					if(cacheHash.equals(hash)) {
					    skipUpload = true;
					}
				    }
				}
			    } catch (IllegalArgumentException ex) {
				Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, "Could not use local cache", ex);
				notificationAboutLocalCache = false;
			    }
			}
		    }
    		    if(!skipUpload && deploymentMethod.deploy(new ByteArrayInputStream(c), toLocation)){
			filesChanged.add(toLocation);
		    }
		    if(pn != null && notificationAboutLocalCache && hash != null) {
			try {
			    lc.put(toLocation, hash);
			    pn.set(dm, new String[]{"site_deploy", "local_cache"}, JSONValue.toJSONString(lc));
			} catch (DataSourceException | ReadOnlyException | IllegalArgumentException ex) {
			    Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, null, ex);
			    notificationAboutLocalCache = false;
			}
		    }
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

    static synchronized String getLocalMD5(InputStream localFile) throws IOException {
	try {
	    byte [] f = StreamUtils.GetBytes(localFile);
	    MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	    digest.update(f);
	    String hash = StringUtils.toHex(digest.digest()).toLowerCase();
	    return hash;
	} catch (NoSuchAlgorithmException ex) {
	    throw new RuntimeException(ex);
	} finally {
	    localFile.close();
	}
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
			Map<String, Generator> standard = getStandardGenerators();
			standard.putAll(DocGenTemplates.GetGenerators());
			b = DocGenTemplates.DoTemplateReplacement(body, standard);
		    } catch (Exception ex) {
			if(ex instanceof GenerateException) {
			    Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, "Failed to substitute template"
				    + " while trying to upload resource to " + toLocation, ex);
			} else {
			    Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, null, ex);
			}
			generateQueue.shutdownNow();
			uploadQueue.shutdownNow();
			return;
		    }
		    // Second, add the template %%body%% and replace that in the frame
		    final Map<String, Generator> g = new HashMap<>();
		    g.put("body", new Generator() {
			@Override
			public String generate(String... args) {
			    return b;
			}
		    });
		    g.put("bodyEscaped", new Generator() {
			@Override
			public String generate(String... args) {
			    String s = b.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'").replaceAll("\n", "\\\\n");
			    s = s.replaceAll("<script.*?</script>", "");
			    return s;
			}
		    });
		    g.put("title", new Generator() {
			@Override
			public String generate(String... args) {
			    return title;
			}
		    });
		    /**
		     * The cacheBuster template is meant to make it easier to deal with caching of resources. The template
		     * allows you to specify the resource, and it creates a path to the resource using resourceBase, but
		     * it also appends a hash of the file, so that as the file changes, so does the hash (using a ?v=hash
		     * query string). Most resources live in /siteDeploy/resources/*, and so the shorthand is to use
		     * %%cacheBuster|path/to/resource.css%%. However, this isn't always correct, because resources can
		     * live all over the place. In that case, you should use the following format:
		     * %%cacheBuster|/absolute/path/to/resource.css|path/to/resource/in/html.css%%
		     */
		    g.put("cacheBuster", new Generator() {
			@Override
			public String generate(String... args) {
			    String resourceLoc = SiteDeploy.this.resourceBase + args[0];
			    String loc = args[0];
			    if(!loc.startsWith("/")) {
				loc = "/siteDeploy/resources/" + loc;
			    } else {
				resourceLoc = SiteDeploy.this.resourceBase + args[1];
			    }
			    String hash = "0";
			    try {
				InputStream in = SiteDeploy.class.getResourceAsStream(loc);
				if(in == null) {
				    throw new RuntimeException("Could not find " + loc + " in resources folder for cacheBuster template");
				}
				hash = getLocalMD5(in);
			    } catch (IOException ex) {
				Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, null, ex);
			    }
			    return resourceLoc + "?v=" + hash;
			}

		    });
		    g.put("useHttps", new Generator() {
			@Override
			public String generate(String... args) {
			    return SiteDeploy.this.siteBase.startsWith("https") ? "true" : "false";
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
	    Queue<File> q = new LinkedList<>();
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
	try {
	    writeFromString(DocGenTemplates.DoTemplateReplacement(index_js, getStandardGenerators()), "resources/js/index.js");
	} catch (Generator.GenerateException ex) {
	    Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, "GenerateException in /siteDeploy/index.js", ex);
	}
    }

    /**
     * Pages deployed: index.html privacy_policy.html
     */
    private void deployFrontPages() {
	writePageFromResource(CHVersion.LATEST.toString() + " - Docs", "/siteDeploy/VersionFrontPage", "index.html");
	writePageFromResource("Privacy Policy", "/siteDeploy/privacy_policy.html", "privacy_policy.html");
	writePageFromResource(Implementation.GetServerType().getBranding(), "/siteDeploy/FrontPage", "../../index.html");
	writePageFromResource("Doc Directory", "/siteDeploy/DocDirectory", "../index.html");
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
    private void deployAPIJSON() {
	generateQueue.submit(new Runnable() {
	    @Override
	    public void run() {
		Map<String, Object> json = new APIBuilder().build();
		writeFromString(JSONValue.toJSONString(json), "api.json");
		currentGenerateTask.addAndGet(1);
	    }
	});
	totalGenerateTasks.addAndGet(1);
    }
}
