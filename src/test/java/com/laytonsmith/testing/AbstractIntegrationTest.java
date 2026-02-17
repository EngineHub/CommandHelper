package com.laytonsmith.testing;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.laytonsmith.abstraction.Implementation;

/**
 * Integration base test class. Manages static setup and cleanup for child integration test classes.
 */
public abstract class AbstractIntegrationTest {

	/**
	 * Runs before a child test class. Runs before the {@link BeforeClass} annotated method in a child class,
	 * unless that method shares the same name.
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpClassGlobal() throws Exception {

		// Set server type.
		Implementation.setServerType(Implementation.Type.TEST);

		// Install fake server frontend.
		// This is initialized only once and kept cross-test-class in StaticTest.
		StaticTest.InstallFakeServerFrontend();
	}

	/**
	 * Runs after a child test class. Runs after the {@link AfterClass} annotated method in a child class,
	 * unless that method shares the same name.
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownClassGlobal() throws Exception {

		// Reset server type.
		Implementation.forceServerType(null);
	}
}
