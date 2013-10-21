package com.laytonsmith.database;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lsmith
 */
public class ProfilesTest {
	
	public ProfilesTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(Profiles.class));
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	
	@Test
	public void testProfileSuccess() throws Exception{
		String good = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
				+ "<profiles>"
				+ "	<profile id=\"profile1\">"
				+ "		<type>mysql</type>"
				+ "		<database>db_name1</database>"
				+ "		<username>username1</username>"
				+ "		<password>password1</password>"
				+ "	</profile>"
				+ "	<profile id=\"profile2\">"
				+ "		<type>mysql</type>"
				+ "		<database>db_name2</database>"
				+ "		<username>username2</username>"
				+ "		<password>password2</password>"
				+ "	</profile>"
				+ "</profiles>";
		Profiles profiles = new Profiles(good);
		
	}
	
	
}
