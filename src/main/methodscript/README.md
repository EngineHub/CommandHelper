This directory contains all the native source files for MethodScript. All the files here
are part of the core library, and are shipped with all installations. To speed up startup,
the files are serialized into a version specific binary at java compile time, which is deserialized at
startup, and is not recompiled, even if the rest of the scripts are.

All files in the native library are mandated to not only use strict mode, but UltraStrict mode,
and must comply with all checks and properly compile, otherwise, the project will not build.

The recommended IDE for development of these files is Visual Studio Code, with the [MethodScript extension](https://marketplace.visualstudio.com/itemdetails?itemName=MethodScriptVSC.methodscriptvsc "MethodScript VSC") installed.
