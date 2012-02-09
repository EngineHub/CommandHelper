/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.SerializedPersistance;
import static com.laytonsmith.PureUtilities.TermColors.*;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.MScriptCompiler;
import com.laytonsmith.core.MScriptComplete;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import static com.laytonsmith.tools.Manager.p;
import static com.laytonsmith.tools.Manager.pl;
import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * This is a command line implementation of the in game interpreter mode.
 * This should only be run while the server is stopped, as it has full
 * access to filesystem resources. Many things won't work as intended, but
 * pure abstract functions should still work fine.
 * @author layton
 */
public class Interpreter {
    static boolean multilineMode = false;
    static String script;

    public static void start(){
        try {
            MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex("player()", null)), new Env(), null, null);
        } catch (ConfigCompileException ex) {}
        CommandHelperPlugin.persist = new SerializedPersistance(new File("CommandHelper/persistance.ser"));
        pl(YELLOW + "You are now in cmdline interpreter mode. Type a dash (-) on a line by itself to exit, and >>> to enter"
                + " multiline mode.\nMost Minecraft features will not work, and your working directory is the"
                + " CommandHelper.jar directory, not the Server directory. Have fun!");
        Scanner scanner = new Scanner(System.in);
        p(BLUE + ":" + WHITE);
        while(textLine(scanner.nextLine())){
            p(BLUE + ":" + WHITE);
        }
    }

    public static boolean textLine(String line) {
        if (line.equals("-")) {
            //Exit interpreter mode
            pl(YELLOW + "Now exiting interpreter mode");
            return false;
        } else if (line.equals(">>>")) {
            //Start multiline mode
            if (multilineMode) {
                pl(RED + "You are already in multiline mode!");
            } else {
                multilineMode = true;
                pl(YELLOW + "You are now in multiline mode. Type <<< on a line by itself to execute.");
                pl(":" + WHITE + ">>>");
            }
        } else if (line.equals("<<<")) {
            //Execute multiline
            pl(":" + WHITE + "<<<");
            multilineMode = false;
            try {
                execute(script);
                script = "";
            } catch (ConfigCompileException e) {
                pl(RED + e.getMessage() + ":" + e.getLineNum());
            }
        } else {
            if (multilineMode) {
                //Queue multiline
                script = script + line + "\n";
                pl(":" + WHITE + line);
            } else {
                try {
                    //Execute single line
                    execute(line);
                } catch (ConfigCompileException ex) {
                    pl(RED + ex.getMessage());
                }
            }
        }
        return true;
    }

    public static void execute(String script) throws ConfigCompileException {
        List<Token> stream = MScriptCompiler.lex(script, new File("Interpreter"));
        GenericTreeNode tree = MScriptCompiler.compile(stream);
        Env env = new Env();
        env.SetPlayer(null);
        env.SetLabel("*");
        try {
            MScriptCompiler.execute(tree, env, new MScriptComplete() {

                public void done(String output) {
                    output = output.trim();
                    if (output.equals("")) {
                        pl(":");
                    } else {
                        if (output.startsWith("/")) {
                            //Run the command
                            pl(":" + YELLOW + output);
                        } else {
                            //output the results
                            pl(":" + GREEN + output);
                        }
                    }
                }
            }, null);
        } catch (CancelCommandException e) {
            pl(":");
        } catch(Exception e){
            pl(RED + e.toString());
            e.printStackTrace();
        }
    }
}
