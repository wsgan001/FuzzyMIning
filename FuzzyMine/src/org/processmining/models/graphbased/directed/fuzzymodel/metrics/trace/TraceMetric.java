
package org.processmining.models.graphbased.directed.fuzzymodel.metrics.trace;

// import org.processmining.framework.log.LogReader;
// import org.processmining.framework.log.LogSummary;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryMetric;

/**
 * @author christian
 * 
 */
public abstract class TraceMetric extends UnaryMetric {

	/**
	 * @param aName
	 * @param aDescription
	 */
	public TraceMetric(String aName, String aDescription, XLogInfo logSummary) {
		super(aName, aDescription, logSummary.getNumberOfTraces());
		normalized = null;
	}

	public abstract void measure(XLog log);

}
