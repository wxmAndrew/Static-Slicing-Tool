package de.uni_passau.fim.se2.slicer.util.output;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import java.util.List;
import java.util.Set;

/** Sorts a set of nodes based on the {@link Node#getLineNumber()} value. */
class NodeSorter {

  /** Prevent initialisation of utility class */
  private NodeSorter() {}

  /**
   * Returns a list of nodes, sorted based on the ascending order of the {@link
   * Node#getLineNumber()} value.
   *
   * @param pNodes A set of {@link Node}s
   * @return The sorted list
   */
  static List<Node> sort(final Set<Node> pNodes) {
    final ListMultimap<Integer, Node> nodes = MultimapBuilder.treeKeys().arrayListValues().build();
    final List<Node> result = Lists.newArrayList();
    for (Node node : pNodes) {
      final int lineNumber = node.getLineNumber();
      nodes.put(lineNumber, node);
    }
    for (Integer lineNumber : nodes.keySet()) {
      final List<Node> nodeList = nodes.get(lineNumber);
      result.addAll(nodeList);
    }
    return result;
  }
}
