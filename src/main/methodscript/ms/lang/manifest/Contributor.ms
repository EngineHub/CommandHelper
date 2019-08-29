<!
	strict;
	name: Contributor.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.manifest.Contributor;
	compilerOptions: UltraStrict;
>

/**
 * Provides information about a contributor.
 */
public interface ms.lang.manifest.Contributor {
	/**
	 * The contributor id, which should be unique for the user. Usually,
	 * this should be the username registered with the official
	 * package manager, but this is not enforced.
	 */
	string @id;
	/**
	 * The display name of the contributor.
	 */
	string @name;
	/**
	 * The email of the contributor.
	 */
	string @email;
	/**
	 * A url associated with the contributor, perhaps their github profile, or a website they own.
	 */
	string @url;
	/**
	 * Roles of the contributor in the project. I.e. "Lead Developer", "Tester" or "QA".
	 */
	List<string> @roles;
	/**
	 * The timezone of the contributor. Useful for knowing what time the person is likely to be available.
	 */
	string @timezone;
	/**
	 * Other info that the contributor wishes to share. This is free form, and may include things like
	 * links to social media profiles, alternative urls, etc.
	 */
	map<string, string> @otherInfo;

	string @comment;
}