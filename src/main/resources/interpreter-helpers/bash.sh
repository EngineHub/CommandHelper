#!/bin/bash

#In order to use this file properly, put #!/usr/local/bin/mscript at the top of your
#script file, set it to executable, then run it with ./file.ms
#If you need to run it under debug conditions, export DEBUG_MSCRIPT=1
#and it will start up in suspended debug mode, on port 9001. (This is not changable)
#To put it back in normal operations, you can run unset DEBUG_MSCRIPT
#on a unix system.
if [ "$#" -eq 0 ]; then
		java -jar "%%LOCATION%%" interpreter --location $0
else
		SCRIPT="$1"
		shift 1
		if [ $SCRIPT = '--' ]; then
			# Script passthrough to java -jar CH.jar <arguments>
			java -Xrs -jar "%%LOCATION%%" $@
			exit
		fi
		if [ -z "$DEBUG_MSCRIPT" ]; then
			java -Xrs -jar "%%LOCATION%%" cmdline "$SCRIPT" "$@"
			exit
		else
			#Start in debug mode
			java -Xrs -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9001 -jar "%%LOCATION%%" cmdline "$SCRIPT" "$@"
			exit
		fi
fi