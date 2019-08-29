<!
	strict;
	name: EqualsGenerator.ms;
	author: cailin;
	created: 2019-04-08;
	description: The file containing the type ms.lang.EqualsGenerator;
	compilerOptions: UltraStrict;
>

/**
 * This annotation can be tagged on classes that wish to use the modified default
 * equals and hashCode handling. Any fields that should be considered in the calculation of object
 * equality should be further tagged with the @{Equals} annotation. This mechanism
 * provides both an equals method and if the class implements Hashable, a hashCode method,
 * and it is an error to both tag
 * a class with this annotation, and provide an implementation of equals (or hashCode).
 *
 * If the tagged class does not implement Hashable, then no hashCode method is provided.
 *
 * If a class that is tagged with this annotation is subclassed, then the subclass will
 * not be affected by this, however, it is highly recommended that the subclass does
 * provide its own equals (and hashCode) implementation, either through manually providing
 * the methods, or by also using this annotation, as the results of the methods will likely
 * be wrong (though not necessarily, which is why the annotation is not automatically inherited.)
 *
 * The default implementation of hashCode provided by this mechanism is based on the implementation suggested in
 * Josh Bloch's *Effective Java*. Two random, large, prime numbers are picked, a, and b. The hash is set to
 * a, then, for each field that is marked with @{Equals}, the hash is multiplied by b, and then the hash code
 * of the field is added to it (0 is used if the field is null).
 * This process is repeated, until all fields are considered. In general, this
 * is considered a good and robust hashCode, and should generally suffice for most all use cases. However, this
 * runs the risk that each time the code is recompiled, the prime values selected will change. This is generally
 * desirable, as the contract for hashCode states that the hashCode across multiple runs of the program are
 * not comparable, nor are the results for the same object across two concurrently running programs. There may
 * be other reasons to wish to use a manual mechanism, however. If the class is immutable, then the constructor(s)
 * are modified to calculate the value at construction, and the value is simply returned.
 */
@{Target(CLASSTYPE)}
public annotation ms.lang.EqualsGenerator {

}
