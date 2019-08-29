<!
	strict;
	name: Required.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.JsonSerializer.Required;
	compilerOptions: UltraStrict;
>

/**
 * Use on a field to denote that it is required. It must be present in the json, or
 * it will trigger an error.
 */
@{Target(FIELD)}
public annotation ms.lang.JsonSerializer.Required {

}