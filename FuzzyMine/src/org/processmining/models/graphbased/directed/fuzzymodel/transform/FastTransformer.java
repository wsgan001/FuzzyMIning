
package org.processmining.models.graphbased.directed.fuzzymodel.transform;

import java.util.ArrayList;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;
//import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;

public class FastTransformer extends FuzzyGraphTransformer {

	/**cutoff threshold 用于找到victim*/
	protected double threshold;
	protected MutableFuzzyGraph graph;
	protected ArrayList<FuzzyGraphTransformer> preTransformers;
	protected ArrayList<FuzzyGraphTransformer> interimTransformers;
	protected ArrayList<FuzzyGraphTransformer> postTransformers;

	ArrayList<FMClusterNode> clusters;
	public FastTransformer(PluginContext context) {
		super(context, "Fast transformer");
		threshold = 1.0;
		graph = null;
		preTransformers = new ArrayList<FuzzyGraphTransformer>();
		interimTransformers = new ArrayList<FuzzyGraphTransformer>();
		postTransformers = new ArrayList<FuzzyGraphTransformer>();
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public double getThreshold() {
		return threshold;
	}

	public void transform(MutableFuzzyGraph graph) {
		this.graph = graph;
		// apply pre-transformers
		//解决边冲突
		for (FuzzyGraphTransformer pre : preTransformers) {
			//context.log("  * Applying pre-flight graph transformer '" + pre.getName() + "'.", MessageLevel.NORMAL);
			pre.transform(graph);
		}
		// perform initial clustering
		clusters = cluster();
		// apply interim-transformers
		for (FuzzyGraphTransformer interim : interimTransformers) {
			//context.log("  * Applying in-flight graph transformer '" + interim.getName() + "'.", MessageLevel.NORMAL);
			interim.transform(graph);
		}
		// merge and reduce cluster set
		clusters = merge(clusters);
		clusters = removeIsolatedClusters(clusters);
		clusters = removeSingularClusters(clusters);
		//set labels to cluster nodes

		for (int i = 0; i < clusters.size(); i++) {
			FMClusterNode clusterNode = clusters.get(i);
			String clusterNodeLabel = TransformerUtils.getNodeLabel(clusterNode);
			clusterNode.setLabel(clusterNodeLabel);
		}
		// apply post-transformers
		for (FuzzyGraphTransformer post : postTransformers) {
			//context.log("  * Applying post-flight graph transformer '" + post.getName() + "'.", MessageLevel.NORMAL);
			post.transform(graph);
		}
		//context.log("Fast transformer completed.", MessageLevel.NORMAL);
	}

	/***
	 * 得到一元重要性小于阈值的牺牲集合
	 * @return
	 */
	protected ArrayList<FMNode> getSimplificationVictims() {
		ArrayList<FMNode> victims = new ArrayList<FMNode>();
		FMNode probe;
		for (int i = graph.getNumberOfInitialNodes() - 1; i >= 0; i--) {
			probe = graph.getPrimitiveNode(i);
			if (probe.getSignificance() < threshold) {
				victims.add(probe);
			}
		}
		return victims;
	}

	protected ArrayList<FMClusterNode> cluster() {
		ArrayList<FMNode> victims = getSimplificationVictims();
		ArrayList<FMClusterNode> clusters = new ArrayList<FMClusterNode>();
		FMNode victim, neighbor;
		int clusterIndex = graph.getNumberOfInitialNodes() + 1;
		// create initial set of clusters
		while (victims.size() > 0) {
			victim = victims.get(0);
			neighbor = getMostCorrelatedNeighbor(victim);
			if (neighbor instanceof FMClusterNode) {
				// most related neighbor is a cluster; merge
				FMClusterNode cluster = (FMClusterNode) neighbor;
				cluster.add(victim);
				graph.setNodeAliasMapping(victim.getIndex(), cluster);
				victims.remove(victim);
				graph.graphElementRemoved(victim);
			} else {
				// create new unary cluster
				//set the clusternode label later
				FMClusterNode cluster = new FMClusterNode(graph, clusterIndex, "");
				clusterIndex++;
				cluster.add(victim);
				graph.setNodeAliasMapping(victim.getIndex(), cluster);
				victims.remove(victim);
				graph.graphElementRemoved(victim);
				if (victims.contains(neighbor)) {
					cluster.add(neighbor);
					graph.setNodeAliasMapping(neighbor.getIndex(), cluster);
					victims.remove(neighbor);
					graph.graphElementRemoved(neighbor);
				}
				graph.addClusterNode(cluster);
				clusters.add(cluster);
			}
		}
		return clusters;
	}

	protected ArrayList<FMClusterNode> merge(ArrayList<FMClusterNode> clusters) {
		int stopCounter = clusters.size();
		FMClusterNode cluster, target;
		for (int index = 0; index < stopCounter;) {
			cluster = clusters.get(index);
			target = getPreferredMergeTarget(cluster);
			if (target != null) {
				// merge into target
				mergeWith(target, cluster);
				clusters.remove(cluster);
				stopCounter--;
			} else {
				// nothing to merge here; move along..
				index++;
			}
		}
		return clusters;
	}

	/***
	 * 将loser中的primitive点add到winner中，在graph的映射中将loser中原始点标记为winner
	 * @param winner
	 * @param loser
	 * @return
	 */
	protected FMClusterNode mergeWith(FMClusterNode winner, FMClusterNode loser) {
		for (FMNode node : loser.getPrimitives()) {
			winner.add(node);
			graph.setNodeAliasMapping(node.getIndex(), winner);
		}
		graph.removeClusterNode(loser);
		return winner;
	}
	
	/***
	 * 每一个cluster的前驱和后继结点中找到correlation最大的那个点，如果是cluster则merge，不是则不改变
	 * @param subject
	 * @return
	 */
	protected FMClusterNode getPreferredMergeTarget(FMClusterNode subject) {
		FMClusterNode preTarget = null;
		FMClusterNode postTarget = null;
		double maxPreCorrelation = 0.0;
		double maxPostCorrelation = 0.0;
		FMClusterNode object;
		double correlation;
		// process subject's preset nodes
		for (FMNode node : subject.getPredecessors()) {
			if (node instanceof FMClusterNode) {
				object = (FMClusterNode) node;
				correlation = getAggregateCorrelation(subject, object);
				if (correlation > maxPreCorrelation) {
					// new preferred target found
					maxPreCorrelation = correlation;
					preTarget = object;
				}
			} else {
				// abort search in preset
				preTarget = null;
				maxPreCorrelation = 0.0;
				break;
			}
		}
		// process subject's postset nodes
		for (FMNode node : subject.getSuccessors()) {
			if (node instanceof FMClusterNode) {
				object = (FMClusterNode) node;
				correlation = getAggregateCorrelation(subject, object);
				if (correlation > maxPostCorrelation) {
					// new preferred target found
					maxPostCorrelation = correlation;
					postTarget = object;
				}
			} else {
				// abort search in postset
				if (preTarget != null) {
					return preTarget;
				} else {
					return null;
				}
			}
		}
		// subject has both only clusters as pre- and postset
		// nodes: return most correlated neighbor out of both
		if (maxPreCorrelation > maxPostCorrelation) {
			return preTarget;
		} else {
			return postTarget;
		}
	}

	/***
	 * cluater A,B内的每一个原始结点之间的correlation相加
	 * @param a
	 * @param b
	 * @return
	 */
	protected double getAggregateCorrelation(FMClusterNode a, FMClusterNode b) {
		Set<FMNode> aPrimitives = a.getPrimitives();
		Set<FMNode> bPrimitives = b.getPrimitives();
		double aggregateCorrelation = 0.0;
		for (FMNode aNode : aPrimitives) {
			for (FMNode bNode : bPrimitives) {
				aggregateCorrelation += graph.getBinaryCorrelation(aNode.getIndex(), bNode.getIndex());
				aggregateCorrelation += graph.getBinaryCorrelation(bNode.getIndex(), aNode.getIndex());
			}
		}
		return aggregateCorrelation;
	}

	/***
	 * have no Predecessors and Successors
	 * @param clusters
	 * @return
	 */
	protected ArrayList<FMClusterNode> removeIsolatedClusters(ArrayList<FMClusterNode> clusters) {
		Set<FMNode> preset;
		Set<FMNode> postset;
		int stopCounter = clusters.size();
		FMClusterNode cluster;
		for (int index = 0; index < stopCounter;) {
			cluster = clusters.get(index);
			preset = cluster.getPredecessors();
			postset = cluster.getSuccessors();
			if ((preset.size() == 0) && (postset.size() == 0)) {
				// cluster is isolated; remove from graph
				for (FMNode node : cluster.getPrimitives()) {
					graph.setNodeAliasMapping(node.getIndex(), null);
				}

				graph.removeClusterNode(cluster);
				//graph.removeFMClusterNode(cluster);
				clusters.remove(index);
				stopCounter--;
			} else {
				index++;
			}
		}
		return clusters;
	}

	/***
	 * 单个点，就自己一个node组成的cluster
	 * @param clusters
	 * @return
	 */
	protected ArrayList<FMClusterNode> removeSingularClusters(ArrayList<FMClusterNode> clusters) {
		int stopCounter = clusters.size();
		FMClusterNode cluster;
		for (int index = 0; index < stopCounter;) {
			cluster = clusters.get(index);
			if (cluster.size() == 1) {
				// remove cluster here
				eliminateSingularClusterPreservingLinks(cluster);
				clusters.remove(index);
				stopCounter--;
			} else {
				index++;
			}
		}
		return clusters;
	}

	/***
	 * 计算单个聚类的前后连接
	 * @param cluster
	 */
	protected void eliminateSingularClusterPreservingLinks(FMClusterNode cluster) {
		FMNode singularNode = cluster.getPrimitives().toArray(new FMNode[1])[0];
		int ownIndex = singularNode.getIndex();
		int preIndex, postIndex;
		double fromSig, toSig, fromCorr, toCorr;
		Set<FMNode> preSet = singularNode.getPredecessors();
		Set<FMNode> postSet = singularNode.getSuccessors();
		for (FMNode pre : preSet) {
			if (pre instanceof FMClusterNode) {
				continue;
			}
			preIndex = pre.getIndex();
			for (FMNode post : postSet) {
				if (post instanceof FMClusterNode) {
					continue;
				}
				postIndex = post.getIndex();
				if (graph.getBinarySignificance(preIndex, postIndex) == 0.0) {
					// no link previously existing
					fromSig = graph.getBinarySignificance(preIndex, ownIndex);
					toSig = graph.getBinarySignificance(ownIndex, postIndex);
					fromCorr = graph.getBinaryCorrelation(preIndex, ownIndex);
					toCorr = graph.getBinaryCorrelation(ownIndex, postIndex);
					graph.setBinarySignificance(preIndex, postIndex, (fromSig + toSig) / 2.0);
					graph.setBinaryCorrelation(preIndex, postIndex, (fromCorr + toCorr) / 2.0);
				}
				// delete edges to singular node
				graph.setBinaryCorrelation(preIndex, ownIndex, 0.0);
				graph.setBinarySignificance(preIndex, ownIndex, 0.0);
				graph.setBinaryCorrelation(ownIndex, postIndex, 0.0);
				graph.setBinarySignificance(ownIndex, postIndex, 0.0);
			}
		}
		graph.setNodeAliasMapping(singularNode.getIndex(), null);
		graph.removeClusterNode(cluster);
		//LJF comment start  to remove the corresponding event
		//graph.removeFMClusterNode(cluster);
	}

	protected FMNode getMostSignificantNeighbor(FMNode node) {
		int refIndex = node.getIndex();
		double maxSignificance = 0.0;
		double curSignificance;
		FMNode winner = null;
		for (int i = graph.getNumberOfInitialNodes() - 1; i >= 0; i--) {
			if (i == refIndex) {
				continue;
			} // skip self
			// check forward relation
			curSignificance = graph.getBinarySignificance(refIndex, i);
			if (curSignificance > maxSignificance) {
				winner = graph.getNodeMappedTo(i);
				maxSignificance = curSignificance;
			}
			// check backward relation
			curSignificance = graph.getBinarySignificance(i, refIndex);
			if (curSignificance > maxSignificance) {
				winner = graph.getNodeMappedTo(i);
				maxSignificance = curSignificance;
			}
		}
		return winner;
	}

	/***
	 * 得到相关性最高的邻居结点
	 * @param node
	 * @return
	 */
	protected FMNode getMostCorrelatedNeighbor(FMNode node) {
		int refIndex = node.getIndex();
		double maxCorrelation = 0.0;
		double curCorrelation;
		FMNode winner = null;
		for (int i = graph.getNumberOfInitialNodes() - 1; i >= 0; i--) {
			if (i == refIndex) {
				continue;
			} // skip self
			// check forward relation
			curCorrelation = graph.getBinaryCorrelation(refIndex, i);
			if (curCorrelation > maxCorrelation) {
				winner = graph.getNodeMappedTo(i);
				maxCorrelation = curCorrelation;
			}
			// check backward relation
			curCorrelation = graph.getBinaryCorrelation(i, refIndex);
			if (curCorrelation > maxCorrelation) {
				winner = graph.getNodeMappedTo(i);
				maxCorrelation = curCorrelation;
			}
		}
		return winner;
	}

	public void addPreTransformer(FuzzyGraphTransformer transformer) {
		if (preTransformers.contains(transformer) == false) {
			preTransformers.add(transformer);
		}
	}

	public boolean removePreTransformer(FuzzyGraphTransformer transformer) {
		return preTransformers.remove(transformer);
	}

	public void clearPreTransformers() {
		preTransformers.clear();
	}

	public void addInterimTransformer(FuzzyGraphTransformer transformer) {
		if (interimTransformers.contains(transformer) == false) {
			interimTransformers.add(transformer);
		}
	}

	public boolean removeInterimTransformer(FuzzyGraphTransformer transformer) {
		return interimTransformers.remove(transformer);
	}

	public void clearInterimTransformers() {
		interimTransformers.clear();
	}

	public void addPostTransformer(FuzzyGraphTransformer transformer) {
		if (postTransformers.contains(transformer) == false) {
			postTransformers.add(transformer);
		}
	}

	public boolean removePostTransformer(FuzzyGraphTransformer transformer) {
		return postTransformers.remove(transformer);
	}

	public void clearPostTransformers() {
		postTransformers.clear();
	}

	public ArrayList<FMClusterNode> getClusters() {
		return clusters;
	}
	
}
