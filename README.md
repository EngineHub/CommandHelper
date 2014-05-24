CommandHelper
=============

CommandHelper adds simple command aliases, complex macros,
and the ability to script your own commands and events into Minecraft,
using the MethodScript scripting language.

Compiling
---------

You need to have Maven installed (http://maven.apache.org). Once installed,
simply run:

    mvn clean package install

Maven will automatically download dependencies for you. Note: For that to work,
be sure to add Maven to your "PATH". If you get a message about tests failing,
try running:

	mvn -Pprovisional-build clean package install

Contributing
------------

We happily accept contributions. The best way to do this is to fork
CommandHelper on GitHub, add your changes, and then submit a pull request.
We'll look at it, make comments, and merge it into CommandHelper if
everything works out. If you make a PR, and feel your code is being
nitpicked to death, don't worry! Whenever a code review is done, it tends
to find lots of minor errors, even in a very experienced programmer. Don't
get discouraged! We'll work with you to make the changes, and all contributions
are appreciated. If the feature you want to add makes a significant change,
however, it may be best to discuss the changes with the other contributors
before you begin work on the feature.

By submitting code, you agree to dual license your code under the
the MIT License and GPL, barring the special restriction regarding code submissions,
explained in the SPECIAL_LICENSE.txt file, which is attached.

For details about code formatting standards, and other basic information for
contributors, please see the CONTRIBUTING.txt file.

Portions of CommandHelper are copyright by various contributors.