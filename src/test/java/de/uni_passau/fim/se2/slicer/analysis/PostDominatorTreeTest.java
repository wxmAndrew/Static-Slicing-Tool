package de.uni_passau.fim.se2.slicer.analysis;

import static com.google.common.truth.Truth.assert_;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import de.uni_passau.fim.se2.slicer.util.cfg.ProgramGraph;

class PostDominatorTreeTest {

	@Test
	void test_reverseGraph() {
		final ProgramGraph pg = new ProgramGraph();
		final Node n1 = new Node("n1");
		final Node n2 = new Node("n2");
		final Node n3 = new Node("n3");
		final Node n4 = new Node("n4");

		pg.addNode(n1);
		pg.addNode(n2);
		pg.addNode(n3);
		pg.addNode(n4);
		pg.addEdge(n1, n2);
		pg.addEdge(n2, n3);
		pg.addEdge(n3, n4);

		final ProgramGraph pg_reversed_expected = new ProgramGraph();

		pg_reversed_expected.addNode(n1);
		pg_reversed_expected.addNode(n2);
		pg_reversed_expected.addNode(n3);
		pg_reversed_expected.addNode(n4);
		pg_reversed_expected.addEdge(n2, n1);
		pg_reversed_expected.addEdge(n3, n2);
		pg_reversed_expected.addEdge(n4, n3);

		PostDominatorTree PostDominatorGraph = new PostDominatorTree(pg);
		final ProgramGraph pg_reversed = PostDominatorGraph.reverseGraph(pg);
		assertTrue(equalGraphs(pg_reversed_expected, pg_reversed));

	}

	@Test
	void test_computeDominance() {

		final ProgramGraph pg = new ProgramGraph();

		final Node n1 = new Node("n1");
		final Node n2 = new Node("n2");
		final Node n3 = new Node("n3");
		final Node n4 = new Node("n4");
		final Node n5 = new Node("n5");
		final Node n6 = new Node("n6");
		final Node n7 = new Node("n7");
		final Node n8 = new Node("n8");
		final Node n9 = new Node("n9");
		final Node n10 = new Node("n10");
		final Node Exit = new Node("Exit");

		pg.addNode(n1);
		pg.addNode(n2);
		pg.addNode(n3);
		pg.addNode(n4);
		pg.addNode(n5);
		pg.addNode(n6);
		pg.addNode(n7);
		pg.addNode(n8);
		pg.addNode(n9);
		pg.addNode(n10);
		pg.addNode(Exit);

		pg.addEdge(n1, n2);
		pg.addEdge(n1, n3);
		pg.addEdge(n2, n3);
		pg.addEdge(n3, n4);
		pg.addEdge(n4, n5);
		pg.addEdge(n4, n6);
		pg.addEdge(n5, n7);
		pg.addEdge(n6, n7);
		pg.addEdge(n7, n8);
		pg.addEdge(n8, n9);
		pg.addEdge(n8, n10);
		pg.addEdge(n9, n1);
		pg.addEdge(n10, Exit);
		pg.addEdge(n10, n7);
		pg.addEdge(n8, n3);
		pg.addEdge(n4, n3);
		pg.addEdge(n7, n4);

		Map<Node, Set<Node>> dependencyMap_expected = Maps.newHashMap();

		Set<Node> set = Sets.newHashSet();
		set.add(n1);
		set.add(n3);
		set.add(n4);
		set.add(n7);
		set.add(n8);
		set.add(n10);
		set.add(Exit);
		dependencyMap_expected.put(n1, set);

		set = Sets.newHashSet();
		set.add(n2);
		set.add(n3);
		set.add(n4);
		set.add(n7);
		set.add(n8);
		set.add(n10);
		set.add(Exit);
		dependencyMap_expected.put(n2, set);

		set = Sets.newHashSet();
		set.add(n3);
		set.add(n4);
		set.add(n7);
		set.add(n8);
		set.add(n10);
		set.add(Exit);
		dependencyMap_expected.put(n3, set);

		set = Sets.newHashSet();
		set.add(n4);
		set.add(n7);
		set.add(n8);
		set.add(n10);
		set.add(Exit);
		dependencyMap_expected.put(n4, set);

		set = Sets.newHashSet();
		set.add(n5);
		set.add(n7);
		set.add(n8);
		set.add(n10);
		set.add(Exit);
		dependencyMap_expected.put(n5, set);

		set = Sets.newHashSet();
		set.add(n6);
		set.add(n7);
		set.add(n8);
		set.add(n10);
		set.add(Exit);
		dependencyMap_expected.put(n6, set);

		set = Sets.newHashSet();
		set.add(n7);
		set.add(n8);
		set.add(n10);
		set.add(Exit);
		dependencyMap_expected.put(n7, set);

		set = Sets.newHashSet();
		set.add(n8);
		set.add(n10);
		set.add(Exit);
		dependencyMap_expected.put(n8, set);

		set = Sets.newHashSet();
		set.add(n1);
		set.add(n3);
		set.add(n4);
		set.add(n7);
		set.add(n8);
		set.add(n9);
		set.add(n10);
		set.add(Exit);
		dependencyMap_expected.put(n9, set);

		set = Sets.newHashSet();
		set.add(n10);
		set.add(Exit);
		dependencyMap_expected.put(n10, set);

		set = Sets.newHashSet();
		set.add(Exit);
		dependencyMap_expected.put(Exit, set);

		PostDominatorTree PostDominatorGraph = new PostDominatorTree(pg);
		Map<Node, Set<Node>> dependencyMap = PostDominatorGraph.computeDominance();
		assert_().that(dependencyMap_expected).isEqualTo(dependencyMap);

	}

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

		final ProgramGraph PDT_expected = new ProgramGraph();
		for (Node node : pg.getNodes()) {
			PDT_expected.addNode(node);
		}

		PDT_expected.addEdge(Exit, n300);
		PDT_expected.addEdge(n300, n110);
		PDT_expected.addEdge(n110, n210);
		PDT_expected.addEdge(n110, n100);
		PDT_expected.addEdge(n100, n5);
		PDT_expected.addEdge(n5, n3);
		PDT_expected.addEdge(n3, n2);
		PDT_expected.addEdge(n2, n1);
		PDT_expected.addEdge(n1, Entry);
		PDT_expected.addEdge(n210, n200);
		PDT_expected.addEdge(n200, n140);
		PDT_expected.addEdge(n140, n130);
		PDT_expected.addEdge(n130, n120);
		PDT_expected.addEdge(n140, n190);
		PDT_expected.addEdge(n190, n160);
		PDT_expected.addEdge(n160, n150);
		PDT_expected.addEdge(n190, n180);
		PDT_expected.addEdge(n180, n170);

		PostDominatorTree PostDominatorGraph = new PostDominatorTree(pg);
		ProgramGraph PDT = PostDominatorGraph.computeResult();
		assertTrue(equalGraphs(PDT, PDT_expected));

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
