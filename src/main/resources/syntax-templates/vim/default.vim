if exists("b:current_syntax")
  finish
endif

syn keyword restrictedFunctions %%space:functions:restricted%% 
syn keyword unrestrictedFunctions %%space:functions:unrestricted%% 
syn keyword events %%space:events%% 
syn keyword colors %%space:colors%% 
syn keyword unsymboledkeywords %%space:keywords%%
syn match linecomment /#.*/
syn region blockcomment start=/\/\*/ end=/\*\//
syn region stringlit start=/'/ skip=/\\'/ end=/'/
syn region smartstring start=/"/ skip=/\\"/ end=/"/
syn match ivariable /@[a-zA-Z0-9]\+/
syn match variable /\$[a-zA-Z0-9]\+/
syn match finalvar /\$[^a-zA-Z0-9]/
syn match label /[a-zA-Z][a-zA-Z0-9]\+:/

let b:current_syntax = "commandhelper"

highlight restrictedFunctions ctermfg=DarkCyan guifg=#1265A9
highlight unrestrictedFunctions ctermfg=DarkGreen guifg=#B35900
highlight events ctermfg=LightMagenta guifg=#B30059
highlight colors ctermfg=yellow guifg=#5900B3
highlight unsymboledkeywords ctermfg=LightBlue guifg=LightBlue
highlight linecomment ctermfg=LightGray guifg=LightGray
highlight blockcomment ctermfg=LightGray guifg=LightGray
highlight stringlit ctermfg=3 guifg=orange
highlight smartstring ctermfg=white ctermbg=red guifg=white guibg=red
highlight ivariable ctermfg=green guifg=green
highlight variable ctermfg=LightCyan guifg=LightCyan
highlight finalvar ctermfg=6 guifg=6
highlight label ctermfg=yellow guifg=yellow