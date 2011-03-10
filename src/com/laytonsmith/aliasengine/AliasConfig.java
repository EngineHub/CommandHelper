/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine;

import com.laytonsmith.Alias.Tree.GenericTree;
import com.laytonsmith.Alias.Tree.GenericTreeNode;
import com.laytonsmith.Alias.Tree.GenericTreeTraversalOrderEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Layton
 */
public class AliasConfig {

    /**
     * Once the config file is compiled, it gets added to this variable
     */
    Alias aliasFile = null;

    public AliasConfig(String config) throws ConfigCompileException{
        config = config.replaceAll("\r\n", "\n");
        ArrayList<Token> token_list = new ArrayList<Token>();
        boolean state_in_quote = false;
        boolean in_comment = false;
        boolean in_opt_var = false;
        StringBuffer buf = new StringBuffer();
        int line_num = 1;
        //first we lex
        for(int i = 0; i < config.length(); i++){
            Character c = config.charAt(i);
            if(c == '\n'){
                line_num++;
            }
            if((token_list.isEmpty() || token_list.get(token_list.size() - 1).type.equals("newline")) &&
                    c == '#'){
                in_comment = true;
            }
            if(in_comment && c != '\n'){
                continue;
            }
            if(c == '[' && !state_in_quote){
                if(buf.length() > 0){
                    token_list.add(new Token("unknown", buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token("opt_var_start", "[", line_num));
                in_opt_var = true;
                continue;
            }
            if(c == '=' && !state_in_quote){
                if(buf.length() > 0){
                    token_list.add(new Token("unknown", buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                if(in_opt_var){
                    token_list.add(new Token("opt_var_assign", "=", line_num));
                } else{
                    token_list.add(new Token("alias_end", "=", line_num));
                }
                continue;
            }
            if(c == ']' && !state_in_quote){
                if(buf.length() > 0){
                    token_list.add(new Token("unknown", buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token("opt_var_end", "]", line_num));
                in_opt_var = false;
                continue;
            }
            if(c == ',' && !state_in_quote){
                if(buf.length() > 0){
                    token_list.add(new Token("unknown", buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token("comma", ",", line_num));
                continue;
            }
            if(c == '(' && !state_in_quote){
                if(buf.length() > 0){
                    token_list.add(new Token("func_name", buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token("func_start", "(", line_num));
                continue;
            }
            if(c == ')' && !state_in_quote){
                if(buf.length() > 0){
                    token_list.add(new Token("unknown", buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token("func_end", ")", line_num));
                continue;
            }
            if(Character.isWhitespace(c) && !state_in_quote && c != '\n'){
                if(buf.length() > 0){
                    token_list.add(new Token("unknown", buf.toString(), line_num));
                    buf = new StringBuffer();
                }
            }
            else if(c == '\''){
                if(state_in_quote){
                    if(buf.length() > 2 && buf.charAt(buf.length() - 1) == '\\'){
                        //escaped quote, take it out
                        buf = new StringBuffer(buf.substring(0, buf.length() - 1));
                        buf.append("'");
                        continue;
                    }
                    token_list.add(new Token("string", buf.toString(), line_num));
                    buf = new StringBuffer();
                    state_in_quote = false;
                    continue;
                } else{
                    state_in_quote = true;
                    if(buf.length() > 0){
                        token_list.add(new Token("unknown", buf.toString(), line_num));
                        buf = new StringBuffer();
                    }
                    continue;
                }
            } else if(state_in_quote){
                buf.append(c);
                continue;
            } else if(c == '\n'){
                if(buf.length() > 0){
                    token_list.add(new Token("unknown", buf.toString(), line_num));
                    buf = new StringBuffer();
                }
                token_list.add(new Token("newline", "\n", line_num));
                in_comment = false;
                continue;
            } else{ //in a literal
                buf.append(c);
                continue;
            }
        }

        //So, there has to be a more efficient way to do this, but I don't
        //really care right now. It's only read in occasionally, so it's no biggie.

        //take out extra newlines
        ArrayList<Token> tokens1 = new ArrayList<Token>();
        for(int i = 0; i < token_list.size(); i++){
            try{
                if(token_list.get(i).type.equals("newline")){
                    tokens1.add(new Token("newline", "\n", line_num));
                    while(token_list.get(++i).type.equals("newline")){
                    }
                }
                tokens1.add(token_list.get(i));
            } catch(IndexOutOfBoundsException e){}
        }

        //take out newlines that are behind a \
        ArrayList<Token> tokens2 = new ArrayList<Token>();
        for(int i = 0; i < tokens1.size(); i++){
            if(tokens1.get(i).value.equals("\\") && tokens1.get(i + 1).type.equals("newline")){
                tokens2.add(tokens1.get(i));
                i++;
                continue;
            }
            tokens2.add(tokens1.get(i));
        }

        //look at the tokens, and get meaning from them
        for(Token t : tokens2){
            if(t.type.equals("unknown")){
                if(t.value.matches("/.*")){
                    t.type = "command";
                } else if(t.value.matches("\\\\")){
                    t.type = "seperator";
                } else if(t.value.matches("\\$[a-zA-Z0-9_]+")){
                    t.type = "variable";
                } else if(t.value.matches("\\[\\$\\d=.*\\]")){
                    t.type = "opt_var_def";
                } else if(t.value.equals("$")){
                    t.type = "final_var";
                }
                else {
                    t.type = "lit";
                }
            }
        }

        //split the tokens into seperate commands now
        ArrayList<Token> aliasBuffer = new ArrayList<Token>();
        ArrayList<ArrayList<Token>> commandBuffer = new ArrayList<ArrayList<Token>>();
        ArrayList<Token> tinyCmdBuffer = new ArrayList<Token>();
        boolean in_alias = true;
        Alias a = new Alias();
        for(Token t : tokens2){
            if(t.type.equals("newline")){
                if(aliasBuffer.size() > 0){
                    if(in_alias){
                        throw new ConfigCompileException("Unexpected newline", line_num);
                    }
                    if(commandBuffer.size() > 0 || tinyCmdBuffer.size() > 0){
                        commandBuffer.add((ArrayList<Token>)tinyCmdBuffer.clone());
                        a.alias.add(new Command(aliasBuffer));
                        ArrayList<Command> tempBuf = new ArrayList<Command>();
                        for(int i = 0; i < commandBuffer.size(); i++){
                            ArrayList<Token> tlist = commandBuffer.get(i);
                            tempBuf.add(new Command(tlist));
                        }
                        a.real.add(tempBuf);
                        aliasBuffer.clear();
                        commandBuffer.clear();
                        tinyCmdBuffer.clear();
                        in_alias = true;
                        continue;
                    }
                }
            }
            else if(t.type.equals("alias_end")){
                in_alias = false;
            } else {
                if(in_alias){
                    aliasBuffer.add(t);
                } else{
                    if(t.type.equals("seperator")){
                        commandBuffer.add((ArrayList<Token>)tinyCmdBuffer.clone());
                        tinyCmdBuffer.clear();
                    } else{
                        tinyCmdBuffer.add(t);
                    }
                }
            }
        }
        //Check the syntax. Look for mismatched variables, mismatched parenthesis and square brackets,
        //missing commands, bad function names, strings on the left side (except directly after a
        //opt_var_assign), optional args on the right
        /**
         * Valid tokens on left:
         * command, opt_var_start, variable, final_var, opt_var_assign, opt_var_end, lit,
         * string (only directly after opt_var_assign)
         */
        for(int i = 0; i < a.alias.size(); i++){
            Command c = a.alias.get(i);
            ArrayList<Command> real = a.real.get(i);
            ArrayList<Variable> left_vars = new ArrayList<Variable>();
            ArrayList<Variable> right_vars = new ArrayList<Variable>();
            boolean inside_opt_var = false;
            boolean after_no_def_opt_var = false;
            for(int j = 0; j < c.tokens.size(); j++){
               Token t = c.tokens.get(j);
               Token prev_token = j - 2 >= 0?c.tokens.get(j - 2):new Token("unknown", "", t.line_num);
               Token last_token = j - 1 >= 0?c.tokens.get(j - 1):new Token("unknown", "", t.line_num);
               Token next_token = j + 1 < c.tokens.size()?c.tokens.get(j + 1):new Token("unknown", "", t.line_num);
               Token after_token = j + 2 < c.tokens.size()?c.tokens.get(j + 2):new Token("unknown", "", t.line_num);

               if(t.type.equals("final_var") && c.tokens.size() - j >= 5){
                   throw new ConfigCompileException("final_var $ must be the last argument in the alias", t.line_num);
               }
               if(t.type.equals("variable") || t.type.equals("final_var")){
                   left_vars.add(new Variable(t.value, null, t.line_num));
               }
               if(j == 0 && !t.type.equals("command")){
                   throw new ConfigCompileException("Expected command (/command) at start of alias." +
                           " Instead, found " + t.type + " (" + t.value + ")", t.line_num);
               }
               if(after_no_def_opt_var && !inside_opt_var){
                   if(t.type.equals("lit") || t.type.equals("string") || t.type.equals("opt_var_assign")){
                       throw new ConfigCompileException("You cannot have anything other than optional arguments after your" +
                               " first optional argument, other that other optional arguments with no default", t.line_num);
                   }
               }
               if(!t.type.equals("opt_var_start") &&
                       !t.type.equals("opt_var_assign") &&
                       !t.type.equals("opt_var_end") &&
                       !t.type.equals("variable") &&
                       !t.type.equals("lit") &&
                       !t.type.equals("command") &&
                       !t.type.equals("final_var")){
                   if(!(t.type.equals("string") && j - 1 > 0 && c.tokens.get(j - 1).type.equals("opt_var_assign")))
                       throw new ConfigCompileException("Unexpected " + t.type + " (" + t.value + ")", t.line_num);
               }
               if(last_token.type.equals("command")){
                   if(!(t.type.equals("variable") || t.type.equals("opt_var_start") || t.type.equals("final_var") ||
                            t.type.equals("lit"))){
                       throw new ConfigCompileException("Unexpected " + t.type + " (" + t.value + ") after command", t.line_num);
                   }
               }
               if(last_token.type.equals("opt_var_start")){
                   inside_opt_var = true;
                   if(!(t.type.equals("final_var") || t.type.equals("variable"))){
                       throw new ConfigCompileException("Unexpected " + t.type + " (" + t.value + ")", t.line_num);
                   }
               }
               if(inside_opt_var && t.type.equals("opt_var_assign")){
                   if(!((next_token.type.equals("string") || next_token.type.equals("lit")) && after_token.type.equals("opt_var_end") ||
                           (next_token.type.equals("opt_var_end")))){
                       throw new ConfigCompileException("Unexpected token in opt_var", t.line_num);
                   } else if(next_token.type.equals("string") || next_token.type.equals("lit")){
                       left_vars.get(left_vars.size() - 1).def = next_token.value;
                   } else {
                       left_vars.get(left_vars.size() - 1).def = "";
                   }
               }
               if(t.type.equals("opt_var_end")){
                   if(!inside_opt_var){
                       throw new ConfigCompileException("Unexpected opt_var_end", t.line_num);
                   }
                   inside_opt_var = false;
                   if(last_token.type.equals("variable")
                           || last_token.type.equals("final_var")){
                       after_no_def_opt_var = true;
                   }
               }
            }
            for(int j = 0; j < real.size(); j++){
                Command r = real.get(j);
                int paren_stack = 0;
                GenericTree<Construct> root = new GenericTree<Construct>();
                root.setRoot(new GenericTreeNode<Construct>(new Token("root", "root", 0)));
                GenericTreeNode<Construct> node = root.getRoot();
                Stack<GenericTreeNode> parents = new Stack<GenericTreeNode>();
                parents.push(root.getRoot());
                for(int k = 0; k < r.tokens.size(); k++){
                    Token t = r.tokens.get(k);
                    Token before = k - 2 >= 0?r.tokens.get(k - 2):new Token("unknown", "", t.line_num);
                    Token prev = k - 1 >= 0?r.tokens.get(k - 1):new Token("unknown", "", t.line_num);
                    Token next = k + 1 < r.tokens.size()?r.tokens.get(k + 1):new Token("unknown", "", t.line_num);
                    Token after = k + 2 < r.tokens.size()?r.tokens.get(k + 2):new Token("unknown", "", t.line_num);
                    if(k == 0 && !t.type.equals("command") && !t.type.equals("func_name")){
                        throw new ConfigCompileException("Expected command (/command) or function at start of" +
                                " command", t.line_num);
                    }
                    if(t.type.equals("opt_var_assign") || t.type.equals("opt_var_start")
                            || t.type.equals("opt_var_end")){
                        throw new ConfigCompileException("Unexpected " + t.type, t.line_num);
                    }
                    if(t.type.equals("lit") || t.type.equals("string")){
                        node.addChild(new GenericTreeNode<Construct>(t));
                    } else if(t.type.equals("variable") || t.type.equals("final_var")){
                        node.addChild(new GenericTreeNode<Construct>(new Variable(t.value, null, t.line_num)));
                        right_vars.add(new Variable(t.value, null, t.line_num));
                    } else if(t.type.equals("func_name")){
                        try {
                            GenericTreeNode<Construct> f = new GenericTreeNode<Construct>(new Function(t.value, t.line_num));
                            node.addChild(f);
                            node = f;
                            parents.push(f);
                        } catch (Exception ex) {
                            throw new ConfigCompileException("\"" + t.value + "\" is not a built in function", t.line_num);
                        }
                    } else if(t.type.equals("func_start")){
                        if(!prev.type.equals("func_name")){
                            throw new ConfigCompileException("Unexpected parenthesis", t.line_num);
                        }
                        paren_stack++;
                        continue;
                    } else if(t.type.equals("func_end")){
                        paren_stack--;
                        if(paren_stack < 0){
                            throw new ConfigCompileException("Unexpected parenthesis", t.line_num);
                        }
                        parents.pop();
                        node = parents.peek();
                    } else if(t.type.equals("command")){
                        node.addChild(new GenericTreeNode<Construct>(t));
                    } else if(t.type.equals("comma")){
                        continue;
                    } else{
                        throw new ConfigCompileException("Unexpected " + t.type + " (" + t.value + ")", t.line_num);
                    }
                }
                List<GenericTreeNode<Construct>> l = root.build(GenericTreeTraversalOrderEnum.PRE_ORDER);
                for(GenericTreeNode<Construct> n : l){
                    if(n.getData() instanceof Function){
                        int args = n.getChildren().size();
                        if(((Function)n.getData()).argTotal() != args){
                            throw new ConfigCompileException("Incorrect number of args for " +
                                    ((Function)n.getData()).toString(), ((Function)n.getData()).line_num);
                        }
                    }
                }
                //Look to see if all the variables defined on the left are used on the right
                for(Variable left_var : left_vars){
                    boolean found = false;
                    for(Variable right_var : right_vars){
                        if(left_var.name.equals(right_var.name)){
                            found = true;
                        }
                    }
                    if(!found){
                        //Just a warning in this case
                        System.err.println("Notice: Defined variable " + left_var.name + " is unused near line " + left_var.line_num);
                    }
                }
                //Look to see if all the variables on the right are defined on the left
                for(Variable right_var : right_vars){
                    boolean found = false;
                    for(Variable left_var : left_vars){
                        if(right_var.name.equals(left_var.name)){
                            found = true;
                            right_var.def = left_var.def;
                        }
                    }
                    if(!found){
                        //This is a fatal error
                        throw new ConfigCompileException("Undefined variable " + right_var.name, right_var.line_num);
                    }
                }


            }
        }
        //Compile the (now syntactically correct) left side into a ArrayList<Construct>
//        for(int i = 0; i < a.alias.size(); i++){
//            Command c = a.alias.get(i);
//            for(int j = 0; j < c.tokens.size(); j++){
//                Token t = c.tokens.get(j);
//                ArrayList<Construct> ac = a.aliasConstructs.get(i);
//                if(t.type.equals("command")){
//                    ac.add(t);
//                } else if(t.type.equals("variable")){
//                    ac.add(new Variable(t.value, null, t.line_num));
//                } else if(t.type.equals("final_var")){
//                    ac.add(new Variable(t.value, null, t.line_num));
//                } else if(t.type.equals("lit")){
//                    ac.add(t);
//                } else if(t.type.equals("opt_var_start")){
//                    if(j + 2 < c.tokens.size() && c.tokens.get(j + 2).type.equals("opt_var_assign")){
//                        Variable v = new Variable(c.tokens.get(j + 1).value,
//                                c.tokens.get(j + 3).value, t.line_num);
//                        v.optional = true;
//                        ac.add(v);
//                        j += 4;
//                    } else{
//                        t = c.tokens.get(j + 1);
//                        Variable v = new Variable(t.value, null, t.line_num);
//                        v.optional = true;
//                        ac.add(v);
//                        j += 2;
//                    }
//                }
//
//            }
//        }

        //Copy this working config file to another file so that if a bad config file is read in, at least the old
        //on can be recovered
        //TODO

        System.out.println("Config file compiled sucessfully, with " + a.alias.size() + " alias(es) defined.");
        aliasFile = a;
    }

    /**
     * This is the command that runs the magic once the config file is compiled and tucked away.
     * It takes a real command from the user and sees if it fits with any of the aliases. If it
     * does, it actually returns the commands to run. Since it's a bit more complicated than just
     * returning a string, it returns a "RunnableAlias" that has implementation specific functions
     * in it.
     * @param command
     * @return
     */
    public ArrayList<RunnableAlias> getRunnableAliases(String command, String playerName){
//        String[] cmds = command.split(" ");
//        ArrayList<String> args = new ArrayList(Arrays.asList(cmds));
//        for(int i = 0; i < this.aliasFile.alias.size(); i++){
//            ArrayList<Construct> tokens = this.aliasFile.aliasConstructs.get(i);
//            System.out.println(tokens);
//            for(int j = 0; j < tokens.size(); j++){
//                Construct t = tokens.get(j);
//                String arg = args.get(j);
//            }
//        }
//
        return null;
    }

    public class Alias{
        ArrayList<Command> alias = new ArrayList<Command>();
        ArrayList<ArrayList<Construct>> aliasConstructs = new ArrayList<ArrayList<Construct>>();
        ArrayList<ArrayList<Command>> real = new ArrayList<ArrayList<Command>>();
        HashMap<Command, ArrayList<Command>> map = new HashMap<Command, ArrayList<Command>>();
        public String toString(){
            StringBuffer b = new StringBuffer();
            for(int i = 0; i < alias.size(); i++){
                b.append("\nAlias: ");
                b.append(alias.get(i));
                for(int j = 0; j < real.get(i).size(); j++){
                    b.append("\n\t" + real.get(i).get(j));
                }
            }
            return b.toString();
        }
    }

    enum Name{
        DIE, MSG, IF, EQUALS, DATA_VALUES, PLAYER, INVALID
    }

    public class Function extends Construct{
        ArrayList<Construct> args = new ArrayList<Construct>();
        Name name;
        int line_num = 0;
        Function(String name, int line_num) throws Exception{
            ctype = ConstructType.FUNCTION;
            this.name = this.getName(name);
            if(this.name == Name.INVALID){
                throw new Exception();
            }
            this.line_num = line_num;
        }
        void addArg(Construct function){
            args.add(function);
        }

        int argTotal(){
            switch(name){
                case DIE:
                    return 1;
                case MSG:
                    return 1;
                case IF:
                    return 3;
                case EQUALS:
                    return 2;
                case DATA_VALUES:
                    return 1;
                case PLAYER:
                    return 0;
                default:
                    return 0;
            }
        }
        Name getName(String name){
            name = name.toLowerCase();
            if(name.equals("die"))
                return Name.DIE;
            if(name.equals("msg"))
                return Name.MSG;
            if(name.equals("if"))
                return Name.IF;
            if(name.equals("equals"))
                return Name.EQUALS;
            if(name.equals("data_values"))
                return Name.DATA_VALUES;
            if(name.equals("player"))
                return Name.PLAYER;
            return Name.INVALID;
        }

        public String toString(){
            return "function:" + name.toString().toLowerCase();
        }
    }
    public class Command extends Construct{
        ArrayList<Token> tokens = new ArrayList<Token>();
        public Command(ArrayList<Token> a){
            tokens = (ArrayList<Token>) a.clone();
            this.ctype = ConstructType.COMMAND;
        }
        public String toString(){
            StringBuffer b = new StringBuffer();
            for(Token t : tokens){
                b.append(t.toSimpleString() + " ");
            }
            return b.toString();
        }
    }

    public class Token extends Construct{
        public Token(String type, String value, int line_num){
            this.type = type;
            this.value = value;
            this.line_num = line_num;
            if(type.equals("string") || type.equals("lit")){
                ctype = ConstructType.ATOMIC;
            } else{
                ctype = ConstructType.TOKEN;
            }
        }
        public String toString(){
            if(type.equals("newline")){
                return "newline";
            }
            if(type.equals("string")){
                return "string:'" + value + "'";
            }
            return type + ":" + value;
        }

        public String toSimpleString(){
            if(type.equals("string")){
                return "'" + value + "'";
            }
            return value;
        }
        public String toOutputString(){
            if(type.equals("string")){
                return value.replace("'", "\\'");
            }
            return value;
        }
        String type;
        String value;
        int line_num;
    }

    public class Variable extends Construct{
        String name;
        String def;
        int line_num;
        boolean optional;
        public Variable(String name, String def, int line_num){
            this.name = name;
            this.def = def;
            this.line_num = line_num;
            ctype = ConstructType.ATOMIC;
        }

        public String toString(){
            return "var:" + name;
        }
    }

    enum ConstructType{
        TOKEN, COMMAND, FUNCTION, ATOMIC
    }
    public class Construct{
        ConstructType ctype;
    }
}
