<!
    strict;
    name: Regex;
    author: cailin;
    created: 2019-03-13;
    description: The file containing the type ms.lang.Target;
    compilerOptions: UltraStrict;
>

/**
 * Added to annotations themselves, this declares what types of
 * targets this annotation may be used on.
 */
// Because the annotation has not been created yet, the
// compiler has to have special handling to help bootstrap
// this particular annotation.
@{Target(ANNOTATION)}
public annotation ms.lang.Target {
    /**
     * A list of TargetTypes where the tagged annotation can
     * be placed. If this is empty, that means that it can
     * be placed on any target.
     */
    List<TargetType> @value = new ArrayList<>();

    /**
     * For certain types of targets, it may be useful to further
     * refine what types this may be placed on. Depending on the
     * target, this has slightly different meanings. For types,
     * this means the type must extend the given type. For methods,
     * the return type must extend this type, and for fields, it
     * must be of this type or a subclass. For other types, this
     * is simply ignored.
     */
    ClassType @type = mixed;
}