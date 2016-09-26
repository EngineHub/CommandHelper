(function ($, skel) {
    var resourceBase = "%%resourceBase%%";
    var $body = $("#body");
    var $learningTrail = $("#learningTrail");
    var learningTrailJSON = JSON.parse("%%js_string_learning_trail%%");
            function generateLearningTrail(learningTrail, asTable) {
                var lt = "";
                if (asTable) {
                    lt += "<table><thead><tr><th colspan=\"2\">Learning Trail</th></tr></thead><tbody>";
                } else {
                    lt += "<h1>Learning Trail</h1>";
                }
                learningTrail.forEach(function (v) {
                    //{"category": []}
                    var category = Object.keys(v)[0];
                    if (asTable) {
                        lt += "<tr><td>" + category + "</td><td>";
                    } else {
                        lt += "<h3>" + category + "</h3><ul>";
                    }
                    var pages = v[category];
                    var first = true;
                    pages.forEach(function (pageInfo) {
                        //{name: "", page: "", category: "", exists: ""}
                        var name = pageInfo["name"];
                        var page = pageInfo["page"];
                        var exists = pageInfo["exists"] === "true";
                        if (!first && asTable) {
                            lt += " · ";
                        } else {
                            first = false;
                        }
                        if (!asTable) {
                            lt += "<li>"
                        }
                        if (exists) {
                            lt += "<a href=\"%%siteRoot%%" + page + ".html\">" + name + "</a>";
                        } else {
                            lt += "<span class=\"redLink\">" + name + "</span>";
                        }
                        if (!asTable) {
                            lt += "</li>";
                        }
                    });
                    if (asTable) {
                        lt += "</td></tr>";
                    } else {
                        lt += "</ul>";
                    }
                });
                if (asTable) {
                    lt += "</tbody></table>";
                }
                return lt;
            }
    function render() {
        $body.html(wiky.process($("#body").html()));
        $body.html($body.html().replace(/{{TakeNote\|text=([\s\S]*?)}}/g, "<div class=\"TakeNote\"><strong>Note:</strong> $1</div>"));
        $body.html($body.html().replace(/\[(https?:\/\/.*?) (.*)\]/g, "<a href=\"$1\">$2</a>"));
        $body.html($body.html().replace(/__NOTOC__/g, ""));
        var internalLink = /\[\[(.*?)(?:\|(.*))?\]\]/g;
        var result;
        while ((result = internalLink.exec($body.html())) !== null) {
            var replacement = result[0];
            var link = result[1];
            var text = result[2] || null;
            if (text === null) {
                text = link.replace(/_/g, " ");
            }
            if (link.slice(0, 6) === "Image:") {
                // image
                $body.html($body.html().replace(replacement, "<img class= \"maxWidth100Percent\" src=\"" + resourceBase + "images/" + link.substring(6) + "\" alt=\"" + link + "\" />"));
            } else {
                // plain link
                $body.html($body.html().replace(replacement, "<a href=\"" + link + "\">" + text + "</a>"));
            }
        }
        if (/\{\{LearningTrail\}\}/.exec($body.html())) {
            console.log("Page contains references to {{LearningTrail}} template. They are being ignored.");
            $body.html($body.html().replace(/\{\{LearningTrail\}\}/, ""));
        }
        $body.removeClass("hidden");
        if (skel.getStateId().match(/medium/)) {
            renderSmall();
        } else {
            renderLarge();
        }
        // TODO: Render a TOC
    }
    function renderLarge() {
        $learningTrail.html(generateLearningTrail(learningTrailJSON, true));
    }

    function renderSmall() {
        $learningTrail.html(generateLearningTrail(learningTrailJSON, false));
    }
    
    function renderXSmall(){
        $body.addClass('bodyXSmall');
    }
    
    function renderNoXSmall(){
        $body.removeClass('bodyXSmall');
    }
    
    $(function () {
        render();
    });
    skel.on("change", function () {
        console.log("Current state is: " + skel.vars.stateId);
    });
    skel.on("-medium", renderLarge);
    skel.on("+medium", renderSmall);
    skel.on("-xsmall", renderNoXSmall);
    skel.on("+xsmall", renderXSmall);
})(jQuery, skel);
