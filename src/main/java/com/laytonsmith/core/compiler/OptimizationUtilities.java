
package com.laytonsmith.core.compiler;

import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.List;

/**
 *
 * @author Layton
 */
public class OptimizationUtilities {

	/**
	 * Goes one deep, and pulls up like children. This is only for use where the
	 * same function being chained doesn't make sense, for instance, in the case
	 * of adding, add(2, add(2, add(2, 2))) should just turn into add(2, 2, 2,
	 * 2).
	 *
	 * @param children
	 * @param functionName
	 */
	public static void pullUpLikeFunctions(List<ParseTree> children, String functionName) {
		int size = children.size() - 1;
		for (int i = size; i >= 0; i--) {
			ParseTree tree = children.get(i);
			if (tree.getData() instanceof CFunction && tree.getData().val().equals(functionName)) {
				//We can pull it up. Just go through the children and insert them here. Remove the node
				//that was this child though.
				children.remove(i);
				for (int j = tree.getChildren().size() - 1; j >= 0; j--) {
					children.add(i, tree.getChildAt(j));
				}
			}
		}
	}
	
	/**
	 * This function takes a string script, and returns an equivalent, optimized script.
	 * @param script
	 * @return
	 * @throws ConfigCompileException 
	 */
	public static String optimize(String script, Environment env) throws ConfigCompileException{
        ParseTree tree = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true), env);
        StringBuilder b = new StringBuilder();
        //The root always contains null.
		if(tree.getData() == null){
			for(ParseTree child : tree.getChildren()){
				b.append(optimize0(child));
			}        
		} else {
			b.append(optimize0(tree));
		}
        return b.toString();
    }
    private static String optimize0(ParseTree node){
        if(node.getData() instanceof CFunction){
            StringBuilder b = new StringBuilder();
            boolean first = true;
            b.append(((CFunction)node.getData()).val()).append("(");
            for(ParseTree child : node.getChildren()){
                if(!first){
                    b.append(",");
                }
                first = false;
                b.append(optimize0(child));
            }
            b.append(")");
            return b.toString();
        } else if(node.getData() instanceof CString){
            //strings
            return new StringBuilder().append("'").append(node.getData().val().replaceAll("\t", "\\t").replaceAll("\n", "\\n").replace("\\", "\\\\").replace("'", "\\'")).append("'").toString();
        } else if(node.getData() instanceof IVariable){
            return ((IVariable)node.getData()).getName();
		} else if(node.getData() instanceof Variable){
			return ((Variable)node.getData()).getName();
        } else {
            //static
            return node.getData().toString();
        }
    }
}
