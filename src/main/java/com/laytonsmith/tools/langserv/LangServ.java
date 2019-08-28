package com.laytonsmith.tools.langserv;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.AbstractCommandLineTool;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
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
import java.net.Socket;
import java.net.URISyntaxException;
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
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
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
						.setRequired()
						.setName("host")
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.STRING))
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("The port the client is running on.")
						.setUsageName("port")
						.setRequired()
						.setName("port")
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.NUMBER))

					.setErrorOnUnknownArgs(false)
					.addArgument(new ArgumentParser.ArgumentBuilder()
						.setDescription("For future compatibility reasons, unrecognized arguments are not an error,"
								+ " but they are not supported unless otherwise noted.")
						.setUsageName("unrecognizedArgs")
						.setOptionalAndDefault()
						.setArgType(ArgumentParser.ArgumentBuilder.BuilderTypeNonFlag.ARRAY_OF_STRINGS));
		}

		@Override
		@SuppressWarnings("UseSpecificCatch")
		public void execute(ArgumentParser.ArgumentParserResults parsedArgs) throws Exception {
			String hostname = parsedArgs.getStringArgument("host");
			int port = parsedArgs.getNumberArgument("port").intValue();

			LangServ langserv = new LangServ();
			langserv.log("Starting up Language Server: " + parsedArgs.getRawArguments(), LogLevel.INFO);
			try {
				Socket socket = new Socket(hostname, port);
				socket.setKeepAlive(true);

				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(langserv, is, os);
				LanguageClient client = launcher.getRemoteProxy();
				((LanguageClientAware) langserv).connect(client);
				langserv.log("Starting language server", LogLevel.INFO);
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

	public void logd(String s) {
		log(s, LogLevel.DEBUG);
	}

	public void logd(MSLog.StringProvider s) {
		log(s, LogLevel.DEBUG);
	}

	public void logi(String s) {
		log(s, LogLevel.INFO);
	}

	public void logi(MSLog.StringProvider s) {
		log(s, LogLevel.INFO);
	}

	public void logv(String s) {
		log(s, LogLevel.VERBOSE);
	}

	public void logv(MSLog.StringProvider s) {
		log(s, LogLevel.VERBOSE);
	}
	//</editor-fold>

	private LanguageClient client;
	private final Map<String, Map<Integer, ParseTree>> documents = new HashMap<>();
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
					CompletionItem ci = new CompletionItem(fb.getName());
					ci.setCommitCharacters(Arrays.asList("("));
					ci.setKind(CompletionItemKind.Function);
					DocGen.DocInfo di = new DocGen.DocInfo(fb.docs());
					ci.setDetail(di.ret);
					ci.setDocumentation(di.originalArgs + "\n\n" + di.desc
							+ (di.extendedDesc == null ? "" : "\n\n" + di.extendedDesc));
					list.add(ci);
				}
				functionCompletionItems = list;
				logv("Function completion list completed.");
			}
			{
				List<CompletionItem> list = new ArrayList<>();
				for(Event e : EventList.GetEvents()) {
					CompletionItem ci = new CompletionItem(e.getName());
					ci.setCommitCharacters(Arrays.asList("'", "\""));
					ci.setKind(CompletionItemKind.Function);
					final DocGen.EventDocInfo edi = new DocGen.EventDocInfo(e.docs(), e.getName());
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
				logv("Event completion list completed.");
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
				logv("Object completion list completed.");
			}
			allCompletionItems = new ArrayList<>();
			allCompletionItems.addAll(functionCompletionItems);
			allCompletionItems.addAll(eventCompletionItems);
			allCompletionItems.addAll(objectCompletionItems);
			logd("Completion list generated.");
		});
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		CompletableFuture<InitializeResult> cf = new CompletableFuture<>();
		ServerCapabilities sc = new ServerCapabilities();
		sc.setTextDocumentSync(TextDocumentSyncKind.Full);
//		sc.setHoverProvider(true);
		CompletionOptions co = new CompletionOptions(true, Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h",
				"i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "_"));
		sc.setCompletionProvider(co);
		cf.complete(new InitializeResult(sc));
		return cf;
	}

	@Override
	public void initialized(InitializedParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");

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

	/**
	 * Compiles the file, on the given thread pool.
	 * @param threadPool
	 * @param uri
	 */
	public void doCompilation(Executor threadPool, String uri) {
		threadPool.execute(() -> {
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
			} catch (IOException | DataSourceException | URISyntaxException | Profiles.InvalidProfileException ex) {
				throw new RuntimeException(ex);
			}
			CompilerEnvironment compilerEnv = env.getEnv(CompilerEnvironment.class);
			compilerEnv.setLogCompilerWarnings(false); // No one would see them
			GlobalEnv gEnv = env.getEnv(GlobalEnv.class);
			// This disables things like security checks and whatnot. These may be present in the runtime environment,
			// but it's not possible for us to tell that at this point.
			gEnv.SetCustom("cmdline", true);
			File f = new File(uri.replaceFirst("file://", ""));
			gEnv.SetRootFolder(f.getParentFile());
			TokenStream tokens = null;
			try {
				logd(() -> "Compiling " + f);
				code = FileUtil.read(f);
				tokens = MethodScriptCompiler.lex(code, env, f, true);
				MethodScriptCompiler.compile(tokens, env, envs);
			} catch (ConfigCompileException e) {
				exceptions.add(e);
			} catch (ConfigCompileGroupException e) {
				exceptions.addAll(e.getList());
			} catch (IOException e) {
				// Just skip this, we can't do much here.
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
					d.setSeverity(DiagnosticSeverity.Warning);
					d.setMessage(c.getMessage());
					diagnosticsList.add(d);
				}
			}

			// We need to report to the client always, with an empty list, implying that all problems are fixed.
			PublishDiagnosticsParams diagnostics
					= new PublishDiagnosticsParams(uri, diagnosticsList);
			client.publishDiagnostics(diagnostics);
		});
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		doCompilation(highPriorityProcessors, params.getTextDocument().getUri());
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		logv(() -> "Changing " + params);
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		doCompilation(highPriorityProcessors, params.getTextDocument().getUri());
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



}
