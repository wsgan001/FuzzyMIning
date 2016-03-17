
package org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XEvent;


public class DataTypeCorrelationMetric extends CorrelationBinaryLogMetric {

	/**
	 * @param eventCount
	 */
	public DataTypeCorrelationMetric(int eventCount) {
		super("Data type correlation",
				"Measures the correlation of two events by their relative overlap of attribute types.", eventCount);
	}

	/**
	 * 
	 */
	public DataTypeCorrelationMetric() {
		super("Data type correlation",
				"Measures the correlation of two events by their relative overlap of attribute types.");
	}

	protected boolean isStandardKey(String key) {
		if (key.contains("concept") || key.contains("lifecycle") || key.contains("org") || key.contains("time")
				|| key.contains("semantic")) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.BinaryLogMetric#measure(
	 * org.processmining.framework.log.AuditTrailEntry,
	 * org.processmining.framework.log.AuditTrailEntry)
	 */
	protected double measure(XEvent reference, XEvent follower) {
		Set<String> refKeySet = reference.getAttributes().keySet();
		Set<String> folKeySet = follower.getAttributes().keySet();
		Set<String> refDataKeySet = new HashSet<String>();
		Set<String> folDataKeySet = new HashSet<String>();

		//omit the standard XES Extentions,keep the data type extensions only
		for (String key : refKeySet) {
			if (!isStandardKey(key)) {
				refDataKeySet.add(key);
			}
		}
		for (String key : folKeySet) {
			if (!isStandardKey(key)) {
				folDataKeySet.add(key);
			}
		}

		if ((refDataKeySet.size() == 0) || (folDataKeySet.size() == 0)) {
			return 0.0;
		}
		int overlap = 0;
		for (String key : refDataKeySet) {
			if (folDataKeySet.contains(key)) {
				overlap++;
			}
		}
		return (overlap / refDataKeySet.size());

	}

}
