#!/bin/bash

#In order to use this file properly, put #!/usr/local/bin/mscript at the top of your
#script file, set it to executable, then run it with ./file.ms
#If you need to run it under debug conditions, export DEBUG_MSCRIPT=1
#and it will start up in suspended debug mode, on port 9001. (This is not changable)
#To put it back in normal operations, you can run unset DEBUG_MSCRIPT
#on a unix system.

# These lines are required to get rid of the warnings in Java > 8. This is a shorter term fix.

jdk_version() {
  local result
  local java_cmd
  if [[ -n $(type -p java) ]]
  then
    java_cmd=java
  elif [[ (-n "$JAVA_HOME") && (-x "$JAVA_HOME/bin/java") ]]
  then
    java_cmd="$JAVA_HOME/bin/java"
  fi
  local IFS=$'\n'
  # remove \r for Cygwin
  local lines=$("$java_cmd" -Xms32M -Xmx32M -version 2>&1 | tr '\r' '\n')
  if [[ -z $java_cmd ]]
  then
    result=no_java
  else
    for line in $lines; do
      if [[ (-z $result) && ($line = *"version \""*) ]]
      then
        local ver=$(echo $line | sed -e 's/.*version "\(.*\)"\(.*\)/\1/; 1q')
        # on macOS, sed doesn't support '?'
        if [[ $ver = "1."* ]]
        then
          result=$(echo $ver | sed -e 's/1\.\([0-9]*\)\(.*\)/\1/; 1q')
        else
          result=$(echo $ver | sed -e 's/\([0-9]*\)\(.*\)/\1/; 1q')
        fi
      fi
    done
  fi
  echo "$result"
}

if [[ "$(jdk_version)" -gt "8" ]]; then
	# Read the required modules from the jar itself, so that each jar can have different dependencies, without requiring
	# a re-install.
	MODULES=$(unzip -p "%%LOCATION%%" interpreter-helpers/modules)
	# Ensure that the modules file will work even if it has windows line endings
	MODULES=$(echo "$MODULES" | sed 's/\r$//g')
	MPATH=$(echo "$MODULES" | sed 's/\(.*\)/--add-opens \1=ALL-UNNAMED/' | tr '\n' ' ')
else
	MPATH=""
fi

DEBUG=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9001
if [ -z "$DEBUG_MSCRIPT" ]; then
	DEBUG=""
fi


if [ "$#" -eq 0 ]; then
		java $DEBUG $MPATH -jar "%%LOCATION%%" interpreter --location----- $(pwd)
else
		SCRIPT="$1"
		shift 1
		if [ $SCRIPT = '--' ]; then
			# Script passthrough to java -jar CH.jar <arguments>
			java $DEBUG $MPATH -Xrs -jar "%%LOCATION%%" $@
			exit
		fi

		java $DEBUG $MPATH -Xrs -jar "%%LOCATION%%" cmdline "$SCRIPT" "$@"
		exit
fi