package org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary;

import java.util.Date;

import org.deckfour.xes.model.XEvent;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMLog;

public class ProximityCorrelationMetric extends CorrelationBinaryLogMetric {

	/**
	 * @param eventCount
	 */
	public ProximityCorrelationMetric(int eventCount) {
		super("Proximity correlation", "Measures the correlation of two events by their temporal proximity", eventCount);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	public ProximityCorrelationMetric() {
		super("Proximity correlation", "Measures the correlation of two events by their temporal proximity");
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.BinaryLogMetric#measure(
	 * org.processmining.framework.log.XEvent,
	 * org.processmining.framework.log.XEvent)
	 */
	protected double measure(XEvent reference, XEvent follower) {
		//Date tsRef = reference.getTimestamp();
		//Date tsFol = follower.getTimestamp();
		Date tsRef = FMLog.getTimestamp(reference);
		Date tsFol = FMLog.getTimestamp(follower);
//		String key = "time:timestamp";
//		XAttributeTimestampImpl tsref_attr = (XAttributeTimestampImpl) reference.getAttributes().get(key);
//		XAttributeTimestampImpl tsFol_attr = (XAttributeTimestampImpl) follower.getAttributes().get(key);
		if ((tsRef != null) && (tsFol != null)) {
//			Date tsRef = tsref_attr.getValue();
//			Date tsFol = tsFol_attr.getValue();
			long tRef = tsRef.getTime();
			long tFol = tsFol.getTime();
			if (tRef != tFol) {
				return (1.0 / (tFol - tRef));
			} else {
				return 1.0;
			}
		} else {
			return 0.0;
		}
	}
}
