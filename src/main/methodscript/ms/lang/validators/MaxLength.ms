<!
	strict;
	name: MaxLength.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.validators.MaxLength;
	compilerOptions: UltraStrict;
>

/**
 * Indicates that the value must have no more than a particular length.
 */
@{Target(value: DECLARATION, type: Sizeable)}
public annotation ms.lang.validators.MaxLength {
	/**
	 * The max length of this element.
	 */
	@{NonNegative}
	int @value;
}