
var search = {};

search.index = null;

search.load = function(docsBase, skel) {	

	function searchIndex(string) {
		let matches = {};
		// Probably need to make this a tree, rather than a linear lookup, but this seems to have good
		// enough performance for now.
		for (const property in search.index) {
			let s = property;
			let pages = search.index[s];
			if(s === string || s.toUpperCase().match(string.toUpperCase())) {
				matches[s] = pages;
			}
		}
		return matches;
	}

	function setupNav() {
		$('.searchNav').show();
		let $body = $('body');
		let $sbSearch = $('.sb-search');
		let $sbSearchInput = $('input.sb-search-input');
		let $sbSearchSubmit = $('input.sb-search-submit');
		let $sbSearchIcon = $('span.sb-icon-search');
		let $sbResult = $('.sb-search-results');
		let $sbResultLoading = $('.sb-search-results-loading');
		let $sbResultNothing = $('.sb-search-results-nothing');
		let $sbResultFound = $('.sb-search-results-found');
		let $sbIconNormal = $('.sb-icon-search-normal');
		let $sbIconClose = $('.sb-icon-search-close');

		function resetUI() {
			$sbSearch.removeClass('sb-search-open');
			$sbSearchInput.val("");
			$sbResult.hide();
			$sbIconNormal.show();
			$sbIconClose.hide();
			$sbResultLoading.show();
			$sbResultNothing.hide();
			$sbResultFound.hide();
		}

		function formatResults(results) {
			$sbResultLoading.hide();
			if(Object.keys(results).length === 0) {
				$sbResultNothing.show();
				$sbResultFound.hide();
			} else {
				$sbResultNothing.hide();
				$sbResultFound.show();
				$sbResultFound.empty();
				let resultSet = new Set();
				for(var string in results) {
					for(var page in results[string]) {
						// Ughhhhh JavaScript Set only really works with primitives, since {} !== {}, so we stringify
						// the object before putting it in the set.
						resultSet.add(JSON.stringify(results[string][page]));
					}
				}

				for(let entry of resultSet) {
					entry = JSON.parse(entry);
					entry.location = entry.location.replace('%s', docsBase + '../..');
					// TODO: Remove this
					entry.location = entry.location.replace('.tmem.xml', '.html');
					$sbResultFound.append("<li><a href=\"" + entry.location + "\">" + entry.title 
							+ " <span class=\"sb-search-result-type\">"
							+ entry.type
							+ "</span></></li>");
				}
			}
		}

		$sbSearchIcon.on('click', function() {
			$sbSearch.addClass('sb-search-open');
			$sbIconNormal.hide();
			$sbIconClose.show();
			$sbSearchInput.focus();
			$body.on('mousedown', function(bodyEvent) {
				if(!$sbSearch.has(bodyEvent.target).length) {
					resetUI();
					$body.off('mousedown');
				}
			});
			return false;
		});

		$sbSearchSubmit.on('click', function() {
			if($sbSearchInput.val()) {
				// Do search
				formatResults(searchIndex($sbSearchInput.val()));
			} else {
				$sbSearch.removeClass('sb-search-open');
				$sbIconNormal.show();
				$sbIconClose.hide();
			}
			return false;
		});

		let previousTimeouts = 0;

		$sbSearchInput.on('keyup', function(event) {
			if(event.key === "Escape") {
				resetUI();
				return false;
			}
			let val = $sbSearchInput.val();
			if(val === "") {
				$sbResult.hide();
				$sbIconNormal.hide();
				$sbIconClose.show();
			} else {
				setTimeout(function(){
					previousTimeouts--;
					// console.log("In timeout " + previousTimeouts);
					if(previousTimeouts === 0) {
						formatResults(searchIndex(val));
					}
				}, 1000);
				// console.log("Created timeout " + previousTimeouts);
				previousTimeouts++;
				$sbResult.show();
				$sbIconNormal.show();
				$sbIconClose.hide();
			}
		});
	}

	$.getJSON(docsBase + "searchIndexExists.json", null, function(data) {
		if(data && data.hasOwnProperty("searchIndexExists") && data.searchIndexExists) {
			$.getJSON(docsBase + "searchIndex.json", null, function(data) {
				search.index = data;
				$(function() {
					setupNav();
				});
			});
		}
	});
	
	// For small screens, the search bar should just be hidden for now. In the 
	// future, there's no reason it can't be made to work, but it won't work as
	// is, and it looks like it will be a decent amount of work to get it working,
	// so save that for later.
	function turnOff() {
		$(".searchNav").hide();
	}
	
	function turnOn() {
		$(".searchNav").show();
	}
	
	skel.on("-medium", turnOn); // large
    skel.on("+medium", turnOff); // small
};
