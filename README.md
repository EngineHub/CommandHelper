CommandHelper
=============
| Service | Badge |
|--------|---------:|
| TeamCity | [![TC Build Status](http://ci.enginehub.org/app/rest/builds/buildType:bt12,branch:master/statusIcon.svg)](http://ci.enginehub.org/viewType.html?buildTypeId=bt12&guest=1) |
| Azure | [![Azure Build Status](https://dev.azure.com/MethodScript/CommandHelper/_apis/build/status/EngineHub.CommandHelper)](https://dev.azure.com/MethodScript/CommandHelper/_build/latest?definitionId=1) |
| Snyk.io | [![Known Vulnerabilities](https://snyk.io/test/github/EngineHub/CommandHelper/badge.svg)](https://snyk.io/test/github/EngineHub/CommandHelper) |
| Discord | [![Discord](https://img.shields.io/discord/446057847428481044.svg)](https://img.shields.io/discord/446057847428481044.svg) |
| Code Size | [![Code Size](https://img.shields.io/github/languages/code-size/EngineHub/CommandHelper.svg)](https://img.shields.io/github/languages/code-size/EngineHub/CommandHelper.svg) |
| Stars | [![Stars](https://img.shields.io/github/stars/EngineHub/CommandHelper.svg?style=social)](https://img.shields.io/github/stars/EngineHub/CommandHelper.svg?style=social) |
| Website | [![Website](https://img.shields.io/website/https/methodscript.com.svg?down_color=red&down_message=offline&up_color=green&up_message=online)](https://methodscript.com) |
| Contributors | [![Contributors](https://img.shields.io/github/contributors/EngineHub/CommandHelper.svg)](https://img.shields.io/github/contributors/EngineHub/CommandHelper.svg) |
| Last Commit | [![Last Commit](https://img.shields.io/github/last-commit/EngineHub/CommandHelper.svg)](https://img.shields.io/github/last-commit/EngineHub/CommandHelper.svg) |

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

This project uses BrowserStack (https://www.browserstack.com) for testing the website.

Installing
----------

There are two modes of installation, both first require obtaining the MethodScript jar.
You can build it yourself, or download the official builds from
[here](http://builds.enginehub.org/job/commandhelper?branch=master).

Minecraft: Installation in Minecraft is simple. Simply drop the jar in the plugins
folder.

Standalone Programming: MethodScript is a fledgling general purpose programming language,
and can be used from the command line, much like python, node, or other programming
languages. To install, place the jar file in whatever location you like, (noting that
it will create a folder at the same level which contains the configuration files, so
it's probably easiest to put this in your user directory),
then run `java -jar MethodScript.jar install-cmdline` as root/Administrator.
This will install the `mscript` program and add it to your path, 
which can be used to start a REPL shell for quick tasks, execute a script file, or
easily run the commandline tools. On Windows, this also installs a PowerShell module,
which can be used with `Import-Module -Name MethodScript` and `Invoke-MethodScript`. On Windows,
you must reboot your system after installatation to use the `mscript` command in cmd.exe.
You can install MethodScript using the same jar that is used in the Minecraft server,
though two different environments are used, with separate folders for the CommandHelper
installation, and the MethodScript installation. You can symlink these folders together if you
wish your configuration to be the same for both environments.

Commandline Tools: Various command line tools are available for use, and are useful
both for those that use the jar as a plugin, or as a general purpose language. Run
`java -jar MethodScript.jar help` for a list of these tools, or if you have installed
the commandline version, you can use `mscript -- help` on unix, or `mscript -Tool help`
on Windows.
