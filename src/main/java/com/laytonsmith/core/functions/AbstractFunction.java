package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.annotations.DocumentLink;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.SimpleDocumentation;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.snapins.PackagePermission;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import com.laytonsmith.tools.docgen.DocGenTemplates.Generator.GenerateException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public abstract class AbstractFunction implements Function {

	private boolean shouldProfile = true;

	protected AbstractFunction() {
		//If we have the noprofile annotation, cache that we don't want to profile.
		shouldProfile = !this.getClass().isAnnotationPresent(noprofile.class);
	}

	/**
	 * By default, we return CVoid.
	 *
	 * @param t
	 * @param env
	 * @param parent
	 * @param nodes
	 * @return
	 */
	@Override
	public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
		return CVoid.VOID;
	}

	/**
	 * By default, we return false, because most functions do not need this
	 *
	 * @return
	 */
	@Override
	public boolean useSpecialExec() {
		return false;
	}

	/**
	 * Most functions should show up in the normal documentation. However, if this function shouldn't show up in the
	 * documentation, it should mark itself with the @hide annotation.
	 *
	 * @return
	 */
	@Override
	public final boolean appearInDocumentation() {
		return this.getClass().getAnnotation(hide.class) == null;
	}

	/**
	 * Just return null by default. Most functions won't get to this anyways, since canOptimize is returning false.
	 *
	 * @param t
	 * @param env
	 * @param args
	 * @return
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 */
	public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
		return null;
	}

	/**
	 * It may be that a function can simply check for compile errors, but not optimize. In this case, it is appropriate
	 * to use this definition of optimizeDynamic, to return a value that will essentially make no changes, or in the
	 * case where it can optimize anyways, even if some values are undetermined at the moment.
	 *
	 * @param t
	 * @param env
	 * @param envs
	 * @param children
	 * @param fileOptions
	 * @return
	 * @throws com.laytonsmith.core.exceptions.ConfigCompileException
	 */
	public ParseTree optimizeDynamic(Target t, Environment env,
			Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children, FileOptions fileOptions)
			throws ConfigCompileException, ConfigRuntimeException {
		return null;
	}

	/**
	 * Most functions don't need the varlist.
	 *
	 * @param varList
	 */
	public void varList(IVariableList varList) {
	}

	/**
	 * Most functions want the atomic values, not the variable itself.
	 *
	 * @return
	 */
	@Override
	public boolean preResolveVariables() {
		return true;
	}

	@Override
	public ExampleScript[] examples() throws ConfigCompileException {
		return null;
	}

	@Override
	public boolean shouldProfile() {
		return shouldProfile;
	}

	@Override
	public LogLevel profileAt() {
		return LogLevel.VERBOSE;
	}

	@Override
	public String profileMessage(Mixed... args) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for(Mixed ccc : args) {
			if(!first) {
				b.append(", ");
			}
			first = false;
			if(ccc.isInstanceOf(CArray.TYPE)) {
				//Arrays take too long to toString, so we don't want to actually toString them here if
				//we don't need to.
				b.append("<arrayNotShown size:").append(((CArray) ccc).size()).append(">");
			} else if(ccc.isInstanceOf(CClosure.TYPE)) {
				//The toString of a closure is too long, so let's not output them either.
				b.append("<closureNotShown>");
			} else if(ccc.isInstanceOf(CString.TYPE)) {
				String val = ccc.val().replace("\\", "\\\\").replace("'", "\\'");
				int max = 1000;
				if(val.length() > max) {
					val = val.substring(0, max) + "... (" + (val.length() - max) + " more characters hidden)";
				}
				b.append("'").append(val).append("'");
			} else if(ccc instanceof IVariable) {
				b.append(((IVariable) ccc).getVariableName());
			} else {
				b.append(ccc.val());
			}
		}
		return "Executing function: " + this.getName() + "(" + b.toString() + ")";
	}

	/**
	 * Returns the documentation for this function that is provided as an external resource. This is useful for
	 * functions that have especially long or complex documentation, and adding it as a string directly in code would be
	 * cumbersome.
	 *
	 * @return
	 */
	protected String getBundledDocs() {
		try {
			return getBundledDocs(null);
		} catch (GenerateException ex) {
			// This condition is impossible, so we just ignore this case.
			return "";
		}
	}

	/**
	 * Returns the documentation for this function that is provided as an external resource. This is useful for
	 * functions that have especially long or complex documentation, and adding it as a string directly in code would be
	 * cumbersome. To facilitate dynamic docs, templates can be provided, which will be replaced for you.
	 *
	 * @param map
	 * @throws GenerateException If the templates cannot be properly parsed
	 * @return
	 */
	protected String getBundledDocs(Map<String, DocGenTemplates.Generator> map) throws GenerateException {
		String template = StreamUtils.GetString(AbstractFunction.class.getResourceAsStream("/functionDocs/"
				+ getName()));
		if(map == null) {
			map = new HashMap<>();
		}
		return DocGenTemplates.DoTemplateReplacement(template, map);
	}

	protected <T extends Enum<?> & SimpleDocumentation> String createEnumTable(Class<T> c) {
		StringBuilder b = new StringBuilder();
		MEnum me = c.getAnnotation(MEnum.class);
		String title;
		if(me == null) {
			title = c.getSimpleName();
		} else {
			title = me.value();
		}
		b.append("<br>'''").append(title).append("'''<br>\n");
		b.append("{|\n");
		b.append("|-\n! Name\n! Docs\n! Since\n");
		Enum[] elist = c.getEnumConstants();
		for(Enum e : elist) {
			SimpleDocumentation d = (SimpleDocumentation) e;
			b.append("|-\n")
					.append("| ").append(d.getName()).append("\n")
					.append("| ").append(d.docs()).append("\n")
					.append("| ").append(d.since()).append("\n");
		}
		b.append("|}\n");
		return b.toString();
	}

	@Override
	public String profileMessageS(List<ParseTree> args) {
		return "Executing function: " + this.getName() + "(<" + args.size() + " child"
				+ (args.size() == 1 ? "" : "ren") + " not shown>)";
	}

	@Override
	public PackagePermission getPermission() {
		return PackagePermission.NO_PERMISSIONS_NEEDED;
	}

	@Override
	public URL getSourceJar() {
		return ClassDiscovery.GetClassContainer(this.getClass());
	}

	private static final Class[] EMPTY_CLASS = new Class[0];

	/**
	 * Checks for the &#64;seealso annotation on this class, and returns the value listed there. This is to prevent
	 * subclasses from inheriting the list from super classes.
	 *
	 * @return
	 */
	@Override
	public final Class<? extends Documentation>[] seeAlso() {
		seealso see = this.getClass().getAnnotation(seealso.class);
		if(see == null) {
			return EMPTY_CLASS;
		} else {
			return see.value();
		}
	}

	@Override
	public final boolean isCore() {
		Class c = this.getClass();
		do {
			if(c.getAnnotation(core.class) != null) {
				return true;
			}
			c = c.getDeclaringClass();
		} while(c != null);
		return false;
	}

	public void link(Target t, List<ParseTree> children) throws ConfigCompileException {
		// Do nothing, as a default
	}

	@Override
	public int compareTo(Function o) {
		return this.getName().compareTo(o.getName());
	}

	public Set<ParseTree> getDocumentLinks(List<ParseTree> children) {
		Set<ParseTree> files = new HashSet<>();
		DocumentLink documentLink = this.getClass().getAnnotation(DocumentLink.class);
		if(documentLink != null && this instanceof DocumentLinkProvider) {
			for(int i : documentLink.value()) {
				if(children.size() >= i) {
					files.add(children.get(i));
				}
			}
		} else {
			throw new Error(this.getClass() + " is not tagged with the DocumentLink annotation, or does not"
					+ " implement DocumentLinkProvider, and this method cannot be called on it.");
		}
		return files;
	}

}
