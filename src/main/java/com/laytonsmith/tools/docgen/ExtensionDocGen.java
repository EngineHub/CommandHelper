package com.laytonsmith.tools.docgen;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.PureUtilities.DynamicClassLoader;
import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.functions.Function;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 *
 * @author cgallarno
 */
public class ExtensionDocGen {

	public static void generate(File inputExtension, File outputFile) throws InstantiationException, IllegalAccessException, MalformedURLException, IOException {
		ClassDiscovery customDiscovery = new ClassDiscovery();
		URL url = new URL("jar:" + inputExtension.toURI().toURL() + "!/");
		customDiscovery.addDiscoveryLocation(url);
		String fdocs = "";
		DynamicClassLoader classloader = new DynamicClassLoader();
		classloader.addJar(url);
		Set<Class<Function>> functions = customDiscovery.loadClassesWithAnnotationThatExtend(api.class, Function.class, classloader, true);
		for(Class<Function> cmf : functions) {
			Function f = cmf.newInstance();
			DocGen.DocInfo di = new DocGen.DocInfo(f.docs());
			fdocs = fdocs + StringUtils.nl + "# " + f.getName() + "()" + StringUtils.nl + "###### " + di.ret + "; " + di.args + StringUtils.nl + di.desc;
		}
		FileUtility.write(fdocs, outputFile);
	}
}
