package org.processmining.models.graphbased.directed.fuzzymodel.metrics;


// import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryDerivateMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryLogMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryDerivateMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryLogMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMLog;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMLogEvents;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FuzzyMinerLog;

/**
 * @author christian
 * 
 */
public class LogScanner {

	private static final boolean DEBUG = false;
	protected int maxLookBack = 5;
	protected ArrayList<XEvent> lookBack;
	protected ArrayList<Integer> lookBackIndices;

	public void scan(XLog log, MetricsRepository metrics, Attenuation attenuation, int maxLookBack, Progress progress,
			int initialProgressValue) throws IndexOutOfBoundsException {
		if (progress != null) {
			progress.setCaption("Scanning log based metrics...");
		}
		this.maxLookBack = maxLookBack;
		FMLogEvents logEvents = FuzzyMinerLog.getLogEvents(log);

		XEvent referenceAte;
		int referenceIndex, followerIndex;
		double att;
		//Deal with every trace
		int num =1;
		for(XTrace pi : FuzzyMinerLog.getTraces(log)){
			//System.out.println("Begin to work on Instance " + num);
			lookBack = new ArrayList<XEvent>(maxLookBack);
			lookBackIndices = new ArrayList<Integer>(maxLookBack);
            for (XEvent followerAte : FuzzyMinerLog.getEvents(pi)){       	
				// update progress, if available
				if (progress != null) {
					initialProgressValue++;
					if (initialProgressValue % 200 == 0) {
						progress.setValue(initialProgressValue);
					}
				}
				// update look back buffer with next audit trail entry
				followerIndex = logEvents.findLogEventNumber(followerAte);
				//for degug
				if (DEBUG) {
					String followeEventname = FMLog.getConceptName(followerAte);
					System.out.println("Now!!! The EventName of the followerAte is " + followeEventname);
					System.out.println("Now!!! The followerIndex is " + followerIndex);

				}
				lookBack.add(0, followerAte);//将当前的event放入lookBack
				lookBackIndices.add(0, followerIndex);
				if (lookBack.size() > (maxLookBack + 1)) {
					// trim look back buffer
					lookBack.remove((maxLookBack + 1));
					lookBackIndices.remove((maxLookBack + 1));
				}
				// transmit event to unary metrics
				for (UnaryLogMetric metric : metrics.getUnaryLogMetrics()) {
					//System.out.println(metric.get);
					metric.measure(followerAte, followerIndex);
				}
				// iterate over multi-step relations
				for (int k = 1; k < lookBack.size(); k++) {
					referenceAte = lookBack.get(k);
					referenceIndex = lookBackIndices.get(k);
					if (DEBUG) {
						String referenceEventname = FMLog.getConceptName(referenceAte);
						System.out.println("Now!!! The EventName of the referenceAte is " + referenceEventname);
						System.out.println("Now!!! The referenceIndex is " + referenceIndex);

					}
					att = attenuation.getAttenuationFactor(k);
					// transmit relation to all registered metrics
					for (BinaryLogMetric metric : metrics.getBinaryLogMetrics()) {
						metric.measure(referenceAte, followerAte, referenceIndex, followerIndex, att);
					}
				}
			}
			num++;
		}
		// calculate derivate metrics
		List<UnaryDerivateMetric> unaryDerivateMetrics = metrics.getUnaryDerivateMetrics();
		for (UnaryDerivateMetric metric : unaryDerivateMetrics) {
			metric.measure();
		}
		List<BinaryDerivateMetric> binaryDerivateMetrics = metrics.getBinaryDerivateMetrics();
		for (BinaryDerivateMetric metric : binaryDerivateMetrics) {
			metric.measure();
		}
	}
	public void scan(XLog log, MetricsRepository metrics, Attenuation attenuation, int maxLookBack) 
			throws IndexOutOfBoundsException {
		
		this.maxLookBack = maxLookBack;
		FMLogEvents logEvents = FuzzyMinerLog.getLogEvents(log);

		XEvent referenceAte;
		int referenceIndex, followerIndex;
		double att;
		//Deal with every trace
		int num =1;
		for(XTrace pi : FuzzyMinerLog.getTraces(log)){
			//System.out.println("Begin to work on Instance " + num);
			lookBack = new ArrayList<XEvent>(maxLookBack);
			lookBackIndices = new ArrayList<Integer>(maxLookBack);
            for (XEvent followerAte : FuzzyMinerLog.getEvents(pi)){       	
				// update progress, if available
				
				// update look back buffer with next audit trail entry
				followerIndex = logEvents.findLogEventNumber(followerAte);
				//for degug
				
				lookBack.add(0, followerAte);//将当前的event放入lookBack
				lookBackIndices.add(0, followerIndex);
				if (lookBack.size() > (maxLookBack + 1)) {
					// trim look back buffer
					lookBack.remove((maxLookBack + 1));
					lookBackIndices.remove((maxLookBack + 1));
				}
				// transmit event to unary metrics
				for (UnaryLogMetric metric : metrics.getUnaryLogMetrics()) {
					metric.measure(followerAte, followerIndex);
				}
				// iterate over multi-step relations
				for (int k = 1; k < lookBack.size(); k++) {
					referenceAte = lookBack.get(k);
					referenceIndex = lookBackIndices.get(k);
					
					att = attenuation.getAttenuationFactor(k);
					// transmit relation to all registered metrics
					for (BinaryLogMetric metric : metrics.getBinaryLogMetrics()) {
						metric.measure(referenceAte, followerAte, referenceIndex, followerIndex, att);
					}
				}
			}
			num++;
		}
		// calculate derivate metrics
		//List<UnaryDerivateMetric> unaryDerivateMetrics = metrics.getUnaryDerivateMetrics();
		for (UnaryDerivateMetric metric : metrics.getUnaryDerivateMetrics()) {
			metric.measure();
		}
		//List<BinaryDerivateMetric> binaryDerivateMetrics = metrics.getBinaryDerivateMetrics();
		for (BinaryDerivateMetric metric : metrics.getBinaryDerivateMetrics()) {
			metric.measure();
		}
	}
}
