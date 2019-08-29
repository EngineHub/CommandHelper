<!
	strict;
	name: CIManagement.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.manifest.CIManagement;
	compilerOptions: UltraStrict;
>

use ms.lang.validators;

/**
 * Provides information linking to the project's Continuous Integration environment.
 * Plugins for IDEs may be able to use this
 * to integrate with the IDE's issue tracker GUI.
 */
public interface ms.lang.manifest.CIManagement {
	/**
	 * The name of the system. While this is a freeform string, so that new
	 * CI systems can be added, for a few well known CI systems, there
	 * are defined values, which all projects should use. Particularly:
	 * JENKINS, GITLAB, TRAVIS, TEAMCITY, AZURE. For
	 * others, come up with a sensible identifier for it, and petition for
	 * it to become standardized.
	 */
	string @system;

	/**
	 * The URL to the CI system, which should be used for opening in a web browser.
	 */
	@{ValidUrl}
	string @url;

	string @comment;
}
