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

	private final Map<String, List<String>> segments = new HashMap<>();

	void addSegment(String toLocation, String segment) {
		if(!segments.containsKey(toLocation)) {
			segments.put(toLocation, new ArrayList<>());
		}
		segments.get(toLocation).add(segment);
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
		Map<String, List<String>> index = new HashMap<>();
		for(Map.Entry<String, List<String>> entry : segments.entrySet()) {
			String location = entry.getKey();
			for(String string : entry.getValue()) {
				if(!index.containsKey(string)) {
					index.put(string, new ArrayList<>());
				}
				index.get(string).add(location);
			}
		}
		return JSONObject.toJSONString(index);
	}

}
