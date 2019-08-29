<!
	strict;
	name: SourceCode.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.manifest.SourceCode;
	compilerOptions: UltraStrict;
>

/**
 * Links to a project's public source code system. This can be integrated with in the
 * GUI, if there is support for the specified source control system.
 */
public interface ms.lang.manifest.SourceCode {

	/**
	 * The name of the source control system. While this is a freeform string, so that new
	 * types can be added, for a few well known types, there
	 * are defined values, which all projects should use. Particularly:
	 * GIT, SVN, CVS. For
	 * others, come up with a sensible identifier for it, and petition for
	 * it to become standardized.
	 */
	string @system;


	/**
	 * The URL to the source control system, which should be used for opening in a web browser.
	 */
	@{ValidUrl}
	string @url;

	/**
	 * The source control identifier that is needed to pull down the code in read-only mode.
	 */
	string @connection;

	/**
	 * The source control identifier that is needed to pull down the code in read/write mode.
	 */
	string @developerConnection;

	string @comment;

}
