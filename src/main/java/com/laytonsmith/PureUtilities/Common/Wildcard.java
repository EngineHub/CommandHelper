package com.laytonsmith.PureUtilities.Common;

/**
 * A Wildcard object is a regex-like matcher, which uses a greatly simplified syntax, and is useful only for specific
 * application needs. A Wildcard is used to specify a fuzzy match on file patters, namespace patterns, or other
 * segmented data. A Wildcard object must be constructed with a separator character and a pattern, and then data can be
 * fed to it to be matched. Wildcards only support 3 special symbols, *, **, and ?, and support escaping of those
 * characters for literals with a backslash. A * symbol matches all characters across one and only one segment, **
 * matches all characters across any number of segments, and ? matches any one character. For instance, if you have a
 * file path: /home/user/files/file.html, then the separator would be a "/", and to match this file, you might use the
 * pattern "/home/user/files/*.htm?" This pattern would also match /home/user/files/file2.htm,
 * /home/user/files/file3.html, etc, but it would not match /home/user/files/resources/frame.html, because the last
 * match does not span segments. However, "/home/user/files/**.htm?" would match.
 *
 *
 */
public class Wildcard {

}
