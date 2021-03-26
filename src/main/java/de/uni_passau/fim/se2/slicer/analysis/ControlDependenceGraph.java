package de.uni_passau.fim.se2.slicer.analysis;

import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import de.uni_passau.fim.se2.slicer.util.cfg.ProgramGraph;

import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Sets;

/** Provides an analysis that implements a control-dependence tree */
public class ControlDependenceGraph extends Analysis {

	ControlDependenceGraph(final ClassNode pClassNode, final MethodNode pMethodNode) {
		super(pClassNode, pMethodNode);
	}
	
	
	ControlDependenceGraph(ProgramGraph pCFG) {
		super(pCFG);
	}

	/**
	 * Return a graph representing the control dependence graph of the control flow
	 * graph, which is stored in the controlFlowProgramGraph class attribute (this
	 * is inherited from the Analysis class).
	 *
	 * <p>
	 * You may wish to use the post dominator graph code you implement to support
	 * computing the Control Dependence ProgramGraph.
	 *
	 * @return The graph that is the resulting control-dependence graph
	 */
	public ProgramGraph computeResult() {
		// TODO implement computation of control-dependence graph here
		
		PostDominatorTree PostDominatorGraph = new PostDominatorTree(controlFlowProgramGraph);
		ProgramGraph PDT = PostDominatorGraph.computeResult();
		ProgramGraph controlDependenceGraph = new ProgramGraph();
		Set<Node> setOfAlldependentNodes = Sets.newHashSet();
		
		for(Node node: controlFlowProgramGraph.getNodes()) {
			controlDependenceGraph.addNode(node);
		}
		
		// loop through all nodes, get successors and check if this node is a transitive
		// successor of that successor in PDT
		for (Node node : controlFlowProgramGraph.getNodes()) {
			Set<Node> setOfdependentNodes = Sets.newHashSet();
			for (Node sucessor : controlFlowProgramGraph.getSuccessors(node)) {
				if (!PDT.getTransitiveSuccessors(sucessor).contains(node)) { // check if B is not an ancestor of A
					Node leastCommonAncestor = PDT.getLeastCommonAncestor(node, sucessor);
					Node current = sucessor;
					while (!current.equals(leastCommonAncestor)) { // Traverse backward
						setOfdependentNodes.add(current);
						setOfAlldependentNodes.add(current);
						current = PDT.getPredecessors(current).iterator().next(); // the set suppose to have only one //
																					// element which is the parent
					}
					if (leastCommonAncestor.equals(node)) {
						setOfdependentNodes.add(leastCommonAncestor);
						setOfAlldependentNodes.add(leastCommonAncestor);
					}
					for (Node seondNode : setOfdependentNodes) {
						controlDependenceGraph.addEdge(node, seondNode);
					}
				}
			}
		}
	
		return controlDependenceGraph;
	}
}
