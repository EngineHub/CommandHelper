<!
    strict;
    name: OperatorOverloadInterface;
    author: cailin;
    created: 2019-04-09;
    description: The file containing the type ms.lang.BinaryOperatorOverloadInterface;
    compilerOptions: UltraStrict;
>

/**
 * If an interface is tagged with this, then it contains methods that should be used to overload
 * the operation of certain methods. For binary operations, that is, where @this |operator| @b, this
 * will be turned into @this->|method|(@b), where |operator| is the value provided in the OperatorOverload
 * annotation, and |method| is the method it is tagged on.
 */
@{Target(INTERFACE)}
internal annotation ms.lang.BinaryOperatorOverloadInterface {

}
