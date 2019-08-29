<!
	strict;
	name: MinLength.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.validators.MinLength;
	compilerOptions: UltraStrict;
>

/**
 * Indicates that this field must have a minimum length specified.
 */
@{Target(value: DECLARATION, type: Sizeable)}
public annotation ms.lang.validators.MinLength {
	/**
	 * The minimum length of an element.
	 */
	@{NonNegative}
	int @value;
}