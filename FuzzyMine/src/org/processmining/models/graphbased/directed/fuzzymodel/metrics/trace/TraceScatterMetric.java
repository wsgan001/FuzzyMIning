
package org.processmining.models.graphbased.directed.fuzzymodel.metrics.trace;

import java.util.HashSet;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMLog;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FuzzyMinerLog;

/**
 * @author christian
 * 
 */
public class TraceScatterMetric extends TraceMetric {

	/**
	 * @param aName
	 * @param aDescription
	 * @param log
	 */
	public TraceScatterMetric(XLogInfo logSummary) {
		super("Trace scatter",
				"Measures traces by their number of distinct event classes relative to their overall size.", logSummary);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.TraceMetric#measure(org.
	 * processmining.framework.log.LogReader)
	 */

	public void measure(XLog log) {
		HashSet<String> eventSet;
		String eventName, eventType;
		int i = 0;
		for (XTrace pi : FuzzyMinerLog.getTraces(log)) {
			eventSet = new HashSet<String>();
			for (XEvent ate : FuzzyMinerLog.getEvents(pi)) {
				try {
					eventName = FMLog.getConceptName(ate);
					eventType = FMLog.getLifecycleTransition(ate);
					eventSet.add(eventName + eventType);
				} catch (IndexOutOfBoundsException e) {
					// no critical error, fail gracefully
					e.printStackTrace();
				}
			}
			values[i] = ((double) eventSet.size() / (double) pi.size());
			i++;
		}
	}

}
