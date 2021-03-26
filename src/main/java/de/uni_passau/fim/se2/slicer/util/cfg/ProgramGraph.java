package de.uni_passau.fim.se2.slicer.util.cfg;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.Var;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.objectweb.asm.tree.LineNumberNode;

/** Represents a graph structure. */
public class ProgramGraph {

  // A facade class to store graphs as DirectedMultiGraphs using the JGraphT framework.
  // Replaced the DirectedMultiGraph instance by one as described in
  // https://jgrapht.org/guide/UserOverview#graph-structures, where a DirectedMultiGraph is a graph
  // with directed edges, no self-loops, multiple edges, and no weighting.
  private final org.jgrapht.Graph<Node, DefaultEdge> graph;

  public ProgramGraph() {
    Node.sNextId = 1;
    graph =
        GraphTypeBuilder.<Node, DefaultEdge>directed()
            .allowingSelfLoops(true)
            .allowingMultipleEdges(true)
            .weighted(false)
            .edgeClass(DefaultEdge.class)
            .buildGraph();
  }

  /**
   * Adds a node to the graph.
   *
   * @param pNode The node to add
   */
  public void addNode(final Node pNode) {
    graph.addVertex(pNode);
  }

  /**
   * Adds a directed edge between two {@link Node}s to the graph.
   *
   * @param pStartNode The start node of the edge
   * @param pEndNode The end node of the edge
   */
  public void addEdge(final Node pStartNode, final Node pEndNode) {
    graph.addEdge(pStartNode, pEndNode);
  }

  /**
   * Returns the immediate predecessors of a node.
   *
   * @param pNode The node who's predecessors we're searching for
   * @return A set of node's that are predecessors to {@code a}
   */
  public Set<Node> getPredecessors(final Node pNode) {
    Set<Node> predecessors = Sets.newHashSet();
    for (DefaultEdge edge : graph.incomingEdgesOf(pNode)) {
      predecessors.add(graph.getEdgeSource(edge));
    }
    return predecessors;
  }

  /**
   * Returns the immediate successors of a node.
   *
   * @param pNode The node who's successors we're searching for
   * @return A set of node's that are successors to {@code a}
   */
  public Set<Node> getSuccessors(final Node pNode) {
    final Set<Node> successors = Sets.newHashSet();
    if (!graph.containsVertex(pNode)) {
      return successors;
    }
    for (DefaultEdge edge : graph.outgoingEdgesOf(pNode)) {
      successors.add(graph.getEdgeTarget(edge));
    }
    return successors;
  }

  /**
   * Returns all of the nodes in the graph.
   *
   * @return A set of all {@link Node}s in the graph
   */
  public Set<Node> getNodes() {
    return graph.vertexSet();
  }

  /**
   * Returns the entry node - the node with no predecessors. Assumes that there is only one such
   * node in the graph.
   *
   * @return The entry {@link Node} of the graph
   */
  public Node getEntry() {
    for (Node node : getNodes()) {
      if (graph.incomingEdgesOf(node).isEmpty()) {
        return node;
      }
    }
    return null;
  }

  /**
   * Returns the exit node - the node with no successors. Assumes that there is only one such node
   * in the graph.
   *
   * @return The exit {@link Node} of the graph
   */
  public Node getExit() {
    for (Node node : getNodes()) {
      if (graph.outgoingEdgesOf(node).isEmpty()) {
        return node;
      }
    }
    return null;
  }

  /**
   * Returns a representation of the graph in the GraphViz dot format. This can be written to a file
   * and visualised using GraphViz.
   *
   * @return A string representation of the graph in GraphViz dot format.
   */
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("digraph cfg{\n");
    for (Node node : getNodes()) {
      for (Node successor : getSuccessors(node)) {
        builder.append(node.toString()).append("->").append(successor.toString()).append("\n");
      }
    }
    builder.append("}");
    return builder.toString();
  }

  /**
   * Return all transitive successors of m - i.e. any instructions that could eventually be reached
   * from m.
   *
   * @param pNode The {@link Node} who's transitive successors we're searching for
   * @return A collection of {@link Node}s that are transitive successors to {@code m}
   */
  public Collection<Node> getTransitiveSuccessors(final Node pNode) {
    return transitiveSuccessors(pNode, Sets.newHashSet());
  }

  private Collection<Node> transitiveSuccessors(final Node pNode, final Set<Node> pDoneSet) {
    final Collection<Node> successors = Sets.newHashSet();
    for (Node node : getSuccessors(pNode)) {
      if (!pDoneSet.contains(node)) {
        successors.add(node);
        pDoneSet.add(node);
        successors.addAll(transitiveSuccessors(node, pDoneSet));
      }
    }
    return successors;
  }

  /**
   * Searches the CFG for successors of a {@link Node} until the next node containing a {@link
   * LineNumberNode} is found. These successors are returned.
   *
   * @param pNode The {@link Node} of the graph to start with
   * @return A collection of nodes that follow the {@code pNode} until a {@link LineNumberNode}
   *     follows.
   */
  public Collection<Node> getSuccessorsUntilNextLineNumber(final Node pNode) {
    final Collection<Node> successors = Sets.newHashSet();
    final Queue<Node> waitList = Lists.newLinkedList();
    waitList.offer(pNode);
    while (!waitList.isEmpty()) {
      final Node current = waitList.poll();
      for (final Node successor : getSuccessors(current)) {
        if (successor.getInstruction() instanceof LineNumberNode) {
          continue;
        }
        successors.add(successor);
        waitList.offer(successor);
      }
    }
    return successors;
  }

  /**
   * For a given pair of nodes in a DAG, return the ancestor that is common to both nodes.
   *
   * <p>Important: This operation presumes that the graph contains no cycles.
   *
   * @param pFirstNode A {@link Node}
   * @param pSecondNode Another {@link Node}
   * @return The node that is the least common ancestor of the two parameter nodes
   */
  public Node getLeastCommonAncestor(final Node pFirstNode, final Node pSecondNode) {
    @Var Node current = pFirstNode;
    while (!containsTransitiveSuccessors(current, pFirstNode, pSecondNode)) {
      current = getPredecessors(current).iterator().next();
    }
    return current;
  }

  private boolean containsTransitiveSuccessors(
      final Node pStartNode, final Node pFirstNode, final Node pSecondNode) {
    Collection<Node> transitiveSuccessors = getTransitiveSuccessors(pStartNode);
    transitiveSuccessors.add(pStartNode);
    return transitiveSuccessors.contains(pFirstNode) && transitiveSuccessors.contains(pSecondNode);
  }

}
