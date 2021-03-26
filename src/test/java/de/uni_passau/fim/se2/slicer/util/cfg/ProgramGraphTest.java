package de.uni_passau.fim.se2.slicer.util.cfg;

import static com.google.common.truth.Truth.assert_;

import org.junit.jupiter.api.Test;

class ProgramGraphTest {

  @Test
  void test_getLeastCommonAncestor() {
    final ProgramGraph pg = new ProgramGraph();
    final Node n1 = new Node("n1");
    final Node n2 = new Node("n2");
    final Node n3 = new Node("n3");
    pg.addNode(n1);
    pg.addNode(n2);
    pg.addNode(n3);
    pg.addEdge(n1, n2);
    pg.addEdge(n2, n3);

    final Node leastCommonAncestor = pg.getLeastCommonAncestor(n2, n3);

    assert_().that(leastCommonAncestor).isEqualTo(n2);
  }
}
