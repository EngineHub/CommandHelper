if exists("b:current_syntax")
  finish
endif

syn keyword restrictedFunctions %%space:functions:restricted%%
syn keyword unrestrictedFunctions %%space:functions:unrestricted%%
syn keyword events %%space:events%%
syn keyword enums %%space:enums%%
syn keyword unsymboledkeywords %%space:keywords%%
syn keyword classTypes %%space:types%%
syn match linecomment /#.*/
syn match linecomment /\/\/.*/
syn region blockcomment start=/\/\*/ end=/\*\//
syn region stringlit start=/'/ skip=/\\'/ end=/'/
syn region smartstring start=/"/ skip=/\\"/ end=/"/
	\ contains=ivariable,smartIVariable
syn region smartIVariable start="@{" end="}" contained
syn match ivariable /@[a-zA-Z0-9_]\+/
syn region fileOptions start="<!" end='>'
syn match fileOptionsLabel /\v(%%pipe:fileOptions%%)(:|;)/ containedin=fileOptions
syn match variable /\$[a-zA-Z0-9_]\+/
syn match finalvar /\$[^a-zA-Z0-9_]/
syn match label /[a-zA-Z_][a-zA-Z0-9_]\+:/

let b:current_syntax = "commandhelper"

highlight restrictedFunctions ctermfg=DarkCyan guifg=#1265A9
highlight unrestrictedFunctions ctermfg=DarkGreen guifg=#B35900
highlight events ctermfg=LightMagenta guifg=#B30059
highlight enums ctermfg=yellow guifg=#5900B3
highlight classTypes ctermfg=LightCyan guifg=LightCyan
highlight unsymboledkeywords ctermfg=LightBlue guifg=LightBlue
highlight linecomment ctermfg=LightGray guifg=LightGray
highlight blockcomment ctermfg=LightGray guifg=LightGray
highlight stringlit ctermfg=3 guifg=orange
highlight smartstring ctermfg=3 guifg=orange
highlight ivariable ctermfg=green guifg=green
highlight smartIVariable ctermfg=green guifg=green
highlight variable ctermfg=LightCyan guifg=LightCyan
highlight finalvar ctermfg=DarkCyan guifg=DarkCyan
highlight label ctermfg=yellow guifg=yellow
highlight fileOptions ctermfg=grey guifg=grey
highlight fileOptionsLabel ctermfg=blue guifg=blue