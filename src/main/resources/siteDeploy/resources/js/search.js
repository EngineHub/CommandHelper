
var search = {};

search.index = null;

search.load = function(docsBase) {

	function searchIndex(string) {
		let matches = {};
		// Probably need to make this a tree, rather than a linear lookup
		for (const property in search.index) {
			let s = property;
			let pages = search.index[s];
			if(s === string || s.match(string)) {
				matches[s] = pages;
			}
		}
		return matches;
	}

	function setupNav() {
		$('.searchNav').show();
		$body = $('body');
		$sbSearch = $('.sb-search');
		$sbSearchInput = $('input.sb-search-input');
		$sbSearchSubmit = $('input.sb-search-submit');
		$sbSearchIcon = $('span.sb-icon-search');
		$sbResult = $('.sb-search-results');
		$sbResultLoading = $('.sb-search-results-loading');
		$sbResultNothing = $('.sb-search-results-nothing');
		$sbResultFound = $('.sb-search-results-found');
		$sbIconNormal = $('.sb-icon-search-normal');
		$sbIconClose = $('.sb-icon-search-close');

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
						resultSet.add(results[string][page]);
					}
				}

				for(let entry of resultSet) {
					entry = entry.replace('%s', docsBase + '../..');
					// TODO: Remove this
					entry = entry.replace('.tmem.xml', '.html');
					$sbResultFound.append("<li><a href=\"" + entry + "\">" + entry + "</></li>");
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
				formatResults(searchIndex($sbSearchInput.val()))
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
			if(val == "") {
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
			$.getJSON("searchIndex.json", null, function(data) {
				search.index = data;
				$(function() {
					setupNav();
				});
			});
		}
	});
};
