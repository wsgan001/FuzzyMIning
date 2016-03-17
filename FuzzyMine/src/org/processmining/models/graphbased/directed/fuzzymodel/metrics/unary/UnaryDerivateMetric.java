package org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary;

import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;

public abstract class UnaryDerivateMetric extends UnaryMetric {
	
	        protected MetricsRepository repository;
	
	        /**
	         * @param aName
	         * @param aDescription
	         * @param aRepository
	         */
	        public UnaryDerivateMetric(String aName, String aDescription, MetricsRepository aRepository) {
	                super(aName, aDescription, aRepository.getNumberOfLogEvents());
	                repository = aRepository;
	        }
	
	        public abstract void measure();
	
	}

