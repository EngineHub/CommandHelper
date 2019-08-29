<!
	strict;
	name: NonPositive.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.validators.NonPositive;
	compilerOptions: UltraStrict;
>

/**
 * Indicates that a value assigned to this number may not be positive.
 * Zero is allowed, however.
 */
@{Target(value: DECLARATION, type: number)}
public annotation ms.lang.validators.NonPositive {

}
