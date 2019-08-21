package com.laytonsmith.tools.docgen.sitedeploy;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cailin
 */
class LocalDeploymentMethod implements DeploymentMethod {

	String rootDirectory;

	public LocalDeploymentMethod(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	@Override
	public boolean deploy(InputStream data, String toLocation, String ignored) {
		File outLoc = new File(rootDirectory, toLocation);
		try {
			byte[] d = StreamUtils.GetBytes(data);
			String currentFile;
			try {
				currentFile = SiteDeploy.getLocalMD5(new FileInputStream(outLoc));
			} catch (FileNotFoundException ex) {
				// Doesn't exist, so just set the currentFile to INVALID
				currentFile = "INVALID";
			}
			String newFile = SiteDeploy.getLocalMD5(new ByteArrayInputStream(d));
			if(currentFile.equals(newFile)) {
				return false;
			}
			FileUtil.write(d, outLoc, FileUtil.OVERWRITE, true);
		} catch (IOException ex) {
			Logger.getLogger(SiteDeploy.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
		return true;
	}

	@Override
	public void finish() {
	}

	@Override
	public String getID() {
		return rootDirectory;
	}

}
