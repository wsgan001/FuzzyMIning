/**
 * 
 */
package org.processmining.models.graphbased.directed.fuzzymodel.metrics.simplification;

import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.impl.FMEdgeImpl;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Administrator
 *TODO
 */
public class Pretreat {
	private MutableFuzzyGraph mfg;
	private DoubleMatrix2D rel;
	private DoubleMatrix2D util;
	private int size;
	public Pretreat(MutableFuzzyGraph mfg) {
		super();
		this.mfg = mfg;
		this.size = mfg.getNumberOfInitialNodes();
		if (size < 512) {
			//稠密矩阵
			rel = DoubleFactory2D.dense.make(size, size, 0.0);
			util = DoubleFactory2D.dense.make(size, size, 0.0);
		} else {
			//稀疏矩阵
			rel = DoubleFactory2D.sparse.make(size, size, 0.0);
			util = DoubleFactory2D.sparse.make(size, size, 0.0);
		}
		
		mfg.initializeGraph();
		mfg.setEdgeImpls();
	}
	public void caculate() {
		// int size = this.edgeSignificance.size();
		System.out.println(this.size + "innner");
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if (x == y) {
					continue;
				}
				double relAB = getRelAB(x, y);
				// System.out.println(this.relations==null);
				rel.set(x, y, relAB);
				// System.out.println("x:"+x+" y:"+y+" relAB:"+relAB);
			}
		}
	}

	public double getRelAB(int indexA, int indexB) {
		double totalSigA = 0, totalSigB = 0;
		double temp;
		double relAB;
		// int size = this.edgeSignificance.size();
		for (int i = 0; i < size; i++) {
			if (i != indexA) {
				//temp = edgeSignificance.getMeasure(indexA, i);
				temp = mfg.getBinarySignificance(indexA, i);
				totalSigA += temp;
				// System.out.println("Signficance:A,i "+
				// temp+" indexA:"+indexA+" indexB:"+indexB);
			}
			if (i != indexB) {
				//temp = edgeSignificance.getMeasure(i, indexB);
				temp = mfg.getBinarySignificance(i, indexB);
				totalSigB += temp;
				// System.out.println("Signficance:i,B "+
				// temp+" indexA:"+indexA+" indexB:"+indexB);
			}
		}
		double sigAB = mfg.getBinarySignificance(indexA, indexB);
		if (totalSigA == 0) {
			if (totalSigB == 0) {
				return 0;
			} else {
				relAB = 0.5 * (sigAB / totalSigB);
			}
		} else if (totalSigB == 0) {
			relAB = 0.5 * (sigAB / totalSigA);
		} else {
			relAB = 0.5 * (sigAB / totalSigA) + 0.5 * (sigAB / totalSigB);
		}

		return relAB;
	}
	public void cut(double preserveThreshold, double radioThreshold) {
		caculate();
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < x; y++) {
				if (rel.get(x, y) != 0 && rel.get(y, x) != 0) {
					if (rel.get(x, y) >= preserveThreshold
							&& rel.get(y, x) >= preserveThreshold) {//大于保护阈值
						continue;
					} else {
						double offset = rel.get(x, y) - rel.get(y, x);
						if (offset < 0) {
							offset = -offset;
						}
						if (offset > radioThreshold) {//大于比例阈值
							if (rel.get(x, y) > rel.get(y, x)) {
								//FMEdgeImpl edge = ;
								//mfg.removeEdgePermanently(edge );
								//afterPretreatmentEdgeCorrelation.setMeasure(y, x, 0);
								//afterPretreatmentEdgeSignificance.setMeasure(y, x, 0);
								
							} else {
								//afterPretreatmentEdgeCorrelation.setMeasure(x, y, 0);
								//afterPretreatmentEdgeSignificance.setMeasure(x, y, 0);
							}
						} else {
							//afterPretreatmentEdgeCorrelation.setMeasure(y, x, 0);
							//afterPretreatmentEdgeSignificance.setMeasure(y, x, 0);
							//afterPretreatmentEdgeCorrelation.setMeasure(x, y, 0);
							//afterPretreatmentEdgeSignificance.setMeasure(x, y, 0);
						}
					}
				}
			}
		}
	}
}
