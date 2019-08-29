<!
	strict;
	name: ValidUri.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.validators.ValidUri;
	compilerOptions: UltraStrict;
>

/**
 * Meant to be tagged on strings that are intended to represent URIs. The URI must
 * be formatted according to RFC 3987 (http://www.faqs.org/rfcs/rfc3987.html).
 * <p>
 * Note that URIs are a very specific standard, and many programs do not
 * accept what should otherwise be a valid URI based on the standard, so
 * validating against this only provides a small amount of assurance that
 * this will be a *useful* URI, however, if it does not match this, it
 * most assureadly is not a URI.
 */
@{Target(value: DECLARATION, type: string)}
@{RegexValidator(
	// Stolen shamelessly and without looking back from
	// https://stackoverflow.com/a/190405/731752
	comp_read('uri_validator.txt')
)}
public annotation ms.lang.validators.ValidUri {

}