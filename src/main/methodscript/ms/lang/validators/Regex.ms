<!
    strict;
    name: Regex;
    author: cailin;
    created: 2019-03-13;
    description: The file containing the type ms.lang.validators.Regex;
>

/**
 * Meant to be tagged on any string, indicating that the string assigned to the variable
 * should follow the specific regex.
 */
@{Target(value: DECLARATION, type: string)}
annotation ms.lang.validators.Regex {
    /**
     * The regex that the string must match.
     */
    string @value;
}
