package de.uni_passau.fim.se2.slicer.util.output;

import de.uni_passau.fim.se2.slicer.util.cfg.Node;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class XMLFileExtractor implements Extractor {

  private final Set<Node> nodes;

  public XMLFileExtractor(final Set<Node> pNodes) {
    nodes = pNodes;
  }

  @Override
  public String extract() throws IOException {
    final List<Node> sortedNodes = NodeSorter.sort(nodes);
    final StringBuilder builder = new StringBuilder();
    builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    builder.append("<report>\n");

    for (Node node : sortedNodes) {
      final String prettyPrint = node.prettyPrint().trim();
      final String nodeID = node.getID();
      if (nodeID.equals("\"start\"")) {
        continue;
      } else if (nodeID.matches("-?\\d+")) {
        builder.append(
            String.format(
                "  <line nr=\"%d\" id=\"%d\" instruction=\"%s\"/>\n",
                node.getLineNumber(), Integer.parseInt(nodeID), prettyPrint));
      } else {
        builder.append(
            String.format(
                "  <line nr=\"%d\" id=\"%s\" instruction=\"%s\"/>\n",
                node.getLineNumber(), nodeID, prettyPrint));
      }
    }

    builder.append("</report>\n");
    return builder.toString();
  }
}
