package com.laytonsmith.tools.langserv;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.URIUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.ScriptProvider;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.RuntimeMode;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventList;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.functions.IncludeCache;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.tools.docgen.DocGen;
import static com.laytonsmith.tools.langserv.LangServ.LANGSERVLOGTAG;
import static com.laytonsmith.tools.langserv.LangServ.convertTargetToRange;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * This class contains the database for a given LangServ project. This contains runtime data which the operators in
 * LangServ operate on. Non-destructive actions can operate directly on the model, without having to do any further
 * compilation, and eventually, incremental and partial dirtying can potentially occur. Getting data while the tree is
 * dirty blocks, so all operations should use completable futures.
 */
public class LangServModel {

	private LanguageClient client;
	private final LangServ langServ;

	private volatile boolean isDirty = true;

	private Executor highPriorityProcessors;
	private Executor lowPriorityProcessors;

	/**
	 * Maps from URI to document text. If the document isn't in this map, it may be safely read from disk.
	 */
	private final Map<String, String> documents = new HashMap<>();

	private static List<CompletionItem> functionCompletionItems = null;
	private static List<CompletionItem> objectCompletionItems = null;
	private static List<CompletionItem> eventCompletionItems = null;
	private static List<CompletionItem> allCompletionItems = null;

	private final List<WorkspaceFolder> workspaceFolders = new ArrayList<>();

	private final Set<String> openDocuments = new HashSet<>();

	public LangServModel(LangServ server) {
		this.langServ = server;
	}

	public void setClient(LanguageClient client) {
		this.client = client;
	}

	public void setProcessors(Executor highPriorityExecutor, Executor lowPriorityExecutor) {
		this.highPriorityProcessors = highPriorityExecutor;
		this.lowPriorityProcessors = lowPriorityExecutor;
	}

	public List<CompletionItem> getFunctionCompletionItems() {
		return functionCompletionItems;
	}

	public List<CompletionItem> getObjectCompletionItems() {
		return objectCompletionItems;
	}

	public List<CompletionItem> getEventCompletionItems() {
		return eventCompletionItems;
	}

	public List<CompletionItem> getAllCompletionItems() {
		return allCompletionItems;
	}

	/**
	 * Called when other model changes would cause the tree to dirty. This version of the function dirties everything.
	 * The tree is NOT attempted to be rebuilt immediately after, so if the change that caused the tree to be dirtied is
	 * completed, then the caller should also call rebuildTree with a low priority executor so that it will be rebuilt
	 * in the background as soon as possible.
	 */
	private void dirtyTree() {
		isDirty = true;
	}

	/**
	 * Combines dirtying and then rebuilding the tree on the given executor. No callback is provided.
	 *
	 * @param executor
	 */
	private void dirtyAndRebuildTree(Executor executor) {
		dirtyTree();
		rebuildTree(executor, null);
	}

	/**
	 * Rebuilds the tree, only if necessary. If the tree is not dirty, calls the Runnable immediately.
	 *
	 * @param executor
	 * @param afterBuild
	 */
	private void rebuildTree(Executor executor, Runnable afterBuild) {
		// Set interrupt, as a new change has just come in that invalidates any build that
		// may be in progress already, then we'll immediately queue the new one up.
		interruptBuilding = true;
		executor.execute(() -> {
			// TODO
			if(isDirty) {
				synchronized(LangServModel.this) {
					if(isDirty) {
						syncRebuild();
					}
				}
			}
			if(afterBuild != null) {
				afterBuild.run();
			}
		});
	}

	private volatile boolean interruptBuilding = false;
	private final Map<String, ParseTree> parseTrees = new HashMap<>();
	private StaticAnalysis staticAnalysis;

