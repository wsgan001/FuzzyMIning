package org.processmining.models.graphbased.directed.fuzzymodel.attenuation;

public abstract class Attenuation {
	
	        protected int bufSize;
	        protected double[] attenuationFactors;
	
	        public Attenuation() {
	                this(10);
	        }
	
	        public Attenuation(int bufferSize) {
	                bufSize = bufferSize;
	                attenuationFactors = null;
	        }
	
	        public double attenuate(double value, int distance) {	                
	        	return (value * getAttenuationFactor(distance));
	        }
	        /**
	         * 
	         * @param distance
	         * @return
	         */
	        public double getAttenuationFactor(int distance) {
	                if (distance < bufSize) {
	                        if (attenuationFactors == null) {
	                                generateBuffer();
	                        }
	                        return attenuationFactors[distance];
	                } else {
	                        return createAttenuationFactor(distance);
	                }
	        }
	
	        protected void generateBuffer() {
	                attenuationFactors = new double[bufSize];
	                for (int i = 0; i < bufSize; i++) {
	                        attenuationFactors[i] = createAttenuationFactor(i);
	                }
	        }
	
	        protected abstract double createAttenuationFactor(int distance);
	
	        public abstract String getName();
	
	        public abstract String getDescription();
	}
