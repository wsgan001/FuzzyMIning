/**
 * 
 */
package org.processmining.models.graphbased.directed.fuzzymodel.metrics.simplification;

import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryMetric;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Administrator
 *TODO
 */
public class Pretreatment {
	private UnaryMetric nodeSignificance;
	private BinaryMetric edgeSignificance;
	private BinaryMetric edgeCorrelation;
	private BinaryMetric afterPretreatmentEdgeSignificance;
	private BinaryMetric afterPretreatmentEdgeCorrelation;
	private DoubleMatrix2D rel;
	private DoubleMatrix2D util;
	private int size;
	//private MutableFuzzyGraph mfg;
	public Pretreatment(BinaryMetric edgeSignificance, BinaryMetric edgeCorrelation, UnaryMetric nodeSignificance,int size) {
		super();
		this.edgeSignificance = edgeSignificance;
		this.edgeCorrelation = edgeCorrelation;
		this.nodeSignificance = nodeSignificance;
		this.size = size;
		afterPretreatmentEdgeSignificance = new BinaryMetric("", "", edgeSignificance);
		afterPretreatmentEdgeCorrelation = new BinaryMetric("", "", edgeCorrelation);
		if (size < 512) {
			//稠密矩阵
			rel = DoubleFactory2D.dense.make(size, size, 0.0);
			util = DoubleFactory2D.dense.make(size, size, 0.0);
		} else {
			//稀疏矩阵
			rel = DoubleFactory2D.sparse.make(size, size, 0.0);
			util = DoubleFactory2D.sparse.make(size, size, 0.0);
		}
	}
	
	

	public BinaryMetric getAfterPretreatmentEdgeSignificance() {
		return afterPretreatmentEdgeSignificance;
	}

	public BinaryMetric getAfterPretreatmentEdgeCorrelation() {
		return afterPretreatmentEdgeCorrelation;
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
				temp = edgeSignificance.getMeasure(indexA, i);
				totalSigA += temp;
				// System.out.println("Signficance:A,i "+
				// temp+" indexA:"+indexA+" indexB:"+indexB);
			}
			if (i != indexB) {
				temp = edgeSignificance.getMeasure(i, indexB);
				totalSigB += temp;
				// System.out.println("Signficance:i,B "+
				// temp+" indexA:"+indexA+" indexB:"+indexB);
			}
		}
		double sigAB = edgeSignificance.getMeasure(indexA, indexB);
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
								afterPretreatmentEdgeCorrelation.setMeasure(y, x, 0);
								afterPretreatmentEdgeSignificance.setMeasure(y, x, 0);
								
							} else {
								afterPretreatmentEdgeCorrelation.setMeasure(x, y, 0);
								afterPretreatmentEdgeSignificance.setMeasure(x, y, 0);
							}
						} else {
							afterPretreatmentEdgeCorrelation.setMeasure(y, x, 0);
							afterPretreatmentEdgeSignificance.setMeasure(y, x, 0);
							afterPretreatmentEdgeCorrelation.setMeasure(x, y, 0);
							afterPretreatmentEdgeSignificance.setMeasure(x, y, 0);
						}
					}
				}
			}
		}
	}
	public void filterEdge(double utilityRadio,double cutoff){
		//DoubleMatrix2D temp;
		double minValue = Double.MAX_VALUE;
		
		for(int x = 0; x < size; x++){
			for(int y = 0; y < size; y++){
				if(x==y){
					continue;
				}
				double utilAB = utilityRadio * edgeSignificance.getMeasure(x, y)
						+ (1 - utilityRadio) * edgeCorrelation.getMeasure(x, y);
				if(utilAB != 0){
					util.set(x, y, utilAB);
					if(utilAB < minValue){
						minValue = utilAB;
					}
				}
			}
		}
		for(int x = 0; x < size; x++){
			for(int y = 0; y < size; y++){
				if(util.get(x, y)==0){
					continue;
				}else{
					double factor = (util.get(x, y)-minValue)/(1.0-minValue);
					//System.out.println(factor+"++++++++++");
					if(factor < cutoff){
						afterPretreatmentEdgeCorrelation.setMeasure(x, y, 0);
						afterPretreatmentEdgeSignificance.setMeasure(x, y, 0);
					}
				}
			}
		}
	}
}
