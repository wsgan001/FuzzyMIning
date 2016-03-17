package org.processmining.models.graphbased.directed.fuzzymodel.metrics;

public class Metric implements Cloneable {
	protected String name;
	protected String description;
	protected double normalizationMaximum;
	protected boolean invert;

	public Metric(String aName, String aDescription) {
		name = aName;
		description = aDescription;
		normalizationMaximum = 1.0;
		/**是否反转*/
		invert = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getNormalizationMaximum() {
		return normalizationMaximum;
	}

	public void setNormalizationMaximum(double normalizationMaximum) {
		this.normalizationMaximum = normalizationMaximum;
	}

	public boolean isInvert() {
		return invert;
	}

	public void setInvert(boolean invert) {
		this.invert = invert;
	}

	public Object clone() {
		Metric clone = null;
		try {
			clone = (Metric) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		return clone;
	}
}
