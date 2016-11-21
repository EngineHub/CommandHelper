package com.laytonsmith.tools.docgen.sitedeploy;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.GNUErrorMessageFormat;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Web.HTTPMethod;
import com.laytonsmith.PureUtilities.Web.HTTPResponse;
import com.laytonsmith.PureUtilities.Web.RequestSettings;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.ReadOnlyException;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import com.laytonsmith.tools.docgen.DocGen;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import com.laytonsmith.tools.docgen.DocGenTemplates.Generator;
import com.laytonsmith.tools.docgen.DocGenTemplates.Generator.GenerateException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import org.json.simple.JSONValue;

/**
 * This class is responsible for deploying documentation to an html webserver. It converts wiki markup to html, and uses
 * scp to transfer the complete website structure to the specified location.
 *
 * @author cailin
 */
public class SiteDeploy {

    private static final String USERNAME = "username";
    private static final String HOSTNAME = "hostname";
    private static final String PORT = "port";
    private static final String DIRECTORY = "directory";
    private static final String PASSWORD = "use-password";
    private static final String DOCSBASE = "docs-base";
    private static final String SITEBASE = "site-base";
    private static final String SHOW_TEMPLATE_CREDIT = "show-template-credit";
    private static final String GITHUB_BASE_URL = "github-base-url";
    private static final String VALIDATOR_URL = "validator-url";

