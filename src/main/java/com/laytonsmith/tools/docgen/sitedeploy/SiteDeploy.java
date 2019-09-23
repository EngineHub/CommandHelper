package com.laytonsmith.tools.docgen.sitedeploy;

import com.laytonsmith.tools.docgen.localization.TranslationMaster;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.MethodMirror;
import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.GNUErrorMessageFormat;
import com.laytonsmith.PureUtilities.Common.HTMLUtils;
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
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.ExampleScript;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.PersistenceNetworkImpl;
import com.laytonsmith.persistence.ReadOnlyException;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import com.laytonsmith.tools.Interpreter;
import com.laytonsmith.tools.docgen.DocGen;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import com.laytonsmith.tools.docgen.DocGenTemplates.Generator;
import com.laytonsmith.tools.docgen.DocGenTemplates.Generator.GenerateException;
import com.laytonsmith.tools.docgen.templates.Template;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
@SuppressWarnings("UseSpecificCatch")
public final class SiteDeploy {

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
	private static final String POST_SCRIPT = "post-script";

	private static final String TRANSLATION_MEMORY_DB = "translation-memory-db";
	private static final String PRODUCTION_TRANSLATIONS = "production-translations";

	private static final String INSTALL_URL = "install-url";
	private static final String INSTALL_PEM_FILE = "install-pem-file";
	private static final String INSTALL_PUB_KEYS = "install-pub-keys";

