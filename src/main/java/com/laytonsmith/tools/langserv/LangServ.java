package com.laytonsmith.tools.langserv;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Triplet;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.AbstractCommandLineTool;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.functions.DocumentLinkProvider;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.tool;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.tools.docgen.DocGen;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkOptions;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 *
 */
public class LangServ implements LanguageServer, LanguageClientAware, TextDocumentService, WorkspaceService {

	@MSLog.LogTag
	public static final MSLog.Tag LANGSERVLOGTAG = new MSLog.Tag() {
		@Override
		public String getName() {
			return "langserv";
		}

		@Override
		public String getDescription() {
			return "Logs events related to the Language Server";
		}

		@Override
		public LogLevel getLevel() {
			return LogLevel.WARNING;
		}
	};


	@tool("lang-serv")
	public static class LangServMode extends AbstractCommandLineTool {


		@Override
		public ArgumentParser getArgumentParser() {
			return ArgumentParser.GetParser()
					.addDescription("Starts up the language server, which implements the Language Server Protocol")
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("The host location that the client is running on.")
						.setUsageName("host")
						.setOptional()
						.setName("host")
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.STRING))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("The port the client is running on.")
						.setUsageName("port")
						.setOptional()
						.setName("port")
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.NUMBER))

					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("If set, stdio is used instead of socket connections.")
						.asFlag()
						.setName("stdio"))

					.setErrorOnUnknownArgs(false)
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("For future compatibility reasons, unrecognized arguments are not an error,"
								+ " but they are not supported unless otherwise noted.")
						.setUsageName("unrecognizedArgs")
						.setOptionalAndDefault()
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.ARRAY_OF_STRINGS));
		}

		@Override
		public boolean startupExtensionManager() {
			return false;
		}

		@Override
		@SuppressWarnings("UseSpecificCatch")
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			boolean useStdio = parsedArgs.isFlagSet("stdio");
			String hostname = null;
			int port = 0;
			if(!useStdio) {
				hostname = parsedArgs.getStringArgument("host");
				port = parsedArgs.getNumberArgument("port").intValue();
			}

			LangServ langserv = new LangServ(useStdio);
			langserv.log("Starting up Language Server: " + parsedArgs.getRawArguments(), LogLevel.INFO);
			try {
				InputStream is;
				OutputStream os;
				if(!useStdio) {
					Socket socket = new Socket(hostname, port);
					socket.setKeepAlive(true);
					is = socket.getInputStream();
					os = socket.getOutputStream();
				} else {
					is = System.in;
					os = System.out;
				}
				Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(langserv, is, os);
				LanguageClient client = launcher.getRemoteProxy();
				((LanguageClientAware) langserv).connect(client);
				RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
				List<String> arguments = runtimeMxBean.getInputArguments();
				langserv.log("Java started with args: " + arguments.toString(), LogLevel.DEBUG);
				langserv.log("Starting language server", LogLevel.INFO);
				if(useStdio) {
					System.err.println("Started Language Server, awaiting connections");
				}
				launcher.startListening();
			} catch (Throwable t) {
				t.printStackTrace(System.err);
				System.exit(1);
			}
		}

		@Override
		public boolean noExitOnReturn() {
			return true;
		}
	}

	// <editor-fold defaultstate="collapsed" desc="Loggers">
	public void log(String s, LogLevel level) {
		log(() -> s, level);
	}

	public void log(MSLog.StringProvider s, LogLevel level) {
		MSLog.GetLogger().Log(LANGSERVLOGTAG, level, s, Target.UNKNOWN);
		if(client != null && MSLog.GetLogger().WillLog(LANGSERVLOGTAG, level)) {
			MessageType type;
			switch(level) {
				case DEBUG:
					type = MessageType.Log;
					break;
				case INFO:
					type = MessageType.Info;
					break;
				case WARNING:
					type = MessageType.Warning;
					break;
				case ERROR:
					type = MessageType.Error;
					break;
				default:
					type = MessageType.Log;
					break;
			}

			String full = s.getString();
			client.logMessage(new MessageParams(type, full));
			if(level == LogLevel.ERROR) {
				client.showMessage(new MessageParams(MessageType.Error, full));
			}
		}
	}

	public void loge(Throwable t) {
		log(StackTraceUtils.GetStacktrace(t), LogLevel.ERROR);
	}

	public void loge(String s) {
		log(s, LogLevel.ERROR);
	}

	public void loge(MSLog.StringProvider s) {
		log(s, LogLevel.ERROR);
	}

	public void logw(String s) {
		log(s, LogLevel.WARNING);
	}

	public void logw(MSLog.StringProvider s) {
		log(s, LogLevel.WARNING);
	}

	public void logi(String s) {
		log(s, LogLevel.INFO);
	}

	public void logi(MSLog.StringProvider s) {
		log(s, LogLevel.INFO);
	}

	public void logd(String s) {
		log(s, LogLevel.DEBUG);
	}

	public void logd(MSLog.StringProvider s) {
		log(s, LogLevel.DEBUG);
	}

	public void logv(String s) {
		log(s, LogLevel.VERBOSE);
	}

	public void logv(MSLog.StringProvider s) {
		log(s, LogLevel.VERBOSE);
	}
	//</editor-fold>

	public LangServ(boolean useStdio) {
		this.usingStdio = useStdio;
	}

	private final boolean usingStdio;

	private LanguageClient client;

	/**
	 * This executor uses an unbounded thread pool, and should only be used for task in which a user is actively
	 * waiting for results, however, tasks submitted to this processor will begin immediately, as opposed to the
	 * lowPriorityProcessors queue, which has a fixed size thread pool.
	 */
	private final Executor highPriorityProcessors = Executors.newCachedThreadPool(new ThreadFactory() {
		private int count = 0;
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "HighPriority-thread-pool-" + (++count));
		}
	});
	/**
	 * This executor uses a bounded thread pool, and should be used for all tasks that a user is not actively waiting
	 * on, for instance, compilation of files that are not open.
	 */
	private final Executor lowPriorityProcessors = Executors.newFixedThreadPool(5, new ThreadFactory() {
		private int count = 0;
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "LowPriority-thread-pool-" + (++count));
		}
	});
	private List<CompletionItem> functionCompletionItems = null;
	private List<CompletionItem> objectCompletionItems = null;
	private List<CompletionItem> eventCompletionItems = null;
	private List<CompletionItem> allCompletionItems = null;

	@Override
	@SuppressWarnings("UseSpecificCatch")
	public void connect(LanguageClient client) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		this.client = client;
		highPriorityProcessors.execute(() -> {
			// Create the base completion list.
			{
				List<CompletionItem> list = new ArrayList<>();
				for(FunctionBase fb : FunctionList.getFunctionList(api.Platforms.INTERPRETER_JAVA, null)) {
					if(fb.getClass().getAnnotation(hide.class) != null) {
						continue;
					}
					DocGen.DocInfo di;
					try {
						di = new DocGen.DocInfo(fb.docs());
					} catch (IllegalArgumentException ex) {
						MSLog.GetLogger().Log(LANGSERVLOGTAG, LogLevel.ERROR, "Error parsing function \""
								+ fb.getName() + "\". " + ex.getMessage(), Target.UNKNOWN);
						continue;
					}
					CompletionItem ci = new CompletionItem(fb.getName());
//					ci.setCommitCharacters(Arrays.asList("("));
					ci.setKind(CompletionItemKind.Function);
					ci.setDetail(di.ret);
					ci.setDocumentation(di.originalArgs + "\n\n" + di.desc
							+ (di.extendedDesc == null ? "" : "\n\n" + di.extendedDesc));
					list.add(ci);
				}
				functionCompletionItems = list;
				logv("Function completion list completed. (" + list.size() + ")");
			}
			{
				List<CompletionItem> list = new ArrayList<>();
				for(Event e : EventList.GetEvents()) {
					final DocGen.EventDocInfo edi;
					try {
						edi = new DocGen.EventDocInfo(e.docs(), e.getName());
					} catch (IllegalArgumentException ex) {
						MSLog.GetLogger().Log(LANGSERVLOGTAG, LogLevel.ERROR, ex.getMessage(), Target.UNKNOWN);
						continue;
					}
					CompletionItem ci = new CompletionItem(e.getName());
					ci.setCommitCharacters(Arrays.asList("'", "\""));
					ci.setKind(CompletionItemKind.Function);
					ci.setDetail("Event Type");
					StringBuilder description = new StringBuilder();
					description.append(edi.description).append("\n");
					if(!edi.prefilter.isEmpty()) {
						for(DocGen.EventDocInfo.PrefilterData pdata : edi.prefilter) {
							description.append(pdata.name).append(": ")
									.append(pdata.formatDescription(DocGen.MarkupType.TEXT))
									.append("\n");
						}
						description.append("\n");
					}
					if(!edi.eventData.isEmpty()) {
						for(DocGen.EventDocInfo.EventData edata : edi.eventData) {
							description.append(edata.name)
									.append(!edata.description.isEmpty() ? ": " + edata.description : "")
									.append("\n");
						}
						description.append("\n");
					}
					if(!edi.mutability.isEmpty()) {
						for(DocGen.EventDocInfo.MutabilityData mdata : edi.mutability) {
							description.append(mdata.name)
									.append(!mdata.description.isEmpty() ? ": " + mdata.description : "")
									.append("\n");
						}
						description.append("\n");
					}
					ci.setDocumentation(description.toString());
					list.add(ci);
				}
				eventCompletionItems = list;
				logv("Event completion list completed. (" + list.size() + ")");
			}
			{
				List<CompletionItem> list = new ArrayList<>();
				for(FullyQualifiedClassName fqcn : NativeTypeList.getNativeTypeList()) {
					try {
						Mixed m = NativeTypeList.getInvalidInstanceForUse(fqcn);
						CompletionItem ci = new CompletionItem(m.typeof().getSimpleName());
						ci.setKind(CompletionItemKind.TypeParameter);
						ci.setDetail(m.getName());
						ci.setDocumentation(m.docs());
						ci.setCommitCharacters(Arrays.asList(" "));
						list.add(ci);
					} catch (Throwable ex) {
						// Skip it.
					}
				}
				objectCompletionItems = list;
				logv("Object completion list completed. (" + list.size() + ")");
			}
			allCompletionItems = new ArrayList<>();
			allCompletionItems.addAll(functionCompletionItems);
			allCompletionItems.addAll(eventCompletionItems);
			allCompletionItems.addAll(objectCompletionItems);
			logd("Completion list generated.");
		});
	}

	@java.lang.annotation.Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Command {
		/**
		 * The name of the command.
		 * @return
		 */
		String value();
	}

	public static interface CommandProvider {
		CompletableFuture<Object> execute(LanguageClient client, ExecuteCommandParams params);
	}

	// Need to implement stuff in the extension before this is useful
