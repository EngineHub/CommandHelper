<!
	strict;
	name: ValidRegex.ms;
	author: cailin;
	created: 2019-03-15;
	description: The file containing the type ms.lang.validators.ValidRegex;
	compilerOptions: UltraStrict;
>

/**
 * Indicates that the string itself must be a valid regex.
 * This is opposed to the {@code Regex} annotation,
 * which is used to tag content strings directly, and the {@code RegexValidator}
 * annotation, which is tagged to annotations, and make them a
 * validator as well.
 */
@{Target(value: DECLARATION, type: string)}
annotation ms.lang.validators.ValidRegex {

}
