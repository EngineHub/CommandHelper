<!
	strict;
	name: UnaryOperatorOverload.ms;
	author: cailin;
	created: 2019-04-14;
	description: The file containing the type ms.lang.UnaryOperatorOverload;
	compilerOptions: UltraStrict;
>

/**
 * If an method is tagged with this, then it should be used to overload
 * the given operator.
 *
 * For unary operations, that is, where (|operator| @this) or (@this |operator|), that is turned into
 * @this->|method|() where |operator| is the value provided in the OperatorOveralod annotation, and
 * |method| is the method it is tagged on.
 *
 * The various properties of this annotation control if this is a binary operator or unary operator, and if unary,
 * whether it is postfix or prefix.
 *
 * In all cases, operator precedence follows the language defined precedence, and cannot be otherwise changed.
 */
@{Target(METHOD)}
public annotation ms.lang.UnaryOperatorOverload {
	/**
	 * Provides the name of the function that this operator overload applies to.
	 */
	// TODO: Change this to an enum, once such an enum exists
	string value();

	/**
	 * Indicates whether or not this is a unary operator. If true, the format is (|operator| @this) or
	 * (@this |operator|) depending on the value of {@link #isPrefix}, but if false, it applies to @this |operator| @b.
	 */
	boolean isUnary() = false;

	/**
	 * If this is binary (that is, not unary) this value is ignored. If it is unary, then setting this to true indicates
	 * that this operator is a prefix operator, that is (|operator| @this). If this is false, then it implies that it
	 * applies to (@this |operator|).
	 */
	boolean isPrefix() = false;
}
