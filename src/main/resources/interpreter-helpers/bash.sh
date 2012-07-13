#!/bin/bash

if [ "$#" -eq 0 ]; then
        java -jar ~/NetBeansProjects/CommandHelper/target/commandhelper-* --interpreter
else
        SCRIPT=$1
        shift 1
        ARGUMENTS="assign(@arguments, array("
        FIRST=1
        while (( "$#" )); do
                if [ $FIRST -eq 1 ]; then
                        ARGUMENTS="$ARGUMENTS, "
                fi
                FIRST=0
                #TODO Finish this
                shift 1
        done
        cat "$1" | java -jar ~/NetBeansProjects/CommandHelper/target/commandhelper-* --interpreter
fi