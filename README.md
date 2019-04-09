CommandHelper
=============
| Service | Badge |
|--------|---------:|
| Travis | [![Travis Build Status](https://travis-ci.org/EngineHub/CommandHelper.svg?branch=master)](https://travis-ci.org/EngineHub/CommandHelper) |
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
