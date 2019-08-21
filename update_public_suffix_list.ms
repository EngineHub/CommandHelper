#!/usr/bin/env /usr/local/bin/mscript
<!
	strict;
	name: update_public_suffix_list.ms;
	created: 2018-12-18;
	description: Updates the public-suffix.txt file with the latest version;
>

http_request('https://publicsuffix.org/list/public_suffix_list.dat', array(
	success: closure(){
		msg('Download complete');
	},
	error: closure(@ex) {
		msg(@ex);
	},
	headers: array(
		'cache-control': 'max-age=0'
	),
	download: './src/main/resources/public-suffix.dat',
	downloadStrategy: 'OVERWRITE',
	blocking: true,
	log: true
));
