package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ExecutionQueue;
import com.laytonsmith.PureUtilities.SSHWrapper;
import com.laytonsmith.core.functions.Meta;
import com.laytonsmith.persistance.PersistanceNetwork;
import java.net.URI;

/**
 * This class is for testing concepts
 *
 * @author Layton
 */
public class MainSandbox {

	public static void main(String[] argv) throws Exception {
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
		final ExecutionQueue queue = new ExecutionQueue("Test", "default");
		for(int i = 0; i < 3; i++){
			final int j = i;
			new Thread(new Runnable() {

				public void run() {
					for(int i = 0; i < 10; i++){
						final int k = i;
						queue.push("queue-" + j, new Runnable(){

							public void run() {
								String space = "";
								switch(j){
									case 1: space = "     "; break;
									case 2: space = "          "; break;
								}
								System.err.println("In Queue " + space + j + ": " + k);
							}
						});
					}
					System.err.println("Finished queueing up events for queue " + j);
				}
			}, "Thread-" + i).start();
		}
	}
	
	
}
