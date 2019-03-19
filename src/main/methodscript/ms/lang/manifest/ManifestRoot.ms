<!
    strict;
    name: Regex;
    author: cailin;
    created: 2019-03-13;
    description: The file containing the type ms.lang.manifest.ManifestRoot;
>

use ms.lang.JsonSerializer;
use ms.lang.util;
use ms.lang.validators;

public interface ms.lang.manifest.ManifestRoot {
	/**
	 * The manifest version. Should be 1.0.0.
	 */
	@{Required}
	Version @manifestVersion;
	/**
	 * The organization name. You don't need to own a company to put something here,
	 * but you do need to choose something unique and which you have exclusive control
	 * over. The format should be reverse domain style, so if you own example.com, you
	 * could put "com.example" here. Your github domain "io.github.name" is also a good
	 * option. If you don't own the domain listed, this will cause problems down the road
	 * if you wish to publicly publish your code in official repositories.
	 */
	@{Required}
	@{MinLength(3)}
	string @organizationName;
	/**
	 * This is the project name. Within your organization, this must be unique. This is
	 * the "programmatic" name of your project, and must match the given regex.
	 */
	@{Required}
	@{Regex("[a-zA-Z_]+[a-zA-Z_0-9\\-]*")}
	@{MinLength(3)}
	string @projectName;
	/**
	 * This is the version of the project.
	 */
	@{Required}
	Version @projectVersion;
	/**
	 * This is the human readable name of the project. Not strictly required, but the project name
	 * is used if this is empty.
	 */
	@{Recommended}
	string @name;
	/**
	 * The project description.
	 */
	@{Recommended}
	string @description;

	/**
	 * Information about the organization that is publishing this project.
	 */
	OrganizationInfo @organizationInfo;

	/**
	 * Contains a list of information about contributors.
	 */
	List<Contributor> @contributors = new ArrayList<>();

	/**
	 * A link to the issue management system for this project.
	 */
	IssueManagement @issueManagement;

	/**
	 * A link to the CI management system for this project.
	 */
	CIManagement @ciManagement;

	/**
	 * A link to the source code system.
	 */
	@{Recommended}
	SourceCode @sourceCode;

	/**
	 * A list of licenses that this project is released under.
	 */
	@{Recommended}
	List<License> @licenses;

	string @comment;
}