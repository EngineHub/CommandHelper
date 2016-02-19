#!/bin/bash

_mscript(){
	local cur
	cur=${COMP_WORDS[COMP_CWORD]}
	if [[ ${COMP_WORDS[1]} = '--' && $COMP_CWORD = 2 ]]; then
		COMPREPLY=( $( compgen -W '%%COMMANDS%%' -- $cur));
	fi
	if [[ ${COMP_WORDS[2]} = 'syntax' ]]; then
		COMPREPLY=( $( compgen -W 'npp textwrangler geshi vim nano' -- $cur));
		if [[ ${COMP_WORDS[3]} = 'npp' ]]; then
			COMPREPLY=( $( compgen -W 'default obsidian' -- $cur));
		fi
	fi
	return 0;
}
complete -o default -o nospace -F _mscript mscript