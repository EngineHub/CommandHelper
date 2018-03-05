package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.TermColors;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This tool reads a file that contains full verbosity profiling data and creates a summary of the data.
 */
public class ProfilerSummary {

	private static final Pattern PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{1,2}:\\d{2}\\.\\d{2}:\\s*\\[(\\d+\\.\\d+)ms\\]\\[Lvl:(4|5)\\]:\\s*Executing function: ([a-zA-Z_]+[a-zA-Z0-9_]*)\\(.*\\).*");

	/**
	 * The actual profile file data
	 */
	private String data;
	/**
	 * Data that is below this percentage of total time is ignored. Defaults to 0.
	 */
	private double ignorePercentage = 0D;

	/**
	 * Creates a new ProfileAnalyzer object from an InputStream. It is expected that the input stream will contain UTF-8
	 * encoded data.
	 *
	 * @param input
	 */
	public ProfilerSummary(InputStream input) {
		this(StreamUtils.GetString(input));
	}

	/**
	 * Creates a new ProfileAnalyzer object from string data.
	 *
	 * @param data
	 */
	public ProfilerSummary(String data) {
		this.data = data;
	}

	/**
	 * Any functions that use up less than this percentage of total time are omitted from the report. This allows
	 * summaries to be more concise and show only the largest bottlenecks, instead of all data.
	 *
	 * @param percentage
	 */
	public void setIgnorePercentage(double percentage) {
		if(percentage < 0 || percentage > 1) {
			throw new IllegalArgumentException("The percentage must be between 0 and 1.");
		}
		this.ignorePercentage = percentage;
	}

	/**
	 * Returns a human readable getAnalysis of functions, given the arguments provided.
	 *
	 * @return Human readable results.
	 */
	public String getAnalysis() {
		Map<String, Double> functionData = new HashMap<>();
		Matcher m = PATTERN.matcher(data);
		double totalTime = 0;
		boolean foundLevel5 = false;
		while(m.find()) {
			String function = m.group(3);
			if("5".equals(m.group(2))) {
				foundLevel5 = true;
			}
			Double time = Double.parseDouble(m.group(1));
			totalTime += time;
			if(functionData.containsKey(function)) {
				functionData.put(function, functionData.get(function) + time);
			} else {
				functionData.put(function, time);
			}
		}
		if(!foundLevel5) {
			return "Analysis can only be done on a profile summary file, which was created with verbosity level 5.";
		}
		int originalSize = functionData.size();
		//Now, figure out which results to ignore
		for(String f : new ArrayList<>(functionData.keySet())) {
			if(functionData.get(f) / totalTime <= ignorePercentage) {
				//Remove it
				functionData.remove(f);
			}
		}
		//Now print out the data that remains
		StringBuilder b = new StringBuilder();
		b.append("Profiler data summary:\n\n");
		b.append(StringUtils.PluralTemplateHelper(functionData.size(), "One function was", "%d functions were")).append(" profiled in total");
		if(originalSize == functionData.size()) {
			b.append(".");
		} else {
			b.append(", and ");
			b.append(StringUtils.PluralTemplateHelper(originalSize - functionData.size(), "one function is", "%d functions are"));
			b.append(" being hidden from the report, as they are less than ").append((int) (ignorePercentage * 100)).append("% of the total time spent.");
		}
		b.append("\n\n");
		//Reverse the order, so higher usage functions go on top.
		Map<String, Double> sortedMap = sortByValue(functionData);
		List<String> keySet = new ArrayList<>(sortedMap.keySet());
		Collections.reverse(keySet);
		for(String f : keySet) {
			b.append(TermColors.WHITE).append(f).append(TermColors.RESET).append(": ").append(String.format("%.3f", sortedMap.get(f))).append(" ms\n");
		}
		return b.toString();
	}

	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for(Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
