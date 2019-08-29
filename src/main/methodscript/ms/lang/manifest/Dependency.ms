<!
	strict;
	name: Dependency.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.manifest.Dependency;
	compilerOptions: UltraStrict;
>

/**
 * A dependency is a link to an external project. The project is identified by
 * its organization, project, and version. Only direct dependencies should
 * be listed, transient dependencies (that is, dependencies of dependencies) will
 * automatically be calculated and populated in the final dependency graph.
 */
public interface ms.lang.manifest.Dependency {

	// TODO

	string @comment;
}
