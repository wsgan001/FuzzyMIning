package org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary;

import java.util.Arrays;

import org.processmining.models.graphbased.directed.fuzzymodel.metrics.Metric;
/**
 * 测试值存于values之中，normalized用于存标准化
 * @author Administrator
 *TODO
 */
public class UnaryMetric extends Metric {

	protected int size;
	protected double[] values;
	protected double[] normalized;

	/**
	 * @param aName
	 * @param aDescription
	 * @param aSize
	 */
	public UnaryMetric(String aName, String aDescription, int aSize) {
	//	super(aName, aDescription);
		super(aName, aDescription);
		size = aSize;
		values = initializeVector(size, 0.0);//初始为0
		normalized = null;
	}

	/**
	 * @param aName
	 * @param aDescription
	 */
	public UnaryMetric(String aName, String aDescription) {
		super(aName, aDescription);
		 values = initializeVector(size, 0.0);
		normalized = null;
	}

	public void setSize(int aSize) {
		size = aSize;
		values = initializeVector(size, 0.0);
	}

	public UnaryMetric(String aName, String aDescription, UnaryMetric template) {
		this(aName, aDescription, template.size());
		setNormalizationMaximum(template.getNormalizationMaximum());
		for (int i = 0; i < template.size(); i++) {
			setMeasure(i, template.getMeasure(i));
		}
	}

	public int size() {
		return size;
	}

	public double getMeasure(int index) {
		if (normalized == null) {
			normalized = normalizeVector(values, normalizationMaximum);
		}
		double result = normalized[index];
		if (invert == true) {
			result = normalizationMaximum - result;
		}
		return result;
	}

	public void setMeasure(int index, double value) {
		values[index] = value;
		normalized = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.Metric#setNormalizationMaximum
	 * (double)
	 */
	public void setNormalizationMaximum(double aNormalizationMaximum) {
		normalizationMaximum = aNormalizationMaximum;
		normalized = null; // reset normalization matrix
	}

	public boolean isValid() {
		for (int i = 0; i < size; i++) {
			if (getMeasure(i) > 0.0) {
				return true;
			}
		}
		return false;
	}

	protected double[] initializeVector(int size, double initialValue) {
		double[] result = new double[size];
		Arrays.fill(result, initialValue);
		return result;
	}

	/**
	 * 找到最大值与所需要标准化的最大值算出比例，剩下的值按比例放缩
	 * @param vector 需要标准化的矢量组
	 * @param maxValue 标准化的最大值
	 * @return
	 */
	protected double[] normalizeVector(double[] vector, double maxValue) {
		if (maxValue == 0.0) {
			return initializeVector(vector.length, 0.0); // disabled metric
		}
		double[] normalized = new double[vector.length];
		double foundMax = Double.MIN_VALUE;
		for (double current : vector) {
			foundMax = Math.max(current, foundMax);
		}
		double normFactor = maxValue / foundMax;
		for (int i = 0; i < vector.length; i++) {
			normalized[i] = normFactor * vector[i];
		}
		return normalized;
	}

	public double[] getNormalizedValues() {
		if (normalized == null) {
			normalized = normalizeVector(values, normalizationMaximum);
		}
		return normalized;
	}

	public Object clone() {
		super.clone();
		UnaryMetric clone = (UnaryMetric) super.clone();
		clone.values = values.clone();
		clone.normalized = null;
		return clone;
	}
}
