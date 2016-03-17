package org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary;

import org.deckfour.xes.model.XEvent;

public abstract class UnaryLogMetric extends UnaryMetric {

	/**
	 * @param aName
	 * @param aDescription
	 */
	public UnaryLogMetric(String aName, String aDescription, int aNumberOfEvents) {
		super(aName, aDescription, aNumberOfEvents);
	}

	/**
	 * @param aName
	 * @param aDescription
	 */
	public UnaryLogMetric(String aName, String aDescription) {
		super(aName, aDescription);
	}

	public void measure(XEvent ate, int ateIndex) {
		//System.out.println(values==null);
		values[ateIndex] += measureImpl(ate);
		normalized = null;
	}

	protected abstract double measureImpl(XEvent ate);

}
