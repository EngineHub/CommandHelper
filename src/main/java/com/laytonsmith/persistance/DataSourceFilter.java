package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.PropertiesManager;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Given a File, creates a data source filter, which can be
 *
 * @author layton
 */
public class DataSourceFilter {

	/**
	 * Maps the regex-converted filter to a URI string. This is not stored
	 * as a URI, because it might have capture usages in it.
	 */
	private Map<Pattern, String> mappings = new HashMap<Pattern, String>();
	/**
	 * This maps the compiled pattern to the original in-config filter.
	 * So, hi\..*? would map to hi.**
	 */
	private Map<Pattern, String> original = new HashMap<Pattern, String>();
	/**
	 * This maps the split key to the URI string, for use in namespace comparisons.
	 */
	private Map<String[], String> namespaced = new HashMap<String[], String>();
	/**
	 * Since data lookups are expensive, cache them.
	 */
	private Map<String, URI> cache = new TreeMap<String, URI>();
	/**
	 * Namespace lookups are also expensive, so let's also cache the results.
	 */
	private Map<String, List<URI>> namespaceCache = new TreeMap<String, List<URI>>();

	/**
	 * Creates a new data source filter. This is represented by a file that
	 * contains mappings to the filters.
	 *
	 * @param file The file that contains the filter network
	 * @param defaultURI The URI to be used in the case that no ** filter is
	 * found in the file
	 * @throws FileNotFoundException If the file cannot be found
	 * @throws DataSourceException If any condition causes the data sources
	 * to be unreadable
	 * @throws NullPointerException If the defaultURI is null
	 */
	public DataSourceFilter(File file, URI defaultURI) throws IOException, DataSourceException {
		try {
			process(FileUtil.read(file), defaultURI);
		} catch (DataSourceException e) {
			throw new DataSourceException("Could not process filter file located at " + file.getAbsolutePath() + ": " + e.getMessage());
		}
	}

	/**
	 * Creates a new data source filter. This is represented by a string
	 * that contains mappings to the filters.
	 *
	 * @param filters The filter configuration that contains the filter
	 * network
	 * @param defaultURI The URI to be used in the case that no ** filter is
	 * found in the file
	 * @throws DataSourceException If any condition causes the data sources
	 * to be unreadable
	 * @throws NullPointerException If the defaultURI is null
	 */
	public DataSourceFilter(String filters, URI defaultURI) throws DataSourceException {
		process(filters, defaultURI);
	}

	private void process(String filters, URI defaultURI) throws DataSourceException {
		if (defaultURI == null) {
			throw new NullPointerException("defaultURI cannot be null");
		}

		PropertiesManager p = new PropertiesManager(filters);
		//We now have the whole list of filters=connection. We need to parse out and
		//find the connection aliases and normal filters, then parse those individually.
		//First, find the aliases, so we can go ahead and distribute those out when
		//we get to them.
		Map<String, String> aliases = new HashMap<String, String>();
		boolean hasDefault = false;
		for (String key : p.keySet()) {
			key = key.trim();
			if (key.matches("\\$[^a-zA-Z_]+")) {
				//Bad alias, bail                
				throw new DataSourceException("Aliases in your filters may not start with a digit.");
			}
			if (key.matches("\\$[a-zA-Z_][a-zA-Z0-9_]*")) {
				//It's an alias
				if(aliases.containsKey(key)){
					throw new DataSourceException("Duplicate aliases defined: " + key);
				}
				aliases.put(key, p.get(key));
			}
			if(key.equals("**")){
				hasDefault = true;
			}
		}


		//Ok, now let's load up the actual connections.
		for (String key : p.keySet()) {
			if (!key.matches("\\$.*")) {
				if (key.matches("[^a-zA-Z0-9_\\*]")) {
					//Bad character in the filter. Bail.
					throw new DataSourceException("Invalid character in filter. Only"
						+ " the following characters are allowed: a-zA-Z0-9_()*"
						+ " Found this instead: " + key);
				}
				
				String regexKey = toRegex(key);
				
				Pattern pattern = Pattern.compile(regexKey + "$");

				//Ok, have the pattern, now lets see if the value is an alias

				String value = p.get(key);
				String originalValue = value;
				//Used for more meaningful error messages below
				boolean isAlias = false;
				if (value.matches("\\$.*")) {
					if (!aliases.containsKey(value)) {
						throw new DataSourceException("Invalid alias: " + value + " is trying to be"
							+ " used, but has not been defined.");
					} else {
						value = aliases.get(value);
						isAlias = true;
					}
				}

				//Is this pattern already in the mapping? If so, we need to throw an error.
				if (mappings.containsKey(pattern)) {
					throw new DataSourceException("Multiple definitions exist for the key: " + key);
				}

				//Ok, finally, one last validation, we want to make sure that the URI at least
				//looks valid enough. Dollar signs aren't valid though, so lets replace our otherwise
				//valid capture usages.
				URI uriValue;
				try {
					uriValue = new URI(value);
				} catch (URISyntaxException e) {
					throw new DataSourceException("Invalid URI for " + value
						+ (isAlias ? "(Defined for alias " + originalValue + ")" : "") + ".");
				}
				//Alright. It's cool. Add it to the list.
				mappings.put(pattern, value);
				original.put(pattern, key);
				namespaced.put(key.split("\\."), value);
			}
			//else it's an alias, and we've already dealt with it
		}
		if(!hasDefault){
			Pattern m = Pattern.compile(".*?");
			mappings.put(m, defaultURI.toString());
			original.put(m, "**");
			namespaced.put(new String[]{"**"}, defaultURI.toString());
		}
	}
	
