<!
	strict;
	name: TargetType.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.TargetType;
	compilerOptions: UltraStrict;
>

/**
 * These are mostly used in the @{Target} annotation, and define the location
 * on which a given annotation is valid.
 */
public enum ms.lang.TargetType {
	/**
	 * A CLASSTYPE is anything that creates a new object type, including
	 * annotations, enums, interfaces, and others.
	 */
	CLASSTYPE,
	/**
	 * ANNOTATION means that this annotation can only be applied to other annotations.
	 */
	ANNOTATION,
	/**
	 * INTERFACE means this can only be applied to interface definitions.
	 */
	INTERFACE,
	/**
	 * CLASS means this can only be applied to class definitions. Abstract classes
	 * are also allowed.
	 */
	CLASS,
	/**
	 * ENUM means this can only be applied to enum definitions.
	 */
	ENUM,
	/**
	 * FIELD means this can only be applied to member variables. This also applies to the
	 * variable declarations in annotations.
	 */
	FIELD,
	/**
	 * METHOD means this can only be applied to class methods.
	 */
	METHOD,
	/**
	 * CONSTRUCTOR means this can only be applied to constructors.
	 */
	CONSTRUCTOR,
	/**
	 * ELEMENT means this can be applied to METHODs CONSTRUCTORs, or FIELDs.
	 */
	ELEMENT,
	/**
	 * PARAMETER means this can only be applied to the parameters of a method.
	 */
	PARAMETER,
	/**
	 * LOCAL means this can only be applied to local variable definitions.
	 */
	LOCAL,
	/**
	 * DECLARATION is any declaration of a variable, that is, FIELDs, PARAMETERs, and LOCALs.
	 */
	DECLARATION,
}