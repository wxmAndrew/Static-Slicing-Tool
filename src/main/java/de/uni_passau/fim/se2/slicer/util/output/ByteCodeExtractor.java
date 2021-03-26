package de.uni_passau.fim.se2.slicer.util.output;

import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import java.util.List;
import java.util.Set;

public class ByteCodeExtractor implements Extractor {

  private final Set<Node> nodes;

  public ByteCodeExtractor(final Set<Node> pNodes) {
    nodes = pNodes;
  }

  /** {@inheritDoc} */
  @Override
  public String extract() {
    final List<Node> sortedNodes = NodeSorter.sort(nodes);
    final StringBuilder builder = new StringBuilder();
    for (Node node : sortedNodes) {
      final String prettyPrint = node.prettyPrint().trim();
      final String nodeID = node.getID();
      if (nodeID.equals("\"start\"")) {
        continue;
      } else if (nodeID.matches("-?\\d+")) {
        builder.append(
            String.format(
                "(line: %4d, id: %4d)  %s\n",
                node.getLineNumber(), Integer.parseInt(nodeID), prettyPrint));
      } else {
        builder.append(
            String.format("(line: %4d, id: %s)  %s\n", node.getLineNumber(), nodeID, prettyPrint));
      }
    }
    return builder.toString();
  }
}
