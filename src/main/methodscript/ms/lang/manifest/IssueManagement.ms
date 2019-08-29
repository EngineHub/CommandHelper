<!
	strict;
	name: IssueManagement.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.manifest.IssueManagement;
	compilerOptions: UltraStrict;
>

/**
 * Provides a mechanism to indicate where the issue tracker is
 * for this project. Plugins for IDEs may be able to use this
 * to integrate with the IDE's issue tracker GUI.
 */
public interface ms.lang.manifest.IssueManagement {
	/**
	 * The name of the system. While this is a freeform string, so that new
	 * issue trackers can be added, for a few well known issue trackers, there
	 * are defined values, which all projects should use. Particularly:
	 * GITHUB, BUGZILLA, JIRA, MANTIS, YOUTRACK, AZURE (for Azure Boards). For
	 * others, come up with a sensible identifier for it, and petition for
	 * it to become standardized.
	 */
	string @system;

	/**
	 * The URL to the issue tracker.
	 */
	@{ValidUrl}
	string @url;

	string @comment;
}