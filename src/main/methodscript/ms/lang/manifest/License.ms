<!
	strict;
	name: License.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.manifest.License;
	compilerOptions: UltraStrict;
>

use ms.lang.validators;

/**
 * Represents a license that the source code is released under.
 */
public interface ms.lang.manifest.License {
	/**
	 * The name of the license.
	 */
	string @name;

	/**
	 * A URL pointing to the full license text.
	 */
	@{ValidUrl}
	string @url;

	string @comment;
}
