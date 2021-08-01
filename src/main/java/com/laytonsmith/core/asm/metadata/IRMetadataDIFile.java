package com.laytonsmith.core.asm.metadata;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.environments.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class IRMetadataDIFile extends IRMetadata {

	public IRMetadataDIFile(Environment env, File file, boolean release) throws FileNotFoundException, IOException {
		super(env, new IRMetadata.PrototypeBuilder()
			.put("filename", DataType.STRING)
			.put("directory", DataType.STRING)
			.put("checksumkind", DataType.CONST)
			.put("checksum", DataType.STRING)
			.build(), "DIFile");
		try {
			file = file.getAbsoluteFile();
			this.putAttribute("filename", file.getName());
			if(!release) {
				this.putAttribute("directory", file.getParentFile().getAbsolutePath());
			} else {
				this.putAttribute("directory", "");
			}
			this.putConst("checksumkind", "CSK_SHA256");
			byte[] val = StreamUtils.GetBytes(new FileInputStream(file));
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
			digest.update(val);
			String hash = StringUtils.toHex(digest.digest()).toLowerCase();
			this.putAttribute("checksum", hash);
		} catch(NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}


	}
}
