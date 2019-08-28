<!
	strict;
	name: BinaryOperatorOverload.ms;
	author: cailin;
	created: 2019-04-09;
	description: The file containing the type ms.lang.BinaryOperatorOverload;
	compilerOptions: UltraStrict;
>

/**
 * If an method is tagged with this, then it should be used to overload
 * the given operator.
 *
 * For binary operations, that is, where @this |operator| @b, this
 * will be turned into @this->|method|(@b), where |operator| is the value provided in the OperatorOverload
 * annotation, and |method| is the method it is tagged on.
 *
 * In all cases, operator precedence follows the language defined precedence, and cannot be otherwise changed.
 */
@{Target(METHOD)}
public annotation ms.lang.BinaryOperatorOverload {
	/**
	 * Provides the name of the function that this operator overload applies to.
	 */
	// TODO: Change this to an enum, once such an enum exists
	string value();
}
