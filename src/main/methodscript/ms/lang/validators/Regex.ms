<!
	strict;
	name: Regex.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.validators.Regex;
	compilerOptions: UltraStrict;
>

/**
 * Meant to be tagged on any string, indicating that the string assigned to the variable
 * should follow the specific regex. This is opposed to the {@code RegexValidator} annotation,
 * which is used to tag other annotations, and the {@code ValidRegex} annotation,
 * which indicates that a string should itself be a valid regex.
 */
@{Target(value: DECLARATION, type: string)}
public annotation ms.lang.validators.Regex {
	/**
	 * The regex that the string must match.
	 */
	@{ValidRegex}
	string @value;
}
