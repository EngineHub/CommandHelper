(function ($, skel, wiky, bodyEscaped, showLearningTrail, pageRender) {
    var resourceBase = "%%resourceBase%%";
    var docsBase = "%%docsBase%%";
    var $body = $("#body");
    var $learningTrail = $("#learningTrail");
    var learningTrailJSON = JSON.parse("%%js_string_learning_trail%%");
    var apiURL = docsBase + "api.json?v=" + apiJsonVersion;
    var api;
    var apiData = sessionStorage.getItem(apiURL);
    if(apiData) {
        api = $.Deferred();
        api.resolve(JSON.parse(apiData));
    } else { 
        api = $.getJSON(apiURL).promise();
    }
    api.fail(function() {
        console.log("Could not load api.json");
        console.log(arguments);
    });
    function generateLearningTrail(learningTrail, asTable) {
        if (!showLearningTrail) {
            return;
        }
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
                    lt += "<li>";
                }
                if (exists) {
                    lt += "<a href=\"%%docsBase%%" + (page.match(/\./g) ? page : page + ".html") + "\">" + name + "</a>";
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
    function doStandardReplacement(html) {
        var promise = $.Deferred();
        api.done(function (resp) {
            sessionStorage.setItem(apiURL, JSON.stringify(resp));
            (function () {
                var r = /{{function\|(.*?)}}/g;
                var match;
                while ((match = r.exec(html)) !== null) {
                    if (typeof (resp.functions[match[1]]) !== "undefined") {
                        html = html.substr(0, match.index) + "<a href=\"" + docsBase + "API/functions/" + match[1] + "\">"
                                + "<span class=\"function_tooltip tt_cursor\" data-tooltip-content=\"#function_tooltip_content\">"
                                + match[1]
                                + "</span></a>"
                                + html.substr(match.index + match[0].length);
                    } else {
                        html = html.substr(0, match.index)
                                + match[1]
                                + html.substr(match.index + match[0].length);
                    }
                }
            })();
            // TODO
            (function () {
                var r = /{{keyword\|(.*?)}}/g;
                var match;
                while ((match = r.exec(html)) !== null) {
                    html = html.substr(0, match.index)
                            + "<span class=\"keyword_tooltip tt_cursor\" data-tooltip-content=\"#keyword_tooltip_content\">"
                            + match[1]
                            + "</span>"
                            + html.substr(match.index + match[0].length);
                }
            })();
            html = html.replace(/{{object\|(.*?)}}/g, "$1");
            return promise.resolve(html);
        });
        return promise.promise();
    }

    function render() {
        var html = bodyEscaped;
        html = html.replace(/\\\n/g, '');
        html = html.replace(/\n\n/g, '\n<p>\n');
        html = html.replace(/\n\s*\n/g, '\n');
        html = wiky.process(html);
        html = html.replace(/{{TakeNote\|text=([\s\S]*?)}}/g, "<div class=\"TakeNote\"><strong>Note:</strong> $1</div>");
        html = html.replace(/{{Warning\|text=([\s\S]*?)}}/g, "<div class=\"Warning\"><strong>Warning:</strong> $1</div>");
        html = html.replace(/\[(https?:\/\/.*?) (.*)\]/g, "<a href=\"$1\">$2</a>");
        html = html.replace(/__NOTOC__/g, "");
        var internalLink = /\[\[(.*?)(?:\|(.*?))?\]\]/g;
        var result;
        while ((result = internalLink.exec(html)) !== null) {
            var replacement = result[0];
            var link = result[1];
            var text = result[2] || null;
            if (text === null) {
                text = link.replace(/_/g, " ");
            }
            if (link.slice(0, 6) === "Image:") {
                // image
                html = html.replace(replacement, "<img class= \"maxWidth100Percent\" src=\"" + resourceBase + "images/" + link.substring(6) + "\" alt=\"" + link + "\" />");
            } else {
                // plain link
                html = html.replace(replacement, "<a href=\"" + link + "\">" + text + "</a>");
            }
        }
        if (/\{\{LearningTrail\}\}/.exec(html)) {
            console.log("Page contains references to {{LearningTrail}} template. They are being ignored.");
            html = html.replace(/\{\{LearningTrail\}\}/, "");
        }

        html = renderWikiTables(html);

        var htmlPromise = doStandardReplacement(html);

        htmlPromise.then(function (html) {

            if (skel.getStateId().match(/xsmall/)) {
                renderXSmall();
            } else {
                renderNoXSmall();
            }
            if (skel.getStateId().match(/medium/)) {
                renderSmall();
            } else {
                renderLarge();
            }
            // TODO: Render a TOC

            $body.html(html);
            $body.removeClass("hidden");
            $body.css('display', 'inline');

            $(".function_tooltip").tooltipster({
                contentCloning: true,
                interactive: true,
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
            $(".keyword_tooltip").tooltipster({
                contentCloning: true,
                interactive: true,
                theme: 'tooltipster-shadow',
                maxWidth: 400,
                delay: 600,
                functionBefore: function (instance, helper) {
                    var f = helper.origin.innerText;
                    console.debug("Triggering tooltip for " + f);
                    api.done(function (resp) {
                        var fd = resp.keywords[f];
                        if (fd !== null) {
                            var $tt = $(instance.content());
                            $tt.find(".desc").html(fd.docs);
                        }
                        instance.reposition();
                    });
                }
            });
            if (window.location.hash) {
                $(window.location.hash)[0].scrollIntoView({behavior: "smooth"});
            }
            pageRender.resolve();
        });
    }
    function renderLarge() {
        $learningTrail.html(generateLearningTrail(learningTrailJSON, true));
    }

    function renderSmall() {
        $learningTrail.html(generateLearningTrail(learningTrailJSON, false));
    }

    function renderXSmall() {
        $body.addClass('bodyXSmall');
    }

    function renderNoXSmall() {
        $body.removeClass('bodyXSmall');
    }

    function renderWikiTables(html) {
        function renderTable(html) {
            function join(arry, glue) {
                var first = true;
                var ret = "";
                for(var i = 0; i < arry.length; i++) {
                    if(!first) {
                        ret += glue;
                    }
                    first = false;
                    ret += arry[i];
                }
                return ret;
            }
            // The string sent here is just the text within the table
            var ret = "";
            // This is an array of the lines
            var lines = html.split(/\n/g);
            
            var firstRow = true;
            var lastCellWasTH = false;
            var firstCell = true;
            var finishLast = false;
            // lines[0] is the table attributes (if present)
            ret += "<table " + lines[0] + ">";
            for(var i = 1; i < lines.length; i++) {
                var line = lines[i].trim();
                if(line.match(/^\|-/)) {
                    // new row
                    if(firstRow) {
                        firstRow = false;
                    } else if(finishLast) {
                        // also close out the last cell
                        ret += lastCellWasTH ? "</th>" : "</td>";
                        ret += "</tr>";
                    }
                    var attrRow = "";
                    var q;
                    if((q = line.match(/^\|-(.+)$/))) {
                        attrRow = q[1].trim();
                    }
                    ret += "<tr " + attrRow + ">";
                    firstCell = true;
                    continue;
                }
                if(line.match(/^!/)) {
                    // table header start
                    if(firstCell) {
                        firstCell = false;
                    } else if(finishLast) {
                        ret += lastCellWasTH ? "</th>" : "</td>";
                    }
                    lastCellWasTH = true;
                    line = line.substr(1);
                    // Some lines start with:
                    // ! attr="stuff" | header text
                    // In this case, we need to parse out the things between ! and | and put those in the the tag
                    var r;
                    var attr = "";
                    if((r = line.match(/(.*?)\|(.*)/))) {
                        attr = r[1].trim();
                        line = r[2].trim();
                    }
                    ret += "<th " + attr + ">" + join(line.split(/!!/g), "</th><th>");
                    finishLast = true;
                } else if(line.match(/^\|/)) {
                    // normal cell start
                    if(firstCell) {
                        firstCell = false;
                    } else if(finishLast) {
                        ret += lastCellWasTH ? "</th>" : "</td>";
                    }
                    lastCellWasTH = false;
                    ret += "<td>" + join(line.substr(1).split(/\|\|/g), "</td><td>");
                    finishLast = true;
                } else {
                    // continuation of previous line, just output the line with \n
                    ret += "\n" + line;
                }
            }
            ret += "<tr></table>";
            return ret;
        }
        var newHtml = "";
        var tableHtml = [];
        var tableIndex = 0;
        for (var i = 0; i < html.length; i++) {
            var c = html[i];
            var c2 = html[i + 1];
            if(c === '{' && c2 === '|') {
                tableIndex++;
                tableHtml[tableIndex] = "";
                i++; continue;
            }
            if(c === '|' && c2 === '}') {
                tableIndex--;
                if(tableIndex > 0) {
                    tableHtml[tableIndex] += renderTable(tableHtml[tableIndex + 1]);
                } else {
                    newHtml += renderTable(tableHtml[tableIndex + 1]);
                }
                i++; continue;
            }
            if(tableIndex > 0) {
                tableHtml[tableIndex] += c;
            } else {
                newHtml += c;
            }
        }
        return newHtml;
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
})(jQuery, skel, wiky, bodyEscaped, showLearningTrail, pageRender);
