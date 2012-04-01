/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.testing;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.*;
import com.laytonsmith.core.exceptions.*;
import com.laytonsmith.core.functions.BasicLogic.equals;
import com.laytonsmith.core.functions.Function;
import com.sk89q.wepif.PermissionsResolverManager;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.plugin.PluginDescriptionFile;
import static org.junit.Assert.fail;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * 
 * @author Layton
 */
@RunWith(PowerMockRunner.class)

public class StaticTest {
    /**
     * Tests the boilerplate functions in a Function. While all functions should conform to
     * at least this, it is useful to also use the more strict TestBoilerplate function.
     * @param f 
     */
    public static void TestBoilerplate(Function f, String name) {
        //For the "quality test code coverage" number, set this to true
        boolean runQualityTestsOnly = false;

        MCPlayer fakePlayer = Mockito.mock(MCPlayer.class);
        MCServer fakeServer = Mockito.mock(MCServer.class);
        MCWorld fakeWorld = Mockito.mock(MCWorld.class);
        Mockito.when(fakePlayer.getServer()).thenReturn(fakeServer);
        Mockito.when(fakePlayer.getWorld()).thenReturn(fakeWorld);
        //make sure that these functions don't throw an exception. Any other results
        //are fine
        f.isRestricted();
        f.runAsync();
        f.preResolveVariables();
        f.thrown();

        //name should match the given value
        if (!f.getName().equals(name)) {
            fail("Expected name of function to be " + name + ", but was given " + f.getName());
        }

        //docs needs to at least be more than a non-empty string, though in the future this should follow a more strict 
        //requirement set.
        if (f.docs().length() <= 0) {
            fail("docs must return a non-empty string");
        }

        TestDocs(f);


        //if creating a version string from this yeilds bogus data, it will throw an
        //exception for us.
        if (f.since().getVersionString().length() <= 0) {
            Version v = f.since().getVersion();
        }
        if (f.numArgs().length == 0) {
            fail("numArgs must return an Integer array with more than zero values");
        }

        //If we want a "quality test coverage" number, we can't run this section, because it bombards the code
        //with random data to see if it fails in expected ways (to simulate how a user could run the scripts)
        //If we are interested in tests that are specific to the functions however, we shouldn't run this.
        if (!runQualityTestsOnly) {
            TestExec(f, fakePlayer);
            TestExec(f, null);
            TestExec(f, StaticTest.GetFakeConsoleCommandSender());
        }
        
        //Let's make sure that if execs is defined in the class, useSpecialExec returns true.
        for(Method method : f.getClass().getDeclaredMethods()){
            if(method.getName().equals("execs")){
                if(!f.useSpecialExec()){
                    fail(f.getName() + " declares execs, but returns false for useSpecialExec.");
                }
            }
        }

        //now the only function left to test is exec. This cannot be abstracted, unfortunately.
    }

    /**
     * Checks to see if the documentation follows the specified format
     */
    public static void TestDocs(Function f) {
        //TODO
    }

    public static void TestExec(Function f, MCCommandSender p) {
        Env env = new Env();
        env.SetCommandSender(p);
        //See if the function throws something other than a ConfigRuntimeException or CancelCommandException if we send it bad arguments,
        //keeping in mind of course, that it isn't supposed to be able to accept the wrong number of arguments. Specifically, we want to try
        //strings, numbers, arrays, and nulls
        for (Integer i : f.numArgs()) {
            if (i == Integer.MAX_VALUE) {
                //er.. let's just try with 100...
                i = 100;
            }
            Construct[] con = new Construct[i];
            //Throw the book at it. Most functions will fail, and that is ok, what isn't
            //ok is if it throws an unexpected type of exception. It should only ever
            //throw a ConfigRuntimeException, or a CancelCommandException. Further,
            //if it throws a ConfigRuntimeException, the documentation should state so.
            for (int z = 0; z < 11; z++) {
                for (int a = 0; a < i; a++) {
                    switch (z) {
                        case 0:
                            con[a] = C.onstruct("hi");
                            break;
                        case 1:
                            con[a] = C.onstruct(1);
                            break;
                        case 2:
                            con[a] = C.Array(C.onstruct("hi"), C.onstruct(1));
                            break;
                        case 3:
                            con[a] = C.Null();
                            break;
                        case 4:
                            con[a] = C.onstruct(-1);
                            break;
                        case 5:
                            con[a] = C.onstruct(0);
                        case 6:
                            con[a] = C.onstruct(90000);
                        case 7:
                            con[a] = C.onstruct(a);
                        case 8:
                            con[a] = C.onstruct(new Random(System.currentTimeMillis()).nextDouble());
                        case 9:
                            con[a] = C.onstruct(new Random(System.currentTimeMillis()).nextInt());
                        case 10:
                            con[a] = C.onstruct(new Random(System.currentTimeMillis()).nextBoolean());
                    }
                }
                try {
                    f.exec(Target.UNKNOWN, env, con);
                } catch (CancelCommandException e) {
                } catch (ConfigRuntimeException e) {
                    if (e.getExceptionType() != null) {
                        if (f.thrown() == null || !Arrays.asList(f.thrown()).contains(e.getExceptionType())) {
                            fail("The documentation for " + f.getName() + " doesn't state that it can throw a "
                                    + e.getExceptionType() + ", but it did.");
                        }
                    } //else it's uncatchable, which while it probably shouldn't happen often, it can.
                } catch (Throwable e) {
                    if (e instanceof LoopBreakException && !f.getName().equals("break")) {
                        fail("Only break() can throw LoopBreakExceptions");
                    }
                    if (e instanceof LoopContinueException && !f.getName().equals("continue")) {
                        fail("Only continue() can throw LoopContinueExceptions");
                    }
                    if (e instanceof FunctionReturnException && !f.getName().equals("return")) {
                        fail("Only return() can throw FunctionReturnExceptions");
                    }
                }
            }
        }
    }

