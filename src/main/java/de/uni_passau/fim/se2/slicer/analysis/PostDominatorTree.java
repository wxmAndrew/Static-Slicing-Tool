package de.uni_passau.fim.se2.slicer.analysis;

import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import de.uni_passau.fim.se2.slicer.util.cfg.ProgramGraph;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/** Provides an analyses calculating a post-dominator tree. */
public class PostDominatorTree extends Analysis {

	PostDominatorTree(final ClassNode pClassNode, final MethodNode pMethodNode) {
		super(pClassNode, pMethodNode);
	}

	PostDominatorTree(ProgramGraph pCFG) {
		super(pCFG);
	}

	/**
	 * Return a graph representing the post-dominator tree of the control flow
	 * graph, which is stored in the controlFlowProgramGraph class attribute (this
	 * is inherited from the Analysis class).
	 *
	 * @return A graph representing the post-dominator tree of the control-flow
	 *         graph
	 */
	public ProgramGraph computeResult() {
		// TODO implement computation of post-dominance tree here
		Map<Node, Set<Node>> Dominates = Maps.newHashMap();
		Queue<Node> queue = new LinkedList<Node>();
		final ProgramGraph postDominatorGraph = new ProgramGraph();
		final ProgramGraph reversedGraph = reverseGraph(controlFlowProgramGraph);

		// the computeDominance method returns unmodifiable sets,this loop copy the
		// return into new map-- naive solution
		for (Node node : reversedGraph.getNodes()) {
			Set<Node> transeferDominators = Sets.newHashSet();
			transeferDominators.addAll(computeDominance().get(node));
			Dominates.put(node, transeferDominators);
		}

		postDominatorGraph.addNode(reversedGraph.getEntry());
		queue.add(reversedGraph.getEntry());

		for (Node node : reversedGraph.getNodes()) {
			Dominates.get(node).remove(node);
		}

		while (!queue.isEmpty()) {
			Node m = queue.remove();
			for (Node node : reversedGraph.getNodes()) {
				if (!Dominates.get(node).isEmpty()) {
					if (Dominates.get(node).contains(m)) {
						Dominates.get(node).remove(m);
						if (Dominates.get(node).isEmpty()) {
							postDominatorGraph.addNode(node);
							postDominatorGraph.addEdge(m, node);
							queue.add(node);
						}
					}
				}
			}
		}
		return postDominatorGraph;
	}
	
	/**
	 * Return a map representing the post-dominators for each node in the control flow
	 * graph, which is stored in the controlFlowProgramGraph class attribute (this
	 * is inherited from the Analysis class).
	 *
	 * @return A map maps each node to the set of its post-dominators in the control-flow
	 *         graph
	 */

	public Map<Node, Set<Node>> computeDominance() {

		ProgramGraph reversedGraph = reverseGraph(controlFlowProgramGraph);
		Map<Node, Set<Node>> dependencyMap = Maps.newHashMap();
		Set<Node> set = Sets.newHashSet();
		Set<Node> reversedGraphNodes = reversedGraph.getNodes();
		Node entry = reversedGraph.getEntry();
		set.add(entry);
		dependencyMap.put(entry, set);

		for (Node node : reversedGraphNodes) {
			if (node.equals(entry)) {
				continue;
			}
			dependencyMap.put(node, reversedGraphNodes);
		}
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Node node : reversedGraphNodes) {
				Set<Node> newDominators = Sets.newHashSet();
				Set<Node> currentDominators = Sets.newHashSet();
				if (node.equals(entry)) {
					continue;
				}
				currentDominators = dependencyMap.get(node);
				boolean first = true;
				for (Node predecessor : reversedGraph.getPredecessors(node)) {
					if (first) {
						newDominators.addAll(dependencyMap.get(predecessor));
						first = false;
					} else {
						newDominators.retainAll(dependencyMap.get(predecessor));
					}
				}
				newDominators.add(node);

				if (!currentDominators.equals(newDominators)) {
					dependencyMap.put(node, newDominators);
					changed = true;
				}
			}
		}
		return dependencyMap;
	}

	/**
	 * Produce a new ProgramGraph object, representing the reverse of the
	 * ProgramGraph given in the cfg parameter.
	 *
	 * @param pCFG The graph to be reversed
	 * @return The reverse of the {@link ProgramGraph} {@code cfg}
	 */
	ProgramGraph reverseGraph(final ProgramGraph pCFG) {
		// TODO implement graph reversal here
		final ProgramGraph reversedGraph = new ProgramGraph();
		for (Node node : pCFG.getNodes()) {
			if (node.equals(pCFG.getEntry())) {
				continue;
			}
			reversedGraph.addNode(node);
			for (Node pre : pCFG.getPredecessors(node)) {
				reversedGraph.addNode(pre);
				reversedGraph.addEdge(node, pre);
			}
		}
		return reversedGraph;
	}
}
