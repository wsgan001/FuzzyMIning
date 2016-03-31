/**
 * 
 */
package test;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.json.Json;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XParserRegistry;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.LinearAttenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.impl.FMEdgeImpl;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.FastTransformer;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.FuzzyEdgeTransformer;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.NewConcurrencyEdgeTransformer;


/**
 * @author Administrator TODO
 */
public class Test3 {

	@Test
	public void test() throws Exception {
		double concurrency_ratio = 0.25;//CONCURRENCY_RATIO ratio
		double concurrency_threshold = 0.7;//CONCURRENCY_THRESHOLD threshold
		double fuzzy_edge_cutoff = 0.6;//FUZZY_EDGE_CUTOFF fuzzyECutoff
		double fuzzy_edge_ratio = 0.7;//FUZZY_EDGE_RATIO fuzzyERatio
		double node_cutoff = 0.5;//NODE_CUTOFF nodeThreshold
		
		
		File file = new File("doc/teleclaims.xes");
		//File file = new File("doc/L1.xes");
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
			FastTransformer fastTransformer = new FastTransformer(null);
			
			//BestEdgeTransformer bestEdgeTransformer = new BestEdgeTransformer(null);
			FuzzyEdgeTransformer fuzzyEdgeTransformer = new FuzzyEdgeTransformer(null);
			NewConcurrencyEdgeTransformer concurrencyEdgeTransformer = 
					new NewConcurrencyEdgeTransformer(null);
			
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(fuzzy_edge_ratio);
			fuzzyEdgeTransformer.setPreservePercentage(fuzzy_edge_cutoff);
			concurrencyEdgeTransformer.setPreserveThreshold(concurrency_threshold);
			concurrencyEdgeTransformer.setRatioThreshold(concurrency_ratio);
			fastTransformer.setThreshold(node_cutoff );
			
			fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
			
			
			mfg.initializeGraph();
			mfg.setBinaryRespectiveSignificance();
			fastTransformer.transform(mfg);
			mfg.setEdgeImpls();
			
			Set<FMEdgeImpl> fmEdges = mfg.getFMEdges();
			JSONObject obj = null;
			JSONArray edgeArray = new JSONArray();
			for (FMEdgeImpl fmEdgeImpl : fmEdges) {
				obj = new JSONObject();
				obj.put("source", fmEdgeImpl.getSource().getIndex());
				obj.put("target", fmEdgeImpl.getTarget().getIndex());
				obj.put("significance", fmEdgeImpl.getSignificance());
				obj.put("correlation", fmEdgeImpl.getCorrelation());
				edgeArray.add(obj);
			}
			System.out.println(edgeArray);
			System.out.println("-----------------------------");
			Set<FMNode> nodes = mfg.getNodes();
			JSONArray nodeArray = new JSONArray();
			for (FMNode fmNode : nodes) {
				obj = new JSONObject();
				obj.put("index", fmNode.getIndex());
				obj.put("significance", fmNode.getSignificance());
				obj.put("name", fmNode.getElementName());
				if(fmNode.getElementName().contains("Cluster")){
					Set<FMNode> primitives = ((FMClusterNode) fmNode).getPrimitives();
					JSONArray primitiveNode = new JSONArray();
					for (FMNode node : primitives) {
						JSONObject o = new JSONObject();
						o.put("name", node.getElementName());
						primitiveNode.add(o);
					}
					obj.put("primitiveNode", primitiveNode);
					obj.put("label", "Cluster");
				}else{
					obj.put("label", "Node");
				}
				nodeArray.add(obj);
			}
			System.out.println(nodeArray);
			System.out.println("-----------------------------");
		}
		
		//fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
	}

}
