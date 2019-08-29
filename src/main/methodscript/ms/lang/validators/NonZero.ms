<!
	strict;
	name: NonZero.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.validators.NonZero;
	compilerOptions: UltraStrict;
>

/**
 * Indicates that the value may be either positive or negative, but is not allowed
 * to be zero.
 */
@{Target(value: DECLARATION, type: number)}
public annotation ms.lang.validators.NonZero {

}
