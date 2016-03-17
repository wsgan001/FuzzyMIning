package org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary;

import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MatrixUtils;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.Metric;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
/**
 * 测试值存于relations之中，normalized用于存标准化
 * @author Administrator
 *TODO
 */
public class BinaryMetric extends Metric {

	protected int size;
	protected DoubleMatrix2D relations; // relations matrix (sums of measurements)
	protected DoubleMatrix2D normalized; // 1.0-normalized shadow matrix

	/**
	 * @param aName
	 * @param aDescription
	 * @param aSize
	 */
	public BinaryMetric(String aName, String aDescription, int aSize) {
		super(aName, aDescription);
		size = aSize;
		// use sparse matrices on high event count (exponential matrix size,
		// #cells * 8 bytes)
		if (size < 512) {
			//稠密矩阵
			relations = DoubleFactory2D.dense.make(size, size, 0.0);
		} else {
			//稀疏矩阵
			relations = DoubleFactory2D.sparse.make(size, size, 0.0);
		}
		// initialize normalized shadow matrix as invalid
		normalized = null;
	}

	/**
	 * @param aName
	 * @param aDescription
	 */
	public BinaryMetric(String aName, String aDescription) {
		super(aName, aDescription);
		// initialize normalized shadow matrix as invalid
		normalized = null;
	}

	public void setSize(int aSize) {
		size = aSize;
		// use sparse matrices on high event count (exponential matrix size,
		// #cells * 8 bytes)
		if (size < 512) {
			relations = DoubleFactory2D.dense.make(size, size, 0.0);
		} else {
			relations = DoubleFactory2D.sparse.make(size, size, 0.0);
		}
	}

	public BinaryMetric(String aName, String aDescription, BinaryMetric template) {
		this(aName, aDescription, template.size());
		setNormalizationMaximum(template.getNormalizationMaximum());
		for (int x = 0; x < template.size(); x++) {
			for (int y = 0; y < template.size(); y++) {
				setMeasure(x, y, template.getMeasure(x, y));
			}
		}
	}

	public double getMeasure(int fromIndex, int toIndex) {
		// lazy normalization on the fly
		if (normalized == null) {
			normalized = MatrixUtils.normalize(relations.copy(),
					normalizationMaximum);
		}
		// retrieve result
		double result = normalized.get(fromIndex, toIndex);
		if (invert == true) {
			result = normalizationMaximum - result;
		}
		return result;
	}

	public void setMeasure(int fromIndex, int toIndex, double value) {
		relations.set(fromIndex, toIndex, value);
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

	public int size() {
		return size;
	}

	public boolean isValid() {
		return ((normalizationMaximum > 0.0) && (MatrixUtils
				.getMaxValue(relations) > 0.0));
	}

	public DoubleMatrix2D getNormalizedMatrix() {
		if (normalized == null) {
			normalized = MatrixUtils.normalize(relations.copy(),
					normalizationMaximum);
		}
		return normalized;
	}

	public Object clone() {
		super.clone();
		BinaryMetric clone = (BinaryMetric) super.clone();
		clone.relations = relations.copy();
		clone.normalized = null;
		return clone;
	}

}
