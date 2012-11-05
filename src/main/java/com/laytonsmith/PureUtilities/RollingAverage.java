package com.laytonsmith.PureUtilities;

import java.util.Arrays;

/**
 * A RollingAverage class allows you to keep track of X number of past results, and
 * keep a rolling average across them. If at any point a number is not set, it is not
 * used in the average, so initial results may be statistically skewed in favor of the
 * earlier results.
 * @author lsmith
 */
public class RollingAverage {
	private Double[] data;
	private int insertionIndex;
	private int dataSize;
	
	/**
	 * Creates a new rolling average object.
	 * @param datasetSize The size of the queue
	 * @param initialValue The initial value to add to the
	 * dataset. If average() is called before addData, this will
	 * allow an actual number to be returned.
	 */
	public RollingAverage(int datasetSize, double initialValue){
		data = new Double[datasetSize];
		Arrays.fill(data, null);
		dataSize = datasetSize;
		insertionIndex = 0;
	}
	
	/**
	 * Adds
	 * @param d 
	 */
	public void addData(double d){
		data[insertionIndex] = d;
		insertionIndex++;
		if(insertionIndex == dataSize){
			insertionIndex = 0; //rollover
		}
	}
	
	public double getAverage(){
		double sum = 0;
		int count = 0;
		for(Double d : data){
			if(d != null){
				sum += d;
				count++;
			}
		}
		return sum / count;
	}
}
