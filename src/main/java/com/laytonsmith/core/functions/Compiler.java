package com.laytonsmith.core.functions;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.List;

/**
 *
 * @author layton
 */
public class Compiler {
    public static String docs(){
        return "Compiler internal functions should be declared here. If you're reading this from anywhere"
                + " but the source code, there's a bug, because these functions shouldn't be public or used"
                + " in a script.";
    }
    
    @api
    public static class p extends AbstractFunction {

        public String getName() {
            return "p";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public String docs() {
            return "mixed {c...} Used internally by the compiler. You shouldn't use it.";
        }

        public Exceptions.ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_1_2;
        }

        public Boolean runAsync() {
            return null;
        }
       

        @Override
        public boolean useSpecialExec() {
            return true;
        }

        @Override
        public Construct execs(Target t, Env env, Script parent, GenericTreeNode<Construct>... nodes) {
            if(nodes.length == 1){
                return parent.eval(nodes[0], env);
            } else {
                return new __autoconcat__().execs(t, env, parent, nodes);
            }
        }
        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            return new CVoid(t);
        }

        @Override
        public boolean appearInDocumentation() {
            return false;
        }                
    }
    
    @api public static class centry extends AbstractFunction{

        public String getName() {
            return "centry";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "CEntry {label, content} Dynamically creates a CEntry. This is used internally by the "
                    + "compiler.";
        }

        public Exceptions.ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }
        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CEntry(args[0], args[1], t);
        }

        public CHVersion since() {
            return CHVersion.V3_3_1;
        }

        @Override
        public boolean appearInDocumentation() {
            return false;
        }

        @Override
        public boolean canOptimize() {
            return true;
        }

        @Override
        public Construct optimize(Target t, Construct... args) throws ConfigCompileException {
            return exec(t, null, args);
        }                
    }
    
    
    @api
    public static class __autoconcat__ extends AbstractFunction {

        public String getName() {
            return "__autoconcat__";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            throw new Error("Should not have gotten here");
        }

        public String docs() {
            return "string {var1, [var2...]} This function should only be used by the compiler, behavior"
                    + " may be undefined if it is used in code.";
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V0_0_0;
        }

        public Boolean runAsync() {
            return null;
        }

        @Override
        public boolean canOptimizeDynamic() {
            return true;
        }

        @Override
        public GenericTreeNode<Construct> optimizeDynamic(Target t, List<GenericTreeNode<Construct>> list) throws ConfigCompileException {
            return optimizeSpecial(t, list, true);
        }

        /**
         * __autoconcat__ has special optimization techniques needed, since it's
         * really a part of the compiler itself, and not so much a function. It
         * being a function is merely a convenience, so we can defer processing
         * until after parsing. While it is tightly coupled with the compiler,
         * this is ok, since it's really a compiler mechanism more than a
         * function.
         *
         * @param t
         * @param list
         * @return
         */
        public GenericTreeNode<Construct> optimizeSpecial(Target t, List<GenericTreeNode<Construct>> list, boolean returnSConcat) throws ConfigCompileException {
            //If any of our nodes are CSymbols, we have different behavior
            boolean inSymbolMode = false; //catching this can save Xn

            //postfix
            for (int i = 0; i < list.size(); i++) {
                GenericTreeNode<Construct> node = list.get(i);
                if (node.data instanceof CSymbol) {
                    inSymbolMode = true;
                }
                if (node.data instanceof CSymbol && ( (CSymbol) node.data ).isPostfix()) {
                    if (i - 1 >= 0 && list.get(i - 1).data instanceof IVariable) {
                        CSymbol sy = (CSymbol) node.data;
                        GenericTreeNode<Construct> conversion;
                        if (sy.val().equals("++")) {
                            conversion = new GenericTreeNode<Construct>(new CFunction("postinc", t));
                        } else {
                            conversion = new GenericTreeNode<Construct>(new CFunction("postdec", t));
                        }
                        conversion.addChild(list.get(i - 1));
                        list.set(i - 1, conversion);
                        list.remove(i);
                        i--;
                    }
                }
            }
            if (inSymbolMode) {
                try {
                    //look for unary operators
                    for (int i = 0; i < list.size() - 1; i++) {
                        GenericTreeNode<Construct> node = list.get(i);
                        if (node.data instanceof CSymbol && ( (CSymbol) node.data ).isUnary()) {
                            GenericTreeNode<Construct> conversion;
                            if (node.data.val().equals("-") || node.data.val().equals("+")) {
                                //These are special, because if the values to the left isn't a symbol,
                                //it's not unary
                                if ((i == 0 || list.get(i - 1).data instanceof CSymbol)
                                        && !(list.get(i + 1).data instanceof CSymbol)) {
                                    if (node.data.val().equals("-")) {
                                        //We have to negate it
                                        conversion = new GenericTreeNode<Construct>(new CFunction("neg", t));
                                    } else {
                                        conversion = new GenericTreeNode<Construct>(new CFunction("p", t));
                                    }
                                } else {
                                    continue;
                                }
                            } else {
                                conversion = new GenericTreeNode<Construct>(new CFunction(( (CSymbol) node.data ).convert(), t));
                            }
                            conversion.addChild(list.get(i + 1));
                            list.set(i, conversion);
                            list.remove(i + 1);
                            i--;
                        }
                    }
                    
                    for(int i = 0; i < list.size() - 1; i++){
                        GenericTreeNode<Construct> next = list.get(i + 1);
                        if(next.data instanceof CSymbol){
                            if(((CSymbol)next.data).isExponential()){
                                GenericTreeNode<Construct> conversion = new GenericTreeNode<Construct>(new CFunction(((CSymbol)next.data).convert(), t));
                                conversion.addChild(list.get(i));
                                conversion.addChild(list.get(i + 2));
                                list.set(i, conversion);
                                list.remove(i + 1);
                                list.remove(i + 1);
                                i--;
                            }
                        }
                    }

                    //Multiplicative
                    for (int i = 0; i < list.size() - 1; i++) {
                        GenericTreeNode<Construct> next = list.get(i + 1);
                        if (next.data instanceof CSymbol) {
                            if (( (CSymbol) next.data ).isMultaplicative()) {
                                GenericTreeNode<Construct> conversion = new GenericTreeNode<Construct>(new CFunction(( (CSymbol) next.data ).convert(), t));
                                conversion.addChild(list.get(i));
                                conversion.addChild(list.get(i + 2));
                                list.set(i, conversion);
                                list.remove(i + 1);
                                list.remove(i + 1);
                                i--;
                            }
                        }
                    }
                    //Additive
                    for (int i = 0; i < list.size() - 1; i++) {
                        GenericTreeNode<Construct> next = list.get(i + 1);
                        if (next.data instanceof CSymbol && ( (CSymbol) next.data ).isAdditive()) {
                            GenericTreeNode<Construct> conversion = new GenericTreeNode<Construct>(new CFunction(( (CSymbol) next.data ).convert(), t));
                            conversion.addChild(list.get(i));
                            conversion.addChild(list.get(i + 2));
                            list.set(i, conversion);
                            list.remove(i + 1);
                            list.remove(i + 1);
                            i--;
                        }
                    }
                    //relational
                    for (int i = 0; i < list.size() - 1; i++) {
                        GenericTreeNode<Construct> node = list.get(i + 1);
                        if (node.data instanceof CSymbol && ( (CSymbol) node.data ).isRelational()) {
                            CSymbol sy = (CSymbol) node.data;
                            GenericTreeNode<Construct> conversion = new GenericTreeNode<Construct>(new CFunction(sy.convert(), t));
                            conversion.addChild(list.get(i));
                            conversion.addChild(list.get(i + 2));
                            list.set(i, conversion);
                            list.remove(i + 1);
                            list.remove(i + 1);
                            i--;
                        }
                    }
                    //equality
                    for (int i = 0; i < list.size() - 1; i++) {
                        GenericTreeNode<Construct> node = list.get(i + 1);
                        if (node.data instanceof CSymbol && ( (CSymbol) node.data ).isEquality()) {
                            CSymbol sy = (CSymbol) node.data;
                            GenericTreeNode<Construct> conversion = new GenericTreeNode<Construct>(new CFunction(sy.convert(), t));
                            conversion.addChild(list.get(i));
                            conversion.addChild(list.get(i + 2));
                            list.set(i, conversion);
                            list.remove(i + 1);
                            list.remove(i + 1);
                            i--;
                        }
                    }
                    //logical and
                    for (int i = 0; i < list.size() - 1; i++) {
                        GenericTreeNode<Construct> node = list.get(i + 1);
                        if (node.data instanceof CSymbol && ( (CSymbol) node.data ).isLogicalAnd()) {
                            CSymbol sy = (CSymbol) node.data;
                            GenericTreeNode<Construct> conversion = new GenericTreeNode<Construct>(new CFunction(sy.convert(), t));
                            conversion.addChild(list.get(i));
                            conversion.addChild(list.get(i + 2));
                            list.set(i, conversion);
                            list.remove(i + 1);
                            list.remove(i + 1);
                            i--;
                        }
                    }
                    //logical or
                    for (int i = 0; i < list.size() - 1; i++) {
                        GenericTreeNode<Construct> node = list.get(i + 1);
                        if (node.data instanceof CSymbol && ( (CSymbol) node.data ).isLogicalOr()) {
                            CSymbol sy = (CSymbol) node.data;
                            GenericTreeNode<Construct> conversion = new GenericTreeNode<Construct>(new CFunction(sy.convert(), t));
                            conversion.addChild(list.get(i));
                            conversion.addChild(list.get(i + 2));
                            list.set(i, conversion);
                            list.remove(i + 1);
                            list.remove(i + 1);
                            i--;
                        }
                    }
                }
                catch (IndexOutOfBoundsException e) {
                    throw new ConfigCompileException("Unexpected symbol (" + list.get(list.size() - 1).data.val() + "). Did you forget to quote your symbols?", t);
                }
            }

            //Look for a CEntry here
            if (list.size() >= 1) {
                GenericTreeNode<Construct> node = list.get(0);
                if (node.data instanceof CLabel) {
                    GenericTreeNode<Construct> value = new GenericTreeNode<Construct>(new CFunction("__autoconcat__", t));
                    for (int i = 1; i < list.size(); i++) {
                        value.addChild(list.get(i));
                    }
                    GenericTreeNode<Construct> ce = new GenericTreeNode<Construct>(new CFunction("centry", t));
                    ce.addChild(node);
                    ce.addChild(value);
                    return ce;
                }
            }

            //We've eliminated the need for __autoconcat__ either way, however, if there are still arguments
            //left, it needs to go to sconcat, which MAY be able to be further optimized, but that will
            //be handled in MethodScriptCompiler's optimize function. Also, we must scan for CPreIdentifiers,
            //which may be turned into a function
            if (list.size() == 1) {
                return list.get(0);
            } else {
                for(int i = 0; i < list.size(); i++){
                    if(list.get(i).data.getCType() == Construct.ConstructType.IDENTIFIER){
                        if(i == 0){
                            //Yup, it's an identifier
                            CFunction identifier = new CFunction(list.get(i).data.val(), t);
                            list.remove(0);
                            GenericTreeNode<Construct> child = list.get(0);
                            if(list.size() > 1){
                                child = new GenericTreeNode<Construct>(new CFunction("sconcat", t));
                                child.setChildren(list);
                            }
                            try{
                                Function f = (Function)FunctionList.getFunction(identifier);                                
                                GenericTreeNode<Construct> node 
                                        = new GenericTreeNode<Construct>(f.execs(t, null, null, child));                                
                                return node;
                            } catch(Exception e){
                                throw new Error("Unknown function " + identifier.val() + "?");
                            }                                                      
                        } else {
                            //Hmm, this is weird. I'm not sure what condition this can happen in
                            throw new ConfigCompileException("Unexpected IDENTIFIER? O.o Please report a bug,"
                                    + " and include the script you used to get this error.", t);
                        }
                    }
                }
                GenericTreeNode<Construct> tree;
                if (returnSConcat) {
                    tree = new GenericTreeNode<Construct>(new CFunction("sconcat", t));
                } else {
                    tree = new GenericTreeNode<Construct>(new CFunction("concat", t));
                }
                tree.setChildren(list);
                return tree;
            }
        }

        @Override
        public boolean appearInDocumentation() {
            return false;
        }
                
    }
    
    @api
    public static class npe extends AbstractFunction {

        public String getName() {
            return "npe";
        }

        public Integer[] numArgs() {
            return new Integer[]{0};
        }

        public String docs() {
            return "void {}";
        }

        public Exceptions.ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V0_0_0;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env env, Construct... args) throws ConfigRuntimeException {
            Object o = null;
            o.toString();
            return new CVoid(t);
        }

        @Override
        public boolean appearInDocumentation() {
            return false;
        }
                
    }
    
    @api public static class dyn extends AbstractFunction{

        public String getName() {
            return "dyn";
        }

        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

        public String docs() {
            return "exception {[argument]} Registers as a dynamic component, for optimization testing; that is"
                    + " to say, this will not be optimizable ever."
                    + " It simply returns the argument provided, or void if none.";
        }

        public ExceptionType[] thrown() {
            return null;
        }

        public boolean isRestricted() {
            return false;
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            if(args.length == 0){
                return new CVoid(t);
            }
            return args[0];
        }

        public CHVersion since() {
            return CHVersion.V0_0_0;
        }               
        
    }
}
