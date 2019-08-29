<!
	strict;
	name: Recommended.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.JsonSerializer.Recommended;
	compilerOptions: UltraStrict;
>

/**
 * Denotes that a field is not strictly required, but is recommended. In strict mode,
 * this will cause a warning to be issued.
 */
@{Target(FIELD)}
public annotation ms.lang.JsonSerializer.Recommended {

}