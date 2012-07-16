#!/bin/bash

if [ "$#" -eq 0 ]; then
        java -jar "%%LOCATION%%" --interpreter
else
        SCRIPT="$1"
        shift 1
        cat "$SCRIPT" | java -jar "%%LOCATION%%" --interpreter "$SCRIPT" "$@"
fi