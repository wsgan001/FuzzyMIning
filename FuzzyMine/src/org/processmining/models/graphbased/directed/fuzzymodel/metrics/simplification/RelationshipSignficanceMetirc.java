/**
 * 
 */
package org.processmining.models.graphbased.directed.fuzzymodel.metrics.simplification;

import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryMetric;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Administrator TODO
 */
public class RelationshipSignficanceMetirc extends BinaryMetric {

	

	private BinaryMetric edgeSignificance;
	private BinaryMetric edgeCorrelation;

	// private int size;
	public BinaryMetric getEdgeSignificance() {
		return edgeSignificance;
	}

	public void setEdgeSignificance(BinaryMetric edgeSignificance) {
		this.edgeSignificance = edgeSignificance;
	}

	/**
	 * @param aName
	 * @param aDescription
	 * @param aSize
	 */
	public RelationshipSignficanceMetirc(String aName, String aDescription,
			int aSize) {
		super(aName, aDescription, aSize);
		// TODO Auto-generated constructor stub
	}

	public RelationshipSignficanceMetirc(String aName, String aDescription,
			BinaryMetric edgeSignificance, BinaryMetric edgeCorrelation,int size) {
		this(aName, aDescription, size);
		// this.size = size;
		this.edgeSignificance = edgeSignificance;
		this.edgeCorrelation = edgeCorrelation;

	}

	public RelationshipSignficanceMetirc getRelationshipSignficanceMetirc() {
		return this;

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
				setMeasure(x, y, relAB);
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
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < x; y++) {
				if (getMeasure(x, y) != 0 && getMeasure(y, x) != 0) {
					if (getMeasure(x, y) >= preserveThreshold
							&& getMeasure(y, x) >= preserveThreshold) {//大于保护阈值
						continue;
					} else {
						double offset = getMeasure(x, y) - getMeasure(y, x);
						if (offset < 0) {
							offset = -offset;
						}
						if (offset > radioThreshold) {//大于比例阈值
							if (getMeasure(x, y) > getMeasure(y, x)) {
								setMeasure(y, x, 0);
							} else {
								setMeasure(x, y, 0);
							}
						} else {
							setMeasure(x, y, 0);
							setMeasure(y, x, 0);
						}
					}
				}
			}
		}
	}
	public void filterEdge(double utilityRadio,double cutoff){
		DoubleMatrix2D temp;
		double minValue = Double.MAX_VALUE;
		if (size < 512) {
			//稠密矩阵
			temp = DoubleFactory2D.dense.make(size, size, 0.0);
		} else {
			//稀疏矩阵
			temp = DoubleFactory2D.sparse.make(size, size, 0.0);
		}
		for(int x = 0; x < size; x++){
			for(int y = 0; y < size; y++){
				if(x==y){
					continue;
				}
				double utilAB = utilityRadio * edgeSignificance.getMeasure(x, y)
						+ (1 - utilityRadio) * edgeCorrelation.getMeasure(x, y);
				if(utilAB != 0){
					temp.set(x, y, utilAB);
					if(utilAB < minValue){
						minValue = utilAB;
					}
				}
			}
		}
		for(int x = 0; x < size; x++){
			for(int y = 0; y < size; y++){
				if(temp.get(x, y)==0){
					continue;
				}else{
					double factor = (temp.get(x, y)-minValue)/(1.0-minValue);
					//System.out.println(factor+"++++++++++");
					if(factor < cutoff){
						setMeasure(x, y, 0);
					}
				}
			}
		}
	}
}
