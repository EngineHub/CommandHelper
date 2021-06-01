
// This script is only intended for use on the downloads page.
pageRender.then(function() {
	$.getJSON("https://apps.methodscript.com/builds/commandhelperjar", function(data) {
		console.log("Got " + data.length + " results");
		console.log(data);
		data.sort(function(a, b){
			if(a.date === b.date) {
				return 0;
			}
			return (new Date(a.date).getTime() > new Date(b.date).getTime() ? -1 : 1);
		});
		var html = "<div><ul>";
		var first = true;
		for (const version of data) {
			html += "<li><a href='https://apps.methodscript.com" + version.link + "'>" + version.buildId + "</a>";
			if(first) {
				html += " (Recommended)";
				first = false;
			}
			html += "</li>";
		}
		html += "</ul></div>";
		$('#downloadList').html(html);
	});
});