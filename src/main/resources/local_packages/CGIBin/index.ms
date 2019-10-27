
#!/usr/bin/env /usr/local/bin/mscript

// This is extremely rudimentary, but it works well, and is only a few lines of code,
// and requires no special support in MethodScript!
if(string_contains(get_env('QUERY_STRING'), 'src=true')) {
	if(!has_value('code_hit_count')) {
		store_value('code_hit_count', 0);
	}
	store_value('code_hit_count', get_value('code_hit_count') + 1);
	// If they have requested the source code, print out ourselves, with the text/plain content type.
	msg("Content-type: text/plain\n");
	msg(read(reflect_pull('file')));
} else {
	msg("Content-type: text/html\n\n");
	msg("<html>");
	msg('<head><link rel="stylesheet" href="styles.css"></head>');
	msg("<body><h1>Hello MethodScript World!</h1><br>");
	msg("<div>Click <a href=\"" . get_env('REQUEST_URI') . "?src=true\">here</a> to see the source code.</div>");
	msg('<h2>Environment values</h2>');
	msg("<table>");
	foreach(@key: @value in get_env()) {
		msg("<tr><td>@key</td><td>@value</td></tr>");
	}
	msg("</table>");
	if(!has_value('hit_count')) {
		store_value('hit_count', 0);
	}
	@hitCount = get_value('hit_count') + 1;
	store_value('hit_count', @hitCount);
	msg('<h2>Server data</h2><table>');
	@serverData = array(
		'Engine Build Date': simple_date('yyyy-MM-dd', engine_build_date()),
		'Engine Location': engine_location(),
		'Hit Count': @hitCount,
		'Source Code Hit Count': get_value('code_hit_count'),
	);
	foreach(@key: @value in @serverData) {
		msg("<tr><td>@key</td><td>@value</td></tr>");
	}
	msg('</table>');
}