    public static void run(boolean generate_prefs, boolean useLocalCache, File sitedeploy, String password, boolean doValidation) throws Exception {
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

	// Other options
	defaults.add(new Preferences.Preference(SHOW_TEMPLATE_CREDIT, "true", Preferences.Type.BOOLEAN, "Whether or not"
		+ " to show the template credit. (Design by TEMPLATED logo in bottom.) If you set this to false, you"
		+ " agree that you have purchased a license for your deployment, and are legally allowed to supress"
		+ " this from the templates."));
	defaults.add(new Preferences.Preference(GITHUB_BASE_URL, "", Preferences.Type.STRING, "The base url for"
		+ " the github project. If empty string, then the value " + DEFAULT_GITHUB_BASE_URL + " is used."));
	defaults.add(new Preferences.Preference(VALIDATOR_URL, "", Preferences.Type.STRING, "The validator url."
		+ " This service must be based on the https://validator.github.io/validator/ service. If the url"
		+ " is left blank, then using the --do-validation flag is an error. Generally, you will need"
		+ " to host your own validator for this solution to work, as running against a public service"
		+ " will undoubtably result in being blacklisted from the service. This should be something"
		+ " like http://localhost:8888/"));

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
	Boolean showTemplateCredit = (Boolean) prefs.getPreference(SHOW_TEMPLATE_CREDIT);
	String githubBaseUrl = (String) prefs.getPreference(GITHUB_BASE_URL);
	String validatorUrl = (String) prefs.getPreference(VALIDATOR_URL);

	{
	    // Check for config errors
	    List<String> configErrors = new ArrayList<>();
	    if ("".equals(directory)) {
		configErrors.add("Directory cannot be empty.");
	    }
	    if ("".equals(docsBase)) {
		configErrors.add("DocsBase cannot be empty.");
	    }
	    if ("".equals(hostname)) {
		configErrors.add("Hostname cannot be empty.");
	    }
	    if (!docsBase.startsWith("https://") && !docsBase.startsWith("http://")) {
		configErrors.add("DocsBase must begin with either http:// or https://");
	    }
	    if (!siteBase.startsWith("https://") && !siteBase.startsWith("http://")) {
		configErrors.add("SiteBase must begin with either http:// or https://");
	    }
	    if (!"localhost".equals(hostname)) {
		if (port < 0 || port > 65535) {
		    configErrors.add("Port must be a number between 0 and 65535.");
		}
		if ("".equals(username)) {
		    configErrors.add("Username cannot be empty.");
		}
	    }
	    if (doValidation && "".equals(validatorUrl)) {
		configErrors.add("Validation cannot occur while an empty validation url is specified in the config."
			+ " Either set a validator url, or re-run without the --do-validation flag.");
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
	System.out.println("Using the following settings, loaded from " + sitedeploy.getCanonicalPath());
	System.out.println("username: " + username);
	System.out.println("hostname: " + hostname);
	System.out.println("port: " + port);
	System.out.println("directory: " + directory);
	System.out.println("docs-base: " + docsBase);
	System.out.println("site-base: " + siteBase);
	System.out.println("github-base-url: " + githubBaseUrl);
	if (doValidation) {
	    System.out.println("validator-url: " + validatorUrl);
	}

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
	if ("localhost".equals(hostname)) {
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
	deploy(useLocalCache, siteBase, docsBase, deploymentMethod, doValidation,
		showTemplateCredit, githubBaseUrl, validatorUrl);
    }

    private static void deploy(boolean useLocalCache, String siteBase, String docsBase,
	    DeploymentMethod deploymentMethod, boolean doValidation, boolean showTemplateCredit,
	    String githubBaseUrl, String validatorUrl) throws IOException, InterruptedException {
	new SiteDeploy(siteBase, docsBase, useLocalCache, deploymentMethod, doValidation,
		showTemplateCredit, githubBaseUrl, validatorUrl).deploy();
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
    private final boolean doValidation;
    private final Map<String, String> uploadedPages = new HashMap<>();
    private final boolean showTemplateCredit;
    private final String githubBaseUrl;
    private final String validatorUrl;

    private static final String EDIT_THIS_PAGE_PREAMBLE = "Find a bug in this page? <a rel=\"noopener noreferrer\" target=\"_blank\" href=\"";
    private static final String EDIT_THIS_PAGE_POSTAMBLE = "\">Edit this page yourself, then submit a pull request.</a>";
    private static final String DEFAULT_GITHUB_BASE_URL = "https://github.com/EngineHub/CommandHelper/edit/master/src/main/%s";

    @SuppressWarnings("unchecked")
    private SiteDeploy(String siteBase, String docsBase, boolean useLocalCache,
	    DeploymentMethod deploymentMethod, boolean doValidation, boolean showTemplateCredit,
	    String githubBaseUrl, String validatorUrl) throws IOException {
	this.siteBase = siteBase;
	this.docsBase = docsBase;
	this.resourceBase = docsBase + "resources/";
	this.reader = new jline.console.ConsoleReader();
	this.generateQueue = Executors.newSingleThreadExecutor();
	this.uploadQueue = Executors.newSingleThreadExecutor();
	this.useLocalCache = useLocalCache;
	this.deploymentMethod = deploymentMethod;
	this.doValidation = doValidation;
	this.showTemplateCredit = showTemplateCredit;
	this.validatorUrl = validatorUrl;
	if (githubBaseUrl.equals("")) {
	    githubBaseUrl = DEFAULT_GITHUB_BASE_URL;
	}
	this.githubBaseUrl = githubBaseUrl;
	pn = getPersistenceNetwork();
	if (pn != null) {
	    try {
		String localCache = pn.get(new String[]{"site_deploy", "local_cache"});
		if (localCache == null) {
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
     *
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

    private synchronized void writeStatus(String additionalInfo) {
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
		+ "; " + additionalInfo;
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
	/**
	 * The cacheBuster template is meant to make it easier to deal with caching of resources. The template allows
	 * you to specify the resource, and it creates a path to the resource using resourceBase, but it also appends a
	 * hash of the file, so that as the file changes, so does the hash (using a ?v=hash query string). Most
	 * resources live in /siteDeploy/resources/*, and so the shorthand is to use
	 * %%cacheBuster|path/to/resource.css%%. However, this isn't always correct, because resources can live all over
	 * the place. In that case, you should use the following format:
	 * %%cacheBuster|/absolute/path/to/resource.css|path/to/resource/in/html.css%%
	 */
	g.put("cacheBuster", new Generator() {
	    @Override
	    public String generate(String... args) {
		String resourceLoc = SiteDeploy.this.resourceBase + args[0];
		String loc = args[0];
		if (!loc.startsWith("/")) {
		    loc = "/siteDeploy/resources/" + loc;
		} else {
		    resourceLoc = SiteDeploy.this.resourceBase + args[1];
		}
		String hash = "0";
		try {
		    InputStream in = SiteDeploy.class.getResourceAsStream(loc);
		    if (in == null) {
			throw new RuntimeException("Could not find " + loc + " in resources folder for cacheBuster template");
		    }
		    hash = getLocalMD5(in);
		} catch (IOException ex) {
		    Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, null, ex);
		}
		return resourceLoc + "?v=" + hash;
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
			    if (page.contains(".")) {
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
	/**
	 * If showTemplateCredit is false, then this will return "display: none;" otherwise, it will return an empty
	 * string.
	 */
	g.put("showTemplateCredit", new Generator() {
	    @Override
	    public String generate(String... args) throws GenerateException {
		return showTemplateCredit ? "" : "display: none;";
	    }
	});
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
	deployObjects();
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
	// Next, we need to validate the pages
	System.out.println();
	if (doValidation) {
	    System.out.println("Upload complete, running html5 validation");
	    int filesValidated = 0;
	    int specifiedErrors = 0;
	    try {
		for (Map.Entry<String, String> e : uploadedPages.entrySet()) {
		    Map<String, List<String>> headers = new HashMap<>();
		    RequestSettings settings = new RequestSettings();
		    //settings.setLogger(Logger.getLogger(SiteDeploy.class.getName()));
		    settings.setFollowRedirects(true);
		    headers.put("Content-Type", Arrays.asList(new String[]{"text/html; charset=utf-8"}));
		    headers.put("Content-Encoding", Arrays.asList(new String[]{"gzip"}));
		    headers.put("Accept-Encoding", Arrays.asList(new String[]{"gzip"}));
		    settings.setHeaders(headers);

		    byte[] outStream = e.getValue().getBytes("UTF-8");
		    ByteArrayOutputStream out = new ByteArrayOutputStream(outStream.length);
		    try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
			gz.write(outStream);
		    }
		    byte[] param = out.toByteArray();
		    settings.setRawParameter(param);
		    settings.setTimeout(10000);
		    settings.setMethod(HTTPMethod.POST);
		    HTTPResponse response = WebUtility.GetPage(new URL(validatorUrl + "?out=gnu"), settings);
		    System.out.println(Static.MCToANSIColors("Response for "
			    + MCChatColor.AQUA + e.getKey() + MCChatColor.PLAIN_WHITE + ":"));
		    if (response.getResponseCode() != 200) {
			System.out.println(response.getContent());
			throw new IOException("Response was non-200, refusing to continue with validation");
		    }
		    String[] errors = response.getContent().split("\n");
		    int errorsDisplayed = 0;
		    for (String error : errors) {
			GNUErrorMessageFormat gnuError = new GNUErrorMessageFormat(error);
			String supressWarning = "info warning: Section lacks heading. Consider using “h2”-“h6”"
				+ " elements to add identifying headings to all sections.";
			if (supressWarning.equals(gnuError.message())) {
			    continue;
			}
			StringBuilder output = new StringBuilder();
			switch (gnuError.messageType()) {
			    case ERROR:
				output.append(MCChatColor.RED);
				break;
			    case WARNING:
				output.append(MCChatColor.GOLD);
				break;
			}
			output.append("line ").append(gnuError.fromLine()).append(" ")
				.append(gnuError.message()).append(MCChatColor.PLAIN_WHITE);
			String[] page = e.getValue().split("\n");
			for (int i = gnuError.fromLine(); i < gnuError.toLine() + 1; i++) {
			    output.append("\n").append(page[i - 1]);
			}
			output.append("\n");
			for (int i = 0; i < gnuError.fromColumn() - 1; i++) {
			    output.append(" ");
			}
			output.append(MCChatColor.RED).append("^").append(MCChatColor.PLAIN_WHITE);
			System.out.println(Static.MCToANSIColors(output.toString()));
			specifiedErrors++;
			errorsDisplayed++;
		    }
		    if (errorsDisplayed == 0) {
			System.out.println(Static.MCToANSIColors(MCChatColor.GREEN.toString())
				+ "No errors" + Static.MCToANSIColors(MCChatColor.PLAIN_WHITE.toString()));
		    }
		    filesValidated++;
		}
	    } catch (IOException ex) {
		System.err.println("Validation could not occur due to the following exception: " + ex.getMessage());
		ex.printStackTrace(System.err);
	    }
	    System.out.println("Files validated: " + filesValidated);
	    System.out.println("Errors found: " + specifiedErrors);
	}
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
		    writeStatus("Currently uploading " + toLocation);
		    // Read the contents only once
		    byte[] c = StreamUtils.GetBytes(contents);
		    contents.close();
		    boolean skipUpload = false;
		    String hash = null;
		    if (pn != null) {
			if (notificationAboutLocalCache) {
			    hash = getLocalMD5(new ByteArrayInputStream(c));
			    try {
				if (lc.containsKey(deploymentMethod.getID() + toLocation)) {
				    if (useLocalCache) {
					String cacheHash = lc.get(deploymentMethod.getID() + toLocation);
					if (cacheHash.equals(hash)) {
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
		    if (!skipUpload && deploymentMethod.deploy(new ByteArrayInputStream(c), toLocation)) {
			filesChanged.add(toLocation);
		    }
		    if (pn != null && notificationAboutLocalCache && hash != null) {
			try {
			    lc.put(deploymentMethod.getID() + toLocation, hash);
			    pn.set(dm, new String[]{"site_deploy", "local_cache"}, JSONValue.toJSONString(lc));
			} catch (DataSourceException | ReadOnlyException | IllegalArgumentException ex) {
			    Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, null, ex);
			    notificationAboutLocalCache = false;
			}
		    }
		    currentUploadTask.addAndGet(1);
		    writeStatus("");
		} catch (Throwable ex) {
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
	    byte[] f = StreamUtils.GetBytes(localFile);
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
     * @param title The title of the page
     * @param resource The absolute path (or path relative to SiteDeploy.java) of the resource to use
     * @param toLocation The location on the remote server
     * @param keywords A list of keywords to be added to the meta tag on the page
     * @param description A description of the page
     * @return
     */
    private void writePageFromResource(String title, String resource, String toLocation, List<String> keywords,
	    String description) {
	String s = StreamUtils.GetString(SiteDeploy.class.getResourceAsStream(resource));
	s += "<p id=\"edit_this_page\">"
		+ EDIT_THIS_PAGE_PREAMBLE
		+ String.format(githubBaseUrl, "resources" + resource)
		+ EDIT_THIS_PAGE_POSTAMBLE
		+ "</p>";
	writePage(title.replace("_", " "), s, toLocation, keywords, description);
    }

    /**
     * Most pages should use this method instead of the other methods. This takes care of all the steps, including
     * substituting the body into the frame, and handling all the other connections.
     *
     * @param title The title of the page
     * @param resource The absolute path (or path relative to SiteDeploy.java) of the resource to use
     * @param toLocation The location on the remote server
     * @return
     */
    private void writePageFromResource(String title, String resource, String toLocation) {
	writePageFromResource(title, resource, toLocation, null, "");
    }

    /**
     * Most pages should use this method instead of the other methods. This takes care of all the steps, including
     * substituting the body into the frame, and handling all the other connections.
     *
     * @param title The title of the page
     * @param body The content body
     * @param toLocation the location on the remote server
     */
    private void writePage(String title, String body, String toLocation) {
	writePage(title, body, toLocation, null, "");
    }

    /**
     * Most pages should use this method instead of the other methods. This takes care of all the steps, including
     * substituting the body into the frame, and handling all the other connections.
     *
     * @param body The content body
     * @param title The title of the page
     * @param toLocation the location on the remote server
     * @param keywords A list of keywords to be added to the meta tag on the page
     * @param description A description of the page's content
     */
    private void writePage(final String title, final String body, final String toLocation,
	    List<String> keywords, final String description) {
	if (keywords == null) {
	    keywords = new ArrayList<>();
	}
	final List<String> kw = keywords;
	generateQueue.submit(new Runnable() {
	    @Override
	    public void run() {
		String bW = body;
		if (!bW.contains(EDIT_THIS_PAGE_PREAMBLE)) {
		    bW += "<p id=\"edit_this_page\">"
			    + EDIT_THIS_PAGE_PREAMBLE
			    + "java/" + String.format(githubBaseUrl, SiteDeploy.class.getName().replace(".", "/")) + ".java"
			    + EDIT_THIS_PAGE_POSTAMBLE
			    + "</p>";
		}
		try {
		    writeStatus("Currently generating " + toLocation);
		    // First, substitute the templates in the body
		    final String b;
		    try {
			Map<String, Generator> standard = getStandardGenerators();
			standard.putAll(DocGenTemplates.GetGenerators());
			b = DocGenTemplates.DoTemplateReplacement(bW, standard);
		    } catch (Exception ex) {
			if (ex instanceof GenerateException) {
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
		    g.put("useHttps", new Generator() {
			@Override
			public String generate(String... args) {
			    return SiteDeploy.this.siteBase.startsWith("https") ? "true" : "false";
			}
		    });
		    g.put("keywords", new Generator() {
			@Override
			public String generate(String... args) throws GenerateException {
			    List<String> k = new ArrayList<>(kw);
			    k.add(Implementation.GetServerType().getBranding());
			    return StringUtils.Join(k, ", ");
			}
		    });
		    g.put("description", new Generator() {
			@Override
			public String generate(String... args) throws GenerateException {
			    return description;
			}
		    });
		    g.putAll(getStandardGenerators());
		    g.putAll(DocGenTemplates.GetGenerators());
		    String frame = StreamUtils.GetString(SiteDeploy.class.getResourceAsStream("/siteDeploy/frame.html"));
		    final String bb = DocGenTemplates.DoTemplateReplacement(frame, g);
		    // Write out using writeFromString
		    uploadedPages.put(toLocation, bb);
		    writeFromString(bb, toLocation);
		    currentGenerateTask.addAndGet(1);
		    writeStatus("");
		} catch (Exception ex) {
		    Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, "While writing " + toLocation + " the following error occured:", ex);
		}
	    }
	});
	totalGenerateTasks.addAndGet(1);
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
	writePageFromResource(CHVersion.LATEST.toString() + " - Docs", "/siteDeploy/VersionFrontPage", "index.html",
		Arrays.asList(new String[]{CHVersion.LATEST.toString()}), "Front page for " + CHVersion.LATEST.toString());
	writePageFromResource("Privacy Policy", "/siteDeploy/privacy_policy.html", "privacy_policy.html",
		Arrays.asList(new String[]{"privacy policy"}), "Privacy policy for the site");
	writePageFromResource(Implementation.GetServerType().getBranding(), "/siteDeploy/FrontPage", "../../index.html",
		Arrays.asList(new String[]{"index", "front page"}), "The front page for " + Implementation.GetServerType().getBranding());
	writePageFromResource("Doc Directory", "/siteDeploy/DocDirectory", "../index.html",
		Arrays.asList(new String[]{"directory"}), "The directory for all documented versions");
	writePageFromResource("404 Not Found", "/siteDeploy/404", "../../404.html",
		Arrays.asList(new String[]{"404"}), "Page not found");
    }

    /**
     * Pages deployed: All files from /docs/*
     */
    private void deployLearningTrail() throws IOException {
	File root = new File(SiteDeploy.class.getResource("/docs").toExternalForm());
	ZipReader zReader = new ZipReader(root);
	for (File r : zReader.listFiles()) {
	    String filename = r.getAbsolutePath().replaceFirst(Pattern.quote(zReader.getFile().getAbsolutePath()), "");
	    writePageFromResource(r.getName(), "/docs" + filename, r.getName() + ".html",
		    Arrays.asList(new String[]{r.getName().replace("_", " ")}), "Learning trail page for " + r.getName().replace("_", " "));
	}
    }

    private void deployAPI() {
	generateQueue.submit(new Runnable() {
	    @Override
	    public void run() {
		try {
		    Set<Class<? extends Function>> functionClasses = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(api.class, Function.class);
		    // A map of where it maps the enclosing class to the list of function rows, which contains a list of
		    // table cells.
		    Map<Class<?>, List<List<String>>> data = new TreeMap<>(new Comparator<Class<?>>() {
			@Override
			public int compare(Class<?> o1, Class<?> o2) {
			    return o1.getCanonicalName().compareTo(o2.getCanonicalName());
			}
		    });
		    for (Class<? extends Function> functionClass : functionClasses) {
			if (!data.containsKey(functionClass.getEnclosingClass())) {
			    data.put(functionClass.getEnclosingClass(), new ArrayList<List<String>>());
			}
			List<List<String>> d = data.get(functionClass.getEnclosingClass());
			List<String> c = new ArrayList<>();
			// function name, returns, arguments, throws, description, since, restricted
			Function f;
			try {
			    f = ReflectionUtils.instantiateUnsafe(functionClass);
			} catch (ReflectionUtils.ReflectionException ex) {
			    throw new RuntimeException("While trying to construct " + functionClass + ", got the following", ex);
			}
			DocGen.DocInfo di = new DocGen.DocInfo(f.docs());
			c.add(f.getName());
			c.add(di.ret);
			c.add(di.args);
			List<String> exc = new ArrayList<>();
			if (f.thrown() != null) {
			    for (Class<? extends CREThrowable> e : f.thrown()) {
				CREThrowable ct = ReflectionUtils.instantiateUnsafe(e);
				exc.add("{{object|" + ct.getName() + "}}");
			    }
			}
			c.add(StringUtils.Join(exc, "<br>"));
			StringBuilder desc = new StringBuilder();
			desc.append(di.desc);
			if (di.extendedDesc != null) {
			    desc.append(" [[functions/").append(f.getName()).append("|See more...]]");
			}
			try {
			    if (f.examples() != null && f.examples().length > 0) {
				desc.append("<br>([[functions/").append(f.getName()).append("#Examples|Examples...]]");
			    }
			} catch (ConfigCompileException ex) {
			    Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, null, ex);
			}
			c.add(desc.toString());
			c.add(f.since().toString());
			c.add("<span class=\"api_" + (f.isRestricted() ? "yes" : "no") + "\">" + (f.isRestricted() ? "Yes" : "No")
				+ "</span>");
			d.add(c);
		    }
		    // data is now constructed.
		    StringBuilder b = new StringBuilder();
		    for (Map.Entry<Class<?>, List<List<String>>> e : data.entrySet()) {
			Class<?> clazz = e.getKey();
			b.append("== ").append(clazz.getSimpleName()).append(" ==\n");
			String docs = (String) ReflectionUtils.invokeMethod(clazz, null, "docs");
			b.append("<p>").append(docs).append("</p>");
		    }

		    writePage("API", b.toString(), "API",
			    Arrays.asList(new String[]{"API", "functions"}),
			    "A list of all " + Implementation.GetServerType().getBranding() + " functions");
		    currentGenerateTask.addAndGet(1);
		} catch (Exception ex) {
		    ex.printStackTrace(System.err);
		}
	    }
	});
	totalGenerateTasks.addAndGet(1);
    }

    private void deployEventAPI() {

    }

    private void deployFunctions() {

    }

    private void deployEvents() {

    }

    private void deployObjects() {

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
