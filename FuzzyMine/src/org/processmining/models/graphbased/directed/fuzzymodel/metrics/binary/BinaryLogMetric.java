package org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary;

import org.deckfour.xes.model.XEvent;

import cern.colt.matrix.DoubleMatrix2D;

public abstract class BinaryLogMetric extends BinaryMetric {

	protected DoubleMatrix2D divisors;
	protected boolean compensateFrequency;

	public BinaryLogMetric(String name, String description, int eventCount) {
		super(name, description, eventCount);
		divisors = relations.copy();
		compensateFrequency = true;
	}

	public BinaryLogMetric(String name, String description) {
		super(name, description);
		compensateFrequency = true;
	}

	public void setSize(int eventCount) {
		super.setSize(eventCount);
		divisors = relations.copy();
	}

	public double getMeasure(int fromIndex, int toIndex) {
		if ((compensateFrequency == true) && (normalized == null)) {
			rectifyFrequency();
		}
		return super.getMeasure(fromIndex, toIndex);
	}

	public void measure(XEvent reference, XEvent follower, int referenceIndex,
			int followerIndex, double attenuationFactor) {
		double attenuated = measure(reference, follower) * attenuationFactor;
		attenuated += relations.get(referenceIndex, followerIndex);
		relations.set(referenceIndex, followerIndex, attenuated);
		double divisor = divisors.get(referenceIndex, followerIndex)
				+ attenuationFactor;
		divisors.set(referenceIndex, followerIndex, divisor);
		normalized = null;
	}

	protected abstract double measure(XEvent reference, XEvent follower);

	protected void rectifyFrequency() {
		double value, divisor;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				value = relations.get(x, y);
				divisor = divisors.get(x, y);
				if (divisor > 0.0) {
					value /= divisor;
					relations.set(x, y, value);
				}
			}
		}
	}

}
