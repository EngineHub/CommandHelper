package com.laytonsmith.core.asm;

import com.laytonsmith.core.asm.metadata.IRMetadata;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.asm.metadata.IRMetadata.PrototypeBuilder;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.testing.AbstractIntegrationTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 *
 * @author Cailin
 */
public class IRMetadataTest extends AbstractIntegrationTest {

	com.laytonsmith.core.environments.Environment env;

	public IRMetadataTest() throws Exception {
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment(), new LLVMEnvironment());
	}

	@Test
	public void testSimpleMetadataRender() {
		IRMetadata data = new IRMetadata(env, new PrototypeBuilder()
			.put("key1", IRMetadata.DataType.STRING)
			.put("key2", IRMetadata.DataType.CONST)
			.put("key3", IRMetadata.DataType.NUMBER)
			.build(), "Test");
		data.putAttribute("key1", "String with spaces and \"quotes\"");
		data.putConst("key2", "asdf");
		data.putNumber("key3", 1234);
		String def = data.getDefinition();
		int id = data.getMetadataId();
		assertEquals("!" + id + " = !Test(key1: \"String with spaces and \\34quotes\\34\", key2: asdf, key3: 1234)", def);
	}

	@Test
	public void testReferenceOtherMetadata() {
		IRMetadata data1 = new IRMetadata(env, new PrototypeBuilder()
			.put("key", IRMetadata.DataType.CONST)
			.build(), "Test")
			.putConst("key", "asdf");
		IRMetadata data2 = new IRMetadata(env, new PrototypeBuilder()
			.put("data1", IRMetadata.DataType.REFERENCE)
			.put("moreData", IRMetadata.DataType.STRING)
			.build(), "Test2")
			.putMetadataReference("data1", data1)
			.putAttribute("moreData", "foo");
		int data1id = data1.getMetadataId();
		int data2id = data2.getMetadataId();
		String def = data2.getDefinition();
		assertEquals("!" + data2id + " = !Test2(moreData: \"foo\", data1: !" + data1id + ")", def);
	}

}
