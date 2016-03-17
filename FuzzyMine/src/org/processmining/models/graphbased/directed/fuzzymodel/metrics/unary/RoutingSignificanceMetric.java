package org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary;

import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.AggregateBinaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FuzzyMinerLog;

/**
 * 
 * 
 */
public class RoutingSignificanceMetric extends UnaryDerivateMetric {

	/**
	 * @param aRepository
	 */
	public RoutingSignificanceMetric(MetricsRepository aRepository) {
		super("Routing significance",
				"Measures the significance of a node by weighing incoming against outgoing relations", aRepository);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.fuzzymining.metrics.UnaryDerivateMetric#measure
	 * ()
	 */
	public void measure() {
		int size = FuzzyMinerLog.getLogEvents(repository.getLogReader()).size();
		double inValue, outValue, quotient, tmpSig, tmpCor;
		AggregateBinaryMetric binMetricCorrelation = repository.getAggregateCorrelationBinaryLogMetric();
		AggregateBinaryMetric binMetricSignificance = repository.getAggregateSignificanceBinaryLogMetric();
		for (int i = 0; i < size; i++) {
			inValue = 0.0;
			outValue = 0.0;
			// compose incoming and outgoing forces, combined
			for (int x = 0; x < size; x++) {
				if (x == i) {
					continue;
				} // skip self-references
				// compose incoming force
				tmpSig = binMetricSignificance.getMeasure(x, i);
				tmpCor = binMetricCorrelation.getMeasure(x, i);
				inValue += (tmpSig * tmpCor);
				// compose outgoing force
				tmpSig = binMetricSignificance.getMeasure(i, x);
				tmpCor = binMetricCorrelation.getMeasure(i, x);
				outValue += (tmpSig * tmpCor);
			}
			// calculate quotient
			if ((inValue == 0.0) && (outValue == 0.0)) {
				quotient = 0.0;
			} else {
				quotient = ((inValue - outValue) / (inValue + outValue));
				if (quotient < 0.0) {
					quotient = -quotient;
				}
			}
			values[i] = quotient;
		}
		// reset normalization matrix
		normalized = null;
	}
}
