<!
	strict;
	name: ValidEmail.ms;
	author: cailin;
	created: 2019-03-14;
	description: The file containing the type ms.lang.validators.ValidEmail;
	compilerOptions: UltraStrict;
>

/**
 * Meant to be tagged on strings that are intended to represent email addresses. The address must
 * be formatted according to RFC 3987 (http://www.faqs.org/rfcs/rfc3987.html).
 * <p>
 * Note that an email address according the standard is a very specific standard, and many programs do not
 * accept what should otherwise be a valid address based on the standard, so
 * validating against this only provides a small amount of assurance that
 * this will be a *useful* email address, however, if it does not match this, it
 * most assureadly is not a email address. However, email addresses such as "email@/" will pass
 * validation, so further parsing is likely needed.
 */
@{Target(value: DECLARATION, type: string)}
@{RegexValidator(
	// Stolen shamelessly and without looking back from
	// http://www.ex-parrot.com/~pdw/Mail-RFC822-Address.html
	comp_read('email_validator.txt')
)}
public annotation ms.lang.validators.ValidEmail {

}
