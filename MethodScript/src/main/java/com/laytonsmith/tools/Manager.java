package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.commandhelper.CommandHelperFileLocations;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.Installer;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.MethodScriptExecutionQueue;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.core.taskmanager.TaskManager;
import com.laytonsmith.persistence.DataSource;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.DataSourceFactory;
import com.laytonsmith.persistence.DataSourceFilter;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.ReadOnlyException;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 */
public class Manager {

	private static Profiler profiler;
	private static GlobalEnv gEnv;
	private static final File jarLocation = new File(Interpreter.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();
	private static final File chDirectory = new File(jarLocation, "CommandHelper");
	private static PersistenceNetwork persistenceNetwork;
	public static PrintStream out = StreamUtils.GetSystemOut();
	public static final String[] options = new String[]{
		"refactor", "print", "cleardb", "edit", "interpreter", "merge", "hidden-keys"
	};

	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public static void start() throws IOException, DataSourceException, URISyntaxException, Profiles.InvalidProfileException {
		Implementation.useAbstractEnumThread(false);
		Implementation.forceServerType(Implementation.Type.BUKKIT);
		ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
		options.setWorkingDirectory(chDirectory);
		persistenceNetwork = new PersistenceNetwork(CommandHelperFileLocations.getDefault().getPersistenceConfig(),
				CommandHelperFileLocations.getDefault().getDefaultPersistenceDBFile().toURI(), options);
		Installer.Install(chDirectory);
		CHLog.initialize(chDirectory);
		profiler = new Profiler(CommandHelperFileLocations.getDefault().getProfilerConfigFile());
		gEnv = new GlobalEnv(new MethodScriptExecutionQueue("Manager", "default"), profiler, persistenceNetwork,
				chDirectory, new Profiles(MethodScriptFileLocations.getDefault().getProfilesFile()),
				new TaskManager());
		TermColors.cls();
		pl("\n" + Static.Logo() + "\n\n" + Static.DataManagerLogo());


		pl("Starting the Data Manager...");
		try {
			Environment env = Environment.createEnvironment(gEnv, new CommandHelperEnvironment());
			MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex("player()", null, true)), env, null, null);
		} catch (ConfigCompileException | ConfigCompileGroupException ex) {
		}
		pl(TermColors.GREEN + "Welcome to the CommandHelper " + TermColors.CYAN + "Data Manager!");
		pl(TermColors.BLINKON + TermColors.RED + "Warning!" + TermColors.BLINKOFF + TermColors.YELLOW + " Be sure your server is not running before using this tool to make changes to your database!");
		pl("------------------------");
		boolean finished = false;
		do {
			pl(TermColors.YELLOW + "What function would you like to run? Type \"help\" for a full list of options.");
			String input = TermColors.prompt();
			pl();
			if (input.toLowerCase().startsWith("help")) {
				help(input.replaceFirst("help ?", "").toLowerCase().split(" "));
			} else if (input.equalsIgnoreCase("refactor")) {
				refactor();
			} else if (input.toLowerCase().startsWith("print")) {
				print(input.replaceFirst("print ?", "").toLowerCase().split(" "));
			} else if (input.equalsIgnoreCase("cleardb")) {
				cleardb();
			} else if (input.equalsIgnoreCase("edit")) {
				edit();
			} else if (input.equalsIgnoreCase("merge")) {
				merge();
			} else if (input.equalsIgnoreCase("interpreter")) {
				new Interpreter(null, System.getProperty("user.dir"));
			} else if (input.equalsIgnoreCase("hidden-keys")){
				hiddenKeys();
			} else if (input.equalsIgnoreCase("exit")) {
				pl("Thanks for using the " + TermColors.CYAN + TermColors.BOLD + "Data Manager!" + TermColors.reset());
				finished = true;
			} else {
				pl("I'm sorry, that's not a valid command. Here's the help:");
				help(new String[]{});
			}
		} while (finished == false);
		StreamUtils.GetSystemOut().println(TermColors.reset());
	}

	public static void merge() {
		TermColors.cls();
		pl(TermColors.GREEN + "Transferring a database takes all the keys from a database connection, and puts\n"
				+ "them straight into another database. If there are key conflicts, this tool will prompt\n"
				+ "you for an action.");
		ConnectionMixinFactory.ConnectionMixinOptions mixinOptions = new ConnectionMixinFactory.ConnectionMixinOptions();
		mixinOptions.setWorkingDirectory(chDirectory);
		DataSource source;
		DataSource destination;

		do {
			//Verify that the destination doesn't have any of the keys in the source, barring keys that have
			//the exact same value.
			do {
				//Get the source connection set up
				pl(TermColors.YELLOW + "What is the source connection you would like to read the keys from?\n"
						+ "(Type the connection exactly as you would in the persistence configuration,\n"
						+ "aliases are not supported)");
				String ssource = TermColors.prompt();
				try {
					source = DataSourceFactory.GetDataSource(ssource, mixinOptions);
					break;
				} catch (DataSourceException ex) {
					pl(TermColors.RED + ex.getMessage());
				} catch (URISyntaxException ex) {
					pl(TermColors.RED + ex.getMessage());
				}
			} while (true);

			do {
				//Get the destination connection set up
				pl(TermColors.YELLOW + "What is the destination connection?");
				String sdestination = TermColors.prompt();
				try {
					destination = DataSourceFactory.GetDataSource(sdestination, mixinOptions);
					break;
				} catch (DataSourceException ex) {
					pl(TermColors.RED + ex.getMessage());
				} catch (URISyntaxException ex) {
					pl(TermColors.RED + ex.getMessage());
				}
			} while (true);
			try {
				//Run through all the source's keys, and check to see that either the
				//destination's key doesn't exist, or the value at that key is the
				//exact same as the source's value
				boolean acceptAllDestination = false;
				boolean acceptAllSource = false;
				DaemonManager dm = new DaemonManager();
				for (String[] key : source.keySet(new String[0])) {
					if (destination.hasKey(key)) {
						if (!source.get(key).equals(destination.get(key))) {
							String data;
							//If the key is null, it's empty, so we can just stick it in, no
							//problem. If there is data there, it's a conflict, and we need to
							//ask.
							if (destination.get(key) != null) {
								boolean useSource = false;
								boolean useDestination = false;
								if (acceptAllDestination || acceptAllSource) {
									TermColors.p(TermColors.RED + "Conflict found for " + StringUtils.Join(key, ".") + ", using ");
									if (useSource) {
										useSource = true;
										TermColors.p("source");
									} else {
										useDestination = true;
										TermColors.p("destination");
									}
									pl(" value.");

								} else {
									pl(TermColors.BG_RED + TermColors.BRIGHT_WHITE + "The key " + StringUtils.Join(key, ".") + " has a different value"
											+ " in the source and the destination: " + TermColors.reset());
									pl(TermColors.WHITE + "Source: " + source.get(key));
									pl(TermColors.WHITE + "Destination: " + destination.get(key));
									do {
										pl(TermColors.YELLOW + "Would you like to keep " + TermColors.CYAN + "S" + TermColors.YELLOW + "ource, "
												+ "keep " + TermColors.GREEN + "D" + TermColors.YELLOW + "estination, keep " + TermColors.MAGENTA
												+ "A" + TermColors.YELLOW + "ll " + TermColors.MAGENTA + "S" + TermColors.YELLOW + "ource, or keep "
												+ TermColors.BLUE + "A" + TermColors.YELLOW + "ll " + TermColors.BLUE + "D" + TermColors.YELLOW + "estination?");
										pl(TermColors.WHITE + "["
												+ TermColors.CYAN + "S"
												+ TermColors.WHITE + "/"
												+ TermColors.GREEN + "D"
												+ TermColors.WHITE + "/"
												+ TermColors.MAGENTA + "AS"
												+ TermColors.WHITE + "/"
												+ TermColors.BLUE + "AD"
												+ TermColors.WHITE + "]");
										String response = TermColors.prompt();
										if ("AS".equalsIgnoreCase(response)) {
											acceptAllSource = true;
											useSource = true;
										} else if ("AD".equalsIgnoreCase(response)) {
											acceptAllDestination = true;
											useDestination = true;
										} else if ("S".equalsIgnoreCase(response)) {
											useSource = true;
										} else if ("D".equalsIgnoreCase(response)) {
											useDestination = true;
										} else {
											continue;
										}
										break;
									} while (true);
								}
								if (useSource) {
									data = source.get(key);
								} else if (useDestination) {
									data = destination.get(key);
								} else {
									throw new RuntimeException("Invalid state, both useSource and useDestination are false");
								}
								//Ok, now put the data in the destination
							} else {
								//Otherwise, just use the data in the source
								data = source.get(key);
							}
							destination.set(dm, key, data);
						}
					} else {
						//Else there is no conflict, it's not in the destination.
						destination.set(dm, key, source.get(key));
					}
				}
				try {
					dm.waitForThreads();
				} catch (InterruptedException ex) {
					//
				}
				break;
			} catch (DataSourceException ex) {
				pl(TermColors.RED + ex.getMessage());
			} catch (ReadOnlyException ex) {
				pl(TermColors.RED + ex.getMessage());
			} catch (IOException ex) {
				pl(TermColors.RED + ex.getMessage());
			}
		} while (true);
		pl(TermColors.GREEN + "Done merging!");
	}

	public static void cleardb() {
		try{
			pl(TermColors.RED + "Are you absolutely sure you want to clear out your database? " + TermColors.BLINKON + "No backup is going to be made." + TermColors.BLINKOFF);
			pl(TermColors.WHITE + "This will completely wipe your persistence information out. (No other data will be changed)");
			pl("[YES/No]");
			String choice = TermColors.prompt();
			if (choice.equals("YES")) {
				pl("Positive? [YES/No]");
				if (TermColors.prompt().equals("YES")) {
					TermColors.p("Ok, here we go... ");
					Set<String[]> keySet = persistenceNetwork.getNamespace(new String[]{}).keySet();
					DaemonManager dm = new DaemonManager();
					for(String [] key : keySet){
						try {
							persistenceNetwork.clearKey(dm, key);
						} catch (ReadOnlyException ex) {
							pl(TermColors.RED + "Read only data source found: " + ex.getMessage());
						}
					}
					try{
						dm.waitForThreads();
					} catch(InterruptedException e){
						//
					}
					pl("Done!");
				}
			} else if (choice.equalsIgnoreCase("yes")) {
				pl("No, you have to type YES exactly.");
			}
		} catch(DataSourceException ex){
			pl(TermColors.RED + ex.getMessage());
		} catch(IOException ex){
			pl(TermColors.RED + ex.getMessage());
		}
	}

	public static void help(String[] args) {
		if (args.length < 1 || args[0].isEmpty()) {
			pl("Currently, your options are:\n"
					+ "\t" + TermColors.GREEN + "refactor" + TermColors.WHITE + " - Allows you to shuffle data around in the persistence network more granularly than the merge tool.\n"
					+ "\t" + TermColors.GREEN + "print" + TermColors.WHITE + " - Prints out the information from your persisted data\n"
					+ "\t" + TermColors.GREEN + "cleardb" + TermColors.WHITE + " - Clears out your database of persisted data\n"
					+ "\t" + TermColors.GREEN + "edit" + TermColors.WHITE + " - Allows you to edit individual fields\n"
					+ "\t" + TermColors.GREEN + "interpreter" + TermColors.WHITE + " - Command Line Interpreter mode. Most minecraft related functions don't work.\n"
					+ "\t" + TermColors.GREEN + "merge" + TermColors.WHITE + " - Merges an entire database from one backend into another, even across formats. (Not individual keys.)\n"
					+ "\t\tYou can also use this tool to an extent to import or export data.\n"
					+ "\t" + TermColors.GREEN + "hidden-keys" + TermColors.WHITE + " - Lists all hidden keys in known data sources"
					+ "\n\t" + TermColors.RED + "exit" + TermColors.WHITE + " - Quits the Data Manager\n");

			pl("Type " + TermColors.MAGENTA + "help <command>" + TermColors.WHITE + " for more details about a specific command");
		} else {
			if(null != args[0])switch (args[0]) {
				case "refactor":
					pl("This tool allows you to granularly move individual keys from one datasource to another."
							+ " Unlike the merge tool, this works with individual keys, not necessarily keys that are"
							+ " within a particular data source. There are three required inputs, the transfer key pattern,"
							+ " the input configuration file, and the output configuration file. Data is transferred from"
							+ " one configuration to the other, that is, it is added in the new place, and removed in the old place."
							+ " This tool is more complicated"
							+ " than the merge tool, so consider using the other tool for simple tasks.");
					break;
				case "upgrade":
					pl("Converts any old formatted data into the new format. Any data that doesn't explicitely"
							+ " match the old format is not touched. Do not use this utility unless specifically"
							+ " told to during upgrade notices.");
					break;
				case "print":
					pl("Prints out the information in your persistence file. Entries may be narrowed down by"
							+ " specifying the namespace (for instance " + TermColors.MAGENTA + "print user.username" + TermColors.WHITE
							+ " will only show that particular users's aliases.) This is namespace based, so you"
							+ " must provide the entire namespace that your are trying to narrow down."
							+ "(" + TermColors.MAGENTA + "print storage" + TermColors.WHITE + " is valid, but " + TermColors.MAGENTA + "print stor"
							+ TermColors.WHITE + " is not)");
					break;
				case "cleardb":
					pl("Wipes your database clean of CommandHelper's persistence entries, but not other data. This"
							+ " includes any data that CommandHelper would have inserted into the database, or data"
							+ " that CommandHelper otherwise knows how to use. If using Serialized Persistence (ser), this"
							+ " means the entire file. For other data backends, this may vary slightly, for instance,"
							+ " an SQL backend would only have the CH specific tables truncated, but the rest of the"
							+ " database would remain untouched.");
					break;
				case "edit":
					pl("Allows you to manually edit the values in the database. You have the option to add or edit an existing"
							+ " value, delete a single value, or view the value of an individual key.");
					break;
				case "interpreter":
					pl("Generally speaking, works the same as the in game interpreter mode, but none"
							+ " of the minecraft related functions will work. You should not"
							+ " run this while the server is operational.");
					break;
				case "merge":
					pl("The merge tool allows you to shuffle persisted data around as entire databases, not as individual keys, however."
							+ " You specify the source database, and the output database, and it copies all the database entries. This"
							+ " can be used to an extent to import and export values, but it is not granular at all. Key conflicts are"
							+ " handled by prompting the user for an action, whether to overwrite the destination's value, or to keep"
							+ " it as is. Thusly, this operation is very safe from accidentally deleting your data. Keys that don't exist"
							+ " in the destination already are simply copied, and keys that have the same value are skipped. No changes"
							+ " are made to the source database.");
					break;
				case "hidden-keys":
					pl("The hidden-keys tool allows you to locate any \"hidden keys,\" that is, keys that exist in a data source,"
							+ " but can't be accessed normally. This can happen if you make changes to your persistence.ini file"
							+ " but don't refactor or otherwise migrate the data when you \"hide\" the keys. For instance, say you"
							+ " only have \"**=sqlite://persistence.db\" in your file, and you store some value in \"storage.a\". Later, you"
							+ " add \"storage.a=json://file.json\" to your persistence.ini file, but you don't refactor. The value stored"
							+ " at \"storage.a\" in the sqlite file is now inaccessible, and if you store another value in \"storage.a,\" the"
							+ " new value would be stored in file.json, and the original value in the sqlite file would simply be dead"
							+ " memory. This wouldn't cause any direct issues, but if a significant number of keys are \"dead,\" this would"
							+ " take up hard disk space for no reason. Additionally, refactors could have unexpected results.\n\n"
							+ "You will have the option to view or delete the hidden keys. Viewing them will print out a summary"
							+ " of all the keys, and deleting them will allow you to wholesale delete them.");
					break;
				case "exit":
					pl("Exits the data manager");
					break;
				default:
					pl("That's not a recognized command: '" + args[0] + "'");
					break;
			}
		}
	}

	public static void edit() {
		TermColors.cls();
		while (true) {
			pl("Would you like to " + TermColors.GREEN + "(a)dd/edit" + TermColors.WHITE
					+ " a value, " + TermColors.RED + "(r)emove" + TermColors.WHITE + " a value, " + TermColors.CYAN
					+ "(v)iew" + TermColors.WHITE + " a single value, or "
					+ TermColors.MAGENTA + "(s)top" + TermColors.WHITE + " editting? [" + TermColors.GREEN + "A" + TermColors.WHITE + "/"
					+ TermColors.RED + "R" + TermColors.WHITE + "/" + TermColors.CYAN + "V" + TermColors.WHITE + "/" + TermColors.MAGENTA + "S" + TermColors.WHITE + "]");
			String choice = TermColors.prompt();
			if (choice.equalsIgnoreCase("s") || choice.equalsIgnoreCase("exit")) {
				break;
			} else if (choice.equalsIgnoreCase("a")) {
				pl("Type the name of the key " + TermColors.YELLOW + "EXACTLY" + TermColors.WHITE + " as shown in the"
						+ " persistence format,\nnot the format you use when using store_value().");
				String key = TermColors.prompt();
				pl("Provide a value for " + TermColors.CYAN + key + TermColors.WHITE + ". This value you provide will"
						+ " be interpreted as pure MethodScript. (So things like array() will work)");
				String value = TermColors.prompt();
				if (doAddEdit(key, value)) {
					pl("Value changed!");
				}
			} else if (choice.equalsIgnoreCase("r")) {
				pl("Type the name of the key " + TermColors.YELLOW + "EXACTLY" + TermColors.WHITE + " as shown in the"
						+ " persistence format,\nnot the format you use when using store_value().");
				String key = TermColors.prompt();
				if (doRemove(key)) {
					pl("Value removed!");
				} else {
					pl("That value wasn't in the database to start with");
				}
			} else if (choice.equalsIgnoreCase("v")) {
				pl("Type the name of the key " + TermColors.YELLOW + "EXACTLY" + TermColors.WHITE + " as shown in the"
						+ " persistence format,\nnot the format you use when using store_value().");
				String key = TermColors.prompt();
				doView(key);
			} else {
				pl("I'm sorry, that's not a valid choice.");
			}
		}

	}

	public static boolean doView(String key) {
		try {
			String [] k = key.split("\\.");
			if (!persistenceNetwork.hasKey(k)) {
				pl(TermColors.RED + "That value is not set!");
				return true;
			}
			pl(TermColors.CYAN + key + ":" + TermColors.WHITE + persistenceNetwork.get(k));
			return true;
		} catch (DataSourceException ex) {
			pl(TermColors.RED + ex.getMessage());
			return false;
		}
	}

	public static boolean doAddEdit(String key, String valueScript) {
		try {
			Environment env = Environment.createEnvironment(gEnv, new CommandHelperEnvironment());
			Construct c = MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(valueScript, null, true)), env, null, null);
			String value = Construct.json_encode(c, Target.UNKNOWN);
			pl(TermColors.CYAN + "Adding: " + TermColors.WHITE + value);
			String [] k = key.split("\\.");
			DaemonManager dm = new DaemonManager();
			persistenceNetwork.set(dm, k, value);
			try{
				dm.waitForThreads();
			} catch(InterruptedException e){
				//
			}
			return true;
		} catch (Exception ex) {
			pl(TermColors.RED + ex.getMessage());
			return false;
		}
	}

	public static boolean doRemove(String key) {
		try {
			String [] k = key.split("\\.");
			if (persistenceNetwork.hasKey(k)) {
				DaemonManager dm = new DaemonManager();
				persistenceNetwork.clearKey(dm, k);
				try{
					dm.waitForThreads();
				} catch(InterruptedException e){
					//
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			pl(TermColors.RED + ex.getMessage());
			return false;
		}
	}

	public static void print(String[] args) {
		try{
			int count = 0;
			for(String [] key : persistenceNetwork.getNamespace(new String[]{}).keySet()){
				count++;
				pl(TermColors.CYAN + StringUtils.Join(key, ".") + ": " + TermColors.WHITE + persistenceNetwork.get(key));
			}
			pl(TermColors.BLUE + count + " items found");
		} catch(Exception e){
			pl(TermColors.RED + e.getMessage());
		}
	}

	public static void refactor(){
		pl("This tool allows you to granularly move individual keys from one datasource to another."
				+ " Unlike the merge tool, this works with individual keys, not necessarily keys that are"
				+ " within a particular data source. There are three required inputs, the transfer key pattern,"
				+ " the input configuration file, and the output configuration file. Data is transferred from"
				+ " one configuration to the other, that is, it is added in the new place, and removed in the old place."
				+ " This tool is more complicated"
				+ " than the merge tool, so consider using the other tool for simple tasks.\n\n");
		pl("Would you like to continue? [" + TermColors.GREEN + "Y" + TermColors.WHITE + "/"
				+ TermColors.RED + "N" + TermColors.WHITE + "]");
		String choice = TermColors.prompt();
		if("Y".equals(choice)){
			String filter;
			File input;
			File output;
			while(true){
				while(true){
					pl("What keys are you interested in transferring? The filter should be in the same format as the persistence.ini file, i.e."
							+ " \"storage.test\" or \"storage.test.**\". If a wildcard is used, multiple keys may be moved, otherwise, only one will"
							+ " be.");
					filter = TermColors.prompt();
					break;
				}
				File def = MethodScriptFileLocations.getDefault().getPersistenceConfig();
				while(true) {
					pl("What is the input configuration (where keys will be read in from, then deleted)? Leave blank for the default, which is " + def.toString()
							+ ". The path should be relative to " + jarLocation.toString());
					String sinput = TermColors.prompt();
					if("".equals(sinput.trim())){
						input = def;
					} else {
						File temp = new File(sinput);
						if(!temp.isAbsolute()){
							temp = new File(jarLocation, sinput);
						}
						input = temp;
					}
					if(!input.exists() || !input.isFile()){
						pl(TermColors.RED + input.toString() + " isn't a file. Please enter an existing file.");
					} else {
						break;
					}
				}
				while(true){
					pl("What is the output configuration (where keys will be written to)? The path should be relative to " + jarLocation.toString());
					String soutput = TermColors.prompt();
					if("".equals(soutput.trim())){
						pl(TermColors.RED + "The output cannot be empty");
						continue;
					} else {
						File temp = new File(soutput);
						if(!temp.isAbsolute()){
							temp = new File(jarLocation, soutput);
						}
						output = temp;
					}
					if(!output.exists() || !output.isFile()){
						pl(TermColors.RED + output.toString() + " isn't a file. Please enter an existing file.");
					} else {
						break;
					}
				}

				pl("The filter is \"" + TermColors.MAGENTA + filter + TermColors.WHITE + "\".");
				pl("The input configuration is \"" + TermColors.MAGENTA + input.toString() + TermColors.WHITE + "\".");
				pl("The output configuration is \"" + TermColors.MAGENTA + output.toString() + TermColors.WHITE + "\".");
				pl("Is this correct? [" + TermColors.GREEN + "Y" + TermColors.WHITE + "/"
					+ TermColors.RED + "N" + TermColors.WHITE + "]");
				if("Y".equals(TermColors.prompt())){
					break;
				}
			}
			pl(TermColors.YELLOW + "Now beginning transfer...");
			URI defaultURI;
			try {
				defaultURI = new URI("file://persistence.db");
			} catch (URISyntaxException ex) {
				throw new Error(ex);
			}
			ConnectionMixinFactory.ConnectionMixinOptions mixinOptions = new ConnectionMixinFactory.ConnectionMixinOptions();
			try {
				DaemonManager dm = new DaemonManager();
				mixinOptions.setWorkingDirectory(chDirectory);
				PersistenceNetwork pninput = new PersistenceNetwork(input, defaultURI, mixinOptions);
				PersistenceNetwork pnoutput = new PersistenceNetwork(output, defaultURI, mixinOptions);
				Pattern p = Pattern.compile(DataSourceFilter.toRegex(filter));
				Map<String[], String> inputData = pninput.getNamespace(new String[]{});
				boolean errors = false;
				int transferred = 0;
				int skipped = 0;
				for(String [] k : inputData.keySet()){
					String key = StringUtils.Join(k, ".");
					if(p.matcher(key).matches()){
						pl(TermColors.GREEN + "transferring " + TermColors.YELLOW + key);
						//This key matches, so we need to add it to the output network, and then remove it
						//from the input network
						if(pnoutput.getKeySource(k).equals(pninput.getKeySource(k))){
							continue; //Don't transfer it if it's the same source, otherwise we would
									  //end up just deleting it.
						}
						try {
							pnoutput.set(dm, k, inputData.get(k));
							transferred++;
							try {
								pninput.clearKey(dm, k);
							} catch (ReadOnlyException ex) {
								pl(TermColors.RED + "Could not clear out original key for the value for \"" + TermColors.MAGENTA + StringUtils.Join(k, ".") + TermColors.RED + "\", as the input"
										+ " file is set to read only.");
								errors = true;
							}
						} catch (ReadOnlyException ex) {
							pl(TermColors.RED + "Could not write out the value for \"" + TermColors.MAGENTA + StringUtils.Join(k, ".") + TermColors.RED + "\", as the output"
									+ " file is set to read only.");
							errors = true;
						}
					} else {
						skipped++;
					}
				}
				pl(TermColors.YELLOW + StringUtils.PluralTemplateHelper(transferred, "%d key was", "%d keys were") + " transferred.");
				pl(TermColors.YELLOW + StringUtils.PluralTemplateHelper(skipped, "%d key was", "%d keys were") + " skipped.");
				if(errors){
					pl(TermColors.YELLOW + "Other than the errors listed above, all keys were transferred successfully.");
				} else {
					pl(TermColors.GREEN + "Done!");
				}
				pl(TermColors.GREEN + "If this is being done as part of an entire transfer process, don't forget to set " + output.toString()
						+ " as your main Persistence Network configuration file.");
			} catch (IOException ex) {
				Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
			} catch (DataSourceException ex) {
				Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public static void hiddenKeys(){
		String action;
		while(true){
			pl(TermColors.WHITE + "Would you like to \"" + TermColors.GREEN + "view" + TermColors.WHITE + "\" or \"" + TermColors.RED + "delete" + TermColors.WHITE + "\" the hidden keys (default: view)? [view/delete]");
			action = TermColors.prompt();
			if("".equals(action)){
				action = "view";
			}
			if("view".equals(action) || "delete".equals(action)){
				break;
			} else {
				pl(TermColors.RED + "Invalid selection.");
			}
		}
		File configuration = MethodScriptFileLocations.getDefault().getPersistenceConfig();
		while(true){
			pl("Currently, " + configuration.getAbsolutePath() + " is being used as the persistence config file, but you may"
					+ " specify another (blank to use the default).");
			String file = TermColors.prompt();
			if("".equals(file)){
				break;
			} else {
				File f = new File(file);
				if(f.exists()){
					configuration = f;
					break;
				} else {
					pl(TermColors.RED + "The file you specified doesn't seem to exist, please enter it again.");
				}
			}
		}
		pl(TermColors.YELLOW + "Using " + configuration.getAbsolutePath() + " as our Persistence Network config.");
		File workingDirectory = MethodScriptFileLocations.getDefault().getConfigDirectory();
		while(true){
			pl("Currently, " + workingDirectory.getAbsolutePath() + " is being used as the default \"working directory\" for the"
					+ " persistence config file, but you may specify another (blank to use the default).");
			String file = TermColors.prompt();
			if("".equals(file)){
				break;
			} else {
				File f = new File(file);
				if(f.exists()){
					workingDirectory = f;
					break;
				} else {
					pl(TermColors.RED + "The file you specified doesn't seem to exist, please enter it again.");
				}
			}
		}
		pl(TermColors.YELLOW + "Using " + workingDirectory.getAbsolutePath() + " as our Persistence Network config working directory.");
		try {
			DataSourceFilter filter = new DataSourceFilter(configuration, new URI("sqlite://persistence.db"));
			ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
			options.setWorkingDirectory(workingDirectory);
			Set<URI> uris = filter.getAllConnections();
			boolean noneFound = true;
			int runningTotal = 0;
			for (URI uri : uris) {

				DataSource ds = DataSourceFactory.GetDataSource(uri, options);
				Map<String[], String> db = ds.getValues(new String[0]);
				Map<String[], String> map = new HashMap<>();
				DaemonManager dm = new DaemonManager();
				try {
					for(String[] key : db.keySet()){
						if(!filter.getConnection(key).equals(uri)){
							map.put(key, db.get(key));
							if("delete".equals(action)){
								ds.clearKey(dm, key);
							}
						}
					}
					runningTotal += map.size();
				} catch (ReadOnlyException ex) {
					pl(TermColors.RED + "Cannot delete any keys from " + uri + " as it is marked as read only, so it is being skipped.");
				}
				if("delete".equals(action)){
					try {
						dm.waitForThreads();
					} catch (InterruptedException ex) {
						// Ignored
					}
				}
				if(!map.isEmpty()){
					noneFound = false;
					if("view".equals(action)){
						pl("Found " + StringUtils.PluralTemplateHelper(map.size(), "one hidden key", "%d hidden keys") + " in data source "
							+ TermColors.MAGENTA + uri.toString());
						for(String[] key : map.keySet()){
							pl("\t" + TermColors.GREEN + StringUtils.Join(key, ".") + TermColors.WHITE + ":" + TermColors.CYAN + map.get(key));
						}
						if(ds.hasModifier(DataSource.DataSourceModifier.READONLY)){
							pl(TermColors.YELLOW + "This data source is marked as read only, and the keys cannot be deleted from it by this utility.");
						}
						pl();
					}
				}
			}
			if(noneFound){
				pl(TermColors.GREEN + "Done searching, no hidden keys were found.");
			} else {
				if("delete".equals(action)){
					pl(TermColors.GREEN + "Done, " + StringUtils.PluralTemplateHelper(runningTotal, "one hidden key was", "%d hidden keys were") + " deleted.");
				} else {
					pl(TermColors.GREEN + "Found " + StringUtils.PluralTemplateHelper(runningTotal, "one hidden key", "%d hidden keys") + " in total.");
				}
			}
		} catch(URISyntaxException | IOException | DataSourceException ex){
			pl(TermColors.RED + ex.getMessage());
			ex.printStackTrace(StreamUtils.GetSystemErr());
		}
	}

	private static void pl(){
		out.println();
	}

	private static void pl(String string){
		out.println(string + TermColors.WHITE);
	}
}
