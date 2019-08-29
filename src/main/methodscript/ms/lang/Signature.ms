<!
	strict;
	name: Signature.ms;
	author: cailin;
	created: 2019-04-14;
	description: The file containing the type ms.lang.Signature;
	compilerOptions: UltraStrict;
>

/**
 * When added to an annotation, implies that this annotation can only be added to a method with the given signature.
 * It is an error to add this to an annotation that itself does not itself target {@link TargetType::METHOD}. The return
 * type of the method can be specified, using the {@code type} parameter of the Target annotation, this annotation
 * specifies the parameter types.
 */
@{Target(ANNOTATION)}
public annotation ms.lang.Signature {
	/**
	 * Specifies the parameter types that the method this annotation must be tagged on. The return type is specified
	 * in the Target annotation itself. If there are no parameters, then the value may be an empty array.
	 */
	ClassType[] @value;
}
