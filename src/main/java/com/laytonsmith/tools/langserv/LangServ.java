package com.laytonsmith.tools.langserv;

import com.laytonsmith.PureUtilities.ArgumentParser;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.MapBuilder;
import com.laytonsmith.PureUtilities.SmartComment;
import com.laytonsmith.core.AbstractCommandLineTool;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.compiler.analysis.Declaration;
import com.laytonsmith.core.compiler.analysis.Namespace;
import com.laytonsmith.core.compiler.analysis.ParamDeclaration;
import com.laytonsmith.core.compiler.analysis.ProcDeclaration;
import com.laytonsmith.core.compiler.analysis.Scope;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.DocumentLinkProvider;
import com.laytonsmith.core.functions.DocumentSymbolProvider;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.tool;
import com.laytonsmith.persistence.DataSourceException;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DeclarationParams;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkOptions;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TypeDefinitionParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceFoldersOptions;
import org.eclipse.lsp4j.WorkspaceServerCapabilities;
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
				langserv.connect(client);
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
			type = switch(level) {
				case DEBUG -> MessageType.Log;
				case INFO -> MessageType.Info;
				case WARNING -> MessageType.Warning;
				case ERROR -> MessageType.Error;
				default -> MessageType.Log;
			};

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
	private LangServModel model;

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


	@Override
	@SuppressWarnings("UseSpecificCatch")
	public void connect(LanguageClient client) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		this.client = client;

		if(this.model == null) {
			this.model = new LangServModel(this);
		}
		this.model.setClient(client);
		this.model.setProcessors(highPriorityProcessors, lowPriorityProcessors);

		this.model.startup();
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

	private final Map<String, CommandProvider> commandProviders = new HashMap<>();

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

		WorkspaceServerCapabilities wsc = new WorkspaceServerCapabilities();
		WorkspaceFoldersOptions wfo = new WorkspaceFoldersOptions();
		wfo.setSupported(true);
		wfo.setChangeNotifications(true);
		wsc.setWorkspaceFolders(wfo);
		sc.setWorkspace(wsc);

		sc.setDocumentSymbolProvider(true);
		sc.setDeclarationProvider(true);
		sc.setHoverProvider(true);
