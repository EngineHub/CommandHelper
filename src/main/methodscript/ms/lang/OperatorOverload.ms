<!
    strict;
    name: OperatorOverload;
    author: cailin;
    created: 2019-04-09;
    description: The file containing the type ms.lang.OperatorOverload;
>

/**
 *
 */
@{Target(METHOD)}
public annotation ms.lang.OperatorOverload {
    /**
     * Provides the name of the function that this operator overload applies to.
     */
    string function();
}
