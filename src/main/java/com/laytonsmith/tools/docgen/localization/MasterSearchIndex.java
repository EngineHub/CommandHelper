package com.laytonsmith.tools.docgen.localization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;

/**
 *
 */
public class MasterSearchIndex {

	private static class IndexEntry {
		private final List<String> segments;
		private final String title;
		private final ResultType type;

		public IndexEntry(String title, ResultType type) {
			this.segments = new ArrayList<>();
			this.title = title;
			this.type = type;
		}

		public void addSegment(String segment) {
			this.segments.add(segment);
		}

		public List<String> getSegments() {
			return this.segments;
		}

		public String getTitle() {
			return this.title;
		}

		public ResultType getType() {
			return type;
		}

	}

	private final Map<String, IndexEntry> segments = new HashMap<>();

	void addSegment(String title, String toLocation, ResultType type, String segment) {
		if(!segments.containsKey(toLocation)) {
			segments.put(toLocation, new IndexEntry(title, type));
		}
		segments.get(toLocation).addSegment(segment);
	}

	/**
	 * Creates the index file, ready to be written to the server. The general format of the file is a json file
	 * with the following schema:
	 * <p>
	 * The outermost element is a map. Each element of the map is a string containing the full search segment. The
	 * value of each element is an array of locations where this string occurs.
	 * <p>
	 * The intended search pattern is to loop through the segments finding segments where the strings exist, eliminating
	 * those index elements where the search text isn't found. Of the remaining segments, the next problem is to sort
	 * these by relevance. Priority should be given to segments that have a higher match percentage, so if the search
	 * string is "test" and the remaining segments are "Hello Test!", "Testing", and "Test", then they should be ranked
	 * as 1. "Test", 2. "Testing", and 3. "Hello Test!".
	 * <p>
	 * In any case, this logic is implemented client side.
	 * @return
	 */
	public String getIndex() {
		Map<String, List<Object>> index = new HashMap<>();
		for(Map.Entry<String, IndexEntry> entry : segments.entrySet()) {
			String location = entry.getKey();
			String title = entry.getValue().getTitle();
			for(String string : entry.getValue().getSegments()) {
				if(!index.containsKey(string)) {
					index.put(string, new ArrayList<>());
				}
				Map<String, String> locationData = new HashMap<>();
				locationData.put("location", location);
				locationData.put("title", title);
				locationData.put("type", entry.getValue().getType().name());
				index.get(string).add(locationData);
			}
		}
		return JSONObject.toJSONString(index);
	}

}