//		sc.setTypeDefinitionProvider(true);

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
		model.addWorkspace(params.getWorkspaceFolders());
		return cf;
	}

	@Override
	public void initialized(InitializedParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		client.workspaceFolders().thenAccept((List<WorkspaceFolder> t) -> {

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

	public static DiagnosticSeverity getSeverity(CompilerWarning warning) {
		if(warning.getSuppressCategory() == null) {
			return DiagnosticSeverity.Warning;
		}
		switch(warning.getSuppressCategory().getSeverityLevel()) {
			case HIGH -> {
				return DiagnosticSeverity.Warning;
			}
			case MEDIUM -> {
				return DiagnosticSeverity.Information;
			}
			case LOW -> {
				return DiagnosticSeverity.Hint;
			}
		}
		throw new Error("Unaccounted for case: " + warning.getSuppressCategory());
	}



	@Override
	public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
		model.removeWorkspace(params.getEvent().getRemoved());
		model.addWorkspace(params.getEvent().getAdded());
	}



	//<editor-fold defaultstate="collapsed" desc="DocumentManagement">

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		model.didOpen(params);
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		model.didChange(params);
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		model.didClose(params);
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		model.didSave(params);
	}

	//</editor-fold>

	public static Range convertTargetToRange(Target t) {
		int tokenLength = t.length();
		if(tokenLength < 1) {
			// Something went wrong, but we always want an error to show up, so set this here
			tokenLength = 1;
		}
		
		Position start = new Position(t.line() - 1, t.col() - 1);
		Position end = new Position(t.line() - 1, t.col() + tokenLength - 1);
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

	public static Range convertTargetToRange(ParseTree node) {
		return convertTargetToRange(node.getTarget());
	}

	public void convertPositionToParseTree(CompletableFuture<ParseTree> future, Executor threadPool, String uri, Position position) {
		CompletableFuture<ParseTree> privateFuture = new CompletableFuture<>();
		model.getParseTree(privateFuture, uri);
		privateFuture.thenAccept((ParseTree t) -> {
			ParseTree result = LangServModel.findToken(t, position);
			future.complete(result);
		});
	}

	public static Location convertTargetToLocation(Target t) {
		Range range = convertTargetToRange(t);
		Location location = new Location(t.file().toURI().toString(), range);
		return location;
	}
	
	public static Location convertTargetToLocation(ParseTree node) {
		return convertTargetToLocation(node.getTarget());
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		logv(() -> String.format("Completion request sent: %s", position));
		CompletableFuture<Either<List<CompletionItem>, CompletionList>> result = new CompletableFuture<>();
		highPriorityProcessors.execute(() -> {
			result.complete(Either.forLeft(model.getFunctionCompletionItems()));
			logv(() -> "Completion list returned with " + model.getFunctionCompletionItems().size() + " items");
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
		model.getParseTree(future, uri);
		future.thenAccept((tree) -> {
			if(tree == null) {
				return;
			}
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
						if(f instanceof DocumentLinkProvider documentLinkProvider) {
							logv(() -> "Found DocumentLinkProvider " + f.getName());
							for(ParseTree link : documentLinkProvider.getDocumentLinks(node.getChildren())) {
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
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> declaration(DeclarationParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		logv(() -> params.toString());
		CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> result = new CompletableFuture<>();
		CompletableFuture<ParseTree> future = new CompletableFuture<>();
		String uri = params.getTextDocument().getUri();
		convertPositionToParseTree(future, highPriorityProcessors, uri, params.getPosition());
		future.thenAccept((ParseTree t) -> {
			if(t == null) {
				result.cancel(true);
				return;
			}
			if(t.getData() instanceof CFunction cf) {
				if(cf.hasProcedure()) {
					String procName = cf.val();
					StaticAnalysis sa = model.getStaticAnalysis();
					List<Location> locations = new ArrayList<>();
					Scope scope = sa.getTermScope(t);
					if(scope != null) {
						Collection<Declaration> decls = scope.getDeclarations(Namespace.PROCEDURE, procName);
						for(Declaration decl : decls) {
							locations.add(convertTargetToLocation(decl.getTarget()));
						}
						result.complete(Either.forLeft(locations));
						return;
					}
				}
			}
			result.cancel(true);
		});
		return result;
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> typeDefinition(TypeDefinitionParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		logv(() -> params.toString());
		CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> result = new CompletableFuture<>();
		result.cancel(true);
		return result;
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams params) {
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		logv(() -> params.toString());

		CompletableFuture<Hover> result = new CompletableFuture<>();
		CompletableFuture<ParseTree> findParseTree = new CompletableFuture<>();
		String uri = params.getTextDocument().getUri();
		convertPositionToParseTree(findParseTree, highPriorityProcessors, uri,
				params.getPosition());

		findParseTree.thenAccept((ParseTree t) -> {
			if(t == null) {
				result.cancel(true);
				return;
			}
			Hover hover = null;
			StaticAnalysis sa = model.getStaticAnalysis();
			if(sa == null) {
				// We can't find declarations of anything.
				result.cancel(true);
				return;
			}
			if(t.getData() instanceof CFunction cf) {
				if(cf.hasProcedure()) {
					Scope scope = sa.getTermScope(t);
					if(scope == null) {
						result.cancel(true);
						return;
					}
					Collection<Declaration> col = scope.getReachableDeclarations(Namespace.PROCEDURE, cf.val());
					if(!col.isEmpty()) {
						ProcDeclaration decl = (ProcDeclaration) new ArrayList<>(col).get(0);
						SmartComment sc = decl.getNodeModifiers().getComment();
						if(sc != null) {
							sc = doReplacements(sc);
						}
						String content = "## ";
						content += decl.getType().getSimpleName() + " " + decl.getIdentifier() + "(";
						boolean first = true;
						for(ParamDeclaration pDecl : decl.getParameters()) {
							if(!first) {
								content += ", ";
							}
							content += pDecl.getType().getSimpleName() + " " + pDecl.getIdentifier();
							first = false;
						}
						content += ")\n\n";
						if(sc != null) {
							content += sc.getBody() + "\n\n";
							List<String> parameters = sc.getAnnotations("param");
							if(!decl.getParameters().isEmpty()) {
								content += "### Parameters\n";
								for(ParamDeclaration pDecl : decl.getParameters()) {
									content += " - " + pDecl.getType().getSimpleName() + " " + pDecl.getIdentifier();
									ParseTree defaultValue = pDecl.getDefaultValue();
									if(defaultValue != null) {
										if(defaultValue.isConst() && defaultValue.getData() != CNull.UNDEFINED) {
											Mixed data = defaultValue.getData();
											content += " [default ";
											if(data instanceof CString str) {
												content += str.getQuote();
											} else {
												content += data.val();
											}
											content += "]";
										}
									}
									for(String paramDocs : parameters) {
										String[] split = paramDocs.split(" ", 2);
										if(split.length > 1) {
											if(pDecl.getIdentifier().replace("@", "").equals(split[0])) {
												content += " - " + split[1].replace("\r", "").replace("\n", "");
												break;
											}
										}
									}
									content += "\n";
								}
							}
							content += "\n";
							if(!sc.getAnnotations("returns").isEmpty()) {
								content += "### Returns\n" + sc.getAnnotations("returns").get(0) + "\n\n";
							}
							
							if(!sc.getAnnotations("seeAlso").isEmpty()) {
								content += "### See Also\n";
								for(String seeAlso : sc.getAnnotations("seeAlso")) {
									if(content.matches("https?://.*")) {
										content += " - " + convertURLToLink(seeAlso);
									} else {
										content += " - " + seeAlso;
									}
									content += "\n";
								}
								content += "\n";
							}
						}
						
						MarkupContent mContent = new MarkupContent("markdown", content);
						hover = new Hover();
						hover.setContents(mContent);
					}
				}
			}
			if(hover == null) {
				result.cancel(true);
			} else {
				result.complete(hover);
			}
		});

		return result;
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
		String uri = params.getTextDocument().getUri();
		logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		logv(() -> "Requested symbols for " + uri);

		CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> result = new CompletableFuture<>();
		List<Either<SymbolInformation, DocumentSymbol>> links = new ArrayList<>();

		// TODO: Sort by kind/link name

		// Different handling for msa and ms. msa returns aliases, ms returns procs and things.
		if(uri.endsWith(".msa")) {
			CompletableFuture<List<Script>> future = new CompletableFuture<>();
			model.doPreprocess(future, lowPriorityProcessors, uri, false);
			future.thenAccept((scripts) -> {
				for(Script script : scripts) {
					String link = script.getSignatureWithoutLabel();
					link = link.replace("[ ", "[").replace(" ]", "]");
					SymbolInformation docSymbol = new SymbolInformation(link, SymbolKind.Method,
							convertTargetToLocation(script.getTarget()));
					links.add(Either.forLeft(docSymbol));
				}
				result.complete(links);
			});
		} else {
			CompletableFuture<ParseTree> future = new CompletableFuture<>();
			model.getParseTree(future, uri);
			future.thenAccept((tree) -> {
				if(tree == null) {
					return;
				}
				tree.getAllNodes().forEach(node -> {
					if(node.getData() instanceof CFunction && ((CFunction) (node.getData())).hasFunction()) {
						try {
							Function f = ((CFunction) (node.getData())).getFunction();
							if(f instanceof DocumentSymbolProvider documentSymbolProvider) {
								logv(() -> "Found DocumentSymbolProvider " + f.getName());
								String link = documentSymbolProvider.symbolDisplayName(node.getChildren());
								if(link != null) {
									SymbolInformation docSymbol = new SymbolInformation(link,
											documentSymbolProvider.getSymbolKind(),
											convertTargetToLocation(node));
									links.add(Either.forLeft(docSymbol));
								}
							}
						} catch (ConfigCompileException ex) {
							// Ignore this. This can be caused errors, but the point is, it's not a
							// valid symbol right now.
						}
					}
				});
				result.complete(links);
			});
		}
		return result;
	}

	@SuppressWarnings("Convert2Lambda")
	public static SmartComment doReplacements(SmartComment sc) {
		return new SmartComment(sc, MapBuilder.empty(String.class, SmartComment.Replacement.class)
			.set("code", new SmartComment.Replacement() {
				@Override
				public String replace(String data) {
					return "`" + data + "`";
				}
			})
			.set("url", new SmartComment.Replacement() {
				@Override
				public String replace(String data) {
					return convertURLToLink(data);
				}
			})
			.build());
	}

	private static String convertURLToLink(String annotationText) {
		String[] split = annotationText.split(" ", 2);
		String url;
		String display;
		switch(split.length) {
			case 0 -> {
				return "";
			}
			case 1 -> url = display = split[0];
			default -> {
					url = split[0];
					display = split[1];
			}
		}
		return "[" + display + "](" + url + ")";
	}

	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
		return commandProviders.get(params.getCommand()).execute(client, params);
	}
}
