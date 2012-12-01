/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.packetjumper;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.ReflectionUtils;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.PureUtilities.WebUtility;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.constructs.Construct;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

/**
 *
 * @author Layton
 */
public class PacketJumper {
	
	private static boolean started = false;
	private static SortedSet<PacketInfo> packetInfo;
	private static Thread initializingThread = null;
	private static String protocolDocs = "";
	public static void startup(){
		if(true) return; //TODO:
		if(started){
			return;
		}
		initializingThread = new Thread(new Runnable() {

			public void run() {
				try {
					protocolDocs = WebUtility.GetPageContents("http://mc.kev009.com/Protocol");
				} catch (IOException ex) {
					Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
				}
				//Get all the classloaders. Our classloader must be an instanceof the bukkit classloader for this to work though
				Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
				ClassLoader l = PacketJumper.class.getClassLoader();
				if(l instanceof org.bukkit.plugin.java.PluginClassLoader){
					//Perfect.
					JavaPluginLoader loader = (JavaPluginLoader)ReflectionUtils.get(PluginClassLoader.class, l, "loader");
					Map<String, PluginClassLoader> loaders = (Map<String, PluginClassLoader>)ReflectionUtils.get(JavaPluginLoader.class, loader, "loaders");
					//We have to be careful here, because we could get circular references
					classLoaders.add(l);
					for(PluginClassLoader ll : loaders.values()){
						if(!classLoaders.contains(ll)){
							//It's not added, so add it
							classLoaders.add(ll);					
						}
					}
				}
				for(String url : ClassDiscovery.GetKnownPackageHierarchies(classLoaders)){
					ClassDiscovery.InstallDiscoveryLocation(url);
				}

				Class[] packets = ClassDiscovery.GetAllClassesOfSubtype(Packet.class, classLoaders);
				packetInfo = new TreeSet<PacketInfo>();
				for(Class packet : packets){
					packetInfo.add(new PacketInfo(packet));
				}
			}
		}, "PacketJumperInitializer");
		initializingThread.start();
		for(PacketInfo p : getPacketInfo()){
			System.out.println(p);
		}
	}
	
	public static boolean started(){
		return started;
	}
	
	private static void waitForInitialization() throws InterruptedException{
		if(initializingThread == null){
			startup();
		}
		if(initializingThread.isAlive()){
			//Wait for the startup thread, if it's running
			initializingThread.join();
		}
	}
	
	public static Set<PacketInfo> getPacketInfo(){
		try {
			waitForInitialization();
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
		}
		return new TreeSet<PacketInfo>(packetInfo);
	}
	
	public static void fakePacketToPlayer(MCPlayer player, Packet packet){
		try {
			waitForInitialization();
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
		}
		//TODO:
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static void fakePacketFromPlayer(MCPlayer player, Packet packet){
		try {
			waitForInitialization();
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
		}
		//TODO:
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	//TODO: Add interceptor listeners, and make this support more than one. It should
	//probably support binds even.
	
	public static void setPacketRecievedInterceptor(int id, PacketHandler handler){
		try {
			waitForInitialization();
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
		}
		//TODO:
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static void setPacketSentInterceptor(int id, PacketHandler handler){
		try {
			waitForInitialization();
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
		}
		//TODO:
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static Packet getPacket(int id, Construct ... args){
		try {
			waitForInitialization();
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
		}
		//TODO:
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static class PacketInfo implements Comparable<PacketInfo> {
		private final int packetID;
		private final String packetName;
		private final Class<? extends Packet> packetClass;
		private final Constructor<? extends Packet>[] constructors;
		private final Class[][] arguments;
		private final String docs;

		public PacketInfo(Class<? extends Packet> packetClass) {
			this.packetClass = packetClass;
			constructors = (Constructor<? extends Packet>[])packetClass.getConstructors();
			arguments = new Class[constructors.length][];
			for(int i = 0; i < constructors.length; i++){
				Constructor<? extends Packet> constructor = constructors[i];
				arguments[i] = constructor.getParameterTypes();
			}
			packetID = Integer.parseInt(packetClass.getSimpleName().replaceAll(".*?(\\d+).*", "$1"));
			packetName = packetClass.getSimpleName().replaceAll(".*\\d+(.*)", "$1");
			//TODO: Parse docs
			docs = "Docs coming soon!";
		}

		public int getPacketID() {
			return packetID;
		}

		public String getPacketName() {
			return packetName;
		}

		public Class<? extends Packet> getPacketClass() {
			return packetClass;
		}

		public Constructor<? extends Packet>[] getConstructors() {
			return constructors;
		}

		public Class[] getArguments() {
			Class[] a = new Class[arguments.length];
			System.arraycopy(arguments, 0, a, 0, arguments.length);
			return a;
		}

		public String getDocs() {
			return docs;
		}				

		@Override
		public String toString() {
			List<String> l = new ArrayList<String>();
			for(Class[] args : arguments){
				l.add(StringUtils.Join(args, ", "));
			}
			return packetClass.getName() + "(" + StringUtils.Join(l, " | ") + ")";
		}

		public int compareTo(PacketInfo o) {
			if(this.packetID == o.packetID){
				return 0;
			}
			return this.packetID < o.packetID?-1:1;
		}
		
	}
	
	/**
	 * Used for packet interceptors, this allows an opportunity for a class to
	 * manipulate a packet before it is processed/sent
	 */
	public static interface PacketHandler{
		/**
		 * The packet to be processed/sent is passed to this method, and it is expected
		 * that this method returns a packet (which is actually going to be sent) or
		 * null, which cancels the packet send entirely.
		 * @param player The player sending/recieving the packet
		 * @param packet The packet in question
		 * @return 
		 */
		Packet Handle(MCPlayer player, Packet packet);
	}
}
