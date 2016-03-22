/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;

import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XParserRegistry;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.LinearAttenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.impl.FMEdgeImpl;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.LogScanner;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryLogMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.simplification.Pretreat;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryLogMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryMetric;

/**
 * @author Administrator
 *TODO
 */
public class Test {

	@org.junit.Test
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
			Pretreat pretreat = new Pretreat(mfg);
			Set<FMEdgeImpl> edgeImpls = mfg.getEdgeImpls();
			for (FMEdgeImpl fmEdgeImpl : edgeImpls) {
				System.out.println("Source: " +fmEdgeImpl.getSource().getElementName()
						+" target: "+fmEdgeImpl.getTarget().getElementName() 
						+ " Significance: "+fmEdgeImpl.getSignificance()
						+ " Correlation: "+fmEdgeImpl.getCorrelation());
			}
			System.out.println("----------------------------------");
			pretreat.conflictResolve(0.4, 0.4);
			edgeImpls = mfg.getEdgeImpls();
			for (FMEdgeImpl fmEdgeImpl : edgeImpls) {
				System.out.println("Source: " +fmEdgeImpl.getSource().getElementName()
						+" target: "+fmEdgeImpl.getTarget().getElementName() 
						+ " Significance: "+fmEdgeImpl.getSignificance()
						+ " Correlation: "+fmEdgeImpl.getCorrelation());
			}
			System.out.println("----------------------------------");
			pretreat.edgeFilter(0.5, 0.4);
			edgeImpls = mfg.getEdgeImpls();
			for (FMEdgeImpl fmEdgeImpl : edgeImpls) {
				System.out.println("Source: " +fmEdgeImpl.getSource().getElementName()
						+" target: "+fmEdgeImpl.getTarget().getElementName() 
						+ " Significance: "+fmEdgeImpl.getSignificance()
						+ " Correlation: "+fmEdgeImpl.getCorrelation());
			}
			System.out.println("----------------------------------");
			pretreat.nodeAggregatinoAndAbstraction(1);
		}
	}

}
