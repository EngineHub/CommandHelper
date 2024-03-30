
// This script is only intended for use on the downloads page.
(function() {

	function escapeHTML(unsafeText) {
		let div = document.createElement('div');
		div.innerText = unsafeText;
		return div.innerHTML;
	}

	function formatList(archive, id, showRecommended=true) {
		$.getJSON("https://apps.methodscript.com/builds/" + archive, function(data) {
			console.log("Got " + data.length + " results for " + archive);
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
				html += "<li><a href='https://apps.methodscript.com" + version.link + "'>";
				html += version.buildId + "</a> ";
				if(archive === "commandhelperjar") {
					// This is the only one that has meta information at this stage
					if(version.sha) {
						html += "[";
						if(version.commitDetails) {
							html += "<span id=\"commit_sha_" + version.sha + "\">";
						}
						html += "<a href='https://github.com/EngineHub/CommandHelper/commit/" + version.sha + "'>"
							+ version.sha.substring(0, 7) + "</a>"
						if(version.commitDetails) {
							html += "</span>";
						}
						html += "] ";

						if(version.commitDetails) {
							pageRender.then(function() {
								setTimeout(function() {
									$("#commit_sha_" + version.sha).tooltipster({
										content: $("<code><pre style='white-space: pre-wrap;'>"
											+ escapeHTML(version.commitDetails)
											+ "</pre></code>")
									});
								}, 1);
							});
						}
					}
				}
				if(showRecommended && first && !version.poisoned) {
					html += " (Recommended)";
					first = false;
				}
				if(version.poisoned) {
					html += " (Bad build)";
				}
				html += "</li>";
			}
			html += "</ul></div>";
			pageRender.then(function() {
				$('#' + id).html(html);
			});
		});
	}
	formatList("commandhelperjar", "commandhelperjar");
	formatList("commandhelperpinned", "commandhelperpinned", false);
	formatList("commandhelperwindowsinstaller", "commandhelperwindowsinstaller");

	$(".commit_tooltip").tooltipster({
		contentCloning: true,
		interactive: false,
		theme: 'tooltipster-shadow',
		maxWidth: 400,
		delay: 600,
		functionBefore: function (instance, helper) {
			var f = helper.origin.innerText;
			console.debug("Triggering tooltip for " + f);
			api.done(function (resp) {
				var fd = resp.functions[f];
				if (fd !== null) {
					var $tt = $(instance.content());
					$tt.find(".ret").html(fd.ret);
					$tt.find(".name").html(fd.name);
					$tt.find(".args").html(fd.args);
					var p = doStandardReplacement(fd.desc);
					p.then(function (rep) {
						$tt.find(".desc").html(rep);
						instance.reposition();
					});
				}
			});
		}
	});
})();
