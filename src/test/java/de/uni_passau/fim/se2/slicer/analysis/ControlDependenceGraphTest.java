package de.uni_passau.fim.se2.slicer.analysis;

import static org.junit.Assert.assertTrue;
import org.junit.jupiter.api.Test;
import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import de.uni_passau.fim.se2.slicer.util.cfg.ProgramGraph;

class ControlDependenceGraphTest {

	
	@Test
	void test_computeResult() {
		
	    final ProgramGraph pg = new ProgramGraph();

	    final Node Entry = new Node("Entry"); 
	    final Node n1 = new Node("n1");
	    final Node n2 = new Node("n2");
	    final Node n3 = new Node("n3");
	    final Node n5 = new Node("n5");
	    final Node n100 = new Node("n100");
	    final Node n110 = new Node("n110");
	    final Node n120 = new Node("n120");
	    final Node n130 = new Node("n130");
	    final Node n140 = new Node("n140");
	    final Node n150 = new Node("n150");
	    final Node n160 = new Node("n160");
	    final Node n170 = new Node("n170");
	    final Node n180 = new Node("n180");
	    final Node n190 = new Node("n190");
	    final Node n200 = new Node("n200");
	    final Node n210 = new Node("n210");
	    final Node n300 = new Node("n300");
	    final Node Exit = new Node("Exit");
	    
	    pg.addNode(Entry); 
	    pg.addNode(n1);
	    pg.addNode(n2);
	    pg.addNode(n3);
	    pg.addNode(n5);
	    pg.addNode(n100);
	    pg.addNode(n110);
	    pg.addNode(n120);
	    pg.addNode(n130);
	    pg.addNode(n140);
	    pg.addNode(n200);
	    pg.addNode(n210);
	    pg.addNode(n150);
	    pg.addNode(n160);
	    pg.addNode(n170);
	    pg.addNode(n180);
	    pg.addNode(n190);
	    pg.addNode(n300);
	    pg.addNode(Exit);

	    pg.addEdge(Entry, n1);
	    pg.addEdge(n1, n2);
	    pg.addEdge(n2, n3);
	    pg.addEdge(n3, n5);
	    pg.addEdge(n5, n100);
	    pg.addEdge(n100, n110);
	    pg.addEdge(n110, n120);
	    pg.addEdge(n110, n300);
	    pg.addEdge(n120, n130);
	    pg.addEdge(n130, n140);
	    pg.addEdge(n140, n150);
	    pg.addEdge(n140, n200);
	    pg.addEdge(n200, n210);
	    pg.addEdge(n210, n110);
	    pg.addEdge(n150, n160);
	    pg.addEdge(n160, n170);
	    pg.addEdge(n160, n190);
	    pg.addEdge(n170, n180);
	    pg.addEdge(n180, n190);
	    pg.addEdge(n190, n140);
	    pg.addEdge(n300, Exit);
	    
	    
	    final ProgramGraph CDG_expected = new ProgramGraph();
	    for(Node node: pg.getNodes()){
	    	CDG_expected.addNode(node);
	    }
	    
	    CDG_expected.addEdge(n110, n210);
	    CDG_expected.addEdge(n110, n200);
	    CDG_expected.addEdge(n110, n120);
	    CDG_expected.addEdge(n110, n130);
	    CDG_expected.addEdge(n110, n140);
	    CDG_expected.addEdge(n140, n150);
	    CDG_expected.addEdge(n140, n160);
	    CDG_expected.addEdge(n140, n190);
	    CDG_expected.addEdge(n160, n170);
	    CDG_expected.addEdge(n160, n180);	    

	    ControlDependenceGraph ControlDependencegraph = new ControlDependenceGraph(pg);		
		ProgramGraph CDG = ControlDependencegraph.computeResult();
		assertTrue(equalGraphs(CDG, CDG_expected));

	}

	boolean equalGraphs(ProgramGraph firstGraph, ProgramGraph secondGraph) {

		for (Node node : firstGraph.getNodes()) {
			if (!secondGraph.getNodes().contains(node)
					&& !(secondGraph.getPredecessors(node) == firstGraph.getPredecessors(node))
					&& !(secondGraph.getSuccessors(node) == firstGraph.getSuccessors(node)))
				return false;
		}

		return true;
	}
	
	

}
