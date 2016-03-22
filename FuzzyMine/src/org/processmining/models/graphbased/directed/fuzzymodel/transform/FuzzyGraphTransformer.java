
package org.processmining.models.graphbased.directed.fuzzymodel.transform;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;

public abstract class FuzzyGraphTransformer {

	protected String name;
	protected PluginContext context;

	@SuppressWarnings("unused")
	private FuzzyGraphTransformer() {
		// disable usage in subclasses
	}

	public FuzzyGraphTransformer(PluginContext context, String name) {
		this.name = name;
		this.context = context;
	}

	public String getName() {
		return name;
	}

	public PluginContext getContext() {
		return context;
	}

	public String toString() {
		return name;
	}

	public abstract void transform(MutableFuzzyGraph graph);

}