	/**
	 * Usually this should only be called by rebuildTree. This method blocks until the rebuild is finished. If the tree
	 * is not dirty, this is a no-op.
	 */
	private void syncRebuild() {
		if(!isDirty) {
			return;
		}
		interruptBuilding = false;
		parseTrees.clear();
		// Calculate all auto includes first
		Set<File> autoIncludes = new HashSet<>();
		try {
			for(WorkspaceFolder folder : getWorkspaceFolders()) {
				URI uuri = new URI(folder.getUri());
				File ai = Paths.get(uuri).toFile();
				FileUtil.recursiveFind(ai, (r) -> {
					if(r.isFile() && r.getAbsolutePath().endsWith("auto_include.ms")) {
						String path = r.getAbsolutePath().replace("\\", "/");
						if(!path.contains(".disabled/")
								&& !path.contains(".library/")) {
							autoIncludes.add(r);
						}
					}
				});
			}
		} catch(IOException | URISyntaxException ex) {
			client.logMessage(new MessageParams(MessageType.Warning, ex.getMessage()));
		}
		
		IncludeCache includeCache = new IncludeCache();
		staticAnalysis = new StaticAnalysis(true);
		Environment env;
		try {
			// Cmdline mode disables things like security checks and whatnot.
			// These may be present in the runtime environment,
			// but it's not possible for us to tell that at this point.
			env = Static.GenerateStandaloneEnvironment(false, EnumSet.of(RuntimeMode.CMDLINE), includeCache, 
					staticAnalysis);
			// Make this configurable at some point. For now, however, we need this so we can get
			// correct handling on minecraft functions.
			env = env.cloneAndAdd(new CommandHelperEnvironment());
		} catch(IOException | DataSourceException | URISyntaxException | Profiles.InvalidProfileException ex) {
			throw new RuntimeException(ex);
		}
		CompilerEnvironment compilerEnv = env.getEnv(CompilerEnvironment.class);
		compilerEnv.setLogCompilerWarnings(false); // No one would see them
		GlobalEnv gEnv = env.getEnv(GlobalEnv.class);
		gEnv.SetScriptProvider((File file) -> getDocument(file.toURI().toString()));
		Set<ConfigCompileException> exceptions = new HashSet<>();
		
		for(WorkspaceFolder f : workspaceFolders) {
			if(interruptBuilding) {
				return;
			}
			File workspace = new File(f.getUri().replaceFirst("file://", ""));
			
			langServ.logv(() -> "Providing StaticAnalysis with auto includes: " + autoIncludes.toString());
			StaticAnalysis.setAndAnalyzeAutoIncludes(new ArrayList<>(autoIncludes), env, env.getEnvClasses(), exceptions);
			
			final Environment _env = env;
			
			try {
				FileUtil.recursiveFind(workspace, (File f1) -> {
					if(f1.isFile() && (f1.getName().endsWith(".ms") || f1.getName().endsWith(".msa"))) {
						parseTrees.put(URIUtils.canonicalize(f1.toURI()).toString(),
								doCompilation(f1.toURI().toString(), includeCache, staticAnalysis, _env, exceptions));
					}
				});
			} catch(IOException ex) {
				client.logMessage(new MessageParams(MessageType.Warning, ex.getMessage()));
			}
		}
		
		Map<String, List<Diagnostic>> diagnosticsLists = new HashMap<>();
		if(!exceptions.isEmpty()) {
			langServ.logi(() -> "Errors found, reporting " + exceptions.size() + " errors");
			for(ConfigCompileException e : exceptions) {
				Diagnostic d = new Diagnostic();
				if(e.getTarget().file() == null) {
					continue;
				}
				String uri = URIUtils.canonicalize(e.getTarget().file().toURI()).toString();
				d.setRange(convertTargetToRange(e.getTarget()));
				d.setSeverity(DiagnosticSeverity.Error);
				d.setMessage(e.getMessage());
				List<Diagnostic> diagnosticsList = diagnosticsLists.get(uri);
				if(diagnosticsList == null) {
					diagnosticsList = new ArrayList<>();
					diagnosticsLists.put(uri, diagnosticsList);
				}
				diagnosticsList.add(d);
			}
		}
		
		List<CompilerWarning> warnings = compilerEnv.getCompilerWarnings();
		if(!warnings.isEmpty()) {
			for(CompilerWarning c : warnings) {
				Diagnostic d = new Diagnostic();
				if(c.getTarget().file() == null) {
					continue;
				}
				String uri = URIUtils.canonicalize(c.getTarget().file().toURI()).toString();
				d.setRange(convertTargetToRange(c.getTarget()));
				d.setSeverity(LangServ.getSeverity(c));
				d.setMessage(c.getMessage());
				List<Diagnostic> diagnosticsList = diagnosticsLists.get(uri);
				if(diagnosticsList == null) {
					diagnosticsList = new ArrayList<>();
					diagnosticsLists.put(uri, diagnosticsList);
				}
				diagnosticsList.add(d);
			}
		}

		// We need to report to the client always, with an empty list, implying that all problems are fixed.
		for(String uri : parseTrees.keySet()) {
			List<Diagnostic> diagnosticsList = diagnosticsLists.get(uri);
			PublishDiagnosticsParams diagnostics
					= new PublishDiagnosticsParams(uri, diagnosticsList != null ? diagnosticsList : new ArrayList<>());
			client.publishDiagnostics(diagnostics);
		}
		
		isDirty = false;
	}

