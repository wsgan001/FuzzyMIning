package org.processmining.models.graphbased.directed.fuzzymodel.transform;

import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;

public class TransformerUtils {
	public static String getNodeLabel(FMNode fmNode) {
		String label = "";
		if (fmNode instanceof FMClusterNode) {
			FMClusterNode clusterNode = (FMClusterNode) fmNode;
			label = "<html>" + clusterNode.id() + "<br>" + Integer.toString(clusterNode.size()) + " elements " + "<br>"
					+ "~" + MutableFuzzyGraph.format(clusterNode.getSignificance()) + "<html>";
		} else {

			label = "<html>" + fmNode.getElementName() + "<br>" + fmNode.getEventType() + "<br>"
					+ MutableFuzzyGraph.format(fmNode.getSignificance()) + "<html>";
		}
		return label;

	}

}
