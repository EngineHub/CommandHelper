<!
	strict;
	name: Version.ms;
	author: cailin;
	created: 2019-03-13;
	description: The file containing the type ms.lang.util.Version;
	compilerOptions: UltraStrict;
>

/**
 * A version is represented by a string, containing 3 or 4 parts, seprated by the dot character.
 * The first three parts must be integers, and the fourth part may be any string that does not
 * contain spaces or dots. It is intended that version numbers of this type follow the
 * Symantic Versioning standard, which can be found here: https://semver.org/
 */
public class ms.lang.util.Version {
	public int @major {get;};
	public int @minor {get;};
	public int @patch {get;};
	public string @tag {get;};

	public Version(int @major, int @minor, int @patch) {
		@this->major = @major;
		@this->minor = @minor;
		@this->patch = @patch;
	}

	public Version(int @major, int @minor, int @patch, string @tag) {
		@this(@major, @minor, @patch);
		@this->tag = @tag;
	}

	public Version string(
			@{Regex('(\\d+)\\.(\\d+).(\\d+)(?:-(.*))*')}
			string @version
	) {
		array<string> @parts = reg_match('(\\d+)\\.(\\d+).(\\d+)(?:-(.*))*', @version);
		@this(integer(@parts[0]), integer(@parts[1]), integer(@parts[2]), @parts[3]);
	}

	public string toString() {
		return("@major.@minor.@patch" . if(@tag, @tag, ""));
	}
}