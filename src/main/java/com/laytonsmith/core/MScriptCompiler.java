/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.constructs.Token.TType;
import com.laytonsmith.core.functions.FunctionList;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Layton
 */
public class MScriptCompiler {

    public static List<Token> lex(String config, File file) throws ConfigCompileException {
        config = config.replaceAll("\r\n", "\n");
        config = config + "\n";
        List<Token> token_list = new ArrayList<Token>();
        //Set our state variables
        boolean state_in_quote = false;
        boolean in_comment = false;
            boolean comment_is_block = false;
        boolean in_opt_var = false;
        StringBuffer buf = new StringBuffer();
        int line_num = 1;
        //first we lex
        for (int i = 0; i < config.length(); i++) {
            Character c = config.charAt(i);
            Character c2 = null;
            if (i < config.length() - 1) {
                c2 = config.charAt(i + 1);
            }
            if (c == '\n') {
                line_num++;
            }
//            if ((token_list.isEmpty() || token_list.get(token_list.size() - 1).type.equals(TType.NEWLINE))
//                    && c == '#') {
            if((c == '#' || (c == '/' && (c2 == '*'))) && !in_comment && !state_in_quote){
                in_comment = true;
                if(c2 == '*'){
                    comment_is_block = true;
                }
                continue;
            }
            if (in_comment){                
                if(!comment_is_block && c != '\n' || comment_is_block && c != '*' && (c2 != null && c2 != '/')){
                    continue;
                }
            }
            if(c == '*' && c2 == '/' && in_comment && comment_is_block){
                in_comment = false;
                comment_is_block = false;
                i++;
                continue;
            }
            if (c == '[' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num, file));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.LSQUARE_BRACKET, "[", line_num, file));
                in_opt_var = true;
                continue;
            }
            if (c == '=' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num, file));
                    buf = new StringBuffer();
                }
                if (in_opt_var) {
                    token_list.add(new Token(TType.OPT_VAR_ASSIGN, "=", line_num, file));
                } else {
                    token_list.add(new Token(TType.ALIAS_END, "=", line_num, file));
                }
                continue;
            }
            if (c == ']' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num, file));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.RSQUARE_BRACKET, "]", line_num, file));
                in_opt_var = false;
                continue;
            }
            if (c == ':' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num, file));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.IDENT, ":", line_num, file));
                continue;
            }
            if (c == ',' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num, file));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.COMMA, ",", line_num, file));
                continue;
            }
            if (c == '(' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.FUNC_NAME, buf.toString(), line_num, file));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.FUNC_START, "(", line_num, file));
                continue;
            }
            if (c == ')' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num, file));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.FUNC_END, ")", line_num, file));
                continue;
            }
            if (Character.isWhitespace(c) && !state_in_quote && c != '\n') {
                //ignore the whitespace, but end the previous token
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num, file));
                    buf = new StringBuffer();
                }
            } else if (c == '\'') {
                if (state_in_quote) {
                    token_list.add(new Token(TType.STRING, buf.toString(), line_num, file));
                    buf = new StringBuffer();
                    state_in_quote = false;
                    continue;
                } else {
                    state_in_quote = true;
                    if (buf.length() > 0) {
                        token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num, file));
                        buf = new StringBuffer();
                    }
                    continue;
                }
            } else if (c == '\\') {
                //escaped characters
                if (state_in_quote) {
                    if (c2 == '\\') {
                        buf.append("\\");
                    } else if (c2 == '\'') {
                        buf.append("'");
                    } else if(c2 == 'n'){
                        buf.append("\n");
                    } else if(c2 == 'u'){
                        //Grab the next 4 characters, and check to see if they are numbers
                        StringBuilder unicode = new StringBuilder();
                        for(int m = 0; m < 4; m++){
                            unicode.append(config.charAt(i + 2 + m));
                        }
                        try{
                            Integer.parseInt(unicode.toString(), 16);
                        } catch(NumberFormatException e){
                            throw new ConfigCompileException("Unrecognized unicode escape sequence", line_num, file);
                        }
                        buf.append(Character.toChars(Integer.parseInt(unicode.toString(), 16)));                        
                        i += 4;
                    } else {
                        //Since we might expand this list later, don't let them
                        //use unescaped backslashes
                        throw new ConfigCompileException("The escape sequence \\" + c2 + " is not a recognized escape sequence", line_num, file);
                    }

                    i++;
                    continue;
                } else {
                    //Control character backslash
                    token_list.add(new Token(TType.SEPERATOR, "\\", line_num, file));
                }
            } else if (state_in_quote) {
                buf.append(c);
                continue;
            } else if (c == '\n' && !comment_is_block) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num, file));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.NEWLINE, "\n", line_num, file));
                in_comment = false;
                comment_is_block = false;
                continue;
            } else { //in a literal
                buf.append(c);
                continue;
            }
        } //end lexing
        if(state_in_quote){
            throw new ConfigCompileException("Unended string literal", line_num, file);
        }
        if(in_comment || comment_is_block){
            throw new ConfigCompileException("Unended comment", line_num, file);
        }
                //look at the tokens, and get meaning from them
        for (Token t : token_list) {
            if (t.type.equals(TType.UNKNOWN)) {
                if (t.val().matches("/.*")) {
                    t.type = TType.COMMAND;
                } else if (t.val().matches("\\\\")) {
                    t.type = TType.SEPERATOR;
                } else if (t.val().matches("\\$[a-zA-Z0-9_]+")) {
                    t.type = TType.VARIABLE;
                } else if (t.val().matches("\\@[a-zA-Z0-9_]+")) {
                    t.type = TType.IVARIABLE;
                } else if (t.val().equals("$")) {
                    t.type = TType.FINAL_VAR;
                } else {
                    t.type = TType.LIT;
                }
            }
        }
        return token_list;
    }

    /**
     * This function breaks the token stream into parts, seperating the aliases/MScript from the command triggers
     * @param tokenStream
     * @return
     * @throws ConfigCompileException 
     */
    public static List<Script> preprocess(List<Token> tokenStream, Env env) throws ConfigCompileException {
        //First, pull out the duplicate newlines
        ArrayList<Token> temp = new ArrayList<Token>();
        for (int i = 0; i < tokenStream.size(); i++) {
            try {
                if (tokenStream.get(i).type.equals(TType.NEWLINE)) {
                    temp.add(new Token(TType.NEWLINE, "\n", tokenStream.get(i).line_num, tokenStream.get(i).file));
                    while (tokenStream.get(++i).type.equals(TType.NEWLINE)) {
                    }
                }
                temp.add(tokenStream.get(i));
            } catch (IndexOutOfBoundsException e) {
            }
        }

        if (temp.size() > 0 && temp.get(0).type.equals(TType.NEWLINE)) {
            temp.remove(0);
        }

        tokenStream = temp;
        temp = new ArrayList<Token>();

        //Handle multiline constructs
        ArrayList<Token> tokens1_1 = new ArrayList<Token>();
        boolean inside_multiline = false;
        for (int i = 0; i < tokenStream.size(); i++) {
            Token prevToken = i - 1 >= tokenStream.size() ? tokenStream.get(i - 1) : new Token(TType.UNKNOWN, "", 0, null);
            Token thisToken = tokenStream.get(i);
            Token nextToken = i + 1 < tokenStream.size() ? tokenStream.get(i + 1) : new Token(TType.UNKNOWN, "", 0, null);
            //take out newlines between the = >>> and <<< tokens (also the tokens)
            if (thisToken.type.equals(TType.ALIAS_END) && nextToken.val().equals(">>>")) {
                inside_multiline = true;
                tokens1_1.add(thisToken);
                i++;
                continue;
            }
            if (thisToken.val().equals("<<<")) {
                if (!inside_multiline) {
                    throw new ConfigCompileException("Found multiline end symbol, and no multiline start found",
                            thisToken.line_num, thisToken.file);
                }
                inside_multiline = false;
                continue;
            }
            if (thisToken.val().equals(">>>") && !prevToken.type.equals(TType.ALIAS_END)) {
                throw new ConfigCompileException("Multiline symbol must follow the alias_end token", thisToken.line_num, thisToken.file);
            }

            //If we're not in a multiline construct, or we are in it and it's not a newline, add
            //it
            if (!inside_multiline || (inside_multiline && !thisToken.type.equals(TType.NEWLINE))) {
                tokens1_1.add(thisToken);
            }
        }

        //take out newlines that are behind a \
        ArrayList<Token> tokens2 = new ArrayList<Token>();
        for (int i = 0; i < tokens1_1.size(); i++) {
            if (!tokens1_1.get(i).type.equals(TType.STRING) && tokens1_1.get(i).val().equals("\\") && tokens1_1.size() > i
                    && tokens1_1.get(i + 1).type.equals(TType.NEWLINE)) {
                tokens2.add(tokens1_1.get(i));
                i++;
                continue;
            }
            tokens2.add(tokens1_1.get(i));
        }





        //Now that we have all lines minified, we should be able to split
        //on newlines, and easily find the left and right sides

        List<Token> left = new ArrayList<Token>();
        List<Token> right = new ArrayList<Token>();
        List<Script> scripts = new ArrayList<Script>();
        boolean inLeft = true;
        for (Token t : tokens2) {
            if (inLeft) {
                if (t.type == TType.ALIAS_END) {
                    inLeft = false;
                } else {
                    left.add(t);
                }
            } else {
                if (t.type == TType.NEWLINE) {
                    inLeft = true;
                    //Env newEnv = new Env();//env;
//                    try{
//                        newEnv = env.clone();
//                    } catch(Exception e){}
                    Script s = new Script(left, right);
                    scripts.add(s);
                    left = new ArrayList();
                    right = new ArrayList();
                } else {
                    right.add(t);
                }
            }
        }
        return scripts;
    }

    public static GenericTreeNode<Construct> compile(List<Token> stream) throws ConfigCompileException {
        GenericTreeNode<Construct> tree = new GenericTreeNode<Construct>();
        tree.setData(new CNull(0, null));
        Stack<GenericTreeNode> parents = new Stack<GenericTreeNode>();
        Stack<AtomicInteger> constructCount = new Stack<AtomicInteger>();
        constructCount.push(new AtomicInteger(0));
        Stack<AtomicInteger> arrayStack = new Stack<AtomicInteger>();
        arrayStack.add(new AtomicInteger(-1));
        parents.push(tree);
        int parens = 0;
        Token t = null;
        for (int i = 0; i < stream.size(); i++) {
            t = stream.get(i);
            Token prev = i - 1 >= 0 ? stream.get(i - 1) : new Token(TType.UNKNOWN, "", t.line_num, t.file);
            Token next = i + 1 < stream.size() ? stream.get(i + 1) : new Token(TType.UNKNOWN, "", t.line_num, t.file); 
                
            //Associative array handling
            if(next.type.equals(TType.IDENT)){
                tree.addChild(new GenericTreeNode<Construct>(new CLabel(Static.resolveConstruct(t.val(), t.line_num, t.file))));
                constructCount.peek().incrementAndGet();
                i++;
                continue;
            }           
            //Array notation handling
            if(t.type.equals(TType.LSQUARE_BRACKET)){                
                arrayStack.push(new AtomicInteger(tree.getChildren().size() - 1));
                continue;
            } else if(t.type.equals(TType.RSQUARE_BRACKET)){
                boolean emptyArray = false;
                if(prev.type.equals(TType.LSQUARE_BRACKET)){
                    //throw new ConfigCompileException("Empty array_get operator ([])", t.line_num); 
                    emptyArray = true;
                }
                if(arrayStack.size() == 1){
                    throw new ConfigCompileException("Mismatched square bracket", t.line_num, t.file);
                }
                int array = arrayStack.pop().get();
                int index = array + 1;
                GenericTreeNode<Construct> myArray = tree.getChildAt(array);
                GenericTreeNode<Construct> myIndex;
                if(!emptyArray){
                    myIndex = tree.getChildAt(index);
                } else {
                    myIndex = new GenericTreeNode<Construct>(new CString("0..-1", t.line_num, t.file));
                }
                tree.setChildren(tree.getChildren().subList(0, array));
                GenericTreeNode<Construct> arrayGet = new GenericTreeNode<Construct>(new CFunction("array_get", t.line_num, t.file));
                arrayGet.addChild(myArray);
                arrayGet.addChild(myIndex);
                tree.addChild(arrayGet);
                constructCount.peek().decrementAndGet();
            }
            /*if (t.type.equals(TType.OPT_VAR_ASSIGN) || t.type.equals(TType.LSQUARE_BRACKET)
                    || t.type.equals(TType.RSQUARE_BRACKET)) {
                throw new ConfigCompileException("Unexpected " + t.type.toString(), t.line_num);
            } else */if (t.type == TType.LIT) {
                tree.addChild(new GenericTreeNode<Construct>(Static.resolveConstruct(t.val(), t.line_num, t.file)));
                constructCount.peek().incrementAndGet();
            } else if (t.type.equals(TType.STRING) || t.type.equals(TType.COMMAND)) {
                tree.addChild(new GenericTreeNode<Construct>(new CString(t.val(), t.line_num, t.file)));
                constructCount.peek().incrementAndGet();
            } else if (t.type.equals(TType.IVARIABLE)) {
                tree.addChild(new GenericTreeNode<Construct>(new IVariable(t.val(), t.line_num, t.file)));
                constructCount.peek().incrementAndGet();
            } else if(t.type.equals(TType.UNKNOWN)){
                tree.addChild(new GenericTreeNode<Construct>(Static.resolveConstruct(t.val(), t.line_num, t.file)));
                constructCount.peek().incrementAndGet();
            } else if (t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)) {
                tree.addChild(new GenericTreeNode<Construct>(new Variable(t.val(), null, false, t.type.equals(TType.FINAL_VAR), t.line_num, t.file)));
                constructCount.peek().incrementAndGet();
                //right_vars.add(new Variable(t.val(), null, t.line_num));
            } else if (t.type.equals(TType.FUNC_NAME)) {
                CFunction func = new CFunction(t.val(), t.line_num, t.file);
                //This will throw an exception for us if the function doesn't exist
                if(!func.val().matches("^_[^_].*")){
                    FunctionList.getFunction(func);
                }
                GenericTreeNode<Construct> f = new GenericTreeNode<Construct>(func);
                tree.addChild(f);
                constructCount.push(new AtomicInteger(0));
                tree = f;
                parents.push(f);
            } else if (t.type.equals(TType.FUNC_START)) {
                if (!prev.type.equals(TType.FUNC_NAME)) {
                    throw new ConfigCompileException("Unexpected parenthesis", t.line_num, t.file);
                }
                parens++;
            } else if (t.type.equals(TType.FUNC_END)) {
                if (parens < 0) {
                    throw new ConfigCompileException("Unexpected parenthesis", t.line_num, t.file);
                }
                parens--;
                parents.pop();
                if(constructCount.peek().get() > 1){
                    //We need to autoconcat some stuff
                    int stacks = constructCount.peek().get();
                    int replaceAt = tree.getChildren().size() - stacks;
                    GenericTreeNode<Construct> c = new GenericTreeNode<Construct>(new CFunction("sconcat", 0, null));
                    List<GenericTreeNode<Construct>> subChildren = new ArrayList<GenericTreeNode<Construct>>();
                    for(int b = replaceAt; b < tree.getNumberOfChildren(); b++){
                        subChildren.add(tree.getChildAt(b));
                    }
                    c.setChildren(subChildren);                    
                    if(replaceAt > 0){
                        List<GenericTreeNode<Construct>> firstChildren = new ArrayList<GenericTreeNode<Construct>>();
                        for(int d = 0; d < replaceAt; d++){
                            firstChildren.add(tree.getChildAt(d));
                        }
                        tree.setChildren(firstChildren);
                    } else {
                        tree.removeChildren();
                    }
                    tree.addChild(c);
                }
                //Check argument number now
                if(!tree.getData().val().matches("^_[^_].*")){
                    Integer [] numArgs = FunctionList.getFunction(tree.getData()).numArgs();
                    if(!Arrays.asList(numArgs).contains(Integer.MAX_VALUE) && !Arrays.asList(numArgs).contains(tree.getChildren().size())){
                        throw new ConfigCompileException("Incorrect number of arguments passed to " + tree.getData().val(), tree.getData().getLineNum(),
                                tree.getData().getFile());
                    }
                }
                constructCount.pop();
                constructCount.peek().incrementAndGet();
                try{
                    tree = parents.peek();
                } catch(EmptyStackException e){
                    throw new ConfigCompileException("Unexpected end parenthesis", t.line_num, t.file);
                }
            } else if (t.type.equals(TType.COMMA)) {
                if(constructCount.peek().get() > 1){
                    int stacks = constructCount.peek().get();
                    int replaceAt = tree.getChildren().size() - stacks;
                    GenericTreeNode<Construct> c = new GenericTreeNode<Construct>(new CFunction("sconcat", 0, null));
                    List<GenericTreeNode<Construct>> subChildren = new ArrayList<GenericTreeNode<Construct>>();
                    for(int b = replaceAt; b < tree.getNumberOfChildren(); b++){
                        subChildren.add(tree.getChildAt(b));
                    }
                    c.setChildren(subChildren);                    
                    if(replaceAt > 0){
                        List<GenericTreeNode<Construct>> firstChildren = new ArrayList<GenericTreeNode<Construct>>();
                        for(int d = 0; d < replaceAt; d++){
                            firstChildren.add(tree.getChildAt(d));
                        }
                        tree.setChildren(firstChildren);
                    } else {
                        tree.removeChildren();
                    }
                    tree.addChild(c);                   
                }
                constructCount.peek().set(0);
                continue;
            } 
        }
        if(arrayStack.size() != 1){
            throw new ConfigCompileException("Mismatched square brackets", t.line_num, t.file);
        }
        if (parens != 0) {
            throw new ConfigCompileException("Mismatched parenthesis", t.line_num, t.file);
        }
        return tree;
    }      
    
    /**
     * Executes a pre-compiled mscript, given the specified Script environment. Both done and script 
     * may be null, and if so, reasonable defaults will be provided. The value sent to done will also
     * be returned, as a Construct, so this one function may be used synchronously also.
     * @param root
     * @param done
     * @param script 
     */
    public static Construct execute(GenericTreeNode<Construct> root, Env env, MScriptComplete done, Script script){
        if(script == null){
            script = new Script(null, null);
        }
        StringBuilder b = new StringBuilder();
        for (GenericTreeNode<Construct> gg : root.getChildren()) {
            String ret = script.eval(gg, env).val();
            if (ret != null && !ret.trim().equals("")) {
                b.append(ret).append(" ");
            }
        }
        if(done != null){
            done.done(b.toString().trim());
        }
        return Static.resolveConstruct(b.toString().trim(), 0, null);
    }
}
