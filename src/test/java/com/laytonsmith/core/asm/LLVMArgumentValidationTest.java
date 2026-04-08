package com.laytonsmith.core.asm;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.testing.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LLVMArgumentValidationTest extends AbstractIntegrationTest {

	Environment env;

	@Before
	public void setUp() throws Exception {
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment(), new LLVMEnvironment());
		LLVMEnvironment llvmenv = env.getEnv(LLVMEnvironment.class);
		llvmenv.newMethodFrame("test");
	}

	@Test
	public void testConvertCClassTypeToIRType() {
		Map<CClassType, IRType> expected = Map.of(
			CInt.TYPE, IRType.INTEGER64,
			CDouble.TYPE, IRType.DOUBLE,
			CString.TYPE, IRType.STRING,
			CBoolean.TYPE, IRType.INTEGER1,
			CClassType.AUTO, IRType.MS_VALUE
		);
		for(Map.Entry<CClassType, IRType> entry : expected.entrySet()) {
			assertEquals("Mapping for " + entry.getKey(),
					entry.getValue(),
					LLVMArgumentValidation.convertCClassTypeToIRType(entry.getKey()));
		}
	}

	@Test
	public void testBoxTagsAreUnique() {
		Set<Integer> seen = new HashSet<>();
		for(IRType type : IRType.values()) {
			if(type.isBoxable()) {
				assertTrue("Duplicate box tag " + type.getBoxTag() + " on " + type,
						seen.add(type.getBoxTag()));
			}
		}
	}

	@Test
	public void testNonBoxableTypes() {
		IRType[] nonBoxable = {
			IRType.INTEGER8POINTER, IRType.INTEGER8POINTERPOINTER,
			IRType.VOID, IRType.MS_VALUE, IRType.MS_VALUE_PTR, IRType.OTHER
		};
		for(IRType type : nonBoxable) {
			assertFalse(type + " should not be boxable", type.isBoxable());
		}
	}

	@Test
	public void testEmitGetTagEmitsExtractvalue() {
		IRBuilder builder = new IRBuilder();
		IRData result = LLVMArgumentValidation.emitGetTag(builder, Target.UNKNOWN, env, 5);
		assertEquals(IRType.INTEGER8, result.getResultType());
		boolean found = false;
		for(String line : builder.lines) {
			if(line.contains("extractvalue { i8, i64 } %5, 0")) {
				found = true;
				break;
			}
		}
		assertTrue("Expected extractvalue instruction for tag extraction", found);
	}
}
