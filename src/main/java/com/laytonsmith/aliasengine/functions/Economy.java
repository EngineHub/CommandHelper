/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.CDouble;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;
import com.nijikokun.register.payment.Methods;
import java.io.File;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class Economy {
    
    public static String docs(){
        return "Provides functions to hook into the server's economy plugin";
    }
    
    @api public static class acc_balance implements Function{

        public String getName() {
            return "acc_balance";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "double {account_name} Returns the balance of the given account name.";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.InvalidPluginException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.2.0";
        }

        public Boolean runAsync() {
            return null;
        }

        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
            MethodAccount ma = GetAccount(this.getName(), line_num, f, args);
            return new CDouble(ma.balance(), line_num, f);
        }
        
    }
//    @api public static class acc_set implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class acc_add implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class acc_subtract implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class acc_multiply implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class acc_divide implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class acc_remove implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class bacc_balance implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class bacc_set implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class bacc_add implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class bacc_subtract implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class bacc_multiply implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class bacc_divide implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    @api public static class bacc_remove implements Function{
//
//        public String getName() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Integer[] numArgs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String docs() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ExceptionType[] thrown() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean isRestricted() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void varList(IVariableList varList) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean preResolveVariables() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public String since() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Boolean runAsync() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Construct exec(int line_num, File f, Player p, Construct... args) throws ConfigRuntimeException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
    
    public static Method GetMethod(int line_num, File file){
        com.nijikokun.register.payment.Methods m = new Methods();
        if(m.getMethod() == null){
            throw new ConfigRuntimeException("", ExceptionType.InvalidPluginException, line_num, file);
        } else {
            return m.getMethod();
        }
    }
    
    public static MethodAccount GetAccount(String fname, int line_num, File file, Construct ... args){
        String name = args[0].val();
        MethodAccount m = GetMethod(line_num, file).getAccount(name);
        if(m == null){
            throw new ConfigRuntimeException("", ExceptionType.PluginInternalException, line_num, file);
        } else {
            return m;
        }
    }

}
