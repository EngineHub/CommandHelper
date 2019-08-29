<!
	strict;
	name: Repository.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.manifest.Repository;
	compilerOptions: UltraStrict;
>

/**
 * A repository is a place where the raw source code, or MSLP packages can be found.
 * Ideally, this should not be used, as the code should be released publicly. However,
 * there is of course a need to provide private hosts, and those can be configured
 * here. Even more ideally, is that this is a system level configuration, and rather
 * than having the project know where to find its dependencies, it should instead
 * configure the user's system to know where to look.
 */
public interface ms.lang.manifest.Repository {

	// TODO

	string @comment;
}
