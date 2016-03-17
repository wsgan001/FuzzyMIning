/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XParserRegistry;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMEdge;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.LinearAttenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.impl.FMEdgeImpl;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.AggregateBinaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryLogMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.simplification.Pretreatment;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.simplification.RelationshipSignficanceMetirc;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FuzzyMinerLog;

/**
 * @author Administrator TODO
 */
public class Test2 {

	@Test
	public void test() throws Exception {
		File file = new File("doc/L1.xes");
		System.out.println(file == null);
		List<XLog> logs = null;
		for (XParser parser : XParserRegistry.instance().getAvailable()) {
			if (parser.canParse(file)) {
				logs = parser.parse(file);
			}
		}

		System.out.println(logs.size());
		for (XLog log : logs) {
			MetricsRepository mr = MetricsRepository
					.createRepository(XLogInfoFactory.createLogInfo(log));
			Attenuation att = new LinearAttenuation(1, 0);
			mr.apply(log, att, 1);
			MutableFuzzyGraph mfg = new MutableFuzzyGraph(mr);
			BinaryMetric edgeSignificance = mr.getAggregateSignificanceBinaryMetric();
			BinaryMetric edgeCorrelation = mr.getAggregateCorrelationBinaryMetric();
			UnaryMetric nodeSignificanceMetric = mr.getAggregateUnaryMetric();
			int size = XLogInfoFactory.createLogInfo(log).getEventClasses().size();
			
			System.out.println("----------------------------");
			Pretreatment pretreatment = new Pretreatment(edgeSignificance, edgeCorrelation, nodeSignificanceMetric,size);
			pretreatment.cut(0.5, 0.2);
			pretreatment.filterEdge(0.2, 0.3);
			BinaryMetric afterPretreatmentEdgeCorrelation = pretreatment.getAfterPretreatmentEdgeCorrelation();
			BinaryMetric afterPretreatmentEdgeSignificance = pretreatment.getAfterPretreatmentEdgeSignificance();
			for(int x= 0; x < size; x++){
				 for(int y = 0; y < size; y++){
					 System.out.print(afterPretreatmentEdgeCorrelation.getMeasure(x, y) + " ");;
				 }
				 System.out.println();
			}
			System.out.println("----------------------------");
			for(int x= 0; x < size; x++){
				for(int y = 0; y < size; y++){
					System.out.print(afterPretreatmentEdgeSignificance.getMeasure(x, y) + " ");;
				}
				System.out.println();
			}
			System.out.println("----------------------------");
			
			Set<FMNode> set = mfg.getNodes();
			for (FMNode fmNode : set) {
				System.out.println("index: "+fmNode.getIndex() +" name:"+fmNode.getElementName());
			}
			
			mfg.setEdgeCorrelation(afterPretreatmentEdgeCorrelation);
			mfg.setEdgeSignificance(afterPretreatmentEdgeSignificance);
			mfg.initializeGraph();
			mfg.setEdgeImpls();
			
			
			
			Set<FMEdgeImpl> edgeImpls = mfg.getEdgeImpls();
			for (FMEdgeImpl fmEdgeImpl : edgeImpls) {
				System.out.println("Source: " +fmEdgeImpl.getSource().getElementName()
						+" target: "+fmEdgeImpl.getTarget().getElementName() 
						+ " Significance: "+fmEdgeImpl.getSignificance()
						+ " Correlation: "+fmEdgeImpl.getCorrelation());
			}
			double nodeCutoff =1;
			Set<FMNode> nodes = mfg.getNodes();
			for (FMNode fmNode : nodes) {
				if(fmNode.getSignificance() < nodeCutoff){
					double maxCorrelation = Double.MIN_VALUE;
					FMEdgeImpl fmEdgeImpl = null;
					for (FMEdgeImpl fmEdge : edgeImpls) {
						FMNode source = fmEdge.getSource();
						FMNode target = fmEdge.getTarget();
						if(fmNode.equals(source) || fmNode.equals(target)){
							double correlation = fmEdge.getCorrelation();
							if(maxCorrelation < correlation){
								maxCorrelation = correlation;
								fmEdgeImpl = fmEdge;
							}
						}
					}
					if(fmEdgeImpl!=null){
						FMNode source = fmEdgeImpl.getSource();
						FMNode target = fmEdgeImpl.getTarget();
						if(source.equals(fmNode)){
							if(target instanceof FMClusterNode){
								mfg.removeClusterNode((FMClusterNode) target);
								((FMClusterNode) target).add(fmNode);
								mfg.removeNode(fmNode);
								mfg.addClusterNode((FMClusterNode) target);
								System.out.println("1: "+target.getElementName()+" is cluster added"+fmNode.getElementName());
							}else{
								FMClusterNode clusterNode = new FMClusterNode(mfg, mfg.getClusterNodes().size(), "Cluster");
								clusterNode.setIndex(fmNode.getIndex());
								clusterNode.setElementName(fmNode.getElementName());
								mfg.addClusterNode(clusterNode);
								mfg.removeNode(fmNode);
								System.out.println("1: "+fmNode.getElementName()+" is not cluster, become a cluster");
							}
							
							
						}else{//target is fmNode
							if(source instanceof FMClusterNode){
								mfg.removeClusterNode((FMClusterNode) source);
								((FMClusterNode) source).add(fmNode);
								mfg.removeNode(fmNode);
								mfg.addClusterNode((FMClusterNode) source);
								System.out.println("2: "+source.getElementName()+" is cluster added"+fmNode.getElementName());
							}else{
								FMClusterNode clusterNode = new FMClusterNode(mfg, mfg.getClusterNodes().size(), "Cluster");
								clusterNode.setIndex(fmNode.getIndex());
								clusterNode.setElementName(fmNode.getElementName());
								clusterNode.add(fmNode);
								mfg.removeNode(fmNode);
								System.out.println("2: "+fmNode.getElementName()+" is not cluster ,become a cluster");
							}
						}
					}
					
				}
			}
			System.out.println("fisrt step");
			set = mfg.getNodes();
			for (FMNode fmNode : set) {
				System.out.println("index: "+fmNode.getIndex() +" name:"+fmNode.getElementName());
				
			}
			List<FMClusterNode> nodes2 = mfg.getClusterNodes();
			System.out.println(nodes2.size());
			for (FMClusterNode fmClusterNode : nodes2) {
				Set<FMNode> primitives = fmClusterNode.getPrimitives();
				System.out.println("index: "+fmClusterNode.getIndex() +" name:"+fmClusterNode.getElementName()+" inner node:"+primitives.size());
			}
			
			
			List<FMClusterNode> clusterNodes = mfg.getClusterNodes();
			for (FMClusterNode fmClusterNode : clusterNodes) {
				Set<FMNode> predecessors = fmClusterNode.getPredecessors();
				Set<FMNode> successors = fmClusterNode.getSuccessors();
				FMClusterNode target = null;
				boolean isAllCluster = true;
				double max =Double.MIN_VALUE;
				for (FMNode fmNode : predecessors) {
					if(!(fmNode instanceof FMClusterNode)){
						isAllCluster = false;
					}
					
				}
				for(FMNode fmNode : successors){
					if(!(fmNode instanceof FMClusterNode)){
						isAllCluster = false;
					}
				}
				if(!isAllCluster){
					continue;
				}
				for (FMNode fmNode : predecessors) {
					for(FMEdgeImpl edge: edgeImpls){
						if(fmClusterNode.equals(edge.getTarget())&&(fmNode.equals(edge.getSource()))){
							if(fmNode instanceof FMClusterNode){
								if(edge.getCorrelation() > max){
									max = edge.getCorrelation();
									target = (FMClusterNode) fmNode;
								}
							}
						}
					}
					
				}
				for (FMNode fmNode : successors) {
					for(FMEdgeImpl edge: edgeImpls){
						if(fmClusterNode.equals(edge.getSource())&&(fmNode.equals(edge.getTarget()))){
							if(fmNode instanceof FMClusterNode){
								if(edge.getCorrelation() > max){
									max = edge.getCorrelation();
									target = (FMClusterNode) fmNode;
								}
							}
						}
					}
					
				}
				if(target != null){
					mfg.removeClusterNode(target);
					target.add(fmClusterNode);
					mfg.removeClusterNode(fmClusterNode);
					mfg.addClusterNode(target);
				}
				
				
			}
			
			System.out.println("----------------------------");
			nodeSignificanceMetric = mfg.getNodeSignificanceMetric();
			for(int i = 0; i < size; i++ ){
				System.out.print(nodeSignificanceMetric.getMeasure(i)+" ");
			}
			System.out.println();
			// List<BinaryLogMetric> metrics = mr.getBinaryLogMetrics();
			// for(BinaryLogMetric metric : metrics){
			// int size =
			// XLogInfoFactory.createLogInfo(log).getEventClasses().size();
			// int eventSize = metric.size();
			// System.out.println("eventSize: " + eventSize);
			// System.out.println("the metric is " +
			// metric.getName()+"---------------------");
			// for(int x= 0; x < size; x++){
			// for(int y = 0; y < size; y++){
			// System.out.print(metric.getMeasure(x, y) + " ");;
			// }
			// System.out.println();
			// }
			// System.out.println("----------------------------");
			// }
//			System.out.println();
//			MutableFuzzyGraph mfg = new MutableFuzzyGraph(mr);
//			int size = XLogInfoFactory.createLogInfo(log).getEventClasses()
//					.size();
//			BinaryMetric edgeSignificance = mr
//					.getAggregateSignificanceBinaryMetric();
//			BinaryMetric edgeCorrelation = mr
//					.getAggregateCorrelationBinaryMetric();
//			RelationshipSignficanceMetirc rsm = new RelationshipSignficanceMetirc(
//					"", "", edgeSignificance, edgeCorrelation, size);
//			rsm.caculate();
//			for (int x = 0; x < size; x++) {
//				for (int y = 0; y < size; y++) {
//					System.out.print(rsm.getMeasure(x, y) + " ");
//				}
//				System.out.println();
//			}
//			System.out.println("--------------");
//			rsm.cut(0.8, 0.2);
//			for (int x = 0; x < size; x++) {
//				for (int y = 0; y < size; y++) {
//					System.out.print(rsm.getMeasure(x, y) + " ");
//				}
//				System.out.println();
//			}
//			// System.out.println(size+"====");
//			rsm.filterEdge(0.5, 0.2);
//			System.out.println("--------------");
//			for (int x = 0; x < size; x++) {
//				for (int y = 0; y < size; y++) {
//					System.out.print(rsm.getMeasure(x, y) + " ");
//				}
//				System.out.println();
//			}
			
			
			set = mfg.getNodes();
			for (FMNode fmNode : set) {
				System.out.print("index: "+fmNode.getIndex() +" name:"+fmNode.getElementName());
				System.out.println(fmNode instanceof FMClusterNode);
			}
			
			
		}

	}

}
