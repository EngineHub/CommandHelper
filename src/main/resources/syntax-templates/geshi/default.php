<?php
/***************************************
 * commandhelper.php
 * Author: Deaygo Jarkko (deaygo@thezomg.com)
 * Release Version: 0.0.1
 * Date Started: 2012/01/23
 *
 * CommandHelper language file for GeSHi
 *
 * 2012/01/23 (0.0.1)
 *  - First Release
 ****************************************************/
$language_data = array(
    'LANG_NAME' => 'CommandHelper',
    'COMMENT_SINGLE' => array(1 => '#'),
    'HARD_QUOTE' => array("'", "'"),
    'HARDESCAPE' => array("\\"),
    'HARDCHAR' => array("\\"),
    'CASE_KEYWORDS' => GESHI_CAPS_NO_CHANGE,
    'QUOTEMARKS' => array('"', "'"),
    'ESCAPE_REGEXP' => array(
            //Simple Single Char Escapes
            1 => "#\\\\[nfrtv\$\"\n\\\\]#i",
            2 => "#[\\$\\@][a-zA-Z0-9]+"
    ),
    'NUMBERS' => GESHI_NUMBER_INT_BASIC,
    'KEYWORDS' => array(
        // Non-Restricted
        1 => array(
            %%FUNCTIONS_QUOTED_COMMA_SEPARATED_UNRESTRICTED%%
        ),
        // Restricted
        2 => array(
             %%FUNCTIONS_QUOTED_COMMA_SEPARATED_RESTRICTED%%
        ),
        3 => array(
             %%EVENTS_QUOTED_COMMA_SEPARATED%%
        ),
        4 => array(
            %%COLOR_LIST_QUOTED_COMMA_SEPARATED%%
        )
    ),
    'SYMBOLS' => array (
        0 => array(
            '(', ')', '[', ']', '{', '}',
            '$', '@', '<', '>',
            '=', ':', ',', '>>>', '<<<'
        )
    ),
    'CASE_SENSITIVE' => array(
        GESHI_COMMENTS => false,
        1 => false,
        2 => false
    ),
    'STYLES' => array(
        'KEYWORDS' => array(
            1 => 'color: #1265A9;',
            2 => 'color: #B35900;',
            3 => 'color: #B30059;',
            4 => 'color: #5900B3;'
        ),
        'COMMENTS' => array(
            1 => 'color: #666666; font-weight: bold;'
        ),
        'ESCAPE_CHAR' => array(
            1 => 'color: #000099; font-weight: bold;'
        ),
        'BRACKETS' => array(
            0 => 'color: #009900;'
        ),
        'STRINGS' => array(
            0 => 'color: #0000ff;',
            'HARD' => 'color: #0000ff;'
        ),
        'NUMBERS' => array(
            0 => 'color: #cc66cc;'
        ),
        'METHODS' => array(
            1 => 'color: #004000;',
            2 => 'color: #004000;'
        ),
        'SYMBOLS' => array(
            1 => 'color: #339933;',
            2 => 'color: #339933;'
        ),
        'REGEXPS' => array(
            0 => 'color: #000088;'
        )
    ),
    'URLS' => array(
        1 => 'http://wiki.sk89q.com/wiki/CommandHelper/API/{FNAMEL}',
        2 => 'http://wiki.sk89q.com/wiki/CommandHelper/API/{FNAMEL}'
    ),
    'OOLANG' => false,
    'REGEXPS' => array(
        //Variables
        0 => "[\\$\\@][a-zA-Z00-9_]+"
    ),
    'STRICT_MODE_APPLIES' => GESHI_MAYBE,
    'TAB_WIDTH' => 4,
    'PARSER_CONTROL' => array(
        'KEYWORDS' => array(
            'DISALLOWED_AFTER' =>  "(?![\.\-a-zA-Z0-9_%=\\/\&])",
            'DISALLOWED_BEFORE' => "(?<![\.\-a-zA-Z0-9_\$\@\#\&])"
        )
    )
);
?>
