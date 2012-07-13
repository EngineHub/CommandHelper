#!/bin/mscript

sys_out('blah')
sys_out('blarg')

if(2 + 2 != 4){ 
	sys_out('blip')
} else {
	sys_out('else')
}

sys_out(color(RED) . 'This should be red')
sys_out(color(UNDERLINE) . 'This should be underlined')
sys_out(color(ITALIC) . 'This should be italicized')
sys_out(color(STRIKETHROUGH) . 'This should be crossed out')