    public static void TestClassDocs(String docs, Class container) {
        if (docs.length() <= 0) {
            fail("The docs for the " + container.getSimpleName() + " class are missing");
        }
    }

    /**
     * Gets the value out of s construct, ignoring information like line numbers.
     * @return 
     */
    public static Object Val(Construct c) {
        return c.val();
    }

    /**
     * Checks to see if two constructs are equal, using the same method that MethodScript equals() uses. In
     * fact, this method depends on equals() working, as it actually uses the function.
     * @param expected
     * @param actual 
     */
    public static void assertCEquals(Construct expected, Construct actual) throws CancelCommandException {
        equals e = new equals();
        CBoolean ret = (CBoolean) e.exec(Target.UNKNOWN, null, expected, actual);
        if (ret.getBoolean() == false) {
            throw new AssertionError("Expected " + expected + " and " + actual + " to be equal to each other");
        }
    }

    /**
     * Does the opposite of assertCEquals
     * @param expected
     * @param actual
     * @throws CancelCommandException 
     */
    public static void assertCNotEquals(Construct expected, Construct actual) throws CancelCommandException {
        equals e = new equals();
        CBoolean ret = (CBoolean) e.exec(Target.UNKNOWN, null, expected, actual);
        if (ret.getBoolean() == true) {
            throw new AssertionError("Did not expect " + expected + " and " + actual + " to be equal to each other");
        }
    }

    /**
     * Verifies that the given construct <em>resolves</em> to true. The resolution uses Static.getBoolean to
     * do the resolution.
     * @param actual 
     */
    public static void assertCTrue(Construct actual) {
        if (!Static.getBoolean(actual)) {
            fail("Expected '" + actual.val() + "' to resolve to true, but it did not");
        }
    }

    /**
     * Verifies that the given construct <em>resolves</em> to false. The resolution uses Static.getBoolean to
     * do the resolution.
     * @param actual 
     */
    public static void assertCFalse(Construct actual) {
        if (Static.getBoolean(actual)) {
            fail("Expected '" + actual.val() + "' to resolve to false, but it did not");
        }
    }

    /**
     * This function is used to assert that the type of a construct is one of the specified types.
     * @param test
     * @param retTypes 
     */
    public static void assertReturn(Construct test, Class... retTypes) {
        if (!Arrays.asList(retTypes).contains(test.getClass())) {
            StringBuilder b = new StringBuilder();
            if (retTypes.length == 1) {
                b.append("Expected return type to be ").append(retTypes[0].getSimpleName()).append(", but found ").append(test.getClass().getSimpleName());
            } else if (retTypes.length == 2) {
                b.append("Expected return type to be either ").append(retTypes[0].getSimpleName()).append(" or ").append(retTypes[1].getSimpleName()).append(", but found ").append(test.getClass().getSimpleName());
            } else {
                b.append("Expected return type to be one of: ");
                for (int i = 0; i < retTypes.length; i++) {
                    if (i < retTypes.length - 1) {
                        b.append(retTypes[i].getSimpleName()).append(", ");
                    } else {
                        b.append("or ").append(retTypes[i].getSimpleName());
                    }
                }
                b.append(", but found ").append(test.getClass().getSimpleName());
            }
            throw new AssertionError(b);
        }
    }

    public static List<Token> tokens(Token... array) {
        List<Token> tokens = new ArrayList<Token>();
        for (Token t : array) {
            tokens.add(t);
        }
        return tokens;
    }
    
