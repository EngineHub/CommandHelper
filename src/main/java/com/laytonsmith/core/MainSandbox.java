package com.laytonsmith.core;

import com.laytonsmith.core.apps.AppsApiUtil;
import io.swagger.client.api.MetaApi;

/**
 * This class is for testing concepts
 */
public class MainSandbox {

    public static void main(String[] args) throws Exception {
		AppsApiUtil api = new AppsApiUtil("http://localhost:8080", false);
		api.registerDefaultClient();
		MetaApi meta = new MetaApi();
		System.out.println(meta.rootGet());
		System.out.println(meta.pingGet());
	}
}