	private static final String PRODUCTION_TRANSLATIONS_URL
			= "https://raw.githubusercontent.com/LadyCailin/MethodScriptTranslationDB/master/";

	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public static void run(boolean generatePrefs, boolean useLocalCache, File sitedeploy, String password,
			boolean doValidation, boolean clearProgressBar, String overridePostScript,
			String overrideIdRsa) throws Exception {
		List<Preferences.Preference> defaults = new ArrayList<>();
		// SCP Options
		defaults.add(new Preferences.Preference(USERNAME, "", Preferences.Type.STRING, "The username to scp with"));
		defaults.add(new Preferences.Preference(HOSTNAME, "", Preferences.Type.STRING, "The hostname to scp to (not the"
				+ " hostname of the site). If"
				+ " the hostname is \"localhost\", this triggers special handling, which skips the upload, and simply"
				+ " saves to the specified location on local disk. This should work with all OSes, otherwise the host"
				+ " that this connects to must support ssh, though it does not necessarily have to be a unix"
				+ " based system."
				+ " If the value is localhost, the values " + USERNAME + ", " + PORT + ", and " + PASSWORD + " are"
				+ " irrelevant, and not used. This should NOT begin with a protocol, i.e. http://"));
		defaults.add(new Preferences.Preference(PORT, "22", Preferences.Type.INT, "The port to use for SCP"));
		defaults.add(new Preferences.Preference(DIRECTORY, "/var/www/docs", Preferences.Type.STRING,
				"The root location of"
				+ " the remote web server. This must be an absolute path. Note that this is the location of"
				+ " the *docs* folder. Files may be created one directory above this folder, in this folder, and in"
				+ " lower folders that are created by the site deploy tool. So if /var/www is your web"
				+ " root, then you should put /var/www/docs here. It will create an index file in /var/www, as"
				+ " well as in /var/www/docs, but the majority of files will be put in /var/www/docs/."));
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
		defaults.add(new Preferences.Preference(POST_SCRIPT, "", Preferences.Type.FILE,
				"The path to a shell script, or an mscript. This will be executed after"
				+ " the upload (and validation, if specified) is complete, and can be used"
				+ " to run custom procedures afterwards, for instance clearing any systems'"
				+ " caches as might be necessary. The script will be sent a list of all the"
				+ " the changed files as arguments. If the file name ends with .ms, it will"
				+ " be executed with the MethodScript engine. Otherwise, the file will be"
				+ " executed using the system shell. Leave this option empty to skip this"
				+ " step. If the file is specified, it must exist, and if it does not end"
				+ " in .ms, it must be executable."));
		defaults.add(new Preferences.Preference(INSTALL_URL, "", Preferences.Type.STRING, "The ec2 instance public url."
				+ " NOTE: The security group of the instance must be configured to allow access to port 22."
				+ " Ports 80 and"
				+ " 443 are also used, and should be opened, but that will not affect the installation process."));
		defaults.add(new Preferences.Preference(INSTALL_PEM_FILE, "", Preferences.Type.STRING,
				"The path to the PEM file"
				+ " used for initial login."));
		defaults.add(new Preferences.Preference(INSTALL_PUB_KEYS, "", Preferences.Type.STRING, "A list of public keys"
				+ " to upload to, and add to the authorized_keys file on the server. These keys will not"
				+ " be used by this script, but can allow easier login in the future. If blank, no additional keys"
				+ " will be uploaded."));
		defaults.add(new Preferences.Preference(TRANSLATION_MEMORY_DB, "", Preferences.Type.FILE, "The path to a"
				+ " local checkout of a translation memory database. This may be empty, in which case"
				+ " internationalization options will not be available on the deployed site."));
		defaults.add(new Preferences.Preference(PRODUCTION_TRANSLATIONS, "", Preferences.Type.STRING, "The base url"
				+ " for the production version of the localization database. If blank, the official url is used, but"
				+ " this should be set to your fork of the official repository, or a local server that serves the"
				+ " translations if you are testing localizations."));

		Preferences prefs = new Preferences("Site-Deploy", Logger.getLogger(SiteDeploy.class.getName()), defaults);
		if(generatePrefs) {
			prefs.init(sitedeploy);
			System.out.println("Preferences file is now located at " + sitedeploy.getAbsolutePath()
					+ ". Please fill in the"
					+ " values, then re-run this command without the --generate-prefs option.");
			System.exit(0);
		}
		prefs.init(sitedeploy);

		String username = prefs.getStringPreference(USERNAME);
		String hostname = prefs.getStringPreference(HOSTNAME);
		Integer port = prefs.getIntegerPreference(PORT);
		String directory = prefs.getStringPreference(DIRECTORY);
		Boolean usePassword = prefs.getBooleanPreference(PASSWORD);
		String docsBase = prefs.getStringPreference(DOCSBASE);
		String siteBase = prefs.getStringPreference(SITEBASE);
		Boolean showTemplateCredit = prefs.getBooleanPreference(SHOW_TEMPLATE_CREDIT);
		String githubBaseUrl = prefs.getStringPreference(GITHUB_BASE_URL);
		String validatorUrl = prefs.getStringPreference(VALIDATOR_URL);
		File finalizerScript = prefs.getFilePreference(POST_SCRIPT);
		File translationMemoryDb = prefs.getFilePreference(TRANSLATION_MEMORY_DB);
		String productionTranslations = prefs.getStringPreference(PRODUCTION_TRANSLATIONS);


		if(!overridePostScript.equals("")) {
			finalizerScript = new File(overridePostScript);
		}

		{
			// Check for config errors
			List<String> configErrors = new ArrayList<>();
			if("".equals(directory)) {
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
			if(doValidation && "".equals(validatorUrl)) {
				configErrors.add("Validation cannot occur while an empty validation url is specified in the config."
						+ " Either set a validator url, or re-run without the --do-validation flag.");
			}
			if(finalizerScript != null) {
				if(!finalizerScript.exists()) {
					configErrors.add("post-script file specified does not exist (" + finalizerScript.getCanonicalPath()
							+ ")");
				} else if(!finalizerScript.getPath().endsWith(".ms") && !finalizerScript.canExecute()) {
					configErrors.add("post-script does not end in .ms, and is not executable");
				}
			}
			if(overrideIdRsa != null) {
				if(!new File(overrideIdRsa).exists()) {
					configErrors.add("override-id-rsa parameter points to a non-existent file.");
				}
			}
			if(translationMemoryDb != null && !translationMemoryDb.exists()) {
				configErrors.add("Translation memory db must point to an existing database. (" + translationMemoryDb
						+ ")");
			}
			if("".equals(productionTranslations)) {
				productionTranslations = PRODUCTION_TRANSLATIONS_URL;
			}

			try {
				new URL(productionTranslations);
			} catch (MalformedURLException e) {
				configErrors.add("Invalid URL for " + PRODUCTION_TRANSLATIONS + " value: "
						+ productionTranslations);
			}

			if(!configErrors.isEmpty()) {
				System.err.println("Invalid input. Check preferences in " + sitedeploy.getAbsolutePath()
						+ " and re-run");
				System.err.println(StringUtils.PluralTemplateHelper(configErrors.size(),
						"Here is the %d error:", "Here are the %d errors:"));
				System.err.println(" - " + StringUtils.Join(configErrors, "\n - "));
				System.exit(1);
			}
		}

		if(!directory.endsWith("/")) {
			directory += "/";
		}
		if(!docsBase.endsWith("/")) {
			docsBase += "/";
		}
		directory += MSVersion.LATEST;
		docsBase += MSVersion.LATEST + "/";
		System.out.println("Using the following settings, loaded from " + sitedeploy.getCanonicalPath());
		System.out.println("username: " + username);
		System.out.println("hostname: " + hostname);
		System.out.println("port: " + port);
		System.out.println("directory: " + directory);
		System.out.println("docs-base: " + docsBase);
		System.out.println("site-base: " + siteBase);
		System.out.println("github-base-url: " + githubBaseUrl);
		if(translationMemoryDb != null) {
			System.out.println("Translation memory database: " + translationMemoryDb);
		}
		if(productionTranslations != null) {
			System.out.println("Production translations url: " + productionTranslations);
		}
		if(finalizerScript != null) {
			System.out.println("post-script: " + finalizerScript.getCanonicalPath());
		}
		if(doValidation) {
			System.out.println("validator-url: " + validatorUrl);
		}

		if(usePassword && password != null) {
			jline.console.ConsoleReader reader = null;
			try {
				Character cha = (char) 0;
				reader = new jline.console.ConsoleReader();
				reader.setExpandEvents(false);
				password = reader.readLine("Please enter your password: ", cha);
			} finally {
				if(reader != null) {
					reader.close();
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
		deploy(useLocalCache, siteBase, docsBase, deploymentMethod, doValidation,
				showTemplateCredit, githubBaseUrl, validatorUrl, finalizerScript, clearProgressBar, overrideIdRsa,
				translationMemoryDb, productionTranslations);
	}

	private static void deploy(boolean useLocalCache, String siteBase, String docsBase,
			DeploymentMethod deploymentMethod, boolean doValidation, boolean showTemplateCredit,
			String githubBaseUrl, String validatorUrl, File finalizerScript, boolean clearProgressBar,
			String overrideIdRsa, File translationMemoryDb, String productionTranslations)
			throws IOException, InterruptedException {
		new SiteDeploy(siteBase, docsBase, useLocalCache, deploymentMethod, doValidation,
				showTemplateCredit, githubBaseUrl, validatorUrl, finalizerScript, clearProgressBar,
				overrideIdRsa, translationMemoryDb, productionTranslations).deploy();
	}

	String apiJson;
	String apiJsonVersion;

	@SuppressWarnings({"StringEquality", "ImplicitArrayToString"})
	private void deploy() throws InterruptedException, IOException {
		apiJson = JSONValue.toJSONString(new APIBuilder().build());
		apiJsonVersion = getLocalMD5(StreamUtils.GetInputStream(apiJson));
		deployAPI();
		deployEventAPI();
		deployAPIJSON();
		deployFrontPages();
		deployLearningTrail();
		deployEvents();
		deployObjects();
		deployResources();
		deployJar();
		Runnable generateFinalizer = new Runnable() {
			@Override
			public void run() {
				// Just us left, shut us down
				if(generateQueue.getQueue().isEmpty()) {
					generateQueue.shutdown();
				} else {
					// Oops, we're a bit premature. Schedule us to run again.
					generateQueue.submit(this);
				}
			}
		};
		generateQueue.submit(generateFinalizer);
		generateQueue.awaitTermination(1, TimeUnit.DAYS);
		Runnable uploadFinalizer = new Runnable() {
			@Override
			public void run() {
				if(uploadQueue.getQueue().isEmpty()) {
					try {
						writeMasterTranslations();
					} catch (Throwable ex) {
						writeLog("Could not write out translations!", ex);
					}
					uploadQueue.shutdown();
				} else {
					uploadQueue.submit(this);
				}
			}
		};
		uploadQueue.submit(uploadFinalizer);
		uploadQueue.awaitTermination(1, TimeUnit.DAYS);
		dm.waitForThreads();
		deploymentMethod.finish();
		// Next, we need to validate the pages
		System.out.println();
		if(doValidation) {
			System.out.println("Upload complete, running html5 validation");
			int filesValidated = 0;
			int specifiedErrors = 0;
			try {
				for(Map.Entry<String, String> e : uploadedPages.entrySet()) {
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
					try(GZIPOutputStream gz = new GZIPOutputStream(out)) {
						gz.write(outStream);
					}
					byte[] param = out.toByteArray();
					settings.setRawParameter(param);
					settings.setTimeout(10000);
					settings.setMethod(HTTPMethod.POST);
					HTTPResponse response = WebUtility.GetPage(new URL(validatorUrl + "?out=gnu"), settings);

					if(response.getResponseCode() != 200) {
						System.out.println(Static.MCToANSIColors("Response for "
								+ MCChatColor.AQUA + e.getKey() + MCChatColor.PLAIN_WHITE + ":"));
						System.out.println(response.getContent());
						throw new IOException("Response was non-200, refusing to continue with validation");
					}

					String[] errors = response.getContentAsString().split("\n");
					int errorsDisplayed = 0;
					for(String error : errors) {
						GNUErrorMessageFormat gnuError = new GNUErrorMessageFormat(error);
						String supressWarning = "info warning: Section lacks heading. Consider using “h2”-“h6”"
								+ " elements to add identifying headings to all sections.";
						if(supressWarning.equals(gnuError.message())) {
							continue;
						}
						// == on String, yes this is what I want
						if(error == errors[0]) {
							System.out.println(Static.MCToANSIColors("Response for "
									+ MCChatColor.AQUA + e.getKey() + MCChatColor.PLAIN_WHITE + ":"));
						}
						StringBuilder output = new StringBuilder();
						switch(gnuError.messageType()) {
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
						for(int i = gnuError.fromLine(); i < gnuError.toLine() + 1; i++) {
							output.append("\n").append(page[i - 1]);
						}
						output.append("\n");
						for(int i = 0; i < gnuError.fromColumn() - 1; i++) {
							output.append(" ");
						}
						output.append(MCChatColor.RED).append("^").append(MCChatColor.PLAIN_WHITE);
						System.out.println(Static.MCToANSIColors(output.toString()));
						specifiedErrors++;
						errorsDisplayed++;
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
		if(finalizerScript != null) {
			System.out.println("Running post-script");
			if(finalizerScript.getPath().endsWith(".ms")) {
				try {
					Interpreter.startWithTTY(finalizerScript, filesChanged, false);
				} catch (DataSourceException | URISyntaxException | Profiles.InvalidProfileException ex) {
					ex.printStackTrace(System.err);
				}
			} else {
				List<String> args = new ArrayList<>();
				args.add(finalizerScript.getCanonicalPath());
				args.addAll(filesChanged);
				CommandExecutor exec = new CommandExecutor(args.toArray(new String[args.size()]));
				exec.setSystemInputsAndOutputs();
				exec.start();
				exec.waitFor();
			}
		}
		System.out.println("Done!");
		System.out.println("Summary of changed files (" + filesChanged.size() + ")");
		System.out.println(StringUtils.Join(filesChanged, "\n"));
		if(masterMemories != null) {
			System.out.println(masterMemories.size() + " translation segments exist.");
		}
	}

	private final String siteBase;
	private final String docsBase;
	private final String resourceBase;
	private final String productionTranslations;
	private final jline.console.ConsoleReader reader;
	private final ThreadPoolExecutor generateQueue;
	private final ThreadPoolExecutor uploadQueue;
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
	private final File finalizerScript;
	private final boolean clearProgressBar;
	private final String overrideIdRsa;
	private final File translationMemoryDb;

	/**
	 * The master memories object exists purely to prevent duplicate translations being requested from the
	 * translation server for new translations. However, identical keys may be translated differently
	 * on different pages, and that's ok. The TM that gets set in here is non deterministic, but only the
	 * firstt time, because after that, each page should have it's own TM, and if the one that was picked
	 * was wrong, then once it's manually corrected, it will stay correct forever.
	 */
	private final TranslationMaster masterMemories;

	private static final String EDIT_THIS_PAGE_PREAMBLE
			= "Find a bug in this page? <a rel=\"noopener noreferrer\" target=\"_blank\" href=\"";
	private static final String EDIT_THIS_PAGE_POSTAMBLE
			= "\">Edit this page yourself, then submit a pull request.</a>";
	private static final String DEFAULT_GITHUB_BASE_URL
			= "https://github.com/EngineHub/CommandHelper/edit/master/src/main/%s";

	@SuppressWarnings({"unchecked", "checkstyle:regexpsingleline"})
	private SiteDeploy(String siteBase, String docsBase, boolean useLocalCache,
			DeploymentMethod deploymentMethod, boolean doValidation, boolean showTemplateCredit,
			String githubBaseUrl, String validatorUrl, File finalizerScript, boolean clearProgressBar,
			String overrideIdRsa, File translationMemoryDb, String productionTranslations)
			throws IOException {
		this.siteBase = siteBase;
		this.docsBase = docsBase;
		this.resourceBase = docsBase + "resources/";
		this.finalizerScript = finalizerScript;
		this.reader = new jline.console.ConsoleReader();
		this.generateQueue = new ThreadPoolExecutor(1, 1,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(),
				new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "generateQueue");
			}
		});
		this.uploadQueue = new ThreadPoolExecutor(1, 1,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(),
				new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "uploadQueue");
			}
		});
		this.useLocalCache = useLocalCache;
		this.deploymentMethod = deploymentMethod;
		this.doValidation = doValidation;
		this.showTemplateCredit = showTemplateCredit;
		this.validatorUrl = validatorUrl;
		if(githubBaseUrl.isEmpty()) {
			githubBaseUrl = DEFAULT_GITHUB_BASE_URL;
		}
		this.githubBaseUrl = githubBaseUrl;
		this.clearProgressBar = clearProgressBar;
		pn = getPersistenceNetwork();
		if(pn != null) {
			try {
				String localCache = pn.get(new String[]{"site_deploy", "local_cache"});
				if(localCache == null) {
					localCache = "{}";
				}
				lc = (Map<String, String>) JSONValue.parse(localCache);
			} catch (DataSourceException | IllegalArgumentException ex) {
				writeLog("Could not read in local cache", ex);
				notificationAboutLocalCache = false;
			}
		}
		this.overrideIdRsa = overrideIdRsa;
		this.translationMemoryDb = translationMemoryDb;
		this.productionTranslations = productionTranslations;
		if(translationMemoryDb != null) {
			writeStatus("Loading translation memories, this may take a while.");
			this.masterMemories = new TranslationMaster(translationMemoryDb, (current, total) -> {
				if(!clearProgressBar) {
					return;
				}
				writeStatus("Loading translation memories, this may take a while (" + ((int) current) + "/"
						+ ((int) total) + ")");
			});
			writeStatus("Done loading translation memories.");
		} else {
			this.masterMemories = null;
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
			p = new PersistenceNetworkImpl(MethodScriptFileLocations.getDefault().getPersistenceConfig(),
					new URI("sqlite://" + MethodScriptFileLocations.getDefault().getDefaultPersistenceDBFile()
							.getCanonicalFile().toURI().getRawSchemeSpecificPart().replace('\\', '/')),
					new ConnectionMixinFactory.ConnectionMixinOptions());
		} catch (DataSourceException | URISyntaxException | IOException ex) {
			p = null;
		}
		return p;
	}

	private void resetLine() throws IOException {
		if(clearProgressBar) {
			reader.getOutput().write("\u001b[1G\u001b[K");
			reader.flush();
		} else {
			reader.getOutput().write("\n");
			reader.flush();
		}
	}

	private synchronized void writeLog(String log, Throwable e) {
		try {
			reader.getOutput().write("\n" + log + "\n");
			e.printStackTrace(new PrintWriter(reader.getOutput()));
			reader.getOutput().write("\n");
			reader.flush();
		} catch (IOException ex) {
			System.err.println("Failure while logging exception!");
			System.err.println("Original exception:");
			e.printStackTrace(System.err);
			System.err.println("Logging exception:");
			ex.printStackTrace(System.err);
		}
	}

	private synchronized void writeStatus(String additionalInfo) {
		int generatePercent = 0;
		if(totalGenerateTasks.get() != 0) {
			generatePercent = (int) (currentGenerateTask.get() / ((double) totalGenerateTasks.get()) * 100.0);
		}
		int uploadPercent = 0;
		if(totalUploadTasks.get() != 0) {
			uploadPercent = (int) (currentUploadTask.get() / ((double) totalUploadTasks.get()) * 100.0);
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
		g.put("resourceBase", (Generator) (String... args) -> SiteDeploy.this.resourceBase);
		g.put("branding", (Generator) (String... args) -> Implementation.GetServerType().getBranding());
		g.put("siteRoot", (Generator) (String... args) -> SiteDeploy.this.siteBase);
		g.put("productionTranslations", (args) -> SiteDeploy.this.productionTranslations);
		g.put("docsBase", (Generator) (String... args) -> SiteDeploy.this.docsBase);
		g.put("apiJsonVersion", (Generator) (String... args) -> apiJsonVersion);
		/**
		 * The cacheBuster template is meant to make it easier to deal with caching of resources. The template allows
		 * you to specify the resource, and it creates a path to the resource using resourceBase, but it also appends a
		 * hash of the file, so that as the file changes, so does the hash (using a ?v=hash query string). Most
		 * resources live in /siteDeploy/resources/*, and so the shorthand is to use
		 * %%cacheBuster|path/to/resource.css%%. However, this isn't always correct, because resources can live all over
		 * the place. In that case, you should use the following format:
		 * %%cacheBuster|/absolute/path/to/resource.css|path/to/resource/in/html.css%%
		 */
		g.put("cacheBuster", (Generator) (String... args) -> {
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
					throw new RuntimeException("Could not find " + loc
							+ " in resources folder for cacheBuster template");
				}
				hash = getLocalMD5(in);
			} catch (IOException ex) {
				writeLog(null, ex);
			}
			return resourceLoc + "?v=" + hash;
		});
		final Generator learningTrailGen = (String... args) -> {
			String learningTrail =
					StreamUtils.GetString(SiteDeploy.class.getResourceAsStream("/siteDeploy/LearningTrail.json"));
			List<Map<String, List<Map<String, String>>>> ret = new ArrayList<>();
			@SuppressWarnings("unchecked")
			List<Map<String, List<Object>>> lt = (List<Map<String, List<Object>>>) JSONValue.parse(learningTrail);
			for(Map<String, List<Object>> l : lt) {
				for(Map.Entry<String, List<Object>> e : l.entrySet()) {
					String category = e.getKey();
					List<Map<String, String>> catInfo = new ArrayList<>();
					for(Object ll : e.getValue()) {
						Map<String, String> pageInfo = new LinkedHashMap<>();
						String page = null;
						String name = null;
						if(ll instanceof String) {
							name = page = (String) ll;
						} else if(ll instanceof Map) {
							@SuppressWarnings("unchecked")
									Map<String, String> p = (Map<String, String>) ll;
							if(p.entrySet().size() != 1) {
								throw new RuntimeException("Invalid JSON for learning trail");
							}
							for(Map.Entry<String, String> ee : p.entrySet()) {
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
		};
		g.put("js_string_learning_trail", (Generator) (String... args) -> {
			String g1 = learningTrailGen.generate(args);
			g1 = g1.replaceAll("\\\\", "\\\\");
			g1 = g1.replaceAll("\"", "\\\\\"");
			return g1;
		});
		g.put("learning_trail", learningTrailGen);
		/**
		 * If showTemplateCredit is false, then this will return "display: none;" otherwise, it will return an empty
		 * string.
		 */
		g.put("showTemplateCredit", (Generator) (String... args) -> showTemplateCredit ? "" : "display: none;");
		return g;
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
		uploadQueue.submit(() -> {
			try {
				writeStatus("Currently uploading " + toLocation);
				// Read the contents only once
				byte[] c = StreamUtils.GetBytes(contents);
				contents.close();
				boolean skipUpload = false;
				String hash = null;
				if(pn != null) {
					if(notificationAboutLocalCache) {
						hash = getLocalMD5(new ByteArrayInputStream(c));
						try {
							if(lc.containsKey(deploymentMethod.getID() + toLocation)) {
								if(useLocalCache) {
									String cacheHash = lc.get(deploymentMethod.getID() + toLocation);
									if(cacheHash.equals(hash)) {
										skipUpload = true;
									}
								}
							}
						} catch (IllegalArgumentException ex) {
							writeLog("Could not use local cache", ex);
							notificationAboutLocalCache = false;
						}
					}
				}
				if(!skipUpload && deploymentMethod.deploy(new ByteArrayInputStream(c), toLocation, overrideIdRsa)) {
					filesChanged.add(toLocation);
				}
				if(pn != null && notificationAboutLocalCache && hash != null) {
					try {
						lc.put(deploymentMethod.getID() + toLocation, hash);
						pn.set(dm, new String[]{"site_deploy", "local_cache"}, JSONValue.toJSONString(lc));
					} catch (DataSourceException | ReadOnlyException | IllegalArgumentException ex) {
						writeLog(null, ex);
						notificationAboutLocalCache = false;
					}
				}
				currentUploadTask.addAndGet(1);
				writeStatus("");
			} catch (Throwable ex) {
				writeLog("Failed while uploading " + toLocation, ex);
				generateQueue.shutdownNow();
				uploadQueue.shutdownNow();
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
		String s = StreamUtils.GetString(SiteDeploy.class.getResourceAsStream(resource.replace('\\', '/')));
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
		if(keywords == null) {
			keywords = new ArrayList<>();
		}
		final List<String> kw = keywords;
		generateQueue.submit(() -> {
			try {
				String bW = body;
				if(!bW.contains(EDIT_THIS_PAGE_PREAMBLE)) {
					bW += "<p id=\"edit_this_page\">"
							+ EDIT_THIS_PAGE_PREAMBLE
							+ String.format(githubBaseUrl, "java/" + SiteDeploy.class.getName().replace(".", "/"))
							+ ".java"
							+ EDIT_THIS_PAGE_POSTAMBLE
							+ "</p>";
				}
				try {
					writeStatus("Currently generating " + toLocation);
					if(translationMemoryDb != null) {
						generateQueue.submit(() -> {
							try {
								createTranslationMemory(toLocation, body);
							} catch (Throwable t) {
								writeLog("While generating translation memory for " + toLocation + "an error occured: ",
										t);
							}
							currentGenerateTask.addAndGet(1);
						});
						totalGenerateTasks.addAndGet(1);
					}
					// First, substitute the templates in the body
					final String b;
					try {
						Map<String, Generator> standard = getStandardGenerators();
						standard.putAll(DocGenTemplates.GetGenerators());
						b = DocGenTemplates.DoTemplateReplacement(bW, standard);
					} catch (Exception ex) {
						if(ex instanceof GenerateException) {
							writeLog("Failed to substitute template"
									+ " while trying to upload resource to " + toLocation, ex);
						} else {
							writeLog(null, ex);
						}
						reader.flush();
						generateQueue.shutdownNow();
						uploadQueue.shutdownNow();
						return;
					}
					// Second, add the template %%body%% and replace that in the frame
					final Map<String, Generator> g = new HashMap<>();
					g.put("body", (Generator) (String... args) -> b);
					g.put("bodyEscaped", (Generator) (String... args) -> {
						String s = b.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'")
								.replaceAll("\r", "").replaceAll("\n", "\\\\n");
						s = s.replaceAll("<script.*?</script>", "");
						return s;
					});
					g.put("title", (Generator) (String... args) -> title);
					g.put("useHttps", (Generator) (String... args)
							-> SiteDeploy.this.siteBase.startsWith("https") ? "true" : "false");
					g.put("keywords", (Generator) (String... args) -> {
						List<String> k = new ArrayList<>(kw);
						k.add(Implementation.GetServerType().getBranding());
						return StringUtils.Join(k, ", ");
					});
					g.put("description", (Generator) (String... args) -> description);
					g.putAll(getStandardGenerators());
					g.putAll(DocGenTemplates.GetGenerators());
					String frame = StreamUtils.GetString(SiteDeploy.class
							.getResourceAsStream("/siteDeploy/frame.html"));
					final String bb = DocGenTemplates.DoTemplateReplacement(frame, g);
					// Write out using writeFromString
					uploadedPages.put(toLocation, bb);
					writeFromString(bb, toLocation);
					currentGenerateTask.addAndGet(1);
					writeStatus("");
				} catch (Exception ex) {
					writeLog("While writing " + toLocation + " the following error occured:", ex);
				}
			} catch (Throwable t) {
				writeLog("Failure!", t);
			}
		});
		totalGenerateTasks.addAndGet(1);
	}

	/**
	 * Translation memories allow strings within the page to be localized in to different languages. The translations
	 * themselves are by default created by machine translation, but it's important to be able to override these when
	 * necessary, so the translation memories (tmem) files are created and committed to a repository, so PRs can
	 * be created. This function is charged with orchestrating the process, which is comprised of several smaller tasks.
	 * @param toLocation
	 * @param inputString
	 */
	private void createTranslationMemory(String toLocation, String inputString) throws IOException {
		toLocation = StringUtils.replaceLast(toLocation, "\\.html", ".tmem.xml");
		String location = "%s/docs/" + MSVersion.V3_3_4 + "/" + toLocation;
		writeStatus("Creating memory file for " + location);
		masterMemories.createTranslationMemory(location, inputString);
	}

	private void writeMasterTranslations() throws IOException {
		if(masterMemories != null) {
			writeStatus("Writing out translation database");
			masterMemories.save((current, total) -> {
				if(!clearProgressBar) {
					return;
				}
				writeStatus("Writing out translation database (" + ((int) current) + "/"
						+ ((int) total) + ")");
			});
		}
	}

	/**
	 * Pages deployed: All pages under the siteDeploy/resources folder. They are transferred as is, including
	 * recursively looking through the folder structure. Binary files are also supported. index.js (after template
	 * replacement)
	 */
	private void deployResources() {
		generateQueue.submit(() -> {
			try {
				writeStatus("Generating resources");
				File root = new File(SiteDeploy.class.getResource("/siteDeploy/resources").toExternalForm());
				ZipReader reader1 = new ZipReader(root);
				Queue<File> q = new LinkedList<>();
				q.addAll(Arrays.asList(reader1.listFiles()));
				while(q.peek() != null) {
					ZipReader r = new ZipReader(q.poll());
					if(r.isDirectory()) {
						q.addAll(Arrays.asList(r.listFiles()));
					} else {
						String fileName = r.getFile().getAbsolutePath().replaceFirst(Pattern.quote(reader1.getFile()
								.getAbsolutePath()), "");
						writeStatus("Generating " + fileName);
						writeFromStream(r.getInputStream(), "resources" + fileName);
					}
				}
			} catch (IOException ex) {
				writeLog(null, ex);
			}
			String indexJs = StreamUtils.GetString(SiteDeploy.class.getResourceAsStream("/siteDeploy/index.js"));
			try {
				writeFromString(DocGenTemplates.DoTemplateReplacement(indexJs, getStandardGenerators()),
						"resources/js/index.js");
			} catch (Generator.GenerateException ex) {
				writeLog("GenerateException in /siteDeploy/index.js", ex);
			}
			currentGenerateTask.addAndGet(1);
		});
		totalGenerateTasks.addAndGet(1);
	}

	/**
	 * Pages deployed: index.html privacy_policy.html sponsors.html
	 */
	private void deployFrontPages() {
		generateQueue.submit(() -> {
			try {
				writePageFromResource(MSVersion.LATEST.toString() + " - Docs", "/siteDeploy/VersionFrontPage",
						"index.html",
						Arrays.asList(new String[]{MSVersion.LATEST.toString(), "better than skript"}),
						"Front page for "
								+ MSVersion.LATEST.toString());
				currentGenerateTask.addAndGet(1);
			} catch (Throwable t) {
				writeLog("Failure!", t);
			}
		});
		totalGenerateTasks.addAndGet(1);
		generateQueue.submit(() -> {
			try {
				writePageFromResource("Privacy Policy", "/siteDeploy/privacy_policy.html", "privacy_policy.html",
						Arrays.asList(new String[]{"privacy policy"}), "Privacy policy for the site");
				currentGenerateTask.addAndGet(1);
			} catch (Throwable t) {
				writeLog("Failure!", t);
			}
		});
		totalGenerateTasks.addAndGet(1);
		generateQueue.submit(() -> {
			try {
				writePageFromResource(Implementation.GetServerType().getBranding(), "/siteDeploy/FrontPage",
						"../../index.html",
						Arrays.asList(new String[]{"index", "front page"}), "The front page for "
								+ Implementation.GetServerType().getBranding());
				currentGenerateTask.addAndGet(1);
			} catch (Throwable t) {
				writeLog("Failure!", t);
			}
		});
		totalGenerateTasks.addAndGet(1);
		generateQueue.submit(() -> {
			try {
				writePageFromResource(Implementation.GetServerType().getBranding(), "/siteDeploy/Sponsors",
						"../../sponsors.html",
						Arrays.asList(new String[]{"index", "front page"}), "Sponsors of MethodScript");
				currentGenerateTask.addAndGet(1);
			} catch (Throwable t) {
				writeLog("Failure!", t);
			}
		});
		totalGenerateTasks.addAndGet(1);
		generateQueue.submit(() -> {
			try {
				writePageFromResource("Doc Directory", "/siteDeploy/DocDirectory", "../index.html",
						Arrays.asList(new String[]{"directory"}), "The directory for all documented versions");
				currentGenerateTask.addAndGet(1);
			} catch (Throwable t) {
				writeLog("Failure!", t);
			}
		});
		totalGenerateTasks.addAndGet(1);
		generateQueue.submit(() -> {
			try {
				writePageFromResource("404 Not Found", "/siteDeploy/404", "../../404.html",
						Arrays.asList(new String[]{"404"}), "Page not found");
				currentGenerateTask.addAndGet(1);
			} catch (Throwable t) {
				writeLog("Failure!", t);
			}
		});
		totalGenerateTasks.addAndGet(1);
	}

	/**
	 * Pages deployed: All files from /docs/*
	 */
	private void deployLearningTrail() {
		generateQueue.submit(() -> {
			try {
				File root = new File(SiteDeploy.class.getResource("/docs").toExternalForm());
				ZipReader zReader = new ZipReader(root);
				String path = Pattern.quote(zReader.getFile().getAbsolutePath());
				for(File r : zReader.listFiles()) {
					String filename = r.getAbsolutePath().replaceFirst(path, "");
					writePageFromResource(r.getName(), "/docs" + filename, r.getName() + ".html",
							Arrays.asList(new String[]{r.getName().replace("_", " ")}), "Learning trail page for "
									+ r.getName().replace("_", " "));
				}
			} catch (IOException ex) {
				writeLog(null, ex);
			}
			currentGenerateTask.addAndGet(1);
		});
		totalGenerateTasks.addAndGet(1);
	}

	private void deployAPI() {
		generateQueue.submit(() -> {
			try {
				writeStatus("Generating API");
				Set<Class<? extends Function>> functionClasses = new TreeSet<>(
						(Class<? extends Function> o1, Class<? extends Function> o2) -> {
					Function f1 = ReflectionUtils.instantiateUnsafe(o1);
					Function f2 = ReflectionUtils.instantiateUnsafe(o2);
					return f1.getName().compareTo(f2.getName());
				});
				functionClasses.addAll(ClassDiscovery.getDefaultInstance()
						.loadClassesWithAnnotationThatExtend(api.class, Function.class));
				// A map of where it maps the enclosing class to the list of function rows, which contains a list of
				// table cells.
				Map<Class<?>, List<List<String>>> data = new TreeMap<>((Class<?> o1, Class<?> o2)
						-> o1.getCanonicalName().compareTo(o2.getCanonicalName()));
				List<String> hiddenFunctions = new ArrayList<>();
				for(Class<? extends Function> functionClass : functionClasses) {
					try {
						if(!data.containsKey(functionClass.getEnclosingClass())) {
							data.put(functionClass.getEnclosingClass(), new ArrayList<>());
						}
						List<List<String>> d = data.get(functionClass.getEnclosingClass());
						List<String> c = new ArrayList<>();
						// function name, returns, arguments, throws, description, restricted
						final Function f;
						try {
							f = ReflectionUtils.instantiateUnsafe(functionClass);
						} catch (ReflectionUtils.ReflectionException ex) {
							throw new RuntimeException("While trying to construct " + functionClass
									+ ", got the following", ex);
						}
						final DocGen.DocInfo di = new DocGen.DocInfo(f.docs());
						// If the function is hidden, we don't want to put it on the main page by default. Regardless,
						// we do want to generate the function page, it will just remain unlinked.
						generateQueue.submit(() -> {
							try {
								generateFunctionDocs(f, di);
							} catch (Throwable ex) {
								ex.printStackTrace(System.err);
							}
						});
						if(f.since().equals(MSVersion.V0_0_0)) {
							// Don't add these
							continue;
						}
						if(f.getClass().getAnnotation(hide.class) != null) {
							hiddenFunctions.add(f.getName());
						}
						c.add("[[API/functions/" + f.getName() + "|" + f.getName() + "]]()");
						c.add(di.ret);
						c.add(di.args);
						List<String> exc = new ArrayList<>();
						if(f.thrown() != null) {
							for(Class<? extends CREThrowable> e : f.thrown()) {
								// for the API reference page we want the simple name
								String[] splitType = ReflectionUtils.instantiateUnsafe(e).getName().split("\\.");
								exc.add("{{object|" + (splitType[splitType.length - 1]) + "}}");
							}
						}
						c.add(StringUtils.Join(exc, "<br>"));
						StringBuilder desc = new StringBuilder();
						desc.append(HTMLUtils.escapeHTML(di.desc));
						if(di.extendedDesc != null) {
							desc.append(" [[API/functions/").append(f.getName()).append("|See more...]]<br>\n");
						}
						try {
							if(f.examples() != null && f.examples().length > 0) {
								desc.append("<br>([[API/functions/").append(f.getName())
										.append("#Examples|Examples...]])\n");
							}
						} catch (ConfigCompileException | NoClassDefFoundError ex) {
							writeLog(null, ex);
						}
						c.add(desc.toString());
						c.add("<span class=\"api_" + (f.isRestricted() ? "yes" : "no") + "\">"
								+ (f.isRestricted() ? "Yes" : "No")
								+ "</span>");
						d.add(c);
					} catch (Throwable t) {
						writeLog("Failure while generating " + functionClass, t);
					}
				}
				//					System.out.println("Functions to be deployed: " + data + "\n\n");
				// data is now constructed.
				StringBuilder b = new StringBuilder();
				b.append("<ul id=\"TOC\">");
				for(Class<?> clazz : data.keySet()) {
					b.append("<li><a href=\"#").append(clazz.getSimpleName())
							.append("\">").append(clazz.getSimpleName()).append("</a></li>");
				}
				b.append("</ul>\n");
				for(Map.Entry<Class<?>, List<List<String>>> e : data.entrySet()) {
					Class<?> clazz = e.getKey();
					List<List<String>> clazzData = e.getValue();
					if(clazzData.isEmpty()) {
						// If there are no functions in the class, don't display it. This is most likely to happen
						// if all the class's functions are hidden with @hide.
						continue;
					}
					try {
						b.append("== ").append(clazz.getSimpleName()).append(" ==\n");
						String docs = (String) ReflectionUtils.invokeMethod(clazz, null, "docs");
						b.append("<div>").append(docs).append("</div>\n\n");
						b.append("{|\n|-\n");
						b.append("! scope=\"col\" width=\"8%\" | Function Name\n"
								+ "! scope=\"col\" width=\"4%\" | Returns\n"
								+ "! scope=\"col\" width=\"16%\" | Arguments\n"
								+ "! scope=\"col\" width=\"8%\" | Throws\n"
								+ "! scope=\"col\" width=\"62%\" | Description\n"
								+ "! scope=\"col\" width=\"2%\" |"
								+ " <span class=\"abbr\" title=\"Restricted\">Res</span>\n");
						for(List<String> row : clazzData) {
							b.append("|-");
							if(hiddenFunctions.contains(row.get(0))) {
								b.append(" class=\"hiddenFunction\"");
							}
							b.append("\n");
							for(String cell : row) {
								b.append("| ").append(cell).append("\n");
							}

						}
						b.append("|}\n");
						b.append("<p><a href=\"#TOC\">Back to top</a></p>\n");
					} catch (Error ex) {
						writeLog("While processing " + clazz + " got:", ex);
					}
				}
				b.append("<div><a href=\"#\" id=\"showHidden\">Show hidden</a></div>");
				b.append("<script type=\"text/javascript\">\n"
						+ "pageRender.then(function() {\n"
						+ "$('#showHidden').click(function(event){\n"
						+ "$('.hiddenFunction').removeClass('hiddenFunction');\n"
						+ "$('#showHidden').remove();\n"
						+ "event.preventDefault();\nreturn false;\n"
						+ "});\n"
						+ "});\n"
						+ "</script>");
				writePage("API", b.toString(), "API.html",
						Arrays.asList(new String[]{"API", "functions"}),
						"A list of all " + Implementation.GetServerType().getBranding() + " functions");
				currentGenerateTask.addAndGet(1);
			} catch (Throwable ex) {
				ex.printStackTrace(System.err);
			}
		});
		totalGenerateTasks.addAndGet(1);
	}

	private void generateFunctionDocs(Function f, DocGen.DocInfo docs) {
		writeStatus("Generating function docs for " + f.getName());
		StringBuilder page = new StringBuilder();
		page.append("== ").append(f.getName()).append(" ==\n");
		page.append("<div>").append(docs.desc).append("</div>\n");

		page.append("=== Vital Info ===\n");
		page.append("{| style=\"width: 40%;\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\" class=\"wikitable\"\n");
		page.append("|-\n"
				+ "! scope=\"col\" width=\"20%\" | \n"
				+ "! scope=\"col\" width=\"80%\" | \n"
				+ "|-\n"
				+ "! scope=\"row\" | Name\n"
				+ "| ").append(f.getName()).append("\n"
				+ "|-\n"
				+ "! scope=\"row\" | Returns\n"
				+ "| ").append(docs.ret).append("\n"
				+ "|-\n"
				+ "! scope=\"row\" | Usages\n"
				+ "| ").append(docs.args).append("\n"
				+ "|-\n"
				+ "! scope=\"row\" | Throws\n"
				+ "| ");
		List<String> exceptions = new ArrayList<>();
		if(f.thrown() != null) {
			for(Class<? extends CREThrowable> c : f.thrown()) {
				String t = ClassDiscovery.GetClassAnnotation(c, typeof.class).value();
				exceptions.add("[[../objects/" + t + "|" + t + "]]");
			}
		}
		page.append(StringUtils.Join(exceptions, "<br>"));
		page.append("\n"
				+ "|-\n"
				+ "! scope=\"row\" | Since\n"
				+ "| ").append(f.since()).append("\n"
				+ "|-\n"
				+ "! scope=\"row\" | Restricted\n");
		page.append("| <div style=\"background-color: ");
		page.append(f.isRestricted() ? "red" : "green");
		page.append("; font-weight: bold; text-align: center;\">"
		).append(f.isRestricted() ? "Yes" : "No").append("</div>\n"
				+ "|-\n"
				+ "! scope=\"row\" | Optimizations\n"
				+ "| ");
		String optimizationMessage = "None";
		if(f instanceof Optimizable) {
			Set<Optimizable.OptimizationOption> options = ((Optimizable) f).optimizationOptions();
			List<String> list = new ArrayList<>();
			for(Optimizable.OptimizationOption option : options) {
				list.add("[[../../Optimizer#" + option.name() + "|" + option.name() + "]]");
			}
			optimizationMessage = StringUtils.Join(list, " <br /> ");
		}
		page.append(optimizationMessage);
		page.append("\n|}");
		if(docs.extendedDesc != null) {
			page.append("<div>").append(docs.extendedDesc).append("</div>");
		}

		String[] usages = docs.originalArgs.split("\\|");
		StringBuilder usageBuilder = new StringBuilder();
		for(String usage : usages) {
			usageBuilder.append("<pre>\n").append(f.getName()).append("(").append(usage.trim()).append(")\n</pre>");
		}
		page.append("\n=== Usages ===\n");
		page.append(usageBuilder.toString());

		StringBuilder exampleBuilder = new StringBuilder();
		try {
			if(f.examples() != null && f.examples().length > 0) {
				int count = 1;
				//If the output was automatically generated, change the color of the pre
				for(ExampleScript es : f.examples()) {
					exampleBuilder.append("====Example ").append(count).append("====\n")
							.append(HTMLUtils.escapeHTML(es.getDescription())).append("\n\n"
							+ "Given the following code:\n");
//					exampleBuilder.append(SimpleSyntaxHighlighter.Highlight(es.getScript(), true)).append("\n");
					exampleBuilder.append("<%CODE|").append(es.getScript()).append("%>\n");
					String style = "";
					exampleBuilder.append("\n\nThe output ");
					if(es.isAutomatic()) {
						style = " background-color: #BDC7E9;";
						exampleBuilder.append("would");
					} else {
						exampleBuilder.append("might");
					}
					exampleBuilder.append(" be:\n<pre class=\"pre\" style=\"border-top: 1px solid blue;"
							+ " border-bottom: 1px solid blue;")
							.append(style).append("\"");
					exampleBuilder.append("><%NOWIKI|").append(es.getOutput())
							.append("%>").append("</pre>\n");
					count++;
				}
			} else {
				exampleBuilder.append("Sorry, there are no examples for this function! :(\n");
			}
		} catch (Exception ex) {
			exampleBuilder.append("Error while compiling the examples for ").append(f.getName());
		}

		page.append("\n=== Examples ===\n");
		page.append(exampleBuilder.toString());

		Class<?>[] seeAlso = f.seeAlso();
		String seeAlsoText = "";
		if(seeAlso != null && seeAlso.length > 0) {
			seeAlsoText += "===See Also===\n";
			boolean first = true;
			for(Class<?> c : seeAlso) {
				if(!first) {
					seeAlsoText += ", ";
				}
				first = false;
				if(Function.class.isAssignableFrom(c)) {
					Function f2 = (Function) ReflectionUtils.newInstance(c);
					seeAlsoText += "<code>[[API/functions/" + f2.getName() + ".html|" + f2.getName() + "]]</code>";
				} else if(Template.class.isAssignableFrom(c)) {
					Template t = (Template) ReflectionUtils.newInstance(c);
					seeAlsoText += "[[" + t.getPath() + t.getName() + "|Learning Trail: " + t.getDisplayName() + "]]";
				} else {
					throw new Error("Unsupported class found in @seealso annotation: " + c.getName());
				}
			}
		}
		page.append(seeAlsoText);

		Class<?> container = f.getClass();
		while(container.getEnclosingClass() != null) {
			container = container.getEnclosingClass();
		}
		int lineNum = 0;
		try {
			MethodMirror m = ClassDiscovery.getDefaultInstance().forName(f.getClass().getName())
					.getMethod("docs", new Class[0]);
			lineNum = m.getLineNumber();
		} catch (NoSuchMethodException | ClassNotFoundException ex) {
			// Oh well.
		}
		String bW = "<p id=\"edit_this_page\">"
				+ EDIT_THIS_PAGE_PREAMBLE
				+ String.format(githubBaseUrl, "java/" + container.getName().replace(".", "/")) + ".java#L"
				+ (lineNum < 10 ? lineNum : lineNum + 10) // Add 10, so we scroll a bit more in view
				+ EDIT_THIS_PAGE_POSTAMBLE
				+ " (Note this page is automatically generated from the documentation in the source code.)</p>";
		page.append(bW);
		String description = f.getName() + "() api page";
		writePage(f.getName(), page.toString(), "API/functions/" + f.getName() + ".html", Arrays.asList(
				new String[]{f.getName(), f.getName() + " api", f.getName() + " example", f.getName()
						+ " description"}), description);
	}

	private void deployEventAPI() {
		generateQueue.submit(() -> {
			try {
				Set<Class<? extends Event>> eventClasses = new TreeSet<>(
						(Class<? extends Event> o1, Class<? extends Event> o2) -> {
					Event f1 = ReflectionUtils.instantiateUnsafe(o1);
					Event f2 = ReflectionUtils.instantiateUnsafe(o2);
					return f1.getName().compareTo(f2.getName());
				});
				eventClasses.addAll(ClassDiscovery.getDefaultInstance()
						.loadClassesWithAnnotationThatExtend(api.class, Event.class));
				// A map of where it maps the enclosing class to the list of event rows,
				// which contains a list of table cells.
				Map<Class<?>, List<List<String>>> data = new TreeMap<>((Class<?> o1, Class<?> o2)
						-> o1.getCanonicalName().compareTo(o2.getCanonicalName()));
				for(Class<? extends Event> eventClass : eventClasses) {
					if(!data.containsKey(eventClass.getEnclosingClass())) {
						data.put(eventClass.getEnclosingClass(), new ArrayList<>());
					}
					List<List<String>> d = data.get(eventClass.getEnclosingClass());
					List<String> c = new ArrayList<>();
					// event name, description, prefilters, data, mutable
					final Event e;
					try {
						e = ReflectionUtils.instantiateUnsafe(eventClass);
					} catch (ReflectionUtils.ReflectionException ex) {
						throw new RuntimeException("While trying to construct " + eventClass
								+ ", got the following", ex);
					}
					final DocGen.EventDocInfo edi = new DocGen.EventDocInfo(e.docs(), e.getName());
					if(e.since().equals(MSVersion.V0_0_0)) {
						// Don't add these
						continue;
					}
					c.add(e.getName());
					c.add(edi.description);
					List<String> pre = new ArrayList<>();
					if(!edi.prefilter.isEmpty()) {
						for(DocGen.EventDocInfo.PrefilterData pdata : edi.prefilter) {
							pre.add("<p><strong>" + pdata.name + "</strong>: "
									+ pdata.formatDescription(DocGen.MarkupType.HTML) + "</p>");
						}
					}
					c.add(StringUtils.Join(pre, ""));
					List<String> ed = new ArrayList<>();
					if(!edi.eventData.isEmpty()) {
						for(DocGen.EventDocInfo.EventData edata : edi.eventData) {
							ed.add("<p><strong>" + edata.name + "</strong>"
									+ (!edata.description.isEmpty() ? ": " + edata.description : "") + "</p>");
						}
					}
					c.add(StringUtils.Join(ed, ""));
					List<String> mut = new ArrayList<>();
					if(!edi.mutability.isEmpty()) {
						for(DocGen.EventDocInfo.MutabilityData mdata : edi.mutability) {
							mut.add("<p><strong>" + mdata.name + "</strong>"
									+ (!mdata.description.isEmpty() ? ": " + mdata.description : "") + "</p>");
						}
					}
					c.add(StringUtils.Join(mut, ""));
					d.add(c);
				}
				// data is now constructed.
				StringBuilder b = new StringBuilder();
				b.append("<ul id=\"TOC\">");
				for(Class<?> clazz : data.keySet()) {
					b.append("<li><a href=\"#").append(clazz.getSimpleName())
							.append("\">").append(clazz.getSimpleName()).append("</a></li>");
				}
				b.append("</ul>\n");
				for(Map.Entry<Class<?>, List<List<String>>> e : data.entrySet()) {
					Class<?> clazz = e.getKey();
					List<List<String>> clazzData = e.getValue();
					if(clazzData.isEmpty()) {
						// If there are no events in the class, don't display it.
						continue;
					}
					try {
						b.append("== ").append(clazz.getSimpleName()).append(" ==\n");
						String docs = (String) ReflectionUtils.invokeMethod(clazz, null, "docs");
						b.append("<div>").append(docs).append("</div>\n\n");
						b.append("{|\n|-\n");
						b.append("! scope=\"col\" width=\"7%\" | Event Name\n"
								+ "! scope=\"col\" width=\"30%\" | Description\n"
								+ "! scope=\"col\" width=\"20%\" | Prefilters\n"
								+ "! scope=\"col\" width=\"25%\" | Event Data\n"
								+ "! scope=\"col\" width=\"18%\" | Mutable Fields\n");
						for(List<String> row : clazzData) {
							b.append("|-");
							b.append("\n");
							for(String cell : row) {
								b.append("| ").append(cell).append("\n");
							}
						}
						b.append("|}\n");
						b.append("<p><a href=\"#TOC\">Back to top</a></p>\n");
					} catch (Error ex) {
						writeLog("While processing " + clazz + " got:", ex);
					}
				}
				writePage("Event API", b.toString(), "Event_API.html",
						Arrays.asList(new String[]{"API", "events"}),
						"A list of all " + Implementation.GetServerType().getBranding() + " events");
				currentGenerateTask.addAndGet(1);
			} catch (Error ex) {
				ex.printStackTrace(System.err);
			}
		});
		totalGenerateTasks.addAndGet(1);
	}

	private void deployEvents() {
//	generateQueue.submit(new Runnable() {
//		@Override
//		public void run() {
//		currentGenerateTask.addAndGet(1);
//		}
//	});
//	totalGenerateTasks.addAndGet(1);
	}

	private void deployObjects() {
//	generateQueue.submit(new Runnable() {
//		@Override
//		public void run() {
//		currentGenerateTask.addAndGet(1);
//		}
//	});
//	totalGenerateTasks.addAndGet(1);
	}

	/**
	 * Pages deployed: api.json - This page is the json version of the api
	 */
	private void deployAPIJSON() {
		generateQueue.submit(() -> {
			try {
				writeStatus("Generating api.json");
				writeFromString(apiJson, "api.json");
				currentGenerateTask.addAndGet(1);
			} catch (Throwable t) {
				writeLog("Failure!", t);
			}
		});
		totalGenerateTasks.addAndGet(1);
	}

	private void deployJar() {
		uploadQueue.submit(() -> {
			try {
				writeFromStream(ClassDiscovery.GetClassContainer(SiteDeploy.class).openStream(),
						"MethodScript.jar");
				// It goes in two places, so the latest release is always available no matter the last
				// build this version was built with.
				writeFromStream(ClassDiscovery.GetClassContainer(SiteDeploy.class).openStream(),
						"../../MethodScript.jar");

			} catch (Throwable e) {
				e.printStackTrace(System.err);
			}
		});
	}
}