	/**
	 * Given a key filter, returns a regex pattern that is suitable for
	 * matching against actual keys.
	 * @param key
	 * @return 
	 */
	public static String toRegex(String key){
		//We need to change * into [^\.]*? and ** into .*? and . into \.
		//Parenthesis are kept as is.
		String newKey = key.replaceAll("\\.", "\\\\.");
		StringBuilder b = new StringBuilder("^");
		for (int i = 0; i < newKey.length(); i++) {
			Character c1 = newKey.charAt(i);
			Character c2 = null;
			if (i + 1 < newKey.length()) {
				c2 = newKey.charAt(i + 1);
			}
			if (c1 == '*' && c2 != null && c2 == '*') {
				//Double star
				b.append(".*?");
				i++;
			} else if (c1 == '*') {
				//Single star
				b.append("[^\\.]*?");
			} else {
				//Some other character
				b.append(c1);
			}
		}
		return b.toString();
	}

	/**
	 * Given a full key, returns the connection that contains it.
	 *
	 * @param key
	 * @return
	 */
	public URI getConnection(String[] key) {
		return getConnection(StringUtils.Join(key, "."));
	}

	/**
	 * Returns all the connections that actually match this namespace part.
	 *
	 * @param key
	 * @return
	 */
	public List<URI> getAllConnections(String[] key) {
		return getAllConnections(StringUtils.Join(key, "."));
	}

	/**
	 * Returns all the connections that actually match this namespace part.
	 * This is typically used to get a subset of keys based on namespace.
	 *
	 * @param key
	 * @return
	 */
	public List<URI> getAllConnections(String key) {
		if(namespaceCache.containsKey(key)){
			return new ArrayList<URI>(namespaceCache.get(key));
		}
		Map<String[], String> matches = new HashMap<String[], String>();
		String [] split = key.split("\\.");
		outer: for(String [] comparison : namespaced.keySet()){
			inner: for(int comparing = 0; comparing < split.length; comparing++){
				//If the length of the key is greater than this comparison, it's not a match, unless
				//the key had a ** in it at some point before this index
				if(arrayContains(comparison, "**", 0, comparing)){
					//Yes, it matches, regardless.
					matches.put(comparison, namespaced.get(comparison));
					break inner;
				} else if(comparison.length > comparing){
					//Ok, so we know that it has the correct number of parts.					
					String requestedPart = split[comparing];
					String myPart = comparison[comparing];
					if(myPart.contains("*")){
						//It's got a wildcard, so we need to convert it to a regex and compare from there
						String regexPart = toRegex(myPart);
						if(!requestedPart.matches(regexPart)){							
							continue outer;
						}
					} else {
						//Else it's a simple a string match
						if(!requestedPart.equals(myPart)){
							continue outer;
						}
					}
					//It matched this part, so let's continue with the investigation.
					//If this was the last part we need to compare, it's good, we can add it to the list now.
					if(comparing == split.length - 1){
						matches.put(comparison, namespaced.get(comparison));
						break inner;
					}
				}
			}
		}	
		
		List<URI> list = new ArrayList<URI>();
		for(String [] match : matches.keySet()){
			String uri = matches.get(match);
			try {
				list.add(new URI(uri));
			} catch (URISyntaxException ex) {
				//Won't happen
			}
		}
		namespaceCache.put(key, list);
		return new ArrayList<URI>(list);
	}
	
	private boolean arrayContains(String[] array, String contains, int from, int to){
		for(int i = from; i < (to + 1<=array.length?to + 1:array.length); i++){
			String part = array[i];
			if(part.contains(contains)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Given a full key, returns the connection that contains it.
	 *
	 * @param key
	 * @return
	 */
	public URI getConnection(String key) {
		//Since looking through these patterns, doing the matches, calculating string distance are all
		//fairly expensive operations, let's improve the runtime complexity by using a cache
		if (cache.containsKey(key)) {
			return cache.get(key);
		}
		List<Pattern> matches = new ArrayList<Pattern>();
		for (Pattern p : mappings.keySet()) {
			if (p.matcher(key).matches()) {
				matches.add(p);
			}
		}
		//Ok, we have a list of the actual matches, we have to narrow it down to the closest
		//match.
		Pattern closest = null;
		if (matches.isEmpty()) {
			//Trivial case
			return null;
		} else if (matches.size() == 1) {
			//Yay! Also a trivial case!            
			closest = matches.get(0);
		} else {
			int lowest = Integer.MAX_VALUE;
			for (Pattern p : matches) {
				//The closest match is defined as a filter that, minus wild cards, matches more characters.
				//So, for instance, if the key is a.b.c.d, then this matches a.*.c.d better than a.*.*.d
				//The easiest way to detect this is to simply remove * characters, and do a Levenshtein distance on the strings, and
				//whichever one is lowest, is the closest.
				String originalKey = original.get(p);
				int dist = StringUtils.LevenshteinDistance(key, originalKey.replaceAll("\\*", "").replaceAll("[\\(\\)]", ""));
				if (dist < lowest) {
					closest = p;
					lowest = dist;
				}
			}
		}

		try {
			if (closest == null) {
				return null;
			}
			String uri = mappings.get(closest);
			URI u = new URI(uri);
			//Store it in our cache
			cache.put(key, u);
			return u;
		} catch (URISyntaxException ex) {
			//We already verified that this won't happen, so yeah.
			return null;
		}
	}
}
