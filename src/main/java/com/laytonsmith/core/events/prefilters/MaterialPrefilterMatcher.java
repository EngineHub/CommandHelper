package com.laytonsmith.core.events.prefilters;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.ClassMirror;
import com.laytonsmith.PureUtilities.ClassLoading.ClassMirror.FieldMirror;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.net.URL;

/**
 *
 * @param <T>
 */
public abstract class MaterialPrefilterMatcher<T extends BindableEvent> extends StringPrefilterMatcher<T> {

	private final Object lock = new Object();
	private static ClassMirror orgBukkitMaterial = null;
	private static boolean materialClassFound = false;
	private static ClassDiscovery privateClassDiscovery = null;

	@api
	public static class MaterialPrefilterDocs implements PrefilterDocs {

		@Override
		public String getName() {
			return "material match";
		}

		@Override
		public String getNameWiki() {
			return "[[Prefilters#material match|Material Match]]";
		}

		@Override
		public String docs() {
			return """
				A material match is a simple string match against the list of materials in the game. For instance,
				SOUL_TORCH or STONE. Unlike a string match though, the compiler is aware of the material types, and
				will be a compile error if the material doesn't exist.

				<%CODE|
					bind('player_interact', null, array(itemname: 'SOUL_TORCH', block: 'STONE'), @event) {
						# This code will run if a player clicks stone while holding a soul torch.
					}
				%>""";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}
	}

	@Override
	public PrefilterDocs getDocsObject() {
		return new MaterialPrefilterDocs();
	}

	@Override
	public void validate(ParseTree node, CClassType nodeType, Environment env)
			throws ConfigCompileException, ConfigCompileGroupException, ConfigRuntimeException {
		if(!materialClassFound) {
			synchronized(lock) {
				if(!materialClassFound) {
					try {
						// Oh my god, this is insanely evil, I love it.
						// Basically, we shade the org.bukkit.Material class in. Loading it with Class.forName doesn't work
						// because of some initialization problems, but we don't actually care about that, so don't initialize.
						// However, even if we use Class.forName(..., false, ...) that still causes errors later, due to
						// missing dependedencies that I have no intention of also shading in, but we
						// literally only care about the string enum names here, no code should be executed, so we use
						// the ClassDiscovery library to read the class data without loading anything. However! The initial
						// ClassDiscovery precache doesn't contain shaded classes, so we have to thoroughly clear the caches,
						// rescan (with the now much larger surface area) and then the class will be in there (under the shaded
						// name, of course, just to make it more interesting.) Under "normal" circumstances, this is all fine
						// even though it takes a while, because we won't hit this case unless we're running in the langserv
						// and writing Minecraft code. If we hit this code at runtime and are running shell code, then... it's
						// going to be an error anyways, though it does mean it'll take much longer to figure that out, but
						// something is seriously wrong anyways, so, whatever. Brilliant.
						if(Implementation.GetServerType() != Implementation.Type.BUKKIT) {
							privateClassDiscovery = new ClassDiscovery();
							URL container = ClassDiscovery.GetClassContainer(MaterialPrefilterMatcher.class);
							privateClassDiscovery.removeDiscoveryLocation(container);
							privateClassDiscovery.addDiscoveryLocation(container);
							privateClassDiscovery.setClassDiscoveryCache(null);
							orgBukkitMaterial = privateClassDiscovery.forName("org.bukkit.Material");
						} else {
							// This time we want to actually load the original Material enum, so don't allow shade to rewrite
							orgBukkitMaterial = new ClassMirror(Class.forName("org.bukkit.Material"));
						}
					} catch (ClassNotFoundException ex) {
						// Do nothing.
					}
					materialClassFound = true;
				}
			}
		}
		if(orgBukkitMaterial != null && node.isConst() && !node.getData().equals(CNull.NULL)) {
			String name = node.getData().val();
			boolean found = false;
			for(FieldMirror e : orgBukkitMaterial.getFields()) {
				if(e.getName().equals(name)) {
					found = true;
					break;
				}
			}
			if(!found) {
				env.getEnv(CompilerEnvironment.class).addCompilerWarning(node.getFileOptions(),
						new CompilerWarning("\"" + name + "\" is not a valid material type, this will never match."
								+ " (This will eventually be a compile error)", node.getTarget(), null));
			}
		}
	}

	@Override
	protected String getProperty(T event) {
		MCMaterial material = getMaterial(event);
		if(material == null) {
			return null;
		}
		return material.getName();
	}

	protected abstract MCMaterial getMaterial(T event);

}
