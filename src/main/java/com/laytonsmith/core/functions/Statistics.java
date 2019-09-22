package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@core
public class Statistics {

	public static String docs() {
		return "Provides a set of functions that deal with statistical analysis of numbers.";
	}

	public abstract static class StatisticsFunction extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREIndexOverflowException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.NO_SIDE_EFFECTS,
					OptimizationOption.CACHE_RETURN);
		}

	}

	@api
	public static class average extends StatisticsFunction {

		@Override
		public CNumber exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			long count;
			if(args.length == 1 && args[0].isInstanceOf(CArray.TYPE)) {
				CArray c = ArgumentValidation.getArray(args[0], t);
				count = c.size();
			} else {
				count = args.length;
			}
			double sum = new sum().exec(t, environment, args).getNumber();
			return new CDouble(sum / count, t);
		}

		@Override
		public String getName() {
			return "average";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "number {array<number> | number input...} Returns the average (also known as the arithmetic mean) across all"
					+ " the numbers in the set. The input may be an array of numbers, or individual numbers as"
					+ " arguments. The average of a set of numbers is the result of adding all the numbers in the"
					+ " set, and dividing it by the number of values in the set."
					+ "\n\nIf an empty array is provided, a IndexOverflowException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("With single number", "average(1)"),
				new ExampleScript("Two arguments", "average(5, 10)"),
				new ExampleScript("As an array", "average(array(1, 2, 3, 4))")
			};
		}
	}

	@api
	public static class sum extends StatisticsFunction {

		@Override
		public CNumber exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			List<Double> values = new ArrayList<>();
			if(args.length == 1 && args[0].isInstanceOf(CArray.TYPE)) {
				CArray c = ArgumentValidation.getArray(args[0], t);
				for(Mixed m : c.asList()) {
					values.add(ArgumentValidation.getDouble(m, t));
				}
			} else {
				for(Mixed m : args) {
					values.add(ArgumentValidation.getDouble(m, t));
				}
			}
			double value = 0;
			for(Double d : values) {
				value += d;
			}
			return new CDouble(value, t);
		}

		@Override
		public String getName() {
			return "sum";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "number {array<number> | number input...} Returns the sum across all"
					+ " the numbers in the set. The input may be an array of numbers, or individual numbers as"
					+ " arguments. The sum is the result of adding all the numbers in the set together."
					+ "\n\nIf an empty array is provided, a IndexOverflowException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("With single number", "sum(1)"),
				new ExampleScript("Two arguments", "sum(5, 10)"),
				new ExampleScript("As an array", "sum(array(1, 2, 3, 4))")
			};
		}
	}

	@api
	public static class median extends StatisticsFunction {

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			List<Double> values = new ArrayList<>();
			if(args.length == 1 && args[0].isInstanceOf(CArray.TYPE)) {
				CArray c = ArgumentValidation.getArray(args[0], t);
				for(Mixed m : c.asList()) {
					values.add(ArgumentValidation.getDouble(m, t));
				}
			} else {
				for(Mixed m : args) {
					values.add(ArgumentValidation.getDouble(m, t));
				}
			}
			Collections.sort(values);
			double median;
			if(values.size() % 2 == 0) {
				// Even number, harder logic
				median = (values.get(values.size() / 2) + values.get(values.size() / 2 - 1)) / 2;
			} else {
				median = values.get(values.size() / 2);
			}
			return new CDouble(median, t);
		}

		@Override
		public String getName() {
			return "median";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "number {array<number> | number input...} Returns the median across all"
					+ " the numbers in the set. The input may be an array of numbers, or individual numbers as"
					+ " arguments. The median is the number that is in the center of set, once the values in the"
					+ " set are ordered from least to greatest. That is, in the set [1, 2, 3], 2 is the median. If"
					+ " there is an even number of value in the set, the middle two values are averaged, and that"
					+ " value is returned."
					+ "\n\nIf an empty array is provided, a IndexOverflowException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("With single number", "median(1)"),
				new ExampleScript("Three arguments", "median(1, 2, 3)"),
				new ExampleScript("As an array", "median(array(1, 2, 3))"),
				new ExampleScript("With an even number of values", "median(1, 2, 3, 4)")
			};
		}
	}

	@api
	public static class mode extends StatisticsFunction {

		@Override
		public CArray exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			List<Double> values = new ArrayList<>();
			if(args.length == 1 && args[0].isInstanceOf(CArray.TYPE)) {
				CArray c = ArgumentValidation.getArray(args[0], t);
				for(Mixed m : c.asList()) {
					values.add(ArgumentValidation.getDouble(m, t));
				}
			} else {
				for(Mixed m : args) {
					values.add(ArgumentValidation.getDouble(m, t));
				}
			}
			List<Double> returns = mode(values);
			CArray ret = new CArray(t, returns.size());
			for(Double d : returns) {
				ret.push(new CDouble(d, t), t);
			}
			return ret;
		}

		public static List<Double> mode(List<Double> array) {
			Map<Double, Integer> counts = new HashMap();
			int max = 1;

			for(int i = 0; i < array.size(); i++) {
				if(counts.get(array.get(i)) != null) {

					int count = counts.get(array.get(i));
					count++;
					counts.put(array.get(i), count);

					if(count > max) {
						max = count;
					}
				} else {
					counts.put(array.get(i), 1);
				}
			}

			List<Double> ret = new ArrayList<>();
			for(Map.Entry<Double, Integer> e : counts.entrySet()) {
				if(e.getValue() == max) {
					ret.add(e.getKey());
				}
			}
			return ret;
		}

		@Override
		public String getName() {
			return "mode";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "array<number> {array<number> | number input...} Returns the mode across all"
					+ " the numbers in the set. The input may be an array of numbers, or individual numbers as"
					+ " arguments. The mode of a set of numbers is the values that occur most in the set. This function"
					+ " supports bimodal (and generally n-modal sets), as well as fully unique sets, by returning an"
					+ " array. If the set is fully unique, i.e. [1, 2, 3], then the original set will be returned all"
					+ " values occur once). If there are more than one modes, i.e. [1, 1, 2, 3, 3], then an array of"
					+ " both 1 and 3 will be returned. The values will not necessarily be returned in any particular"
					+ " order."
					+ "\n\nIf an empty array is provided, a IndexOverflowException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("With single number", "mode(1)"),
				new ExampleScript("Multiple arguments", "mode(1, 1, 2, 3, 4)"),
				new ExampleScript("As an array", "mode(array(1, 1, 2, 3, 4))"),
				new ExampleScript("Bimodal set", "mode(1, 1, 2, 3, 3)"),
				new ExampleScript("n-modal set (n=3)", "mode(1, 1, 2, 3, 3, 4, 5, 6, 6)"),
				new ExampleScript("unsorted n-modal set (n=3)", "mode(6, 4, 2, 6, 5, 2, 1, 3, 4)"),
				new ExampleScript("unique set", "mode(1, 2, 3, 4, 5)"),
			};
		}
	}

	@api
	public static class percentile extends StatisticsFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREIndexOverflowException.class, CRERangeException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			double percentile = ArgumentValidation.getDouble(args[0], t);
			List<Double> values = new ArrayList<>();
			if(args.length == 2 && args[1].isInstanceOf(CArray.TYPE)) {
				CArray c = ArgumentValidation.getArray(args[1], t);
				for(Mixed m : c.asList()) {
					values.add(ArgumentValidation.getNumber(m, t));
				}
			} else {
				boolean first = true;
				for(Mixed m : args) {
					if(!first) {
						values.add(ArgumentValidation.getNumber(m, t));
					}
					first = false;
				}
			}

			return new CDouble(percentile(values, percentile), t);
		}

		public static double percentile(List<Double> values, double percentile) {
			Collections.sort(values);
			int index = (int) java.lang.Math.ceil(percentile * values.size());
			return values.get(index - 1);
		}

		@Override
		public String getName() {
			return "percentile";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "number {number percentile, array<number> | number percentile, number input...}"
					+ " Returns the nth-percentile across all"
					+ " the numbers in the set. The input may be an array of numbers, or individual numbers as"
					+ " arguments. A percentile is a measure indicating the value below which a given percentage of"
					+ " observations in a group of observations falls. For example, the 20th percentile is the value"
					+ " (or score) below which 20% of the observations may be found."
					+ "\n\nIf an empty array is provided, a IndexOverflowException is thrown. If the percentile is"
					+ " not within the range of 0 or 1, a RangeException is thrown.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("With single number (50th percentile)", "percentile(0.5, 1)"),
				new ExampleScript("40th percentile of 1-10", "percentile(0.4, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)"),
				new ExampleScript("As an array", "percentile(0.5, array(1, 2, 3, 4))")
			};
		}
	}
}
