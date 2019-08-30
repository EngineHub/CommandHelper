package com.laytonsmith.core.packetjumper;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.constructs.Construct;
import org.bukkit.Bukkit;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 */
public class PacketJumper {

	@SuppressWarnings("FieldMayBeFinal")
	private static boolean started = false;
	private static SortedSet<PacketInfo> packetInfo;
	@SuppressWarnings("FieldMayBeFinal")
	private static Thread initializingThread = null;
	private static final String PROTOCOL_DOCS = "";

	public static void startup() {
		if(true) {
			return; //TODO:
		}
		if(started) {
			return;
		}
		packetInfo = new TreeSet<>();
		initializingThread = new Thread(new Runnable() {

			@Override
			public void run() {
//				try {
//					protocolDocs = WebUtility.GetPageContents("http://mc.kev009.com/Protocol");
//				} catch (IOException ex) {
//					Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
//				}
				String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
				try {
					Class packetClass = Class.forName("net.minecraft.server." + version + ".Packet");
					Set<Class> packets = ReflectionUtils.getAllExtensions(packetClass);
					for(Class packet : packets) {
						packetInfo.add(new PacketInfo(packet));
					}
					started = true;
				} catch (ClassNotFoundException ex) {
					StreamUtils.GetSystemOut().println("[CommandHelper] Failed to find Minecraft Packet classes.");
				}
			}
		}, "PacketJumperInitializer");
		initializingThread.start();
		for(PacketInfo p : getPacketInfo()) {
			StreamUtils.GetSystemOut().println(p);
		}
	}

	public static boolean started() {
		return started;
	}

	private static void waitForInitialization() throws InterruptedException {
		if(initializingThread == null) {
			startup();
		}
		if(initializingThread.isAlive()) {
			//Wait for the startup thread, if it's running
			initializingThread.join();
		}
	}

	public static Set<PacketInfo> getPacketInfo() {
		try {
			waitForInitialization();
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
		}
		return new TreeSet<>(packetInfo);
	}

	public static void fakePacketToPlayer(MCPlayer player, PacketInstance packet) {
		try {
			waitForInitialization();
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
		}
		//TODO:
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public static void fakePacketFromPlayer(MCPlayer player, PacketInstance packet) {
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
	public static void setPacketReceivedInterceptor(int id, PacketHandler handler) {
		try {
			waitForInitialization();
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
		}
		//TODO:
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public static void setPacketSentInterceptor(int id, PacketHandler handler) {
		try {
			waitForInitialization();
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
		}
		//TODO:
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public static PacketInstance getPacket(int id, Construct... args) {
		try {
			waitForInitialization();
		} catch (InterruptedException ex) {
			Logger.getLogger(PacketJumper.class.getName()).log(Level.SEVERE, null, ex);
		}
		//TODO:
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Used for packet interceptors, this allows an opportunity for a class to manipulate a packet before it is
	 * processed/sent
	 */
	public static interface PacketHandler {

		/**
		 * The packet to be processed/sent is passed to this method, and it is expected that this method returns a
		 * packet (which is actually going to be sent) or null, which cancels the packet send entirely.
		 *
		 * @param player The player sending/recieving the packet
		 * @param packet The packet in question
		 * @return
		 */
		PacketInstance Handle(MCPlayer player, PacketInstance packet);
	}
}