    public static MCPlayer GetOnlinePlayer(){
        MCServer s = GetFakeServer();
        return GetOnlinePlayer("wraithguard01", s);
    }
    
    public static MCPlayer GetOnlinePlayer(MCServer s){
        return GetOnlinePlayer("wraithguard01", s);
    }
    
    public static MCPlayer GetOnlinePlayer(String name, MCServer s){
        MCPlayer p = mock(MCPlayer.class);
        when(p.isOnline()).thenReturn(true);
        when(p.getName()).thenReturn(name);        
        when(p.getServer()).thenReturn(s); 
        when(p.instanceofPlayer()).thenReturn(true);
        when(p.isOp()).thenReturn(true);
        if(s != null && s.getOnlinePlayers() != null){
            List<MCPlayer> online = new ArrayList<MCPlayer>(Arrays.asList(s.getOnlinePlayers()));
            boolean alreadyOnline = false;
            for(MCPlayer o : online){
                if(o.getName().equals(name)){
                    alreadyOnline = true;
                    break;
                }
            }
            if(!alreadyOnline){
                online.add(p);
                when(s.getOnlinePlayers()).thenReturn(online.toArray(new MCPlayer[]{}));
            }            
        }
        return p;
    }
    
    public static MCPlayer GetOp(String name, MCServer s){
        MCPlayer p = GetOnlinePlayer(name, s);
        when(p.isOp()).thenReturn(true);
        return p;
    }
    
    public static BukkitMCWorld GetWorld(String name){
        BukkitMCWorld w = mock(BukkitMCWorld.class);
        when(w.getName()).thenReturn(name);
        return w;
    }
    
    public static MCConsoleCommandSender GetFakeConsoleCommandSender(){
        MCConsoleCommandSender c = mock(MCConsoleCommandSender.class);
        MCServer s = GetFakeServer();
        when(c.getServer()).thenReturn(s);
        when(c.instanceofMCConsoleCommandSender()).thenReturn(true);
        return c;
    }
    
    public static MCLocation GetFakeLocation(MCWorld w, double x, double y, double z){
        MCLocation loc = mock(BukkitMCLocation.class);
        World bukkitWorld = mock(World.class);
        when(loc.getWorld()).thenReturn(w);
        return loc;
    }
    
    public static Object GetVariable(Object instance, String var) throws Exception{
        return GetVariable(instance.getClass(), var, instance);
    }
    public static Object GetVariable(Class c, String var, Object instance) throws Exception{
        Field f = c.getField(var);
        f.setAccessible(true);
        return f.get(instance);
    }
    
    /**
     * Lexes, compiles, and runs a given MethodScript, using the given player.
     * @param script
     * @param player
     * @throws ConfigCompileException 
     */
    public static void Run(String script, MCCommandSender player) throws ConfigCompileException{
        Run(script, player, null);
    }
    
    public static void Run(String script, MCCommandSender player, MethodScriptComplete done) throws ConfigCompileException{
        Env env = new Env();
        env.SetCommandSender(player);
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, done, null);
    }
    
    public static String SRun(String script, MCCommandSender player) throws ConfigCompileException{
        final StringBuffer b = new StringBuffer();
        Run(script, player, new MethodScriptComplete() {

            public void done(String output) {
                b.append(output);
            }
        });
        return b.toString();
    }
    //TODO: Fix this
//    public static void RunVars(List<Variable> vars, String script, MCCommandSender player) throws ConfigCompileException{
//        Env env = new Env();
//        env.SetCommandSender(player);
//        MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null));
//        injectAliasCore();
//        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(script, null), env).get(0);        
//        s.compile();
//        s.run(vars, env, null);
//        
//    }
    
    //Blarg. Dumb thing.
//    private static void injectAliasCore() throws ConfigCompileException{
//        PermissionsResolverManager prm = mock(PermissionsResolverManager.class);
//        CommandHelperPlugin chp = mock(CommandHelperPlugin.class);
//        AliasCore ac = new AliasCore(new File("plugins/CommandHelper/config.txt"), 
//                new File("plugins/CommandHelper/LocalPackages"), 
//                new File("plugins/CommandHelper/preferences.txt"), 
//                new File("plugins/CommandHelper/main.ms"), prm, chp);
//        try{
//            Field aliasCore = CommandHelperPlugin.class.getDeclaredField("ac");
//            aliasCore.setAccessible(true);
//            aliasCore.set(null, ac);
//        } catch(Exception e){
//            throw new RuntimeException("Core could not be injected", e);
//        }
//    }
    
    /**
     * Creates an entire fake server environment, adding players and everything.
     */
    public static MCServer GetFakeServer(){
        MCServer fakeServer = mock(MCServer.class);
        String [] pnames = new String[]{"wraithguard01", "wraithguard02", "wraithguard03"};
        ArrayList<MCPlayer> pps = new ArrayList<MCPlayer>();
        for(String p : pnames){
            MCPlayer pp = GetOnlinePlayer(p, fakeServer);
            pps.add(pp);
        }
        when(fakeServer.getOnlinePlayers()).thenReturn(pps.toArray(new MCPlayer[]{}));  
        CommandHelperPlugin.myServer = fakeServer;  
        CommandHelperPlugin.perms = mock(PermissionsResolverManager.class);
        return fakeServer;
    }

