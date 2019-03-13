<!
    strict;
    name: Regex;
    author: cailin;
    created: 2019-03-13;
    description: The file containing the type ms.lang.JsonSerializer.MinLength;
>

use ms.lang.validators;

/**
 * Indicates that this field must have a minimum length specified.
 */
@{Target(FIELD)}
annotation ms.lang.JsonSerializer.MinLength {
    /**
     * The minimum length of an element.
     */
    @{NonNegative}
    int @value;
}