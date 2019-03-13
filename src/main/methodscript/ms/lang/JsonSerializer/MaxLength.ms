<!
    strict;
    name: Regex;
    author: cailin;
    created: 2019-03-13;
    description: The file containing the type ms.lang.JsonSerializer.MaxLength;
>

use ms.lang.validators;

/**
 * Indicates that the value must have no more than a particular length.
 */
@{Target(value: FIELD)}
annotation ms.lang.JsonSerializer.MaxLength {
    /**
     * The max length of this element.
     */
    @{NonNegative}
    int @value;
}