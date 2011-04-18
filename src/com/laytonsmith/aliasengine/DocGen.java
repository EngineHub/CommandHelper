/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.functions.Function;
import com.laytonsmith.aliasengine.functions.FunctionList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Layton
 */
public class DocGen {

    public static void start(String type) {
        ArrayList<Function> functions = FunctionList.getFunctionList();
        HashMap<Class, ArrayList<Function>> functionlist = new HashMap<Class, ArrayList<Function>>();
        for (int i = 0; i < functions.size(); i++) {
            //Sort the functions into classes
            Function f = functions.get(i);
            Class apiClass = (f.getClass().getEnclosingClass() != null
                    ? f.getClass().getEnclosingClass()
                    : null);
            ArrayList<Function> fl = functionlist.get(apiClass);
            if (fl == null) {
                fl = new ArrayList<Function>();
                functionlist.put(apiClass, fl);
            }
            fl.add(f);
        }
        for (Map.Entry<Class, ArrayList<Function>> entry : functionlist.entrySet()) {
            Class apiClass = entry.getKey();
            String className = apiClass.getName().split("\\.")[apiClass.getName().split("\\.").length - 1];
            String classDocs = null;
            try {
                Method m = apiClass.getMethod("docs", (Class[]) null);
                classDocs = (String) m.invoke(null, (Object[]) null);
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            } catch (NoSuchMethodException e) {}
            if(type.equals("html")){
                if(className != null){
                    System.out.println("<h1>" + className + "</h1>");
                    System.out.println(classDocs == null?"":classDocs);
                } else {
                    System.out.println("<h1>Other Functions</h1>");
                }
                System.out.println("<table>");
            } else if(type.equals("wiki")){
                if(className != null){
                    System.out.println("===" + className + "===");
                    System.out.println(classDocs == null?"":classDocs);
                } else {
                    System.out.println("===Other Functions===");
                }
                System.out.println("{| width=\"100%\" cellspacing=\"1\" cellpadding=\"1\" border=\"1\" align=\"left\" class=\"wikitable\"\n" +
                                    "|-\n" +
                                    "! scope=\"col\" width=\"6%\" | Function Name\n" + 
                                    "! scope=\"col\" width=\"5%\" | Returns\n" + 
                                    "! scope=\"col\" width=\"15%\" | Arguments\n" + 
                                    "! scope=\"col\" width=\"69%\" | Description\n" +
                                    "! scope=\"col\" width=\"5%\" | Restricted");
            }
            for(Function f : entry.getValue()){
                String doc = f.docs();
                String ret = null;
                String args = null;
                String desc = null;
                String restricted = f.isRestricted()?"<div style=\"background-color: red; font-weight: bold; text-align: center;\">Yes</div>":
                        "<div style=\"background-color: green; font-weight: bold; text-align: center;\">No</div>";
                Pattern p = Pattern.compile("\\s*(.*?)\\s*\\{(.*?)\\}\\s*(.*)\\s*");
                Matcher m = p.matcher(doc);
                if(m.find()){
                    ret = m.group(1);
                    args = m.group(2);
                    desc = m.group(3);
                }
                if(type.equals("html")){
                    System.out.println("<tr><td>" + ret + "</td><td>" + args + "</td><td>" + desc + "</td><td>" + restricted + "</td></tr>\n");
                } else if(type.equals("wiki")){
                    System.out.println("|-\n"
                            + "! scope=\"row\" | " + f.getName() + "\n"
                            + "| " + ret + "\n"
                            + "| " + args + "\n"
                            + "| " + desc + "\n"
                            + "| " + restricted);
                   
                }
            }
            if(type.equals("html")){
                System.out.println("</table>");
            } else if(type.equals("wiki")){
                System.out.println("|}");
            }
        }
    }
}
