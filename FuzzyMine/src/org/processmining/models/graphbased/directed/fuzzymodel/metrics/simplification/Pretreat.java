/**
 * 
 */
package org.processmining.models.graphbased.directed.fuzzymodel.metrics.simplification;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMEdge;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
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
	private Set<FMEdgeImpl> fmEdges;
	//private Set<FMNode> nodes;
	private List<FMClusterNode> clusterNodes;
	private int size;
	
	protected FMNode[] nodeAliasMap;
	protected double[][] actBinarySignificance;
	protected double[][] actBinaryCorrelation;
	
	public Pretreat(MutableFuzzyGraph mfg) {
		super();
		this.mfg = mfg;
		this.size = mfg.getNumberOfInitialNodes();
		mfg.initializeGraph();
		mfg.setEdgeImpls();
		this.fmEdges = mfg.getFMEdges();
		//this.nodes = mfg.getNodes();
		this.clusterNodes = mfg.getClusterNodes();
		this.nodeAliasMap = mfg.getNodeAliasMap();
		this.actBinaryCorrelation = mfg.getActBinarySignificance();
		this.actBinarySignificance = mfg.getActBinarySignificance();
	}
	
	public void conflictResolve(double preserveThreshold, double radioThreshold){
		for(int x = 0; x < size; x++){
			for(int y = 0; y < x; y++){
				if(x == y ){
					continue;
				}
				
				if(mfg.getBinarySignificance(x, y) != 0 
						&& mfg.getBinarySignificance(y, x) != 0){
					double relAB = 0,relBA = 0;
					//caculate relAB
					relAB = caculateRel(x, y);
					//caculate relBA
					relBA = caculateRel(y, x);
//					System.out.println(mfg.getPrimitiveNode(x).getElementName()+" -> "
//					+mfg.getPrimitiveNode(y).getElementName()+" rel:"+ relAB);
//					System.out.println(mfg.getPrimitiveNode(y).getElementName()+" -> "
//							+mfg.getPrimitiveNode(x).getElementName()+" rel:"+ relBA);
					if (relAB >= preserveThreshold && relBA >= preserveThreshold) {//大于保护阈值
						continue;
					} else {
						double offset = relAB - relBA;;
						if (offset < 0) {
							offset = -offset;
						}
						if (offset > radioThreshold) {//大于比例阈值
							if (relAB > relBA) {
								mfg.removeEdge(mfg.getPrimitiveNode(y), mfg.getPrimitiveNode(x));
							} else {
								mfg.removeEdge(mfg.getPrimitiveNode(x), mfg.getPrimitiveNode(y));
							}
						} else {
							mfg.removeEdge(mfg.getPrimitiveNode(x), mfg.getPrimitiveNode(y));
							mfg.removeEdge(mfg.getPrimitiveNode(y), mfg.getPrimitiveNode(x));
						}
					}
				}
			}
		}
	}
	
	private double caculateRel(int fromIndex,int  toIndex){
		FMNode source = mfg.getPrimitiveNode(fromIndex);
		FMNode target = mfg.getPrimitiveNode(toIndex);
		double relAB = 0;
		double sigAB = mfg.getBinarySignificance(fromIndex, toIndex);
		double totalSigA = 0, totalSigB = 0;
		Collection<FMEdge<? extends FMNode, ? extends FMNode>> outEdges = mfg.getOutEdges(source);
		for (FMEdge<? extends FMNode, ? extends FMNode> fmEdge : outEdges) {
			totalSigA = totalSigA + fmEdge.getSignificance();
		}
		Collection<FMEdge<? extends FMNode, ? extends FMNode>> inEdges = mfg.getInEdges(target);
		for (FMEdge<? extends FMNode, ? extends FMNode> fmEdge : inEdges) {
			totalSigB = totalSigB + fmEdge.getSignificance();
		}
		if(totalSigA != 0){
			if(totalSigB != 0){
				relAB = 0.5 * (sigAB / totalSigA) + 0.5 * (sigAB / totalSigB);
			}else{
				relAB = 0.5 * (sigAB / totalSigA);
			}
		}else{
			if(totalSigB != 0){
				relAB = 0.5 * (sigAB / totalSigB);
			}else{
				relAB = 0;
			}
		}
		return relAB;
	}
	
	public void edgeFilter(double utilityRadio,double cutoff){
		Set<FMEdgeImpl> edgeImpls = mfg.getEdgeImpls();
		int size = edgeImpls.size();
		double[] utils = new double[size];
		FMEdgeImpl[] edges = new FMEdgeImpl[size];
		double utilAB = 0;
		int i = 0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (FMEdgeImpl fmEdgeImpl : edgeImpls) {
			utilAB =  utilityRadio * fmEdgeImpl.getSignificance() 
					+ (1 - utilityRadio) * fmEdgeImpl.getCorrelation();
			//System.out.println("utilAB:" + utilAB );
			if(utilAB > 0.001 && utilAB < min){
				min = utilAB;
			}
			if(utilAB > max){
				max = utilAB;
			}
			utils[i] = utilAB;
			edges[i] = fmEdgeImpl;
			i++;
		}
		if(min == max){
			return;
		}
		//System.out.println(max +" "+ min+"++++");
		for(i = 0; i < size; i++){
			double normalUtil = (utils[i] - min) / (max - min);
			
			//System.out.println(normalUtil+" source: "+edges[i].getSource().getElementName());
			if(normalUtil < cutoff){
				mfg.removeEdgePermanently(edges[i]);
			}
		}
		
	}
	
	public void nodeAggregatinoAndAbstraction(double nodeCutoff){
		Set<FMNode> nodes = mfg.getNodes();
		//System.out.println(nodes.size());
		//Aggregate And Abstract FMNode
		for (FMNode fmNode : nodes) {
			if(fmNode.getSignificance() >= nodeCutoff){
				continue;
			}
			double max = Double.MIN_VALUE;
			FMEdgeImpl targetEdge = null;
			Collection<FMEdge<? extends FMNode,? extends FMNode>> inEdges = mfg.getInEdges(fmNode);
			for (FMEdge<? extends FMNode, ? extends FMNode> fmEdge : inEdges) {
				double correlation = fmEdge.getCorrelation();
				if(correlation > max){
					max = correlation;
					targetEdge = (FMEdgeImpl) fmEdge;
				}
				
			}
			Collection<FMEdge<? extends FMNode,? extends FMNode>> outEdges = mfg.getOutEdges(fmNode);
//			System.out.println(fmNode.getElementName() 
//					+ " number of inEdges:" + inEdges.size()
//					+ " number of outEdges:" + outEdges.size());
			for (FMEdge<? extends FMNode, ? extends FMNode> fmEdge : outEdges) {
				double correlation = fmEdge.getCorrelation();
				if(correlation > max){
					max = correlation;
					targetEdge = (FMEdgeImpl) fmEdge;
				}
			}
			if(targetEdge != null){
				FMNode source = targetEdge.getSource();
				FMNode target = targetEdge.getTarget();
				if(source.equals(fmNode)){
					if(target instanceof FMClusterNode){
						int index = fmNode.getIndex();
						((FMClusterNode) target).add(fmNode);
						nodeAliasMap[index] = target;
					}else{
						
					}
				}else{
					if(source instanceof FMClusterNode){
						
					}else{
						
					}
				}
			}
		
		
		}
		////Aggregate And Abstract FMClusterNode
	}
}
