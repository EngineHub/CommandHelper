<!
	strict;
	name: RegexValidator.ms;
	author: cailin;
	created: 2019-03-14;
	description: The file containing the type ms.lang.validators.RegexValidator;
	compilerOptions: UltraStrict;
>

/**
 * When added to another annotation, it becomes a regex validator
 * with the given regex. This is useful for quickly creating validators
 * that can be described with a regex. This is opposed to the {@code Regex} annotation,
 * which is used to tag strings directly, and the {@code ValidRegex} annotation,
 * which indicates that a string should itself be a valid regex.
 */
@{Target(value: ANNOTATION)}
public annotation ms.lang.validators.RegexValidator {
	/**
	 * The regex that the value should match.
	 */
	@{ValidRegex}
	string @value;
}