	/**
	 * Returns the StaticAnalysis object that was used to compile the given URI.NOTE! This will only return a valid
	 * value after compilation, so always call getParseTree before calling this method.
	 *
	 * @return
	 */
	public StaticAnalysis getStaticAnalysis() {
		return staticAnalysis;
	}

	/**
	 * Returns the ParseTree for the given URI. If the file is already compiled, this will return relatively quickly,
	 * otherwise it will wait for the file to be compiled and then return it. Note that in some cases, this will return
	 * null, which indicates that either this file shouldn't or couldn't be compiled. All callers must ensure that null
	 * returns are handled correctly.
	 *
	 * @param future
	 * @param uri
	 */
	public void getParseTree(CompletableFuture<ParseTree> future, String uri) {
		Runnable getter = () -> {
			try {
				future.complete(parseTrees.get(URIUtils.canonicalize(new URI(uri)).toString()));
			} catch(URISyntaxException ex) {
				future.completeExceptionally(ex);
			}
		};
		if(!isDirty) {
			getter.run();
		} else {
			rebuildTree(highPriorityProcessors, getter);
		}
	}

	private static boolean onceEverStartupCompleted = false;

	@SuppressWarnings("UseSpecificCatch")
	public void startup() {
		/*
			This method should include only things that literally never change, unless the jar is recompiled.
			This is only done once, and the data is stored in static fields.
		 */
		if(!onceEverStartupCompleted) {
			synchronized(LangServModel.class) {
				if(!onceEverStartupCompleted) {
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
								} catch(IllegalArgumentException ex) {
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
							langServ.logv("Function completion list completed. (" + list.size() + ")");
						}
						{
							List<CompletionItem> list = new ArrayList<>();
							for(Event e : EventList.GetEvents()) {
								final DocGen.EventDocInfo edi;
								try {
									edi = new DocGen.EventDocInfo(e, e.docs(), e.getName(), DocGen.MarkupType.HTML);
								} catch(IllegalArgumentException ex) {
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
							langServ.logv("Event completion list completed. (" + list.size() + ")");
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
								} catch(Throwable ex) {
									// Skip it.
								}
							}
							objectCompletionItems = list;
							langServ.logv("Object completion list completed. (" + list.size() + ")");
						}
						allCompletionItems = new ArrayList<>();
						allCompletionItems.addAll(functionCompletionItems);
						allCompletionItems.addAll(eventCompletionItems);
						allCompletionItems.addAll(objectCompletionItems);
						langServ.logd("Completion list generated.");
					});
					onceEverStartupCompleted = true;
				}
			}
		}
	}

	public void addWorkspace(List<WorkspaceFolder> workspace) {
		this.workspaceFolders.addAll(workspace);
		dirtyTree();
		rebuildTree(lowPriorityProcessors, null);
	}

	public void removeWorkspace(List<WorkspaceFolder> workspace) {
		this.workspaceFolders.removeAll(workspace);
		dirtyTree();
		rebuildTree(lowPriorityProcessors, null);
	}

	public List<WorkspaceFolder> getWorkspaceFolders() {
		return new ArrayList<>(workspaceFolders);
	}

	//<editor-fold defaultstate="collapsed" desc="DocumentManagement">
	/**
	 * Returns the document text either from the document cache, if the client is managing the document, or from the
	 * file system if it isn't.
	 *
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
		} catch(URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
		return new ScriptProvider.FileSystemScriptProvider().getScript(f);
	}

	public void didOpen(DidOpenTextDocumentParams params) {
		// The document open notification is sent from the client to the server to signal newly opened text documents.
		// The document’s truth is now managed by the client and the server must not try to read the document’s truth
		// using the document’s Uri.
		langServ.logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		String uri = params.getTextDocument().getUri();
		documents.put(uri, params.getTextDocument().getText());
		openDocuments.add(uri);
		dirtyAndRebuildTree(highPriorityProcessors);
	}

	public void didChange(DidChangeTextDocumentParams params) {
		langServ.logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		langServ.logv(() -> "Changing " + params);
		String uri = params.getTextDocument().getUri();
		// If the processing mode is changed to incremental, this logic needs modification
//		String text = documents.get(uri);
		if(params.getContentChanges().size() > 1) {
			langServ.logw("Unexpected size from didChange event.");
		}
		for(TextDocumentContentChangeEvent change : params.getContentChanges()) {
			String newText = change.getText();
			documents.put(uri, newText);
		}
//		dirtyAndRebuildTree(lowPriorityProcessors);
	}

	public void didClose(DidCloseTextDocumentParams params) {
		// The document close notification is sent from the client to the server when the document got closed in the
		// client. The document’s truth now exists where the document’s Uri points to (e.g. if the document’s Uri is
		// a file Uri the truth now exists on disk).
		langServ.logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		String uri = params.getTextDocument().getUri();
		documents.remove(uri);
		openDocuments.remove(uri);
	}

	public void didSave(DidSaveTextDocumentParams params) {
		langServ.logv(this.getClass().getName() + "." + StackTraceUtils.currentMethod() + " called");
		dirtyAndRebuildTree(lowPriorityProcessors);
	}

	//</editor-fold>
	/**
	 * Compiles the file. Null is returned if the file couldn't (or shouldn't) be compiled, and any errors will have
	 * already been reported to the client.
	 *
	 * @param uri The URI of the file to compile.
	 */
	@SuppressWarnings("UseSpecificCatch")
	private ParseTree doCompilation(final String uri, IncludeCache includeCache, StaticAnalysis staticAnalysis,
			Environment env, Set<ConfigCompileException> exceptions) {
		try {

			String code;
			// Eventually we want to rework this so that this is available
			Set<Class<? extends Environment.EnvironmentImpl>> envs = new HashSet<>();
			for(Class<Environment.EnvironmentImpl> c
					: ClassDiscovery.getDefaultInstance().loadClassesThatExtend(Environment.EnvironmentImpl.class)) {
				envs.add(c);
			}
			

			File f;
			{
				URI uuri = new URI(uri);
				if("untitled".equals(uuri.getScheme())) {
					// For open files that aren't saved to disk, the client sends something like "untitled:untitled-1",
					// which isn't a valid file provider, so we can't call Paths.get on it. Instead, we just mock
					// the name here. We also need to provide getAbsoluteFile, so that the below call to getParentFile
					// will work.
					f = new File(uuri.getSchemeSpecificPart()).getAbsoluteFile();
				} else {
					f = Paths.get(uuri).toFile();
				}
			}

			if(f.getAbsolutePath().replace("\\", "/").contains(".disabled/")) {
				// Don't compile files in disabled folders at all.
				return null;
			}

			env.getEnv(GlobalEnv.class).SetRootFolder(f.getParentFile());
			TokenStream tokens = null;
			ParseTree tree = null;
			try {
				ParseTree fTree;
				langServ.logd(() -> "Compiling " + f);
				code = getDocument(uri);
				if(f.getName().endsWith(".msa")) {
					tokens = MethodScriptCompiler.lex(code, env, f, false);
					fTree = new ParseTree(null);
					final Environment finalEnv = env;
					MethodScriptCompiler.preprocess(tokens, envs).forEach((script) -> {
						try {
							script.compile(finalEnv);
						} catch(ConfigCompileException ex) {
							exceptions.add(ex);
						} catch(ConfigCompileGroupException ex) {
							exceptions.addAll(ex.getList());
						}
						script.getTrees().forEach(r -> fTree.addChild(r));
					});
				} else {
					// Actually, for untitled files, this may not be a correct default. However, there's no
					// other good way of determining that, so let's just assume it's pure methodscript.
					tokens = MethodScriptCompiler.lex(code, env, f, true);
					fTree = MethodScriptCompiler.compile(tokens, env, envs, staticAnalysis);
				}
				tree = fTree;
			} catch(ConfigCompileException e) {
				exceptions.add(e);
			} catch(ConfigCompileGroupException e) {
				exceptions.addAll(e.getList());
			} catch(Throwable e) {
				// Just skip this, we can't do much here.
				langServ.loge(() -> StackTraceUtils.GetStacktrace(e));
			}
			
			
			return tree;
		} catch(Throwable t) {
			client.logMessage(new MessageParams(MessageType.Error, t.getMessage() + "\n"
					+ StackTraceUtils.GetStacktrace(t)));
		}
		return null;
	}

	/**
	 * This function compiles MSA files and returns the Script objects.Note that this is not intended for use to get
	 * compile errors.If there are compile errors, it will not call the future.This is also the case if the uri does not
	 * point to a msa file.Any other errors will result in the future being called.
	 *
	 * @param future
	 * @param threadPool
	 * @param uri
	 * @param withDelay
	 */
	@SuppressWarnings("UnnecessaryReturnStatement")
	public void doPreprocess(CompletableFuture<List<Script>> future, Executor threadPool, final String uri,
			boolean withDelay) {
		threadPool.execute(() -> {
			URI uuri;
			String code;
			try {
				uuri = new URI(uri);
				code = getDocument(uri);
			} catch(URISyntaxException | IOException ex) {
				return;
			}
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
			if(!f.getName().endsWith(".msa")) {
				return;
			}

			langServ.logd(() -> "Compiling " + f);

			Environment env;
			try {
				// Cmdline mode disables things like security checks and whatnot.
				// These may be present in the runtime environment,
				// but it's not possible for us to tell that at this point.
				env = Static.GenerateStandaloneEnvironment(false, EnumSet.of(RuntimeMode.CMDLINE));
				// Make this configurable at some point. For now, however, we need this so we can get
				// correct handling on minecraft functions.
				env = env.cloneAndAdd(new CommandHelperEnvironment());
			} catch(IOException | DataSourceException | URISyntaxException | Profiles.InvalidProfileException ex) {
				throw new RuntimeException(ex);
			}
			CompilerEnvironment compilerEnv = env.getEnv(CompilerEnvironment.class);
			compilerEnv.setLogCompilerWarnings(false); // No one would see them
			GlobalEnv gEnv = env.getEnv(GlobalEnv.class);
			gEnv.SetRootFolder(f.getParentFile());
			// Eventually we want to rework this so that this is available
			Set<Class<? extends Environment.EnvironmentImpl>> envs = new HashSet<>();
			for(Class<Environment.EnvironmentImpl> c
					: ClassDiscovery.getDefaultInstance().loadClassesThatExtend(Environment.EnvironmentImpl.class)) {
				envs.add(c);
			}
			try {
				TokenStream tokens = MethodScriptCompiler.lex(code, env, f, false);
				List<Script> scripts = MethodScriptCompiler.preprocess(tokens, envs);
				future.complete(scripts);
			} catch(ConfigCompileException ex) {
				return;
			}
		});
	}
	
	public static ParseTree findToken(ParseTree start, Target target) {
		ParseTree bestCandidate = null;
		for(ParseTree node : start.getAllNodes()) {
			if(node.getTarget().equals(target)) {
				if(node.isSyntheticNode()) {
					bestCandidate = node;
				} else {
					return node;
				}
			}
		}
		return bestCandidate;
	}
	
	public static ParseTree findToken(ParseTree start, Position position) {
		// TODO: Should be able to convert this to a O(log n)ish algo if we're smarter about it. Also, should
		// probably add original token length to the Target, so we can be smarter about that too.
		ParseTree bestCandidate = null;
		for(ParseTree node : start.getAllNodes()) {
			Target t = node.getTarget();
			// Both line numbers and column numbers are 1 indexed in MethodScript, since they are usually
			// human readable fields, and text editors always start at line one. However, the protocol is
			// zero based, so we add 1 to all those numbers.
			if(t.line() != position.getLine() + 1) {
				continue;
			}
			if(position.getCharacter() + 1 >= t.col()
					&& position.getCharacter() + 1 <= (t.col() + node.getData().val().length())) {
				if(node.isSyntheticNode()) {
					// This might be the best candidate, but maybe there is a better choice after us
					bestCandidate = node;
				} else {
					// This definitely is the best candidate.
					return node;
				}
			}
		}
		return bestCandidate;
	}
}