//    public static void StartServer() {
//        Class[] classes = new Class[]{};
//        MCServer fakeServer = GetFakeServer();
//        try{
//            Convertor c = mock(Convertor.class);
//            when(c.GetServer()).thenReturn(fakeServer);
//            Field f = StaticLayer.class.getDeclaredField("convertor");
//            f.setAccessible(true);
//            f.set(null, c);
//        } catch(Exception e){
//            fail("Could not set up reflective sections of the test code.");
//        }
//        PowerMockito.mockStatic(PermissionsResolverManager.class);
//        PermissionsResolverManager prm = mock(PermissionsResolverManager.class);        
//        when(PermissionsResolverManager.getInstance()).thenReturn(prm);
//        CommandHelperPlugin chp = spy(new CommandHelperPlugin());
//        PluginDescriptionFile pdf = PowerMockito.mock(PluginDescriptionFile.class);
//        when(pdf.getVersion()).thenReturn("0.0.0");
//        when(chp.getDescription()).thenReturn(pdf);       
//        //when(chp.getServer()).thenReturn(fakeServer);
//        chp.onLoad();
//    }
    
    /**
     * Installs the fake convertor into the server, so event based calls will
     * work. Additionally, adds the fakePlayer to the server, if player based
     * events are to be called, this is the player returned.
     * @param fakePlayer 
     */
    public static void InstallFakeConvertor(MCPlayer fakePlayer){
        try {
            //We need to add the test directory to the ClassDiscovery path
            //This should probably not be hard coded at some point.
            ClassDiscovery.InstallDiscoveryLocation(new File("./src/test/java").toURI().toURL().toString());
        }
        catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        System.exit(1010101);
        Implementation.setServerType(Implementation.Type.TEST);
        MCServer fakeServer = GetFakeServer();
        TestConvertor.fakeServer = fakeServer;
        FakeServerMixin.fakePlayer = fakePlayer;
        CommandHelperPlugin chp = mock(CommandHelperPlugin.class);
        EventList.Startup(chp);
//        try{
//            Convertor c = mock(Convertor.class);
//            when(c.GetServer()).thenReturn(fakeServer);
//            when(c.GetServerEventMixin()).thenReturn(FakeServerMixin.class);
//            Field f = StaticLayer.class.getDeclaredField("convertor");
//            f.setAccessible(true);
//            f.set(null, c);
//        } catch(Exception e){
//            fail("Could not set up reflective sections of the test code.");
//        }
        
    }
    
    @convert(type=Implementation.Type.TEST)
    public static class TestConvertor implements Convertor{
        
        private static MCServer fakeServer;

        public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
            return StaticTest.GetFakeLocation(w, x, y, z);
        }

        public Class GetServerEventMixin() {
            return FakeServerMixin.class;
        }

        public MCEnchantment[] GetEnchantmentValues() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MCEnchantment GetEnchantmentByName(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MCServer GetServer() {
            return fakeServer;
        }

        public MCItemStack GetItemStack(int type, int qty) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void Startup(CommandHelperPlugin chp) {
            //Nothing.
        }

        public int LookupItemId(String materialName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String LookupMaterialName(int id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MCItemStack GetItemStack(int type, byte data, int qty) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int SetFutureRunnable(long ms, Runnable r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void ClearAllRunnables() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void ClearFutureRunnable(int id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int SetFutureRepeater(long ms, long initialDelay, Runnable r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    public static class FakeServerMixin implements EventMixinInterface{
        
        public static MCPlayer fakePlayer;

        public void cancel(BindableEvent e, boolean state) {
            
        }

        public boolean isCancellable(BindableEvent o) {
            return true;
        }

        public Map<String, Construct> evaluate_helper(BindableEvent e) throws EventException {
            Map<String, Construct> map = new HashMap<String, Construct>();
            if(fakePlayer != null){
                map.put("player", new CString(fakePlayer.getName(), Target.UNKNOWN));
            }
            return map;
        }

        public void manualTrigger(BindableEvent e) {
            throw new RuntimeException("Manual triggering is not supported in tests yet");
        }
        
    }
    
}
