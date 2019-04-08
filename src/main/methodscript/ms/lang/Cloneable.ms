<!
    strict;
    name: Cloneable;
    author: cailin;
    created: 2019-04-08;
    description: The file containing the type ms.lang.Cloneable;
    compilerOptions: UltraStrict;
>

/**
 * A Cloneable object is one that can be cloned.
 */
public interface ms.lang.Cloneable {
    /**
     * Returns a clone of the instance. In general, it should be true that after cloning, the new
     * instance is completely independent of the old instance, but is .equals to it, and not
     * {@function ref_equals}.
     */
    self clone();
}
