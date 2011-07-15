/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.Constructs.Token.TType;
import com.laytonsmith.aliasengine.functions.FunctionList;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class MScriptCompiler {

    public static List<Token> lex(String config) throws ConfigCompileException {
        config = config.replaceAll("\r\n", "\n");
        config = config + "\n";
        List<Token> token_list = new ArrayList<Token>();
        //Set our state variables
        boolean state_in_quote = false;
        boolean in_comment = false;
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
            if ((token_list.isEmpty() || token_list.get(token_list.size() - 1).type.equals(TType.NEWLINE))
                    && c == '#') {
                in_comment = true;
            }
            if (in_comment && c != '\n') {
                continue;
            }
            if (c == '[' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.LSQUARE_BRACKET, "[", line_num));
                in_opt_var = true;
                continue;
            }
            if (c == '=' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                if (in_opt_var) {
                    token_list.add(new Token(TType.OPT_VAR_ASSIGN, "=", line_num));
                } else {
                    token_list.add(new Token(TType.ALIAS_END, "=", line_num));
                }
                continue;
            }
            if (c == ']' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.RSQUARE_BRACKET, "]", line_num));
                in_opt_var = false;
                continue;
            }
            if (c == ':' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.IDENT, ":", line_num));
                continue;
            }
            if (c == ',' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.COMMA, ",", line_num));
                continue;
            }
            if (c == '(' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.FUNC_NAME, buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.FUNC_START, "(", line_num));
                continue;
            }
            if (c == ')' && !state_in_quote) {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.FUNC_END, ")", line_num));
                continue;
            }
            if (Character.isWhitespace(c) && !state_in_quote && c != '\n') {
                //ignore the whitespace, but end the previous token
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
                    buf = new StringBuffer();
                }
            } else if (c == '\'') {
                if (state_in_quote) {
                    token_list.add(new Token(TType.STRING, buf.toString(), line_num));
                    buf = new StringBuffer();
                    state_in_quote = false;
                    continue;
                } else {
                    state_in_quote = true;
                    if (buf.length() > 0) {
                        token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
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
                    } else {
                        //Since we might expand this list later, don't let them
                        //use unescaped backslashes
                        throw new ConfigCompileException("The escape sequence \\" + c2 + " is not a recognized escape sequence", line_num);
                    }

                    i++;
                    continue;
                } else {
                    //Control character backslash
                    token_list.add(new Token(TType.SEPERATOR, "\\", line_num));
                }
            } else if (state_in_quote) {
                buf.append(c);
                continue;
            } else if (c == '\n') {
                if (buf.length() > 0) {
                    token_list.add(new Token(TType.UNKNOWN, buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token(TType.NEWLINE, "\n", line_num));
                in_comment = false;
                continue;
            } else { //in a literal
                buf.append(c);
                continue;
            }
        } //end lexing
        return token_list;
    }

    /**
     * This function breaks the token stream into parts, seperating the aliases/MScript from the command triggers
     * @param tokenStream
     * @return
     * @throws ConfigCompileException 
     */
    public static List<Script> preprocess(List<Token> tokenStream) throws ConfigCompileException {
        //First, pull out the duplicate newlines
        ArrayList<Token> temp = new ArrayList<Token>();
        for (int i = 0; i < tokenStream.size(); i++) {
            try {
                if (tokenStream.get(i).type.equals(TType.NEWLINE)) {
                    temp.add(new Token(TType.NEWLINE, "\n", tokenStream.get(i).line_num));
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
            Token prevToken = i - 1 >= tokenStream.size() ? tokenStream.get(i - 1) : new Token(TType.UNKNOWN, "", 0);
            Token thisToken = tokenStream.get(i);
            Token nextToken = i + 1 < tokenStream.size() ? tokenStream.get(i + 1) : new Token(TType.UNKNOWN, "", 0);
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
                            thisToken.line_num);
                }
                inside_multiline = false;
                continue;
            }
            if (thisToken.val().equals(">>>") && !prevToken.type.equals(TType.ALIAS_END)) {
                throw new ConfigCompileException("Multiline symbol must follow the alias_end token", thisToken.line_num);
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



        //look at the tokens, and get meaning from them
        for (Token t : tokens2) {
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
        tree.setData(new Construct("root", Construct.ConstructType.NULL, 0));
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
            Token prev = i - 1 >= 0 ? stream.get(i - 1) : new Token(TType.UNKNOWN, "", t.line_num);
            
            //Array notation handling
            if(t.type.equals(TType.LSQUARE_BRACKET)){                
                arrayStack.push(new AtomicInteger(tree.getChildren().size() - 1));
                continue;
            } else if(t.type.equals(TType.RSQUARE_BRACKET)){
                int array = arrayStack.pop().get();
                int index = array + 1;
                GenericTreeNode<Construct> myArray = tree.getChildAt(array);
                GenericTreeNode<Construct> myIndex = tree.getChildAt(index);
                tree.setChildren(tree.getChildren().subList(0, array));
                GenericTreeNode<Construct> arrayGet = new GenericTreeNode<Construct>(new CFunction("array_get", t.line_num));
                arrayGet.addChild(myArray);
                arrayGet.addChild(myIndex);
                tree.addChild(arrayGet);
                constructCount.peek().decrementAndGet();
            }
            /*if (t.type.equals(TType.OPT_VAR_ASSIGN) || t.type.equals(TType.LSQUARE_BRACKET)
                    || t.type.equals(TType.RSQUARE_BRACKET)) {
                throw new ConfigCompileException("Unexpected " + t.type.toString(), t.line_num);
            } else */if (t.type == TType.LIT) {
                tree.addChild(new GenericTreeNode<Construct>(Static.resolveConstruct(t.val(), t.line_num)));
                constructCount.peek().incrementAndGet();
            } else if (t.type.equals(TType.STRING) || t.type.equals(TType.COMMAND)) {
                tree.addChild(new GenericTreeNode<Construct>(new CString(t.val(), t.line_num)));
                constructCount.peek().incrementAndGet();
            } else if (t.type.equals(TType.IVARIABLE)) {
                tree.addChild(new GenericTreeNode<Construct>(new IVariable(t.val(), t.line_num)));
                constructCount.peek().incrementAndGet();
            } else if(t.type.equals(TType.UNKNOWN)){
                tree.addChild(new GenericTreeNode<Construct>(Static.resolveConstruct(t.val(), t.line_num)));
                constructCount.peek().incrementAndGet();
            } else if (t.type.equals(TType.VARIABLE) || t.type.equals(TType.FINAL_VAR)) {
                tree.addChild(new GenericTreeNode<Construct>(new Variable(t.val(), null, t.line_num)));
                constructCount.peek().incrementAndGet();
                //right_vars.add(new Variable(t.val(), null, t.line_num));
            } else if (t.type.equals(TType.FUNC_NAME)) {
                CFunction func = new CFunction(t.val(), t.line_num);
                //This will throw an exception for us if the function doesn't exist
                FunctionList.getFunction(func);
                GenericTreeNode<Construct> f = new GenericTreeNode<Construct>(func);
                tree.addChild(f);
                constructCount.push(new AtomicInteger(0));
                tree = f;
                parents.push(f);
            } else if (t.type.equals(TType.FUNC_START)) {
                if (!prev.type.equals(TType.FUNC_NAME)) {
                    throw new ConfigCompileException("Unexpected parenthesis", t.line_num);
                }
                parens++;
            } else if (t.type.equals(TType.FUNC_END)) {
                if (parens < 0) {
                    throw new ConfigCompileException("Unexpected parenthesis", t.line_num);
                }
                parens--;
                parents.pop();
                if(constructCount.peek().get() > 1){
                    //We need to autoconcat some stuff
                    int stacks = constructCount.peek().get();
                    int replaceAt = tree.getChildren().size() - stacks;
                    GenericTreeNode<Construct> c = new GenericTreeNode<Construct>(new CFunction("sconcat", 0));
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
                Integer [] numArgs = FunctionList.getFunction(tree.getData()).numArgs();
                if(!Arrays.asList(numArgs).contains(Integer.MAX_VALUE) && !Arrays.asList(numArgs).contains(tree.getChildren().size())){
                    throw new ConfigCompileException("Incorrect number of arguments passed to " + tree.getData().val(), tree.getData().line_num);
                }
                constructCount.pop();
                constructCount.peek().incrementAndGet();
                try{
                    tree = parents.peek();
                } catch(EmptyStackException e){
                    throw new ConfigCompileException("Unexpected end parenthesis", t.line_num);
                }
            } else if (t.type.equals(TType.COMMA)) {
                if(constructCount.peek().get() > 1){
                    int stacks = constructCount.peek().get();
                    int replaceAt = tree.getChildren().size() - stacks;
                    GenericTreeNode<Construct> c = new GenericTreeNode<Construct>(new CFunction("sconcat", 0));
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
        if (parens != 0) {
            throw new ConfigCompileException("Mismatched parenthesis", t.line_num);
        }
        return tree;
    }      
}
