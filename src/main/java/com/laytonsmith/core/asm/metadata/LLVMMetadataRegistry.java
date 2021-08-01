package com.laytonsmith.core.asm.metadata;

import com.laytonsmith.core.asm.metadata.IRMetadataDIFile;
import com.laytonsmith.core.asm.metadata.IRMetadata;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.environments.Environment;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Contains generic and specific references to the stored metadata entries. Calling the specific versions of "add"
 * will also add the reference to the generic metadata registry.
 */
public class LLVMMetadataRegistry {
	private final Set<IRMetadata> allMetadata = new LinkedHashSet<>();

	public void addMetadata(IRMetadata data) {
		allMetadata.add(data);
	}

	public Set<IRMetadata> getAllMetadata() {
		return new LinkedHashSet<>(allMetadata);
	}

	private final Map<File, IRMetadataDIFile> difile = new HashMap<>();

	public IRMetadataDIFile getFileMetadata(Environment env, File file, boolean release) throws IOException {
		if(!difile.containsKey(file)) {
			IRMetadataDIFile mdFile = new IRMetadataDIFile(env, file, release);
			difile.put(file, mdFile);
			return mdFile;
		}
		return this.difile.get(file);
	}

	private final Map<CClassType, IRMetadataDIBasicType> basicTypeMap = new HashMap<>();

	/**
	 * There should only be one reference to each type per file.NOTE: If CNull or CVoid is sent to this, an Error will
	 * occur. The correct value to send in the IR is "null", not a BasicType with name null or void. Therefore, to
	 * prevent this error, it is checked here, and not allowed.
	 * @param env
	 * @param type
	 * @return
	 */
	public IRMetadataDIBasicType getBasicType(Environment env, CClassType type) {
		if(CNull.TYPE.equals(type) || CVoid.TYPE.equals(type)) {
			throw new RuntimeException("[Compiler Bug] Cannot get BasicType for null or void.");
		}

		if(!basicTypeMap.containsKey(type)) {
			IRMetadataDIBasicType bt = new IRMetadataDIBasicType(env, type);
			basicTypeMap.put(type, bt);
			return bt;
		}
		return basicTypeMap.get(type);
	}

	private IRMetadata emptyTuple = null;

	/**
	 * Returns (or creates and returns) a reference to the empty tuple, !{}.
	 * @param env
	 * @return
	 */
	public IRMetadata getEmptyTuple(Environment env) {
		if(emptyTuple == null) {
			emptyTuple = IRMetadata.AsEmptyTuple(env);
		}
		return emptyTuple;
	}
}
