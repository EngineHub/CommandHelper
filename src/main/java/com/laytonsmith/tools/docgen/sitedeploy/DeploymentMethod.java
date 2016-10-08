package com.laytonsmith.tools.docgen.sitedeploy;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides a mechanism to deploy to a particular system using a particular method.
 * @author cailin
 */
interface DeploymentMethod {

    /**
     * Writes the file to the appropriate location.
     * @param data The data to write
     * @param toLocation The general location to write the file to.
     * @return true, if the file was actually changed, false otherwise
     * @throws IOException
     */
    boolean deploy(InputStream data, String toLocation) throws IOException;

    /**
     * After the deployment is finished, this will be called. This can optionally
     * clean up i.e. network connections if they were being cached.
     */
    void finish();

}
