package de.uni_passau.fim.se2.slicer.analysis;

import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import de.uni_passau.fim.se2.slicer.util.cfg.ProgramGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import com.google.common.collect.Sets;

import br.usp.each.saeg.asm.defuse.Variable;

/** Provides an analysis that calculates the program-dependence graph. */
public class ProgramDependenceGraph extends Analysis {
	public ProgramDependenceGraph(final ClassNode pClassNode, final MethodNode pMethodNode) {
		super(pClassNode, pMethodNode);
	}
	
	/**
	 * Return a graph representing the Program Dependence ProgramGraph of the
	 * control flow graph, which is stored in the controlFlowProgramGraph class
	 * attribute (this is inherited from the Analysis class).
	 *
	 * <p>
	 * You may wish to use the class that computes the Control Dependence Tree to
	 * obtain the control dependences, and may wish to use the code in the
	 * {@link DataFlowAnalysis} class to obtain the data dependences.
	 *
	 * @return A graph representing the program-dependence graph of the control-flow
	 *         graph
	 */
	public ProgramGraph computeResult() {
		// TODO implement computation of program-dependence graph here

		ControlDependenceGraph CDG = new ControlDependenceGraph(classNode, methodNode);
		ProgramGraph prgramDependencyGraph = CDG.computeResult();
		ProgramGraph dataDependenceGraph = computeDataDependenceGraph();
		for (Node node : CDG.computeResult().getNodes()) {
			Set<Node> predecessors = dataDependenceGraph.getPredecessors(node);
			Set<Node> successors = dataDependenceGraph.getSuccessors(node);
			for (Node pre : predecessors) {
				prgramDependencyGraph.addEdge(pre, node);
			}
			for (Node suc : successors) {
				prgramDependencyGraph.addEdge(node, suc);
			}
		}
		return prgramDependencyGraph;
	}

	/**
	 * Compute the set of nodes that belong to a backward slice, computed from a
	 * given node in the program dependence graph.
	 *
	 * @param pNode The {@link Node} to start the backward slice
	 * @return A set of nodes that represent the backward slice
	 */

	public Set<Node> backwardSlice(final Node pNode) {
		// TODO implement backward slicing here
		ProgramGraph reversedGraph = reverseGraph(computeResult());
		Set<Node> slicedNodes = (Set<Node>) reversedGraph.getTransitiveSuccessors(pNode);
		slicedNodes.add(pNode);
		return slicedNodes;
	}

	/**
	 * initialize the dataGraphDependency for each node of CFG we first check if it
	 * has variables, if so we get the use variables in this node we get all the
	 * predecessors of the node filter the predecessors to get only the nodes with a
	 * definition of the use variable get the reaching definitions from all the
	 * definitions
	 * 
	 * @return dataDependenceGraph the graph representing the data-dependence graph
	 *         of the control-flow graph
	 */

	public ProgramGraph computeDataDependenceGraph() {

		final ProgramGraph dataDependenceGraph = new ProgramGraph();
		for (Node node : controlFlowProgramGraph.getNodes()) {
			dataDependenceGraph.addNode(node);
		}
		
		for (Node node : controlFlowProgramGraph.getNodes()) {
			if (node.equals(controlFlowProgramGraph.getEntry()) || node.equals(controlFlowProgramGraph.getExit()))
				continue;
			try {
				Set<Variable> UseVariables = (Set<Variable>) DataFlowAnalysis.usedBy(super.classNode.toString(),
						super.methodNode, node.getInstruction());
				if (UseVariables.isEmpty())
					continue;
				Set<Node> predecessorsNodes = computePredecessors(node);
				for (Variable useVar : UseVariables) {
					Set<Node> reachedDefinitions = Sets.newHashSet();
					Set<Node> definitionsList = Sets.newHashSet();
					for (Node pre : predecessorsNodes) {
						if (pre.equals(controlFlowProgramGraph.getEntry()))
							continue;
						if (!(DataFlowAnalysis
								.definedBy(super.classNode.toString(), super.methodNode, pre.getInstruction())
								.isEmpty())
								&& (DataFlowAnalysis
										.definedBy(super.classNode.toString(), super.methodNode, pre.getInstruction())
										.contains(useVar))) {
							definitionsList.add(pre);
						}
					}
					reachedDefinitions = computeReachingDefinitions(definitionsList, node);

					for (Node reachedDef : reachedDefinitions) {
						dataDependenceGraph.addEdge(reachedDef, node);
					}
				}
			}

			catch (AnalyzerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return dataDependenceGraph;
	}

	/**
	 * Compute the set of reaching definitions of a variable used in a node
	 * 
	 * @param definitionsList Set of all definition of a use variable
	 * @param node            The node contains the use variable
	 * @return reachedDefinitions A set of nodes of reaching definitions
	 */

	private Set<Node> computeReachingDefinitions(Set<Node> definitionsList, Node node) {

		Set<Node> reachedDefinitions = Sets.newHashSet();
		if (definitionsList.size() == 1) {
			reachedDefinitions.add(definitionsList.iterator().next());
		} else {
			for (Node def1 : definitionsList) {
				for (Node def2 : definitionsList) {
					if (def1.equals(def2) || containsTransitiveSuccessors(def1, def2, node)) {
						continue;
					} else {
						reachedDefinitions.add(def1);
					}
				}
			}
		}
		return reachedDefinitions;
	}

	/**
	 * Compute the set of all predecessors of a node
	 * 
	 * @param node The node to calculate the predecessors for.
	 * @return predecessorsNodes A set of nodes of all predecessors
	 */

	public Set<Node> computePredecessors(Node node) {
		ProgramGraph reversedGraph = reverseGraph(controlFlowProgramGraph);
		Set<Node> predecessorsNodes = (Set<Node>) reversedGraph.getTransitiveSuccessors(node);

		return predecessorsNodes;

	}

	/**
	 * Reveres the target graph
	 * 
	 * @param graph the graph to be traversed
	 * @return reversedGraph the traversed graph
	 */

	 ProgramGraph reverseGraph(final ProgramGraph graph) {

		final ProgramGraph reversedGraph = new ProgramGraph();
		for (Node node : graph.getNodes()) {
			if (node.equals(graph.getEntry())) {
				continue;
			}
			reversedGraph.addNode(node);
			for (Node pre : graph.getPredecessors(node)) {
				reversedGraph.addNode(pre);
				reversedGraph.addEdge(node, pre);
			}
		}
		return reversedGraph;
	}

	/**
	 * check if there a node between tow nodes
	 * 
	 * @param pStartNode  the start node
	 * @param pFirstNode  the node in between
	 * @param pSecondNode the end node
	 * @return true if the first node is in between and false if not
	 */

	private boolean containsTransitiveSuccessors(final Node pStartNode, final Node pFirstNode, final Node pSecondNode) {

		Collection<Node> transitiveSuccessors = controlFlowProgramGraph.getTransitiveSuccessors(pStartNode);
		transitiveSuccessors.add(pStartNode);
		boolean not_loop = false;
		if (pStartNode.getLineNumber() < pFirstNode.getLineNumber()
				&& pFirstNode.getLineNumber() < pSecondNode.getLineNumber())
			not_loop = true;
		return transitiveSuccessors.contains(pFirstNode) && transitiveSuccessors.contains(pSecondNode) && not_loop;
	}

}
