package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StreamUtils;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.snapins.PackagePermission;
import com.laytonsmith.tools.docgen.DocGenTemplates;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author layton
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
	 * @param nodes
	 * @return
	 */
	public Construct execs(Target t, Environment env, Script parent, ParseTree... nodes) {
		return new CVoid(t);
	}

	/**
	 * By default, we return false, because most functions do not need this
	 *
	 * @return
	 */
	public boolean useSpecialExec() {
		return false;
	}

	/**
	 * Most functions should show up in the normal documentation.
	 *
	 * @return
	 */
	public boolean appearInDocumentation() {
		return true;
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

	public ExampleScript[] examples() throws ConfigCompileException {
		return null;
	}

	public boolean shouldProfile() {
		return shouldProfile;
	}

	public LogLevel profileAt() {
		return LogLevel.VERBOSE;
	}

	public String profileMessage(Construct... args) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (Construct ccc : args) {
			if (!first) {
				b.append(", ");
			}
			first = false;
			if (ccc instanceof CArray) {
				//Arrays take too long to toString, so we don't want to actually toString them here if
				//we don't need to.
				b.append("<arrayNotShown>");
			} else if (ccc instanceof CClosure) {
				//The toString of a closure is too long, so let's not output them either.
				b.append("<closureNotShown>");
			} else if (ccc instanceof CString) {
				b.append("'").append(ccc.val().replace("\\", "\\\\").replace("'", "\\'")).append("'");
			} else if (ccc instanceof IVariable) {
				b.append(((IVariable) ccc).getName());
			} else {
				b.append(ccc.val());
			}
		}
		return "Executing function: " + this.getName() + "(" + b.toString() + ")";
	}
	
	/**
	 * Returns the documentation for this function that is provided as an external resource.
	 * This is useful for functions that have especially long or complex documentation, and adding
	 * it as a string directly in code would be cumbersome.
	 * @return 
	 */
	protected String getBundledDocs(){
		return getBundledDocs(null);
	}
	
	/**
	 * Returns the documentation for this function that is provided as an external resource.
	 * This is useful for functions that have especially long or complex documentation, and adding
	 * it as a string directly in code would be cumbersome. To facilitate dynamic docs, templates
	 * can be provided, which will be replaced for you.
	 * @param map
	 * @return 
	 */
	protected String getBundledDocs(Map<String, DocGenTemplates.Generator> map){
		String template = StreamUtils.GetString(AbstractFunction.class.getResourceAsStream("/functionDocs/" + getName()));
		if(map == null){
			map = new HashMap<String, DocGenTemplates.Generator>();
		}
		return DocGenTemplates.doTemplateReplacement(template, map);
	}

	public String profileMessageS(List<ParseTree> args) {
		return "Executing function: " + this.getName() + "(<" + args.size() + " child"
				+ (args.size() == 1 ? "" : "ren") + " not shown>)";
	}

	public PackagePermission getPermission() {
		return PackagePermission.NO_PERMISSIONS_NEEDED;
	}
	
		/**
	 * This is called during compile time, if canOptimize returns true. It
	 * should return the construct to replace this function, if possible. If
	 * only type checking is being done, it may return null, in which case no
	 * changes will be made to the parse tree. During the optimization, it is
	 * also possible for a function to throw a ConfigCompileException. It may
	 * also throw a ConfigRuntimeException, which will be caught, and changed
	 * into a ConfigCompileException.
	 *
	 * @param t
	 * @param args
	 * @return
	 */
	public Construct optimize(Target t, Environment env, Construct... args) throws ConfigRuntimeException, ConfigCompileException{
		return null;
	}

	/**
	 * If the function indicates it can optimize dynamic values, this method is
	 * called. It may also throw a compile exception should the parameters be
	 * unacceptable. It may return null if no changes should be made (which is
	 * likely the default).
	 *
	 * @param t
	 * @param children
	 * @return
	 */
	public ParseTree optimizeDynamic(Target t, Environment env, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException{
		return null;
	}
	
	
}
