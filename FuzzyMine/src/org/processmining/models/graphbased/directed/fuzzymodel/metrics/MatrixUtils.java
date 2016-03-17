package org.processmining.models.graphbased.directed.fuzzymodel.metrics;

import cern.colt.matrix.DoubleMatrix2D;

public class MatrixUtils {
	
	        public static double getMaxValue(DoubleMatrix2D matrix) {
	                double maxFound = 0.0;
	                double current;
	                for (int x = matrix.columns() - 1; x >= 0; x--) {
	                        for (int y = matrix.rows() - 1; y >= 0; y--) {
	                                current = matrix.get(x, y);
	                                if (current > maxFound) {
	                                        maxFound = current;
	                                }
	                        }
	                }
	                return maxFound;
	        }
	
	        public static DoubleMatrix2D multiplyAllFields(DoubleMatrix2D matrix, double factor) {
	                for (int x = matrix.columns() - 1; x >= 0; x--) {
                        for (int y = matrix.rows() - 1; y >= 0; y--) {
	                                matrix.set(x, y, (matrix.get(x, y) * factor));
	                        }
	                }
	                return matrix;
	        }
	
	        public static DoubleMatrix2D setAllFields(DoubleMatrix2D matrix, double value) {
	                for (int x = matrix.columns() - 1; x >= 0; x--) {
	                        for (int y = matrix.rows() - 1; y >= 0; y--) {
	                                matrix.set(x, y, value);
	                        }
	                }
	                return matrix;
        }
	
	        public static DoubleMatrix2D normalize(DoubleMatrix2D matrix, double maximum) {
	                if (maximum == 0.0) {
	                        return setAllFields(matrix, 0.0); // disabled matrix / metric
	                }
	                double maxValue = getMaxValue(matrix);
                // do not normalize all-zero matrices
	                if (maxValue > 0.0) {	                        
	                	double factor = maximum / maxValue;
	                        return multiplyAllFields(matrix, factor);
	                } else {
	                        return matrix;
	                }
	        }
	
	        public static DoubleMatrix2D normalize(DoubleMatrix2D matrix) {
	                return normalize(matrix, 1.0);
	        }
	
	        public static boolean verifyMatrix(DoubleMatrix2D matrix) {
	                return verifyMatrix(matrix, 0.0, 1.0);
	        }
	
	        public static boolean verifyMatrix(DoubleMatrix2D matrix, double min, double max) {
	                int width = matrix.columns();
	                int height = matrix.rows();
	                if (width != height) {
	                        System.err.println("Matrix test failed (unbalanced)!");
	                        return false;                }
	                double probe;
	                for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
	                                probe = matrix.get(x, y);
	                                if (probe == Double.NaN) {
	                                        System.err.println("Matrix test failed (NaN at " + x + "," + y + ")!");
	                                        return false;
	                                } else if (probe < min) {
	                                        System.err.println("Matrix test failed (< MIN at " + x + "," + y + ")!");
	                                        return false;
	                                } else if (probe > max) {
	                                        System.err.println("Matrix test failed (> MAX at " + x + "," + y + ")!");
                                        return false;
	                                }
                       }
	                }
	                return true;
	        }

	}

