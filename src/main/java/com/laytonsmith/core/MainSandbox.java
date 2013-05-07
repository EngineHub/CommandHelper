package com.laytonsmith.core;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.annotations.WrappedItem;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.testing.AbstractConstructor;
import com.laytonsmith.annotations.testing.MustOverride;
import com.laytonsmith.annotations.testing.SubclassesMustHaveAnnotation;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.functions.Regex;
import com.laytonsmith.core.functions.StringHandling;
import com.laytonsmith.database.DB;
import com.laytonsmith.database.MySQL;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftCreeper;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftWolf;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;

/**
 * This class is for testing concepts
 *
 * @author Layton
 */
public class MainSandbox {

	public static void main(String[] argv) throws Exception {
		//URI information
//		String[] uris = new String[]{"yml:user@remote:22:abcd:path/to/remote/file"
//		,"yml:user@remote:22:/path/to/remote/file", "yml:user@remote:/path/to/remote/file"};
//		for (String s : uris) {
//			URI uri = new URI(s);
//			System.out.println("For the URI " + uri.toString() + ", the following are set:");
//			System.out.println("Scheme: " + uri.getScheme());
//			System.out.println("Scheme specific part: " + uri.getSchemeSpecificPart());
//			System.out.println("Authority: " + uri.getAuthority());
//			System.out.println("User info: " + uri.getUserInfo());
//			System.out.println("Host: " + uri.getHost());
//			System.out.println("Port: " + uri.getPort());
//			System.out.println("Path: " + uri.getPath());
//			System.out.println("Query: " + uri.getQuery());
//			System.out.println("Fragment: " + uri.getFragment());
//			System.out.println("\n\n***********************************\n\n");
//		}
		
		//Execution queue usage
//		final ExecutionQueue queue = new ExecutionQueue("Test", "default");
//		for(int i = 0; i < 3; i++){
//			final int j = i;
//			new Thread(new Runnable() {
//
//				public void run() {
//					for(int i = 0; i < 10; i++){
//						final int k = i;
//						queue.push("queue-" + j, new Runnable(){
//
//							public void run() {
//								String space = "";
//								switch(j){
//									case 1: space = "     "; break;
//									case 2: space = "          "; break;
//								}
//								System.err.println("In Queue " + space + j + ": " + k);
//							}
//						});
//					}
//					System.err.println("Finished queueing up events for queue " + j);
//				}
//			}, "Thread-" + i).start();
//		}
		
		//Profiler usage
//		Profiler p = new Profiler(new File("profiler.config"));
//		p.doLog("Starting profiling");
//		ProfilePoint ProfilerTop = p.start("Profiler Top", LogLevel.ERROR);
//		ProfilePoint ProfilerMiddle = p.start("Profiler Middle", LogLevel.WARNING);
//		ProfilePoint ProfilerBottom = p.start("Profiler Bottom", LogLevel.INFO);
//		ProfilePoint InnerMost1 = p.start("InnerMost1", LogLevel.DEBUG);
//		ProfilePoint InnerMost2 = p.start("InnerMost2", LogLevel.VERBOSE);
////		System.gc();
//		p.stop(InnerMost2);
//		p.stop(InnerMost1);
//		p.stop(ProfilerBottom);
//		p.stop(ProfilerMiddle);
//		p.stop(ProfilerTop);
		
		//Profiling reg_split vs split
//		String toSplit = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z";
//		Regex.reg_split regSplitFunction = new Regex.reg_split();
//		StringHandling.split splitFunction = new StringHandling.split();
//		Target t = Target.UNKNOWN;
//		int times = 1000000;
//		
//		CString subject = new CString(toSplit, t);
//		CString pattern = new CString(",", t);
//		
//		long startRegSplit = System.currentTimeMillis();
//		{
//			for(int i = 0; i < times; i++){
//				regSplitFunction.exec(t, null, pattern, subject);
//			}
//		}
//		long stopRegSplit = System.currentTimeMillis();
//		long startSplit = System.currentTimeMillis();
//		{
//			for(int i = 0; i < times; i++){
//				splitFunction.exec(t, null, subject, pattern);
//			}
//		}
//		long stopSplit = System.currentTimeMillis();
//		
//		System.out.println("reg_split took " + (stopRegSplit - startRegSplit) + "ms under " + times + " iterations.");
//		System.out.println("split took " + (stopSplit - startSplit) + "ms under " + times + " iterations.");
//		InetAddress i1 = InetAddress.getByName("173.194.37.72");
//		System.out.println("i1.toString(): " + i1.toString());
//		System.out.println("i1.getHostAddress(): " + i1.getHostAddress());
//		InetAddress i2 = InetAddress.getByName("173.194.37.72");
//		System.out.println("i2.toString(): " + i2.toString());
//		System.out.println("i2.getHostAddress(): " + i2.getHostAddress());
//		System.out.println("i2.getHostName(): " + i2.getHostName());
//		System.out.println("i2.toString(): " + i2.toString());
//		System.out.println("i2.getHostAddress(): " + i2.getHostAddress());
		
//		DB.CConnection conn = DB.CConnection.GetConnection(DB.SupportedDBConnectors.MYSQL, "localhost", "test", 3306, "", "");
//		DB db = new MySQL();
//		db.connect(conn);
//		Object o = db.query("SHOW TABLES;");
//		if(o instanceof ResultSet){
//			ResultSet rs = (ResultSet)o;
//		}
//		System.out.println(o);
		
		System.out.println(AbstractionUtils.doLookup(Creeper.class).getName());
	}

	
}
