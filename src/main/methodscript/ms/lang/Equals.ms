<!
	strict;
	name: Equals.ms;
	author: cailin;
	created: 2019-04-08;
	description: The file containing the type ms.lang.Equals;
	compilerOptions: UltraStrict;
>

/**
 * When tagged to a field within a class that is tagged with
 * the @{EqualsGenerator} annotation, the generated equals method
 * uses that field in the calculation of equality.
 */
@{Target(FIELD)}
public annotation ms.lang.Equals {

}