//	@Command("new-ms-file")
//	public static class NewMsFileCommand implements CommandProvider {
//
//		@Override
//		public CompletableFuture<Object> execute(LanguageClient client, ExecuteCommandParams params) {
//			CompletableFuture<Object> result = new CompletableFuture<>();
////			ShowMessageRequestParams smrp = new ShowMessageRequestParams();
////			MessageActionItem action = new MessageActionItem();
////			action.setTitle("Name of file");
////			smrp.setActions(Arrays.asList(action));
////			client.showMessageRequest(smrp).thenAccept(action -> {
////				action.
////			});
//			result.complete(null);
//			return result;
//		}
//
//	}

	private Map<String, CommandProvider> commandProviders = new HashMap<>();

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		// So base dir restrictions won't apply
		Security.setSecurityEnabled(false);
		CompletableFuture<InitializeResult> cf = new CompletableFuture<>();
		ServerCapabilities sc = new ServerCapabilities();
		sc.setTextDocumentSync(TextDocumentSyncKind.Full);
		DocumentLinkOptions documentLinkOptions = new DocumentLinkOptions();
		documentLinkOptions.setResolveProvider(false);
		sc.setDocumentLinkProvider(documentLinkOptions);
		{
			ExecuteCommandOptions eco = new ExecuteCommandOptions();
			List<String> commands = new ArrayList<>();
			for(Class<? extends CommandProvider> c : ClassDiscovery.getDefaultInstance()
					.loadClassesWithAnnotationThatExtend(Command.class, CommandProvider.class)) {
				CommandProvider cp;
				try {
					cp = c.newInstance();
				} catch (InstantiationException | IllegalAccessException ex) {
					// We can't recover from this, so just skip it
					Logger.getLogger(LangServ.class.getName()).log(Level.SEVERE, null, ex);
					continue;
				}
				String command = c.getAnnotation(Command.class).value();
				commands.add(command);
				commandProviders.put(command, cp);
			}
			eco.setCommands(commands);
			sc.setExecuteCommandProvider(eco);
		}
		CompletionOptions co = new CompletionOptions(true, Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h",
				"i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "_"));
		sc.setCompletionProvider(co);
		cf.complete(new InitializeResult(sc));
		if(usingStdio) {
			System.err.println("Language Server Connected");
		}
		return cf;
	}

	@Override
	public void initialized(InitializedParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		client.workspaceFolders().thenAccept((List<WorkspaceFolder> t) -> {
			for(WorkspaceFolder f : t) {
				File workspace = new File(f.getUri().replaceFirst("file://", ""));
				try {
					FileUtil.recursiveFind(workspace, (File f1) -> {
						if(f1.isFile() && (f1.getName().endsWith(".ms") || f1.getName().endsWith(".msa"))) {
							doCompilation(null, lowPriorityProcessors, f1.toURI().toString(), false);
						}
					});
				} catch (IOException ex) {
					client.logMessage(new MessageParams(MessageType.Warning, ex.getMessage()));
				}
			}
		});
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		CompletableFuture<Object> cf = new CompletableFuture<>();
		cf.complete(null);
		return cf;
	}

	@Override
	public void exit() {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		System.exit(0);
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		return this;
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		return this;
	}

	private static final int COMPILATION_DELAY = 3;

	private final Map<String, Triplet<Long, Executor, CompletableFuture<ParseTree>>> compileDelays = new HashMap<>();

	private Thread compilerDelayThread = null;

	private static DiagnosticSeverity getSeverity(CompilerWarning warning) {
		if(warning.getSuppressCategory() == null) {
			return DiagnosticSeverity.Warning;
		}
		switch(warning.getSuppressCategory().getSeverityLevel()) {
			case HIGH:
				return DiagnosticSeverity.Warning;
			case MEDIUM:
				return DiagnosticSeverity.Information;
			case LOW:
				return DiagnosticSeverity.Hint;
		}
		throw new Error("Unaccounted for case: " + warning.getSuppressCategory());
	}
	/**
	 * Compiles the file, on the given thread pool.
	 * @param future After compilation is done, the parse tree is returned. May be null if you don't need it.
	 * @param threadPool
	 * @param uri
	 * @param withDelay If true, waits {@link #COMPILATION_DELAY} seconds before compiling, resetting the timer if
	 * another request to compile the file comes in before the timer is up.
	 */
	@SuppressWarnings({"UseSpecificCatch", "SleepWhileInLoop"})
	public void doCompilation(CompletableFuture<ParseTree> future, Executor threadPool, final String uri,
			boolean withDelay) {
		// This has to be finished before compile on change can be enavled, but for now compile on save is good enough
//		if(compilerDelayThread == null) {
//			compilerDelayThread = new Thread(() -> {
//				try {
//					while(true) {
//						Thread.sleep(1000);
//						if(compileDelays.isEmpty()) {
//							continue;
//						}
//						Map<String, Triplet<Long, Executor, CompletableFuture<ParseTree>>> localCompileDelays;
//						synchronized(compileDelays) {
//							// Don't do the compilation while in the synchronized block,
//							// we want to just copy the map then leave the block
//							localCompileDelays = new HashMap<>(compileDelays);
//						}
//						for(Map.Entry<String, Triplet<Long, Executor, CompletableFuture<ParseTree>>> entry :
//								localCompileDelays.entrySet()) {
//							Triplet<Long, Executor, CompletableFuture<ParseTree>> params = entry.getValue();
//							if(params.getFirst() < System.currentTimeMillis()) {
//								doCompilation(params.getThird(), params.getSecond(), entry.getKey(), false);
//							}
//						}
//					}
//				} catch (InterruptedException ex) {
//					//
//				}
//			}, "CompilerDelayThread");
//			compilerDelayThread.setDaemon(true);
//			compilerDelayThread.start();
//		}
//		if(withDelay) {
//			synchronized(compileDelays) {
//				compileDelays.put(uri, new Triplet<>(System.currentTimeMillis() + (COMPILATION_DELAY * 1000),
//						threadPool, future));
//				return;
//			}
//		} else {
//			// If a compilation request withDelay = false comes in, we need to clear out the queue, since it will be
//			// compiled immediately anyways.
//		}


		threadPool.execute(() -> {
			try {
				Set<ConfigCompileException> exceptions = new HashSet<>();
				String code;
				// Eventually we want to rework this so that this is available
				Set<Class<? extends Environment.EnvironmentImpl>> envs = new HashSet<>();
				for(Class<Environment.EnvironmentImpl> c
						: ClassDiscovery.getDefaultInstance().loadClassesThatExtend(Environment.EnvironmentImpl.class)) {
					envs.add(c);
				}
				Environment env;
				try {
					env = Static.GenerateStandaloneEnvironment(false);
					// Make this configurable at some point. For now, however, we need this so we can get
					// correct handling on minecraft functions.
					env = env.cloneAndAdd(new CommandHelperEnvironment());
				} catch (IOException | DataSourceException | URISyntaxException | Profiles.InvalidProfileException ex) {
					throw new RuntimeException(ex);
				}
				CompilerEnvironment compilerEnv = env.getEnv(CompilerEnvironment.class);
				compilerEnv.setLogCompilerWarnings(false); // No one would see them
				GlobalEnv gEnv = env.getEnv(GlobalEnv.class);

				// This disables things like security checks and whatnot.
				// These may be present in the runtime environment,
				// but it's not possible for us to tell that at this point.
				gEnv.SetCustom("cmdline", true);
				URI uuri = new URI(uri);
				File f;
				if("untitled".equals(uuri.getScheme())) {
					// For open files that aren't saved to disk, the client sends something like "untitled:untitled-1",
					// which isn't a valid file provider, so we can't call Paths.get on it. Instead, we just mock
					// the name here. We also need to provide getAbsoluteFile, so that the below call to getParentFile
					// will work.
					f = new File(uuri.getSchemeSpecificPart()).getAbsoluteFile();
				} else {
					f = Paths.get(uuri).toFile();
				}
				gEnv.SetRootFolder(f.getParentFile());
				TokenStream tokens = null;
				ParseTree tree = null;
				try {
					ParseTree fTree;
					logd(() -> "Compiling " + f);
					code = getDocument(uri);
					if(f.getName().endsWith(".msa")) {
						tokens = MethodScriptCompiler.lex(code, env, f, false);
						fTree = new ParseTree(null);
						MethodScriptCompiler.preprocess(tokens, envs).forEach((script) -> {
							try {
								script.compile();
							} catch (ConfigCompileException ex) {
								exceptions.add(ex);
							} catch (ConfigCompileGroupException ex) {
								exceptions.addAll(ex.getList());
							}
							script.getTrees().forEach(r -> fTree.addChild(r));
						});
					} else {
						// Actually, for untitled files, this may not be a correct default. However, there's no
						// other good way of determining that, so let's just assume it's pure methodscript.
						tokens = MethodScriptCompiler.lex(code, env, f, true);
						fTree = MethodScriptCompiler.compile(tokens, env, envs);
					}
					tree = fTree;
				} catch (ConfigCompileException e) {
					exceptions.add(e);
				} catch (ConfigCompileGroupException e) {
					exceptions.addAll(e.getList());
				} catch (Throwable e) {
					// Just skip this, we can't do much here.
					loge(() -> StackTraceUtils.GetStacktrace(e));
				}
				List<Diagnostic> diagnosticsList = new ArrayList<>();
				if(!exceptions.isEmpty()) {
					logi(() -> "Errors found, reporting " + exceptions.size() + " errors");
					for(ConfigCompileException e : exceptions) {
						Diagnostic d = new Diagnostic();
						d.setRange(convertTargetToRange(tokens, e.getTarget()));
						d.setSeverity(DiagnosticSeverity.Error);
						d.setMessage(e.getMessage());
						diagnosticsList.add(d);
					}
				}
				List<CompilerWarning> warnings = compilerEnv.getCompilerWarnings();
				if(!warnings.isEmpty()) {
					for(CompilerWarning c : warnings) {
						Diagnostic d = new Diagnostic();
						d.setRange(convertTargetToRange(tokens, c.getTarget()));
						d.setSeverity(getSeverity(c));
						d.setMessage(c.getMessage());
						diagnosticsList.add(d);
					}
				}

				// We need to report to the client always, with an empty list, implying that all problems are fixed.
				PublishDiagnosticsParams diagnostics
						= new PublishDiagnosticsParams(uri, diagnosticsList);
				client.publishDiagnostics(diagnostics);

				if(future != null && tree != null) {
					future.complete(tree);
				}
			} catch (Throwable t) {
				client.logMessage(new MessageParams(MessageType.Error, t.getMessage() + "\n"
						+ StackTraceUtils.GetStacktrace(t)));
			}

		});
	}

	//<editor-fold defaultstate="collapsed" desc="DocumentManagement">

	/**
	 * Maps from URI to document text. If the document isn't in this map, it may be safely read from disk.
	 */
	private final Map<String, String> documents = new HashMap<>();

	/**
	 * Returns the document text either from the document cache, if the client is managing the document, or from
	 * the file system if it isn't.
	 * @param uri
	 * @return
	 * @throws java.io.IOException
	 */
	public String getDocument(String uri) throws IOException {
		if(documents.containsKey(uri)) {
			return documents.get(uri);
		}
		File f;
		try {
			f = Paths.get(new URI(uri)).toFile();
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
		return FileUtil.read(f);
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		// The document open notification is sent from the client to the server to signal newly opened text documents.
		// The document’s truth is now managed by the client and the server must not try to read the document’s truth
		// using the document’s Uri.
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		String uri = params.getTextDocument().getUri();
		documents.put(uri, params.getTextDocument().getText());
		doCompilation(null, highPriorityProcessors, uri, false);
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		logv(() -> "Changing " + params);
		String uri = params.getTextDocument().getUri();
		// If the processing mode is changed to incremental, this logic needs modification
//		String text = documents.get(uri);
		if(params.getContentChanges().size() > 1) {
			logw("Unexpected size from didChange event.");
		}
		for(TextDocumentContentChangeEvent change : params.getContentChanges()) {
			String newText = change.getText();
			documents.put(uri, newText);
		}
//		doCompilation(null, highPriorityProcessors, uri, true);
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		// The document close notification is sent from the client to the server when the document got closed in the
		// client. The document’s truth now exists where the document’s Uri points to (e.g. if the document’s Uri is
		// a file Uri the truth now exists on disk).
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		documents.remove(params.getTextDocument().getUri());
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		doCompilation(null, highPriorityProcessors, params.getTextDocument().getUri(), false);
	}

	//</editor-fold>

	public Range convertTargetToRange(ParseTree node) {
		String val = Construct.nval(node.getData());
		if(val == null) {
			val = "null";
		}
		int tokenLength = val.length();
		Target t = node.getTarget();
		if(tokenLength < 1) {
			// Something went wrong, but we always want an error to show up, so set this here
			tokenLength = 1;
		}
		// I'm not sure if the column offset -2 is because of a bug in the code target calculation,
		// or due to how VSCode indexes the column numbers, but either way it seems most all errors
		// suffer from the weird -2 offset.
		Position start = new Position(t.line() - 1, t.col() - 2);
		Position end = new Position(t.line() - 1, t.col() + tokenLength - 2);
		if(start.getLine() < 0) {
			start.setLine(0);
		}
		if(start.getCharacter() < 0) {
			start.setCharacter(0);
		}
		if(end.getLine() < 0) {
			end.setLine(0);
		}
		if(end.getCharacter() < 0) {
			end.setCharacter(1);
		}
		return new Range(start, end);
	}

	public Range convertTargetToRange(TokenStream tokens, Target t) {
		int tokenLength = 5;
		if(tokens != null) {
			for(int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				if(token.lineNum == t.line() && token.column == t.col()) {
					tokenLength = token.value.length();
					break;
				}
			}
		}
		if(tokenLength < 1) {
			// Something went wrong, but we always want an error to show up, so set this here
			tokenLength = 1;
		}
		// I'm not sure if the column offset -2 is because of a bug in the code target calculation,
		// or due to how VSCode indexes the column numbers, but either way it seems most all errors
		// suffer from the weird -2 offset.
		Position start = new Position(t.line() - 1, t.col() - 2);
		Position end = new Position(t.line() - 1, t.col() + tokenLength - 2);
		if(start.getLine() < 0) {
			start.setLine(0);
		}
		if(start.getCharacter() < 0) {
			start.setCharacter(0);
		}
		if(end.getLine() < 0) {
			end.setLine(0);
		}
		if(end.getCharacter() < 0) {
			end.setCharacter(1);
		}
		return new Range(start, end);
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
	}

//	@Override
//	public CompletableFuture<Hover> hover(final TextDocumentPositionParams position) {
//		final CompletableFuture<Hover> result = new CompletableFuture<>();
//		processors.execute(() -> {
//			position.getPosition().
//		});
//		return result;
//	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		logv(() -> String.format("Completion request sent: %s", position));
		CompletableFuture<Either<List<CompletionItem>, CompletionList>> result = new CompletableFuture<>();
		highPriorityProcessors.execute(() -> {
			result.complete(Either.forLeft(functionCompletionItems));
			logv(() -> "Completion list returned with " + functionCompletionItems.size() + " items");
		});
		return result;
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		logv(() -> unresolved.toString());
		CompletableFuture<CompletionItem> result = new CompletableFuture<>();
		// For now, this will always be correct, but once procs are added, this will not necessarily be true.
		result.complete(unresolved);
		return result;
	}

	@Override
	public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
		String uri = params.getTextDocument().getUri();
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		logv(() -> "Requested " + uri);
		CompletableFuture<ParseTree> future = new CompletableFuture<>();
		CompletableFuture<List<DocumentLink>> result = new CompletableFuture<>();
		doCompilation(future, lowPriorityProcessors, uri, false);
		future.thenAccept((tree) -> {
			Environment env;
			try {
				env = Static.GenerateStandaloneEnvironment(false);
			} catch (IOException | DataSourceException | URISyntaxException | Profiles.InvalidProfileException ex) {
				loge(ex);
				result.cancel(true);
				return;
			}
			List<DocumentLink> links = new ArrayList<>();
			tree.getAllNodes().forEach(node -> {
				if(node.getData() instanceof CFunction && ((CFunction) (node.getData())).hasFunction()) {
					try {
						Function f = ((CFunction) (node.getData())).getFunction();
						if(f instanceof DocumentLinkProvider) {
							logv(() -> "Found DocumentLinkProvider " + f.getName());
							for(ParseTree link : ((DocumentLinkProvider) f).getDocumentLinks(node.getChildren())) {
								if(link.isConst()) {
									File file = Static.GetFileFromArgument(link.getData().val(), env, link.getTarget(),
											null);
									if(file != null && file.exists() && file.isFile()) {
										logv(() -> "Found document link to " + file.toURI());
										DocumentLink docLink = new DocumentLink();
										docLink.setRange(convertTargetToRange(link));
										docLink.setTarget(file.toURI().toString());
										links.add(docLink);
									}
								}
							}
						}
					} catch (ConfigCompileException ex) {
						// Ignore this. This can be caused by procs, or other errors, but the point is, it's not a
						// DocumentLinkProvider.
					}
				}
			});
			result.complete(links);
		});
		return result;
	}

	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
		return commandProviders.get(params.getCommand()).execute(client, params);
	}
}
