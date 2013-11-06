package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.entities.MCPlayer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lsmith
 */
public abstract class MCPluginMeta {
	
	private List<String> openOutgoingChannels = new ArrayList<String>();
	private List<String> openIncomingChannels = new ArrayList<String>();
	protected MCPluginMeta(){
		StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

			public void run() {
				List<String> copyOutgoing = new ArrayList<String>(openOutgoingChannels);
				for(String s : copyOutgoing){
					closeOutgoingChannel(s);
				}
				List<String> copyIncoming = new ArrayList<String>(openIncomingChannels);
				for(String s : copyIncoming){
					closeIncomingChannel(s);
				}
			}
		});
	}
	
	
	public void closeOutgoingChannel(String channel){
		if(openOutgoingChannels.contains(channel)){
			closeOutgoingChannel0(channel);
			openOutgoingChannels.remove(channel);
		}
	}
	
	public void openOutgoingChannel(String channel){
		if(!openOutgoingChannels.contains(channel)){
			openOutgoingChannel0(channel);
			openOutgoingChannels.add(channel);
		}
	}
	
	public void registerChannelListener(String channel, PluginMessageListener listener){
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	protected void triggerOnMessage(MCPlayer player, String channel, byte[] message){
		//TODO
	}
	
	public abstract void closeOutgoingChannel0(String channel);
	public abstract void openOutgoingChannel0(String channel);
	
	public void closeIncomingChannel(String channel){
		if(openIncomingChannels.contains(channel)){
			closeIncomingChannel0(channel);
			openIncomingChannels.remove(channel);
		}
	}
	
	public void openIncomingChannel(String channel){
		if(!openIncomingChannels.contains(channel)){
			openIncomingChannel0(channel);
			openIncomingChannels.add(channel);
		}
	}
	
	/**
	 * Sends a message to the given player. If the channel specified is not opened,
	 * it will be opened first.
	 * @param from
	 * @param channel
	 * @param message 
	 */
	public final void fakeIncomingMessage(MCPlayer from, String channel, byte[] message){
		openOutgoingChannel(channel);
		sendIncomingMessage0(from, channel, message);
	}
	
	public abstract void closeIncomingChannel0(String channel);
	public abstract void openIncomingChannel0(String channel);
	protected abstract void sendIncomingMessage0(MCPlayer player, String channel, byte[] message);
	
	public static interface PluginMessageListener {
		void trigger(MCPlayer player, byte[] message);
	}
	
}
